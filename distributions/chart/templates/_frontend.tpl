{{/*
The name of the Axelix Master frontend deployment.
*/}}
{{- define "master.frontend.name" -}}
{{- default $.Chart.Name $.Values.frontend.name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels for Axelix Master frontend
*/}}
{{- define "master.frontend.labels" -}}
helm.sh/hook-time: {{ now | date "20060102-150405" | quote }}
helm.sh/chart: {{ include "master.chart" . }}
app.kubernetes.io/version: {{ $.Chart.AppVersion | quote }}
{{ include "master.frontend.selectorLabels" $ }}
{{- end }}

{{/*
Frontend pods selector labels
*/}}
{{- define "master.frontend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "master.frontend.name" . }}
app.kubernetes.io/instance: {{ $.Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use for frontend pods.
*/}}
{{- define "master.frontend.serviceAccountName" -}}
{{- if $.Values.frontend.serviceAccount.create }}
{{- default (include "master.frontend.name" $) $.Values.frontend.serviceAccount.name }}
{{- else }}
{{- default "default" $.Values.frontend.serviceAccount.name }}
{{- end }}
{{- end }}
