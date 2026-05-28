// Variables globales Groovy dynamiques pour le suivi SRE (Chronomètres)
import groovy.transform.Field
@Field String timeCheckout = '0'
@Field String timeMaven = '0'
@Field String timeSonar = '0'
@Field String timeQualityGate = '0'
@Field String timeDocker = '0'
@Field String timeDockerHUB = '0'
@Field String timePushOnNexus = '0'
@Field String timeDeploy = '0'

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

        // Configuration Blue/Green
        BLUE_PORT = '8084'
        GREEN_PORT = '8085'
        BLUE_CONTAINER = "${APP_NAME}-blue"
        GREEN_CONTAINER = "${APP_NAME}-green"
        HEALTH_ENDPOINT = '/actuator/health'

        // Configuration du Monitoring Grafana & Résilience
        METRICS_FILE = 'build-metrics.json'
        MAX_HEALTH_RETRIES = '12'

        // Chronomètres des étapes (Initialisés à 0)
       // TIME_CHECKOUT = '0'
       // TIME_MAVEN = '0'
       // TIME_SONAR = '0'
       // TIME_DOCKER = '0'
       // TIME_QUALITY_GATE = '0'
       // TIME_DOCKER_HUB = '0'
       // TIME_PUSH_ON_NEXUS = '0'
        // TIME_DEPLOY = '0'

    }
    stages {
        stage('Checkout') {
            steps {
                echo '================================================'
                echo 'ETAPE 1 : Recuperation du code source'
                echo '================================================'
                script {
                    // 1. On capture l'heure de départ (en millisecondes)
                    double startTime = System.currentTimeMillis()

                    // 2. Recuperation du code source (sur GitHub)
                    checkout scm
                    echo "Branch: ${env.GIT_BRANCH}"
                    echo "Commit: ${env.GIT_COMMIT}"
                    echo 'Code récupère avec succès depuis GitHub'

                    // 3. On capture l'heure de fin
                    double endTime = System.currentTimeMillis()
                    timeCheckout =  String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps d'exécution du Checkout : ${timeCheckout}s"
                }
            }
        }


        stage('Build Maven') {
            steps {
                echo '================================================'
                echo 'ETAPE 2 : Compilation et Packaging Maven'
                echo '================================================'
                script {
                    // 1. Départ du chronomètre Maven
                    long startTime = System.currentTimeMillis()

                    // 2. Compilation et Packaging Maven
                    bat 'mvn clean package '
                    echo 'Build Maven termine avec succès'

                    // 3. Fin du chronomètre
                    long endTime = System.currentTimeMillis()
                    // 4. On écrase le '0' de TIME_MAVEN par la durée réelle
                    timeMaven = String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps d'exécution du Build Maven : ${timeMaven}s"
                }
            }
        }


        stage('SonarQube Analysis') {
            steps {
                echo '================================================'
                echo 'ETAPE 3 : Analyse de la qualité du code'
                echo '================================================'
                script {
                    // 1. Départ du chronomètre SonarQube
                    long startTime = System.currentTimeMillis()
                    //Analyse de la qualité du code
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
                        // 3. Fin du chronomètre
                        long endTime = System.currentTimeMillis()
                    timeSonar = String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps d'exécution de SonarQube : ${timeSonar}s"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '================================================'
                echo 'ETAPE 4 : Verification du Quality Gate'
                echo '================================================'
                script {
                    long startTime = System.currentTimeMillis()
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                    echo 'Quality Gate passe avec succès'

                    long endTime = System.currentTimeMillis()
                    // On écrase le '0' de TIME_QUALITY_GATE par la durée réelle d'attente
                    timeQualityGate = String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps d'attente du Quality Gate : ${timeQualityGate}s"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '================================================'
                echo 'ETAPE 5 : Construction de l image Docker'
                echo '================================================'
                script {
                    long startTime = System.currentTimeMillis()

                    echo "Image: ${DOCKER_IMAGE}:${env.IMAGE_TAG}"
                    bat "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} ."
                    bat "docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${DOCKER_IMAGE}:latest"

                    long endTime = System.currentTimeMillis()
                    timeDocker = String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps d'exécution du Build Docker : ${timeDocker}s"
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
                    long startTime = System.currentTimeMillis()
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
                    long endTime = System.currentTimeMillis()
                    timeDockerHUB =String.valueOf((endTime - startTime) / 1000)
                            echo "⏱️ Temps d'exécution du Build Docker  Hub : ${timeDockerHUB}s"
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
                    long startTime = System.currentTimeMillis()
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

                    long endTime = System.currentTimeMillis()
                    timePushOnNexus = String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps d'exécution du publication sur Nexus : ${timePushOnNexus}s"
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

        stage('Deploy with Rollback') {
            when {
                expression { params.DEPLOY_APP == true }
            }
            steps {
                echo '================================================'
                echo 'ETAPE 9 : Déploiement Blue/Green avec Rollback Automatique'
                echo '================================================'
                script {
                    // 💡 1. DÉPART DU CHRONOMÈTRE DE DÉPLOIEMENT
                    long startTime = System.currentTimeMillis()

                    // ════════════════════════════════════════════
                    // PHASE 1 : DÉTECTION ET AIGUILLAGE AUTOMATIQUE
                    // ════════════════════════════════════════════
                    // Appel de la méthode locale pour obtenir la configuration cible
                    def envConfig = detectEnvironment(BLUE_CONTAINER, GREEN_CONTAINER, BLUE_PORT, GREEN_PORT)

                    echo "🎯 Environnement cible détecté : ${envConfig.target.toUpperCase()} (Port externe: ${envConfig.port})"

                    // ════════════════════════════════════════════
                    // PHASE 2 & 3 : AUTHENTIFICATION, VALIDATION NEXUS ET RUN DYNAMIQUE
                    // ════════════════════════════════════════════

                    // 1. On récupère le fichier secret du coffre-fort Jenkins et du Nexus(Authentication Barrier)
                    withCredentials([file(credentialsId: 'backend-prod-secrets', variable: 'SECRET_ENV'),
                                     usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PWD', usernameVariable: 'NEXUS_USER')]) {

                        // 2. Nettoyage chirurgical : On supprime UNIQUEMENT le conteneur cible pour ne pas couper la prod actuelle
                        bat "docker rm -f ${envConfig.container} || true"

                        // 3.  On supprime l'image locale pour obliger le PULL depuis Nexus
                        bat "docker rmi ${NEXUS_IMAGE}:latest || true"

                        // 4. RE-CONNEXION au Nexus pour le Pull
                        bat "docker login ${NEXUS_REGISTRY} -u ${NEXUS_USER} -p ${NEXUS_PWD}"


                        // 5. On lance le nouveau avec le fichier secret (--env-file)
                        // %SECRET_ENV% est le chemin temporaire du fichier créé par Jenkins
                        // Lancement dynamique : On injecte le port et le nom calculés en Phase 1
                        echo "🚀 Lancement de l'instance sur la zone ${envConfig.target.toUpperCase()}..."
                        bat "docker run -d -p ${envConfig.port}:8084 --name ${envConfig.container} --env-file \"${SECRET_ENV}\" ${NEXUS_IMAGE}:latest"

                        // 6. Logout (Optionnel ici car le bloc always le fera, mais plus propre)
                        bat "docker logout ${NEXUS_REGISTRY}"
                    }

                    // ════════════════════════════════════════════
                    // PHASE 4 : HEALTHCHECK AUTOMATISÉ (SMOKE TEST)
                    // ════════════════════════════════════════════
                    // On appelle notre méthode locale sur le port temporaire de test (max 12 tentatives de 5s)
                    def isHealthy = runHealthcheck(envConfig.port, HEALTH_ENDPOINT, 12, 5)

                    // ════════════════════════════════════════════
                    // PHASE 5 : DÉCISION FINALE (CLEANUP OU ROLLBACK)
                    // ════════════════════════════════════════════
                    if (isHealthy) {
                        echo "════════════════════════════════════════════"
                        echo "✅ DÉPLOIEMENT RÉUSSI SUR L'ENVIRONNEMENT ${envConfig.target.toUpperCase()} !"
                        echo "════════════════════════════════════════════"

                        // Si une ancienne version tournait, on l'éteint proprement car la relève est assurée
                        if (envConfig.current != 'none') {
                            def oldContainer = (envConfig.current == 'blue') ? BLUE_CONTAINER : GREEN_CONTAINER
                            echo "🗑️ Suppression de l'ancienne instance de production devenue inutile : ${oldContainer}"
                            bat "docker rm -f ${oldContainer} || true"
                        }
                    } else {
                        echo "════════════════════════════════════════════"
                        echo "❌ ÉCHEC DU HEALTHCHECK - DÉCLENCHEMENT DU ROLLBACK AUTOMATIQUE"
                        echo "════════════════════════════════════════════"

                        // Extraction des 50 dernières lignes de logs pour comprendre pourquoi Spring Boot n'a pas démarré
                        def failedLogs = bat(returnStdout: true, script: "docker logs --tail 50 ${envConfig.container}").trim()
                        echo "📋 Extraits des logs d'erreur (Runtime Error) :\n${failedLogs}"

                        // Destruction immédiate de l'instance défaillante pour libérer les ressources
                        echo "🗑️ Nettoyage du conteneur cible en échec..."
                        bat "docker rm -f ${envConfig.container}"

                        // Transmission d'état intelligente vers le bloc 'post'
                        env.ROLLBACK_EXECUTED = 'true'
                        env.OLD_ENV_PRESERVED = envConfig.current.toUpperCase()
                        env.OLD_PORT_PRESERVED = (envConfig.current == 'blue') ? BLUE_PORT : GREEN_PORT

                        // On force la coupure du pipeline en erreur pour alerter l'équipe
                        error("Le déploiement a échoué. L'environnement stable d'origine (${envConfig.current}) a été maintenu.")
                    }
                    // 💡 2. FIN DU CHRONOMÈTRE DE DÉPLOIEMENT
                    long endTime = System.currentTimeMillis()
                    // 💡 3. ON ÉCRASE LE '0' DE TIME_DEPLOY PAR LA DURÉE RÉELLE
                    timeDeploy = String.valueOf((endTime - startTime) / 1000)
                    echo "⏱️ Temps total d'exécution du Déploiement/Rollback : ${timeDeploy}s"

                }
                echo "✅ Processus de déploiement et de vérification terminé."
            }
        }

        stage('Collect Metrics') {
            steps {
                echo '================================================'
                echo 'ETAPE 10 : Collection des Métriques Réelles'
                echo '================================================'
                script {
                    // 1. Calcul des métriques temporelles globales de Jenkins
                    long totalMillis = currentBuild.duration
                    long buildDuration = totalMillis / 1000
                    def buildStartTime = new Date(currentBuild.startTimeInMillis).format('yyyy-MM-dd HH:mm:ss')

                    // 2. Appel de nos fonctions d'infrastructure locales (Windows 11)
                    def jarSizeMB = getJarSizeMB()
                    def dockerSize = getDockerImageSize(DOCKER_IMAGE, IMAGE_TAG)
                    def changedFiles = getGitChangedFiles()
                    def commitAuthor = getGitAuthor() // 💡 Traçabilité de l'auteur

                    echo "⏱️ Durée totale réelle du pipeline : ${buildDuration}s"
                    echo "📦 Poids réel du fichier JAR : ${jarSizeMB} MB"
                    echo "🐳 Poids réel de l'image Docker : ${dockerSize}"
                    echo "📝 Nombre de fichiers modifiés : ${changedFiles}"
                    echo "👤 Auteur du commit : ${commitAuthor}"

                    // 3. Génération du payload JSON structuré pour Grafana
                    def metricsJson = """
                        {
                          "build_number": ${env.BUILD_VERSION},
                          "git_commit": "${env.GIT_COMMIT_SHORT}",
                          "branch": "${env.GIT_BRANCH}",
                          "author": "${commitAuthor}",
                          "duration_seconds": ${buildDuration},
                          "start_time": "${buildStartTime}",
                          "stage_durations": {
                            "checkout_ms": ${timeCheckout},
                            "maven_build_ms": ${timeMaven},
                            "sonarqube_ms": ${timeSonar},
                            "quality_gate_ms": ${timeQualityGate},
                            "docker_build_ms": ${timeDocker},
                            "docker_build_Hub_ms": ${timeDockerHUB},
                            "Push_on_NEXUS_ms": ${timePushOnNexus},
                            "deployment_ms": ${timeDeploy}
                          },
                          "artifacts": {
                            "jar_size_mb": ${jarSizeMB},
                            "docker_image_size": "${dockerSize}"
                          },
                          "git": {
                            "changed_files": ${changedFiles}
                          },
                          "metadata": {
                            "registry_target": "${params.REGISTRY_TARGET}",
                            "quality_gate": "PASSED",
                            "deploy_triggered": ${params.DEPLOY_APP}
                          }
                        }
                    """.trim()

                    // 4. Écriture physique et Archivage officiel de la donnée isolée
                    writeFile file: "${METRICS_FILE}", text: metricsJson
                    archiveArtifacts artifacts: "${METRICS_FILE}", fingerprint: true
                    echo "💾 Métriques sauvegardées avec succès dans l'artéfact ${METRICS_FILE}"
                }
            }
        }

        stage('Production Readiness Check') {
            steps {
                echo '================================================'
                echo 'ETAPE FINALE : Validation Production-Ready'
                echo '================================================'
                script {
                    def checks = [:]

                    // ════════════════════════════════════════════
                    // CHECK 1 : QUALITÉ DU CODE
                    // ════════════════════════════════════════════
                    checks['quality_gate'] = true // Validé en amont par ton étape 4

                    // ════════════════════════════════════════════
                    // CHECK 2 : SECRETS MANAGEMENT
                    // ════════════════════════════════════════════
                    // On vérifie nativement si la variable du coffre Jenkins est accessible
                    withCredentials([file(credentialsId: 'backend-prod-secrets', variable: 'SECRET_ENV')]) {
                        checks['secrets_management'] = (SECRET_ENV != null && SECRET_ENV != "")
                    }

                    // ════════════════════════════════════════════
                    // CHECK 3 : IMAGE NEXUS DISPONIBLE
                    // ════════════════════════════════════════════
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PWD', usernameVariable: 'NEXUS_USER')]) {
                        // Utilisation de curl.exe sur une seule ligne propre sans caractère d'échappement Linux
                        def nexusCheck = bat(returnStatus: true, script: "curl.exe -u ${NEXUS_USER}:${NEXUS_PWD} -f -s http://localhost:8081/service/rest/v1/search?repository=docker-private&name=${APP_NAME}")
                        checks['nexus_registry'] = (nexusCheck == 0)
                    }

                    // ════════════════════════════════════════════
                    // CHECK 4 : DOCKERFILE BEST PRACTICES
                    // ════════════════════════════════════════════
                    def dockerfile = readFile('Dockerfile')
                    checks['dockerfile_user'] = dockerfile.contains('USER ') // Non-root user pour la sécurité
                    checks['dockerfile_healthcheck'] = dockerfile.contains('HEALTHCHECK')
                    checks['dockerfile_multistage'] = dockerfile.contains('AS builder') // Ton optimisation Multi-stage

                    // ════════════════════════════════════════════
                    // CHECK 5 : VERSIONING SEMANTIQUE
                    // ════════════════════════════════════════════
                    checks['semantic_versioning'] = (IMAGE_TAG =~ /^\d+-[a-f0-9]{7}$/)

                    // ════════════════════════════════════════════
                    // CHECK 6 : ROLLBACK CAPABILITY
                    // ════════════════════════════════════════════
                    // Si l'option de déploiement est activée, l'infrastructure est parée pour le Rollback
                    checks['rollback_ready'] = (params.DEPLOY_APP == true)

                    // ════════════════════════════════════════════
                    // RAPPORT FINAL DE CONFORMITÉ
                    // ════════════════════════════════════════════
                    echo "════════════════════════════════════════════"
                    echo "   PRODUCTION READINESS REPORT"
                    echo "════════════════════════════════════════════"
                    echo ""

                    def allPassed = true
                    checks.each { key, value ->
                        def status = value ? "✅ PASS" : "❌ FAIL"
                        echo "${status} : ${key}"
                        if (!value) { allPassed = false }
                    }

                    echo ""
                    echo "════════════════════════════════════════════"

                    if (allPassed) {
                        echo "🎉 APPLICATION PRÊTE POUR LA PRODUCTION !"
                        echo "════════════════════════════════════════════"
                    } else {
                        echo "⚠️  ATTENTION : Certains contrôles de conformité ont échoué."
                        echo "👉 Vérifie tes pratiques Dockerfile ou tes configurations."
                        echo "════════════════════════════════════════════"
                    }
                }
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

            script {
                // 💡 Logique dynamique : On prépare la ligne de statut selon le paramètre de déploiement
                def deployStatut = "Image publiée (Aucun déploiement demandé)"

                if (params.DEPLOY_APP == true) {
                    // On peut même ré-interroger rapidement Docker pour afficher le conteneur actif dans Slack
                    def activeGreen = bat(returnStatus: true, script: "docker ps -q --filter name=${GREEN_CONTAINER}") == 0
                    def portActif = activeGreen ? GREEN_PORT : BLUE_PORT
                    def envActif = activeGreen ? "GREEN" : "BLUE"

                    deployStatut = "Déploiement validé sur l'environnement **${envActif}** (Port: ${portActif})"
                }
            slackSend(
                    color: 'good',
                    message: """
                        ✅ *Build SUCCESS* : ${APP_NAME} #${BUILD_VERSION}
                        🚀 *deploy Statut* : ${deployStatut}
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
        }

        failure {
            echo '================================================'
            echo 'PIPELINE ÉCHOUE !'
            echo "Build #${BUILD_VERSION} a echoue"
            echo 'Consultez les logs pour plus de details'
            echo '================================================'

            script {
                // 💡 On intercepte si l'échec vient d'un Rollback Automatique
                if (env.ROLLBACK_EXECUTED == 'true') {
                    echo "🔄 Notification : Alerte Déploiement Échoué avec Rollback Automatique envoyé à Slack."
                    slackSend(
                            color: 'warning', // Couleur Orange/Jaune pour signaler une sécurité activée
                            message: """
                                ⚠️ *DÉPLOIEMENT ÉCHOUÉ - ROLLBACK EFFECTUÉ*
                                📦 *Projet* : ${APP_NAME} #${BUILD_VERSION}
                                ❌ *Nouvelle Version* : Rejetée (Healthcheck KO)
                                ✅ *Production Préservée* : L'instance stable *${env.OLD_ENV_PRESERVED}* reste active sur le port *${env.OLD_PORT_PRESERVED}*
                                📊 *Branche* : ${env.GIT_BRANCH}
                                👤 *Commit incriminé* : ${GIT_COMMIT_SHORT}
                                🔗 <${env.BUILD_URL}console|Analyser les logs d'erreur sur Jenkins>
                            """.stripIndent()
                    )
                } else {
                    // Si l'échec arrive AVANT le déploiement (ex: Compilation Maven, SonarQube ou Push KO)
                    // On conserve l'erreur d'origine (Rouge)
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
            }
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

/**
 * PHASE 1 : DÉTECTION DE L'ENVIRONNEMENT ACTUEL
 * Analyse les conteneurs Docker présents pour déterminer la cible du déploiement.
 *
 * @param blueName       Nom du conteneur pour l'environnement BLUE
 * @param greenName      Nom du conteneur pour l'environnement GREEN
 * @param bluePort       Port d'écoute externe pour l'environnement BLUE
 * @param greenPort      Port d'écoute externe pour l'environnement GREEN
 * @return Map           Configuration dynamique [current, target, port, container]
 */
def detectEnvironment(blueName, greenName, bluePort, greenPort) {
    def blueExists = bat(returnStatus: true, script: "docker ps -q --filter name=${blueName}") == 0
    def greenExists = bat(returnStatus: true, script: "docker ps -q --filter name=${greenName}") == 0

    if (blueExists) {
        return [current: 'blue', target: 'green', port: greenPort, container: greenName]
    } else if (greenExists) {
        return [current: 'green', target: 'blue', port: bluePort, container: blueName]
    } else {
        return [current: 'none', target: 'blue', port: bluePort, container: blueName]
    }
}

/**
 * PHASE 4 : Healthcheck automatisé (Smoke Test) avec gestion des retries.
 * Interroge l'endpoint de l'application sur son port temporaire de test.
 *
 * @param port           Port temporaire à interroger (targetPort)
 * @param endpoint       Chemin de l'Actuator Health (/actuator/health)
 * @param maxRetries     Nombre maximal de tentatives de vérification
 * @param delaySeconds   Temps d'attente en secondes entre chaque tentative
 * @return Boolean       True si l'application répond avec un statut "UP", False sinon
 */
def runHealthcheck(port, endpoint, maxRetries, delaySeconds) {
    def healthy = false
    def count = 0

    echo "⏳ Attente du démarrage de l'application..."
    sleep(time: 10, unit: 'SECONDS') // Temps de chauffe initial requis par Spring Boot

    while (count < maxRetries && !healthy) {
        count++
        echo "🏥 Vérification de santé sur le port de test ${port} : tentative ${count}/${maxRetries}..."

        // Utilisation explicite de curl.exe pour l'environnement Windows 11
        def status = bat(returnStatus: true, script: "curl.exe -f -s http://localhost:${port}${endpoint}")

        if (status == 0) {
            def response = bat(returnStdout: true, script: "curl.exe -s http://localhost:${port}${endpoint}").trim()
            if (response.contains('"status":"UP"')) {
                healthy = true
                echo "✅ Healthcheck réussi !"
            }
        }
        if (!healthy && count < maxRetries) {
            sleep(time: delaySeconds, unit: 'SECONDS')
        }
    }
    return healthy
}

/**
 * PHASE 8 : Calcule la durée exacte entre deux repères temporels.
 *
 * @param startTime  Nombre entier long (System.currentTimeMillis())
 * @param endTime    Nombre entier long (System.currentTimeMillis())
 * @return String    Durée en secondes

def calculateStageDuration(startTime, endTime) {
    long start = (long) startTime
    long end = (long) endTime
    long diffSeconds = (end - start) / 1000
    return diffSeconds.toString()
}
 */


/**
 * PHASE 8 : Extrait la taille du fichier JAR via PowerShell et corrige la régionalisation.
 * Évite le piège de la virgule française (ex: 1,23 -> 1.23) pour sécuriser .toDouble().
 *
 * @return Double Taille en MB
 */
def getJarSizeMB() {
    // Étape native Jenkins pour trouver le fichier sur l'Agent
    def jarFile = powershell(returnStdout: true, script: "(Get-ChildItem target/*.jar | Select-Object -First 1).Name").trim()
    if (!jarFile) return 0.0

    // Récupération des octets bruts sur l'Agent
    def sizeStr = powershell(returnStdout: true, script: "(Get-Item target/${jarFile}).Length").trim()
    if (!sizeStr || !sizeStr.isNumber()) return 0.0

    double octets = sizeStr.toDouble()
    double megaBytes = octets / (1024.0 * 1024.0)

    return Math.round(megaBytes * 100.0) / 100.0
}



/**
 * PHASE 8 : Récupère le poids brut de l'image Docker construite.
 * Sécurisé via PowerShell pour éliminer le prompt CMD parasite (ex: "305MB").
 *
 * @param imageName  Nom de l'image (DOCKER_IMAGE)
 * @param imageTag   Tag unique (IMAGE_TAG)
 * @return String    Taille épurée de l'image
 */
def getDockerImageSize(imageName, imageTag) {
    def sizeStr = powershell(returnStdout: true, script: "docker images ${imageName}:${imageTag} --format '{{.Size}}'").trim()
    return sizeStr ? sizeStr.readLines().last().trim() : "0B"
}


/**
 * PHASE 8 : Compte le nombre de fichiers modifiés dans le dernier commit.
 * Évite les résidus de prompts CMD sous Windows en passant par PowerShell.
 *
 * @return Integer Nombre de fichiers
 */
def getGitChangedFiles() {
    def countStr = powershell(returnStdout: true, script: "(git diff --name-only HEAD~1 HEAD).Count").trim()

    // Si la commande renvoie du texte vide ou invalide (ex: premier commit), sécurité à 0
    if (!countStr || !countStr.isNumber()) return 0

    return countStr.toInteger()
}

/**
 * PHASE 8 : Récupère le nom de l'auteur du dernier commit Git.
 * Sécurisé via l'étape native powershell de Jenkins.
 *
 * @return String Nom de l'auteur
 */
def getGitAuthor() {
    return powershell(returnStdout: true, script: "git log -1 --format='%an'").trim()
}




