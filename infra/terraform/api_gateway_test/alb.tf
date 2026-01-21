// *** //
// ALB //
// *** //
resource "aws_lb" "spot" {
  name               = "spot-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = [aws_subnet.public_a.id, aws_subnet.public_c.id]

  enable_deletion_protection = var.environment == "prod" ? true : false

  access_logs {
    bucket  = aws_s3_bucket.alb_logs.id
    prefix  = "spot-alb"
    enabled = true
  }

  tags = {
    Name        = "spot-alb"
    Environment = var.environment
  }
}

// ************* //
// S3 for ALB Logs //
// ************* //
resource "aws_s3_bucket" "alb_logs" {
  bucket = "spot-alb-logs-${data.aws_caller_identity.current.account_id}"

  tags = {
    Name        = "spot-alb-logs"
    Environment = var.environment
  }
}

resource "aws_s3_bucket_policy" "alb_logs" {
  bucket = aws_s3_bucket.alb_logs.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::600734575887:root" # ap-northeast-2 ELB account
        }
        Action   = "s3:PutObject"
        Resource = "${aws_s3_bucket.alb_logs.arn}/spot-alb/*"
      }
    ]
  })
}

data "aws_caller_identity" "current" {}

// **************** //
// ACM Certificate  //
// **************** //
resource "aws_acm_certificate" "spot" {
  domain_name       = "spotorder.org"
  validation_method = "DNS"

  subject_alternative_names = [
    "*.spotorder.org"
  ]

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name        = "spot-acm-cert"
    Environment = var.environment
  }
}

// ************** //
// Target Groups  //
// ************** //
resource "aws_lb_target_group" "api_gateway" {
  name        = "spot-api-gateway-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    protocol            = "HTTP"
    matcher             = "200"
  }

  tags = {
    Name = "spot-api-gateway-tg"
  }
}

# HTTPS 리스너 (ACM 인증서 사용)
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.spot.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = aws_acm_certificate.spot.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway.arn
  }
}

# HTTP → HTTPS 리다이렉트
resource "aws_lb_listener" "http_redirect" {
  load_balancer_arn = aws_lb.spot.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# User Service - Blue
resource "aws_lb_target_group" "user_blue" {
  name        = "spot-user-blue-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    protocol            = "HTTP"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name    = "user-blue-tg"
    Service = "user"
  }
}

# User Service - Green
resource "aws_lb_target_group" "user_green" {
  name        = "spot-user-green-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    protocol            = "HTTP"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name    = "user-green-tg"
    Service = "user"
  }
}

# Order, Store, Payment도 동일하게 Blue/Green 생성
