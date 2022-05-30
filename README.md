# Item Relationship Service

| __Build Status__ | [![build](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/CI-main.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/CI-main.yml)           | 
|:-----------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| __Coverage__     | [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=catenax-ng_product-item-relationship-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=catenax-ng_product-item-relationship-service) |
| __CodeQL__       | [![CodeQL](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/codeql.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/codeql.yml)       |
| __Checkov__      | [![Checkov](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/checkov.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/checkov.yml)    |   
| __Trivy__        | [![Trivy](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/trivy.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/trivy.yml)    |   
| __VeraCode__     | [![VeraCode](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/VeraCode.yml/badge.svg)](https://github.com/catenax-ng/product-item-relationship-service/actions/workflows/VeraCode.yml) | 
| __License__      | [![GitHub](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/catenax-ng/product-item-relationship-service/blob/main/LICENSE)                                                                |   




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

## Keycloak authentication

Access token is required to access every IRS endpoint and should be included in Authorization header for all requests - otherwise 401 Unauthorized status is returned to client. 
To obtain access token prepared [Postman collection can be used](https://github.com/catenax-ng/product-item-relationship-service/blob/main/testing/IRS%20DEMO%20Collection.postman_collection.json)

## Work with sample data

* Retrieve sample BOM:

```bash
curl -X 'POST' \
  'http://localhost:8080/irs/jobs' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <<token_value>>' \
  -d '{
  "aspects": [
    "SerialPartTypization"
  ],
  "bomLifecycle": "asBuilt",
  "depth": 1,
  "direction": "downward",
  "globalAssetId": "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6"
}'
  
curl -X 'GET'  'http://localhost:8080/irs/jobs/<jobID from first call>' -H 'accept: application/json' -H 'Authorization: Bearer <<token_value>>'
```

## Swagger UI

### IRS API

- Swagger UI: http://localhost:8080/api/swagger-ui
- API docs: http://localhost:8080/api/api-docs
- API docs in yaml:  http://localhost:8080/api/api-docs.yaml

## DEV environment

The latest version on main is automatically picked up by ArgoCD and deployed to the DEV environment.
See https://catenax-ng.github.io/.

http://irs.dev.demo.catena-x.net/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config

Additionally, we supply our own EDC setup to be able to do end-to-end tests in an isolated environment.
This contains of:
 - API Proxy
 - AAS Wrapper
 - DigitalTwin Registry
 - EDC Consumer (Control and Data Plane)
 - EDC Provider (Control and Data Plane)
 - Multiple submodel servers to provide test data

The setup is based on [Catena-X@Home](https://github.com/catenax-ng/catenax-at-home/) and uses those docker images.

Check the Helm charts at ./chart for the configuration.

The testdata on DEV is volatile and gets lost on pod restarts. New testdata can be provisioned using the GitHub action trigger.

## INT environment

The latest version on main is automatically picked up by ArgoCD and deployed to the INT environment.
See https://catenax-ng.github.io/.

http://irs.int.demo.catena-x.net/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config

Additionally, we supply our own EDC consumer to connect to the Catena-X integration system.
This contains of:
- API Proxy
- AAS Wrapper
- EDC Consumer (Control and Data Plane)

This setup uses the docker images provided by [the EDC team](https://github.com/catenax-ng/product-edc/).

Check the Helm charts at ./chart for the configuration. 

## Smoke tests

TBD

## System tests
TBD

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

### Coverage

Sonarcloud Coverage (https://sonarcloud.io/component_measures?id=catenax-ng_product-item-relationship-service&metric=coverage&view=list)

## Licenses
Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
