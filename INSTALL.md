# Installation Instructions

The deployment contains the components required to connect the IRS to an existing Catena-X network. This includes:
asd
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

A detailed instruction on how to configure the IRS and EDC can be found here: [Administration Guide](https://eclipse-tractusx.github.io/item-relationship-service/docs/administration/administration-guide.html)

## Local Installation
IRS provides a local setup which can be deployed to kubernetes.
This setup includes all third-party services which IRS uses and interacts with.

Instructions can be found here [README.md](https://github.com/eclipse-tractusx/item-relationship-service/blob/main/local/deployment/full-irs/README.md).