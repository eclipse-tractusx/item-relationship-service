#!/bin/bash
set -euo pipefail
cd ..

if [ ! -f dev/local/cert.pfx ]; then
    echo "Missing file cert.pfx"
    exit 1
fi

export DOCKER_BUILDKIT=1
docker-compose --profile connector build --build-arg IRS_EDC_PKG_USERNAME=$IRS_EDC_PKG_USERNAME --build-arg IRS_EDC_PKG_PASSWORD=$IRS_EDC_PKG_PASSWORD
docker-compose --profile connector --profile irs up --exit-code-from=connector-integration-test --abort-on-container-exit
