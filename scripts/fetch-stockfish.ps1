param([string]$Version = "sf_19")

$ErrorActionPreference = "Stop"
$repository = "official-stockfish/Stockfish"
$temporary = Join-Path ([System.IO.Path]::GetTempPath()) "chessmind-stockfish-$Version"
$targets = @(
    @{ Pattern = "stockfish-android-arm64-universal.tar.gz"; Folder = "arm64-v8a"; Binary = "stockfish-android-arm64-universal" },
    @{ Pattern = "stockfish-android-armv7-neon.tar.gz"; Folder = "armeabi-v7a"; Binary = "stockfish-android-armv7-neon" }
)

New-Item -ItemType Directory -Path $temporary -Force | Out-Null
foreach ($target in $targets) {
    $archive = Join-Path $temporary $target.Pattern
    gh release download $Version --repo $repository --pattern $target.Pattern --dir $temporary --clobber
    $extract = Join-Path $temporary $target.Folder
    New-Item -ItemType Directory -Path $extract -Force | Out-Null
    tar -xzf $archive -C $extract
    $destination = Join-Path $PSScriptRoot "..\app\src\main\jniLibs\$($target.Folder)\libstockfish.so"
    New-Item -ItemType Directory -Path (Split-Path $destination) -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $extract "stockfish\$($target.Binary)") -Destination $destination -Force
}

Write-Host "Stockfish $Version Android binaries are ready."
