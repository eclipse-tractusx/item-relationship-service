{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "discovery.chart" -}}
    {{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "discovery.labels" -}}
    service: {{ .Chart.Name }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "discovery.selectorLabels" -}}
    service: {{ .Chart.Name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "discovery.serviceAccountName" -}}
    {{- if .Values.serviceAccount.create -}}
        {{- default .Chart.Name .Values.serviceAccount.name -}}
    {{- else -}}
        {{- default "default" .Values.serviceAccount.name -}}
    {{- end -}}
{{- end -}}