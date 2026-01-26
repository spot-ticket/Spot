# =============================================================================
# Spot Production Environment
# =============================================================================

# =============================================================================
# Data Sources
# =============================================================================
data "aws_caller_identity" "current" {}

# =============================================================================
# Network (with NAT Gateway)
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

  # Production: NAT Gateway with Elastic IP
  use_nat_gateway    = var.use_nat_gateway
  single_nat_gateway = var.single_nat_gateway
}

# =============================================================================
# Database (Multi-AZ with Read Replica)
# =============================================================================
module "database" {
  source = "../../modules/database"

  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  vpc_id                = module.network.vpc_id
  vpc_cidr              = module.network.vpc_cidr
  subnet_ids            = module.network.private_subnet_ids
  db_name               = var.db_name
  username              = var.db_username
  password              = var.db_password
  instance_class        = var.db_instance_class
  allocated_storage     = var.db_allocated_storage
  engine_version        = var.db_engine_version
  max_allocated_storage = var.db_allocated_storage * 2

  # Production settings
  multi_az                     = var.db_multi_az
  create_read_replica          = var.db_create_read_replica
  backup_retention_period      = var.db_backup_retention_period
  deletion_protection          = var.db_deletion_protection
  performance_insights_enabled = var.db_performance_insights
  monitoring_interval          = var.db_monitoring_interval
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
# ALB (Direct routing to microservices)
# =============================================================================
module "alb" {
  source = "../../modules/alb"

  name_prefix = local.name_prefix
  common_tags = local.common_tags
  vpc_id      = module.network.vpc_id
  vpc_cidr    = module.network.vpc_cidr
  subnet_ids  = module.network.private_subnet_ids

  services = {
    for k, v in var.services : k => {
      container_port    = v.container_port
      health_check_path = v.health_check_path
      path_patterns     = v.path_patterns
      priority          = v.priority
    }
  }

  # Production settings
  enable_https      = var.enable_https
  certificate_arn   = var.certificate_arn
  enable_blue_green = var.enable_blue_green
}

# =============================================================================
# ECS (Without Gateway + Blue/Green Deployment)
# =============================================================================
module "ecs" {
  source = "../../modules/ecs"

  project               = var.project
  environment           = var.environment
  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  region                = var.region
  vpc_id                = module.network.vpc_id
  subnet_ids            = module.network.private_subnet_ids
  ecr_repository_urls   = module.ecr.repository_urls
  alb_security_group_id = module.alb.security_group_id
  target_group_arns     = module.alb.target_group_arns
  target_group_names    = module.alb.target_group_names
  alb_listener_arn      = var.enable_https && var.certificate_arn != null ? module.alb.https_listener_arn : module.alb.listener_arn
  assign_public_ip      = false

  services               = var.services
  excluded_services      = []
  enable_service_connect = var.enable_service_connect
  standby_mode           = var.standby_mode

  # Blue/Green Deployment
  enable_blue_green       = var.enable_blue_green
  green_target_group_arns = module.alb.green_target_group_arns

  # Database
  db_endpoint = module.database.endpoint
  db_name     = var.db_name
  db_username = var.db_username

  # Redis
  redis_endpoint = module.elasticache.redis_endpoint

  # Kafka
  kafka_bootstrap_servers = module.kafka.bootstrap_servers

  # Parameter Store ARNs
  parameter_arns = {
    db_password     = module.parameters.db_password_arn
    jwt_secret      = module.parameters.jwt_secret_arn
    mail_password   = module.parameters.mail_password_arn
    toss_secret_key = module.parameters.toss_secret_key_arn
  }

  # JWT Settings
  jwt_expire_ms             = var.jwt_expire_ms
  refresh_token_expire_days = var.refresh_token_expire_days

  # Mail Settings
  mail_username = var.mail_username

  # Toss Settings
  toss_customer_key = var.toss_customer_key

  # Service Settings
  service_active_regions = var.service_active_regions

  depends_on = [module.parameters]
}

# =============================================================================
# API Gateway (with Cognito Authentication)
# =============================================================================
module "api_gateway" {
  source = "../../modules/api-gateway"

  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  subnet_ids            = module.network.private_subnet_ids
  ecs_security_group_id = module.ecs.security_group_id
  alb_listener_arn      = var.enable_https && var.certificate_arn != null ? module.alb.https_listener_arn : module.alb.listener_arn

  # Cognito Authentication
  enable_cognito         = var.enable_cognito
  cognito_user_pool_name = "${local.name_prefix}-user-pool"
  cognito_callback_urls  = var.cognito_callback_urls
  cognito_logout_urls    = var.cognito_logout_urls

  public_routes = [
    "/api/auth/*",
    "/api/login",
    "/api/join",
    "/health",
    "/actuator/health"
  ]

  protected_route_patterns = [
    "/api/users/*",
    "/api/orders/*",
    "/api/stores/*",
    "/api/payments/*",
    "/api/admin/*"
  ]
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
# S3 (Static files / Logs)
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
# ElastiCache (Redis with Replication)
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
# Kafka (3-Broker KRaft Cluster)
# =============================================================================
module "kafka" {
  source = "../../modules/kafka"

  name_prefix                = local.name_prefix
  common_tags                = local.common_tags
  vpc_id                     = module.network.vpc_id
  vpc_cidr                   = module.network.vpc_cidr
  subnet_ids                 = module.network.private_subnet_ids
  allowed_security_group_ids = [module.ecs.security_group_id]
  assign_public_ip           = false

  # 3-Broker Cluster
  broker_count        = var.kafka_broker_count
  instance_type       = var.kafka_instance_type
  volume_size         = var.kafka_volume_size
  log_retention_hours = var.kafka_log_retention_hours
  create_private_dns  = true
}

# =============================================================================
# Parameter Store (Secrets)
# =============================================================================
module "parameters" {
  source = "../../modules/parameter-store"

  project     = var.project
  environment = var.environment
  common_tags = local.common_tags

  # Sensitive data (SecureString)
  db_password     = var.db_password
  jwt_secret      = var.jwt_secret
  mail_password   = var.mail_password
  toss_secret_key = var.toss_secret_key

  # Dynamic infrastructure values
  db_endpoint = module.database.endpoint

  depends_on = [module.database]
}

# =============================================================================
# CloudWatch Monitoring
# =============================================================================
module "monitoring" {
  source = "../../modules/monitoring"

  name_prefix = local.name_prefix
  common_tags = local.common_tags
  alert_email = var.alert_email

  # ECS Monitoring
  ecs_cluster_name = module.ecs.cluster_name
  ecs_service_name = module.ecs.service_names["user"]

  # RDS Monitoring
  rds_instance_id = module.database.instance_id

  # ALB Monitoring
  alb_arn_suffix = module.alb.arn_suffix

  # Redis Monitoring
  redis_cluster_id = "${local.name_prefix}-redis-001"
}
