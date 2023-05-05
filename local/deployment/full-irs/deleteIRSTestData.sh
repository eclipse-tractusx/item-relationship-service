#!/usr/bin/bash
##################################################################################
echo "Deleting testdata"
python ../../testing/testdata/reset-env.py \
  -a http://localhost:10200 \
  -edc http://localhost:7181 \
  -k password
