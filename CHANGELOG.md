# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- IRS API now requires 'view_irs' resource access inside Keycloak JWT token.
- New 2.0.0 version of IRS API. Main goal was to remove 'job' prefix from attribute names
    - change 'jobId' to 'id' in GET calls
    - change 'jobState' to 'state' in GET calls
    - change 'jobCompleted' to 'completedOn' in GET calls
    - change 'jobId' to 'id' and 'jobState' to 'state' in callback URI variables

### Known knowns
- PLACEHOLDER REMOVE IF EMPTY: risks that were introduced or discovered in the release and are known but not resolved

## [1.6.0] - 2022-11-25
### Added
- EDC client implementation (for negotiation and data exchange)
- New callback endpoint for EDC (path: /internal/endpoint-data-reference)
- Optional trusted port to make internal interfaces only available via that (config: server.trustedPort)

### Removed
- Removed the need for the API wrapper by directly communicating with the EDC control and data plane.

## [1.5.0] - 2022-11-11
### Added
- Added new parameters 'startedOn' and 'jobCompleted' to Job status response 

### Changed
- Updated Spring Boot to 2.7.5 and Spring Security (Web and OAuth2 Client) dependencies to 5.7.5 due to CVEs
- Renamed parameter from 'status' to 'jobState' in Job status response
- Time to live for finished jobs is now configurable

## [1.4.0] - 2022-10-28
### Added
- Added new 'asPlanned' value for bomLifecycle request parameter - now BomAsPlanned can be traversed by the IRS to build relationships

## [1.3.0] - 2022-10-18
### Added
- BPDM URL (*env:BPDM_URL*) is now configurable
- SemanticsHub URL (*env:SEMANTICSHUB_URL*) and default URNs (*env:SEMANTICSHUB_DEFAULT_URNS*) are now configurable
- Added an administration guide covering installation and configuration topics (TRI-593) 
- **Tombstones** Tombstone contains ProcessStep in ProcessingError
- Added new optional parameter 'callbackUrl' to Job registration request

### Known knowns
- discovered lack of circuit breaker for communication with submodel server which is not responding (low risk) - will be addressed in future release

## [1.2.0] - 2022-09-30
### Added
- Automatic eclipse dash IP-ticket creation
- Automatic cucumber execution based on Tests in Jira

### Fixed
- Update HSTS header configuration (TRI-659)
- Encode log output to avoid log forging (TRI-653)
- Add missing X-Frame-Options header (TRI-661)
- Switching to a distroless Docker base image to avoid vulnerable library (TRI-729)

### Changed
- Update EDC components to version 0.1.1
- Update testdata set to 1.3.2
- Create Tombstone for faulty/null/none BPN ManufactureId
- Update aaswrapper to 0.0.7

## [1.1.0] - 2022-09-12
### Added
- **Aspect Model validation** IRS now validates the aspect model responses requested via EDC. JSON schema files are requested on demand using Semantic Hub API.
- **BPN mapping** IRS job result includes BPNs and the corresponding names.
- **Enabled collecting of "Batch" submodels** IRS supports aspect model "Batch"

### Fixed
- **Malformed date-time in IRS job result** (TRI-627)
- **Job cleanup process** Jobs are completely deleted after retention period exceeded. (TRI-631)
- **IRS job processing** IRS jobs no longer stay stuck in state RUNNING due to malformed URLs. (TRI-675)
- **Security fixes** Fixed various security findings.

### Changed
- **IRS monitoring** Added more metrics and improved Grafana dashboards.
- **Submodel payload in IRS job response** Submodels are stored as object instead of string.
- **CORS** Enabled CORS configuration
- **Documentation** Improved README and UML diagrams.
- **GitHub integrations** Trivy/KICS/Eclipse DASH tool/VeraCode
- **Extended Postman collection**
- **IRS stability and code quality**
- **API docs**
- **Test data and upload script**
- **Helm charts** Improved security and performance configurations. Created a All-in-One Helm Chart for IRS which includes all IRS dependencies. Helm Chart is released separately.
- **Refactor relationship result object of IRS**
- **FOSS initial GitHub & code preparation** Change package structure to `org.eclipse.tractusx`.

## [1.0.0] - 2022-07-25
### Changed
* **Improved Minio Helmchart** Latest Minio version is used now
* **Submodel Information** If requested, the IRS collects submodel information now and adds it to the job result
* **Improved job response** The job response object contains all the required fields now with correct values

## [0.9.1] - 2022-06-14
### Removed
- **Remove AAS Proxy** The IRS works without the AASProxy component

## [0.9.0] - 2022-04-27
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

[Unreleased]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.6.0...HEAD
[1.6.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.5.0...1.6.0
[1.5.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.4.0...1.5.0
[1.4.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.3.0...1.4.0
[1.3.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.2.0...1.3.0
[1.2.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/v1.1.0...1.2.0
[1.1.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/v0.9.1...v1.0.0
[0.9.1]: https://github.com/eclipse-tractusx/item-relationship-service/commits/v0.9.1
[0.9.0]: https://github.com/eclipse-tractusx/item-relationship-service/commits/v0.9.0
