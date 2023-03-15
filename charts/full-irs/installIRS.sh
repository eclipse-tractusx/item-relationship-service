#!/bin/bash

BLUE='\033[0;34m'
NC='\033[0m' # No Color

INSTALL_IRS_BACKEND_DEPENDENCIES=true
INSTALL_IRS_BACKEND=true
INSTALL_IRS_FRONTEND=true

if $INSTALL_IRS_BACKEND_DEPENDENCIES
then
    HELM_CHART_NAME=irs-dependencies
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME \
        --set install.semanticHub=true \
        --set install.digitalTwin=true \
        --set install.keycloak=true \
        --set install.irs.providerBackend=true \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s semantic-hub
    kubectl wait deployment -n irs --for condition=Available --timeout=90s digital-twin-registry
    kubectl wait deployment -n irs --for condition=Available --timeout=90s keycloak
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-provider-backend

fi

if $INSTALL_IRS_BACKEND
then
    HELM_CHART_NAME=irs
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"

    helm install $HELM_CHART_NAME \
        --set irs.enabled=true \
        --set irs.minio.enabled=true \
        --set irs.grafana.enabled=false \
        --set irs.prometheus.enabled=false \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-minio
    # kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-prometheus-server
    # kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-grafana

fi

if $INSTALL_IRS_FRONTEND
then
    HELM_CHART_NAME=irs-frontend
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME \
        --set install.irs.frontend=true \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-frontend
    
fi
