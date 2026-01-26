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
# Cognito User Pool
# =============================================================================
resource "aws_cognito_user_pool" "main" {
  count = var.enable_cognito ? 1 : 0
  name  = var.cognito_user_pool_name != null ? var.cognito_user_pool_name : "${var.name_prefix}-user-pool"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length                   = 8
    require_lowercase                = true
    require_numbers                  = true
    require_symbols                  = true
    require_uppercase                = true
    temporary_password_validity_days = 7
  }

  email_configuration {
    email_sending_account = "COGNITO_DEFAULT"
  }

  mfa_configuration = "OPTIONAL"

  software_token_mfa_configuration {
    enabled = true
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  schema {
    name                     = "email"
    attribute_data_type      = "String"
    mutable                  = true
    required                 = true
    string_attribute_constraints {
      min_length = 1
      max_length = 256
    }
  }

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-user-pool" })
}

resource "aws_cognito_user_pool_client" "main" {
  count        = var.enable_cognito ? 1 : 0
  name         = "${var.name_prefix}-api-client"
  user_pool_id = aws_cognito_user_pool.main[0].id

  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code", "implicit"]
  allowed_oauth_scopes                 = ["email", "openid", "profile"]
  callback_urls                        = var.cognito_callback_urls
  logout_urls                          = var.cognito_logout_urls
  supported_identity_providers         = ["COGNITO"]

  explicit_auth_flows = [
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_SRP_AUTH",
    "ALLOW_USER_PASSWORD_AUTH"
  ]

  access_token_validity  = 1
  id_token_validity      = 1
  refresh_token_validity = 30

  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }
}

resource "aws_cognito_user_pool_domain" "main" {
  count        = var.enable_cognito ? 1 : 0
  domain       = var.name_prefix
  user_pool_id = aws_cognito_user_pool.main[0].id
}

# =============================================================================
# JWT Authorizer
# =============================================================================
resource "aws_apigatewayv2_authorizer" "cognito" {
  count = var.enable_cognito ? 1 : 0

  api_id           = aws_apigatewayv2_api.main.id
  authorizer_type  = "JWT"
  name             = "${var.name_prefix}-cognito-authorizer"
  identity_sources = ["$request.header.Authorization"]

  jwt_configuration {
    audience = [aws_cognito_user_pool_client.main[0].id]
    issuer   = "https://${aws_cognito_user_pool.main[0].endpoint}"
  }
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
  authorizer_id      = aws_apigatewayv2_authorizer.cognito[0].id
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
