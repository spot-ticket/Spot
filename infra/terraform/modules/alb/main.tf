# =============================================================================
# ALB Security Group
# =============================================================================
resource "aws_security_group" "alb_sg" {
  name   = "${var.name_prefix}-alb-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  # HTTPS 인바운드 (Production)
  dynamic "ingress" {
    for_each = var.enable_https ? [1] : []
    content {
      from_port   = 443
      to_port     = 443
      protocol    = "tcp"
      cidr_blocks = [var.vpc_cidr]
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-alb-sg" })
}

  # =============================================================================
  # Application Load Balancer (Internal)
  # =============================================================================
  resource "aws_lb" "main" {
    name               = "${var.name_prefix}-alb"
    internal           = true
    load_balancer_type = "application"
    security_groups    = [aws_security_group.alb_sg.id]
    subnets            = var.subnet_ids

    tags = merge(var.common_tags, { Name = "${var.name_prefix}-alb" })
  }

  # =============================================================================
  # Target Groups (Multiple Services)
  # =============================================================================
  resource "aws_lb_target_group" "services" {
    for_each = var.services

    name        = "${var.name_prefix}-${each.key}-tg"
    port        = each.value.container_port
    protocol    = "HTTP"
    vpc_id      = var.vpc_id
    target_type = "ip"

    health_check {
      enabled             = true
      healthy_threshold   = 2
      unhealthy_threshold = 3
      timeout             = 10
      interval            = 30
      path                = each.value.health_check_path
      matcher             = "200"
    }

    tags = merge(var.common_tags, {
      Name    = "${var.name_prefix}-${each.key}-tg"
      Service = each.key
    })
  }

# =============================================================================
# Green Target Groups (for Blue/Green Deployment)
# =============================================================================
resource "aws_lb_target_group" "services_green" {
  for_each = var.enable_blue_green ? var.services : {}

  name        = "${var.name_prefix}-${each.key}-tg-g"
  port        = each.value.container_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 10
    interval            = 30
    path                = each.value.health_check_path
    matcher             = "200"
  }

  tags = merge(var.common_tags, {
    Name    = "${var.name_prefix}-${each.key}-tg-green"
    Service = each.key
    Color   = "green"
  })
}

# =============================================================================
# ALB Listener - HTTP (Default action returns 404 or redirects to HTTPS)
# =============================================================================
resource "aws_lb_listener" "main" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = var.enable_https && var.certificate_arn != null ? "redirect" : "fixed-response"

    dynamic "redirect" {
      for_each = var.enable_https && var.certificate_arn != null ? [1] : []
      content {
        port        = "443"
        protocol    = "HTTPS"
        status_code = "HTTP_301"
      }
    }

    dynamic "fixed_response" {
      for_each = var.enable_https && var.certificate_arn != null ? [] : [1]
      content {
        content_type = "application/json"
        message_body = jsonencode({ error = "Not Found", message = "No matching route" })
        status_code  = "404"
      }
    }
  }
}

# =============================================================================
# ALB Listener - HTTPS (Production)
# =============================================================================
resource "aws_lb_listener" "https" {
  count = var.enable_https && var.certificate_arn != null ? 1 : 0

  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = var.ssl_policy
  certificate_arn   = var.certificate_arn

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "application/json"
      message_body = jsonencode({ error = "Not Found", message = "No matching route" })
      status_code  = "404"
    }
  }
}

# =============================================================================
# ALB Listener Rules - HTTP (Path-based Routing)
# =============================================================================
resource "aws_lb_listener_rule" "services" {
  for_each = var.enable_https && var.certificate_arn != null ? {} : var.services

  listener_arn = aws_lb_listener.main.arn
  priority     = each.value.priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.services[each.key].arn
  }

  condition {
    path_pattern {
      values = each.value.path_patterns
    }
  }

  tags = merge(var.common_tags, { Service = each.key })
}

# =============================================================================
# ALB Listener Rules - HTTPS (Path-based Routing for Production)
# =============================================================================
resource "aws_lb_listener_rule" "services_https" {
  for_each = var.enable_https && var.certificate_arn != null ? var.services : {}

  listener_arn = aws_lb_listener.https[0].arn
  priority     = each.value.priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.services[each.key].arn
  }

  condition {
    path_pattern {
      values = each.value.path_patterns
    }
  }

  tags = merge(var.common_tags, { Service = each.key })
}
