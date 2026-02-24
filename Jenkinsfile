pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: https://github.com/nguyngc/fliply.git'

            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
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
    }
}
