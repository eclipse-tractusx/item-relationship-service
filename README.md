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


## Introduction

### Vision and Mission Statement

The IRS is providing a technical API Endpoint in the Catena-X Network, which builds an item tree representation of given
digital twins stored across the industry. Therefore, it is a key component for the Network to provide data chains along
the value chain in the industry.

The vision for the Item Relationship Service is to provide an easy access endpoint for complex distributed digital twins
across Catena-X members.
It abstracts the access from separated digital twins towards a connected data chain of twins and provides those.
It enables to apply business logic on complex distributed digital twins across company borders.

### What is the IRS?

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

## Source Layout

The IRS project consists of several different parts:

| Folder                  | Description                                                                                                                                                                                             | Further information                                                  |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| .config                 | Configuration, rules and suppression files for code quality check tools etc.                                                                                                                            |                                                                      | 
| .github                 | Github configuration such as workflow definitions.                                                                                                                                                      |                                                                      |
| .idea                   | Configuration for development via [IntelliJ](https://www.jetbrains.com/idea/).                                                                                                                          |                                                                      |
| .run                    | Run configurations for running the IRS in [IntelliJ](https://www.jetbrains.com/idea/).                                                                                                                  |                                                                      |
| charts                  | [HELM](https://helm.sh/) charts.                                                                                                                                                                        |                                                                      |
| charts/irs-helm         | IRS [HELM](https://helm.sh/) chart for running the IRS with and its direct dependencies in [kubernetes](https://kubernetes.io/).                                                                        | [IRS HELM charts documentation](charts/irs-helm/README.md)           |
| docs                    | Concepts and documentation.                                                                                                                                                                             | [Docs README](docs/README.md)                                        |
| irs-api                 | The IRS API.                                                                                                                                                                                            |                                                                      |
| irs-common              | Common classes of the IRS.                                                                                                                                                                              |                                                                      |
| irs-cucumber-tests      | [Cucumber](https://cucumber.io/) tests for the IRS.                                                                                                                                                     | [Cucumber Tests README](irs-cucumber-tests/README.md)                |
| irs-edc-client          | The EDC client module which is responsible for communicating with the EDCs.                                                                                                                             |                                                                      |
| irs-integration-tests   | Integration tests of the IRS.                                                                                                                                                                           |                                                                      |
| irs-load-tests          | Load tests for the IRS.                                                                                                                                                                                 | [IRS Load Tests README](irs-load-tests/README.md)                    |
| irs-models              | Model classes.                                                                                                                                                                                          |                                                                      |
| irs-parent-spring-boot  | [Spring Boot](https://spring.io/projects/spring-boot) parent POM.                                                                                                                                       |                                                                      |  
| irs-policy-store        | The IRS policy store, an API to store and retrieve accepted EDC policies.                                                                                                                               |                                                                      |
| irs-registry-client     | Digital Twin Registry Client.                                                                                                                                                                           | [Digital Twin Registry Client README](irs-registry-client/README.md) |  
| irs-report-aggregate    | This Maven module aggregates the test coverage reports of all modules.                                                                                                                                  |                                                                      |
| irs-testing             | This module contains testing utilities for the IRS like testcontainers, wiremock configurations and requests etc.                                                                                       |                                                                      |
| local                   | This folder contains resources for local development and demonstration.                                                                                                                                 |                                                                      | 
| local/demo              | Showcases the IRS ESS top-down investigation use-case.                                                                                                                                                  | [ess-demo.py README](local/demo/README.md)                           | 
| local/deployment        | [HELM](https://helm.sh/) charts for local deployment of the IRS.                                                                                                                                        |                                                                      |
| local/development       | This folder contains resources relevant for setting up the local development environment such as the [commit message check hook](CONTRIBUTING.md#commit-messages).                                      |                                                                      |                          
| local/testing           | [REST Request Collection](local/testing/IRS_Request_Collection.json) for [Insomnia](https://insomnia.rest/) and further resources concerning testing in the respective sub-directories described below. | [REST Request Collection README](local/testing/README.md)            |
| local/testing/api-tests | [Tavern](https://tavern.readthedocs.io) API tests for the IRS.                                                                                                                                          | [Tavern API Tests README](local/testing/api-tests/README.md)         | 
| local/testing/testdata  | Test data.                                                                                                                                                                                              | [Test data README](local/testing/testdata/README.md)                 |


## Installation

This section describes both deployment on [kubernetes](https://kubernetes.io) via [Helm](https://helm.sh/) and local deployment for development.

### Helm Deployment

See [INSTALL](INSTALL.md)


### Local Deployment

The following subsection provides instructions for running the infrastructure on docker-compose and the application in
the IDE.

After the application is up and running the local IRS API is available at the following URLs:

- Swagger UI: http://localhost:8080/api/swagger-ui
- API docs: http://localhost:8080/api/api-docs
- API docs in yaml: http://localhost:8080/api/api-docs.yaml


#### Docker-compose + IDE

- Start the necessary infrastructure by running `docker-compose up`

- Start the application from your favorite IDE. For IntelliJ, a run configuration is available in the .run folder.


## Usage

See [Usage](USAGE.md)


## Documentation

- [Item Relationship Service Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/)


## Changelog and Compatibility Matrix

- [Changelog IRS](CHANGELOG.md)
- [Changelog IRS Helm](charts/irs-helm/CHANGELOG.md)
- [Compatibility Matrix](COMPATIBILITY_MATRIX.md)


## Known Issues and Limitations

See [Bugs & Security Incidents](https://github.com/orgs/eclipse-tractusx/projects/8/views/10).


## Reporting Bugs and Vulnerabilities

Please distinguish bugs and vulnerabilities when reporting: 

- **Vulnerabilities:** For vulnerabilities see [SECURITY](SECURITY.md#reporting-a-vulnerability).

- **Bugs:** Normal bugs may be reported [as public GitHub issues](https://github.com/orgs/eclipse-tractusx/projects/8/views/10).


## Iteration Notes and Discussions

For each iteration we create a page under the 
[github discussions section](https://github.com/eclipse-tractusx/item-relationship-service/discussions) 
where we document the iteration goal and the sprint roles. 
Example: [PI 12 iteration 3](https://github.com/eclipse-tractusx/item-relationship-service/discussions/432).

If you have any suggestions feel free to join any discussion or create a new one 
in the [github discussions section](https://github.com/eclipse-tractusx/item-relationship-service/discussions).


## Contributing

See [CONTRIBUTING](CONTRIBUTING.md)


## FAQ

See [FAQ](FAQ.md)


## Licenses

For used licenses, please see the [NOTICE](NOTICE.md).


## Notice for Docker Image

Below you can find the information regarding Docker Notice for this application.

- [Item Relationship Service](./DOCKER_NOTICE.md)


## Contact

See [CONTACT](CONTACT.md)
