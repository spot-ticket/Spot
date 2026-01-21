variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDR blocks"
  type        = map(string)
  default = {
    a = "10.0.1.0/24"
    c = "10.0.2.0/24"
  }
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDR blocks"
  type        = map(string)
  default = {
    a = "10.0.10.0/24"
    c = "10.0.20.0/24"
  }
}

variable "availability_zones" {
  description = "Availability zones"
  type        = map(string)
  default = {
    a = "ap-northeast-2a"
    c = "ap-northeast-2c"
  }
}