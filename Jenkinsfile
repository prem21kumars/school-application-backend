pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        APP_NAME    = 'school-backend'
        IMAGE_NAME  = 'school-backend:latest'
        APP_PORT    = '8001'

        DB_HOST     = 'mysql-8081'
        DB_PORT     = '3306'
        DB_NAME     = 'appdb'
        DB_USER     = 'appuser'
        DB_PASSWORD = 'AppSecure_2026_X9!'

        DB_URL      = "jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/prem21kumars/school-application-backend.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t "$IMAGE_NAME" .'
            }
        }

        stage('Deploy Backend') {
            steps {
                sh '''
                    # Stop old container if running
                    docker rm -f "$APP_NAME" || true

                    # Start new container on the same Docker network as MySQL
                    docker run -d \
                        --name "$APP_NAME" \
                        --restart unless-stopped \
                        --network jenkins-mysql_default \
                        -p "$APP_PORT:8080" \
                        -e SPRING_DATASOURCE_URL="$DB_URL" \
                        -e SPRING_DATASOURCE_USERNAME="$DB_USER" \
                        -e SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
                        "$IMAGE_NAME"
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    sleep 15
                    docker ps --filter "name=$APP_NAME"
                    docker logs --tail 50 "$APP_NAME"
                '''
            }
        }
    }

    post {
        success {
            echo 'School Backend CI/CD completed successfully.'
        }
        failure {
            echo 'CI/CD failed. Check the Jenkins console output.'
        }
    }
}
