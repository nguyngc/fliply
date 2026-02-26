pipeline {
    agent any

    stages {

        stage('Start MariaDB') {
            steps {
                script {
                    docker.image('mariadb:10.6').run(
                        "-e MARIADB_ROOT_PASSWORD=root " +
                        "-e MARIADB_DATABASE=fliply " +
                        "-e MARIADB_USER=appuser " +
                        "-e MARIADB_PASSWORD=password " +
                        "-p 3306:3306 " +
                        "--name mariadb_test"
                    )
                }
            }
        }

        stage('Wait for DB') {
            steps {
                bat "ping 127.0.0.1 -n 10 > nul"
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/nguyngc/fliply.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn -Dtest=*DaoTest,*ServiceTest,*RepositoryTest test'
            }
        }

        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
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

        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("thanh0201/fliply:latest")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub') {
                        dockerImage.push()
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                sh "docker rm -f mariadb_test || true"
            }
        }
    }
}
