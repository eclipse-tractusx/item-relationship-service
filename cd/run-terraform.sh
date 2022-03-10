#!/bin/bash

set -euo pipefail # safe mode
set -a # export all variables

# Settings and credential for Terraform azurerm provider
# See https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/guides/service_principal_client_secret#configuring-the-service-principal-in-terraform
ARM_CLIENT_ID=$(jq -r .clientId <<< $AZURE_CREDENTIALS)
ARM_CLIENT_SECRET=$(jq -r .clientSecret <<< $AZURE_CREDENTIALS)
ARM_TENANT_ID=$(jq -r .tenantId <<< $AZURE_CREDENTIALS)
ARM_SUBSCRIPTION_ID=$(az account show --query "id" --output tsv)

# Output summary information
echo
echo "[Terraform settings]"
echo "Service principal ID: $ARM_CLIENT_ID"
echo "Tenant ID: $ARM_TENANT_ID"
echo "Subscription ID: $ARM_SUBSCRIPTION_ID"
echo "Terraform state blob file: $TERRAFORM_STATE_BLOB"
echo
echo "[Terraform variables]"
cat $TERRAFORM_VARS
echo

. $TERRAFORM_VARS

# Set appropriate config variables for automated run
# See https://www.terraform.io/docs/cli/config/environment-variables.html
TF_INPUT=0 # do not hang the build when inputs are missing
TF_IN_AUTOMATION=1 # remove unnecessary output

# Init terraform
terraform init -backend-config=key=$TERRAFORM_STATE_BLOB

# Apply terraform (also includes plan)
terraform apply -auto-approve

# Fetch outputs
terraform output -json > terraform-outputs.json

# Make non-sensitive outputs available as a file (for artifact generation)
jq 'map_values(select(.sensitive | not))' terraform-outputs.json > terraform-outputs-safe.json

# Make non-sensitive outputs available as GitHub task outputs
# See https://docs.github.com/actions/learn-github-actions/workflow-commands-for-github-actions#setting-an-output-parameter
jq -r '. | to_entries | .[] | "::set-output name=" + .key + "::" + .value.value' terraform-outputs-safe.json
