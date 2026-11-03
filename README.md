# Taller Service Mesh con Spring Boot e Istio

Ingeniería de Software II – UPTC, sesión 20.

Parte del proyecto de la sesión 14 (EDA con Kafka, repo `microkafka`), separado en tres microservicios que se despliegan en Kubernetes (kind) dentro de una malla de servicios Istio.

## Arquitectura

```
                    Istio Gateway (my-gateway, :80)
                              │
        ┌─────────────────────┼─────────────────────┐
   /orders/**            /customers/**          /logins/**
        │                     │                     │
  order-service ──HTTP──► customer-service ──HTTP──► login-service
        │                     │                     │
        └──────── Kafka (order_events / customer_events / login_events) ────────┘
                              │
                MySQL (orderdb / customerdb / logindb)
```

- Cada pod de los microservicios lleva un sidecar Envoy, inyectado por Istio.
- **order → customer:** antes de publicar un pedido, `order-service` consulta por HTTP si el cliente existe.
- **customer → login:** al crear un cliente, `customer-service` le pide a `login-service` que cree su login (punto 3 de la sesión 14).
- MySQL y Kafka quedan fuera de la malla, sin sidecar.

| Servicio | Puerto local | Rutas |
|---|---|---|
| customer-service | 8081 | `POST /customers/addcustomer`, `POST /customers/editcustomer`, `GET /customers/findcustomerbyid/{document}`, `GET /customers/findallcustomers` |
| order-service | 8082 | `POST /orders/addorder`, `POST /orders/editorder`, `GET /orders/findorderbyid/{id}`, `GET /orders/findallorders` |
| login-service | 8083 | `POST /logins/addlogin`, `POST /logins/editlogin`, `GET /logins/findloginbyid/{username}`, `GET /logins/findalllogins` |

En Kubernetes los tres escuchan en el 8080, y su `Service` los expone en el puerto 80.

## Estructura

```
customer-service/  order-service/  login-service/
   ├── src/            código Spring Boot
   ├── k8s/            deployment.yaml y service.yaml
   ├── Dockerfile
   └── pom.xml
k8s/infra/           MySQL y Kafka para el clúster
k8s/istio/           Gateway, VirtualServices, DestinationRules y mTLS
scripts/deploy.ps1   despliegue completo
docker-compose.yml   MySQL y Kafka para correr los servicios en local
```

## Requisitos

JDK 21 o superior, Maven, Docker Desktop, kubectl, kind e istioctl. El clúster usa unos 6 GB de la memoria de Docker.

## Desplegar en kind con Istio

Con Docker Desktop encendido, desde la raíz del repo:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\deploy.ps1
```

El script hace los pasos del taller:

1. `kind create cluster --name istio-cluster`
2. `istioctl install --set profile=demo -y` y `kubectl label namespace default istio-injection=enabled`
3. Instala Prometheus y Kiali
4. Despliega MySQL y Kafka
5. Por cada servicio: `docker build`, `kind load docker-image` y `kubectl apply -f <servicio>/k8s/`
6. `kubectl apply -f k8s/istio`

Para redesplegar después de cambiar código, sin recrear el clúster:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\deploy.ps1 -SkipCluster
```

## Probar

Entrada por el Istio Gateway (déjalo abierto en otra terminal):

```bash
kubectl port-forward svc/istio-ingressgateway -n istio-system 8080:80
```

Crear un cliente, que también crea su login:

```bash
curl -X POST http://localhost:8080/customers/addcustomer -H "Content-Type: application/json" -d "{\"document\":\"1001\",\"firstname\":\"Ana\",\"lastname\":\"Perez\",\"address\":\"Calle 1\",\"phone\":\"3001234567\",\"email\":\"ana@uptc.edu.co\"}"
```

```bash
curl http://localhost:8080/customers/findallcustomers
```

```bash
curl http://localhost:8080/logins/findalllogins
```

Crear un pedido; si el cliente no existe responde 400:

```bash
curl -X POST http://localhost:8080/orders/addorder -H "Content-Type: application/json" -d "{\"id\":\"1\",\"customerDocument\":\"1001\",\"product\":\"Teclado\",\"quantity\":2,\"total\":150000,\"orderDate\":\"2026-11-02\"}"
```

```bash
curl http://localhost:8080/orders/findallorders
```

Comunicación entre servicios desde dentro de la malla (paso 10 del taller), desde el contenedor de la app:

```bash
kubectl exec deploy/order-service -c order-service -- curl -s http://customer-service/customers/findallcustomers
```

Desde el contenedor `istio-proxy` la misma llamada falla con código 56: ese tráfico no pasa por Envoy, sale sin mTLS y la política `STRICT` lo rechaza.

## Kiali (consola gráfica del Control Plane)

```bash
istioctl dashboard kiali
```

En **Traffic Graph**, con el namespace `default`, se ve order → customer → login con el candado de mTLS. Para que haya tráfico en el grafo, repite varias veces las peticiones de arriba.

## Reglas de Istio incluidas (`k8s/istio`)

| Archivo | Qué hace |
|---|---|
| `gateway.yaml` | Abre el puerto 80 del ingress gateway para todos los hosts |
| `virtualservice.yaml` | Enruta `/customers`, `/orders` y `/logins` desde el gateway a cada servicio |
| `destination-rules.yaml` | mTLS (`ISTIO_MUTUAL`), balanceo de carga y circuit breaker (`outlierDetection`) |
| `mesh-routing.yaml` | Timeouts y reintentos en las llamadas internas a customer-service y login-service |
| `peer-authentication.yaml` | mTLS `STRICT` en el namespace `default` |

## Correr en local (sin Kubernetes)

```bash
docker compose up -d
```

Después, en cada carpeta de servicio, `mvn spring-boot:run`. Usan MySQL en el puerto 3307 y Kafka en el 29092.

## Comandos útiles

```bash
kubectl get pods
```

```bash
kubectl logs deploy/customer-service -c customer-service
```

```bash
kind delete cluster --name istio-cluster
```
