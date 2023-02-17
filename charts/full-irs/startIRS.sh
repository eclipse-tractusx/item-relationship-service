#!/usr/bin/sh

BLUE='\033[0;34m'
NC='\033[0m' # No Color

START_IRS_INFRA=false
START_IRS_BACKEND_DEPENDENCIES=true
START_IRS_BACKEND=false
START_IRS_FRONTEND=false

if $START_IRS_INFRA
then
    HELM_CHART_NAME=irs-infra
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME \
        --set install.grafana=true \
        --set install.prometheus=true \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-grafana
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-infra-prometheus-server
fi

if $START_IRS_BACKEND_DEPENDENCIES
then
    HELM_CHART_NAME=irs-backend-dependencies
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME \
        --set install.minio=true \
        --set install.irs.digitalTwin=true \
        --set install.irs.providerBackend=true \
        --set install.irs.semanticHub=true \
        --set install.irs.keycloak=true \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-minio
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-digital-twin-registry
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-provider-backend-service

fi

if $START_IRS_BACKEND
then
    HELM_CHART_NAME=irs-backend
    HELM_CHART=$(helm list -q -f "$HELM_CHART_NAME")

    if [ "$HELM_CHART" != "" ];
    then
        echo -e "${BLUE}Un-installing helm chart: $HELM_CHART_NAME ${NC}"
        helm uninstall $HELM_CHART_NAME --namespace irs
    fi

    echo -e "${BLUE}Installing helm chart: $HELM_CHART_NAME ${NC}"
    helm install $HELM_CHART_NAME \
        --set install.irs.backend=true \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-backend-service

fi

if $START_IRS_FRONTEND
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
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-frontend-service
    
fi
