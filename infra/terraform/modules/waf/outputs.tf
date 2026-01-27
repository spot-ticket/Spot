output "web_acl_arn" {
  description = "WAF Web ACL ARN"
  value       = aws_wafv2_web_acl.main.arn
}

output "web_acl_id" {
  description = "WAF Web ACL ID"
  value       = aws_wafv2_web_acl.main.id
}

output "web_acl_name" {
  description = "WAF Web ACL 이름"
  value       = aws_wafv2_web_acl.main.name
}

output "log_group_name" {
  description = "WAF 로그 그룹 이름"
  value       = aws_cloudwatch_log_group.waf.name
}
