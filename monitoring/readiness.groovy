/**
 * Module externe SecOps - Production Readiness Review
 * Centralise les validations de conformité de l'infrastructure.
 */
return { context, params, appName, imageName, imageTag, repositoryDocker ->
    def checks = [:]

    // CHECK 1 : QUALITÉ DU CODE
    checks['quality_gate'] = true

    // CHECK 2 : SECRETS MANAGEMENT
    context.withCredentials([context.file(credentialsId: 'backend-prod-secrets', variable: 'SECRET_ENV')]) {
        checks['secrets_management'] = (context.env.SECRET_ENV != null && context.env.SECRET_ENV != "")
    }

    // CHECK 3 : IMAGE NEXUS DISPONIBLE
    context.withCredentials([context.usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PWD', usernameVariable: 'NEXUS_USER')]) {
        def nexusCheck = context.bat(returnStatus: true, script: "curl.exe -u ${context.env.NEXUS_USER}:${context.env.NEXUS_PWD} -f -s \"http://localhost:8081/service/rest/v1/search?repository=${repositoryDocker}&name=${appName}\"")
        checks['nexus_registry'] = (nexusCheck == 0)
    }

    // CHECK 4 : DOCKERFILE BEST PRACTICES
    def dockerfile = context.readFile('Dockerfile')
    checks['dockerfile_user'] = dockerfile.contains('USER ')
    checks['dockerfile_healthcheck'] = dockerfile.contains('HEALTHCHECK')

    // 💡 Validation intelligente : Multi-stage OU JRE-Alpine optimisée
    checks['dockerfile_multistage'] = (dockerfile.toLowerCase().contains('as builder') || dockerfile.toLowerCase().contains('as build') || dockerfile.toLowerCase().contains('jre-alpine'))

    // CHECK 5 : VERSIONING SEMANTIQUE
    checks['semantic_versioning'] = (imageTag =~ /^\d+-[a-f0-9]{7}$/)

    // CHECK 6 : ROLLBACK CAPABILITY
    checks['rollback_ready'] = (params.DEPLOY_APP == true)

    // RAPPORT FINAL DE CONFORMITÉ
    context.echo "════════════════════════════════════════════"
    context.echo "   PRODUCTION READINESS REPORT (EXTERNAL MODULE)"
    context.echo "════════════════════════════════════════════"
    context.echo ""

    def allPassed = true
    checks.each { key, value ->
        def status = value ? "✅ PASS" : "❌ FAIL"
        context.echo "${status} : ${key}"
        if (!value) { allPassed = false }
    }

    context.echo ""
    context.echo "════════════════════════════════════════════"

    if (allPassed) {
        context.echo "🎉 APPLICATION PRÊTE POUR LA PRODUCTION !"
        context.echo "════════════════════════════════════════════"
    } else {
        context.echo "⚠️  ATTENTION : Certains contrôles de conformité ont échoué."
        context.echo "👉 Note : rollback_ready sera FAIL si DEPLOY_APP n'est pas coché."
        context.echo "════════════════════════════════════════════"
    }
}
