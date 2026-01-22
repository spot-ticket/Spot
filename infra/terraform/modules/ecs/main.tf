# =============================================================================
# Cloud Map (Service Discovery)
# =============================================================================
resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "${var.project}.local"
  vpc  = var.vpc_id
}

resource "aws_service_discovery_service" "backend" {
  name = "backend"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id
    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# =============================================================================
# ECS Cluster
# =============================================================================
resource "aws_ecs_cluster" "main" {
  name = "${var.name_prefix}-cluster"

  service_connect_defaults {
    namespace = aws_service_discovery_private_dns_namespace.main.arn
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-cluster" })
}

# =============================================================================
# ECS Security Group
# =============================================================================
resource "aws_security_group" "api_sg" {
  name   = "${var.name_prefix}-api-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-api-sg" })
}

# =============================================================================
# IAM Role for ECS Task Execution
# =============================================================================
resource "aws_iam_role" "ecs_task_execution_role" {
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

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# =============================================================================
# CloudWatch Log Group
# =============================================================================
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/${var.project}-backend"
  retention_in_days = var.log_retention_days

  tags = var.common_tags
}

# =============================================================================
# ECS Task Definition
# =============================================================================
resource "aws_ecs_task_definition" "backend" {
  family                   = "${var.project}-backend-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "${var.project}-backend-container"
      image     = "${var.ecr_repository_url}:latest"
      essential = true
      portMappings = [{
        name          = "http"
        containerPort = var.container_port
        hostPort      = var.container_port
      }]
      environment = [
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${var.db_endpoint}/${var.db_name}"
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.db_username
        },
        {
          name  = "SPRING_DATASOURCE_PASSWORD"
          value = var.db_password
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.common_tags
}

# =============================================================================
# ECS Service
# =============================================================================
resource "aws_ecs_service" "main" {
  name            = "${var.project}-backend-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = "${var.project}-backend-container"
    container_port   = var.container_port
  }

  network_configuration {
    subnets          = var.subnet_ids
    security_groups  = [aws_security_group.api_sg.id]
    assign_public_ip = var.assign_public_ip
  }

  depends_on = [var.alb_listener_arn]

  tags = var.common_tags
}
