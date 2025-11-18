pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
    }
    
    environment {
        DOCKER_COMPOSE_VERSION = '1.29.2'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }
        
        stage('Build Backend') {
            steps {
                echo 'Building Spring Boot microservices with Maven...'
                dir('backend_java') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                sh 'docker-compose build --no-cache'
            }
        }
        
        stage('Stop Old Containers') {
            steps {
                echo 'Stopping old containers...'
                sh 'docker rm -f uocc-postgres || true'
                sh 'docker-compose down || true'
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'Deploying with Docker Compose...'
                sh 'docker-compose up -d'
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment...'
                sh 'docker ps'
                sh 'sleep 10'
                sh 'curl -f http://localhost:80 || echo "Frontend not ready yet"'
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
        always {
            echo 'Cleaning up...'
            sh 'docker system prune -f || true'
        }
    }
}
