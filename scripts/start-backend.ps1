# =====================================================
# 启动后端（PowerShell 5.1 兼容）
# 双击此文件即可启动后端
# =====================================================

# 0. 自动杀掉已占 8080 端口的旧后端（避免端口冲突）
$old = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($old) {
  Write-Host "[清理] 旧后端进程 PID=$($old.OwningProcess) 正在停止..." -ForegroundColor Yellow
  Stop-Process -Id $old.OwningProcess -Force -ErrorAction SilentlyContinue
  Start-Sleep -Seconds 3
}

# 1. 把 mvn 加到 PATH（IDEA 自带的 Maven）
$env:Path = "D:\Users\summer\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin;" + $env:Path

# 2. 设置数据库密码（从环境变量读；不写明文到脚本里）
#    用法：先在系统环境变量里设 DB_PASSWORD，或者双击运行前手动设
if (-not $env:DB_PASSWORD) {
    Write-Host "[提示] \$env:DB_PASSWORD 未设置，使用 application-dev.yml 里的 ${DB_PASSWORD:root} 占位符" -ForegroundColor Yellow
}

# 3. 进项目目录
Set-Location "D:\summer\3D-Printing-Management-System\spring-boot-backend"

# 4. 验证 mvn 可用
Write-Host "=== 验证 Maven ===" -ForegroundColor Cyan
mvn -version | Select-Object -First 2

# 5. 启动后端
Write-Host ""
Write-Host "=== 启动后端（按 Ctrl+C 停止）===" -ForegroundColor Green
Write-Host ""
mvn spring-boot:run
