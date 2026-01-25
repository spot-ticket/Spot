# =============================================================================
# CodeDeploy Application (for Blue/Green ECS Deployment)
# =============================================================================
resource "aws_codedeploy_app" "main" {
  count            = var.enable_blue_green ? 1 : 0
  compute_platform = "ECS"
  name             = "${var.name_prefix}-ecs-app"

  tags = var.common_tags
}

# =============================================================================
# CodeDeploy IAM Role
# =============================================================================
resource "aws_iam_role" "codedeploy" {
  count = var.enable_blue_green ? 1 : 0
  name  = "${var.name_prefix}-codedeploy-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "codedeploy.amazonaws.com"
      }
    }]
  })

  tags = var.common_tags
}

resource "aws_iam_role_policy_attachment" "codedeploy" {
  count      = var.enable_blue_green ? 1 : 0
  role       = aws_iam_role.codedeploy[0].name
  policy_arn = "arn:aws:iam::aws:policy/AWSCodeDeployRoleForECS"
}

# =============================================================================
# CodeDeploy Deployment Groups (per service)
# =============================================================================
resource "aws_codedeploy_deployment_group" "services" {
  for_each = var.enable_blue_green ? local.active_services : {}

  app_name               = aws_codedeploy_app.main[0].name
  deployment_group_name  = "${var.name_prefix}-${each.key}-dg"
  deployment_config_name = var.deployment_config
  service_role_arn       = aws_iam_role.codedeploy[0].arn

  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE"]
  }

  blue_green_deployment_config {
    deployment_ready_option {
      action_on_timeout = "CONTINUE_DEPLOYMENT"
    }

    terminate_blue_instances_on_deployment_success {
      action                           = "TERMINATE"
      termination_wait_time_in_minutes = var.termination_wait_time
    }
  }

  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
    deployment_type   = "BLUE_GREEN"
  }

  ecs_service {
    cluster_name = aws_ecs_cluster.main.name
    service_name = aws_ecs_service.services[each.key].name
  }

  load_balancer_info {
    target_group_pair_info {
      prod_traffic_route {
        listener_arns = [var.alb_listener_arn]
      }

      target_group {
        name = var.target_group_names[each.key]
      }

      target_group {
        name = lookup(var.target_group_names, "${each.key}-green", "${var.name_prefix}-${each.key}-tg-g")
      }
    }
  }

  tags = merge(var.common_tags, { Service = each.key })
}
