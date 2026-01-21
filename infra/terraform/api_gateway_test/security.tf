// *** //
// ALB //
// *** //
resource "aws_security_group" "alb" {
  name        = "spot-alb-sg"
  description = "Security group for ALB"
  vpc_id      = aws_vpc.main.id

  # 인바운드: HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTP"
  }

  # 인바운드: HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTPS"
  }

  # 아웃바운드: 모든 트래픽
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound"
  }

  tags = {
    Name = "spot-alb-sg"
  }
}

// *** //
// ECS //
// *** //
resource "aws_security_group" "ecs" {
  name        = "spot-ecs-sg"
  description = "Security group for ECS services"
  vpc_id      = aws_vpc.main.id

  # 인바운드: ALB에서만 허용
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
    description     = "Allow from ALB"
  }

  # 인바운드: ECS 서비스 간 통신 (Service Connect)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    self        = true
    description = "Allow from other ECS services"
  }

  # 아웃바운드: 모든 트래픽
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound"
  }

  tags = {
    Name = "spot-ecs-sg"
  }
}

// *** //
// RDS //
// *** //
resource "aws_security_group" "rds" {
  name        = "spot-rds-sg"
  description = "Security group for RDS"
  vpc_id      = aws_vpc.main.id

  # 인바운드: ECS에서만 허용
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
    description     = "Allow PostgreSQL from ECS"
  }

  # 아웃바운드: 없음 (DB는 외부 통신 불필요)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound"
  }

  tags = {
    Name = "spot-rds-sg"
  }
}

// *********** //
// NAT GATEWAY //
// *********** //
# Elastic IP for NAT Gateway A
resource "aws_eip" "nat_a" {
  domain = "vpc"

  tags = {
    Name = "spot-nat-eip-a"
  }
}

# Elastic IP for NAT Gateway C
resource "aws_eip" "nat_c" {
  domain = "vpc"

  tags = {
    Name = "spot-nat-eip-c"
  }
}

# NAT Gateway A
resource "aws_nat_gateway" "a" {
  allocation_id = aws_eip.nat_a.id
  subnet_id     = aws_subnet.public_a.id

  tags = {
    Name = "spot-nat-a"
  }

  depends_on = [aws_internet_gateway.main]
}

# NAT Gateway C
resource "aws_nat_gateway" "c" {
  allocation_id = aws_eip.nat_c.id
  subnet_id     = aws_subnet.public_c.id

  tags = {
    Name = "spot-nat-c"
  }

  depends_on = [aws_internet_gateway.main]
}

