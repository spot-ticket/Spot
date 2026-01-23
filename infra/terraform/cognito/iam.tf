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
