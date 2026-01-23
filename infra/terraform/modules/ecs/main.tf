# =============================================================================
# Cloud Map (Service Discovery Namespace)
# =============================================================================
resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "${var.project}.local"
  vpc  = var.vpc_id

  tags = merge(var.common_tags, { Name = "${var.project}.local" })
}

# =============================================================================
# Cloud Map Services (Multiple)
# =============================================================================
resource "aws_service_discovery_service" "services" {
  for_each = var.services

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

  # ALB에서 들어오는 트래픽 허용
  ingress {
    description     = "Traffic from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  # Service Connect를 위한 자기 참조 규칙 (서비스 간 통신)
  ingress {
    description = "Inter-service communication"
    from_port   = 8080
    to_port     = 8080
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
            name  = "SPRING_DATASOURCE_URL"
            value = "jdbc:postgresql://${var.db_endpoint}/${var.db_name}?currentSchema=${lookup(each.value.environment_vars, "DB_SCHEMA", each.key)}"
          },
          {
            name  = "SPRING_DATASOURCE_USERNAME"
            value = var.db_username
          },
          {
            name  = "SPRING_DATASOURCE_PASSWORD"
            value = var.db_password
          },
          {
            name  = "SPRING_DATA_REDIS_HOST"
            value = var.redis_endpoint
          },
          {
            name  = "SPRING_DATA_REDIS_PORT"
            value = "6379"
          },
          # Service Discovery 환경 변수 (다른 서비스 URL)
          {
            name  = "ORDER_SERVICE_URL"
            value = "http://order.${var.project}.local:8080"
          },
          {
            name  = "PAYMENT_SERVICE_URL"
            value = "http://payment.${var.project}.local:8080"
          },
          {
            name  = "STORE_SERVICE_URL"
            value = "http://store.${var.project}.local:8080"
          },
          {
            name  = "USER_SERVICE_URL"
            value = "http://user.${var.project}.local:8080"
          }
        ],
        # 서비스별 커스텀 환경 변수
        [for k, v in each.value.environment_vars : {
          name  = k
          value = v
        }]
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
  desired_count   = each.value.desired_count
  launch_type     = "FARGATE"

  load_balancer {
    target_group_arn = var.target_group_arns[each.key]
    container_name   = "${var.project}-${each.key}-container"
    container_port   = each.value.container_port
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

  # Service Discovery Registration
  service_registries {
    registry_arn   = aws_service_discovery_service.services[each.key].arn
    container_name = "${var.project}-${each.key}-container"
    container_port = each.value.container_port
  }

  depends_on = [var.alb_listener_arn]

  tags = merge(var.common_tags, { Service = each.key })

  lifecycle {
    ignore_changes = [desired_count]
  }
}
