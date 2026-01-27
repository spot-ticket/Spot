# =============================================================================
# ECR Repositories (Multiple Services)
# =============================================================================
resource "aws_ecr_repository" "services" {
  for_each = var.service_names

  name                 = "${var.project}-${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = merge(var.common_tags, {
    Name    = "${var.name_prefix}-ecr-${each.key}"
    Service = each.key
  })
}

# =============================================================================
# ECR Lifecycle Policy (per service)
# =============================================================================
resource "aws_ecr_lifecycle_policy" "services" {
  for_each   = var.service_names
  repository = aws_ecr_repository.services[each.key].name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep last ${var.image_retention_count} images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = var.image_retention_count
      }
      action = {
        type = "expire"
      }
    }]
  })
}
