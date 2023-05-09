#!/bin/bash

BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Port forwarding${NC}"
kubectl port-forward svc/edc-vault 8200:8200 -n irs &
kubectl port-forward svc/edc-daps 4567:4567 -n irs &
kubectl port-forward svc/edc-provider-control-plane 6080:8080 6181:8181 -n irs &
kubectl port-forward svc/edc-consumer-control-plane 7080:8080 7181:8181 -n irs &
kubectl port-forward svc/semantic-hub 8088:8080 -n irs &
kubectl port-forward svc/discovery 8888:8080 -n irs &
kubectl port-forward svc/cx-irs-dependencies-registry-svc 10200:8080 -n irs &
kubectl port-forward svc/keycloak 4011:8080 -n irs &
kubectl port-forward svc/irs-provider-backend 10199:8080 -n irs &
kubectl port-forward svc/irs-minio-console 9001:9001 -n irs &
kubectl port-forward svc/irs 8080:8080 -n irs &
kubectl port-forward svc/irs-frontend 3000:8080 -n irs