output "dataexchange_storage_account_name" {
  value = azurerm_storage_account.consumer-dataexchange.name
}

output "key_vault_name" {
  value = azurerm_key_vault.consumer-vault.name
}
