variable "name_prefix" {
  description = "리소스 네이밍 프리픽스"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR"
  type        = string
}

variable "subnet_ids" {
  description = "ALB 서브넷 ID 목록"
  type        = list(string)
}

variable "services" {
  description = "서비스 구성 맵"
  type = map(object({
    container_port    = number
    health_check_path = string
    path_patterns     = list(string)
    priority          = number
  }))
}

# =============================================================================
# HTTPS Settings
# =============================================================================
variable "enable_https" {
  description = "HTTPS 활성화"
  type        = bool
  default     = false
}

variable "certificate_arn" {
  description = "ACM 인증서 ARN"
  type        = string
  default     = null
}

variable "ssl_policy" {
  description = "SSL 정책"
  type        = string
  default     = "ELBSecurityPolicy-TLS13-1-2-2021-06"
}

# =============================================================================
# Blue/Green Deployment Settings
# =============================================================================
variable "enable_blue_green" {
  description = "Blue/Green 배포용 추가 Target Group 생성"
  type        = bool
  default     = false
}
