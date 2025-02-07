# Changelog
All notable changes to the CFX project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

_**For better traceability add the corresponding JIRA issue number in each changelog entry, please.**_

## [Unreleased - DD.MM.YYYY]

### Changed

- TRACEX-224 Setup IRS gatling test workflow using containers
- TRX-226 Enabled highcharts in load tests. Updated order test
- TRX-143 API Change POST /irs/order providing aas identifier as key
- TRX-464 remove fallback for resolving shell via globalAssetId
- TRX-154 API Change POST /irs/jobs providing aas identifier as key

### Added

- TRACEX-417 added jobId to transferProcess to allow quick query in database
- TRACEX-205 Added missing "auditContractNegotiation" flag to Order API

### Removed

- TRACEX-417 removed write lock when creating jobs
- TRACEX-417 removed read lock since s3 already works on atomic level

### Fixed

- TRACEX-417 fixed inefficient blob retrieval for irs orders and batches 
- TRACEX-417 fixed ExecutorCompletionServiceFactory to actually limit threads of batches
- TRACEX-417 fixed Batch and Order state calculation to display the correct state during batch processing

## [5.4.1-cfx-6 - 09.01.2025]

### Added
- TRX-543 Filtering for duplicated connector endpoint addresses for same bpn. 
- Added new section under crosscuting/api-endpoints to arc42 documentation
- Added support for EDC EDR management API. This can be enabled using the config entry
  `irs-edc-client.controlplane.edr-management-enabled`.

### Changed

## [5.4.1-cfx-5 - 05.12.2024]

### Added
- Added load tests for /jobs and /orders API (TRX-93)
- Added load test data generation via JUnit tests (TRX-444)
- Configurable number of threads for parallel processing of batch order jobs (#70)
- Added Github workflow for enabling Sonar scanning (#377)
- Added flag `--dos` to transform-and-upload-py script to allow to switch between DOS and umbrella upload (TRX-13)
- Added asset get and update API endpoint (TRX-462)
- add missing permissions to workflows
- Added dedicated cofinity trivy workflow (TRX-441)

### Changed
- Cccept dates without time and time zone according to standard (TRX-511)
- Update Spring-Boot version to 3.2.8 to mitigate CVE-2024-38821 (TRX-433)

### Fixed

- Fixed GH runner based test execution (TRX-13)

## [5.4.1-cfx-4 - 22.11.2024]

### Added
- Added load tests for /jobs and /orders API (TRX-93)


## [5.4.1-cfx-3 - 06.11.2024]

### Changed

- Added the discovery type configurable, with a default value of bpnl in (ConnectorEndpointsService) (#12)
- secured endpoints /ess/notification/receive-recursive and /ess/notification/receive with api key
- Prevented NullPointerException in ConstraintCheckerService by adding emptyIfNull for safe handling of null constraint lists (#24)
- Release documentation for irs-registry-client (#346)
- #351 Introduced orchestration to EDC negotiation to be able to limit parallel edc calls and reuse already ongoing negotiations.

### Fixed

- Fixed URI composition of href URL and configurable submodel suffix to append the path at the correct position
- #375 fixed auto dispatch workflow for auto deployment from main

### Added

- Added api key authentication for edc notification requests
- Added Github workflow for publishing build artifacts to Github Packages (#346)
- Added Maven profile for publishing into Github Packages (#346)
- Added integration tests for /ir/sorders API (#64)
- Added DigitalTwinType in transform-and-upload-py script(#327)
- Added get_auth_token function in transform-and-upload-py script for submodel and dtr creation(#327)

### Removed
- Removed subjectId from AssetAdministrationShellDescriptor object

[Unreleased]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.4.1...HEAD
[5.4.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.4.0...5.4.1