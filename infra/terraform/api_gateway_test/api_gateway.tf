// *************** //
// AWS API Gateway //
// *************** //
resource "aws_apigatewayv2_api" "spot" {
  name          = "spot-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["https://spotorder.org"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["Content-Type", "Authorization"]
    max_age       = 300
  }

  tags = {
    Name        = "spot-api-gateway"
    Environment = var.environment
  }
}

resource "aws_apigatewayv2_authorizer" "role_based" {
  api_id                            = aws_apigatewayv2_api.spot.id
  authorizer_type                   = "REQUEST"
  authorizer_uri                    = aws_lambda_function.authorizer.invoke_arn
  identity_sources                  = ["$request.header.Authorization"]
  name                              = "role-based-authorizer"
  authorizer_payload_format_version = "2.0"
  enable_simple_responses           = true
  authorizer_result_ttl_in_seconds  = 300  # 5분 캐싱
}

resource "aws_apigatewayv2_stage" "prod" {
  api_id      = aws_apigatewayv2_api.spot.id
  name        = "prod"
  auto_deploy = true

  default_route_settings {
    throttling_burst_limit = 100   # 순간 최대 요청
    throttling_rate_limit  = 50    # 초당 요청 수
  }

  # 주문 API는 더 낮은 제한 - 라우트 연결 후 활성화
  # route_settings {
  #   route_key              = "ANY /api/orders/{proxy+}"
  #   throttling_burst_limit = 20
  #   throttling_rate_limit  = 10
  # }

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gateway.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      routeKey       = "$context.routeKey"
      status         = "$context.status"
      responseLength = "$context.responseLength"
    })
  }

  tags = {
    Name        = "spot-api-stage-prod"
    Environment = var.environment
  }
}


# ============================================
# 인증 불필요 (로그인/회원가입)
# ============================================
resource "aws_apigatewayv2_route" "auth" {
  api_id    = aws_apigatewayv2_api.spot.id
  route_key = "ANY /api/auth/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.ecs.id}"
}

# ============================================
# CUSTOMER 전용 라우팅
# ============================================
resource "aws_apigatewayv2_route" "customer_orders" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/customer/orders/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

resource "aws_apigatewayv2_route" "payments" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/payments/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

resource "aws_apigatewayv2_route" "reviews" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/reviews/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

# ============================================
# CHEF 전용 라우팅
# ============================================
resource "aws_apigatewayv2_route" "chef_orders" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/chef/orders/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

# ============================================
# OWNER 전용 라우팅
# ============================================
resource "aws_apigatewayv2_route" "owner_orders" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/owner/orders/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

resource "aws_apigatewayv2_route" "stores" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/stores/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

resource "aws_apigatewayv2_route" "menus" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/menus/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

resource "aws_apigatewayv2_route" "sales" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/sales/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

# ============================================
# MASTER (관리자) 전용 라우팅
# ============================================
resource "aws_apigatewayv2_route" "admin" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/admin/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

# ============================================
# 공통 (모든 인증된 사용자)
# ============================================
resource "aws_apigatewayv2_route" "users" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "ANY /api/users/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

resource "aws_apigatewayv2_route" "categories" {
  api_id             = aws_apigatewayv2_api.spot.id
  route_key          = "GET /api/categories/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ecs.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.role_based.id
}

// ******************** //
// lambda Authorization //
// ******************** //
resource "aws_lambda_function" "authorizer" {
  function_name = "spot-api-authorizer"
  runtime       = "nodejs18.x"
  handler       = "index.handler"
  role          = aws_iam_role.authorizer_lambda.arn

  filename         = data.archive_file.authorizer.output_path
  source_code_hash = data.archive_file.authorizer.output_base64sha256

  environment {
    variables = {
      COGNITO_USER_POOL_ID = aws_cognito_user_pool.spot.id
      COGNITO_CLIENT_ID    = aws_cognito_user_pool_client.spot.id
    }
  }
}


// ****************** //
// API Gateway -> ECS //
// ****************** //
# 단일 통합 - 모든 라우트가 이 통합을 사용
resource "aws_apigatewayv2_integration" "ecs" {
  api_id             = aws_apigatewayv2_api.spot.id
  integration_type   = "HTTP_PROXY"
  integration_uri    = aws_service_discovery_service.spot.arn
  integration_method = "ANY"
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.spot.id
}

# VPC Link
resource "aws_apigatewayv2_vpc_link" "spot" {
  name               = "spot-vpc-link"
  security_group_ids = [aws_security_group.vpc_link.id]
  subnet_ids         = [aws_subnet.private_a.id, aws_subnet.private_c.id]

  tags = {
    Name = "spot-vpc-link"
  }
}

// **************** //
// CloudWatch Logs  //
// **************** //
resource "aws_cloudwatch_log_group" "api_gateway" {
  name              = "/aws/apigateway/spot-api"
  retention_in_days = 30

  tags = {
    Name        = "spot-api-gateway-logs"
    Environment = var.environment
  }
}

// ************** //
// IAM for Lambda //
// ************** //
resource "aws_iam_role" "authorizer_lambda" {
  name = "spot-authorizer-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "spot-authorizer-lambda-role"
  }
}

resource "aws_iam_role_policy_attachment" "authorizer_lambda_basic" {
  role       = aws_iam_role.authorizer_lambda.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

// ********************** //
// Lambda Authorizer Code //
// ********************** //
data "archive_file" "authorizer" {
  type        = "zip"
  output_path = "${path.module}/authorizer.zip"

  source {
    content  = <<EOF
exports.handler = async (event) => {
  const token = event.headers?.authorization || event.headers?.Authorization;

  if (!token) {
    return {
      isAuthorized: false
    };
  }

  // TODO: Implement actual token validation with Cognito
  return {
    isAuthorized: true,
    context: {
      userId: "user-id",
      role: "CUSTOMER"
    }
  };
};
EOF
    filename = "index.js"
  }
}

// ************************ //
// Cognito User Pool Client //
// ************************ //
resource "aws_cognito_user_pool_client" "spot" {
  name         = "spot-app-client"
  user_pool_id = aws_cognito_user_pool.spot.id

  generate_secret = false

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_SRP_AUTH"
  ]

  supported_identity_providers = ["COGNITO"]

  callback_urls = ["https://spotorder.org/callback"]
  logout_urls   = ["https://spotorder.org/logout"]

  allowed_oauth_flows                  = ["code", "implicit"]
  allowed_oauth_scopes                 = ["email", "openid", "profile"]
  allowed_oauth_flows_user_pool_client = true
}

// *************************** //
// Lambda Permission for APIGW //
// *************************** //
resource "aws_lambda_permission" "api_gateway_authorizer" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.authorizer.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.spot.execution_arn}/*/*"
}

# Cloud Map Service Discovery
resource "aws_service_discovery_private_dns_namespace" "spot" {
  name        = "spot.local"
  vpc         = aws_vpc.main.id
  description = "Service discovery namespace for Spot"
}

resource "aws_service_discovery_service" "spot" {
  name = "api"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.spot.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_cognito_user_pool" "spot" {
  name = "spot-user-pool"

  # 사용자명 설정
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  # 비밀번호 정책
  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = false
    require_uppercase = true
  }

  # 계정 복구
  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  tags = {
    Name        = "spot-cognito-user-pool"
    Environment = var.environment
  }
}
