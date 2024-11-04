# Changelog
All notable changes to the CFX project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

_**For better traceability add the corresponding JIRA issue number in each changelog entry, please.**_

## [Unreleased]

### Changed

- Added the discovery type configurable, with a default value of bpnl in (ConnectorEndpointsService) (#12)
- secured endpoints /ess/notification/receive-recursive and /ess/notification/receive with api key
- Prevented NullPointerException in ConstraintCheckerService by adding emptyIfNull for safe handling of null constraint lists (#24)
- Release documentation for irs-registry-client (#346)

### Fixed

- Fixed URI composition of href URL and configurable submodel suffix to append the path at the correct position

### Added

- Added api key authentication for edc notification requests
- Added Github workflow for publishing build artifacts to Github Packages (#346)
- Added Maven profile for publishing into Github Packages (#346)
- Added integration tests for /ir/sorders API (#64)

### Removed
- Removed subjectId from AssetAdministrationShellDescriptor object

[Unreleased]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.4.1...HEAD
[5.4.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.4.0...5.4.1