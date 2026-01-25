# =============================================================================
# Cloud Map (Service Discovery Namespace)
# =============================================================================
resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "${var.project}.local"
  vpc  = var.vpc_id

  tags = merge(var.common_tags, { Name = "${var.project}.local" })
}

# =============================================================================
# Cloud Map Services (Multiple) - Only when Service Connect is disabled
# =============================================================================
resource "aws_service_discovery_service" "services" {
  for_each = var.enable_service_connect ? {} : var.services

  name = each.key

  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.main.id
    routing_policy = "MULTIVALUE"

    dns_records {
      ttl  = 10
      type = "A"
    }
    dns_records {
      ttl  = 10
      type = "SRV"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = merge(var.common_tags, { Service = each.key })
}

# =============================================================================
# ECS Cluster (Single shared cluster)
# =============================================================================
resource "aws_ecs_cluster" "main" {
  name = "${var.name_prefix}-cluster"

  service_connect_defaults {
    namespace = aws_service_discovery_private_dns_namespace.main.arn
  }

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-cluster" })
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    base              = 1
    weight            = 100
    capacity_provider = "FARGATE"
  }
}

# =============================================================================
# MSA Security Group (Shared by all services)
# =============================================================================
resource "aws_security_group" "msa_sg" {
  name        = "${var.name_prefix}-msa-sg"
  description = "Security group for MSA services"
  vpc_id      = var.vpc_id

  # ALB에서 들어오는 트래픽 허용 (Gateway: 8080)
  ingress {
    description     = "Traffic from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  # Service Connect를 위한 자기 참조 규칙 (서비스 간 통신: 8080-8084)
  ingress {
    description = "Inter-service communication"
    from_port   = 8080
    to_port     = 8084
    protocol    = "tcp"
    self        = true
  }

  # Service Connect proxy port
  ingress {
    description = "Service Connect proxy"
    from_port   = 15000
    to_port     = 15001
    protocol    = "tcp"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-msa-sg" })
}

# =============================================================================
# IAM Role for ECS Task Execution
# =============================================================================
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.name_prefix}-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })

  tags = var.common_tags
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# =============================================================================
# SSM Parameter Store 읽기 권한 (Secrets 주입용)
# =============================================================================
resource "aws_iam_role_policy" "ecs_task_execution_ssm" {
  name = "${var.name_prefix}-ecs-ssm-policy"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameters",
          "ssm:GetParameter"
        ]
        Resource = "arn:aws:ssm:${var.region}:*:parameter/${var.project}/${var.environment}/*"
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "kms:ViaService" = "ssm.${var.region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

# =============================================================================
# IAM Role for ECS Task (Application level)
# =============================================================================
resource "aws_iam_role" "ecs_task_role" {
  name = "${var.name_prefix}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })

  tags = var.common_tags
}

# Service Connect requires CloudWatch permissions
resource "aws_iam_role_policy" "ecs_service_connect" {
  name = "${var.name_prefix}-service-connect-policy"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "*"
      }
    ]
  })
}

# =============================================================================
# CloudWatch Log Groups (per service)
# =============================================================================
resource "aws_cloudwatch_log_group" "services" {
  for_each = var.services

  name              = "/ecs/${var.project}-${each.key}"
  retention_in_days = var.log_retention_days

  tags = merge(var.common_tags, { Service = each.key })
}

