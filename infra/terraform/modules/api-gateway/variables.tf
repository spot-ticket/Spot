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

variable "cognito_issuer" {
  description = "Cognito Issuer URL (https://cognito-idp.<region>.amazonaws.com/<userPoolId>)"
  type        = string
}

variable "cognito_audience" {
  description = "Cognito App Client ID (audience)"
  type        = string
}
