# =============================================================================
# 프로젝트 기본 설정
# =============================================================================
variable "project" {
  description = "프로젝트 이름"
  type        = string
  default     = "spot"
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
  default     = "dev"
}

variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

# =============================================================================
# 네트워크 설정
# =============================================================================
variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public 서브넷 CIDR 목록"
  type        = map(string)
  default = {
    "a" = "10.0.1.0/24"
  }
}

variable "private_subnet_cidrs" {
  description = "Private 서브넷 CIDR 목록"
  type        = map(string)
  default = {
    "a" = "10.0.10.0/24"
    "c" = "10.0.20.0/24"
  }
}

variable "availability_zones" {
  description = "사용할 가용 영역"
  type        = map(string)
  default = {
    "a" = "ap-northeast-2a"
    "c" = "ap-northeast-2c"
  }
}

variable "nat_instance_type" {
  description = "NAT Instance 타입"
  type        = string
  default     = "t3.nano"
}

# =============================================================================
# 데이터베이스 설정
# =============================================================================
variable "db_name" {
  description = "데이터베이스 이름"
  type        = string
  default     = "spotdb"
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

variable "db_instance_class" {
  description = "RDS 인스턴스 클래스"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "RDS 스토리지 크기 (GB)"
  type        = number
  default     = 20
}

variable "db_engine_version" {
  description = "PostgreSQL 버전"
  type        = string
  default     = "16"
}

# =============================================================================
# ECS 설정
# =============================================================================
variable "ecs_cpu" {
  description = "ECS Task CPU"
  type        = string
  default     = "256"
}

variable "ecs_memory" {
  description = "ECS Task Memory"
  type        = string
  default     = "512"
}

variable "ecs_desired_count" {
  description = "ECS 희망 태스크 수"
  type        = number
  default     = 1
}

variable "container_port" {
  description = "컨테이너 포트"
  type        = number
  default     = 8080
}

# =============================================================================
# ALB 설정
# =============================================================================
variable "health_check_path" {
  description = "헬스체크 경로"
  type        = string
  default     = "/"
}

# =============================================================================
# DNS / SSL 설정
# =============================================================================
variable "domain_name" {
  description = "도메인 이름"
  type        = string
  default     = "spotorder.org"
}

variable "create_api_domain" {
  description = "API Gateway 커스텀 도메인 생성 여부"
  type        = bool
  default     = true
}

# =============================================================================
# WAF 설정
# =============================================================================
variable "waf_rate_limit" {
  description = "5분당 최대 요청 수 (Rate Limiting)"
  type        = number
  default     = 2000
}

# =============================================================================
# S3 설정
# =============================================================================
variable "s3_log_transition_days" {
  description = "로그를 Glacier로 이동하는 일수"
  type        = number
  default     = 30
}

variable "s3_log_expiration_days" {
  description = "로그 삭제 일수"
  type        = number
  default     = 90
}

# =============================================================================
# ElastiCache (Redis) 설정
# =============================================================================
variable "redis_node_type" {
  description = "Redis 노드 타입"
  type        = string
  default     = "cache.t3.micro"
}

variable "redis_num_cache_clusters" {
  description = "Redis 클러스터 수 (1=단일, 2+=복제본)"
  type        = number
  default     = 1 # dev 환경에서는 단일 노드
}

variable "redis_engine_version" {
  description = "Redis 엔진 버전"
  type        = string
  default     = "7.1"
}

# =============================================================================
# Monitoring 설정
# =============================================================================
variable "alert_email" {
  description = "알람 알림 받을 이메일 (빈 값이면 구독 안함)"
  type        = string
  default     = ""
}
