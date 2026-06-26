# =====================================================
# 启动前端（双击即可）
# =====================================================

Set-Location "D:\summer\3D-Printing-Management-System\vue-3d"

# 1. 检查 node_modules
if (-not (Test-Path "node_modules")) {
    Write-Host "首次运行，需要安装依赖（可能 1-3 分钟）..." -ForegroundColor Yellow
    npm install
}

# 2. 启动
Write-Host ""
Write-Host "=== 启动前端（按 Ctrl+C 停止）===" -ForegroundColor Green
Write-Host "浏览器打开：http://localhost:5173" -ForegroundColor Cyan
Write-Host ""
npm run dev
