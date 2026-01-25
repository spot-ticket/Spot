# =============================================================================
# Kafka EC2 Module (KRaft Mode - Single Node for Dev)
# =============================================================================

locals {
  kafka_port     = 9092
  kraft_port     = 9093
  instance_name  = "${var.name_prefix}-kafka"
}

# =============================================================================
# Security Group
# =============================================================================
resource "aws_security_group" "kafka" {
  name   = "${var.name_prefix}-kafka-sg"
  vpc_id = var.vpc_id

  # Kafka 클라이언트 포트 (ECS에서 접근)
  ingress {
    from_port       = local.kafka_port
    to_port         = local.kafka_port
    protocol        = "tcp"
    security_groups = var.allowed_security_group_ids
  }

  # SSH (디버깅용 - 선택적)
  dynamic "ingress" {
    for_each = var.enable_ssh ? [1] : []
    content {
      from_port   = 22
      to_port     = 22
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

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-kafka-sg" })
}

# =============================================================================
# IAM Role (CloudWatch Logs, SSM 등)
# =============================================================================
resource "aws_iam_role" "kafka" {
  name = "${var.name_prefix}-kafka-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })

  tags = var.common_tags
}

resource "aws_iam_role_policy_attachment" "kafka_ssm" {
  role       = aws_iam_role.kafka.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "kafka_cloudwatch" {
  role       = aws_iam_role.kafka.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_instance_profile" "kafka" {
  name = "${var.name_prefix}-kafka-profile"
  role = aws_iam_role.kafka.name
}

# =============================================================================
# EC2 Instance
# =============================================================================
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "kafka" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.kafka.id]
  iam_instance_profile   = aws_iam_instance_profile.kafka.name

  # Public IP 할당 (NAT 없는 환경)
  associate_public_ip_address = var.assign_public_ip

  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.volume_size
    delete_on_termination = true
    encrypted             = true
  }

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    kafka_version     = var.kafka_version
    kafka_cluster_id  = var.cluster_id
    kafka_broker_host = local.instance_name
    kafka_port        = local.kafka_port
    kraft_port        = local.kraft_port
    log_retention_hours = var.log_retention_hours
    log_retention_gb    = var.log_retention_gb
  }))

  tags = merge(var.common_tags, {
    Name = local.instance_name
  })

  lifecycle {
    ignore_changes = [ami]
  }
}

# =============================================================================
# Route53 Private DNS (서비스 디스커버리용)
# =============================================================================
resource "aws_route53_zone" "kafka" {
  count = var.create_private_dns ? 1 : 0

  name = "kafka.internal"

  vpc {
    vpc_id = var.vpc_id
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-kafka-zone" })
}

resource "aws_route53_record" "kafka" {
  count = var.create_private_dns ? 1 : 0

  zone_id = aws_route53_zone.kafka[0].zone_id
  name    = "broker"
  type    = "A"
  ttl     = 60
  records = [aws_instance.kafka.private_ip]
}
