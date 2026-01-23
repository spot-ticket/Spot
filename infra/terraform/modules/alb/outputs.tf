output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.main.arn
}

output "alb_dns_name" {
  description = "ALB DNS 이름"
  value       = aws_lb.main.dns_name
}

output "target_group_arns" {
  description = "Target Group ARN 맵"
  value       = { for k, v in aws_lb_target_group.services : k => v.arn }
}

output "listener_arn" {
  description = "Listener ARN"
  value       = aws_lb_listener.main.arn
}

output "security_group_id" {
  description = "ALB 보안그룹 ID"
  value       = aws_security_group.alb_sg.id
}

output "arn_suffix" {
  description = "ALB ARN suffix (CloudWatch용)"
  value       = aws_lb.main.arn_suffix
}

output "target_group_arn_suffixes" {
  description = "Target Group ARN suffix 맵 (CloudWatch용)"
  value       = { for k, v in aws_lb_target_group.services : k => v.arn_suffix }
}
