$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$javaHome = "C:\pleiades\2025-09_Java\java\21"
$localMaven = Join-Path $projectRoot ".tools\apache-maven-3.9.16\bin\mvn.cmd"
$mavenUserHome = Join-Path $projectRoot ".tools\m2"
$mavenArgs = @("-Dmaven.repo.local=$mavenUserHome\repository", "test")

$env:MAVEN_USER_HOME = $mavenUserHome

if (Test-Path $javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

if (Test-Path $localMaven) {
    & $localMaven @mavenArgs
    exit $LASTEXITCODE
}

$pathMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($pathMaven) {
    & $pathMaven.Source @mavenArgs
    exit $LASTEXITCODE
}

& (Join-Path $projectRoot "mvnw.cmd") @mavenArgs
exit $LASTEXITCODE
