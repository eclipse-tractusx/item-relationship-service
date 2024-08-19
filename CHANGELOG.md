# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

_**For better traceability add the corresponding GitHub issue number in each changelog entry, please.**_

## [Unreleased]

## [5.4.1] - 2024-08-19

### Fixed

- Fixed already merged implementation from _"Access and Usage Policy Validation flow correction. #757"_ 
  where fallback to default policy was not implemented correctly.
- Improved exception handling concerning invalid date format in search parameters for `GET /irs/policies/paged`. #639
- Change policy to include full namespace `https://w3id.org/catenax/policy/` instead of `cx-policy:` 
  in some remaining code places (in context of #794).
- Fixed flaky test `InMemoryJobStoreTest.checkLastModifiedOnAfterCreation()` (PR#857).
- Fixed occasion where completed Job callbacks are called multiple times. #755
- BPN and endpointURL(s) are set in the tombstone now. #841

### Changed

- The date search operators `AFTER_LOCAL_DATE` and `BEFORE_LOCAL_DATE` for fields `createdOn` and `validUntil` support any ISO date time now (relates to #639 and #750).
- Improved documentation for `GET /irs/policies/paged` endpoint. #639
- Cleanup in IrsApplicationTest.generatedOpenApiMatchesContract 
  (removed obsolete ignoringFields, improved assertion message)
- Allow local URLs as IRS Job complete callback URLs. eclipse-tractusx/traceability-foss#511

## [5.4.0] - 2024-07-22

### Changed
- Default policies are now configured using JSON in accordance with the ODRL schema. #542
- Improved the exception handling for modification attempts on read-only default policies. Such actions now result in a 400 BAD REQUEST response with a user-friendly error message. #734

## [5.3.0] - 2024-07-15

### Added

- Added filtering by "createdOn", "validUntil" to paging endpoint for Policy Store API: `GET /irs/policies/paged`. #750
- Added autocomplete endpoint Policy Store API: `GET /irs/policies/attributes/{attribute}`. #750
- Added get and delete functionality for contract definitions eclipse-tractusx/traceability-foss#1190

### Fixed

- Access and Usage Policy Validation flow correction. #757
- GET /irs/policies and GET /irs/policies/paged return the configured default policies if no custom default policy is defined now. #734
- IRS Policy Validation does not accept subset of AND constraint any longer. #649

### Changed

- Replaced technical error message when trying to delete the configured default policy with a user-friendly message.
- Renamed EdcCreateContractDefinitionRequest to EdcContractDefinition because it's used for both get and post eclipse-tractusx/traceability-foss#1190

### Known knowns

- [#755] Duplicate callbacks for jobs

## [5.2.0] - 2024-07-03

### Fixed

- Fixed ESS Investigation job processing not starting. #579
- Policy store API returns 'rightOperand' without 'odrl:' prefix now (see traceability-foss/issues/970).
- Fixed trivy workflow to fail only on CRITICAL, HIGH (according to https://github.com/eclipse-tractusx/eclipse-tractusx.github.io/pull/949/files).

### Changed

- Improved policy store API input validation. #528
- Extended datamodel of EdcPolicyPermissionConstraint to include andConstraints
- Marked createAccessPolicy requests with deprecation mark.
- Remove edc namespace from EdcContractAgreementNegotiationResponse and
  EdcContractAgreementsResponse. eclipse-tractusx/traceability-foss#963
- Added missing @context values in edc asset creation. eclipse-tractusx/traceability-foss#978
- Switch to `dct:type` `https://w3id.org/catenax/taxonomy#` for notification asset creation. eclipse-tractusx/traceability-foss#978
- Shells in Job response will contain all submodel descriptors returned by provider, instead filtered by aspect-type parameter. #510
- Updated contributing, notice, and readme files for TRG 7 #681
- Handling of expired policies when approving a notification eclipse-tractusx/traceability-foss#639
- Improve validation for URLs returned by discovery service and dspEndpoint to allow custom top-level-domains. #226

## Added

- Added tests for aspect version compatibility. #529
- Added endpoint for dedicated removal of policy from BPNL. #559
- Integration Test Policy Store API Unhappy Path. #519
- Support for SingleLevelUsageAsPlanned. #470
- Documentation to describe the delegate process. #470
- Added file for CC BY 4.0 license for TRG 7 #681
- Paging endpoint for Policy Store API: `GET /irs/policies/paged`. #639
    - Supports multi-sort for the properties "bpn", "validUntil", "policyId", "createdOn", "action" with ascending / descending order.
    - Supports AND-connected multi-filtering by the properties "bpn", "validUntil", "policyId".
        - Note: filtering by "createdOn", "validUntil" will be implemented in a subsequent story.

## [5.1.4] - 2024-05-27

### Fixed

- Fixed submodel request path by introducing configuration property `irs-edc-client.submodel.submodel-suffix` which will
  be appended to the href URL.

## [5.1.3] - 2024-05-17

### Fixed

- IRS now searches for Digital Twin Registry contract offers by
  type `dct:type`: `https://w3id.org/catenax/taxonomy#DigitalTwinRegistry`
  or `edc:type`: `data.core.digitalTwinRegistry`. #616
- Fix missing and malformed properties for EDC policy transformation. #648

## Fixed

- Propagates exceptions to have more detail in tombstone. #538


## [5.1.2] - 2024-05-13

### Fixed

-  Cleaning up BPNLs without policies. #533

### Changed

- Updated default accepted policy to latest traceability framework agreement #596
- BPN summary was removed from Job response #568


## [5.1.1] - 2024-05-08

### Fixed

- Fixed issue in EDR token renewal. #358

### Added

- Cucumber test step definitions for Policy Store API (Happy Path) including some test helper utilities. #518

## [5.1.0] - 2024-05-06

### Changed

- Removed obsolete entries from acceptedPolicies configuration. #530
- Support of building relationships based on SingleLevelUsageAsBuilt v3.0.0 #558
- Support of building relationships based on SingleLevelBomAsPlanned v3.0.0 #558
- BPN lookup feature was removed #568
- Update IRS EDC client to use EDC 0.7.0 #358

### Fixed

- Update bouncycastle to 1.78 to fix CVE's.
- Fixed validation of json-schemas - IRS is creating tombstone instead collecting Submodel payload, when it not passes validation of schema #522

## [5.0.0] - 2024-04-16

### Added
- SAMM models can now be added locally #488
- Introduced new Cucumber Tests to cover Industry Core 2.0.0 compatibility #488



### Fixed

- Policy store API fixes. #199, #505
  - Create policy request limited to create exactly one policy, not multiple (reason: error handling).
  - Create policy request returns policy id of the created policy now.
  - Harmonized policy store API between #199 and policy structure from #249.
  - Consistent naming for business partner number parameters.
  - Corrected default policy handling.
  - Validation of business partner numbers. #505

- ClassCastException in exception handling of EdcSubmodelClientImp#getEndpointReferencesForAsset corrected (returns the
  exception as failed future now). #405
- RestClientExceptions are handled correctly in BpdmFacade now. #405
- Fixed Base64 encoding and decoding for locally provided Semantic Models #488


## [4.9.0] - 2024-04-03
### Added
- Extended EdcPolicyDefinitionService to check if a policy in the edc exists

### Changed
- IRS now supports Asset Administration Shell v3.1 - adjusted lookup shells endpoint changes (assetIds query param is encoded). #359
- Support of building relationships based on SingleLevelBomAsBuilt v3.0.0 #488
- Renamed item relationship service Helm chart from "irs-helm" to "item-relationship-service". #489


## [4.8.0] - 2024-03-18
### Changed

- Improved maintainability in EdcSubmodelClientImpl by reduced method visibility and better naming (in context of #448).
- EdcPolicyDefinitionService, EdcContractDefinitionService and EdcAssetService throw AlreadyExist exceptions when
  conflict is returned from EDC
- Added AssetAdministrationShellDescriptor specificAssetIds support for externalSubjectId required for data provisioning
- Registering a job - aspects array is now accepting full urn of aspect model instead of name only, eg. 'urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt' instead 'SingleLevelBomAsBuilt'. #439
- Changed the version of irs-registry-client from 1.6.0-SNAPSHOT to 1.6.0
- Policies can now be registered for certain bpnls. #199


## Fixed
- Fixed missing timeouts including configuration. #448

## [4.7.0] - 2024-03-04
### Added
- DigitalTwinRegistryCreateShellService in irs-registry-client for creating shells in DTR directly
- POST /management/v2/contractagreements/request and GET /management/v2/contractagreements/{contractAgreementId}/negotiation to irs-edc-client lib

### Changed
- EDC client handles multiple Digital Twin Registries and Digital Twins now #395
- Change logo of irs
- Added 'businessPartnerNumber' field to Tombstone model. This will be filled only when UsagePolicyValidation tombstone is being created. #404

### Fixed
- Update to Spring Boot 3.1.9 to fix CVE's. #423
- Fixed a case where policy validation could result in a NullPointerException
  if either orConstraint or andConstraint of accepted policies were null. #436

## [4.6.0] - 2024-02-20
### Added

- Added concept to conform with IndustryCore Changes CX-0126 and CX-127. #367
- Added release instructions to CONTRIBUTING.md
- EdcAssetService capable to create assets inside EDC
- EdcPolicyDefinitionService capable to create polices inside EDC
- EdcContractDefinitionService capable to create contracts inside EDC

### Changed
- Updated AUTHORS.md
- Reduced log level in MeterRegistryService in order to avoid excessive logging

### Fixed

- Fixed Log4J configuration. #396
- Fix tavern and cucumber tests

## [4.5.2] - 2024-02-22
## Changed
- Updated IRS OpenAPI version to 4.5.2

## [4.5.1] - 2024-02-20
### Changed

- Moved Docker notice to separate file. #425

## [4.5.0] - 2024-02-07
### Added
- Added helper script for building documentation locally.
- Added new job parameter flag "auditContractNegotiation" which toggles setting contractAgreementId in Shells and Submodels
- Added "contractAgreementId" field to Submodel model
- Added Integration Tests for the entire IRS flow using stubbed responses of Discovery Service, Semantic Hub, EDC,
  Digital Twin Registry and BPDM Pool. #344

### Changed

- Dataspace Discovery Service handles multiple EDC-Urls received for BPN now. #214
- Updated license header to "Copyright (c) 2021,2024 Contributors to the Eclipse Foundation" #349
- Changed lookupGlobalAssetIds to lookupShellsByBPN, which provides full object.
- Suppressed CVE-2024-20932 from graal-sdk-21.2.0.jar because this is not applicable for IRS.
- Updated configuration of `DISCOVERY_REST_TEMPLATE` from `ess.discovery.*` to `digitalTwinRegistry.discovery.*` and discovery finder URL from `digitalTwinRegistry.discoveryFinderUrl` to `digitalTwinRegistry.discovery.discoveryFinderUrl`
- Redesigned shell object - wrapped payload and added "contractAgreementId" field. #370
- Changed structure of policy creation to match EDC format. #249
- Update irs-registry-client to 1.6.0-SNAPSHOT

### Fixed
- Update to Spring Boot 3.1.8. This fixes the following CVEs:
  - CVE-2023-6378 serialization vulnerability in logback
  - CVE-2023-51074 json-path v2.8.0 stack overflow
  - CVE-2024-22233 Spring Framework server Web DoS Vulnerability

## [4.4.0] - 2024-01-15
### Added

- Added EDR token cache to reuse token after contract negotiation. #256
- Added cache mechanism in DiscoveryFinderClientImpl for findDiscoveryEndpoints. #225
- Add concept docs/#322-Provisioning-of-contractAgreementId-for-assets.md. #322

### Changed

- Authentication was redesigned to use API keys, instead of OAuth2 protocol. The api key has to be sent as an X-API-KEY
  request header. IRS is supporting two types of API keys - one for admin and one for regular/view usage. Use
  new ``apiKeyAdmin`` and ``apiKeyRegular`` config entries to set up API keys. #259

### Removed

- Removed ``oauth.resourceClaim``, ``oauth.irsNamespace``,``oauth.roles``,``oauth2.jwkSetUri`` config entries. #259

## [4.3.0] - 2023-12-08
### Added

- Added support for `hasAlternatives` property in SingleLevelBomAsBuilt aspect. #296

### Changed

- Updated EDC dependencies to 0.2.1
- Update deprecated field `providerUrl` to `counterPartyAddress` in EDC catalog request
- Update ESS EDC notification creation asset endpoint to v3

## [4.2.0] - 2023-11-28
### Changed
- Changed default behaviour of IRS - when aspects list is not provided or empty in request body, IRS will not collect any submodel now (previously default aspects were collected).
- ESS
  - Added 'hops' parameter to SupplyChainImpacted Aspect model - contains relative distance in the supply chain
  - Added `impactedSuppliersOnFirstTier` parameter to Supply SupplyChainImpacted Aspect model - contains information of first level supply chain impacted
- Exported health endpoints to prometheus (see HealthMetricsExportConfiguration,
  DependenciesHealthMetricsExportConfiguration) and
  added [system health dashboard](charts/item-relationship-service/dashboards/system-health-dashboard.json)
  in order to visualize health metrics of IRS and its dependencies. #283

### Fixed
- Fixed incorrect passing of incidentBPNS for ESS Orders

### Known knowns

- [#253] Cancellation of order jobs is not working stable

## [4.1.0] - 2023-11-15
### Added
- IRS can now check the readiness of external services. Use the new ``management.health.dependencies.enabled`` config entry to determine if external dependencies health checks should be checked (false by default).
  - The map of external services healthcheck endpoints can be configured with ``management.health.dependencies.urls`` property, eg. ``service_name: http://service_name_host/health``
- Added cache mechanism for ConnectorEndpointService for fetchConnectorEndpoints method cache

### Changed
- Changed name of spring's OAuth2 client registration from 'keycloak' to 'common' like below:
  ```
  spring:
    security:
      oauth2:
        client:
          registration:
            keycloak:
              authorization-grant-type: client_credentials
              client-id: 
              client-secret: 
          provider:
            keycloak:
              token-uri:
  ```
  to:
  ```
  spring:
    security:
      oauth2:
        client:
          registration:
            common:
              authorization-grant-type: client_credentials
              client-id: 
              client-secret: 
          provider:
            common:
              token-uri:
  ```
- Update IRS API Swagger documentation to match AAS 3.0.0

### Fixed
- IRS will return 206 Http status from GET /jobs/{id} endpoint if Job is still running

## [4.0.2] - 2023-11-20
### Changed
- Remove `apk upgrade --no-cache libssl3 libcrypto3` in Docker base image to be TRG compliant

## [4.0.1] - 2023-11-10
### Changed
- Added state `STARTED` as acceptable state to complete the EDC transfer process to be compatible with EDC 0.5.1

## [4.0.0] - 2023-10-27
### Added
- Introduced new API endpoint to register ESS Jobs in Batch - POST {{IRS_HOST}}/irs/ess/orders
- Added role "admin_irs" again

### Changed
- Deprecated query parameter 'jobStates' was removed from GET {{IRS_HOST}}/irs/jobs endpoint. TRI-996
- Moved OAuth2 JWT token claim to configuration. The fields can be configured with `oauth.resourceClaim`, `oauth.irsNamespace`, `oauth.roles`.
- ESS
  - Added Tombstone to ESS investigation in case required aspect models "PartAsPlanned" or "PartSiteInformationAsPlanned" are missing
- Update dependencies to mitigate third party vulnerabilities

## [3.5.4] - 2023-10-25
### Changed
- removed role "admin_irs"

## [3.5.3] - 2023-10-09
### Fixed
- Fixed default policy creation.

### Changed
- Changed configuration for default policies from:
  ```
  irs-edc-client:
    catalog:
      policies:
        allowedNames: A, B
        acceptedLeftOperands: X
        acceptedRightOperands: Y
  ```
  to:
  ```
  irs-edc-client:
    catalog:
      acceptedPolicies:
      - leftOperand: "X"
        operator: "eq"
        rightOperand: "A"
      - leftOperand: "B"
        operator: "eq"
        rightOperand: Y"
  ```

## [3.5.2] - 2023-10-06
### Changed
- Updated dependencies

## [3.5.1] - 2023-10-05
### Fixed
- Fix json schema validation

## [3.5.0] - 2023-09-27
### Changed
- IRS now makes use of the value `dspEndpoint` in `subprotocolBody` of the Asset Administration Shell to request submodel data directly.
- Policy Store API is extended to handle:
  - multi permissions per each allowed Policy in POST call to create Policy
  - multi constraint per each permission in POST call to create Permission
  - logical AndConstraint and OrConstraint to give possibility to create complex restriction

### Fixed
- Fixed a case where IRS submodel requests did not reuqest all EDC endpoints discovered by Discovery Finder
- ESS
  - Updated investigation request body field `incidentBPNs` to `incidentBPNSs`.
  - Streamlined EDC notification flow and adjusted it to existing EDC client methods
  - Changed investigation from BPNL to BPNS (`catenaXSiteId` of `PartSiteInformationAsPlanned`)
  - Additional validation for `validityPeriod` of `PartAsPlanned`

## [3.4.1] - 2023-09-22
### Changed
- Updated SingleLevelUsageAsBuilt schema to 2.0.0 version.

### Fixed
- Fixed missing access control for Batch and ESS API.

## [3.4.0] - 2023-09-01
### Added
- Added fetchCatalog to EDCCatalogFacade
- Introduced new API endpoint to update 'validUntil' property of Policy - PUT {{IRS_HOST}}/irs/policies/{policyId}
- Introduced new IRS role `admin_irs` which has unrestricted access to every API endpoint

### Changed

- Adjusted API access control. Users with role `view_irs` can only access jobs they created themselves. Policy Store API
  access is restricted to role `admin_irs`.

### Fixed
- Fixed bug where BPN's were delivered without 'manufacturerName' property filled

## [3.3.5] - 2023-08-30
### Changed
- Updated IRS Digital Twin Registry Client to support latest version 0.3.14-M1

## [3.3.4] - 2023-08-24
### Fixed
- Added missing license information to documentation and docker image

## [3.3.3] - 2023-08-11
### Changed
- IRS now calls the entire dataplane URL retrieved from the registry href instead of building it from the URL of the EDC token and the path

### Fixed
- Switched to POST for DTR lookup request
- Added Base64 encoding to identifier for DTR shell-descriptor request 
- Fixed an issue where IRS did not pass the BPN correctly for the ESS use-case

## [3.3.2] - 2023-07-31
### Fixed
- BPN is now passed on correctly when traversing the item graph
- EDC Policies now get validated regardless of the type of constraint.
- EDC Policies of type FrameworkAgreement are now validated correctly.
- Fixed error in BPN handling for IRS Batch requests

## [3.3.1] - 2023-07-24
### Fixed
- Added missing field `businessPartner` for relationship aspect SingleLevelUsageAsBuilt

## [3.3.0] - 2023-07-20
### Changed
- BPN is now taken from the submodel data while traversing the item graph
- Tombstone is created if no BPN is available for a child item

## [3.2.1] - 2023-07-19
### Fixed
- EDC Policies now get validated regardless of the type of constraint.
- EDC Policies of type `FrameworkAgreement` are now validated correctly.
- Fixed error in BPN handling for IRS Batch requests

## [3.2.0] - 2023-07-14
### Changed
- The client code for accessing the Digital Twin Registry (central and decentral) is now available as a spring boot maven library. See the README in the irs-registry-client module for more information.
- Update EDC dependencies to 0.1.3
- Add Transformer to support new EDC constraint operator format
- IRS now supports the AAS API 3.0 and its updated models. **Note**: this also reflects in the Job response shells, please check the new schema.

### Known knowns
- [TRI-1460] ESS Notifications endpoints are not working in the decentral Digital Twin Registry scenario because endpoints does not provide bpn as a parameter. 
- [TRI-1096] No limiting of requests in parallel - IRS allows sending API requests unlimited
- [TRI-1100] Potential denial-of-service (DoS) attack - IRS allows to enter a large number of characters, which are reflected in the response of the server
- [TRI-1098] Software related information disclosure - IRS returns redundant information about the type and version of used software
- [TRI-793] Misconfigured Access-Control-Allow- Origin Header - by intercepting network traffic it could be possible to read and modify any messages that are exchanged with server
- [TRI-1095] HTTP security headers configuration could be improved and allow for additional protection against some web application attacks
- [TRI-1441] Synchronous communication with shared C-X services without circuit breaker pattern - potentially could affect IRS resilience when other services becomes non-responsive.
- [TRI-1441] Cascading effects of failure when Digital Twin Registry becomes non-responsive - potentially bulkhead pattern could improve IRS resilience
- [TRI-1477] Retry mechanism used inside IRS could potentially affect IRS resilience - DDOS other services on which IRS is dependent, exhaustion of resources and available threads, etc.
- [TRI-1478] Lack of resources management - thread pooling, heap limitation etc.
- [TRI-1024] IRS does not support scale out on multiple instances

## [3.1.0] - 2023-07-07
### Changed
- Removed catalog cache
- Changed EDC catalog retrieval from pagination to filter
- Item graphs with asBuilt lifecycle & downward direction are now built with usage of SingleLevelBomAsBuilt aspect, instead of AssemblyPartRelationship aspect
- Changed retrieval of BPN value from AAS Shell to SingleLevelBomAsBuilt
- Renamed SerialPartTypization to SerialPart aspect
- ESS
  - Update ESS notification asset creation to new EDC DSP protocol
  - Include DiscoveryFinder into ESS flow

## [3.0.1] - 2023-06-28
### Fixed
- Added missing participantId to contract negotiation for decentral DTR contract negotiation
- Fixed default value for contract negotiation and transfer process state-suffix

## [3.0.0] - 2023-06-26
### Added
- Handling of Decentral Digital Twin Registry as a way of request AAS for identifier
  - Extend Register Job with key field that contain BPN and globalAssetId
  - Requesting BPN endpoint catalog over Discrovery Finder
  - Requesting EDC endpoint addresses for BPN over EDC Discovery Finder
  - Add filter for catalog item search in EDC
  - Authorize Digital Twin client with EDC Endpoint Reference
- Added new Policy Store API to manage acceptable EDC policies
  - `GET /irs/policies`
  - `POST /irs/policies`
  - `DELETE /irs/policies/{policyId}`

### Changed
- Updated EDC Client to use version 0.4.1
  - Adjusted Protocol from IDS to DSP
  - Paths for catalog, contract negotiation and transfer process are now configurable via properties
    - `edc.controlplane.endpoint.catalog`
    - `edc.controlplane.endpoint.contract-negotiation` 
    - `edc.controlplane.endpoint.transfer-process`
- EDR Callback is now configurable via property `edc.callback-url`

## [2.6.1] - 2023-05-15
### Added
- Validation if bpnEndpoint is set in properties before starting a job with lookupBPNs set to true
- Automate release workflow
- Validate if callback url starts with http or https before register a job

## [2.6.0] - 2023-05-05
### Added
- IRS now checks the EDC policies and only negotiates contracts if the policy matches the ones defined in the configuration at `edc.catalog.policies.allowedNames` (comma separated string)

### Changed
- Restructured the repository to make it more comprehensive
- Improved API descriptions regarding errors 

## [2.5.1] - 2023-04-28
### Changed
- Replaced Discovery Service mock with real implementation

## [2.5.0] - 2023-04-17
### Added
- Introduced Batch processing API endpoints. Batch Order is registered and executed for a bunch of globalAssetIds in one call.
  - API Endpoint POST Register Batch Order {{IRS_HOST}}/irs/orders
  - API Endpoint GET Batch Order {{IRS_HOST}}/irs/orders/:orderId
  - API Endpoint GET Batch {{IRS_HOST}}/irs/orders/:orderId/batches/:batchId
- Introduced Environmental- and Social Standards processing API endpoints. 
  - API Endpoint POST Register job to start an investigation if a given bpn is contained in a part chain {{IRS_HOST}}/ess/bpn/investigations
  - API Endpoint GET BPN Investigation {{IRS_HOST}}/ess/bpn/investigations/:id
  - API Endpoint POST EDC Notification receive {{IRS_HOST}}/ess/notification/receive


## [2.4.1] - 2023-04-21
### Fixed
- Updated spring-boot version to 3.0.6 to fix security issue
- change GID in Dockerfile to fix https://github.com/eclipse-tractusx/item-relationship-service/issues/101


## [2.4.0] - 2023-03-30
### Added
- IRS is now able to cache the EDC catalog. Caching can be disabled via application config. Maximum amount of cached items and item time-to-live can be configured as well. 
- EDC policies retrieved from contract offer are now added to the contract negotiation

### Changed
- API endpoints have now additional layer of security and require BPN claim in token. Allowed BPN that can access API can be configured with (*env:API_ALLOWED_BPN*) variable.
- Updated Spring Boot dependency to 3.0.5

### Fixed
- Fixed issue in paging when calling SemanticsHub with some page size configurations 


## [2.3.2] - 2023-03-20
### Changed
- Replace pandoc with downdoc for conversion asciidoc to markdown

### Fixed
- In AssemblyPartRelationship the ``measurementUnit`` can be both parsed from both string and object versions
- Decode URLs for ``assetId`` to prevent bug that encoded ``assetId`` cannot be found in the catalog

## [2.3.1] - 2023-03-07
### Changed
- Updated Spring Boot dependency to 3.0.3

## [2.3.0] - 2023-02-21
### Added
- Introduced new endpoint ``/irs/aspectmodels`` which will list all available aspect models (from semantic hub or locally provided files if present)

### Fixed
- If Grafana is enabled - dashboards will be automatically imported on startup

### Changed
- Job creation validates ``aspects`` by using models available in semantic hub or locally provided.

## [2.2.1] - 2023-03-15
### Fixed
- Property "measurementUnit" of AssemblyPartRelationship can now be a String or a Map. According to the latest model, it is supposed to be a String, but due to varying test data, IRS supports both variants.
- EDC Catalog IDs are now being URL decoded before usage

## [2.2.0] - 2023-01-20
### Added
- Added new job parameter flag "lookupBPNs" which toggles lookup of BPN numbers using the configured BPN URL
- Added new summary item "bpnLookups" which tracks completed and failed BPN requests. Excluded these metrics from "asyncFetchedItems"
- Model schema JSON files can now be provided locally as a backup to the Semantic Hub.
  Use the new ``semanticsHub.localModelDirectory`` config entry to provide a folder with the models.
- Added pagination to EDC catalog retrieval.

### Fixed
- BPNs array is now filled correctly when requesting a running job with parameter "returnUncompletedJob=true"

## [2.1.0] - 2023-01-11
### Changed
- Change 'jobParameter' to 'parameter' in GET calls in IRS API
- Change 'jobStates' to 'states' request parameter in GET call for jobs by states, 'jobStates' is now deprecated
- REST clients for DTR, SemHub and BPDM now use their own RestTemplates and configuration
- application.yaml received some documentation

## [2.0.0] - 2022-12-09
### Added
- Added pagination to GET /irs/jobs endpoint (eg. {{IRS_HOST}}/irs/jobs?page=0&size=10&sort=completedOn,asc)

### Changed
- IRS API now requires 'view_irs' resource access inside Keycloak JWT token.
- New 2.0.0 version of IRS API. Main goal was to remove 'job' prefix from attribute names
  - change 'jobId' to 'id' in GET and POST calls
  - change 'jobState' to 'state' in GET calls
  - change 'jobCompleted' to 'completedOn' in GET calls
  - change 'jobId' to 'id' and 'jobState' to 'state' in callback URI variables

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


[Unreleased]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.4.1...HEAD
[5.4.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.4.0...5.4.1
[5.4.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.3.0...5.4.0
[5.3.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.2.0...5.3.0
[5.2.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.1.4...5.2.0
[5.1.4]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.1.3...5.1.4
[5.1.3]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.1.2...5.1.3
[5.1.2]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.1.1...5.1.2
[5.1.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.1.0...5.1.1
[5.1.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/5.0.0...5.1.0
[5.0.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.9.0...5.0.0
[4.9.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.8.0...4.9.0
[4.8.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.7.0...4.8.0
[4.7.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.6.0...4.7.0
[4.6.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.5.2...4.6.0
[4.5.2]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.5.1...4.5.2
[4.5.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.5.0...4.5.1
[4.5.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.5.0...4.5.1
[4.5.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.4.0...4.5.0
[4.4.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.3.0...4.4.0
[4.3.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.2.0...4.3.0
[4.2.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.1.0...4.2.0
[4.1.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.0.2...4.1.0
[4.0.2]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.0.1...4.0.2
[4.0.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/4.0.0...4.0.1
[4.0.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.5.4...4.0.0
[3.5.4]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.5.3...3.5.4
[3.5.3]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.5.2...3.5.3
[3.5.2]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.5.1...3.5.2
[3.5.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.5.0...3.5.1
[3.5.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.4.1...3.5.0
[3.4.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.4.0...3.4.1
[3.4.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.3.5...3.4.0
[3.3.5]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.3.4...3.3.5
[3.3.4]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.3.3...3.3.4
[3.3.3]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.3.2...3.3.3
[3.3.2]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.3.1...3.3.2
[3.3.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.3.0...3.3.1
[3.3.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.2.1...3.3.0
[3.2.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.2.0...3.2.1
[3.2.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.1.0...3.2.0
[3.1.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.0.1...3.1.0
[3.0.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/3.0.0...3.0.1
[3.0.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.6.1...3.0.0
[2.6.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.6.0...2.6.1
[2.6.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.5.1...2.6.0
[2.5.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.5.0...2.5.1
[2.5.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.4.0...2.5.0
[2.4.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.4.0...2.4.1
[2.4.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.3.2...2.4.0
[2.3.2]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.3.1...2.3.2
[2.3.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.3.0...2.3.1
[2.3.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.2.0...2.3.0
[2.2.1]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.2.0...2.2.1
[2.2.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.1.0...2.2.0
[2.1.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/2.0.0...2.1.0
[2.0.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.6.0...2.0.0
[1.6.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.5.0...1.6.0
[1.5.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.4.0...1.5.0
[1.4.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.3.0...1.4.0
[1.3.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/1.2.0...1.3.0
[1.2.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/v1.1.0...1.2.0
[1.1.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/eclipse-tractusx/item-relationship-service/compare/v0.9.1...v1.0.0
[0.9.1]: https://github.com/eclipse-tractusx/item-relationship-service/commits/v0.9.1
[0.9.0]: https://github.com/eclipse-tractusx/item-relationship-service/commits/v0.9.0
