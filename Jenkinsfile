pipeline {
    agent any

    // Ajout de paramètres pour choisir le registry
    parameters {
        choice(
                name: 'REGISTRY_TARGET',
                choices: ['both', 'dockerhub', 'nexus'],
                description: 'Où publier l\'image Docker ?'
        )
        // Voici ton bouton Manual Gate !
        booleanParam(
                name: 'DEPLOY_APP',
                defaultValue: false,
                description: 'Cochez pour déployer (RUN) l\'application sur le port 8084'
        )
    }

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

        // Registry Nexus
        REPOSITORY_DOCKER = 'docker-private'
        NEXUS_REGISTRY = 'localhost:8082'
        NEXUS_IMAGE = "${NEXUS_REGISTRY}/${APP_NAME}"

        //Name Container
        CONTAINER_NAME = "${APP_NAME}-container-${GIT_COMMIT_SHORT}"
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
            when {
                expression { params.REGISTRY_TARGET == 'dockerhub' || params.REGISTRY_TARGET == 'both' }
            }
            steps {
                echo '================================================'
                echo 'ETAPE 6 : Publication sur Docker Hub'
                echo '================================================'
                script {
                    // On utilise l'ID que tu as créé dans Jenkins
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_HUB_PASSWORD', usernameVariable: 'DOCKER_HUB_USER')]) {
                        // 1. Login
                        bat "docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}"
                        // 2. Push
                        bat "docker push ${DOCKER_IMAGE}:${IMAGE_TAG}"
                        bat "docker push ${DOCKER_IMAGE}:latest"
                        // 3. Logout (Sécurité)
                        bat "docker logout"
                    }
                }
                echo "Image publiee sur Docker Hub: ${DOCKER_IMAGE}:${IMAGE_TAG}"
            }
        }
        stage('Push to Nexus Registry') {
            when {
                expression { params.REGISTRY_TARGET == 'nexus' || params.REGISTRY_TARGET == 'both' }
            }
            steps {
                echo '================================================'
                echo 'ETAPE 7 : Publication sur Nexus (Registry Prive)'
                echo '================================================'
                script {
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PWD', usernameVariable: 'NEXUS_USER')]) {
                        // 1. Tag
                        bat "docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${NEXUS_IMAGE}:${IMAGE_TAG}"
                        bat "docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${NEXUS_IMAGE}:latest"
                        // 2. Login (Préciser le Registry est crucial ici)
                        bat "docker login ${NEXUS_REGISTRY} -u ${NEXUS_USER} -p ${NEXUS_PWD}"
                        // 3. Push
                        bat "docker push ${NEXUS_IMAGE}:${IMAGE_TAG}"
                        bat "docker push ${NEXUS_IMAGE}:latest"
                        // 4. Logout
                        bat "docker logout ${NEXUS_REGISTRY}"
                    }
                }
                echo "✅ Image publiee: ${NEXUS_IMAGE}:${IMAGE_TAG}"
            }
        }

        stage('Archive') {
            steps {
                echo '================================================'
                echo 'ETAPE 8 : Archivage des artefacts'
                echo '================================================'
                archiveArtifacts artifacts: 'target/*.jar',
                        fingerprint: true,
                        allowEmptyArchive: false
                echo 'Artefacts archives dans Jenkins'
            }
        }

        stage('Deploy to Test') {
            when {
                expression { params.DEPLOY_APP == true }
            }
            steps {
                echo '================================================'
                echo 'ETAPE 9 : Déploiement (Run) avec Secrets'
                echo '================================================'
                script {
                    // 1. On récupère le fichier secret du coffre-fort Jenkins et du Nexus(Authentication Barrier)
                    withCredentials([file(credentialsId: 'backend-prod-secrets', variable: 'SECRET_ENV'),
                                     usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PWD', usernameVariable: 'NEXUS_USER')]) {

                        // 2. On stoppe l'ancien conteneur s'il existe
                        bat "docker rm -f java-products-container || true"
                        bat "docker rm -f ${CONTAINER_NAME} || true"

                        // 3.  On supprime l'image locale pour obliger le PULL depuis Nexus
                        bat "docker rmi ${NEXUS_IMAGE}:latest || true"

                        // 4. RE-CONNEXION au Nexus pour le Pull
                        bat "docker login ${NEXUS_REGISTRY} -u ${NEXUS_USER} -p ${NEXUS_PWD}"


                        // 5. On lance le nouveau avec le fichier secret (--env-file)
                        // %SECRET_ENV% est le chemin temporaire du fichier créé par Jenkins
                        bat "docker run -d -p 8084:8084 --name ${CONTAINER_NAME} --env-file \"${SECRET_ENV}\" ${NEXUS_IMAGE}:latest"

                        // 6. Logout (Optionnel ici car le bloc always le fera, mais plus propre)
                        bat "docker logout ${NEXUS_REGISTRY}"
                    }
                }
                echo '✅ Application déployée sur http://localhost:8084'
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
            echo "Docker Hub: ${DOCKER_IMAGE}:${IMAGE_TAG}"
            echo "Nexus: ${NEXUS_IMAGE}:${IMAGE_TAG}"
            echo '================================================'

            slackSend(
                    color: 'good',
                    message: """
                        ✅ *Build SUCCESS* : ${APP_NAME} #${BUILD_VERSION}
                        📦 *Image Docker* : \\`${DOCKER_IMAGE}:${IMAGE_TAG}\\`
                        🔗 *Docker Hub* : https://hub.docker.com/r/${DOCKERHUB_USERNAME}/${APP_NAME}
                        🔒 *Nexus* : http://localhost:8081/#browse/browse:${REPOSITORY_DOCKER}:v2%2F${APP_NAME}
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
            echo 'ETAPE 10 : Nettoyage Automatique (Housekeeping)'
            echo '================================================'
            script {
                // 1. Supprime les tags spécifiques
                bat "docker rmi ${DOCKER_IMAGE}:${IMAGE_TAG} || true"
                bat "docker rmi ${NEXUS_IMAGE}:${IMAGE_TAG} || true"

                // 2. On supprime AUSSI les tags 'latest' qui bloquent le nettoyage
                bat "docker rmi ${DOCKER_IMAGE}:latest || true"
                bat "docker rmi ${NEXUS_IMAGE}:latest || true"


                // 3. Le coup de grâce : supprime tout ce qui n'est pas utilisé ou les couches devenues orphelines
                bat "docker image prune -f"
            }
            // Nettoyage de l'espace de travail Jenkins (fichiers sources, jar)
            cleanWs()
            echo '✅ Nettoyage terminé.'
        }
    }
}
