# User Service CI/CD Pipeline - Spring Boot on Azure AKS

A Spring Boot authentication and user management service with automated CI/CD deployment to Azure Kubernetes Service (AKS) using Jenkins.

## 📋 Overview

This repository demonstrates a complete CI/CD pipeline for deploying a Spring Boot application to Azure Kubernetes Service. The application provides user authentication and management capabilities with JWT-based security, backed by Azure PostgreSQL Flexible Server.

### Key Features

- User registration and authentication
- JWT token-based security
- RESTful API for user management
- PostgreSQL database with Spring Data JPA
- Containerized deployment with Docker
- Automated CI/CD with Jenkins
- Azure Container Registry (ACR) for image storage
- Kubernetes orchestration on AKS

---

## 🏗️ Architecture
```
Jenkins Pipeline → Build & Test → Docker Build → Azure ACR → Deploy to AKS
                                                              ↓
                                                    Azure PostgreSQL
```

---

## 🚀 Prerequisites

Before setting up the pipeline, ensure you have:

- **Azure CLI** installed and configured
- **Azure Subscription** with appropriate permissions
- **Jenkins** server with the following plugins:
  - Docker Pipeline
  - Kubernetes CLI
  - Credentials Binding
- **kubectl** installed on Jenkins agent
- **Maven** installed on Jenkins agent
- **Docker** installed on Jenkins agent
- **envsubst** utility (usually part of `gettext` package)

---

## 🔧 Azure Infrastructure Setup

### 1. Create Resource Group
```bash
az group create -n pipeline-grp --location francecentral
```

### 2. Create Azure Container Registry
```bash
az acr create \
  --resource-group pipeline-grp \
  --name userserviceregistry \
  --sku Premium
```

### 3. Enable ACR Admin Access
```bash
az acr update -n userserviceregistry --admin-enabled true
```

### 4. Get ACR Credentials
```bash
az acr credential show -n userserviceregistry -o table
```

**Note:** Save the username and password for Jenkins credentials configuration.

### 5. Create AKS Cluster
```bash
az aks create \
  --resource-group pipeline-grp \
  --name user-aks \
  --node-count 1 \
  --generate-ssh-keys \
  --enable-managed-identity
```

### 6. Attach ACR to AKS
```bash
az aks update \
  --name user-aks \
  --resource-group pipeline-grp \
  --attach-acr userserviceregistry
```

### 7. Get AKS Credentials
```bash
az aks get-credentials --resource-group pipeline-grp --name user-aks
```

### 8. Create PostgreSQL Flexible Server
```bash
az postgres flexible-server create \
  --resource-group pipeline-grp \
  --name soufyanpipelinepostgresdb \
  --location francecentral \
  --admin-user adminuser \
  --admin-password "Password123" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --version 15 \
  --public-access 0.0.0.0
```

### 9. Create Database
```bash
az postgres flexible-server db create \
  --resource-group pipeline-grp \
  --server-name soufyanpipelinepostgresdb \
  --database-name userdb
```

### 10. Configure Firewall Rules
```bash
az postgres flexible-server firewall-rule create \
  --resource-group pipeline-grp \
  --name soufyanpipelinepostgresdb \
  --rule-name AllowAzure \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0
```

---

## 🔐 Jenkins Configuration

### Configure Jenkins Credentials

You need to add the following credentials in Jenkins (Manage Jenkins → Credentials):

#### 1. ACR Credentials
- **ID:** `acr-creds`
- **Type:** Username with password
- **Username:** Your ACR username (from step 4)
- **Password:** Your ACR password (from step 4)

#### 2. Kubeconfig File
- **ID:** `kubeconfig`
- **Type:** Secret file
- **File:** Your `~/.kube/config` file (generated in step 7)

To get your kubeconfig content:
```bash
cat ~/.kube/config
```

---

## 📝 Kubernetes Secrets Configuration

### Update Database Connection String

Before deploying, you need to update the `DB_URL` in `k8s/secret.yaml` with your actual PostgreSQL connection string.

**Current base64-encoded value in secret.yaml:**
```
DB_URL: amRiYzpwb3N0Z3JlczovL3NvdXZmeWFucGlwZWxpbmVwb3N0Z3Jlc2Ru
```

**To generate the correct value:**

