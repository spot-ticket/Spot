# =============================================================================
# Cognito
# =============================================================================
resource "aws_cognito_user_pool" "pool" {
  name = "spot_cognito_user_pool"

  schema {
    name                = "user_id"
    attribute_data_type = "String"
    mutable             = true
    required            = false
  }

  schema {
    name                = "role"
    attribute_data_type = "String"
    mutable             = true
    required            = false
  }


  lambda_config {
    post_confirmation = aws_lambda_function.post_confirm.arn

    # access token customization을 위해 "pre_token_generation_config" 사용(Trigger event version V2_0)
    pre_token_generation_config {
      lambda_arn     = aws_lambda_function.pre_token.arn
      lambda_version = "V2_0"
    }
  }
}

# =============================================================================
# Cognito Client
# =============================================================================

resource "aws_cognito_user_pool_client" "app_client" {
  name         = "spot-app-client"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret = false # FE로 로그인/회원가입

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH"
  ]

  refresh_token_rotation {
    feature                    = "ENABLED"
    # 네트워크 장애 고려한 기존 refresh token 10초 유예시간
    retry_grace_period_seconds = 10
  }
}



# =============================================================================
# iam
# =============================================================================
data "aws_iam_policy_document" "lambda_assume" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

# Post Confirmation Role
resource "aws_iam_role" "lambda_post_confirm" {
  name               = "spot-lambda-post-confirm"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy" "lambda_post_confirm_policy" {
  role = aws_iam_role.lambda_post_confirm.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # logs
      {
        Effect = "Allow"
        Action = ["logs:CreateLogGroup","logs:CreateLogStream","logs:PutLogEvents"]
        Resource = "*"
      },
      # Cognito custom attribute 업데이트용
      {
        Effect = "Allow"
        Action = ["cognito-idp:AdminUpdateUserAttributes"]
        Resource = aws_cognito_user_pool.pool.arn
      }
    ]
  })
}

# Pre Token Role
resource "aws_iam_role" "lambda_pre_token" {
  name               = "spot-lambda-pre-token"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy" "lambda_pre_token_policy" {
  role = aws_iam_role.lambda_pre_token.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = ["logs:CreateLogGroup","logs:CreateLogStream","logs:PutLogEvents"]
        Resource = "*"
      }
    ]
  })
}

# Cognito가 Lambda 호출하는 permission 연결(AccessDenied 방지)
resource "aws_lambda_permission" "allow_cognito_post_confirm" {
  statement_id  = "AllowCognitoInvokePostConfirm"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.post_confirm.function_name
  principal     = "cognito-idp.amazonaws.com"
  source_arn    = aws_cognito_user_pool.pool.arn
}

resource "aws_lambda_permission" "allow_cognito_pre_token" {
  statement_id  = "AllowCognitoInvokePreToken"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.pre_token.function_name
  principal     = "cognito-idp.amazonaws.com"
  source_arn    = aws_cognito_user_pool.pool.arn
}



# =============================================================================
# lambda
# =============================================================================
resource "aws_lambda_function" "post_confirm" {
  function_name = "spot-post-confirm"
  role          = aws_iam_role.lambda_post_confirm.arn
  handler       = "post_confirm.lambda_handler"
  runtime       = "python3.12"
  timeout       = 10

  filename         = "${path.module}/lambda/post_confirm.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda/post_confirm.zip")

  environment {
    variables = {
      USER_SERVICE_URL = var.user_service_url
    }
  }
}

resource "aws_lambda_function" "pre_token" {
  function_name = "spot-pre-token"
  role          = aws_iam_role.lambda_pre_token.arn
  handler       = "pre_token.lambda_handler"
  runtime       = "python3.12"
  timeout       = 5

  filename         = "${path.module}/lambda/pre_token.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda/pre_token.zip")
}
