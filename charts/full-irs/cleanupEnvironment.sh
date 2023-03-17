#!/usr/bin/bash

BLUE='\033[0;34m'
NC='\033[0m' # No Color

DELETE_MINIKUBE_CONTAINER=true

if [ -z "$1" ]
  then
    echo "No argument supplied"
fi

if [[ $# -eq 0 ]] ; then
    echo -e "${BLUE}Using default to not delete the minikube${NC}"
else
    DELETE_MINIKUBE_CONTAINER=$1
fi

echo -e "${BLUE}Deleteing minikube container: ${DELETE_MINIKUBE_CONTAINER} ${NC}"

MINIKUBE_CONTAINER=$(docker ps -a -q --filter=name=minikube)

if [ "$MINIKUBE_CONTAINER" != "" ];
then
    echo -e "${BLUE}Found minikube"
    # helm uninstall $IRS_HELM --namespace irs

    if $DELETE_MINIKUBE_CONTAINER
    then
        echo -e "${BLUE}Stoping minikube${NC}"
        minikube stop 

        echo -e "${BLUE}Deleting minikube${NC}"
        minikube delete; 

        echo -e "${BLUE}Starting minikube${NC}"
        minikube start --memory 8192 --cpus 2;

        minikube addons enable metrics-server

        echo -e "${BLUE}Change kubernetes namespace to irs ${NC}"
        kubectl config set-context rancher-desktop --namespace=irs

    fi

else
    echo -e "${BLUE}Starting minikube${NC}"
    minikube start --memory 8192 --cpus 2;

    minikube addons enable metrics-server

    echo -e "${BLUE}Change kubernetes namespace to irs ${NC}"
    kubectl config set-context rancher-desktop --namespace=irs
fi