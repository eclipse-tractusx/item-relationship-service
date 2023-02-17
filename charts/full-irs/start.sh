#!/usr/bin/sh

BLUE='\033[0;34m'
NC='\033[0m' # No Color

CLEAN_UP_ENVIRONMENT=false
START_EDC=true
START_IRS=true

if [[ $# -eq 0 ]] ; then
    echo -e "${BLUE}Using default for starting up helm charts${NC}"
else
    CLEAN_UP_ENVIRONMENT=$1
    START_EDC=$2
    START_IRS=$3
fi
echo -e "${BLUE}CLEAN_UP_ENVIRONMENT: ${CLEAN_UP_ENVIRONMENT}${NC}"
echo -e "${BLUE}START_EDC: ${START_EDC}${NC}"
echo -e "${BLUE}START_IRS: ${START_IRS}${NC}"

if $CLEAN_UP_ENVIRONMENT
then
    sh ./cleanupEnvironment.sh $CLEAN_UP_ENVIRONMENT
fi

echo -e "${BLUE}Update chart dependency${NC}"
helm dependency update

echo -e "${BLUE}Build chart dependency${NC}"
helm dependency build

if $START_EDC
then
    sh ./startEDC.sh 
fi

if $START_IRS
then
    sh ./startIRS.sh
fi

# sh ./startForwardingPorts.sh