locals {
  apps = toset(["backend", "frontend"])
}

resource "aws_ecr_repository" "app" {
  for_each = local.apps

  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = "MUTABLE"
  # Images are rebuildable artifacts; allows destroy without emptying the repository first.
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "app" {
  for_each = aws_ecr_repository.app

  repository = each.value.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep last ${var.ecr_keep_last_images} images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = var.ecr_keep_last_images
      }
      action = { type = "expire" }
    }]
  })
}
