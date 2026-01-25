# =============================================================================
# Data Sources
# =============================================================================
data "aws_caller_identity" "current" {}

# =============================================================================
# Network
# =============================================================================
module "network" {
  source = "../../modules/network"

  name_prefix          = local.name_prefix
  common_tags          = local.common_tags
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones
  nat_instance_type    = var.nat_instance_type
}

# =============================================================================
# Database
# =============================================================================
module "database" {
  source = "../../modules/database"

  name_prefix       = local.name_prefix
  common_tags       = local.common_tags
  vpc_id            = module.network.vpc_id
  vpc_cidr          = module.network.vpc_cidr
  subnet_ids        = module.network.private_subnet_ids
  db_name           = var.db_name
  username          = var.db_username
  password          = var.db_password
  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage
  engine_version    = var.db_engine_version
}

# =============================================================================
# ECR (Multiple Repositories)
# =============================================================================
module "ecr" {
  source = "../../modules/ecr"

  project       = var.project
  name_prefix   = local.name_prefix
  common_tags   = local.common_tags
  service_names = toset(keys(var.services))
}

# =============================================================================
# ALB (Gateway Pass-through)
# =============================================================================
module "alb" {
  source = "../../modules/alb"

  name_prefix = local.name_prefix
  common_tags = local.common_tags
  vpc_id      = module.network.vpc_id
  vpc_cidr    = module.network.vpc_cidr
  subnet_ids  = module.network.private_subnet_ids

  # Gateway만 ALB에 연결 - 모든 트래픽이 Spring Gateway로 전달됨
  services = {
    "gateway" = {
      container_port    = var.services["gateway"].container_port
      health_check_path = var.services["gateway"].health_check_path
      path_patterns     = ["/*"]
      priority          = 1
    }
  }
}

# =============================================================================
# ECS (Multiple Services with Service Connect)
# =============================================================================
module "ecs" {
  source = "../../modules/ecs"

  project               = var.project
  environment           = var.environment
  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  region                = var.region
  vpc_id                = module.network.vpc_id
  subnet_ids            = [module.network.public_subnet_a_id] # NAT 문제로 public 사용
  ecr_repository_urls   = module.ecr.repository_urls
  alb_security_group_id = module.alb.security_group_id
  target_group_arns     = module.alb.target_group_arns
  alb_listener_arn      = module.alb.listener_arn
  assign_public_ip      = true # NAT 문제로 public IP 사용

  services               = var.services
  enable_service_connect = var.enable_service_connect
  standby_mode           = var.standby_mode

  # Database 연결 정보
  db_endpoint = module.database.endpoint
  db_name     = var.db_name
  db_username = var.db_username
  db_password = var.db_password

  # Redis 연결 정보
  redis_endpoint = module.elasticache.redis_endpoint

  # JWT 설정
  jwt_secret                = var.jwt_secret
  jwt_expire_ms             = var.jwt_expire_ms
  refresh_token_expire_days = var.refresh_token_expire_days

  # Mail 설정
  mail_username = var.mail_username
  mail_password = var.mail_password

  # Toss 결제 설정
  toss_secret_key   = var.toss_secret_key
  toss_customer_key = var.toss_customer_key

  # 서비스 설정
  service_active_regions = var.service_active_regions
}

# =============================================================================
# API Gateway
# =============================================================================
module "api_gateway" {
  source = "../../modules/api-gateway"

  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  subnet_ids            = module.network.private_subnet_ids
  ecs_security_group_id = module.ecs.security_group_id
  alb_listener_arn      = module.alb.listener_arn
}

# =============================================================================
# DNS (Route 53 + ACM)
# =============================================================================
module "dns" {
  source = "../../modules/dns"

  name_prefix       = local.name_prefix
  common_tags       = local.common_tags
  domain_name       = var.domain_name
  create_api_domain = var.create_api_domain
  api_gateway_id    = module.api_gateway.api_id
}

# =============================================================================
# WAF (Web Application Firewall)
# =============================================================================
module "waf" {
  source = "../../modules/waf"

  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  api_gateway_stage_arn = module.api_gateway.stage_arn
  rate_limit            = var.waf_rate_limit
}

# =============================================================================
# S3 (정적 파일 / 로그 저장)
# =============================================================================
module "s3" {
  source = "../../modules/s3"

  name_prefix         = local.name_prefix
  common_tags         = local.common_tags
  account_id          = data.aws_caller_identity.current.account_id
  region              = var.region
  log_transition_days = var.s3_log_transition_days
  log_expiration_days = var.s3_log_expiration_days
}

# =============================================================================
# ElastiCache (Redis 캐시/세션)
# =============================================================================
module "elasticache" {
  source = "../../modules/elasticache"

  name_prefix                = local.name_prefix
  common_tags                = local.common_tags
  vpc_id                     = module.network.vpc_id
  subnet_ids                 = module.network.private_subnet_ids
  allowed_security_group_ids = [module.ecs.security_group_id]
  node_type                  = var.redis_node_type
  num_cache_clusters         = var.redis_num_cache_clusters
  engine_version             = var.redis_engine_version
}

# =============================================================================
# CloudWatch Monitoring (Updated for MSA)
# =============================================================================
module "monitoring" {
  source = "../../modules/monitoring"

  name_prefix = local.name_prefix
  common_tags = local.common_tags
  alert_email = var.alert_email

  # ECS 모니터링 (대표 서비스)
  ecs_cluster_name = module.ecs.cluster_name
  ecs_service_name = module.ecs.service_names["user"]

  # RDS 모니터링
  rds_instance_id = module.database.instance_id

  # ALB 모니터링
  alb_arn_suffix = module.alb.arn_suffix

  # Redis 모니터링 (선택)
  redis_cluster_id = "${local.name_prefix}-redis-001"
}
