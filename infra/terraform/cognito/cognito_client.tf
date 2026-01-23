
resource "aws_cognito_user_pool_client" "app_client" {
  name         = "spot-app-client"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret = false

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH"
  ]

  refresh_token_rotation {
    feature                    = "ENABLED"
    # 네트워크 장애 고려한 기존 refresh token 10초 유예시간
    retry_grace_period_seconds = 10
  }
}


