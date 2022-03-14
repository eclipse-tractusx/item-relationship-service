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

output "irs_db_fqdn" {
  value       = module.irs_postgresql.fqdn
  description = "The PostgreSQL FQDN."
}

output "irs_db_administrator_username" {
  value       = module.irs_postgresql.administrator_username
  description = "The administrator user login name."
}

output "irs_db_administrator_login_password" {
  value       = module.irs_postgresql.administrator_login_password
  description = "The administrator user password."
  sensitive   = true
}

output "irs_db_name" {
  value       = module.irs_postgresql.db_name
  description = "The PostgreSQL database name."
}
