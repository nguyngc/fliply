pipeline {
    agent any

    stages {

        stage('Start MariaDB') {
            steps {
                // Xóa container cũ nếu còn tồn tại
                bat 'docker rm -f mariadb_test 2>nul || exit 0'

                // Chạy container mới
                bat '''
                    docker run -d ^
                    -e MARIADB_ROOT_PASSWORD=root ^
                    -e MARIADB_DATABASE=fliply ^
                    -e MARIADB_USER=appuser ^
                    -e MARIADB_PASSWORD=password ^
                    -p 3306:3306 ^
                    --name mariadb_test mariadb:10.6
                '''
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
            // Cleanup container sau khi build
            bat 'docker rm -f mariadb_test 2>nul || exit 0'
        }
    }
}
