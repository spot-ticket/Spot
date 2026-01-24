output "cognito_user_pool_id" {
  value = aws_cognito_user_pool.pool.id
}

output "cognito_app_client_id" {
  value = aws_cognito_user_pool_client.app_client.id
}

output "cognito_issuer_url" {
  value = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.pool.id}"
}
