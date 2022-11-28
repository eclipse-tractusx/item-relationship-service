#!/usr/bin/sh
echo "Uploading testdata"
py.exe testdata-transform/transform-and-upload.py \
  -f testdata-transform/CX_Testdata_1.3.3-reduced-with-asPlanned.json \
  -s http://irs-local-submodelservers:8080 \
  -su http://localhost:10199 \
  -a http://localhost:10196 \
  -edc http://irs-local-edc-controlplane-provider \
  -eu http://localhost:10197 \
  -k 123456
