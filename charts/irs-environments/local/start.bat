@echo off
echo Installing IRS-local Helm Charts.
helm install irs-local charts/irs-environments/local/ -n product-traceability-irs --create-namespace

echo Waiting for the deployments to be available...
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-submodelservers
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-minio
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-controlplane
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-controlplane-provider
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-dataplane
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-dataplane-provider
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-irs-helm
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s cx-irs-local-registry

echo All deployments available. Forwarding Ports to local machine.
start .\charts\irs-environments\local\forward-ports.bat

echo Uploading testdata...
python testdata-transform/transform-and-upload.py ^
  -f testdata-transform/CX_Testdata_1.3.3-reduced-with-asPlanned.json ^
  -s http://irs-local-submodelservers:8080 ^
  -su http://localhost:10199 ^
  -a http://localhost:10196 ^
  -edc http://irs-local-edc-controlplane-provider ^
  -eu http://localhost:10197 ^
  -k 123456
