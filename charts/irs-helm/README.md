# Item Relationship Service Helm Chart

This chart installs the Item Relationship Service and its direct dependencies.
This includes:

 - Minio Object Storage (enabled by default)
 - Prometheus (disabled by default) 
 - Grafana (disabled by default) 

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- PV provisioner support in the underlying infrastructure

## Install

```
helm repo add irs https://eclipse-tractusx.github.io/item-relationship-service
helm install irs-app irs/irs-helm
```

## Default configuration values

See [the configuration docs](https://eclipse-tractusx.github.io/item-relationship-service/docs/administration/administration-guide.html#_helm_configuration_irs_values_yaml) or run `helm show values irs/irs-helm` for the default values.