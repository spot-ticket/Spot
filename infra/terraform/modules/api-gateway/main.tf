# =============================================================================
# API Gateway (HTTP API)
# =============================================================================
resource "aws_apigatewayv2_api" "main" {
  name          = "${var.name_prefix}-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins     = ["*"]
    allow_methods     = ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
    allow_headers     = ["Content-Type", "Authorization", "X-Requested-With"]
    expose_headers    = ["X-Request-Id"]
    max_age           = 3600
    allow_credentials = false
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-api" })
}

# =============================================================================
# VPC Link
# =============================================================================
resource "aws_apigatewayv2_vpc_link" "main" {
  name               = "${var.name_prefix}-vpc-link"
  security_group_ids = [var.ecs_security_group_id]
  subnet_ids         = var.subnet_ids

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-vpc-link" })
}

# =============================================================================
# Integration (VPC Link -> ALB)
# =============================================================================
resource "aws_apigatewayv2_integration" "main" {
  api_id             = aws_apigatewayv2_api.main.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = var.alb_listener_arn
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.main.id

  payload_format_version = "1.0"
}

# =============================================================================
# Routes (Public - No Auth Required)
# =============================================================================
resource "aws_apigatewayv2_route" "public" {
  for_each = var.enable_cognito ? toset(var.public_routes) : toset([])

  api_id    = aws_apigatewayv2_api.main.id
  route_key = "ANY ${each.value}"
  target    = "integrations/${aws_apigatewayv2_integration.main.id}"
}

# =============================================================================
# Routes (Protected - Auth Required)
# =============================================================================
resource "aws_apigatewayv2_route" "protected" {
  for_each = var.enable_cognito ? toset(var.protected_route_patterns) : toset([])

  api_id             = aws_apigatewayv2_api.main.id
  route_key          = "ANY ${each.value}"
  target             = "integrations/${aws_apigatewayv2_integration.main.id}"
  authorizer_id      = aws_apigatewayv2_authorizer.cognito_jwt[0].id
  authorization_type = "JWT"
}

# =============================================================================
# Route (Fallback - When Cognito Disabled)
# =============================================================================
resource "aws_apigatewayv2_route" "main" {
  count = var.enable_cognito ? 0 : 1

  api_id    = aws_apigatewayv2_api.main.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.main.id}"
}

# 로그인 / 회원가입을 위한 공개 라우트
# 개발 이후 삭제할 것
resource "aws_apigatewayv2_route" "public_api_join" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "POST /api/join"
  target    = "integrations/${aws_apigatewayv2_integration.main.id}"

  authorization_type = "NONE"
}

resource "aws_apigatewayv2_route" "public_api_login" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "POST /api/login"
  target    = "integrations/${aws_apigatewayv2_integration.main.id}"

  authorization_type = "NONE"
}



# =============================================================================
# Stage
# =============================================================================
resource "aws_apigatewayv2_stage" "main" {
  api_id      = aws_apigatewayv2_api.main.id
  name        = "$default"
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_logs.arn
    format = jsonencode({
      requestId         = "$context.requestId"
      ip                = "$context.identity.sourceIp"
      requestTime       = "$context.requestTime"
      httpMethod        = "$context.httpMethod"
      routeKey          = "$context.routeKey"
      status            = "$context.status"
      protocol          = "$context.protocol"
      responseLength    = "$context.responseLength"
      integrationError  = "$context.integrationErrorMessage"
      authorizerError   = "$context.authorizer.error"
    })
  }

  default_route_settings {
    detailed_metrics_enabled = true
    throttling_burst_limit   = 5000
    throttling_rate_limit    = 2000
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-stage" })
}

# =============================================================================
# CloudWatch Log Group for API Gateway
# =============================================================================
resource "aws_cloudwatch_log_group" "api_logs" {
  name              = "/aws/apigateway/${var.name_prefix}"
  retention_in_days = 30

  tags = var.common_tags
}

# =============================================================================
# Cognito
# =============================================================================
resource "aws_apigatewayv2_authorizer" "cognito_jwt" {
  count           = var.enable_cognito ? 1 : 0
  api_id          = aws_apigatewayv2_api.main.id
  name            = "${var.name_prefix}-cognito-jwt"
  authorizer_type = "JWT"

  identity_sources = ["$request.header.Authorization"]

  jwt_configuration {
    issuer   = "https://cognito-idp.<region>.amazonaws.com/<userPoolId>"
    audience = [appclientid]
  }
}
