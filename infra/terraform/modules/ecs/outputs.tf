output "cluster_name" {
  description = "ECS 클러스터 이름"
  value       = aws_ecs_cluster.main.name
}

output "cluster_arn" {
  description = "ECS 클러스터 ARN"
  value       = aws_ecs_cluster.main.arn
}

output "service_names" {
  description = "ECS 서비스 이름 맵"
  value       = { for k, v in aws_ecs_service.services : k => v.name }
}

output "service_arns" {
  description = "ECS 서비스 ARN 맵"
  value       = { for k, v in aws_ecs_service.services : k => v.id }
}

output "security_group_id" {
  description = "MSA 보안그룹 ID"
  value       = aws_security_group.msa_sg.id
}

output "task_definition_arns" {
  description = "Task Definition ARN 맵"
  value       = { for k, v in aws_ecs_task_definition.services : k => v.arn }
}

output "cloudwatch_log_groups" {
  description = "CloudWatch Log Group 이름 맵"
  value       = { for k, v in aws_cloudwatch_log_group.services : k => v.name }
}

output "service_discovery_namespace_id" {
  description = "Service Discovery Namespace ID"
  value       = aws_service_discovery_private_dns_namespace.main.id
}

output "service_discovery_namespace_arn" {
  description = "Service Discovery Namespace ARN"
  value       = aws_service_discovery_private_dns_namespace.main.arn
}

# =============================================================================
# CodeDeploy Outputs
# =============================================================================
output "codedeploy_app_name" {
  description = "CodeDeploy Application 이름"
  value       = var.enable_blue_green ? aws_codedeploy_app.main[0].name : null
}

output "codedeploy_deployment_group_names" {
  description = "CodeDeploy Deployment Group 이름 맵"
  value       = var.enable_blue_green ? { for k, v in aws_codedeploy_deployment_group.services : k => v.deployment_group_name } : {}
}
