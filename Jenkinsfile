pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'MAVEN_3.6.3'
    }

    environment {
        // Variables d'environnement globales
        APP_NAME = 'java-products-lab'
        BUILD_VERSION = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '================================================'
                echo 'ETAPE 1 : Recuperation du code source'
                echo '================================================'
                checkout scm
                echo 'Code recupere avec succes depuis GitHub'
            }
        }

        stage('Build') {
            steps {
                echo '================================================'
                echo 'ETAPE 2 : Compilation et Packaging'
                echo '================================================'
                bat 'mvn clean package -DskipTests'
                echo 'Build termine avec succes'
            }
        }

        stage('Archive') {
            steps {
                echo '================================================'
                echo 'ETAPE 3 : Archivage des artefacts'
                echo '================================================'
                archiveArtifacts artifacts: 'target/*.jar',
                        fingerprint: true,
                        allowEmptyArchive: false
                echo 'Artefacts archives dans Jenkins'
            }
        }
    }

    post {
        success {
            echo '================================================'
            echo 'PIPELINE REUSSI !'
            echo "Build #${env.BUILD_NUMBER} termine avec succes"
            echo "Artefact disponible : ${APP_NAME}-${BUILD_VERSION}.jar"
            echo '================================================'
        }

        failure {
            echo '================================================'
            echo 'PIPELINE ECHOUE !'
            echo "Build #${env.BUILD_NUMBER} a echoue"
            echo 'Consultez les logs pour plus de details'
            echo '================================================'
        }

        always {
            echo '================================================'
            echo 'NETTOYAGE DU WORKSPACE'
            echo '================================================'
            cleanWs()
        }
    }
}