# =============================================================================
# ECS Task Definitions (per service)
# =============================================================================
resource "aws_ecs_task_definition" "services" {
  for_each = var.services

  family                   = "${var.project}-${each.key}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "${var.project}-${each.key}-container"
      image     = "${var.ecr_repository_urls[each.key]}:latest"
      essential = true

      portMappings = [{
        name          = each.key
        containerPort = each.value.container_port
        hostPort      = each.value.container_port
        protocol      = "tcp"
        appProtocol   = "http"
      }]

      environment = concat(
        # 공통 환경 변수
        [
          {
            name  = "SPRING_PROFILES_ACTIVE"
            value = var.environment
          },
          {
            name  = "SPRING_DATA_REDIS_HOST"
            value = var.redis_endpoint
          },
          {
            name  = "SPRING_DATA_REDIS_PORT"
            value = "6379"
          }
        ],
        # Kafka 환경 변수 (gateway 제외)
        each.key != "gateway" && var.kafka_bootstrap_servers != "" ? [
          {
            name  = "SPRING_KAFKA_BOOTSTRAP_SERVERS"
            value = var.kafka_bootstrap_servers
          },
          {
            name  = "SPRING_KAFKA_CONSUMER_GROUP_ID"
            value = "${var.project}-${each.key}"
          },
          {
            name  = "SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET"
            value = "earliest"
          }
        ] : [],
        # 백엔드 서비스 전용 (gateway 제외) - DB, JPA, JWT 설정
        each.key != "gateway" ? [
          {
            name  = "SPRING_DATASOURCE_URL"
            value = "jdbc:postgresql://${var.db_endpoint}/${var.db_name}?currentSchema=${lookup(each.value.environment_vars, "DB_SCHEMA", each.key)}"
          },
          {
            name  = "SPRING_DATASOURCE_USERNAME"
            value = var.db_username
          },
          {
            name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
            value = "update"
          },
          {
            name  = "SPRING_JPA_SHOW_SQL"
            value = "false"
          },
          {
            name  = "SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"
            value = "org.hibernate.dialect.PostgreSQLDialect"
          },
          {
            name  = "SPRING_JWT_EXPIRE_MS"
            value = tostring(var.jwt_expire_ms)
          },
          {
            name  = "SPRING_SECURITY_REFRESH_TOKEN_EXPIRE_DAYS"
            value = tostring(var.refresh_token_expire_days)
          },
          {
            name  = "SERVICE_ACTIVE_REGIONS"
            value = var.service_active_regions
          }
        ] : [],
        # Service Discovery 환경 변수 (Feign Client URLs)
        each.key != "gateway" ? [
          {
            name  = "FEIGN_ORDER_URL"
            value = "http://order.${var.project}.local:${var.services["order"].container_port}"
          },
          {
            name  = "FEIGN_PAYMENT_URL"
            value = "http://payment.${var.project}.local:${var.services["payment"].container_port}"
          },
          {
            name  = "FEIGN_STORE_URL"
            value = "http://store.${var.project}.local:${var.services["store"].container_port}"
          },
          {
            name  = "FEIGN_USER_URL"
            value = "http://user.${var.project}.local:${var.services["user"].container_port}"
          }
        ] : [],
        # Mail 설정 (user 서비스용)
        each.key == "user" ? [
          {
            name  = "SPRING_MAIL_HOST"
            value = var.mail_host
          },
          {
            name  = "SPRING_MAIL_PORT"
            value = tostring(var.mail_port)
          },
          {
            name  = "SPRING_MAIL_USERNAME"
            value = var.mail_username
          },
          {
            name  = "SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH"
            value = "true"
          },
          {
            name  = "SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE"
            value = "true"
          }
        ] : [],
        # Toss 결제 설정 (payment 서비스용)
        each.key == "payment" ? [
          {
            name  = "TOSS_PAYMENTS_BASE_URL"
            value = var.toss_base_url
          },
          {
            name  = "TOSS_PAYMENTS_CUSTOMER_KEY"
            value = var.toss_customer_key
          }
        ] : [],
        # Gateway 전용 설정 - Spring Cloud Gateway 라우트 (WebFlux 버전용 새 property 이름)
        each.key == "gateway" ? [
          # User Service - Auth 관련
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_0_ID"
            value = "user-login"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_0_URI"
            value = "http://user.${var.project}.local:${var.services["user"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_0_PREDICATES_0"
            value = "Path=/api/login"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_1_ID"
            value = "user-join"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_1_URI"
            value = "http://user.${var.project}.local:${var.services["user"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_1_PREDICATES_0"
            value = "Path=/api/join"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_2_ID"
            value = "user-auth"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_2_URI"
            value = "http://user.${var.project}.local:${var.services["user"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_2_PREDICATES_0"
            value = "Path=/api/auth/**"
          },
          # User Service - Users & Admin
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_3_ID"
            value = "user-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_3_URI"
            value = "http://user.${var.project}.local:${var.services["user"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_3_PREDICATES_0"
            value = "Path=/api/users/**"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_4_ID"
            value = "admin-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_4_URI"
            value = "http://user.${var.project}.local:${var.services["user"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_4_PREDICATES_0"
            value = "Path=/api/admin/**"
          },
          # Store Service
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_5_ID"
            value = "store-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_5_URI"
            value = "http://store.${var.project}.local:${var.services["store"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_5_PREDICATES_0"
            value = "Path=/api/stores/**"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_6_ID"
            value = "category-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_6_URI"
            value = "http://store.${var.project}.local:${var.services["store"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_6_PREDICATES_0"
            value = "Path=/api/categories/**"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_7_ID"
            value = "review-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_7_URI"
            value = "http://store.${var.project}.local:${var.services["store"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_7_PREDICATES_0"
            value = "Path=/api/reviews/**"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_8_ID"
            value = "menu-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_8_URI"
            value = "http://store.${var.project}.local:${var.services["store"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_8_PREDICATES_0"
            value = "Path=/api/menus/**"
          },
          # Order Service
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_9_ID"
            value = "order-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_9_URI"
            value = "http://order.${var.project}.local:${var.services["order"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_9_PREDICATES_0"
            value = "Path=/api/orders/**"
          },
          # Payment Service
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_10_ID"
            value = "payment-service"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_10_URI"
            value = "http://payment.${var.project}.local:${var.services["payment"].container_port}"
          },
          {
            name  = "SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_10_PREDICATES_0"
            value = "Path=/api/payments/**"
          },
          # Actuator 설정 (새 property 이름)
          {
            name  = "MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE"
            value = "health,info,gateway"
          },
          {
            name  = "MANAGEMENT_ENDPOINT_GATEWAY_ACCESS"
            value = "unrestricted"
          },
          # 디버깅용 로깅
          {
            name  = "LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY"
            value = "DEBUG"
          }
        ] : [],
        # 서비스별 커스텀 환경 변수
        [for k, v in each.value.environment_vars : {
          name  = k
          value = v
        }]
      )

      # =============================================================
      # Secrets (Parameter Store에서 주입)
      # =============================================================
      secrets = concat(
        # 백엔드 서비스 (gateway 제외) - DB 비밀번호, JWT 시크릿
        each.key != "gateway" ? [
          {
            name      = "SPRING_DATASOURCE_PASSWORD"
            valueFrom = var.parameter_arns.db_password
          },
          {
            name      = "SPRING_JWT_SECRET"
            valueFrom = var.parameter_arns.jwt_secret
          }
        ] : [],
        # Mail 비밀번호 (user 서비스)
        each.key == "user" && var.parameter_arns.mail_password != null ? [
          {
            name      = "SPRING_MAIL_PASSWORD"
            valueFrom = var.parameter_arns.mail_password
          }
        ] : [],
        # Toss 시크릿 키 (payment 서비스)
        each.key == "payment" && var.parameter_arns.toss_secret_key != null ? [
          {
            name      = "TOSS_PAYMENTS_SECRET_KEY"
            valueFrom = var.parameter_arns.toss_secret_key
          }
        ] : []
      )

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services[each.key].name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:${each.value.container_port}${each.value.health_check_path} || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = merge(var.common_tags, { Service = each.key })
}

