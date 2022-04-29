# Item Relationship Service

| __Build Status__ | [![build](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/CI-main.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/CI-main.yml) | 
| :--- | :--- |
| __CodeQL__ | [![CodeQL](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/codeql.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/codeql.yml) |
| __License__ | [![GitHub](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/catenax-ng/product-item-relationship-service/blob/main/LICENSE) |

## How to run

The two following subsections provide instructions for running either only the infrastructure on docker-compose and the application in the IDE, or for running the full stack (including the application) in docker-compose.

### Docker-compose + IDE

* Start the necessary infrastructure by running `docker-compose up`

* Start the application from your favorite IDE

### Docker-compose full stack

* Run `docker-compose --profile irs up`

### Docker-compose debug profile

* Run `docker-compose --profile debug up`
* This will start additional containers:
  * [Prometheus](https://prometheus.io/docs/introduction/overview/), a server to collect and query metrics. Prometheus is available at http://localhost:9091/.

## Work with sample data

* Retrieve sample BOM:

```bash
curl -X 'POST' \
  'http://localhost:8080/irs/jobs' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "aspects": [
    "SerialPartTypization"
  ],
  "bomLifecycle": "asBuilt",
  "depth": 1,
  "direction": "downward",
  "globalAssetId": "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6"
}'
  
curl -X 'GET'  'http://localhost:8080/irs/jobs/<jobID from first call>' -H 'accept: application/json'
```

## Swagger UI

### IRS API

- Swagger UI: http://localhost:8080/api/swagger-ui
- API docs: http://localhost:8080/api/api-docs
- API docs in yaml:  http://localhost:8080/api/api-docs.yaml

## Deploy to DEV

The latest version on main is automatically picked up by ArgoCD and deployed to the DEV environment.
See https://catenax-ng.github.io/.

http://irs.int.demo.catena-x.net/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config

## Deploy to INT

Not available yet.

## Smoke tests

[Smoke tests](integration-tests/src/test/java/net/catenax/irs/smoketest) are running against the application and the consumer connectors in the IRS Smoke tests pipeline.
To run the tests against locally running application provide `baseURI` (IRS API base URI, by default http://localhost:8080).
If you want to run it against connector you need to add the following VM options:
`-DbaseURI=<consumer-artifact-uri> -Dusername=<username-to-access-consumer> -Dpassword=<password>`

## System tests

[System tests](integration-tests/src/test/java/net/catenax/irs/systemtest) are running against multiple IRS deployments and reconstructing a parts tree from multiple partial trees.
To run the tests, download the file artifact `dataspace-deployments.json` from the latest IRS Deploy GitHub Actions run into the `dev/local` folder.

## Commit messages
The commit messages have to match a pattern in the form of:  
< type >(optional scope):[<Ticket_ID>] < description >

Example:  
chore(api):[TRI-123] some text

Detailed pattern can be found here: [commit-msg](dev/commit-msg)

### Installation
```shell
cp dev/commit-msg .git/hooks/commit-msg && chmod 500 .git/hooks/commit-msg
```

For further information please see https://github.com/hazcod/semantic-commit-hook

## Licenses
Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
