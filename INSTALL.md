# Installation Instructions

The deployment contains the components required to connect the IRS to an existing Catena-X network. This includes:

- IRS with Minio - part of the "item-relationship-service" Helm chart
- EDC Consumer (controlplane & dataplane) - part of the "irs-edc-consumer" Helm chart

Everything else needs to be provided externally.

## Data Chain Kit

You can use the Data Chain Kit to deploy the whole demo scenario with all participating components.  
Instructions can be found here: [Data Chain Kit](https://eclipse-tractusx.github.io/docs-kits/kits/Data%20Chain%20Kit/Operation%20View).

## Installation

The IRS Helm repository can be found here: [index.yaml](https://eclipse-tractusx.github.io/item-relationship-service/index.yaml)

Use the latest release of the "item-relationship-service" Helm chart.
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
    helm install -f your-values.yaml irs-app irs/item-relationship-service
```

Or create a new Helm chart and use the IRS as a dependency.

```(yaml)
    dependencies:
      - name: item-relationship-service
        repository: https://eclipse-tractusx.github.io/item-relationship-service
        version: 7.x.x
      - name: tractusx-connector
        repository: https://eclipse-tractusx.github.io/tractusx-edc
        version: 0.7.x
```

Then provide your configuration as the values.yaml of that chart.

Create a new application in ArgoCD and point it to your repository / Helm chart folder.

## Configuration

A detailed instruction on how to configure the IRS and EDC can be found here: [Administration Guide](https://eclipse-tractusx.github.io/item-relationship-service/docs/administration/administration-guide.html)

## Local Installation

IRS provides a local setup which can be deployed to kubernetes.
This setup includes all third-party services which IRS uses and interacts with.

Instructions can be found here [README](README.md).

## Sample Calls

Sample calls can be found here [USAGE](USAGE.md).

## Local Installation with Umbrella

The eclipse-tractusx/umbrella chart provides a pre-configured Helm Chart with many Tractus-X Services. To integrate IRS
with this Chart, run the following steps:

For detailed instructions on how to set up the umbrella chart, see the chapters "Cluster setup" and "Network setup" in
the umbrella [README.md](https://github.com/eclipse-tractusx/tractus-x-umbrella/blob/main/charts/umbrella/README.md).

Clone the [Umbrella repo](https://github.com/eclipse-tractusx/tractus-x-umbrella) (only required once):

```
git clone https://github.com/eclipse-tractusx/tractus-x-umbrella.git
```

Check out
the [IRS umbrella integration branch](https://github.com/eclipse-tractusx/tractus-x-umbrella/tree/chore/e2e-irs-preparation):

```
cd tractus-x-umbrella/
git fetch origin
git checkout -b chore/e2e-irs-preparation origin/chore/e2e-irs-preparation
```

Build the required images for IATP mock.

```bash
eval $(minikube docker-env)
docker build iatp-mock/ -t tractusx/iatp-mock:testing --platform linux/amd64
```

Install the Umbrella chart

```bash
helm dependency update charts/tx-data-provider
helm dependency update charts/umbrella
helm install umbrella charts/umbrella -f charts/umbrella/values-adopter-irs.yaml -n e2e-testing --create-namespace --set iatpmock.image.repository=tractusx/iatp-mock --set iatpmock.image.tag=testing
```

(Optional) Build IRS Docker image from local

```bash
docker build . -t tractusx/irs-api:local
```

Install the IRS Helm Chart with the local Docker image

```bash
helm dependency update ./charts/item-relationship-service
helm install irs ./charts/item-relationship-service --namespace e2e-testing -f ./charts/item-relationship-service/values-umbrella.yaml --set image.repository=tractusx/irs-api -- set image.tag=local
```

Or use the latest released version

```bash
helm repo add irs https://eclipse-tractusx.github.io/item-relationship-service 
helm install irs irs/item-relationship-service --namespace e2e-testing -f ./charts/item-relationship-service/values-umbrella.yaml --set image.repository=tractusx/irs-api --set image.tag=latest
```

### upload testdata

To upload testdata, first forward the dataprovider pods to your localhost:

```bash
kubectl port-forward svc/umbrella-dataprovider-dtr 4444:8080 --namespace e2e-testing &
kubectl port-forward svc/umbrella-dataprovider-edc-controlplane 8888:8081 --namespace e2e-testing &
kubectl port-forward svc/umbrella-dataprovider-submodelserver 9999:8080 --namespace e2e-testing
```

then use the testdata upload script to seed the dataprovider services with testdata:

```bash
./local/testing/testdata/upload-testdata.sh "TEST2" "BPNL00000003AYRE" "BPNL00000003AZQP" \
  "http://umbrella-dataprovider-submodelserver:8080" "http://localhost:9999" \
  "http://umbrella-dataprovider-dtr:8080/api/v3" "http://localhost:4444/api/v3" \
  "http://umbrella-dataprovider-edc-controlplane:8084" "http://localhost:8888" \
  "http://umbrella-dataprovider-edc-dataplane:8081"
```

Now forward the IRS service port to access the API:

```bash
kubectl port-forward svc/irs-item-relationship-service 8080:8080 --namespace e2e-testing
```

### Uninstall

To uninstall the IRS and Umbrella chart

```bash
helm uninstall irs --namespace e2e-testing
helm uninstall umbrella --namespace e2e-testing
```