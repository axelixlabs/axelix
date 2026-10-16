{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "master.chart.name-and-version" -}}
{{- printf "%s-%s" $.Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels for all Axelix Master K8S resources
*/}}
{{- define "master.labels.common" -}}
helm.sh/hook-time: {{ now | date "20060102-150405" | quote }}
helm.sh/chart: {{ include "master.chart.name-and-version" . }}
app.kubernetes.io/version: {{ $.Chart.AppVersion | quote }}
{{ include "master.selectorLabels" $ }}
{{- end }}

{{/*
Pods selector labels (deployment --> pods & svc --> pods)
*/}}
{{- define "master.selectorLabels" -}}
app.kubernetes.io/name: axelix-master
app.kubernetes.io/instance: {{ $.Release.Name }}
{{- end }}
