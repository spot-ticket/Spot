output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "VPC CIDR 블록"
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_a_id" {
  description = "Public Subnet A ID"
  value       = aws_subnet.public_a.id
}

output "private_subnet_a_id" {
  description = "Private Subnet A ID"
  value       = aws_subnet.private_a.id
}

output "private_subnet_c_id" {
  description = "Private Subnet C ID"
  value       = aws_subnet.private_c.id
}

output "private_subnet_ids" {
  description = "Private Subnet IDs"
  value       = [aws_subnet.private_a.id, aws_subnet.private_c.id]
}
