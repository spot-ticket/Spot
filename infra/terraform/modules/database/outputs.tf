output "endpoint" {
  description = "RDS 엔드포인트 (host:port)"
  value       = aws_db_instance.main.endpoint
}

output "hostname" {
  description = "RDS 호스트명"
  value       = aws_db_instance.main.address
}

output "port" {
  description = "RDS 포트"
  value       = aws_db_instance.main.port
}

output "database_name" {
  description = "데이터베이스 이름"
  value       = aws_db_instance.main.db_name
}

output "jdbc_url" {
  description = "JDBC URL"
  value       = "jdbc:postgresql://${aws_db_instance.main.endpoint}/${aws_db_instance.main.db_name}"
}

output "instance_id" {
  description = "RDS 인스턴스 ID (CloudWatch용)"
  value       = aws_db_instance.main.identifier
}
