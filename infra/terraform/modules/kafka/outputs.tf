# =============================================================================
# Kafka Module Outputs
# =============================================================================

output "instance_id" {
  description = "Kafka EC2 인스턴스 ID"
  value       = aws_instance.kafka.id
}

output "private_ip" {
  description = "Kafka 프라이빗 IP"
  value       = aws_instance.kafka.private_ip
}

output "bootstrap_servers" {
  description = "Kafka Bootstrap Servers (ECS 환경변수용)"
  value       = "${aws_instance.kafka.private_ip}:9092"
}

output "security_group_id" {
  description = "Kafka 보안그룹 ID"
  value       = aws_security_group.kafka.id
}

# Private DNS 사용시
output "dns_endpoint" {
  description = "Kafka DNS 엔드포인트"
  value       = var.create_private_dns ? "broker.kafka.internal:9092" : null
}
