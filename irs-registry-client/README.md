[[Back to IRS main README](../README.md)]

# Digital Twin Registry Client Library

This library assists in communicating with the Digital Twin Registry in a central or decentral approach.
<!-- TODO (mfischer): #410: remove mention of the central approach after-->

In the decentral approach, it also handles the communication via the Discovery Finder and EDC.

The library is based on [Spring Boot](https://spring.io/projects/spring-boot) and uses its configuration features.

## Usage

### Auto setup

Include the library into your project:

```
<dependencies>
    <dependency>
        <groupId>org.eclipse.tractusx.irs</groupId>
        <artifactId>irs-registry-client</artifactId>
        <version>1.3.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Add the following configuration to your `application.yaml`:

```yaml
digitalTwinRegistryClient:
  type: "central" # or "decentral"

  discoveryFinderUrl: "" # required if type is "decentral"

  descriptorEndpoint: "" # required if type is "central", must contain the placeholder {aasIdentifier}
  shellLookupEndpoint: "" # required if type is "central", must contain the placeholder {assetIds}
  shellDescriptorTemplate: /shell-descriptors/{aasIdentifier} # The path to retrieve AAS descriptors from the DTR. Required if type is "decentral", must contain the placeholder {aasIdentifier}
  lookupShellsTemplate: /lookup/shells?assetIds={assetIds} # The path to lookup shells from the DTR. Required if type is "decentral", must contain the placeholder {assetIds}

irs-edc-client:
  callback-url: "" # The URL where the EDR token callback will be sent to. This defaults to {BASE_URL}/internal/endpoint-data-reference. If you want to use a different mapping, you can override it with irs-edc-client.callback.mapping.
  asyncTimeout: PT10M # Timout for future.get requests as ISO 8601 Duration  
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
    urn-prefix: /urn # A prefix used to identify URNs correctly in the submodel endpoint address


  catalog:
    # IRS will only negotiate contracts for offers with a policy as defined in the acceptedPolicies list.
    # If a requested asset does not provide one of these policies, a tombstone will be created and this node will not be processed.
    acceptedPolicies:
      - leftOperand: "PURPOSE"
        operator: "eq"
        rightOperand: "ID 3.0 Trace"
      - leftOperand: "PURPOSE"
        operator: "eq"
        rightOperand: "ID 3.1 Trace"
      - leftOperand: "PURPOSE"
        operator: "eq"
        rightOperand: R2_Traceability
      - leftOperand: "FrameworkAgreement.traceability"
        operator: "eq"
        rightOperand: "active"
      - leftOperand: "Membership"
        operator: "eq"
        rightOperand: "active"

```

Please note that you also need to provide a `RestTemplate` bean for the **Qualifier** `digitalTwinRegistryRestTemplate` (central approach) or `edcClientRestTemplate` (decentral approach).

You also need to tell the EDC client which policies are acceptable. You can either implement a bean with the interface `org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider` or use the fallback `org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider.DefaultAcceptedPoliciesProvider` bean directly and add the accepted policies there.

As a last step, add this annotation to your Application class:

```
@ComponentScan({ "<your.base.package>",
                 "org.eclipse.tractusx.irs.registryclient",
                 "org.eclipse.tractusx.irs.edc.client"
})
```

Now you can start using the client by injecting a bean of the `DigitalTwinRegistryService` interface. Depending on the configured mode, it will be central or decentral. 

### Manual setup

You can set up the beans by yourself. This can be useful if you want to override some default implementations or
programmatically provide the right dependencies. Please refer to
the `org.eclipse.tractusx.irs.registryclient.DefaultConfiguration` for an example.