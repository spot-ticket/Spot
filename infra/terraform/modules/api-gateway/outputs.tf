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
