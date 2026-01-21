resource "aws_security_group" "ecs" {
  name        = "spot-ecs-sg"
  description = "Security group for ECS services"
  vpc_id      = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spot-ecs-sg"
  }
}

resource "aws_security_group" "web" {
    name = "terraform-web-sg"
    description = "Allow HTTP and SSH"
    vpc_id      = aws_vpc.main.id

    ingress {
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
        description = "Allow SSH"
    }

    egress {
        from_port = 0
        to_port   = 0
        protocol  = "-1"
        cidr_blocks = ["0.0.0.0/0"]
        description = "Allow all outbound"
    }

    tags = {
        Name = "terraform-web-sg"
    }
}

resource "aws_security_group" "rds" {
  name        = "spot-rds-sg"
  description = "Security group for RDS"
  vpc_id      = aws_vpc.main.id

  # ECS 서비스에서만 접근 허용
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
    Name = "spot-rds-sg"
  }
}

// ************************* //
// Domain Server ECS 보안 그룹 //
// ************************* //
# resource "aws_security_group" "kafka" {
#   name        = "spot-kafka-sg"
#   description = "Security group for Kafka brokers"
#   vpc_id      = aws_vpc.main.id

#   # ECS 서비스에서 접근
#   ingress {
#     from_port       = 9092
#     to_port         = 9092
#     protocol        = "tcp"
#     security_groups = [aws_security_group.ecs.id]
#     description     = "Kafka from ECS"
#   }

#   # 브로커 간 통신
#   ingress {
#     from_port   = 9092
#     to_port     = 9092
#     protocol    = "tcp"
#     self        = true
#     description = "Inter-broker communication"
#   }

#   # ZooKeeper 연결
#   ingress {
#     from_port       = 2181
#     to_port         = 2181
#     protocol        = "tcp"
#     security_groups = [aws_security_group.zookeeper.id]
#     description     = "ZooKeeper connection"
#   }

#   egress {
#     from_port   = 0
#     to_port     = 0
#     protocol    = "-1"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   tags = {
#     Name = "spot-kafka-sg"
#   }
# }

# # ZooKeeper 보안 그룹
# resource "aws_security_group" "zookeeper" {
#   name        = "spot-zookeeper-sg"
#   description = "Security group for ZooKeeper"
#   vpc_id      = aws_vpc.main.id

#   # Kafka 브로커에서 접근
#   ingress {
#     from_port       = 2181
#     to_port         = 2181
#     protocol        = "tcp"
#     security_groups = [aws_security_group.kafka.id]
#     description     = "ZooKeeper from Kafka"
#   }

#   egress {
#     from_port   = 0
#     to_port     = 0
#     protocol    = "-1"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   tags = {
#     Name = "spot-zookeeper-sg"
#   }
# }
