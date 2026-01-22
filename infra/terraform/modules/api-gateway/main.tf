# =============================================================================
# API Gateway (HTTP API)
# =============================================================================
resource "aws_apigatewayv2_api" "main" {
  name          = "${var.name_prefix}-api"
  protocol_type = "HTTP"

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
# Integration (VPC Link → ALB)
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
# Route
# =============================================================================
resource "aws_apigatewayv2_route" "main" {
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

  tags = merge(var.common_tags, { Name = "${var.name_prefix}-stage" })
}
