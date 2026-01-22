output "cluster_name" {
  description = "ECS 클러스터 이름"
  value       = aws_ecs_cluster.main.name
}

output "cluster_arn" {
  description = "ECS 클러스터 ARN"
  value       = aws_ecs_cluster.main.arn
}

output "service_name" {
  description = "ECS 서비스 이름"
  value       = aws_ecs_service.main.name
}

output "security_group_id" {
  description = "ECS 보안그룹 ID"
  value       = aws_security_group.api_sg.id
}