1. Construct your JDBC URL:
```
jdbc:postgresql://soufyanpipelinepostgresdb.postgres.database.azure.com:5432/userdb?sslmode=require
```

2. Encode it to base64:
```bash
echo -n "jdbc:postgresql://soufyanpipelinepostgresdb.postgres.database.azure.com:5432/userdb?sslmode=require" | base64
```

3. Replace the `DB_URL` value in `k8s/secret.yaml` with the output.

### Secret Values Reference

The `k8s/secret.yaml` contains the following base64-encoded values:

- **DB_USERNAME:** `adminuser@soufvfyanpipelinepostgresdb`
- **DB_PASSWORD:** `Password123`
- **JWT_SECRET:** Your JWT signing key (generate a secure random string)

**To update any secret:**
```bash
echo -n "your-value" | base64
```

---

## 🎯 Pipeline Execution

### Pipeline Stages

1. **Checkout:** Clones the repository
2. **Build & Test:** Runs Maven tests
3. **Package:** Creates the JAR file
4. **Build Docker Image:** Builds container image
5. **Login to ACR:** Authenticates with Azure Container Registry
6. **Push Docker Image:** Pushes image to ACR
7. **Deploy to AKS:** Deploys to Kubernetes cluster

### Running the Pipeline

1. Create a new Pipeline job in Jenkins
2. Configure it to use this repository
3. Point to the `Jenkinsfile` in the root directory
4. Click "Build Now"

---

## 🧪 Testing the Deployment

### Get Service External IP
```bash
kubectl get service user-service-svc -n default
```

If using LoadBalancer type service, wait for external IP to be assigned. For testing purposes with the current NodePort or ClusterIP setup:
```bash
kubectl port-forward service/user-service-svc 8080:8080
```

### Test User Registration

**Endpoint:** `POST http://localhost:8080/auth/register`

**Request Body:**
```json
{
  "firstName": "Test1",
  "lastName": "User1",
  "email": "test@example.com",
  "phone": "+1234567890",
  "password": "password123",
  "country": "USA",
  "city": "New York",
  "street": "123 Main St"
}
```

**Using curl:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test1",
    "lastName": "User1",
    "email": "test@example.com",
    "phone": "+1234567890",
    "password": "password123",
    "country": "USA",
    "city": "New York",
    "street": "123 Main St"
  }'
```

---

## 🐛 Troubleshooting

### Check Pod Status
```bash
kubectl get pods -n default
kubectl describe pod <pod-name> -n default
```

### View Pod Logs
```bash
kubectl logs <pod-name> -n default -f
```

### Check Database Connection
```bash
kubectl exec -it <pod-name> -n default -- /bin/sh
# Inside the pod
env | grep DB
```

### Common Issues

**Issue:** Pods stuck in `ImagePullBackOff`
- Verify ACR is attached to AKS
- Check ACR credentials are correct
- Ensure image exists in ACR: `az acr repository list -n userserviceregistry`

**Issue:** Database connection failures
- Verify PostgreSQL firewall rules allow AKS subnet
- Check database credentials in secrets
- Ensure database `userdb` exists

**Issue:** Pipeline fails at Maven build
- Ensure Maven is installed on Jenkins agent
- Check Java version compatibility

---

## 📂 Project Structure
```
.
├── Jenkinsfile              # CI/CD pipeline definition
├── Dockerfile               # Container image definition
├── k8s/
│   ├── deployment.yaml      # Kubernetes deployment & service
│   ├── secret.yaml          # Kubernetes secrets
│   └── ingress.yaml         # Ingress configuration
├── src/                     # Spring Boot application source
└── pom.xml                  # Maven configuration
```

---

## 🔄 Making Updates

After code changes:

1. Commit and push changes to your repository
2. Jenkins pipeline will automatically trigger (if webhook configured)
3. Or manually click "Build Now" in Jenkins
4. New Docker image will be built with incremented build number
5. Application will be redeployed to AKS with zero-downtime rolling update

---

## 🧹 Cleanup

To delete all Azure resources:
```bash
az group delete -n pipeline-grp --yes --no-wait
```

---

## 📚 Additional Resources

- [Azure Kubernetes Service Documentation](https://docs.microsoft.com/azure/aks/)
- [Azure Container Registry Documentation](https://docs.microsoft.com/azure/container-registry/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)

---

