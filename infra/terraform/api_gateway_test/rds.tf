// *************** //
// DB Subnet Group //
// *************** //
resource "aws_db_subnet_group" "spot" {
  name       = "spot-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_c.id]

  tags = {
    Name = "spot-db-subnet-group"
  }
}

// ********* //
// KMS Key   //
// ********* //
resource "aws_kms_key" "rds" {
  description             = "KMS key for RDS encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = {
    Name = "spot-rds-kms-key"
  }
}

// ***************** //
// DB Parameter Group //
// ***************** //
resource "aws_db_parameter_group" "spot" {
  name   = "spot-postgres15"
  family = "postgres15"

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  tags = {
    Name = "spot-db-parameter-group"
  }
}

// ************************ //
// RDS Security Groups per Service //
// ************************ //
resource "aws_security_group" "rds_user" {
  name        = "spot-rds-user-sg"
  description = "Security group for User Service RDS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
    description     = "PostgreSQL from ECS"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spot-rds-user-sg"
  }
}

resource "aws_security_group" "rds_order" {
  name        = "spot-rds-order-sg"
  description = "Security group for Order Service RDS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
    description     = "PostgreSQL from ECS"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spot-rds-order-sg"
  }
}

resource "aws_security_group" "rds_store" {
  name        = "spot-rds-store-sg"
  description = "Security group for Store Service RDS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
    description     = "PostgreSQL from ECS"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spot-rds-store-sg"
  }
}

resource "aws_security_group" "rds_payment" {
  name        = "spot-rds-payment-sg"
  description = "Security group for Payment Service RDS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
    description     = "PostgreSQL from ECS"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spot-rds-payment-sg"
  }
}

// ************** //
// RDS Variables  //
// ************** //
variable "user_db_username" {
  description = "Username for user DB"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "user_db_password" {
  description = "Password for user DB (set via terraform.tfvars or TF_VAR_user_db_password)"
  type        = string
  sensitive   = true
}

variable "order_db_username" {
  description = "Username for order DB"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "order_db_password" {
  description = "Password for order DB (set via terraform.tfvars or TF_VAR_order_db_password)"
  type        = string
  sensitive   = true
}

variable "store_db_username" {
  description = "Username for store DB"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "store_db_password" {
  description = "Password for store DB (set via terraform.tfvars or TF_VAR_store_db_password)"
  type        = string
  sensitive   = true
}

variable "payment_db_username" {
  description = "Username for payment DB"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "payment_db_password" {
  description = "Password for payment DB (set via terraform.tfvars or TF_VAR_payment_db_password)"
  type        = string
  sensitive   = true
}

# User Service DB
resource "aws_db_instance" "user" {
  count = var.environment == "prod" ? 1 : 0

  identifier = "spot-user-db"

  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = "db.t3.medium"
  allocated_storage    = 50
  max_allocated_storage = 200

  db_name  = "user_db"
  username = var.user_db_username
  password = var.user_db_password
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.spot.name
  vpc_security_group_ids = [aws_security_group.rds_user.id]
  publicly_accessible    = false

  multi_az = true

  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"

  storage_type      = "gp3"
  storage_encrypted = true
  kms_key_id        = aws_kms_key.rds.arn

  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  parameter_group_name = aws_db_parameter_group.spot.name

  deletion_protection       = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "spot-user-final-snapshot"

  tags = {
    Name        = "spot-user-db"
    Service     = "user"
    Environment = var.environment
  }
}

# Order Service DB
resource "aws_db_instance" "order" {
  count = var.environment == "prod" ? 1 : 0

  identifier = "spot-order-db"

  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = "db.t3.medium"
  allocated_storage    = 100
  max_allocated_storage = 500

  db_name  = "order_db"
  username = var.order_db_username
  password = var.order_db_password
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.spot.name
  vpc_security_group_ids = [aws_security_group.rds_order.id]
  publicly_accessible    = false

  multi_az = true

  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"

  storage_type      = "gp3"
  storage_encrypted = true
  kms_key_id        = aws_kms_key.rds.arn

  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  parameter_group_name = aws_db_parameter_group.spot.name

  deletion_protection       = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "spot-order-final-snapshot"

  tags = {
    Name        = "spot-order-db"
    Service     = "order"
    Environment = var.environment
  }
}

# Store Service DB
resource "aws_db_instance" "store" {
  count = var.environment == "prod" ? 1 : 0

  identifier = "spot-store-db"

  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = "db.t3.medium"
  allocated_storage    = 50
  max_allocated_storage = 200

  db_name  = "store_db"
  username = var.store_db_username
  password = var.store_db_password
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.spot.name
  vpc_security_group_ids = [aws_security_group.rds_store.id]
  publicly_accessible    = false

  multi_az = true

  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"

  storage_type      = "gp3"
  storage_encrypted = true
  kms_key_id        = aws_kms_key.rds.arn

  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  parameter_group_name = aws_db_parameter_group.spot.name

  deletion_protection       = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "spot-store-final-snapshot"

  tags = {
    Name        = "spot-store-db"
    Service     = "store"
    Environment = var.environment
  }
}

# Payment Service DB
resource "aws_db_instance" "payment" {
  count = var.environment == "prod" ? 1 : 0

  identifier = "spot-payment-db"

  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = "db.t3.medium"
  allocated_storage    = 50
  max_allocated_storage = 200

  db_name  = "payment_db"
  username = var.payment_db_username
  password = var.payment_db_password
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.spot.name
  vpc_security_group_ids = [aws_security_group.rds_payment.id]
  publicly_accessible    = false

  multi_az = true

  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"

  storage_type      = "gp3"
  storage_encrypted = true
  kms_key_id        = aws_kms_key.rds.arn

  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  parameter_group_name = aws_db_parameter_group.spot.name

  deletion_protection       = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "spot-payment-final-snapshot"

  tags = {
    Name        = "spot-payment-db"
    Service     = "payment"
    Environment = var.environment
  }
}
