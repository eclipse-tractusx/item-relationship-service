# Installation Instructions

The deployment contains the components required to connect the IRS to an existing Catena-X network. This includes:

- IRS with Minio - part of the "irs-helm" Helm chart
- EDC Consumer (controlplane & dataplane) - part of the "irs-edc-consumer" Helm chart

Everything else needs to be provided externally.

## Data Chain Kit

You can use the Data Chain Kit to deploy the whole demo scenario with all participating components.  
Instructions can be found here: [Data Chain Kit](https://eclipse-tractusx.github.io/docs/kits/Data%20Chain%20Kit/Operation%20View/).

## Installation

The IRS Helm repository can be found here: [https://eclipse-tractusx.github.io/item-relationship-service/index.yaml](https://eclipse-tractusx.github.io/docs/kits/Data%20Chain%20Kit/Operation%20View/)

Use the latest release of the "irs-helm" chart.
It contains all required dependencies.

If you also want to set up your own EDC consumer, use the "irs-edc-consumer" chart.

Supply the required configuration properties (see chapter [Configuration](#configuration)) in a values.yaml file or
override the settings directly.

More information: [Administration Guide](https://eclipse-tractusx.github.io/item-relationship-service/docs/administration/administration-guide.html)

### Deployment using Helm

Add the IRS Helm repository:

```(shell)
    helm repo add irs https://eclipse-tractusx.github.io/item-relationship-service
```

Then install the Helm chart into your cluster:

```(shell)
    helm install -f your-values.yaml irs-app irs/irs-helm
```

Or create a new Helm chart and use the IRS as a dependency.

```(yaml)
    dependencies:
      - name: irs-helm
        repository: https://eclipse-tractusx.github.io/item-relationship-service
        version: 3.x.x
      - name: irs-edc-consumer # optional
        repository: https://eclipse-tractusx.github.io/item-relationship-service
        version: 1.x.x
```

Then provide your configuration as the values.yaml of that chart.

Create a new application in ArgoCD and point it to your repository / Helm chart folder.

## Configuration

Take the following template and adjust the configuration parameters (&lt;placeholders&gt; mark the relevant spots).
You can define the URLs as well as most of the secrets yourself.

The Keycloak, DAPS and Vault configuration / secrets depend on your setup and might need to be provided externally.

### Helm configuration IRS (values.yaml)

```(yaml)
    #####################
    # IRS Configuration #
    #####################
    irsUrl: "https://<irs-url>"
    ingress:
      enabled: false
      className: "nginx"
      annotations:
        nginx.ingress.kubernetes.io/ssl-passthrough: "false"
        nginx.ingress.kubernetes.io/backend-protocol: "HTTP"
        nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
      hosts:
        - host: "<irs-url>"
          paths:
            - path: /
              pathType: ImplementationSpecific
      tls:
        - hosts:
            - "<irs-url>"
          secretName: tls-secret
    digitalTwinRegistry:
      url: https://<digital-twin-registry-url>
      descriptorEndpoint: "{{ tpl .Values.digitalTwinRegistry.url . }}/registry/shell-descriptors/{aasIdentifier}"
      shellLookupEndpoint: "{{ tpl .Values.digitalTwinRegistry.url . }}/lookup/shells?assetIds={assetIds}"
    semanticshub:
      url: https://<semantics-hub-url>
      modelJsonSchemaEndpoint: "{{ tpl .Values.semanticsHub.url . }}/{urn}/json-schema"
      defaultUrns: >-
        urn:bamm:io.catenax.serial_part_typization:1.0.0#SerialPartTypization
    #    ,urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship
      localModels:
    #   Map of Base64 encoded strings of semantic models. The key must be the Base64 encoded full URN of the model.
    #   Example for urn:bamm:io.catenax.serial_part_typization:1.1.1#SerialPartTypization:
    #    dXJuOmJhbW06aW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uOjEuMS4xI1NlcmlhbFBhcnRUeXBpemF0aW9u: ewoJIiRzY2hlbWEiOiAiaHR0cDovL2pzb24tc2NoZW1hLm9yZy9kcmFmdC0wNC9zY2hlbWEiLAoJInR5cGUiOiAib2JqZWN0IiwKCSJjb21wb25lbnRzIjogewoJCSJzY2hlbWFzIjogewoJCQkidXJuX2JhbW1faW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uXzEuMS4xX0NhdGVuYVhJZFRyYWl0IjogewoJCQkJInR5cGUiOiAic3RyaW5nIiwKCQkJCSJwYXR0ZXJuIjogIiheWzAtOWEtZkEtRl17OH0tWzAtOWEtZkEtRl17NH0tWzAtOWEtZkEtRl17NH0tWzAtOWEtZkEtRl17NH0tWzAtOWEtZkEtRl17MTJ9JCl8KF51cm46dXVpZDpbMC05YS1mQS1GXXs4fS1bMC05YS1mQS1GXXs0fS1bMC05YS1mQS1GXXs0fS1bMC05YS1mQS1GXXs0fS1bMC05YS1mQS1GXXsxMn0kKSIKCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9LZXlDaGFyYWN0ZXJpc3RpYyI6IHsKCQkJCSJ0eXBlIjogInN0cmluZyIKCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9WYWx1ZUNoYXJhY3RlcmlzdGljIjogewoJCQkJInR5cGUiOiAic3RyaW5nIgoJCQl9LAoJCQkidXJuX2JhbW1faW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uXzEuMS4xX0tleVZhbHVlTGlzdCI6IHsKCQkJCSJ0eXBlIjogIm9iamVjdCIsCgkJCQkicHJvcGVydGllcyI6IHsKCQkJCQkia2V5IjogewoJCQkJCQkiJHJlZiI6ICIjL2NvbXBvbmVudHMvc2NoZW1hcy91cm5fYmFtbV9pby5jYXRlbmF4LnNlcmlhbF9wYXJ0X3R5cGl6YXRpb25fMS4xLjFfS2V5Q2hhcmFjdGVyaXN0aWMiCgkJCQkJfSwKCQkJCQkidmFsdWUiOiB7CgkJCQkJCSIkcmVmIjogIiMvY29tcG9uZW50cy9zY2hlbWFzL3Vybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9WYWx1ZUNoYXJhY3RlcmlzdGljIgoJCQkJCX0KCQkJCX0sCgkJCQkicmVxdWlyZWQiOiBbCgkJCQkJImtleSIsCgkJCQkJInZhbHVlIgoJCQkJXQoJCQl9LAoJCQkidXJuX2JhbW1faW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uXzEuMS4xX0xvY2FsSWRlbnRpZmllckNoYXJhY3RlcmlzdGljIjogewoJCQkJInR5cGUiOiAiYXJyYXkiLAoJCQkJIml0ZW1zIjogewoJCQkJCSIkcmVmIjogIiMvY29tcG9uZW50cy9zY2hlbWFzL3Vybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9LZXlWYWx1ZUxpc3QiCgkJCQl9LAoJCQkJInVuaXF1ZUl0ZW1zIjogdHJ1ZQoJCQl9LAoJCQkidXJuX2JhbW1faW8ub3Blbm1hbnVmYWN0dXJpbmdfY2hhcmFjdGVyaXN0aWNfMi4wLjBfVGltZXN0YW1wIjogewoJCQkJInR5cGUiOiAic3RyaW5nIiwKCQkJCSJwYXR0ZXJuIjogIi0/KFsxLTldWzAtOV17Myx9fDBbMC05XXszfSktKDBbMS05XXwxWzAtMl0pLSgwWzEtOV18WzEyXVswLTldfDNbMDFdKVQoKFswMV1bMC05XXwyWzAtM10pOlswLTVdWzAtOV06WzAtNV1bMC05XShcXC5bMC05XSspP3woMjQ6MDA6MDAoXFwuMCspPykpKFp8KFxcK3wtKSgoMFswLTldfDFbMC0zXSk6WzAtNV1bMC05XXwxNDowMCkpPyIKCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9Qcm9kdWN0aW9uQ291bnRyeUNvZGVUcmFpdCI6IHsKCQkJCSJ0eXBlIjogInN0cmluZyIsCgkJCQkicGF0dGVybiI6ICJeW0EtWl1bQS1aXVtBLVpdJCIKCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9NYW51ZmFjdHVyaW5nQ2hhcmFjdGVyaXN0aWMiOiB7CgkJCQkidHlwZSI6ICJvYmplY3QiLAoJCQkJInByb3BlcnRpZXMiOiB7CgkJCQkJImRhdGUiOiB7CgkJCQkJCSIkcmVmIjogIiMvY29tcG9uZW50cy9zY2hlbWFzL3Vybl9iYW1tX2lvLm9wZW5tYW51ZmFjdHVyaW5nX2NoYXJhY3RlcmlzdGljXzIuMC4wX1RpbWVzdGFtcCIKCQkJCQl9LAoJCQkJCSJjb3VudHJ5IjogewoJCQkJCQkiJHJlZiI6ICIjL2NvbXBvbmVudHMvc2NoZW1hcy91cm5fYmFtbV9pby5jYXRlbmF4LnNlcmlhbF9wYXJ0X3R5cGl6YXRpb25fMS4xLjFfUHJvZHVjdGlvbkNvdW50cnlDb2RlVHJhaXQiCgkJCQkJfQoJCQkJfSwKCQkJCSJyZXF1aXJlZCI6IFsKCQkJCQkiZGF0ZSIKCQkJCV0KCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9QYXJ0SWRDaGFyYWN0ZXJpc3RpYyI6IHsKCQkJCSJ0eXBlIjogInN0cmluZyIKCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9QYXJ0TmFtZUNoYXJhY3RlcmlzdGljIjogewoJCQkJInR5cGUiOiAic3RyaW5nIgoJCQl9LAoJCQkidXJuX2JhbW1faW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uXzEuMS4xX0NsYXNzaWZpY2F0aW9uQ2hhcmFjdGVyaXN0aWMiOiB7CgkJCQkidHlwZSI6ICJzdHJpbmciLAoJCQkJImVudW0iOiBbCgkJCQkJInByb2R1Y3QiLAoJCQkJCSJyYXcgbWF0ZXJpYWwiLAoJCQkJCSJzb2Z0d2FyZSIsCgkJCQkJImFzc2VtYmx5IiwKCQkJCQkidG9vbCIsCgkJCQkJImNvbXBvbmVudCIKCQkJCV0KCQkJfSwKCQkJInVybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9QYXJ0VHlwZUluZm9ybWF0aW9uQ2hhcmFjdGVyaXN0aWMiOiB7CgkJCQkidHlwZSI6ICJvYmplY3QiLAoJCQkJInByb3BlcnRpZXMiOiB7CgkJCQkJIm1hbnVmYWN0dXJlclBhcnRJZCI6IHsKCQkJCQkJIiRyZWYiOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvdXJuX2JhbW1faW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uXzEuMS4xX1BhcnRJZENoYXJhY3RlcmlzdGljIgoJCQkJCX0sCgkJCQkJImN1c3RvbWVyUGFydElkIjogewoJCQkJCQkiJHJlZiI6ICIjL2NvbXBvbmVudHMvc2NoZW1hcy91cm5fYmFtbV9pby5jYXRlbmF4LnNlcmlhbF9wYXJ0X3R5cGl6YXRpb25fMS4xLjFfUGFydElkQ2hhcmFjdGVyaXN0aWMiCgkJCQkJfSwKCQkJCQkibmFtZUF0TWFudWZhY3R1cmVyIjogewoJCQkJCQkiJHJlZiI6ICIjL2NvbXBvbmVudHMvc2NoZW1hcy91cm5fYmFtbV9pby5jYXRlbmF4LnNlcmlhbF9wYXJ0X3R5cGl6YXRpb25fMS4xLjFfUGFydE5hbWVDaGFyYWN0ZXJpc3RpYyIKCQkJCQl9LAoJCQkJCSJuYW1lQXRDdXN0b21lciI6IHsKCQkJCQkJIiRyZWYiOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvdXJuX2JhbW1faW8uY2F0ZW5heC5zZXJpYWxfcGFydF90eXBpemF0aW9uXzEuMS4xX1BhcnROYW1lQ2hhcmFjdGVyaXN0aWMiCgkJCQkJfSwKCQkJCQkiY2xhc3NpZmljYXRpb24iOiB7CgkJCQkJCSIkcmVmIjogIiMvY29tcG9uZW50cy9zY2hlbWFzL3Vybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9DbGFzc2lmaWNhdGlvbkNoYXJhY3RlcmlzdGljIgoJCQkJCX0KCQkJCX0sCgkJCQkicmVxdWlyZWQiOiBbCgkJCQkJIm1hbnVmYWN0dXJlclBhcnRJZCIsCgkJCQkJIm5hbWVBdE1hbnVmYWN0dXJlciIsCgkJCQkJImNsYXNzaWZpY2F0aW9uIgoJCQkJXQoJCQl9CgkJfQoJfSwKCSJwcm9wZXJ0aWVzIjogewoJCSJjYXRlbmFYSWQiOiB7CgkJCSIkcmVmIjogIiMvY29tcG9uZW50cy9zY2hlbWFzL3Vybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9DYXRlbmFYSWRUcmFpdCIKCQl9LAoJCSJsb2NhbElkZW50aWZpZXJzIjogewoJCQkiJHJlZiI6ICIjL2NvbXBvbmVudHMvc2NoZW1hcy91cm5fYmFtbV9pby5jYXRlbmF4LnNlcmlhbF9wYXJ0X3R5cGl6YXRpb25fMS4xLjFfTG9jYWxJZGVudGlmaWVyQ2hhcmFjdGVyaXN0aWMiCgkJfSwKCQkibWFudWZhY3R1cmluZ0luZm9ybWF0aW9uIjogewoJCQkiJHJlZiI6ICIjL2NvbXBvbmVudHMvc2NoZW1hcy91cm5fYmFtbV9pby5jYXRlbmF4LnNlcmlhbF9wYXJ0X3R5cGl6YXRpb25fMS4xLjFfTWFudWZhY3R1cmluZ0NoYXJhY3RlcmlzdGljIgoJCX0sCgkJInBhcnRUeXBlSW5mb3JtYXRpb24iOiB7CgkJCSIkcmVmIjogIiMvY29tcG9uZW50cy9zY2hlbWFzL3Vybl9iYW1tX2lvLmNhdGVuYXguc2VyaWFsX3BhcnRfdHlwaXphdGlvbl8xLjEuMV9QYXJ0VHlwZUluZm9ybWF0aW9uQ2hhcmFjdGVyaXN0aWMiCgkJfQoJfSwKCSJyZXF1aXJlZCI6IFsKCQkiY2F0ZW5hWElkIiwKCQkibG9jYWxJZGVudGlmaWVycyIsCgkJIm1hbnVmYWN0dXJpbmdJbmZvcm1hdGlvbiIsCgkJInBhcnRUeXBlSW5mb3JtYXRpb24iCgldCn0=
    bpdm:
      url: https://<bpdm-url>
      bpnEndpoint: "{{ tpl .Values.bpdm.url . }}/api/catena/legal-entities/{partnerId}?idType={idType}"
    minioUser: <minio-username>
    minioPassword: <minio-password>
    minioUrl: "http://{{ .Release.Name }}-minio:9000"
    keycloak:
      oauth2:
        clientId: <keycloak-client-id>
        clientSecret: <keycloak-client-secret>
        clientTokenUri: <keycloak-token-uri>
        jwkSetUri: <keycloak-jwkset-uri>
    edc:
      controlplane:
        endpoint:
          data: "" #<edc-controlplane-endpoint-data>
        request:
          ttl: PT10M # Requests to controlplane will time out after this duration (see https://en.wikipedia.org/wiki/ISO_8601#Durations)
        provider:
          suffix: /api/v1/ids/data
        catalog:
          limit: 1000 # Max number of catalog items to retrieve from the controlplane
          pagesize: 50 # Number of catalog items to retrieve on one page for pagination
        apikey:
          header: "X-Api-Key" # Name of the EDC api key header field
          secret: "" #<edc-api-key>
      submodel:
        request:
          ttl: PT10M # Requests to dataplane will time out after this duration (see https://en.wikipedia.org/wiki/ISO_8601#Durations)
        path: /submodel
        urnprefix: /urn

    config:
      # If true, the config provided below will completely replace the configmap.
      # In this case, you need to provide all required config values defined above yourself!
      # If false, the custom config will just be appended to the configmap.
      override: false
      # Provide your custom configuration here (overrides IRS Spring application.yaml)
      content:


    env: [] # You can provide your own environment variables for the IRS here.
    #  - name: JAVA_TOOL_OPTIONS
    #  - value: -Dhttps.proxyHost=1.2.3.4

    #######################
    # Minio Configuration #
    #######################
    minio:
      enabled: true
      mode: standalone
      persistence:
        size: 1Gi
      resources:
        requests:
          memory: 4Gi
      rootUser: <minio-username>
      rootPassword: <minio-password>

      environment:
        MINIO_PROMETHEUS_JOB_ID: minio-actuator
        MINIO_PROMETHEUS_URL: http://prometheus:9090

    #########################
    # Grafana Configuration #
    #########################
    grafana:
      enabled: false (1)
      rbac:
        create: false
      persistence:
        enabled: false

      user: <grafana-username>
      password: <grafana-password>

      admin:
        existingSecret: "{{ .Release.Name }}-irs-helm"
        userKey: grafanaUser
        passwordKey: grafanaPassword

      datasources:
        datasources.yaml:
          apiVersion: 1
          datasources:
            - name: Prometheus
              type: prometheus
              url: "http://{{ .Release.Name }}-prometheus-server"
              isDefault: true
      sidecar:
        dashboards:
          enabled: true

      importDashboards:
        minio: dashboards/minio-dashboard.json
        outbound: dashboards/irs-outbound-requests.json
        irsmonitoring: dashboards/resource-monitoring-dashboard.json
        irsjobs: dashboards/irs-jobs-dashboard.json
        irsapi: dashboards/irs-api-dashboard.json

    ############################
    # Prometheus Configuration #
    ############################
    prometheus:
      enabled: false (1)
      rbac:
        create: false
      alertmanager:
        enabled: false
      prometheus-node-exporter:
        enabled: false
      kubeStateMetrics:
        enabled: false
      prometheus-pushgateway:
        enabled: false
      configmapReload:
        prometheus:
          enabled: false

      extraScrapeConfigs: |
        - job_name: 'spring-actuator'
          metrics_path: '/actuator/prometheus'
          scrape_interval: 5s
          static_configs:
            - targets: [ '{{ .Release.Name }}-irs-helm:4004' ]

        - job_name: 'minio-actuator'
          metrics_path: /minio/v2/metrics/cluster
          static_configs:
            - targets: [ '{{ .Release.Name }}-minio:9000' ]
```

| **_(1)_** | Use this to enable or disable the monitoring components |
|-----------|---------------------------------------------------------|

#### Values explained

##### &lt;irs-url&gt;

The hostname where the IRS will be made available.

##### &lt;digital-twin-registry-url&gt;

The URL of the Digital Twin Registry. The IRS uses this service to fetch AAS shells.

##### &lt;semantics-hub-url&gt;

The URL of the SemanticsHub. The IRS uses this service to fetch aspect schemas for payload validation.

##### &lt;bpdm-url&gt;

The URL of the BPDM service. The IRS uses this service to fetch business partner information based on BPNs.

##### &lt;keycloak-token-uri&gt;

The URL of the Keycloak token API. Used by the IRS for token creation to authenticate with other services.

##### &lt;keycloak-jwkset-uri&gt;

The URL of the Keycloak JWK Set. Used by the IRS to validate tokens when the IRS API is called.

##### &lt;grafana-url&gt;

The hostname where Grafana will be made available.

##### &lt;edc-controlplane-endpoint-data&gt;

The EDC consumer controlplane endpoint URL for data management, including the protocol.
If left empty, this defaults to the internal endpoint of the controlplane provided by the irs-edc-consumer Helm chart.

#### Semantic Model Provisioning

The IRS can retrieve semantic models in two ways:

1. via the Semantic Hub, if you provide the URL

2. via local schema files

If you activate both features, IRS will first try to resolve the models via the Hub and use the
local models as a fallback.

If you want to use local schema files, you need to provide them directly in the `values.yaml` file. Use the
param `semanticsHub.localModels` to specify a map of all the local schemas.
The **key** of each entry is the `Base64` encoded URN of the model. The **value** is the `Base64` encoded content of the
schema file itself. The entries will then be mounted into the IRS container and used on demand. For reference, see the
example comment in the default `values.yaml`.

### EDC consumer configuration

If you want to provide your own EDC consumer, add the following entries to your values.yaml:

```(yaml)
    ##############################
    # EDC Postgres Configuration #
    ##############################
    postgresql:
      auth:
        username: edc
        database: edc
        postgresPassword: <postgres-admin-password>
        password: <postgres-password>

    ##################################
    # EDC Controlplane Configuration #
    ##################################
    edc-controlplane:
      ingresses:
        - enabled: true
          hostname: "<controlplane-url>"
          annotations:
            nginx.ingress.kubernetes.io/ssl-passthrough: "false"
            nginx.ingress.kubernetes.io/backend-protocol: "HTTP"
            nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
          endpoints:
            - ids
            - data
          className: ""
          tls:
            - hosts:
                - "<controlplane-url>"
              secretName: tls-secret
          certManager:
            issuer: ""
            clusterIssuer: ""

      edc:
        receiver:
          callback:
            url: "http://{{ .Release.Name }}-irs-helm:8181/internal/endpoint-data-reference" # IRS EDC callback URL, e.g. http://app-irs-helm:8181/internal/endpoint-data-reference
        postgresql:
          user: edc
          password: <postgres-password>
        transfer:
          proxy:
            token:
              verifier:
                publickey:
                  alias: <daps-certificate-name>
              signer:
                privatekey:
                  alias: <daps-privatekey-name>
        api:
          auth:
            key: "<edc-api-key>"
        controlplane:
          url: "https://<controlplane-url>"
        dataplane:
          url: "https://<dataplane-url>"
      configuration:
        properties: |-
          edc.oauth.client.id=<daps-client-id>
          edc.oauth.private.key.alias=<daps-privatekey-name>
          edc.oauth.provider.jwks.url=<daps-jwks-url>
          edc.oauth.public.key.alias=<daps-certificate-name>
          edc.oauth.token.url=<daps-token-url>
          edc.vault.hashicorp.url=<vault-url>
          edc.vault.hashicorp.token=<vault-token>
          edc.vault.hashicorp.api.secret.path=<vault-secret-store-path>
          edc.data.encryption.keys.alias=<daps-privatekey-name>
          edc.data.encryption.algorithm=NONE

    ###############################
    # EDC Dataplane Configuration #
    ###############################
    edc-dataplane:
      edc:
        api:
          auth:
            key: "<edc-api-key>"
      ## Ingress declaration to expose the network service.
      ingresses:
        - enabled: true
          hostname: "<dataplane-url>"
          annotations:
            nginx.ingress.kubernetes.io/ssl-passthrough: "false"
            nginx.ingress.kubernetes.io/backend-protocol: "HTTP"
            nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
          endpoints:
            - public
          className: "nginx"
          tls:
            - hosts:
                - "<dataplane-url>"
              secretName: tls-secret
          certManager:
            issuer: ""
            clusterIssuer: ""

      configuration:
        properties: |-
          edc.oauth.client.id=<daps-client-id>
          edc.oauth.private.key.alias=<daps-privatekey-name>
          edc.oauth.provider.audience=idsc:IDS_CONNECTORS_ALL
          edc.oauth.provider.jwks.url=<daps-jwks-url>
          edc.oauth.public.key.alias=<daps-certificate-name>
          edc.oauth.token.url=<daps-token-url>
          edc.vault.hashicorp.url=<vault-url>
          edc.vault.hashicorp.token=<vault-token>
          edc.vault.hashicorp.api.secret.path=<vault-secret-store-path>
```

#### Values explained

EDC requires a DAPS instance to function correctly. For more information on this, please refer to
the [DAPS](https://github.com/catenax-ng/product-DAPS) or the [EDC](https://github.com/catenax-ng/product-edc)
documentation.

##### &lt;controlplane-url&gt;

The hostname where the EDC consumer controlplane will be made available.

##### &lt;dataplane-url&gt;

The hostname where the EDC consumer dataplane will be made available.

##### &lt;vault-url&gt;

The base URL of the Vault instance.
EDC requires a running instance of HashiCorp Vault to store the DAPS certificate and private key.

##### &lt;vault-secret-store-path&gt;

The path to the secret store in Vault where the DAPS certificate and key can be found.  
_Example: /v1/team-name_

##### &lt;daps-certificate-name&gt;

The name of the DAPS certificate in the Vault.  
_Example: irs-daps-certificate_

##### &lt;daps-privatekey-name&gt;

The name of the DAPS private key in the Vault.  
_Example: irs-daps-private-key_

##### &lt;daps-client-id&gt;

The DAPS client ID.

##### &lt;daps-jwks-url&gt;

The URL of the DAPS JWK Set.  
_Example: [https://daps-hostname/.well-known/jwks.json](https://daps-hostname/.well-known/jwks.json)_

##### &lt;daps-token-url&gt;

The URL of the DAPS token API.  
_Example: [https://daps-hostname/token](https://daps-hostname/token)_

### Secrets

This is a list of all secrets used in the deployment.  
**_Keep the values for these settings safe and do not publish them!_**

#### &lt;postgres-admin-password&gt;

Database password for the **postgres** user. To be defined by you.

#### &lt;postgres-password&gt;

Database password for the application user (default username: **edc**). To be defined by you.

#### &lt;keycloak-client-id&gt;

Client ID for Keycloak. Request this from your Keycloak operator.

#### &lt;keycloak-client-secret&gt;

Client secret for Keycloak. Request this from your Keycloak operator.

#### &lt;minio-username&gt;

Login username for Minio. To be defined by you.

#### &lt;minio-password&gt;

Login password for Minio. To be defined by you.

#### &lt;edc-api-key&gt;

An API key for the EDC API. To be defined by you.

#### &lt;vault-token&gt;

The access token for the HashiCorp Vault API.

#### &lt;grafana-username&gt;

Login username for Grafana. To be defined by you.

#### &lt;grafana-password&gt;

Login password for Grafana. To be defined by you.

