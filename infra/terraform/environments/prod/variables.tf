# =============================================================================
# Project Settings
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
# Network Settings
# =============================================================================
variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.1.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public 서브넷 CIDR 목록"
  type        = map(string)
  default = {
    "a" = "10.1.1.0/24"
    "c" = "10.1.2.0/24"
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

# NAT Gateway (Production)
variable "use_nat_gateway" {
  description = "NAT Gateway 사용 (true) vs NAT Instance (false)"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "단일 NAT Gateway 사용 (비용 절감 vs HA)"
  type        = bool
  default     = false  # Production: AZ별 NAT Gateway for HA
}

variable "nat_instance_type" {
  description = "NAT Instance 타입 (NAT Gateway 미사용시)"
  type        = string
  default     = "t3.micro"
}

# =============================================================================
# Database Settings
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
  default     = "db.t3.small"
}

variable "db_allocated_storage" {
  description = "RDS 스토리지 크기 (GB)"
  type        = number
  default     = 50
}

variable "db_engine_version" {
  description = "PostgreSQL 버전"
  type        = string
  default     = "16"
}

# Production Database Settings
variable "db_multi_az" {
  description = "Multi-AZ 배포"
  type        = bool
  default     = true
}

variable "db_create_read_replica" {
  description = "Read Replica 생성"
  type        = bool
  default     = true
}

variable "db_backup_retention_period" {
  description = "백업 보관 기간 (일)"
  type        = number
  default     = 14
}

variable "db_deletion_protection" {
  description = "삭제 보호"
  type        = bool
  default     = true
}

variable "db_performance_insights" {
  description = "Performance Insights 활성화"
  type        = bool
  default     = true
}

variable "db_monitoring_interval" {
  description = "Enhanced Monitoring 간격 (초)"
  type        = number
  default     = 60
}

# =============================================================================
# Services Configuration (MSA - Gateway 제외)
# =============================================================================
variable "services" {
  description = "MSA 서비스 구성 맵 (Gateway 제외)"
  type = map(object({
    container_port    = number
    cpu               = string
    memory            = string
    desired_count     = number
    health_check_path = string
    path_patterns     = list(string)
    priority          = number
    environment_vars  = map(string)
  }))
  default = {
    "user" = {
      container_port    = 8081
      cpu               = "512"
      memory            = "1024"
      desired_count     = 2
      health_check_path = "/actuator/health"
      path_patterns     = ["/api/users/*", "/api/users", "/api/auth/*", "/api/admin/*", "/api/login", "/api/join"]
      priority          = 100
      environment_vars = {
        SERVICE_NAME = "spot-user"
        DB_SCHEMA    = "users"
      }
    }
    "order" = {
      container_port    = 8082
      cpu               = "512"
      memory            = "1024"
      desired_count     = 2
      health_check_path = "/actuator/health"
      path_patterns     = ["/api/orders/*", "/api/orders"]
      priority          = 200
      environment_vars = {
        SERVICE_NAME = "spot-order"
        DB_SCHEMA    = "orders"
      }
    }
    "store" = {
      container_port    = 8083
      cpu               = "512"
      memory            = "1024"
      desired_count     = 2
      health_check_path = "/actuator/health"
      path_patterns     = ["/api/stores/*", "/api/stores", "/api/categories/*", "/api/reviews/*", "/api/menus/*"]
      priority          = 300
      environment_vars = {
        SERVICE_NAME = "spot-store"
        DB_SCHEMA    = "stores"
      }
    }
    "payment" = {
      container_port    = 8084
      cpu               = "512"
      memory            = "1024"
      desired_count     = 2
      health_check_path = "/actuator/health"
      path_patterns     = ["/api/payments/*", "/api/payments"]
      priority          = 400
      environment_vars = {
        SERVICE_NAME = "spot-payment"
        DB_SCHEMA    = "payments"
      }
    }
  }
}

variable "enable_service_connect" {
  description = "ECS Service Connect 활성화"
  type        = bool
  default     = true
}

