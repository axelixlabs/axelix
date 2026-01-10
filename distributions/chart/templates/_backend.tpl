{{/*
The name of the Axelix Master backend deployment.
*/}}
{{- define "master.backend.name" -}}
{{- default $.Chart.Name $.Values.backend.name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels for Axelix Master baceknd
*/}}
{{- define "master.backend.labels" -}}
helm.sh/hook-time: {{ now | date "20060102-150405" | quote }}
helm.sh/chart: {{ include "master.chart" . }}
app.kubernetes.io/version: {{ $.Chart.AppVersion | quote }}
{{ include "master.backend.selectorLabels" $ }}
{{- end }}

{{/*
Backend pods selector labels
*/}}
{{- define "master.backend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "master.backend.name" $ }}
app.kubernetes.io/instance: {{ $.Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "master.backend.serviceAccountName" -}}
{{- if $.Values.backend.serviceAccount.create }}
{{- default (include "master.backend.name" $) $.Values.backend.serviceAccount.name }}
{{- else }}
{{- default "default" $.Values.backend.serviceAccount.name }}
{{- end }}
{{- end }}

