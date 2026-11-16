output "app_url" {
  description = "Public URL of the application."
  value       = local.public_url
}

output "alb_dns_name" {
  description = "ALB DNS name (target of the Cloudflare CNAME)."
  value       = aws_lb.main.dns_name
}

output "ecr_repository_urls" {
  description = "ECR repository URLs by app."
  value       = { for app, repo in aws_ecr_repository.app : app => repo.repository_url }
}

output "ecs_cluster_name" {
  description = "ECS cluster name."
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_names" {
  description = "ECS service names by app."
  value = {
    backend  = aws_ecs_service.backend.name
    frontend = aws_ecs_service.frontend.name
  }
}

output "db_secret_arn" {
  description = "ARN of the Secrets Manager secret holding the RDS master credentials."
  value       = local.db_secret
}
