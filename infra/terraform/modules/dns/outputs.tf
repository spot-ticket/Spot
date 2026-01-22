output "zone_id" {
  description = "Route 53 Hosted Zone ID"
  value       = aws_route53_zone.main.zone_id
}

output "zone_name" {
  description = "Route 53 Hosted Zone 이름"
  value       = aws_route53_zone.main.name
}

output "name_servers" {
  description = "Route 53 네임서버 목록 (도메인 등록 기관에 설정 필요)"
  value       = aws_route53_zone.main.name_servers
}

output "certificate_arn" {
  description = "ACM 인증서 ARN"
  value       = aws_acm_certificate_validation.main.certificate_arn
}

output "api_domain" {
  description = "API 커스텀 도메인"
  value       = var.create_api_domain ? "api.${var.domain_name}" : null
}

output "api_domain_target" {
  description = "API Gateway 커스텀 도메인의 target domain name"
  value       = var.create_api_domain ? aws_apigatewayv2_domain_name.api[0].domain_name_configuration[0].target_domain_name : null
}
