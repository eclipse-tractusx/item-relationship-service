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

variable "resource_group_name" {
  type        = string
  description = "Resource group used to deploy resources."
  default     = "catenax-dev001-rg"
}