#!/bin/bash

set -euo pipefail

echo "Environment: $ENVIRONMENT"
echo "Data space partition: $TF_VAR_dataspace_partition"

shutdown_on_error() {
    exit 1
}

trap shutdown_on_error INT TERM ERR
