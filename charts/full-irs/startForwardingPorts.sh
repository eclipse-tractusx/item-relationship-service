#!/usr/bin/sh

BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Port forwarding${NC}"
kubectl port-forward svc/edc-vault 8200:8200 -n irs &
kubectl port-forward svc/edc-daps-service 4567:4567 -n irs &
kubectl port-forward svc/edc-provider-control-plane 6080:8080 6181:8181 -n irs &
kubectl port-forward svc/edc-consumer-control-plane 7080:8080 7181:8181 -n irs &
kubectl port-forward svc/irs-minio-console 9001:9001 -n irs &
kubectl port-forward svc/irs-grafana 4000:80 -n irs &
kubectl port-forward svc/irs-prometheus-server 9090:80 -n irs &
kubectl port-forward svc/irs-provider-backend-service 10199:8080 -n irs &
kubectl port-forward svc/irs-digital-twin-registry 10200:8080 -n irs &
kubectl port-forward svc/irs 8080:8080 -n irs &
kubectl port-forward svc/irs-frontend-service 3000:8080 -n irs &
kubectl port-forward svc/irs-keycloak-service 4011:8080 -n irs