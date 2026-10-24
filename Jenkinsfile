pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME        = 'school-backend'
        IMAGE_NAME      = 'school-backend:latest'
        APP_PORT        = '8001'
        DOCKER_NETWORK  = 'jenkins-mysql_default'

        DB_HOST         = 'mysql-8081'
        DB_PORT         = '3306'
        DB_NAME         = 'appdb'
        DB_USER         = 'appuser'
        DB_PASSWORD     = 'Website_12321'

        DB_URL          = "jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/prem21kumars/school-application-backend.git'
            }
        }

        stage('Build and Test') {
            steps {
                sh '''
                    if [ -f mvnw ]; then
                        chmod +x mvnw
                        ./mvnw clean verify
                    else
                        mvn clean verify
                    fi
                '''
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
                    # Stop previous container if running
                    docker rm -f "$APP_NAME" || true

                    # Start new container on the MySQL Docker network with properties injected
                    docker run -d \
                        --name "$APP_NAME" \
                        --restart unless-stopped \
                        --network "$DOCKER_NETWORK" \
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
                    sleep 10
                    docker ps --filter "name=$APP_NAME"
                    docker logs --tail 50 "$APP_NAME"
                '''
            }
        }
    }

    post {
        success {
            echo 'School Backend CI/CD completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check Console Output.'
        }
    }
}
