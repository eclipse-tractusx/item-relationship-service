output "api_url" {
  value       = local.api_url
  description = "The API URL prefix."
}

output "connector_url" {
  value       = local.connector_url
  description = "The URL prefix for the EDC Connector (Provider)."
}

output "dataspace_partition" {
  value       = var.dataspace_partition
  description = "The dataspace partition short code."
}

output "prs_db_fqdn" {
  value       = module.prs_postgresql.fqdn
  description = "The PostgreSQL FQDN."
}

output "prs_db_administrator_username" {
  value       = module.prs_postgresql.administrator_username
  description = "The administrator user login name."
}

output "prs_db_administrator_login_password" {
  value       = module.prs_postgresql.administrator_login_password
  description = "The administrator user password."
  sensitive   = true
}

output "prs_db_name" {
  value       = module.prs_postgresql.db_name
  description = "The PostgreSQL database name."
}
