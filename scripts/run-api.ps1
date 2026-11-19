$ErrorActionPreference = 'Stop'
Get-Content -LiteralPath '.env' | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
    $parts = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), 'Process')
}
mvn spring-boot:run
