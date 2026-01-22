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
# ECR
# =============================================================================
module "ecr" {
  source = "../../modules/ecr"

  project     = var.project
  name_prefix = local.name_prefix
  common_tags = local.common_tags
}

# =============================================================================
# ALB
# =============================================================================
module "alb" {
  source = "../../modules/alb"

  name_prefix       = local.name_prefix
  common_tags       = local.common_tags
  vpc_id            = module.network.vpc_id
  vpc_cidr          = module.network.vpc_cidr
  subnet_ids        = module.network.private_subnet_ids
  container_port    = var.container_port
  health_check_path = var.health_check_path
}

# =============================================================================
# ECS
# =============================================================================
module "ecs" {
  source = "../../modules/ecs"

  project               = var.project
  name_prefix           = local.name_prefix
  common_tags           = local.common_tags
  region                = var.region
  vpc_id                = module.network.vpc_id
  subnet_ids            = module.network.private_subnet_ids  # prod는 private subnet 사용
  ecr_repository_url    = module.ecr.repository_url
  alb_security_group_id = module.alb.security_group_id
  target_group_arn      = module.alb.target_group_arn
  alb_listener_arn      = module.alb.listener_arn
  container_port        = var.container_port
  cpu                   = var.ecs_cpu
  memory                = var.ecs_memory
  desired_count         = var.ecs_desired_count
  assign_public_ip      = false  # prod는 private
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
