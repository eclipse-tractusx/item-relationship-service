#!/bin/bash
##################################################################################
echo "Deleting testdata"

python ../../testing/testdata/reset-env.py \
  -a https://irs-full-registry.dev.demo.catena-x.net \
  -edc https://irs-full-consumer-controlplane.dev.demo.catena-x.net \
  -k password
