variable "aws_region" {
  type        = string
  description = "ap-northeast-2"
  default     = "ap-northeast-2"
}

variable "user_service_url" {
  type        = string
  default = "http://user.spot.local/"
}
variable "name_prefix" {
  type = string
}