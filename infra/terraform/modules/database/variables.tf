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
  description = "VPC CIDR (보안그룹용)"
  type        = string
}

variable "subnet_ids" {
  description = "DB 서브넷 ID 목록"
  type        = list(string)
}

variable "db_name" {
  description = "데이터베이스 이름"
  type        = string
}

variable "username" {
  description = "DB 사용자 이름"
  type        = string
  sensitive   = true
}

variable "password" {
  description = "DB 비밀번호"
  type        = string
  sensitive   = true
}

variable "instance_class" {
  description = "RDS 인스턴스 클래스"
  type        = string
  default     = "db.t3.micro"
}

variable "allocated_storage" {
  description = "스토리지 크기 (GB)"
  type        = number
  default     = 20
}

variable "engine_version" {
  description = "PostgreSQL 버전"
  type        = string
  default     = "16"
}
