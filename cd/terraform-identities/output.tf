output "tenant_id" {
  value       = data.azuread_client_config.current.tenant_id
  description = "Azure Active Directory tenant ID."
}

output "vault_name" {
  value       = azurerm_key_vault.identities.name
  description = "Name of the Azure Key Vault storing generated credentials."
}

output "irs_connector_consumer_client_id" {
  value       = azuread_service_principal.irs-connector-consumer.application_id
  description = "Client ID (Application ID) of the service principal generated for the IRS Connector Consumer."
}

output "irs_connector_consumer_object_id" {
  value       = azuread_service_principal.irs-connector-consumer.object_id
  description = "Object ID of the service principal generated for the IRS Connector Consumer."
}

output "irs_connector_consumer_cert_name" {
  value       = azurerm_key_vault_certificate.irs-connector-consumer.name
  description = "Name of the Certificate in Azure Key Vault for the service principal generated for the IRS Connector Consumer."
}
