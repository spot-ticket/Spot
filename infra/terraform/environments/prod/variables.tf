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
  default     = "prod"
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
  default     = "10.1.0.0/16"  # prod는 다른 CIDR
}

variable "public_subnet_cidrs" {
  description = "Public 서브넷 CIDR 목록"
  type        = map(string)
  default = {
    "a" = "10.1.1.0/24"
  }
}

variable "private_subnet_cidrs" {
  description = "Private 서브넷 CIDR 목록"
  type        = map(string)
  default = {
    "a" = "10.1.10.0/24"
    "c" = "10.1.20.0/24"
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
  default     = "t3.micro"  # prod는 더 큰 타입
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
  default     = "db.t3.small"  # prod는 더 큰 타입
}

variable "db_allocated_storage" {
  description = "RDS 스토리지 크기 (GB)"
  type        = number
  default     = 50  # prod는 더 큰 용량
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
  default     = "512"  # prod는 더 큰 사양
}

variable "ecs_memory" {
  description = "ECS Task Memory"
  type        = string
  default     = "1024"  # prod는 더 큰 사양
}

variable "ecs_desired_count" {
  description = "ECS 희망 태스크 수"
  type        = number
  default     = 2  # prod는 고가용성
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
  default     = "/health"
}
