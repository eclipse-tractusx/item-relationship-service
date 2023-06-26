# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Update to EDC 0.4.1
- Switch to EDC helm chart dependencies
- Moved vault token and api key to secret for control- and data-plane.
- Moved vault token configuration from `configuration.properties` to `edc.vault.hashicorp.token`

## [1.1.2] - 2023-03-30
### Added
- Add edc-postgres resource limits

## [1.1.1] - 2023-03-28
### Changed
- Renamed `edc.receiver.http.endpoint` to `edc.receiver.http.dynamic.endpoint`
- Renamed `edc.ids.endpoint.audience` to `edc.oauth.endpoint.audience`
- Renamed `ids` endpoint to `protocol`

## [1.1.0] - 2023-03-24
### Changed
- Update EDC to 0.3.0
- Renamed controlplane property `edc.oauth.public.key.alias` to `edc.oauth.certificate.alias`
- Changed data-endpoint `/data` to management-endpoint `/api/v1/management`
- Removed management endpoint from ingress

## [1.0.4] - 2023-02-28
### Changed
- Downgrade EDC to 0.1.6
- Renamed controlplane property `edc.oauth.endpoint.audience` to `edc.ids.endpoint.audience`

## [1.0.3] - 2023-02-21
### Changed
- Updated default memory requests

## [1.0.2] - 2022-12-19
### Changed
- Updated EDC to 0.2.0
- Renamed controlplane property `edc.ids.endpoint.audience` to `edc.oauth.endpoint.audience`

## [1.0.1] - 2022-11-27
### Fixed
- Fixed the default callback URL config, which resulted in a Helm template error


## [1.0.0] - 2022-11-25
### Added
- Introducing this Helm chart to provide the EDC consumer.

