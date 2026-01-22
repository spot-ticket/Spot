# =============================================================================
# Database Security Group
# =============================================================================
resource "aws_security_group" "db_sg" {
  name   = "${var.name_prefix}-db-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-db-sg" })
}

# =============================================================================
# RDS Subnet Group
# =============================================================================
resource "aws_db_subnet_group" "main" {
  name       = "${var.name_prefix}-db-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-db-subnet-group" })
}

# =============================================================================
# RDS Instance (PostgreSQL)
# =============================================================================
resource "aws_db_instance" "main" {
  identifier           = "${var.name_prefix}-db"
  allocated_storage    = var.allocated_storage
  engine               = "postgres"
  engine_version       = var.engine_version
  instance_class       = var.instance_class
  db_name              = var.db_name
  username             = var.username
  password             = var.password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db_sg.id]

  skip_final_snapshot = true
  publicly_accessible = false
  storage_type        = "gp3"

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-db" })
}
