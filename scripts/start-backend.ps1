# =====================================================
# 启动后端（PowerShell 5.1 兼容）
# 双击此文件即可启动后端
# =====================================================

# 1. 把 mvn 加到 PATH（IDEA 自带的 Maven）
$env:Path = "D:\Users\summer\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin;" + $env:Path

# 2. 设置数据库密码（替换成你的 MySQL 真实密码）
$env:DB_PASSWORD = "Xy@20070831"

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
