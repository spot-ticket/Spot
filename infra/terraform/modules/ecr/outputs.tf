output "repository_urls" {
  description = "ECR 저장소 URL 맵"
  value       = { for k, v in aws_ecr_repository.services : k => v.repository_url }
}

output "repository_arns" {
  description = "ECR 저장소 ARN 맵"
  value       = { for k, v in aws_ecr_repository.services : k => v.arn }
}

output "repository_names" {
  description = "ECR 저장소 이름 맵"
  value       = { for k, v in aws_ecr_repository.services : k => v.name }
}
