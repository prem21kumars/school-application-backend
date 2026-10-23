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
        DB_PASSWORD = 'AppSecure_2026_X9!'
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
                    docker rm -f "$APP_NAME" || true

                    docker run -d \
                        --name "$APP_NAME" \
                        --restart unless-stopped \
                        --network jenkins-mysql_default \
                        -p "$APP_PORT:8080" \
                        -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql-8081:3306/appdb" \
                        -e SPRING_DATASOURCE_USERNAME="appuser" \
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
            echo 'School Backend CI/CD completed successfully.'
        }
        failure {
            echo 'CI/CD failed. Check the Jenkins console output.'
        }
    }
}
