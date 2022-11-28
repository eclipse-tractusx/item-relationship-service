#!/usr/bin/sh
echo "Installing IRS-local Helm Charts"
helm install irs-local charts/irs-environments/local/ -n product-traceability-irs --create-namespace

echo "Waiting for the deployments to be available"
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-submodelservers
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-minio
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-controlplane
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-controlplane-provider
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-dataplane
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-dataplane-provider
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-irs-helm
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s cx-irs-local-registry
