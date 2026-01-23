# =============================================================================
# Project Settings
# =============================================================================
variable "project" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
  default     = "dev"
}

variable "name_prefix" {
  description = "리소스 네이밍 프리픽스"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}

variable "region" {
  description = "AWS 리전"
  type        = string
}

# =============================================================================
# Network Settings
# =============================================================================
variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnet_ids" {
  description = "ECS 서비스 서브넷 ID 목록"
  type        = list(string)
}

variable "assign_public_ip" {
  description = "Public IP 할당 여부"
  type        = bool
  default     = true
}

# =============================================================================
# ALB Integration
# =============================================================================
variable "alb_security_group_id" {
  description = "ALB 보안그룹 ID"
  type        = string
}

variable "target_group_arns" {
  description = "ALB Target Group ARN 맵"
  type        = map(string)
}

variable "alb_listener_arn" {
  description = "ALB Listener ARN (의존성용)"
  type        = string
}

# =============================================================================
# ECR Integration
# =============================================================================
variable "ecr_repository_urls" {
  description = "ECR 저장소 URL 맵"
  type        = map(string)
}

# =============================================================================
# Services Configuration
# =============================================================================
variable "services" {
  description = "서비스 구성 맵"
  type = map(object({
    container_port    = number
    cpu               = string
    memory            = string
    desired_count     = number
    health_check_path = string
    path_patterns     = list(string)
    priority          = number
    environment_vars  = map(string)
  }))
}

variable "enable_service_connect" {
  description = "ECS Service Connect 활성화 여부"
  type        = bool
  default     = true
}

variable "log_retention_days" {
  description = "로그 보관 일수"
  type        = number
  default     = 30
}

# =============================================================================
# Database Settings
# =============================================================================
variable "db_endpoint" {
  description = "RDS 엔드포인트"
  type        = string
}

variable "db_name" {
  description = "데이터베이스 이름"
  type        = string
}

variable "db_username" {
  description = "데이터베이스 사용자 이름"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "데이터베이스 비밀번호"
  type        = string
  sensitive   = true
}

# =============================================================================
# Redis Settings
# =============================================================================
variable "redis_endpoint" {
  description = "Redis 엔드포인트"
  type        = string
  default     = ""
}
