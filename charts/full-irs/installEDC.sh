#!/bin/sh

BLUE='\033[0;34m'
NC='\033[0m' # No Color

INSTALL_EDC_VAULT=true
INSTALL_EDC_DAPS=true
INSTALL_EDC_CONSUMER=true
INSTALL_EDC_PROVIDER=true

######### EDC Vault #########
if $INSTALL_EDC_VAULT
then
    HELM_CHART_NAME=edc-vault
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME --set install.edc.vault=true --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s edc-vault-agent-injector
fi

######### EDC DAPS #########
if $INSTALL_EDC_DAPS
then
    HELM_CHART_NAME=edc-daps
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME --set install.edc.daps=true --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s edc-daps
fi

######### EDC Consumer #########
if $INSTALL_EDC_CONSUMER
then
    HELM_CHART_NAME=edc-consumer
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME --set install.edc.consumer=true --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s edc-consumer-control-plane
    kubectl wait deployment -n irs --for condition=Available --timeout=90s edc-consumer-data-plane
fi

######### EDC Provider #########
if $INSTALL_EDC_PROVIDER
then

    HELM_CHART_NAME=edc-provider
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME --set install.edc.provider=true --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s edc-provider-control-plane
    kubectl wait deployment -n irs --for condition=Available --timeout=90s edc-provider-data-plane
fi