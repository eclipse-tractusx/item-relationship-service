# Digital Twin Registry Client Library

This library assists in communicating with the Digital Twin Registry in a central or decentral approach.

In the decentral approach, it also handles the communication via the Discovery Finder and EDC.

The library is based on Spring Boot and uses its configuration features.

## Usage

### Auto setup

Add this library to your classpath and add the following configuration to your `application.yaml`:

```yaml
digitalTwinRegistryClient:
  type: "central" # or "decentral"

  discoveryFinderUrl: "" # required if type is "decentral"

  descriptorEndpoint: "" # required if type is "central", must contain the placeholder {aasIdentifier}
  shellLookupEndpoint: "" # required if type is "central", must contain the placeholder {assetIds}

edc:
  callback-url: "" # The URL where the EDR token callback will be sent to.
  controlplane:
    request-ttl: PT10M # How long to wait for an async EDC negotiation request to finish, ISO 8601 Duration
    endpoint:
      data: "" # URL of the EDC consumer controlplane data endpoint
      catalog: /v2/catalog/request # EDC consumer controlplane catalog path
      contract-negotiation: /v2/contractnegotiations # EDC consumer controlplane contract negotiation path
      transfer-process: /v2/transferprocesses # EDC consumer controlplane transfer process path
      state-suffix: /state # Path of the state suffix for contract negotiation and transfer process
    provider-suffix: /api/v1/dsp # Suffix to add to data requests to the EDC provider controlplane
    catalog-limit: 1000 # Max number of items to fetch from the EDC provider catalog
    catalog-page-size: 50 # Number of items to fetch at one page from the EDC provider catalog when using pagination
    api-key:
      header: "" # API header key to use in communication with the EDC consumer controlplane
      secret: "" # API header secret to use in communication with the EDC consumer controlplane
    datareference:
      storage:
        duration: PT1H # Time after which stored data references will be cleaned up, ISO 8601 Duration

  submodel:
    request-ttl: PT10M # How long to wait for an async EDC submodel retrieval to finish, ISO 8601 Duration
    path: /submodel # The path to append to the submodel data reference endpoint
    urn-prefix: /urn # A prefix used to identify URNs correctly in the submodel endpoint address
    timeout:
      read: PT90S # HTTP read timeout for the submodel client
      connect: PT90S # HTTP connect timeout for the submodel client

  catalog:
    cache:
      enabled: false # Set to false to disable caching
      ttl: P1D # Time after which a cached Item is no longer valid and the real catalog is called instead
      maxCachedItems: 64000 # Maximum amount of cached catalog items

```

Please note that you also need to provide a `RestTemplate` bean for the **Qualifier** `digitalTwinRegistryRestTemplate` (central approach) or `edcRestTemplate` (decentral approach).

### Manual setup

You can set up the beans by yourself. This can be useful if you want to override some default implementations or
programmatically provide the right dependencies. Please refer to
the `org.eclipse.tractusx.irs.registryclient.DefaultConfiguration` for an example.