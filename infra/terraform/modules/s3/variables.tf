# =============================================================================
# 공통 변수
# =============================================================================
variable "name_prefix" {
  description = "리소스 이름 접두사"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
}

variable "account_id" {
  description = "AWS 계정 ID (버킷 이름 고유성)"
  type        = string
}

variable "region" {
  description = "AWS 리전"
  type        = string
}

# =============================================================================
# S3 설정
# =============================================================================
variable "cloudfront_oac_arn" {
  description = "CloudFront OAC ARN (정적 파일 버킷 접근용)"
  type        = string
  default     = ""
}

variable "log_transition_days" {
  description = "로그를 Glacier로 이동하는 일수"
  type        = number
  default     = 30
}

variable "log_expiration_days" {
  description = "로그 삭제 일수"
  type        = number
  default     = 90
}
