pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'MAVEN_3.6.3'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Récupération du code depuis GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Compilation du projet avec Maven...'
                bat 'mvn clean package -DskipTests'
            }
        }
    }
}
