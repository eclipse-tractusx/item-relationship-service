#!/usr/bin/sh

BLUE='\033[0;34m'
NC='\033[0m' # No Color

START_IRS_BACKEND_DEPENDENCIES=true
START_IRS_BACKEND=true
START_IRS_FRONTEND=true

if $START_IRS_BACKEND_DEPENDENCIES
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
        --set install.irs.digitalTwin=true \
        --set install.irs.providerBackend=true \
        --set install.irs.semanticHub=true \
        --set install.irs.keycloak=true \
        --set install.edc.daps=false \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-digital-twin-registry
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-provider-backend-service

fi

if $START_IRS_BACKEND
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
        --set irs.prometheus.enabled=false \
        --set irs.grafana.enabled=false \
        --namespace irs --create-namespace .

    echo -e "${BLUE}Waiting for the deployments to be available${NC}"
    # kubectl wait deployment -n irs --for condition=Available --timeout=90s irs
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-minio
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-prometheus-server
    kubectl wait deployment -n irs --for condition=Available --timeout=90s irs-grafana

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
