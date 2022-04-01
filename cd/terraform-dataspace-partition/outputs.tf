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
