# =============================================================================
# Kafka Module Outputs
# =============================================================================

output "instance_ids" {
  description = "Kafka EC2 인스턴스 ID 맵"
  value       = { for k, v in aws_instance.kafka : k => v.id }
}

output "private_ips" {
  description = "Kafka 프라이빗 IP 맵"
  value       = { for k, v in aws_instance.kafka : k => v.private_ip }
}

output "bootstrap_servers" {
  description = "Kafka Bootstrap Servers (ECS 환경변수용)"
  value       = join(",", [for k, v in aws_instance.kafka : "${v.private_ip}:9092"])
}

output "security_group_id" {
  description = "Kafka 보안그룹 ID"
  value       = aws_security_group.kafka.id
}

output "broker_count" {
  description = "Kafka 브로커 수"
  value       = var.broker_count
}

# Private DNS endpoints
output "dns_bootstrap_endpoint" {
  description = "Kafka Bootstrap DNS 엔드포인트"
  value       = var.create_private_dns ? "bootstrap.kafka.internal:9092" : null
}

output "dns_broker_endpoints" {
  description = "개별 브로커 DNS 엔드포인트"
  value       = var.create_private_dns ? { for k, v in local.brokers : k => "kafka-${k}.kafka.internal:9092" } : {}
}
