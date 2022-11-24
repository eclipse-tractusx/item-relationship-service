# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

