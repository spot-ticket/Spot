# =============================================================================
# VPC
# =============================================================================
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-vpc" })
}

# =============================================================================
# Public Subnets
# =============================================================================
resource "aws_subnet" "public_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.public_subnet_cidrs["a"]
  availability_zone = var.availability_zones["a"]

  tags = merge(var.common_tags, {
    Name = "${var.name_prefix}-public-a"
    Tier = "public"
  })
}

# =============================================================================
# Private Subnets
# =============================================================================
resource "aws_subnet" "private_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs["a"]
  availability_zone = var.availability_zones["a"]

  tags = merge(var.common_tags, {
    Name = "${var.name_prefix}-private-a"
    Tier = "private"
  })
}

resource "aws_subnet" "private_c" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs["c"]
  availability_zone = var.availability_zones["c"]

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-private-c" })
}

# =============================================================================
# Internet Gateway
# =============================================================================
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-igw" })
}

# =============================================================================
# NAT Instance
# =============================================================================
resource "aws_security_group" "nat_sg" {
  name   = "${var.name_prefix}-nat-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-nat-sg" })
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023*-x86_64"]
  }

  filter {
    name   = "state"
    values = ["available"]
  }
}

resource "aws_instance" "nat_instance" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = var.nat_instance_type
  subnet_id                   = aws_subnet.public_a.id
  vpc_security_group_ids      = [aws_security_group.nat_sg.id]
  associate_public_ip_address = true
  source_dest_check           = false

  user_data = <<-EOF
              #!/bin/bash
              sudo sysctl -w net.ipv4.ip_forward=1
              sudo nft add table ip nat
              sudo nft add chain ip nat postrouting { type nat hook postrouting priority 100 \; }
              sudo nft add rule ip nat postrouting oifname eth0 masquerade
              EOF

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-nat-instance" })
}

# =============================================================================
# Route Tables
# =============================================================================
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-public-rt" })
}

resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block           = "0.0.0.0/0"
    network_interface_id = aws_instance.nat_instance.primary_network_interface_id
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-private-rt" })
}

resource "aws_route_table_association" "private_a" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_c" {
  subnet_id      = aws_subnet.private_c.id
  route_table_id = aws_route_table.private.id
}
