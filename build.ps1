#Requires -Version 7.0
# vanilla-permissions-forge build script (PowerShell 7+, not Windows PowerShell 5)
[CmdletBinding()]
param(
    [string]$JavaHome,
    [string]$Proxy = 'http://127.0.0.1:7897'
)

$ErrorActionPreference = 'Stop'
$repo = $PSScriptRoot

if (-not $JavaHome) {
    $candidate = Join-Path (Split-Path $repo -Parent) 'graalvm-jdk-17'
    if (Test-Path (Join-Path $candidate 'bin/java.exe')) {
        $JavaHome = $candidate
    } elseif ($env:JAVA_HOME) {
        $JavaHome = $env:JAVA_HOME
    } else {
        throw 'Java 17 not found. Pass -JavaHome <path> or set JAVA_HOME.'
    }
}

$env:JAVA_HOME = $JavaHome
Write-Host "Using JDK: $JavaHome"

if ($Proxy) {
    $u = [Uri]$Proxy
    $env:GRADLE_OPTS = "-Dhttps.proxyHost=$($u.Host) -Dhttps.proxyPort=$($u.Port) -Dhttp.proxyHost=$($u.Host) -Dhttp.proxyPort=$($u.Port) -Dnet.minecraftforge.gradle.check.certs=false"
    Write-Host "Using proxy: $Proxy"
}

Push-Location $repo
try {
    & .\gradlew.bat build
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }
} finally {
    Pop-Location
}

Get-ChildItem (Join-Path $repo 'build/libs') -Filter '*.jar' |
    ForEach-Object { Write-Host "Artifact: $($_.FullName) ($([math]::Round($_.Length / 1KB)) KB)" }