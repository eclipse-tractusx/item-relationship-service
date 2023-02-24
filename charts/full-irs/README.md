# Item Relationship Service

## Prerequisites

Secret should be created:

``` yaml

apiVersion: v1
kind: Secret
metadata:
  name: "digital-twin-registry-docker"
  namespace: irs
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: <Secret>

```

## HELM

Initiate the helm repositories

``` bash
helm repo list

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add vault https://helm.releases.hashicorp.com
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add prometheus https://prometheus-community.github.io/helm-charts
helm repo add minio https://charts.min.io/
helm repo add cx-backend-service https://denisneuling.github.io/cx-backend-service
helm repo add codecentric https://codecentric.github.io/helm-charts

```

Build and update helm charts

``` bash
helm dependency build
helm dependency update
```

Install the helm chart

``` bash
helm install irs --namespace irs --create-namespace .
```

Uninstall the helm chart

``` bash
helm uninstall irs --namespace irs
```

## Kubernetes

Change default namespace

``` bash
kubectl config set-context minikube --namespace=irs
```

## EDC

### VAULT

``` bash
kubectl port-forward svc/edc-vault 8200:8200
```

### Daps

``` bash
kubectl port-forward svc/edc-daps 4567:4567
```

Register new client

``` bash
register_connector.sh edc
```

Get client token

``` bash
ruby create_test_token.rb edc ./keys/edc.key
```

### EDC Consumer

#### EDC Consumer Database

``` bash
kubectl port-forward svc/edc-consumer-database 5432:5432

export PGPASSWORD=edc-consumer-pass; psql -h localhost -p 5432 -d edc-consumer -U edc-consumer-user

psql \l

```

#### EDC Consumer Control Plane

``` bash
kubectl port-forward svc/edc-consumer-control-plane 7181:8181 7080:8080
```

#### EDC Consumer Data Plane

### EDC Provider

#### EDC Provider Database

``` bash
kubectl port-forward svc/edc-provider-database 5432:5432

export PGPASSWORD=edc-provider-pass; psql -h localhost -p 5432 -d edc-provider -U edc-provider-user

psql \l

```

#### EDC Provider Control Plane

``` bash
kubectl port-forward svc/edc-provider-control-plane 8181:8181 8080:8080
```

#### EDC Provider Data Plane

## IRS Dependencies

### Semantic Hub

``` bash
kubectl port-forward svc/semantic-hub 8088:8080
```

### Digital Twins Registry Database

``` bash

kubectl port-forward svc/digital-twin-registry-database 5432:5432

export PGPASSWORD=digital-twin-registry-pass; psql -h localhost -p 5432 -d digital-twin-registry -U digital-twin-registry-user

select * from shell where id_short = 'VehicleCombustion';
psql \l
```

### Digital Twins Registry

``` bash

kubectl port-forward svc/digital-twin-registry 8080:8080
```

### KeyCloak

``` bash

kubectl port-forward svc/keycloak 4011:8080
```

### IRS Provider Backend

``` bash
kubectl port-forward svc/irs-provider-backend 8080:8080
```


## IRS Service

### Grafana

``` bash
kubectl port-forward svc/irs-grafana 4000:80
```

### Prometheus

``` bash
kubectl port-forward svc/irs-prometheus-server 9090:80
```

### Minio

``` bash
kubectl port-forward svc/irs-minio 9000:9000
kubectl port-forward svc/irs-minio-console 9001:9001
```

### IRS Backend Service

``` bash
kubectl port-forward svc/irs 8080:8080 8181:8181 4004:4004
```

### IRS Frontend Service

``` bash

kubectl port-forward svc/irs-frontend 3000:8080
```

## Usage

### Deploying the services

To deploy the services on kubernetes, you should run the ``` ./start.sh ```.

The script takes 3 parameters as input:

* CLEAN_UP_ENVIRONMENT: default is set to false. If this is passed as true, will delete the minikube and recreate it with 2 CPU and 8GB or ram.
* INSTALL_EDC: default is set to true. If this is passed as true, will delete all helm charts related to EDC (vault, DAPS, EDC consumer and EDC provider) and install them again.
* INSTALL_IRS: default is set to true. If this is passed as true, will delete all helm charts related to IRS (dependencies, IRS backend and IRS frontend) and install them again.

To forward the ports, the script: ```forwardingPorts.sh``` should be run.

### Run test data

To create test data, the script: ```upload-testdata.sh``` should be run only after the ports were forwarded.
To clean-up test data, the script: ```deleteIRSTestData.sh``` should be run only after the ports were forwarded.

#### Generate Key Cloak token

Precondition:

* Visual Studio extension: REST Client by Huachao Mao
* Key Cloak service port should run at port 4011

Using the ./test/keycloack-service.rest, you can execute the token request to get a new token.

#### Generate DAPS token

Precondition:

* Visual studio extension: REST Client by Huachao Mao
* Daps service port should run at port 4567
* Token used as client assertion should be created with script: ./daps/create_test_token.rb
  * example: 

``` bash
  ruby create_test_token.rb edc ./keys/edc.key
```

where edc is a client 

Using the ./test/omejdn-service.rest.rest, you can execute the token request to get a new token.

### Next steps

1. Helm Security issues
2. Use the official digital twin chart
3. Deploy on a cloud provider
4. Vault: store certificates in secrets
5. Add liveness and readiness probe for irs provider backend service
6. Use the DAPS chart form: https://catenax-ng.github.io/product-DAPS


### Additional

1. Grafana Dashboards for EDC control plane and data plane
2. Grafana Dashboards for database
3. Helm documentation
4. Version of kubernetes and updating to a new version ?

### Decisions

1. No BPN mockup
2. Use a custom helm chart for semantic hub.
   1. Reason: test data provided
   2. Next steps:  
      1. Use the official helm chart from: https://eclipse-tractusx.github.io/sldt-semantic-hub
      2. Include scripts to provide test data into the script ``` upload-testdata.sh ```
3. Use a custom helm chart for daps.
   1. Reason: already configured with a default client
   2. Next steps:  
      1. Use the official helm chart from: https://catenax-ng.github.io/product-DAPS
      2. Provide configuration with default client from start.
4. Use a custom helm chart for digital twin
   1. Reason: A secret for pulling docker images is missing to use the default chart from: https://eclipse-tractusx.github.io/sldt-digital-twin-registry
