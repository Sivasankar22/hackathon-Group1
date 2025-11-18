71
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
            echo 'Removing old service containers...'
            sh 'docker ps -a --format "{{.Names}}" | grep -E "(uocc-postgres|alert-service|auth-service|gateway-service|cctv-service|traffic-service|power-service|frontend)" | xargs -r docker rm -f || true'
            sh 'docker-compose down || true'
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
