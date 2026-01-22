variable "project" {
  description = "프로젝트 이름"
  type        = string
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

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnet_ids" {
  description = "ECS 서비스 서브넷 ID 목록"
  type        = list(string)
}

variable "ecr_repository_url" {
  description = "ECR 저장소 URL"
  type        = string
}

variable "alb_security_group_id" {
  description = "ALB 보안그룹 ID"
  type        = string
}

variable "target_group_arn" {
  description = "ALB Target Group ARN"
  type        = string
}

variable "alb_listener_arn" {
  description = "ALB Listener ARN (의존성용)"
  type        = string
}

variable "container_port" {
  description = "컨테이너 포트"
  type        = number
  default     = 8080
}

variable "cpu" {
  description = "Task CPU"
  type        = string
  default     = "256"
}

variable "memory" {
  description = "Task Memory"
  type        = string
  default     = "512"
}

variable "desired_count" {
  description = "희망 태스크 수"
  type        = number
  default     = 1
}

variable "assign_public_ip" {
  description = "Public IP 할당 여부"
  type        = bool
  default     = true
}

variable "log_retention_days" {
  description = "로그 보관 일수"
  type        = number
  default     = 30
}

# =============================================================================
# Database 설정
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
