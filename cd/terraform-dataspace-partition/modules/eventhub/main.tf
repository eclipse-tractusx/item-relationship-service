resource "azurerm_eventhub" "main" {
  name                = var.name
  namespace_name      = var.eventhub_namespace_name
  resource_group_name = var.resource_group_name
  partition_count     = var.partition_count
  message_retention   = var.message_retention
}

resource "azurerm_eventhub" "deadletter" {
  name                = "${var.name}.dlt" # name pattern used by Spring-Kafka by convention
  namespace_name      = var.eventhub_namespace_name
  resource_group_name = var.resource_group_name
  partition_count     = var.partition_count
  message_retention   = var.message_retention

  capture_description {
    enabled             = true
    encoding            = "Avro"
    skip_empty_archives = true

    destination {
      name                = "EventHubArchive.AzureBlockBlob" #default and only supported value
      archive_name_format = "{Namespace}/{EventHub}/{PartitionId}/{Year}/{Month}/{Day}/{Hour}/{Minute}/{Second}"
      blob_container_name = azurerm_storage_container.dlt_capture.name
      storage_account_id  = azurerm_storage_account.capture.id
    }
  }
}

resource "azurerm_storage_account" "capture" {
  name                     = var.capture_storage_account_name
  resource_group_name      = var.resource_group_name
  location                 = var.location
  account_tier             = "Standard"
  account_replication_type = "GRS"
}

resource "azurerm_storage_container" "dlt_capture" {
  name                 = "deadletter"
  storage_account_name = azurerm_storage_account.capture.name
}

# Authorization rule to send messages only.
resource "azurerm_eventhub_authorization_rule" "send" {
  name                = "Send"
  namespace_name      = var.eventhub_namespace_name
  resource_group_name = azurerm_eventhub.main.resource_group_name
  eventhub_name       = azurerm_eventhub.main.name

  listen = false
  send   = true
  manage = false
}
