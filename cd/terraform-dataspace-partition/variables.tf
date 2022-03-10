####################################################################################################
# Global variables
####################################################################################################

variable "prefix" {
  type        = string
  description = "First part of name prefix used in naming resources. Use only lowercase letters and numbers."
  default     = "cxmtpdc1"
}

variable "environment" {
  type        = string
  description = "Second part of name prefix used in naming resources. Use only lowercase letters and numbers."
  default     = "dev"
}

variable "dataspace_partition" {
  type        = string
  description = "Third part of name prefix used in naming resources. Use only lowercase letters and numbers."
  default     = "bmw"
}

variable "resource_group_name" {
  type        = string
  description = "Resource group used to deploy resources."
  default     = "catenax-dev001-rg"
}

variable "application_insights_name" {
  type        = string
  description = "The Application Insights resource name."
  default     = "cxmtpdc1-dev-prs-appi"
}

variable "aks_cluster_name" {
  type        = string
  description = "Azure Kubernetes cluster to deploy in."
  default     = "catenax-dev001-aks-services"
}

variable "image_registry" {
  type        = string
  description = "Registry containing PRS images."
  default     = "catenaxdev001acr.azurecr.io"
}

variable "release_name" {
  type        = string
  description = "Helm release name."
  default     = "prs"
}

variable "ingress_host" {
  type        = string
  description = "Ingress host to reach the application."
  default     = "catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com"
}

variable "ingress_class_name" {
  type        = string
  description = "Ingress class name for the given environment (\"nginx\" for DEV and \"service\" for INT)"
  default     = "nginx"
}

variable "image_tag" {
  type        = string
  description = "PRS image tag that will be deployed."
}
