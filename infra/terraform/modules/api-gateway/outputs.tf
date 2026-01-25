output "api_endpoint" {
  description = "API Gateway 엔드포인트 URL"
  value       = aws_apigatewayv2_api.main.api_endpoint
}

output "api_id" {
  description = "API Gateway ID"
  value       = aws_apigatewayv2_api.main.id
}

output "stage_arn" {
  description = "API Gateway Stage ARN (WAF 연결용)"
  value       = aws_apigatewayv2_stage.main.arn
}

output "execution_arn" {
  description = "API Gateway Execution ARN"
  value       = aws_apigatewayv2_api.main.execution_arn
}

# =============================================================================
# Cognito Outputs
# =============================================================================
output "cognito_user_pool_id" {
  description = "Cognito User Pool ID"
  value       = var.enable_cognito ? aws_cognito_user_pool.main[0].id : null
}

output "cognito_user_pool_client_id" {
  description = "Cognito User Pool Client ID"
  value       = var.enable_cognito ? aws_cognito_user_pool_client.main[0].id : null
}

output "cognito_user_pool_endpoint" {
  description = "Cognito User Pool Endpoint"
  value       = var.enable_cognito ? aws_cognito_user_pool.main[0].endpoint : null
}

output "cognito_domain" {
  description = "Cognito Domain URL"
  value       = var.enable_cognito ? "https://${aws_cognito_user_pool_domain.main[0].domain}.auth.ap-northeast-2.amazoncognito.com" : null
}
