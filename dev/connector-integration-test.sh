#!/bin/bash

set -euo pipefail
set -x

curl -O https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
chmod +x wait-for-it.sh
curl -O https://raw.githubusercontent.com/kadwanev/retry/master/retry
chmod +x retry
./wait-for-it.sh -t 60 provider:8181
./wait-for-it.sh -t 60 consumer:8181
./wait-for-it.sh -t 60 prs:8080

# Send request to consumer connector
requestId=$(curl -f -X POST http://consumer:8181/api/v0.1/retrievePartsTree -H "Content-type:application/json" -d '{"byObjectIdRequest": {
    "oneIDManufacturer": "BMW MUC", "objectIDManufacturer": "YS3DD78N4X7055320", "view": "AS_BUILT", "aspect": "MATERIAL", "depth": 2}}')

# Poll status endpoint until job completed
stateUrl="http://consumer:8181/api/v0.1/datarequest/$requestId/state"
./retry -s 1 -t 120 "test \$(curl -f -o /dev/null -s -w '%{http_code}' $stateUrl) == 200"

# Get results using SAS URL
sasUrl=$(curl -f $stateUrl)
curl "$sasUrl"
curl "$sasUrl" | grep "relationships\":"
