# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Changed config parameter ``semanticsHub`` to ``semanticshub``
- Moved path ``/models/`` from ``semanticshub.modelJsonSchemaEndpoint`` to ``semanticshub.url``

## [4.3.0] - 2023-02-07
### Added
- Add support for custom environment variables in Helm chart.

## [4.2.1] - 2023-01-26
### Added
- Added parameter ``edc.controlplane.catalog.pagesize`` for configuration of EDC catalog page size for pagination. Default value is 50.

### Changed
- Minio now uses 1Gi of storage by default.

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