# =============================================================================
# ECS Services (per service)
# =============================================================================
resource "aws_ecs_service" "services" {
  for_each = var.services

  name            = "${var.project}-${each.key}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.services[each.key].arn
  desired_count   = var.standby_mode ? 0 : each.value.desired_count
  launch_type     = "FARGATE"

  dynamic "load_balancer" {
    for_each = each.key == "gateway" ? [1] : []
    content {
      target_group_arn = var.target_group_arns[each.key]
      container_name   = "${var.project}-${each.key}-container"
      container_port   = each.value.container_port
    }
  }

  network_configuration {
    subnets          = var.subnet_ids
    security_groups  = [aws_security_group.msa_sg.id]
    assign_public_ip = var.assign_public_ip
  }

  # Service Connect Configuration
  dynamic "service_connect_configuration" {
    for_each = var.enable_service_connect ? [1] : []
    content {
      enabled   = true
      namespace = aws_service_discovery_private_dns_namespace.main.arn

      service {
        port_name      = each.key
        discovery_name = each.key

        client_alias {
          port     = each.value.container_port
          dns_name = "${each.key}.${var.project}.local"
        }
      }

      log_configuration {
        log_driver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services[each.key].name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "service-connect"
        }
      }
    }
  }

  # Service Discovery Registration (only when Service Connect is disabled)
  dynamic "service_registries" {
    for_each = var.enable_service_connect ? [] : [1]
    content {
      registry_arn   = aws_service_discovery_service.services[each.key].arn
      container_name = "${var.project}-${each.key}-container"
      container_port = each.value.container_port
    }
  }

  depends_on = [var.alb_listener_arn]

  tags = merge(var.common_tags, { Service = each.key })
}
