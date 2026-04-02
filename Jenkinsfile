pipeline {
    agent any

    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
    }

    tools {
        jdk 'JDK17'
        maven 'MAVEN_3.6.3'
    }

    environment {
        // Application
        APP_NAME = 'java-products-lab'
        JAVA_VERSION = '17'

        // Versioning
        BUILD_VERSION = "${env.BUILD_NUMBER}"
        GIT_COMMIT_SHORT = "${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        IMAGE_TAG = "${BUILD_VERSION}-${GIT_COMMIT_SHORT}"

        // SonarQube
        SONAR_PROJECT_KEY = 'Java-Products-Lab'

        // Docker Hub
        DOCKERHUB_USERNAME = 'nyrdi'
        DOCKER_IMAGE = "${DOCKERHUB_USERNAME}/${APP_NAME}"
        DOCKER_BUILDKIT = '0'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '================================================'
                echo 'ETAPE 1 : Recuperation du code source'
                echo '================================================'
                checkout scm
                echo "Branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT}"
                echo 'Code récupère avec succès depuis GitHub'
            }
        }

        stage('Build Maven') {
            steps {
                echo '================================================'
                echo 'ETAPE 2 : Compilation et Packaging Maven'
                echo '================================================'
                bat 'mvn clean package '
                echo 'Build Maven termine avec succès'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '================================================'
                echo 'ETAPE 3 : Analyse de la qualité du code'
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
                echo 'Analyse SonarQube terminée'
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
                echo 'Quality Gate passe avec succès'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '================================================'
                echo 'ETAPE 5 : Construction de l image Docker'
                echo '================================================'
                script {
                    echo "Image: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                    bat "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} ."
                    bat "docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${DOCKER_IMAGE}:latest"
                }
                echo 'Image Docker construite avec succès'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                echo '================================================'
                echo 'ETAPE 6 : Publication sur Docker Hub'
                echo '================================================'
                script {
                    // On utilise l'ID que tu as créé dans Jenkins
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
                        // 1. Login
                        bat "docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}"

                        // 2. Push  .
                        bat "docker push ${DOCKER_IMAGE}:${IMAGE_TAG}"
                        bat "docker push ${DOCKER_IMAGE}:latest"

                        // 3. Logout (Sécurité) - Ferme la porte à clé derrière toi. 🔑
                        bat "docker logout"
                    }
                }
                echo "Image publiee sur Docker Hub: ${DOCKER_IMAGE}:${IMAGE_TAG}"
            }
        }

        stage('Archive') {
            steps {
                echo '================================================'
                echo 'ETAPE 7 : Archivage des artefacts'
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
            echo "Build #${BUILD_VERSION} termine avec succes"
            echo "Image Docker: ${DOCKER_IMAGE}:${IMAGE_TAG}"
            echo "Qualite du code: VALIDE"
            echo '================================================'

            slackSend(
                    color: 'good',
                    message: """
                        ✅ *Build SUCCESS* : ${APP_NAME} #${BUILD_VERSION}
                        📦 *Image Docker* : \\`${DOCKER_IMAGE}:${IMAGE_TAG}\\`
                        🔗 *Docker Hub* : https://hub.docker.com/r/${DOCKERHUB_USERNAME}/${APP_NAME}
                        📊 *Branche* : ${env.GIT_BRANCH}
                        👤 *Commit* : ${GIT_COMMIT_SHORT}
                        ⏱️ *Durée* : ${currentBuild.durationString}
                        🔗 <${env.BUILD_URL}|Voir les détails>
                    """.stripIndent()
            )
        }

        failure {
            echo '================================================'
            echo 'PIPELINE ÉCHOUE !'
            echo "Build #${BUILD_VERSION} a echoue"
            echo 'Consultez les logs pour plus de details'
            echo '================================================'

            slackSend(
                    color: 'danger',
                    message: """
                        ❌ *Build FAILED* : ${APP_NAME} #${BUILD_VERSION}
                        📦 *Branche* : ${env.GIT_BRANCH}
                        ⚠️ *Stage echoue* : ${env.STAGE_NAME}
                        🔗 <${env.BUILD_URL}console|Voir les logs>
                    """.stripIndent()
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
