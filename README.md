# ![Item Relationship Service (IRS)](https://github.com/eclipse-tractusx/item-relationship-service/blob/main/logo.png)


[![Apache 2 License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/eclipse-tractusx/item-relationship-service/blob/main/LICENSE)  
[![Build](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/irs-build.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/irs-build.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=eclipse-tractusx_item-relationship-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=eclipse-tractusx_item-relationship-service)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eclipse-tractusx_item-relationship-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eclipse-tractusx_item-relationship-service)
[![CodeQL](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/codeql.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/codeql.yml)  
[![Kics](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/kics.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/kics.yml)
[![Trivy](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/trivy.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/trivy.yml)
[![Trivy Docker Hub Scan](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/trivy-docker-hub-scan.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/trivy-docker-hub-scan.yml)
[![VeraCode](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/veracode.yaml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/veracode.yaml)
[![OWASP Dependency Check](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/owasp.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/owasp.yml)
[![Spotbugs](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/spotbugs.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/spotbugs.yml)
[![Eclipse-dash](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/eclipse-dash.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/eclipse-dash.yml)
[![Tavern IRS API test](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/tavern.yml/badge.svg)](https://github.com/eclipse-tractusx/item-relationship-service/actions/workflows/tavern.yml)

## Vision and Mission Statement

"The IRS is providing a technical API Endpoint in the Catena-X Network, which builds an item tree representation of given digital twins stored across the industry. Therefore, it is a key component for the Network to provide data chains along the value chain in the industry."

``The vision for the Item Relationship Service is to provide an easy access endpoint for complex distributed digital twins across Catena-X members.
It abstracts the access from separated digital twins towards a connected data chain of twins and provides those.
It enables to apply business logic on complex distributed digital twins across company borders.``

## What is the IRS?

Within the [Catena-X network](https://catena-x.net/), the so-called Item Relationship Service (IRS) forms an essential
foundation for various services and products. Within the Catena-X use cases, the IRS serves to increase business value.
For example, the IRS provides functionalities to serve requirements, such as occasion-based Traceability,
from the Supply Chain Act. In doing so, IDSA and Gaia-X principles, such as data interoperability and sovereignty, are
maintained on the Catena-X network and access to dispersed data is enabled. Data chains are established as a common
asset.

With the help of the IRS, data chains are to be provided ad-hoc across n-tiers within the Catena-X network.
To realize these data chains, the IRS relies on data models of the Traceability use case and provides the federated
data chains to customers or applications. Furthermore, the target picture of the IRS includes the enablement of new
business areas by means of data chains along the value chain in the automotive industry.

## Usage

### Local deployment

The following subsection provides instructions for running the infrastructure on docker-compose and the application in
the IDE.

#### Docker-compose + IDE

- Start the necessary infrastructure by running `docker-compose up`

- Start the application from your favorite IDE. For IntelliJ, a run configuration is available in the .run folder.

#### Local IRS API

- Swagger UI: http://localhost:8080/api/swagger-ui
- API docs: http://localhost:8080/api/api-docs
- API docs in yaml:  http://localhost:8080/api/api-docs.yaml

### Helm deployment

see [INSTALL.md](INSTALL.md)

### Sample calls

#### IRS

Start a job for a globalAssetId:

```bash
curl -X 'POST' \
  'http://localhost:8080/irs/jobs' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token_value>' \
  -d '{
  "aspects": [
    "SerialPart"
  ],
  "bomLifecycle": "asBuilt",
  "depth": 1,
  "direction": "downward",
  "globalAssetId": "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6"
}'
```

Retrieve the job results by using the jobId returned by the previous call:

```bash
curl -X 'GET' 'http://localhost:8080/irs/jobs/<jobID>' -H 'accept: application/json' -H 'Authorization: Bearer <token_value>'
```

#### Environmental and Social Standards (ESS)

Start an ESS investigation for a globalAssetId and Incident BPNS.

```bash
curl -X 'POST' \
  'http://localhost:8080/ess/bpn/investigations' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token_value>' \
  -d '{
	"key": {
		"globalAssetId": "urn:uuid:3d61ada2-1a50-42a0-b411-40a932dd56cc",
		"bpn": "BPNL00ARBITRARY6"
	},
	"incidentBPNSs": [
		"BPNS00ARBITRARY7"
	],
	"bomLifecycle": "asPlanned"
    }'
```

Retrieve the investigation results by using the jobId returned by the previous call:

```bash
curl -X 'GET' 'http://localhost:8080/ess/bpn/investigations/<jobID>' -H 'accept: application/json' -H 'Authorization: Bearer <token_value>'
```

## Documentation

- [Item Relationship Service Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/)

## Licenses

For used licenses, please see
the [NOTICE](https://github.com/eclipse-tractusx/item-relationship-service/blob/main/NOTICE.md).

## Notice for Docker image

This application provides container images for demonstration purposes.

DockerHub: https://hub.docker.com/r/tractusx/irs-api

Eclipse Tractus-X product(s) installed within the image:

- GitHub: https://github.com/eclipse-tractusx/item-relationship-service
- Project home: https://projects.eclipse.org/projects/automotive.tractusx
- Dockerfile: https://github.com/eclipse-tractusx/item-relationship-service/blob/main/Dockerfile
- Project
  license: [Apache License, Version 2.0](https://github.com/eclipse-tractusx/item-relationship-service/blob/main/LICENSE)

**Used base image**

- [eclipse-temurin:17-jre-alpine](https://github.com/adoptium/containers)
- Official Eclipse Temurin DockerHub page: https://hub.docker.com/_/eclipse-temurin
- Eclipse Temurin Project: https://projects.eclipse.org/projects/adoptium.temurin
- Additional information about the Eclipse Temurin
  images: https://github.com/docker-library/repo-info/tree/master/repos/eclipse-temurin

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc
from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies
with any relevant licenses for all software contained within.

## Contact

Contact the project developers via the project's "dev" list.

* https://accounts.eclipse.org/mailing-list/tractusx-dev
* Eclipse Matrix Chat https://chat.eclipse.org/#/room/#tractusx-irs:matrix.eclipse.org