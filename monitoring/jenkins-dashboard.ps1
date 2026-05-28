# Script de monitoring des builds Jenkins
# Usage : .\jenkins-dashboard.ps1

$JENKINS_URL = "http://localhost:8080"
$JOB_NAME = "java-products-lab-project/java-products-lab"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   JENKINS PIPELINE DASHBOARD (SRE)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$totalDuration = 0
$successCount = 0

Write-Host "Analyse des builds archives en cours..." -ForegroundColor Yellow
Write-Host ""

# Boucle linéaire de 83 à 100 pour ignorer le build corrompu #82
for ($buildNum = 83; $buildNum -le 100; $buildNum++) {
    $metricsPath = "C:\Users\ericn\.jenkins\jobs\java-products-lab-project\jobs\java-products-lab\builds\$buildNum\archive\build-metrics.json"

    if (Test-Path $metricsPath) {
        $metrics = Get-Content $metricsPath -Raw | ConvertFrom-Json

        if ($metrics) {
            $successCount++
            $totalDuration += $metrics.duration_seconds

            # Affichage unitaire sécurisé avec les bonnes clés de ton JSON
            Write-Host "Build #$buildNum : OK (Auteur: $($metrics.author))" -ForegroundColor Green
            Write-Host "  -> Duree Totale      : $($metrics.duration_seconds)s" -ForegroundColor Gray
            Write-Host "  -> Taille du JAR     : $($metrics.artifacts.jar_size_mb) MB" -ForegroundColor Gray
            Write-Host "  -> Image Docker      : $($metrics.artifacts.docker_image_size)" -ForegroundColor Gray
            Write-Host "  -> Fichiers modifies : $($metrics.git.changed_files)" -ForegroundColor Gray
            Write-Host "  -> Target Registry   : $($metrics.metadata.registry_target)" -ForegroundColor Gray
            Write-Host ""
        }
    }
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   STATISTIQUES GLOBALES" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

if ($successCount -gt 0) {
    $avg = [math]::Round($totalDuration / $successCount, 2)
    Write-Host "Duree moyenne des builds  : $avg secondes" -ForegroundColor Yellow
    Write-Host "Total des succes analyses : $successCount" -ForegroundColor Green
} else {
    Write-Host "Aucun fichier de metriques archive n'a ete trouve." -ForegroundColor Orange
}
Write-Host ""
