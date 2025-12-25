pipeline {
    agent any

    environment {
        // Docker / ACR
        ACR_NAME   = 'userserviceregistry'
        ACR_LOGIN  = "${ACR_NAME}.azurecr.io"
        IMAGE_NAME = 'user-service'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        FULL_IMAGE = "${ACR_LOGIN}/${IMAGE_NAME}:${IMAGE_TAG}"

        // Kubernetes
        K8S_NAMESPACE = 'default'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $FULL_IMAGE .'
            }
        }

        stage('Login to ACR') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'acr-creds',
                    usernameVariable: 'ACR_USER',
                    passwordVariable: 'ACR_PASS'
                )]) {
                    sh '''
                        echo $ACR_PASS | docker login $ACR_LOGIN -u $ACR_USER --password-stdin
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh 'docker push $FULL_IMAGE'
            }
        }

        stage('Deploy to AKS') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        set -e
                        export KUBECONFIG=$KUBECONFIG_FILE

                        kubectl get nodes

                        kubectl apply -f k8s/secret.yaml
                        envsubst < k8s/deployment.yaml | kubectl apply -f -
                        kubectl apply -f k8s/ingress.yaml

                        kubectl rollout status deployment/user-service
                        kubectl get pods
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
