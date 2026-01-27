terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # 원격 상태 저장소 (팀 협업)
  backend "s3" {
    bucket         = "spot-terraform-state-322546275072"
    key            = "prod/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    use_lockfile   = true
    dynamodb_table = "spot-terraform-lock"
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = local.common_tags
  }
}
