#!/usr/bin/sh
##################################################################################
echo "Uploading testdata"
py.exe testdata/transform-and-upload.py \
  -f testdata/CX_Testdata_v1.4.1-AsBuilt-reduced-with-asPlanned.json \
  -s http://localhost:10199 \
  -su http://localhost:10199 \
  -a http://localhost:10200 \
  -edc http://edc-provider-control-plane:8282 \
  -eu http://localhost:6181 \
  -k password
