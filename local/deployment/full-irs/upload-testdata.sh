#!/bin/bash
##################################################################################
echo "Uploading testdata"
python ../../testing/testdata/transform-and-upload.py \
  -f ../../testing/testdata/CX_Testdata_v1.4.1-AsBuilt-reduced-with-asPlanned.json \
  -s http://irs-provider-backend:8080 \
  -su http://localhost:10199 \
  -a http://localhost:10200 \
  -au http://cx-irs-dependencies-registry-svc:8080 \
  -edc http://edc-provider-control-plane:8282 \
  -eu http://localhost:6181 \
  -k password \
  --aas3
