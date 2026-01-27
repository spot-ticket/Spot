variable "name_prefix" {
  description = "리소스 네이밍 프리픽스"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}

variable "domain_name" {
  description = "도메인 이름 (ex: spotorder.org)"
  type        = string
}

variable "create_api_domain" {
  description = "API Gateway 커스텀 도메인 생성 여부"
  type        = bool
  default     = true
}

variable "api_gateway_id" {
  description = "API Gateway ID"
  type        = string
  default     = ""
}
