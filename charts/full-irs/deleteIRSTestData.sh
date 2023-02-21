#!/usr/bin/sh
##################################################################################
echo "Deleting testdata"
py.exe ../../testdata-transform/reset-env.py \
  -a http://localhost:10200 \
  -edc http://localhost:7181 \
  -k password
