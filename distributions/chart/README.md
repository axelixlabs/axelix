# Axelix Helm Chart

* Installs [Axelix](https://github.com/axelixlabs/axelix) to Kubernetes, deploying both backend and frontend components.

## Get Repo Info

```console
helm repo add axelix <TODO:> 
helm repo update
```

_See [helm repo](https://helm.sh/docs/helm/helm_repo/) for command documentation._

## Installing the Chart

To install the chart with the release name `my-release`:

```console
helm install my-release axelixlabs/axelix
```

The command deploys Axelix Master on the Kubernetes cluster with the default configuration. The [Configuration](#configuration) section lists the parameters that can be configured during installation.

## Uninstalling the Chart

To uninstall/delete the my-release deployment:

```console
helm delete my-release
```

The command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration

The following table lists the configurable parameters of the Axelix Master chart and their default values.

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ingress.enabled` | Whether to create a dedicated ingress K8S resource for external access to the axelix master ui | `true` |
| `ingress.name` | Name of the created ingress. Default is the name of the chart | `""` |
| `ingress.default` | Namespace where the ingress is supposed to be created | `"default"` |
| `ingress.className` | Class name of the ingress | `"nginx"` |
| `ingress.annotations` | Additional annotations for ingress | `{}` |
| `ingress.host` | Host of the ingress | `""` |
| `ingress.tls.enabled` | Whether tls is enabled for the host | `false` |
| `ingress.tls.secretName` | The name of the secret that stores the certificate and private key | `""` |
| `rbac.targetNamespace` | The namespace where monitored pods reside. Inside this namespace will also role and role-binding be created if `autoCreateRole = true` | `"default"` |
| `rbac.autoCreateRole` | Whether to create the Role and Role-Binding for backend service account | `true` |
| `imagePullSecrets` | Array of secret names to be used when pulling the Axelix images | `[]` |
| `backend.name` | The name to be given to deployment and container inside pods | `"axelix-backend"` |
| `backend.serviceAccount.name` | Name of the service account to create (if `create = true`) or reference to an existing service account. Default: the same name as the backend deployment | `""` |
| `backend.serviceAccount.namespace` | Namespace of the service account, where if not creating, existing service account resides. If new service account required, the namespace where a new service account will be created | `"default"` |
| `backend.serviceAccount.create` | Whether to create service account for the backend pods or rely on an existing one | `"false"` |
| `backend.serviceAccount.annotations` | Annotations for the backend service account | `{}` |
| `backend.serviceAccount.automount` | Whether to automount the service account into pod or not | `"true"` |
| `backend.podSecurityContext` | Security context of the axelix master backend pods | `{}` |
| `backend.podLabels` | Custom pod labels | `{}` |
| `backend.podAnnotations` | Pod annotations | `{}` |
| `backend.custom.labels` | Custom labels for the K8S deployment | `""` |
| `backend.custom.annotations` | Custom annotations for the K8S deployment | `""` |
| `backend.autoscaling.enabled` | Whether the HPA is enabled for backend | `"false"` |
| `backend.autoscaling.hpa.minReplicas` | Minimum number of replicas for HPA | `1` |
| `backend.autoscaling.hpa.maxReplicas` | Maximum number of replicas for HPA | `3` |
| `backend.autoscaling.hpa.targetCPU` | Target CPU utilization percentage for HPA | `80` |
| `backend.autoscaling.hpa.targetMemory` | Target memory utilization percentage for HPA | `""` |
| `backend.replicaCount` | Amount of backend replicas to be deployed | `1` |
| `backend.image.tag` | Reference to the axelix master backend image (without repository) to be used | `""` |
| `backend.image.name` | Name of the image including repository prefix | `""` |
| `backend.image.pullPolicy` | Image pull policy | `"IfNotPresent"` |
| `backend.liveness.delay` | Initial delay seconds for liveness probe | `15` |
| `backend.liveness.failureThreshold` | Failure threshold for liveness probe | `10` |
| `backend.liveness.timeoutSeconds` | Timeout seconds for liveness probe | `10` |
| `backend.liveness.period` | Period seconds for liveness probe | `5` |
| `backend.readiness.delay` | Initial delay seconds for readiness probe | `15` |
| `backend.readiness.failureThreshold` | Failure threshold for readiness probe | `10` |
| `backend.readiness.timeoutSeconds` | Timeout seconds for readiness probe | `10` |
| `backend.readiness.period` | Period seconds for readiness probe | `5` |
| `backend.resources` | Resources (limits and requests of memory and cpu) to be used by axelix backend | `{}` |
| `backend.volumeMounts` | Additional volume mounts to be used (expecting a corresponding "volume" definition) | `[]` |
| `backend.volumes` | Additional volumes to be attached to the axelix master backend pods | `[]` |
| `backend.nodeSelector` | The node selector for the Axelix Master backend pods. By default, any node is capable to run the pod | `{}` |
| `backend.affinity` | Custom affinity rules for the Axelix Master backend pods | `{}` |
| `backend.tolerations` | Array of tolerations for the Axelix Master backend | `[]` |
| `backend.service.port` | Port (both target and source) of the K8S service allocated for the backend | `8080` |
| `backend.service.type` | Type of the K8S service allocated for the backend | `"ClusterIP"` |
| `frontend.name` | The name to be given to deployment and container inside pods | `"axelix-frontend"` |
| `frontend.serviceAccount.name` | Name of the service account to create (if `create = true`) or reference to an existing service account. Default: the same name as the frontend deployment | `""` |
| `frontend.serviceAccount.namespace` | Namespace of the service account, where if not creating, existing service account resides. If new service account required, the namespace where a new service account will be created | `"default"` |
| `frontend.serviceAccount.create` | Whether to create service account for the frontend pods or rely on an existing one | `"false"` |
| `frontend.serviceAccount.annotations` | Annotations for the frontend service account | `{}` |
| `frontend.serviceAccount.automount` | Whether to automount the service account into pod or not | `"true"` |
| `frontend.podSecurityContext` | Security context of the axelix master frontend pods | `{}` |
| `frontend.podLabels` | Custom frontend pod labels | `{}` |
| `frontend.podAnnotations` | Frontend pod annotations | `{}` |
| `frontend.custom.labels` | Custom labels for the frontend K8S deployment | `""` |
| `frontend.custom.annotations` | Custom annotations for the frontend K8S deployment | `""` |
| `frontend.autoscaling.enabled` | Whether the HPA is enabled for frontend | `"false"` |
| `frontend.autoscaling.hpa.minReplicas` | Minimum number of replicas for HPA | `1` |
| `frontend.autoscaling.hpa.maxReplicas` | Maximum number of replicas for HPA | `2` |
| `frontend.autoscaling.hpa.targetCPU` | Target CPU utilization percentage for HPA | `80` |
| `frontend.autoscaling.hpa.targetMemory` | Target memory utilization percentage for HPA | `""` |
| `frontend.replicaCount` | Amount of frontend replicas to be deployed. Must be specified if autoscaling is disabled | `1` |
| `frontend.image.tag` | Reference to the axelix master frontend image (without repository) to be used | `""` |
| `frontend.image.name` | Name of the image including repository prefix | `""` |
| `frontend.image.pullPolicy` | Image pull policy | `"IfNotPresent"` |
| `frontend.liveness.delay` | Initial delay seconds for liveness probe | `15` |
| `frontend.liveness.failureThreshold` | Failure threshold for liveness probe | `10` |
| `frontend.liveness.timeoutSeconds` | Timeout seconds for liveness probe | `10` |
| `frontend.liveness.period` | Period seconds for liveness probe | `5` |
| `frontend.readiness.delay` | Initial delay seconds for readiness probe | `15` |
| `frontend.readiness.failureThreshold` | Failure threshold for readiness probe | `10` |
| `frontend.readiness.timeoutSeconds` | Timeout seconds for readiness probe | `10` |
| `frontend.readiness.period` | Period seconds for readiness probe | `5` |
| `frontend.resources` | Resources (limits and requests of memory and cpu) to be used by axelix frontend | `{}` |
| `frontend.volumeMounts` | Additional volume mounts to be used (expecting a corresponding "volume" definition) | `[]` |
| `frontend.volumes` | Additional volumes to be attached to the axelix master frontend pods | `[]` |
| `frontend.nodeSelector` | The node selector for the Axelix Master frontend pods. By default, any node is capable to run the pod | `{}` |
| `frontend.affinity` | Custom affinity rules for the Axelix Master frontend pods | `{}` |
| `frontend.tolerations` | Array of tolerations for the Axelix Master frontend pods | `[]` |
| `frontend.service.port` | Port (both target and source) of the K8S service allocated for the frontend | `80` |
| `frontend.service.type` | Type of the K8S service allocated for the frontend | `"ClusterIP"` |

## Installing the Chart with Custom Values

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`. For example:

```console
helm install my-release axelixlabs/axelix \
  --set backend.replicaCount=2 \
  --set backend.image.name=myregistry/axelix \
  --set backend.image.tag=v1.0.0 \
  --set frontend.image.name=myregistry/axelix-ui \
  --set frontend.image.tag=v1.0.0
```

Alternatively, a YAML file that specifies the values for the parameters can be provided while installing the chart. For example:

```console
helm install my-release axelixlabs/axelix -f values.yaml
```

## Backend Configuration

The backend component runs on port 8080 and handles the API endpoints. The backend service is exposed at `/api` path when ingress is enabled.

### Health Checks

The backend includes health check endpoints:
- Liveness: `/api/axelix/actuator/health/liveness`
- Readiness: `/api/axelix/actuator/health/readiness`

## Frontend Configuration

The frontend component runs on port 80 and serves the web UI. The frontend service is exposed at `/` path when ingress is enabled.

### Health Checks

The frontend includes health check endpoints:
- Liveness: `/api/axelix/actuator/health/liveness`
- Readiness: `/api/axelix/actuator/health/readiness`

## Ingress Configuration

When `ingress.enabled` is set to `true`, the chart creates an Ingress resource that routes traffic:
- `/api` path to the backend service (port 8080)
- `/` path to the frontend service (port 80)

### Example Ingress Configuration

```yaml
ingress:
  enabled: true
  className: nginx
  host: axelix.example.com
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  tls:
    enabled: true
    secretName: axelix-tls
```

## Service Accounts and RBAC

The chart can create service accounts for both backend and frontend components. The backend service account can be granted permissions via RBAC to access Kubernetes resources.

### Backend RBAC

When `rbac.autoCreateRole` is enabled, the chart creates:
- A `Role` in the `rbac.targetNamespace` with permissions to get, list, and watch pods, services, and endpoints
- A `RoleBinding` that binds the backend service account to the role

This allows the backend to monitor Kubernetes resources in the specified namespace.

## Horizontal Pod Autoscaling (HPA)

Both backend and frontend components support horizontal pod autoscaling. Enable HPA by setting `autoscaling.enabled` to `true`.

### Example HPA Configuration

```yaml
backend:
  autoscaling:
    enabled: true
    hpa:
      minReplicas: 2
      maxReplicas: 5
      targetCPU: 70
      targetMemory: 80
```

When HPA is enabled, the `replicaCount` parameter is ignored, and the deployment is managed by the HorizontalPodAutoscaler.

## Resource Management

Both backend and frontend support custom resource requests and limits. Configure resources as follows:

```yaml
backend:
  resources:
    requests:
      memory: "256Mi"
      cpu: "100m"
    limits:
      memory: "512Mi"
      cpu: "500m"

frontend:
  resources:
    requests:
      memory: "128Mi"
      cpu: "50m"
    limits:
      memory: "256Mi"
      cpu: "200m"
```

## Node Selection and Scheduling

You can control pod placement using node selectors, affinity rules, and tolerations:

```yaml
backend:
  nodeSelector:
    kubernetes.io/os: linux
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        podAffinityTerm:
          labelSelector:
            matchExpressions:
            - key: app.kubernetes.io/name
              operator: In
              values:
              - axelix-backend
          topologyKey: kubernetes.io/hostname
  tolerations:
  - key: "special"
    operator: "Equal"
    value: "true"
    effect: "NoSchedule"
```

## Volumes and Volume Mounts

Both components support additional volumes and volume mounts:

```yaml
backend:
  volumes:
  - name: config
    configMap:
      name: axelix-backend-config
  volumeMounts:
  - name: config
    mountPath: /etc/axelix/config
    readOnly: true
```

## Image Configuration

Configure container images for both components:

```yaml
backend:
  image:
    name: "myregistry/axelix-backend"
    tag: "v1.0.0"
    pullPolicy: "IfNotPresent"

frontend:
  image:
    name: "myregistry/axelix-frontend"
    tag: "v1.0.0"
    pullPolicy: "IfNotPresent"
```

### Image Pull Secrets

If your images are in a private registry, configure image pull secrets:

```yaml
imagePullSecrets:
  - name: myregistry-secret
```
