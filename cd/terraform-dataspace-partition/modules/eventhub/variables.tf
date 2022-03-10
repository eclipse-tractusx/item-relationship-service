variable "eventhub_namespace_name" {
  type        = string
  description = "Namespace name."
}

variable "name" {
  type        = string
  description = "Event hub name (Kafka topic name)."
}

variable "location" {
  type        = string
  description = "Azure region where to create resources."
}

variable "resource_group_name" {
  type        = string
  description = "Resource group to deploy in."
}

variable "partition_count" {
  type        = number
  default     = 2
  description = "Event hub partition count."
}

variable "message_retention" {
  type        = number
  default     = 7
  description = "Number of days to retain data, max 7."
}

variable "capture_storage_account_name" {
  type        = string
  description = "Name of the storage account for long-term dead-letter message retention."
}

variable "receive_and_send_primary_connection_string" {
  type        = string
  description = "The primary connection string to receive and send events."
  sensitive   = true
}
