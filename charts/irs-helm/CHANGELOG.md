# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [6.6.1] - 2023-09-22
### Changed
- Update IRS version to 3.4.1
- 
## [6.6.0] - 2023-09-01
### Changed
- Update IRS version to 3.4.0

## [6.5.0] - 2023-08-30
### Added
- Added configurable values for `digitalTwinRegistry.shellDescriptorTemplate` and `digitalTwinRegistry.lookupShellsTemplate`

### Changed
- Update IRS version to 3.3.5

## [6.4.2] - 2023-08-11
### Added
- Added entry to .helmignore to only accept values.yaml

### Changed
- Update IRS version to 3.3.4
 
## [6.4.1] - 2023-08-11
### Changed
- Update IRS version to 3.3.3

## [6.4.0] - 2023-07-31
### Added
- New option to configure accepted operands for policy checks via `edc.catalog.policies.acceptedRightOperands` and `edc.catalog.policies.acceptedLeftOperands`

### Changed
- Update IRS version to 3.3.2

## [6.3.1] - 2023-07-24
### Changed
- Update IRS version to 3.3.1

## [6.3.0] - 2023-07-20
### Changed
- Update IRS version to 3.3.0

## [6.2.1] - 2023-07-19
### Changed
- Update IRS version to 3.2.1
- Removed path segment `/registry` from default template for `digitalTwinRegistry.descriptorEndpoint` 

## [6.2.0] - 2023-07-14
### Changed
- Update IRS version to 3.2.0

## [6.1.0] - 2023-07-07
### Changed
- Remove EDC Catalog cache configuration

### Added
- New configuration for ess asset creation EDC management API path `ess.managementPath`

## [6.0.1] - 2023-06-26
### Changed
- Update IRS to version 3.0.1
- Disable EDC Catalog caching by default. You can enable it again by setting `edc.catalog.cache.enabled` to true

## [6.0.0] - 2023-06-26
### Added
- Add parameter for `digitalTwinRegistry.type` and `digitalTwinRegistry.discoveryFinderUrl`
- Added new configmap values for policy store blobstore connection
- Callback url parameter so set the EDC EDR callback for IRS `edc.callbackurl`
- EDC endpoints for catalog, contractnegotiation, transferprocess and state are now configurable via edc.controlplane.endpoint 

### Changed
- Minio is deployed by default with non-root rights
- Update to IRS 3.0.0

## [5.3.1] - 2023-05-15
### Fixed
- `bpdm.bpnEndpoint` will not be set to an unresolvable URL if `bpdm.url` is not set.

## [5.3.0] - 2023-05-05
### Added
- Added new configuration option for supported policies: `edc.catalog.policies.allowedNames` (comma-separated string)

### Changed
- Updated IRS to version 2.6.0

## [5.2.0] - 2023-04-28
### Added
- New config entries for portal user, used to call discovery service
  - `portal.oauth2.clientId`
  - `portal.oauth2.clientSecret`

### Changed
- Updated IRS version to 2.5.1


## [5.1.1] - 2023-04-17
### Fixed
- Added missing config keys in the spring config map template

## [5.1.0] - 2023-04-17
### Added
- New config entry "bpn", add the BPN for the IRS instance there  **(mandatory)**
- Added config entries for ESS use case:
  - `edc.provider.host` (mandatory for ESS)
  - `discovery.endpoint`
  - `discovery.mockEdcAddress`
  - `ess.mockEdcResult`
  - `ess.mockRecursiveEdcAsset`

### Changed
- Updated IRS version to 2.5.0

### Fixed
- Custom environment variables are now rendered correctly in the deployment resource

## [5.0.10] - 2023-04-21
### Changed
- Update IRS to version 2.4.1

## [5.0.9] - 2023-03-30
### Fixed
- Moved license headers out of if clauses and add dashes (`---`) after each license header

## [5.0.8] - 2023-03-30
### Fixed
- Moved license headers into if clauses to avoid empty resource files which lead to installation errors

