#!/bin/bash

helm uninstall irs-frontend -n irs
helm uninstall irs-dependencies -n irs
helm uninstall irs -n irs
helm uninstall edc-consumer -n irs
helm uninstall edc-provider -n irs
helm uninstall edc-daps -n irs
helm uninstall edc-vault -n irs

helm list