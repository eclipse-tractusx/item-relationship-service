# IRS

## Prerequisites

Secret should be created:

``` yaml

apiVersion: v1
kind: Secret
metadata:
  name: "irs-digital-twin-registry-docker"
  namespace: {{ .Release.Namespace | default "default" | quote }}
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

Package a helm chart

``` bash
helm package ./subcharts/edc-control-plane -d ./charts
helm package ./subcharts/edc-data-plane -d ./charts

```

## kubernetes

Change default namespace

``` bash
kubectl config set-context minikube --namespace=irs
```

## VAULT

``` bash
kubectl port-forward svc/edc-vault 8200:8200
```

## Daps

``` bash
kubectl port-forward svc/edc-daps-service 4567:4567
```

Register new client

``` bash
register_connector.sh edc
```

Get client token

``` bash
ruby create_test_token.rb edc ./keys/edc.key
```

## EDC Consumer

### EDC Consumer Database

``` bash
kubectl port-forward svc/edc-consumer-database 5432:5432

kubectl get secret edc-consumer-database -n irs -o jsonpath="{.data.postgres-password}" | base64 -d

kubectl get secret edc-consumer-database -n irs -o jsonpath="{.data.password}" | base64 -d

export PGPASSWORD=edc-consumer-pass; psql -h localhost -p 5432 -d edc-consumer -U edc-consumer-user

psql \l
psql \c edc
psql \d

```

### EDC Consumer Control Plane

``` bash
kubectl port-forward svc/edc-consumer-control-plane 8181:8181 8080:8080
```

### EDC Consumer Data Plane

## EDC Provider

### EDC Provider Database

``` bash
kubectl port-forward svc/edc-provider-database 5432:5432

kubectl get secret edc-provider-database -n irs -o jsonpath="{.data.postgres-password}" | base64 -d

kubectl get secret edc-provider-database -n irs -o jsonpath="{.data.password}" | base64 -d

export PGPASSWORD=edc-provider-pass; psql -h localhost -p 5432 -d edc-provider -U edc-provider-user

psql \l
psql \c edc
psql \d

```

### EDC Provider Control Plane

``` bash
kubectl port-forward svc/edc-provider-control-plane 8181:8181 8080:8080
```

### EDC Provider Data Plane

## Grafana

``` bash
kubectl port-forward svc/irs-grafana 4000:80
```

## Prometheus

``` bash
kubectl port-forward svc/irs-infra-prometheus-server 9090:80
```

## Minio

``` bash
kubectl port-forward svc/irs-minio 9000:9000
kubectl port-forward svc/irs-minio-console 9001:9001
```

## IRS Backend Service

``` bash
kubectl port-forward svc/irs-backend-service 8080:8080 8181:8181 4004:4004
```

## IRS Provider Backend Service

``` bash
kubectl port-forward svc/irs-provider-backend-service 8080:8080
```

## IRS Digital Twins Registry Database

``` bash

kubectl port-forward svc/irs-digital-twin-registry-database 5432:5432

kubectl get secret irs-digital-twin-registry-database -n irs -o jsonpath="{.data.postgres-password}" | base64 -d

kubectl get secret irs-digital-twin-registry-database -n irs -o jsonpath="{.data.password}" | base64 -d

export PGPASSWORD=irs-digital-twin-registry-pass; psql -h localhost -p 5432 -d irs-digital-twin-registry -U irs-digital-twin-registry-user

select * from shell where id_short = 'VehicleCombustion';
psql \l
psql \c edc
psql \d
```

## IRS Digital Twins Registry Service

``` bash

kubectl port-forward svc/irs-digital-twin-registry 8080:8080
```

## IRS Frontend Service

``` bash

kubectl port-forward svc/irs-frontend-service 3000:8080
```

## IRS Key Cloak

``` bash

kubectl port-forward svc/irs-keycloak-service 4011:8080
```

## IRS Key Cloak Database

``` bash

kubectl port-forward svc/irs-keycloak-database 5432:5432


export PGPASSWORD=irs-keycloak-pass; psql -h localhost -p 5432 -d irs-keycloak -U irs-keycloak-user

psql \l
psql \c edc
psql \d
```

## IRS Key Cloak Service

``` bash
kubectl port-forward svc/irs-keycloak-service 4011:8080
```

## IRS Semantic Hub Service

``` bash
kubectl port-forward svc/irs-semantic-hub-service 8080:8080
```

### Next steps

1. Use the existing test data scripts
2. Use the existing irs helm chart from the repo
3. Helm Security issues
4. Move irs frontend helm chart to frontend github url
5. Deploy on a cloud provider
6. Vault: store certificates in secrets
7. Add liveness and readiness probe for irs provider backend service

### Additional

1. Grafana Dashboards for EDC control plane and data plane
2. Grafana Dashboards for database
3. Helm documentation
4. Version of kubernetes and updating to a new version ?

### Decisions

1. No BPN mockup
2. 
