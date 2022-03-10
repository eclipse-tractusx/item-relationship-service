terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "2.60.0"
    }
  }

  # Persist state in a storage account
  backend "azurerm" {
    resource_group_name  = "catenax-terraform"
    storage_account_name = "catenaxterraformstate"
    container_name       = "tfstate"
    # Key will be overriden with "terraform init -backend-config=key=${TERRAFORM_STATE_KEY}"
    key = "prs.prs.dev.terraform.tfstate"
  }

  required_version = "~> 1.0"
}

provider "azurerm" {
  features {}
}

data "azurerm_resource_group" "main" {
  name = var.resource_group_name
}

locals {
  resource_group_name = data.azurerm_resource_group.main.name
  location            = data.azurerm_resource_group.main.location
}
