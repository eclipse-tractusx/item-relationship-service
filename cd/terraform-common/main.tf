####################################################################################################
# PRS common infrastructure
####################################################################################################

module "prs_application_insights" {
  source = "./modules/application-insights"

  name                = "${var.prefix}-${var.environment}-prs-appi"
  resource_group_name = local.resource_group_name
  location            = local.location
}
