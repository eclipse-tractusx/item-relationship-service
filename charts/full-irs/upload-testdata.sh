#!/bin/bash
##################################################################################
echo "Uploading testdata"
python3 ../../testdata-transform/transform-and-upload.py \
  -f ../../testdata-transform//CX_Testdata_v1.4.1-AsBuilt-reduced-with-asPlanned.json \
  -s http://irs-provider-backend:8080 \
  -su http://localhost:10199 \
  -a http://localhost:10200 \
  -edc http://edc-provider-control-plane:8282 \
  -eu http://localhost:6181 \
  -k password
