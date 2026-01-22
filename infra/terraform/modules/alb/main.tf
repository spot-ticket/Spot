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
# Target Group (Blue/Green)
# =============================================================================
resource "aws_lb_target_group" "blue" {
  name        = "${var.name_prefix}-tg-blue"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    path                = var.health_check_path
    matcher             = "200"
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-tg-blue" })
}

# =============================================================================
# ALB Listener
# =============================================================================
resource "aws_lb_listener" "main" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.blue.arn
  }
}
