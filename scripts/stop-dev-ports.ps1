param(
    [int[]]$Ports = @(5174, 8080)
)

$connections = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object { $Ports -contains $_.LocalPort } |
    Select-Object -Property LocalPort, OwningProcess -Unique

if (-not $connections) {
    Write-Host "No listening dev processes found on ports: $($Ports -join ', ')"
    exit 0
}

foreach ($connection in $connections) {
    try {
        $process = Get-Process -Id $connection.OwningProcess -ErrorAction Stop
        Write-Host "Stopping $($process.ProcessName) (PID $($process.Id)) on port $($connection.LocalPort)..."
        Stop-Process -Id $process.Id -Force -ErrorAction Stop
    } catch {
        Write-Warning "Could not stop PID $($connection.OwningProcess) on port $($connection.LocalPort): $($_.Exception.Message)"
    }
}
