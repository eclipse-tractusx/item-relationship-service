output "name" {
  value       = azurerm_eventhub_namespace.main.name
  description = "The event hub namespace."
}

output "receive_and_send_primary_connection_string" {
  value       = azurerm_eventhub_namespace_authorization_rule.receive_and_send.primary_connection_string
  description = "The primary connection string to receive and send events."
  sensitive   = true
}
