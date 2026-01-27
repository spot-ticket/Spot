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

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "allowed_security_group_ids" {
  description = "Redis 접근을 허용할 Security Group ID 목록"
  type        = list(string)
}

variable "subnet_ids" {
  description = "ElastiCache가 위치할 서브넷 ID 목록"
  type        = list(string)
}

# =============================================================================
# Redis 설정
# =============================================================================
variable "node_type" {
  description = "Redis 노드 타입"
  type        = string
  default     = "cache.t3.micro"
}

variable "num_cache_clusters" {
  description = "캐시 클러스터 수 (1=단일노드, 2+=복제본)"
  type        = number
  default     = 1
}

variable "engine_version" {
  description = "Redis 엔진 버전"
  type        = string
  default     = "7.1"
}

variable "auth_token" {
  description = "Redis AUTH 토큰 (선택, 설정 시 전송 암호화 활성화)"
  type        = string
  default     = ""
  sensitive   = true
}

# =============================================================================
# 유지보수 설정
# =============================================================================
variable "maintenance_window" {
  description = "유지보수 윈도우 (UTC)"
  type        = string
  default     = "sun:05:00-sun:06:00"
}

variable "snapshot_window" {
  description = "스냅샷 윈도우 (UTC)"
  type        = string
  default     = "04:00-05:00"
}

variable "snapshot_retention_limit" {
  description = "스냅샷 보존 일수 (0=비활성화)"
  type        = number
  default     = 1
}
