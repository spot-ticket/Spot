output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.main.arn
}

output "alb_dns_name" {
  description = "ALB DNS 이름"
  value       = aws_lb.main.dns_name
}

output "target_group_arn" {
  description = "Target Group ARN"
  value       = aws_lb_target_group.blue.arn
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
