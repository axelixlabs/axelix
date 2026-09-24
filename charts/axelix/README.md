# Axelix Helm Chart

* Installs [Axelix Master](https://github.com/axelixlabs/axelix) to Kubernetes, deploying the Axelix Master component.

## Get Repo Info

```console
helm repo add axelixlabs https://axelixlabs.github.io/axelix
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

{{ template "chart.valuesTable" . }}

## Installing the Chart with Custom Values

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`. For example:

```console
helm install my-release axelixlabs/axelix \
  --set image.name=myregistry/axelix \
  --set image.ref=1.1.0
```

Alternatively, a YAML file that specifies the values for the parameters can be provided while installing the chart. For example:

```console
helm install my-release axelixlabs/axelix -f values.yaml
```

## App Configuration

By default, axelix-master container runs on port 8080 and handles both API endpoints and the UI. The service is exposed at `/` path when ingress is enabled.

### Discovery

When `axelix.master.discovery.auto.enabled` is `true`, Axelix Master discovers services by querying the Kubernetes API. The namespaces to scan are configured via `rbac.targetNamespaces`.

By default, `axelix.master.discovery.auto.enabled` is `false`. Enable automatic discovery only when you want Axelix Master to discover services from the cluster instead of relying on self-registration.

### Health Checks

The axelix-master includes health check endpoints:
- Liveness: `/api/actuator/health/liveness`
- Readiness: `/api/actuator/health/readiness`

Probe timing (initial delay, period, failure threshold, timeout) is configurable via `liveness` and `readiness` in values.

## Service

When `service.create` is `true` (default), the chart creates a ClusterIP Service for the Axelix Master (ports 8080:8080 by default). Port name, target, and source are configurable via `service.port`. Set `service.create` to `false` if you manage the service yourself.

## Ingress Configuration

When `ingress.enabled` is set to `true`, the chart creates an Ingress resource that routes traffic to the Axelix Master service (into service, port 8080). By default, the ingress is created in the same namespace as the release. You can override this by setting `ingress.namespace`. The ingress resource name is set via `ingress.name` (default: `axelix-master`).

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

When `serviceAccount.create` is `true` (default), the chart creates a service account for the axelix-master component. By default, the service account is created in the same namespace as the release. You can override this by setting `serviceAccount.namespace`. Use an existing service account by setting `serviceAccount.create` to `false` and setting `serviceAccount.name` (and optionally `serviceAccount.namespace`). Token automount is controlled by `serviceAccount.automount` (default: `true`).

The service account can be granted permissions via RBAC to access Kubernetes resources.

### RBAC Configuration

When `rbac.autoCreateRole` is enabled, the chart creates per-namespace RBAC so a single service account can access multiple namespaces.

For each namespace in `rbac.targetNamespaces`, the chart creates a `Role` and a `RoleBinding` in that namespace. The Role grants get, list, and watch on pods, services, and endpoints. The RoleBinding binds the axelix-master service account (from the release or `serviceAccount.namespace`) to that Role.

**NOTE:** Even if you do not want the Roles and RoleBindings to be created (`autoCreateRole : false`), then `rbac.targetNamespaces` should still be specified, since this is the setting that not only means what namespaces needs to be granted access to, it also means the namespaces to be scanned by Axelix Master in order to register the services (regardless of who arranges the access - this Helm Chart, or you manually).

Example for multiple namespaces:

```yaml
rbac:
  autoCreateRole: true
  targetNamespaces:
    - default
    - production
    - staging
```

The service account is created once (either in the release namespace or `serviceAccount.namespace`) and is bound to a Role in each of the target namespaces, so Axelix Master can monitor resources in all of them.

## Resource Management

The axelix-master supports custom resource requests and limits. Configure resources as follows:

```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

## Node Selection and Scheduling

You can control pod placement using node selectors, affinity rules, and tolerations:

```yaml
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
            - axelix-master
        topologyKey: kubernetes.io/hostname

tolerations:
- key: "special"
  operator: "Equal"
  value: "true"
  effect: "NoSchedule"
```

## Volumes and Volume Mounts

The axelix-master supports additional volumes and volume mounts:

```yaml
volumes:
- name: config
  configMap:
    name: axelix-master-config

volumeMounts:
- name: config
  mountPath: /etc/axelix/config
  readOnly: true
```

## Image Configuration

Configure the container image:

```yaml
image:
  name: "ghcr.io/axelixlabs/axelix"
  ref: "1.1.0"
  pullPolicy: "IfNotPresent"
```

### Image Pull Secrets

If your images are in a private registry, configure image pull secrets:

```yaml
imagePullSecrets:
  - name: myregistry-secret
```
