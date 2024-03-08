[[Back to main README](../../README.md)]

# Item Relationship Service Helm Chart

This chart installs the Item Relationship Service and its direct dependencies.
This includes:

 - [Minio Object Storage](https://min.io) (enabled by default)
 - [Prometheus](https://prometheus.io) (disabled by default) 
 - [Grafana](https://grafana.com/grafana) (disabled by default) 

## Prerequisites

- [Kubernetes](https://kubernetes.io) 1.25.11+
- [Helm](https://helm.sh) 3.9.3+
- PV provisioner support in the underlying infrastructure

## Install

```
helm repo add irs https://eclipse-tractusx.github.io/item-relationship-service
helm install irs-app irs/irs-helm
```

## Default configuration values

See [the configuration docs](https://eclipse-tractusx.github.io/item-relationship-service/docs/administration/administration-guide.html#_helm_configuration_irs_values_yaml) or run `helm show values irs/irs-helm` for the default values.