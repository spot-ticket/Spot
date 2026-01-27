# =============================================================================
# S3 Outputs
# =============================================================================
output "static_bucket_id" {
  description = "정적 파일 버킷 ID"
  value       = aws_s3_bucket.static.id
}

output "static_bucket_arn" {
  description = "정적 파일 버킷 ARN"
  value       = aws_s3_bucket.static.arn
}

output "static_bucket_domain_name" {
  description = "정적 파일 버킷 도메인"
  value       = aws_s3_bucket.static.bucket_regional_domain_name
}

output "logs_bucket_id" {
  description = "로그 버킷 ID"
  value       = aws_s3_bucket.logs.id
}

output "logs_bucket_arn" {
  description = "로그 버킷 ARN"
  value       = aws_s3_bucket.logs.arn
}

output "logs_bucket_domain_name" {
  description = "로그 버킷 도메인"
  value       = aws_s3_bucket.logs.bucket_regional_domain_name
}
