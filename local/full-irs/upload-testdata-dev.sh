#!/bin/bash
##################################################################################
echo "Uploading testdata"
python ../../testdata-transform/transform-and-upload.py \
  -f ../../testdata-transform//CX_Testdata_v1.4.1-AsBuilt-reduced-with-asPlanned.json \
  -s https://irs-full-submodel-server.dev.demo.catena-x.net \
  -a https://irs-full-registry.dev.demo.catena-x.net \
  -edc http://edc-provider-control-plane:8282 \
  -eu https://irs-full-provider-controlplane.dev.demo.catena-x.net \
  -k password
