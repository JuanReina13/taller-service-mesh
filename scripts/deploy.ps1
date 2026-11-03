# Despliega todo el taller en un clúster kind con Istio y Kiali.
# Uso (desde la raíz del repo, con Docker Desktop encendido):
#   powershell -ExecutionPolicy Bypass -File scripts\deploy.ps1
# Opcional: -SkipCluster para no crear el clúster ni instalar Istio (solo reconstruir y redesplegar).
param(
    [string]$ClusterName = "istio-cluster",
    [switch]$SkipCluster
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$services = @("customer-service", "order-service", "login-service")

function Run([string]$cmd) {
    Write-Host ">> $cmd" -ForegroundColor Cyan
    Invoke-Expression $cmd
    if ($LASTEXITCODE -ne 0) { throw "Falló: $cmd" }
}

if (-not $SkipCluster) {
    # 1. Clúster de Kubernetes con kind
    $clusters = @(kind get clusters 2>$null)
    if ($clusters -notcontains $ClusterName) {
        Run "kind create cluster --name $ClusterName"
    } else {
        Write-Host "El clúster $ClusterName ya existe" -ForegroundColor Yellow
    }

    # 2. Istio (perfil demo) e inyección automática de sidecars en el namespace default
    Run "istioctl install --set profile=demo -y"
    Run "kubectl label namespace default istio-injection=enabled --overwrite"

    # 3. Addons de observabilidad: Prometheus (métricas) y Kiali (consola gráfica del Control Plane)
    # istioctl imprime "1.23.2" o "client version: 1.23.2" según la versión
    $istioOut = (istioctl version --remote=false 2>$null) -join " "
    if ($istioOut -notmatch "(\d+)\.(\d+)\.\d+") { throw "No se pudo leer la versión de istioctl: $istioOut" }
    $minor = "$($Matches[1]).$($Matches[2])"
    $addons = "https://raw.githubusercontent.com/istio/istio/release-$minor/samples/addons"
    Run "kubectl apply -f $addons/prometheus.yaml"
    Run "kubectl apply -f $addons/kiali.yaml"
}

# 4. Infraestructura: MySQL y Kafka
Run "kubectl apply -f k8s/infra"
Run "kubectl rollout status deployment/mysql --timeout=300s"
Run "kubectl rollout status deployment/kafka --timeout=300s"

# 5. Microservicios: construir imagen, cargarla en kind y desplegar
foreach ($svc in $services) {
    Run "docker build -t ${svc}:latest ./$svc"
    Run "kind load docker-image ${svc}:latest --name $ClusterName"
    Run "kubectl apply -f $svc/k8s/deployment.yaml"
    Run "kubectl apply -f $svc/k8s/service.yaml"
    # Si la imagen cambió, reiniciar los pods para que tomen la nueva
    Run "kubectl rollout restart deployment/$svc"
}

# 6. Reglas de Istio: Gateway, VirtualServices, DestinationRules y mTLS
Run "kubectl apply -f k8s/istio"

foreach ($svc in $services) {
    Run "kubectl rollout status deployment/$svc --timeout=300s"
}

Run "kubectl get pods"
Write-Host ""
Write-Host "Listo. En otra terminal abre la entrada por el Istio Gateway:" -ForegroundColor Green
Write-Host "  kubectl port-forward svc/istio-ingressgateway -n istio-system 8080:80"
Write-Host "Y para Kiali:"
Write-Host "  istioctl dashboard kiali"