variable "standby_mode" {
  description = "스탠바이 모드 (모든 서비스 desired_count = 0)"
  type        = bool
  default     = false
}

# =============================================================================
# Blue/Green Deployment
# =============================================================================
variable "enable_blue_green" {
  description = "Blue/Green 배포 활성화 (CodeDeploy)"
  type        = bool
  default     = true
}

# =============================================================================
# Kafka Settings (3-Broker Cluster)
# =============================================================================
variable "kafka_broker_count" {
  description = "Kafka 브로커 수"
  type        = number
  default     = 3
}

variable "kafka_instance_type" {
  description = "Kafka EC2 인스턴스 타입"
  type        = string
  default     = "t3.small"
}

variable "kafka_volume_size" {
  description = "Kafka EBS 볼륨 크기 (GB)"
  type        = number
  default     = 50
}

variable "kafka_log_retention_hours" {
  description = "Kafka 메시지 보관 시간"
  type        = number
  default     = 168
}

# =============================================================================
# Redis Settings
# =============================================================================
variable "redis_node_type" {
  description = "ElastiCache 노드 타입"
  type        = string
  default     = "cache.t3.small"
}

variable "redis_num_cache_clusters" {
  description = "Redis 클러스터 수 (Replication Group)"
  type        = number
  default     = 2
}

variable "redis_engine_version" {
  description = "Redis 엔진 버전"
  type        = string
  default     = "7.1"
}

# =============================================================================
# API Gateway + Cognito
# =============================================================================
variable "enable_cognito" {
  description = "Cognito 인증 활성화"
  type        = bool
  default     = true
}

variable "cognito_callback_urls" {
  description = "Cognito OAuth 콜백 URL"
  type        = list(string)
  default     = ["https://localhost:3000/callback"]
}

variable "cognito_logout_urls" {
  description = "Cognito 로그아웃 URL"
  type        = list(string)
  default     = ["https://localhost:3000"]
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

# =============================================================================
# JWT Settings
# =============================================================================
variable "jwt_secret" {
  description = "JWT 시크릿 키"
  type        = string
  sensitive   = true
}

variable "jwt_expire_ms" {
  description = "JWT 만료 시간 (밀리초)"
  type        = number
  default     = 3600000
}

variable "refresh_token_expire_days" {
  description = "리프레시 토큰 만료 일수"
  type        = number
  default     = 14
}

# =============================================================================
# Mail Settings
# =============================================================================
variable "mail_username" {
  description = "SMTP 사용자 이름"
  type        = string
  default     = ""
}

variable "mail_password" {
  description = "SMTP 비밀번호"
  type        = string
  sensitive   = true
  default     = ""
}

# =============================================================================
# Toss Payments Settings
# =============================================================================
variable "toss_customer_key" {
  description = "Toss Payments 고객 키"
  type        = string
  default     = "customer_1"
}

variable "toss_secret_key" {
  description = "Toss Payments 시크릿 키"
  type        = string
  sensitive   = true
  default     = ""
}

# =============================================================================
# Service Settings
# =============================================================================
variable "service_active_regions" {
  description = "서비스 활성 지역"
  type        = string
  default     = "종로구"
}

# =============================================================================
# DNS Settings
# =============================================================================
variable "domain_name" {
  description = "도메인 이름"
  type        = string
  default     = ""
}

variable "create_api_domain" {
  description = "API 도메인 생성 여부"
  type        = bool
  default     = false
}

# =============================================================================
# WAF Settings
# =============================================================================
variable "waf_rate_limit" {
  description = "WAF 요청 제한 (5분당)"
  type        = number
  default     = 2000
}

# =============================================================================
# S3 Settings
# =============================================================================
variable "s3_log_transition_days" {
  description = "S3 로그 Glacier 전환 일수"
  type        = number
  default     = 30
}

variable "s3_log_expiration_days" {
  description = "S3 로그 만료 일수"
  type        = number
  default     = 365
}

# =============================================================================
# Monitoring Settings
# =============================================================================
variable "alert_email" {
  description = "알림 이메일"
  type        = string
  default     = ""
}
