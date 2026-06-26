# =====================================================
# pre-commit 钩子（PowerShell 版）：检查敏感文件不含真实密码/密钥
# 用法：
#   1. 在 PowerShell 跑一次安装：
#      Copy-Item scripts\pre-commit-check.ps1 .git\hooks\pre-commit
#   2. 以后每次 git commit 前自动跑
# =====================================================

$ErrorActionPreference = "Stop"

# 检测 application-dev.yml 是否含真实密码
$yml = "spring-boot-backend\src\main\resources\application-dev.yml"
if (Test-Path $yml) {
    $content = Get-Content $yml -Raw
    # 匹配 "password: <非占位符值>" —— 占位符是 ${DB_PASSWORD:xxx}
    if ($content -match "password:\s*[^$\{]\S+") {
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "[!] 检测到 application-dev.yml 含真实密码" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "修复：把 password 改回占位符" -ForegroundColor Yellow
        Write-Host "  password: Xy@20070831   -->   password: `$" + "{DB_PASSWORD:root}" -ForegroundColor Yellow
        Write-Host ""
        exit 1
    }
}

# 检测 .env 是否含真实密钥
foreach ($envfile in @("vue-3d\.env", "vue-3d\.env.production", "vue-3d\.env.development")) {
    if (Test-Path $envfile) {
        $content = Get-Content $envfile -Raw
        if ($content -match "=(sk-|pk-|AKIA)|secret[_-]?key") {
            Write-Host "[!] $envfile 检测到可能的密钥" -ForegroundColor Red
            exit 1
        }
    }
}

# 检测 .env.local / application-local.yml（应该被 .gitignore 屏蔽）
foreach ($f in @("vue-3d\.env.local", "vue-3d\.env.*.local", "spring-boot-backend\src\main\resources\application-local.yml")) {
    if (Test-Path $f) {
        Write-Host "[!] $f 不应该提交到 git" -ForegroundColor Red
        exit 1
    }
}

exit 0
