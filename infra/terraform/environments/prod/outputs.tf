# =============================================================================
# Network
# =============================================================================
output "vpc_id" {
  description = "VPC ID"
  value       = module.network.vpc_id
}

# =============================================================================
# Database
# =============================================================================
output "rds_endpoint" {
  description = "RDS 엔드포인트"
  value       = module.database.endpoint
}

output "spring_datasource_url" {
  description = "Spring JDBC URL"
  value       = module.database.jdbc_url
}

# =============================================================================
# ECR
# =============================================================================
output "ecr_repository_url" {
  description = "ECR 저장소 URL"
  value       = module.ecr.repository_url
}

# =============================================================================
# ECS
# =============================================================================
output "ecs_cluster_name" {
  description = "ECS 클러스터 이름"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "ECS 서비스 이름"
  value       = module.ecs.service_name
}

# =============================================================================
# ALB
# =============================================================================
output "alb_dns" {
  description = "ALB DNS"
  value       = module.alb.alb_dns_name
}

# =============================================================================
# API Gateway
# =============================================================================
output "api_url" {
  description = "API Gateway URL"
  value       = module.api_gateway.api_endpoint
}
