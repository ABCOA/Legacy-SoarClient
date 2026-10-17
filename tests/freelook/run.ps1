param([string]$JavaHome = 'C:\Program Files\Java\jdk-1.8')
$ErrorActionPreference = 'Stop'
$freelookProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$env:JAVA_HOME = $JavaHome
Push-Location $freelookProjectRoot
try {
    & ./gradlew.bat -I tests/classpath.gradle compileJava writeTestClasspath --offline --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Client compilation failed' }
    $freelookClasspath = [IO.File]::ReadAllText((Join-Path $freelookProjectRoot 'build/qa/classpath.txt')).Trim()
    New-Item -ItemType Directory -Force -Path build/freelook-qa | Out-Null
    & (Join-Path $JavaHome 'bin/javac.exe') -encoding UTF-8 -proc:none -cp $freelookClasspath -d build/freelook-qa tests/freelook/FreelookSmoke.java tests/freelook/KeybindConflictsSmoke.java
    if ($LASTEXITCODE -ne 0) { throw 'Freelook test compilation failed' }
    Push-Location build/freelook-qa
    try {
        & (Join-Path $JavaHome 'bin/java.exe') -cp ((Get-Location).Path + ';' + $freelookClasspath) FreelookSmoke
        if ($LASTEXITCODE -ne 0) { throw 'Freelook tests failed' }
        & (Join-Path $JavaHome 'bin/java.exe') -cp ((Get-Location).Path + ';' + $freelookClasspath) KeybindConflictsSmoke
        if ($LASTEXITCODE -ne 0) { throw 'Keybind conflict tests failed' }
    } finally { Pop-Location }
} finally { Pop-Location }
