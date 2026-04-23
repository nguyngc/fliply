pipeline {
    agent any
    tools {
        maven 'Maven 3.9.12'
    }

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'DockerID'
        DOCKERHUB_REPO = 'nguyngc/fliply'
        DOCKER_IMAGE_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/nguyngc/fliply.git'
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn clean install -DskipTests'
                    } else {
                        bat 'mvn clean install -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
        }

        stage('Code Coverage') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn jacoco:report'
                    } else {
                        bat 'mvn jacoco:report'
                    }
                }
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                recordCoverage tools: [[parser: 'JACOCO']]
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    if (isUnix()) {
                        sh 'mvn sonar:sonar'
                    } else {
                        bat 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            environment {
                PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"
            }
            steps {
                script {
                    if (isUnix()) {
                        sh "docker build --platform linux/amd64 -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ."
                    } else {
                        bat "docker build -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ."
                    }
                }
            }
        }

        stage('Push Docker Image') {
            environment {
                PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"
            }
            steps {
                script {
                    if (isUnix()) {
                        withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                            sh '''
                                echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                                docker push ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}
                            '''
                        }
                    } else {
                        docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                            docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            environment {
                PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"
                DB_HOST = "localhost"
                DB_PORT = "3306"
                DB_NAME = "fliply"
                DB_USER = "appuser"
            }
            steps {
                script {
                    if (isUnix()) {
                        withCredentials([string(credentialsId: 'fliply-db-pass', variable: 'SECRET_DB_PASS')]) {
                            sh '''
                                cat > .env <<EOF
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_NAME=${DB_NAME}
DB_USER=${DB_USER}
DB_PASS=${SECRET_DB_PASS}
DOCKERHUB_REPO=${DOCKERHUB_REPO}
IMAGE_TAG=${DOCKER_IMAGE_TAG}
HOST_DB_PORT=3307
EOF
                                docker compose --env-file .env up -d --remove-orphans
                            '''
                        }
                    } else {
                        bat 'echo Deploy stage is currently configured for Unix agents only.'
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                if (isUnix()) {
                    sh 'rm -f .env'
                } else {
                    bat 'docker rm -f mariadb_test 2>nul || exit 0'
                }
            }
        }
    }
}
