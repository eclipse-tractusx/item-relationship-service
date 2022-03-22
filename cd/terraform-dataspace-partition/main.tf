####################################################################################################
# IRS infrastructure
####################################################################################################

data "azurerm_application_insights" "main" {
  name                = var.application_insights_name
  resource_group_name = data.azurerm_resource_group.main.name
}

module "irs_postgresql" {
  source              = "./modules/postgresql"
  name                = "${var.prefix}-${var.environment}-${var.dataspace_partition}-irs-psql"
  database_name       = "irs"
  resource_group_name = local.resource_group_name
  location            = local.location
}

module "eventhubs_namespace" {
  source              = "./modules/eventhubs_namespace"
  name                = "${var.prefix}-${var.environment}-${var.dataspace_partition}-irs-ehub"
  resource_group_name = local.resource_group_name
  location            = local.location
}

module "eventhub_catenax_events" {
  source                                     = "./modules/eventhub"
  eventhub_namespace_name                    = module.eventhubs_namespace.name
  name                                       = "catenax_events"
  resource_group_name                        = local.resource_group_name
  location                                   = local.location
  capture_storage_account_name               = "${var.prefix}${var.environment}${var.dataspace_partition}msg"
  receive_and_send_primary_connection_string = module.eventhubs_namespace.receive_and_send_primary_connection_string
}

# create namespace for IRS
resource "kubernetes_namespace" "irs" {
  metadata {
    name = "irs-${var.dataspace_partition}"
  }
}

locals {
  ingress_prefix                    = "/${var.dataspace_partition}/mtpdc"
  ingress_prefix_irs                = "${local.ingress_prefix}/irs"
  ingress_prefix_connector_provider = "${local.ingress_prefix}/connector"
  api_url                           = "https://${var.ingress_host}${local.ingress_prefix_irs}"
  connector_url                     = "https://${var.ingress_host}${local.ingress_prefix_connector_provider}"
}

# Deploy the IRS service with Helm
resource "helm_release" "irs" {
  name      = "irs-${var.dataspace_partition}"
  chart     = "../helm/irs"
  namespace = kubernetes_namespace.irs.metadata[0].name
  timeout   = 300

  set {
    name  = "ingress.host"
    value = var.ingress_host
  }

  set {
    name  = "ingress.className"
    value = var.ingress_class_name
  }

  set {
    name  = "ingress.prefix"
    value = local.ingress_prefix_irs
  }

  set {
    name  = "irs.image.repository"
    value = "${var.image_registry}/irs-api"
  }

  set {
    name  = "irs.image.tag"
    value = var.image_tag
  }

  set {
    name  = "irs.apiUrl"
    value = local.api_url
  }

  set_sensitive {
    name  = "irs.env.APPLICATIONINSIGHTS_CONNECTION_STRING"
    value = data.azurerm_application_insights.main.connection_string
  }

  set {
    name  = "irs.env.APPLICATIONINSIGHTS_ROLE_NAME"
    value = "${var.dataspace_partition} IRS"
  }

  set {
    name  = "eventHubs.name"
    value = module.eventhub_catenax_events.eventhub_name
  }

  set {
    name  = "eventHubs.namespace"
    value = module.eventhubs_namespace.name
  }

  set_sensitive {
    name  = "eventHubs.sendConnectionString"
    value = module.eventhub_catenax_events.send_primary_connection_string
  }

  set_sensitive {
    name  = "eventHubs.receiveConnectionString"
    value = module.eventhub_catenax_events.receive_primary_connection_string
  }

  set {
    name  = "postgresql.url"
    value = "jdbc:postgresql://${module.irs_postgresql.fqdn}/${module.irs_postgresql.db_name}?sslmode=require"
  }

  set {
    name  = "postgresql.postgresqlUsername"
    value = module.irs_postgresql.administrator_username
  }

  set_sensitive {
    name  = "postgresql.postgresqlPassword"
    value = module.irs_postgresql.administrator_login_password
  }
}

# Deploy the IRS Provider with Helm
resource "helm_release" "irs-connector-provider" {
  name      = "irs-${var.dataspace_partition}-irs-connector-provider"
  chart     = "../helm/irs-connector-provider"
  namespace = kubernetes_namespace.irs.metadata[0].name
  timeout   = 300

  set {
    name  = "ingress.host"
    value = var.ingress_host
  }

  set {
    name  = "ingress.className"
    value = var.ingress_class_name
  }

  set {
    name  = "ingress.prefix"
    value = local.ingress_prefix_connector_provider
  }

  set {
    name  = "image.repository"
    value = "${var.image_registry}/irs-connector-provider"
  }

  set {
    name  = "image.tag"
    value = var.image_tag
  }

  set_sensitive {
    name  = "provider.env.APPLICATIONINSIGHTS_CONNECTION_STRING"
    value = data.azurerm_application_insights.main.connection_string
  }

  set {
    name  = "provider.env.APPLICATIONINSIGHTS_ROLE_NAME"
    value = "${var.dataspace_partition} Provider"
  }

  set {
    name  = "irs.apiUrl"
    value = local.api_url
  }
}
