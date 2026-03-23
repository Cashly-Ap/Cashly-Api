# Deploy Rápido a AWS (Testing)

Guía simple para subir el backend a AWS Elastic Beanstalk sin complicaciones.

---

## 1. Pre-requisitos

- Cuenta de AWS (puede ser gratuita)
- AWS CLI instalado y configurado
- Docker instalado localmente

### Configurar AWS CLI

```bash
aws configure
# Ingresa:
# AWS Access Key ID
# AWS Secret Access Key
# Default region name: eu-north-1
# Default output format: json
```

---

## 2. Crear recursos en AWS

### Opción A: Crear todo manualmente (1 sola vez)

```bash
# 1. Crear bucket S3 para almacenar el deploy
aws s3 mb s3://parsun-api-test-$(date +%s) --region eu-north-1

# 2. Crear rol para Elastic Beanstalk (si no existe)
aws iam create-service-linked-role --aws-service-name elasticbeanstalk.amazonaws.com 2>/dev/null || echo "Rol ya existe"

# 3. Crear aplicación Elastic Beanstalk
aws elasticbeanstalk create-application \
  --application-name "Parsun-API-Test" \
  --description "Parsun API para testing"

# 4. Crear entorno (esto toma ~10 minutos)
aws elasticbeanstalk create-environment \
  --application-name "Parsun-API-Test" \
  --environment-name "parsun-test-env" \
  --solution-stack-name "64bit Amazon Linux 2023 v3.1.0 running Docker" \
  --region eu-north-1
```

### Opción B: Usar la consola web (más fácil)

1. Ve a https://console.aws.amazon.com/elasticbeanstalk
2. Click "Create application"
3. Configura:
   - **Application name**: `Parsun-API-Test`
   - **Platform**: Docker
   - **Region**: eu-north-1 (Stockholm - tiene Free Tier)
4. Click "Create environment"
5. Espera ~10 minutos

---

## 3. Preparar la base de datos

### Opción A: RDS MySQL (gratis hasta 750 horas/mes)

1. Ve a https://console.aws.amazon.com/rds
2. Click "Create database"
3. Configura:
   - **Engine**: MySQL
   - **Templates**: Free tier
   - **DB instance identifier**: `parsun-db`
   - **Master username**: `admin`
   - **Master password**: `tu_password_seguro`
   - **Instance class**: db.t3.micro
4. Espera ~10 minutos
5. Copia el endpoint (algo como `parsun-db.xxxxx.eu-north-1.rds.amazonaws.com`)

### Opción B: MySQL embebido en el contenedor (para testing rápido)

Edita el `Dockerrun.aws.json`:

```json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "registry.gitlab.com/personal-proyect-gabriel/parsun/parsun-api:latest",
    "Update": "true"
  },
  "Ports": [{ "ContainerPort": "8080" }],
  "EnvironmentVariables": [
    { "Key": "SPRING_PROFILES_ACTIVE", "Value": "production" },
    { "Key": "SPRING_DATASOURCE_URL", "Value": "jdbc:mysql://TU-RDS-ENDPOINT:3306/parsun?createDatabaseIfNotExist=true" },
    { "Key": "SPRING_DATASOURCE_USERNAME", "Value": "admin" },
    { "Key": "SPRING_DATASOURCE_PASSWORD", "Value": "tu_password" },
    { "Key": "JWT_SECRET", "Value": "tu_jwt_secret_largo_y_seguro" }
  ]
}
```

---

## 4. Deploy con Docker local

### 4.1. Construir imagen localmente

```bash
docker build -t parsun-api:latest .
```

### 4.2. Etiquetar para ECR (Elastic Container Registry)

```bash
# Crear repositorio en ECR (solo una vez)
aws ecr create-repository --repository-name parsun-api --region eu-north-1

# Etiquetar imagen
aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin TU_AWS_ACCOUNT_ID.dkr.ecr.eu-north-1.amazonaws.com

docker tag parsun-api:latest TU_AWS_ACCOUNT_ID.dkr.ecr.eu-north-1.amazonaws.com/parsun-api:latest
```

### 4.3. Subir imagen a ECR

```bash
docker push TU_AWS_ACCOUNT_ID.dkr.ecr.eu-north-1.amazonaws.com/parsun-api:latest
```

### 4.4. Actualizar Dockerrun.json

