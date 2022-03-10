resource "azurerm_eventhub_namespace" "main" {
  name                = var.name
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = "Standard"
  capacity            = var.capacity
}

# Authorization rule to receive messages. Also allows sending messages (used for dead-letter topic).
resource "azurerm_eventhub_namespace_authorization_rule" "receive_and_send" {
  name                = "ReceiveAndSend"
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = azurerm_eventhub_namespace.main.resource_group_name

  listen = true
  send   = true
  manage = false
}
