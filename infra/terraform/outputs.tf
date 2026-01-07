output "instance_public_ip" {
    description = "EC2 인스턴스 Public IP"
    value       = aws_instance.web.public_ip
}

output "ssh_connection_command" {
    description = "SSH 접속 명령어"
    value       = "ssh -i terraform-key.pem ubuntu@${aws_instance.web.public_ip}"
}

output "vpc_id" {
    description = "VPC ID"
    value       = aws_vpc.main.id
}

output "subnet_id" {
    description = "Subnet ID"
    value       = aws_subnet.public.id
}