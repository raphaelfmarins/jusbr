variable "aws_region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Prefix for resource names and value of the Project cost tag."
  type        = string
  default     = "jusbr"
}

variable "vpc_cidr" {
  description = "CIDR block of the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "image_tag" {
  description = "Image tag deployed for both backend and frontend."
  type        = string
  default     = "latest"
}

variable "ecr_keep_last_images" {
  description = "Number of images kept per ECR repository."
  type        = number
  default     = 10
}

variable "log_retention_days" {
  description = "CloudWatch log retention for the ECS services."
  type        = number
  default     = 14
}

variable "db_name" {
  description = "PostgreSQL database name."
  type        = string
  default     = "shortener"
}

variable "db_username" {
  description = "PostgreSQL master username. The password is generated and stored by RDS in Secrets Manager."
  type        = string
  default     = "shortener"
}

variable "db_deletion_protection" {
  description = "Protects the RDS instance from deletion. Set to false before destroying."
  type        = bool
  default     = true
}

variable "db_skip_final_snapshot" {
  description = "Skips the final snapshot when the RDS instance is destroyed."
  type        = bool
  default     = false
}

variable "cloudflare_zone_name" {
  description = "Cloudflare zone that hosts the application domain."
  type        = string
  default     = "precohonesto.com"
}

variable "domain_name" {
  description = "Public domain of the application."
  type        = string
  default     = "jusbr.precohonesto.com"
}
