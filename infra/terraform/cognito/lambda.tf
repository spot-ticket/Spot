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