## [5.0.7] - 2023-03-30
### Added
- Add minio resource limits 
- Extended configmap and values.yaml with catalog cache configuration  
  You can configure the EDC catalog caching configuration like this:
  ```
  edc:
    catalog:
      cache:
        enabled: true
        ttl: P1D
        maxCachedItems: 64000
  ```
### Changed
- Updated IRS Version to 2.4.0

## [5.0.6] - 2023-03-28
### Added
- Added config parameter for SemanticsHub request page size when retrieving all models. Can be used to fine tune requests. Default: 100 items per page

### Changed
- Updated default path in template for `edc.controlplane.endpoint.data` to match EDC 0.3.0 management endpoint `/api/v1/management`

## [5.0.5] - 2023-03-20
### Changed
- Update IRS to version 2.3.2

## [5.0.4] - 2023-03-07
### Changed
- Update IRS to version 2.3.1


## [5.0.3] - 2023-03-01
### Fixed
- Fixed helm template for bpnEndpoint


## [5.0.2] - 2023-02-27
### Changed
- Updated default values so that IRS can start out of the box without technical errors. Please note that custom configuration is still necessary for IRS to work properly.


## [5.0.1] - 2023-02-21
### Changed
- Fixed semantic hub placeholder in default values


## [5.0.0] - 2023-02-21
### Changed
- Changed config parameter ``semanticsHub`` to ``semanticshub``
- Moved path ``/models/`` from ``semanticshub.modelJsonSchemaEndpoint`` to ``semanticshub.url``


### Migration note
Please make sure that you update your URL config for the semantics hub (see "Changed" section). Otherwise, IRS can not pick up the config correctly. Your new URL needs to contain the /model path.


## [4.3.2] - 2023-03-15
### Changed
- Update IRS version to 2.2.1

## [4.3.1] - 2023-03-01
### Changed
- Updated default values so that IRS can start out of the box without technical errors. Please note that custom configuration is still necessary for IRS to work properly.
- Fixed semantic hub placeholder in default values

## [4.3.0] - 2023-02-07
### Added
- Add support for custom environment variables in Helm chart.

## [4.2.1] - 2023-01-26
### Added
- Added parameter ``edc.controlplane.catalog.pagesize`` for configuration of EDC catalog page size for pagination. Default value is 50.

### Changed
- Minio now uses 1Gi of storage by default.
  > When upgrading from a previous version make sure that the minio PVC and pod is created and accessible by the IRS pod. The previous storage default was 500Gi and Kubernetes can not reduce the PVC size automatically.

## [4.2.0] - 2023-01-20
### Added
- It is now possible to provide semantic schema files as Base64 strings which will be mounted to the IRS container and then loaded via the configuration. 

### Changed
- Update IRS version to 2.2.0

## [4.1.0] - 2023-01-11
### Changed
- IRS configuration is now provided via ConfigMap instead of ENVs. This can be overwritten completely in the values.yaml. This is backward compatible with the previous configuration layout.

## [4.0.0] - 2022-12-09
### Changed
- Update IRS version to 2.0.0

## [3.0.1] - 2022-11-27
### Changed
- Updated default config for Prometheus / Grafana by disabling automatic RBAC creation

## [3.0.0] - 2022-11-25

### Changed
- Replaced the custom charts for Grafana, Prometheus and Minio with dependencies on stock charts. Please see the updated documentation for the new configuration layout.

### Removed
- Removed EDC from deployment. Instead, a new Helm chart is available which contains the EDC consumer: "irs-edc-consumer"
- Removed API wrapper from deployment

## [2.3.0]
### Changed
- Update IRS version to 1.5.0

## [2.2.0]
### Changed
- Update IRS version to 1.4.0

## [2.1.0]
### Added
- BPDM URL is now configurable
- SemanticsHub URL and default URNs are now configurable


## [2.0.0] - 2022-10-10
### Changed
- Refactored chart structure to no longer include environment values
- Simplified configuration in values.yaml

