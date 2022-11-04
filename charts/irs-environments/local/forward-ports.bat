@echo off
kubectl port-forward deployment/irs-local-irs-helm 10165:8080 -n product-traceability-irs | ^
kubectl port-forward deployment/cx-irs-local-registry 10196:4243 -n product-traceability-irs | ^
kubectl port-forward deployment/irs-local-edc-controlplane-provider 10197:8181 -n product-traceability-irs | ^
kubectl port-forward deployment/irs-local-edc-controlplane 10198:8181 -n product-traceability-irs | ^
kubectl port-forward deployment/irs-local-submodelservers 10199:8080 -n product-traceability-irs
