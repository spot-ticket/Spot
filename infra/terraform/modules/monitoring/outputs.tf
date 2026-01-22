# =============================================================================
# Monitoring Outputs
# =============================================================================
output "sns_topic_arn" {
  description = "알람 알림 SNS Topic ARN"
  value       = aws_sns_topic.alerts.arn
}

output "sns_topic_name" {
  description = "알람 알림 SNS Topic 이름"
  value       = aws_sns_topic.alerts.name
}

output "alarm_arns" {
  description = "생성된 CloudWatch Alarm ARN 목록"
  value = {
    ecs_cpu         = aws_cloudwatch_metric_alarm.ecs_cpu_high.arn
    ecs_memory      = aws_cloudwatch_metric_alarm.ecs_memory_high.arn
    rds_cpu         = aws_cloudwatch_metric_alarm.rds_cpu_high.arn
    rds_connections = aws_cloudwatch_metric_alarm.rds_connections_high.arn
    rds_storage     = aws_cloudwatch_metric_alarm.rds_storage_low.arn
    alb_5xx         = aws_cloudwatch_metric_alarm.alb_5xx_errors.arn
    alb_response    = aws_cloudwatch_metric_alarm.alb_response_time.arn
  }
}
