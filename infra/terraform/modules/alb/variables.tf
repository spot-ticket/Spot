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
