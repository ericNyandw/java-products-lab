pipeline {
    agent any
    // 1. Les Triggers (L'allumage)
    triggers {
        githubPush()  // Webhook GitHub
        pollSCM('H/5 * * * *')  // Vérification toutes les 5 min
    }
    tools {
        jdk 'JDK17'
        maven 'MAVEN_3.6.3'
    }

    environment {
        APP_NAME = 'java-products-lab'
        JAVA_VERSION = '17'
        BUILD_VERSION = "${env.BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'Java-Products-Lab'
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
                bat 'mvn clean package'
                echo 'Build termine avec succes'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '================================================'
                echo 'ETAPE 3 : Analyse de la qualite du code'
                echo '================================================'
                withSonarQubeEnv('SonarQube-Local') {
                    bat """
                        mvn sonar:sonar ^
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} ^
                        -Dsonar.projectName="${APP_NAME}" ^
                         -Dsonar.java.source=${JAVA_VERSION} ^
                        -Dsonar.projectVersion=${BUILD_VERSION}
                    """
                }
                echo 'Analyse SonarQube terminee'
            }
        }

        stage('Quality Gate') {
            steps {
                echo '================================================'
                echo 'ETAPE 4 : Verification du Quality Gate'
                echo '================================================'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
                echo 'Quality Gate passe avec succes'
            }
        }

        stage('Archive') {
            steps {
                echo '================================================'
                echo 'ETAPE 5 : Archivage des artefacts'
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
            echo "Qualite du code : VALIDE"
            echo '================================================'
            // On utilise seulement le tokenCredentialId, le reste est auto
            slackSend(
                    tokenCredentialId: 'slack-token',
                    channel: '#jenkins-builds',
                    color: 'good',
                    message: "✅ Build SUCCESS : ${APP_NAME} #${env.BUILD_NUMBER} - <${env.BUILD_URL}|Détails>"
            )
        }

        failure {
            echo '================================================'
            echo 'PIPELINE ECHOUE !'
            echo "Build #${env.BUILD_NUMBER} a echoue"
            echo 'Cause possible : Quality Gate non respecte'
            echo'='
            echo '================================================'
            slackSend(
                    tokenCredentialId: 'slack-token',
                    channel: '#jenkins-builds',
                    color: 'danger',
                    message: "❌ Build FAILED : ${APP_NAME} #${env.BUILD_NUMBER} - <${env.BUILD_URL}console|Logs>"
            )
        }

        always {
            echo '================================================'
            echo 'NETTOYAGE DU WORKSPACE'
            echo '================================================'
            cleanWs()
        }
    }
}
