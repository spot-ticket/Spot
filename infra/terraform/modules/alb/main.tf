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
# ALB Listener (Default action returns 404)
# =============================================================================
resource "aws_lb_listener" "main" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

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
# ALB Listener Rules (Path-based Routing)
# =============================================================================
resource "aws_lb_listener_rule" "services" {
  for_each = var.services

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