```json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "TU_AWS_ACCOUNT_ID.dkr.ecr.eu-north-1.amazonaws.com/parsun-api:latest",
    "Update": "true"
  },
  "Ports": [{ "ContainerPort": "8080" }],
  "EnvironmentVariables": [
    { "Key": "SPRING_PROFILES_ACTIVE", "Value": "production" }
  ]
}
```

### 4.5. Deploy a Elastic Beanstalk

```bash
# Empaquetar
zip -r parsun-deploy.zip Dockerrun.aws.json

# Subir a S3
aws s3 cp parsun-deploy.zip s3://TU_BUCKET_NAME/

# Crear versión y deployar
aws elasticbeanstalk create-application-version \
  --application-name "Parsun-API-Test" \
  --version-label "v1-$(date +%Y%m%d%H%M%S)" \
  --source-bundle S3Bucket=TU_BUCKET_NAME,S3Key=parsun-deploy.zip

aws elasticbeanstalk update-environment \
  --environment-name "parsun-test-env" \
  --version-label "v1-$(date +%Y%m%d%H%M%S)"
```

---

## 5. Verificar el deploy

```bash
# Ver estado del entorno
aws elasticbeanstalk describe-environments \
  --environment-names "parsun-test-env" \
  --query 'Environments[0].Status'

# Ver URL del entorno
aws elasticbeanstalk describe-environments \
  --environment-names "parsun-test-env" \
  --query 'Environments[0].EndpointURL'
```

La URL será algo como: `parsun-test-env.XXXX.eu-north-1.elasticbeanstalk.com`

---

## 6. Probar

```bash
# Health check
curl https://TU-ENDPOINT.elasticbeanstalk.com/actuator/health

# Swagger UI
open https://TU-ENDPOINT.elasticbeanstalk.com/swagger-ui.html
```

---

## 7. Troubleshooting

### El entorno no levanta

```bash
# Ver logs
aws elasticbeanstalk describe-instance-health --environment-name "parsun-test-env"

# Descargar logs completos
aws elasticbeanstalk request-environment-info --environment-name "parsun-test-env" --info-type tail
aws elasticbeanstalk retrieve-environment-info --environment-name "parsun-test-env" --info-type tail
```

### Error de memoria

Edita la configuración en la consola EB:
- Configuration > Software > JVM Options: `-Xmx512m -Xms256m`

### La imagen no se puede descargar

Verifica que el bucket ECR tenga la imagen correcta:
```bash
aws ecr describe-images --repository-name parsun-api
```

### La base de datos no conecta

Verifica:
1. El security group de RDS permite conexiones desde EB
2. Las credenciales son correctas
3. El endpoint está bien escrito

---

## Estructura de archivos

```
.
├── Dockerfile                 # Imagen Docker
├── Dockerrun.aws.json        # Configuración EB
└── parsun-deploy.zip         # Generado al hacer deploy
```

---

## Limpiar recursos (cuando termines testing)

Para evitar cargos, elimina todo:

```bash
# Eliminar entorno EB
aws elasticbeanstalk terminate-environment --environment-name "parsun-test-env"

# Eliminar aplicación
aws elasticbeanstalk delete-application --application-name "Parsun-API-Test"

# Eliminar RDS (si lo creaste)
aws rds delete-db-instance --db-instance-identifier "parsun-db" --skip-final-snapshot

# Eliminar bucket S3
aws s3 rb s3://TU_BUCKET_NAME --force

# Eliminar ECR repository
aws ecr delete-repository --repository-name parsun-api --force
```

---

## URLs importantes

| Recurso | URL |
|---------|-----|
| Elastic Beanstalk Console | https://eu-north-1.console.aws.amazon.com/elasticbeanstalk |
| RDS Console | https://eu-north-1.console.aws.amazon.com/rds |
| ECR Console | https://eu-north-1.console.aws.amazon.com/ecr |
| S3 Console | https://s3.console.aws.amazon.com |

---

## Notas

- **Free Tier**: Elastic Beanstalk es gratis, pero la instancia EC2 y RDS tienen límites en el tier gratuito
- **Tiempo**: La primera implementación tarda ~10-15 minutos
- **Sin SSL**: La URL por defecto de EB ya tiene certificado HTTPS automático
- **Sin dominio**: Usa la URL de EB directamente para testing
