# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [v1.1.0] - 2022-09-12
### Added
- **Added Aspect Model validation** IRS now validates the aspect model responses requested through EDC. JSON schema files are requested on demand using Semantic Hub API.
- **Added BPN mapping** IRS job result now includes BPNs and the corresponding names.
- **Enabling collecting "Batch" submodels** IRS now supports aspect model "Batch"

### Fixed
- **Fixed malformed date-time in IRS job result**
- **Fixed job cleanup process**
- **IRS job processing** IRS jobs no longer stay stuck in state RUNNING due to malformed URLs.
- **Security fixes** Fixed various security findings.

### Changed
- **Improved IRS monitoring** Added more metrics and improved Grafana dashboards.
- **Improved submodel payload in IRS job response**
- **CORS** Enable CORS configuration
- **Documentation** Improved README and UML diagrams.
- **GitHub Integrations** Trivy /KICS/Eclipse DASH tool/VeraCode
- **Improved and Extended Postman Collection**
- **Improvements to IRS stability and code quality**
- **Improved API docs**
- **Improve test data and upload script**
- **Helm Charts** Improved security and performance configurations. Created a All-in-One Helm Chart for IRS which includes all IRS dependencies. Helm Chart is released separately.
- **Refactor Relationship Result Object of IRS**
- **FOSS Initial GitHub & Code Preparation** Change package structure to org.eclipse.tractusx.

## [v1.0.0] - 2022-07-25
### Changed
* **Improved Minio Helmchart** Latest Minio version is used now
* **Submodel Information** If requested, the IRS collects submodel information now and adds it to the job result
* **Improved job response** The job response object contains all the required fields now with correct values

## [v0.9.1] - 2022-06-14
### Removed
- **Remove AAS Proxy** The IRS works without the AASProxy component

## [v0.9.0] - 2022-04-27
### Added
- **Build traceability BoM as built tree** You can now use the IRS to retrieve a BoM tree with lifecycle stage "as built" for serialized components, which are distributed across the Catena-X network. In this release, the tree is being built on the aspects "SerialPartTypization" and "AssemblyPartRelationship". Focus is a tree built  in the direction top-down/parent-child.
- *IRS API v1.0.0* First release of the IRS API.

### Fixed
- **Cloud Agnostic Solution** You have the ability now to deploy the solution on different cloud vendor solutions. We decoupled the application from its former Azure Stack.
- **Security fixes** Various security fixes.

### Changed
- **Asynchronous Job Management** Since we cannot rely on a synchronous answer of each request  within the network, we provide a job management for this process.
- **AAS Proxy**  Requests to Digital Twin Registry are executed via the AAS Proxy.
- **Quality Gate for Release 1** The quality measures were implemented in accordance with the requirements for Release 1.
- **Hotel Budapest catenax-ng** C-X-NG ready using the provided catenax-ng infrastructure.
- **SCA Composition Analysis** Enablement of SCA Composition Analysis using Veracode and CodeQl.
- **Github Integrations** VeraCode/Dependabot/SonarCloud/CodeQl

### Unresolved
- **Select Aspects you need**  You are able to select the needed aspects for which you want to collect the correct endpoint information.

[Unreleased]: https://github.com/catenax-ng/product-item-relationship-service/compare/v1.1.0...HEAD
[v1.1.0]: https://github.com/catenax-ng/product-item-relationship-service/compare/v1.0.0...v1.1.0
[v1.0.0]: https://github.com/catenax-ng/product-item-relationship-service/compare/v0.9.1...v1.0.0
[v0.9.1]: https://github.com/catenax-ng/product-item-relationship-service/commits/v0.9.1
[v0.9.0]: https://github.com/catenax-ng/product-item-relationship-service/commits/v0.9.0