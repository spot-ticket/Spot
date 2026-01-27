# =============================================================================
# ElastiCache Outputs
# =============================================================================
output "redis_endpoint" {
  description = "Redis Primary Endpoint"
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "redis_reader_endpoint" {
  description = "Redis Reader Endpoint (복제본용)"
  value       = aws_elasticache_replication_group.redis.reader_endpoint_address
}

output "redis_port" {
  description = "Redis 포트"
  value       = aws_elasticache_replication_group.redis.port
}

output "redis_connection_string" {
  description = "Redis 연결 문자열 (Spring Boot용)"
  value       = "redis://${aws_elasticache_replication_group.redis.primary_endpoint_address}:${aws_elasticache_replication_group.redis.port}"
}

output "security_group_id" {
  description = "Redis Security Group ID"
  value       = aws_security_group.redis.id
}

output "replication_group_id" {
  description = "Redis Replication Group ID"
  value       = aws_elasticache_replication_group.redis.id
}
