pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
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
                sh 'docker ps -a --format "{{.Names}}" | grep -E "(uocc-postgres|alert-service|auth-service|gateway-service|cctv-service|traffic-service|power-service|python-service|frontend)" | xargs -r docker rm -f || true'
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
