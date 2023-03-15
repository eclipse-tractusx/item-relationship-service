#!/bin/bash

BLUE='\033[0;34m'
NC='\033[0m' # No Color

INSTALL_EDC=true
INSTALL_IRS=true

if [[ $# -eq 0 ]] ; then
    echo -e "${BLUE}Using default for starting up helm charts${NC}"
else
    INSTALL_EDC=$1
    INSTALL_IRS=$2
fi
echo -e "${BLUE}INSTALL_EDC: ${INSTALL_EDC}${NC}"
echo -e "${BLUE}INSTALL_IRS: ${INSTALL_IRS}${NC}"

echo -e "${BLUE}Update chart dependency${NC}"
helm dependency update

echo -e "${BLUE}Build chart dependency${NC}"
helm dependency build

if $INSTALL_EDC
then
    sh ./installEDC.sh 
fi

if $INSTALL_IRS
then
    sh ./installIRS.sh
fi

# sh ./forwardingPorts.sh