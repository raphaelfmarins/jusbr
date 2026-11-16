locals {
  azs = slice(data.aws_availability_zones.available.names, 0, 2)
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "6.5.1"

  name = var.project_name
  cidr = var.vpc_cidr
  azs  = local.azs

  # ALB and Fargate tasks run in public subnets (public IP, no NAT gateway to save cost).
  public_subnets   = [for i in range(2) : cidrsubnet(var.vpc_cidr, 8, i)]
  database_subnets = [for i in range(2) : cidrsubnet(var.vpc_cidr, 8, i + 10)]

  create_database_subnet_group       = true
  create_database_subnet_route_table = true
  enable_nat_gateway                 = false
  enable_dns_hostnames               = true
  enable_dns_support                 = true
}

resource "aws_security_group" "alb" {
  name        = "${var.project_name}-alb"
  description = "Public HTTP/HTTPS access to the ALB"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "HTTP from anywhere (redirected to HTTPS)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS from anywhere"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "backend" {
  name        = "${var.project_name}-backend"
  description = "Backend tasks, reachable only from the ALB"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "App port from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "frontend" {
  name        = "${var.project_name}-frontend"
  description = "Frontend tasks, reachable only from the ALB"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "App port from ALB"
    from_port       = 3000
    to_port         = 3000
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "db" {
  name        = "${var.project_name}-db"
  description = "PostgreSQL, reachable only from backend tasks"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "PostgreSQL from backend tasks"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.backend.id]
  }
}
