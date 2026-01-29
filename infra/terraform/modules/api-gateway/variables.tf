variable "name_prefix" {
  description = "리소스 네이밍 프리픽스"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}

variable "subnet_ids" {
  description = "VPC Link 서브넷 ID 목록"
  type        = list(string)
}

variable "ecs_security_group_id" {
  description = "ECS 보안그룹 ID"
  type        = string
}

variable "alb_listener_arn" {
  description = "ALB Listener ARN"
  type        = string
}

# =============================================================================
# Cognito Settings
# =============================================================================
variable "enable_cognito" {
  description = "Cognito 인증 활성화"
  type        = bool
  default     = false
}

variable "cognito_user_pool_name" {
  description = "Cognito User Pool 이름"
  type        = string
  default     = null
}

variable "cognito_callback_urls" {
  description = "OAuth 콜백 URL 목록"
  type        = list(string)
  default     = ["https://localhost:3000/callback"]
}

variable "cognito_logout_urls" {
  description = "로그아웃 URL 목록"
  type        = list(string)
  default     = ["https://localhost:3000"]
}

variable "public_routes" {
  description = "인증이 필요없는 공개 라우트 패턴"
  type        = list(string)
  default     = ["/api/auth/*", "/health", "/actuator/health", "/api/join","/api/login"]
}

variable "protected_route_patterns" {
  description = "보호된 라우트 패턴 목록"
  type        = list(string)
  default     = ["/api/*"]
}

variable "cognito_issuer" {
  description = "Cognito Issuer URL (https://cognito-idp.<region>.amazonaws.com/<userPoolId>)"
  type        = string
}

variable "cognito_audience" {
  description = "Cognito App Client ID (audience)"
  type        = string
}
