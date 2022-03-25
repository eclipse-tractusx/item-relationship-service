####################################################################################################
# IRS common infrastructure
####################################################################################################

module "irs_application_insights" {
  source = "./modules/application-insights"

  name                = "${var.prefix}-${var.environment}-irs-api"
  resource_group_name = local.resource_group_name
  location            = local.location
}
