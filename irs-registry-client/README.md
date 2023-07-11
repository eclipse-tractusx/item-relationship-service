# Digital Twin Registry Client Library

This library assists in communicating with the Digital Twin Registry in a central or decentral approach.

In the decentral approach, it also handles the communication via the Discovery Finder and EDC.

The library is based on Spring Boot and uses its configuration features.

## Usage

### Auto setup

Include the library into your project:

```
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/catenax-ng/tx-item-relationship-service</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.eclipse.tractusx.irs</groupId>
        <artifactId>irs-registry-client</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```
**Note**: Since the library is currently only available on GitHub Packages, you need to provide a personal access token (PAT) for local builds. Update your Maven settings.xml (usually located at ~/.m2/settings.xml) like this:
```
<servers>
    <server>
        <id>github</id>
        <username>enter-your-github-username</username>
        <password>enter-your-generated-personal-access-token</password>
    </server>
</servers>
```

Add the following configuration to your `application.yaml`:

```yaml
digitalTwinRegistryClient:
  type: "central" # or "decentral"

  discoveryFinderUrl: "" # required if type is "decentral"

  descriptorEndpoint: "" # required if type is "central", must contain the placeholder {aasIdentifier}
  shellLookupEndpoint: "" # required if type is "central", must contain the placeholder {assetIds}

irs-edc-client:
  callback-url: "" # The URL where the EDR token callback will be sent to. This defaults to {BASE_URL}/internal/endpoint-data-reference. If you want to use a different mapping, you can override it with irs-edc-client.callback.mapping.
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


  catalog:
    cache:
      enabled: false # Set to false to disable caching
      ttl: P1D # Time after which a cached Item is no longer valid and the real catalog is called instead
      maxCachedItems: 64000 # Maximum amount of cached catalog items

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