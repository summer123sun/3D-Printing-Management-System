# =====================================================
# 全模块接口测试（后端 D + E）
# 先启动后端：D:\summer\3D-Printing-Management-System\scripts\start-backend.ps1
# =====================================================

$base = "http://localhost:8080"
$pass = 0
$fail = 0
$results = @()

function Test-Api {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [string]$ContentType = "application/json",
        [int]$ExpectedCode = 200
    )
    $script:pass = 0
    $script:fail = 0
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = $ContentType
            ErrorAction = "Stop"
        }
        if ($Body) { $params.Body = ($Body | ConvertTo-Json -Depth 4) }

        $resp = Invoke-RestMethod @params
        if ($resp.code -eq $ExpectedCode) {
            Write-Host "  ✅ $Name (code=$($resp.code))" -ForegroundColor Green
            $script:pass++
            return $true
        } else {
            Write-Host "  ❌ $Name (expected=$ExpectedCode, got=$($resp.code))" -ForegroundColor Red
            $script:fail++
            return $false
        }
    } catch {
        Write-Host "  ❌ $Name (EXCEPTION: $($_.Exception.Message))" -ForegroundColor Red
        $script:fail++
        return $false
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   3D 打印科创会管理系统 - 全模块接口测试" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# =====================================================
# 第 1 步：登录拿 token
# =====================================================
Write-Host ""
Write-Host ">>> [准备] 登录 2023010001（社长）" -ForegroundColor Yellow
try {
    $loginResp = Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -Body (@{ studentId = "2023010001"; password = "123456" } | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
    $token = $loginResp.data.token
    $h = @{ Authorization = "Bearer $token" }
    Write-Host "  ✅ 登录成功，token len = $($token.Length)" -ForegroundColor Green
    Write-Host "  Authorization 头长度 = $($h.Authorization.Length)" -ForegroundColor Gray
} catch {
    Write-Host "  ❌ 登录失败：$($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先启动后端：scripts\start-backend.ps1" -ForegroundColor Yellow
    exit 1
}

# =====================================================
# 第 2 步：D 模块测试（基础设施 + auth + user + file）
# =====================================================
Write-Host ""
Write-Host ">>> [D 模块] 用户与基础设施" -ForegroundColor Yellow

Test-Api -Name "D1 /api/auth/info (我加的)" -Method "GET" -Url "$base/api/auth/info" -Headers $h
Test-Api -Name "D2 /api/user/info" -Method "GET" -Url "$base/api/user/info" -Headers $h
Test-Api -Name "D3 /api/user/list" -Method "GET" -Url "$base/api/user/list?page=1&size=10" -Headers $h
Test-Api -Name "D4 /api/user/2023010005/stats" -Method "GET" -Url "$base/api/user/2023010005/stats" -Headers $h

# =====================================================
# 第 3 步：E 模块测试（任务）
# =====================================================
Write-Host ""
Write-Host ">>> [E 模块] 打印任务" -ForegroundColor Yellow

# E1. 提交任务
$applyBody = @{
    title = "测试任务-前端B"
    modelName = "测试模型"
    stlFilePath = "/uploads/stl/test.stl"
    materialType = "PLA"
    color = "白色"
    layerHeight = 0.2
    infillRate = 20
    needSupport = 0
    priority = 2
    estWeight = 15.5
    estTime = 120
}
$submitted = Test-Api -Name "E1 POST /api/task (提交任务)" -Method "POST" -Url "$base/api/task" -Headers $h -Body $applyBody

if ($submitted -and $script:pass -gt 0) {
    # 重新登录 2023010005（刘洋）拿新 token，因为刚才 0001 提交的不会在他的列表里
    try {
        $login5 = Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -Body (@{ studentId = "2023010005"; password = "123456" } | ConvertTo-Json) -ContentType "application/json"
        $token5 = $login5.data.token   # ⚠️ 必须先提取到简单变量，不能直接放 hashtable
        $h5 = @{ Authorization = "Bearer $token5" }
        Write-Host "  ℹ 切换到 2023010005（刘洋），token len = $($token5.Length)" -ForegroundColor Gray

        Test-Api -Name "E2 GET /api/task/my (我的任务)" -Method "GET" -Url "$base/api/task/my?page=1&size=10" -Headers $h5
        Test-Api -Name "E3 GET /api/task/queue (打印队列)" -Method "GET" -Url "$base/api/task/queue" -Headers $h
        Test-Api -Name "E4 GET /api/task/pending (待审批)" -Method "GET" -Url "$base/api/task/pending" -Headers $h
        Test-Api -Name "E5 GET /api/task/stats (统计)" -Method "GET" -Url "$base/api/task/stats" -Headers $h
    } catch {
        Write-Host "  ❌ E2-E5 测试异常：$($_.Exception.Message)" -ForegroundColor Red
    }
}

# =====================================================
# 第 4 步：E 模块测试（项目）
# =====================================================
Write-Host ""
Write-Host ">>> [E 模块] 项目管理" -ForegroundColor Yellow

Test-Api -Name "E6 GET /api/project/list" -Method "GET" -Url "$base/api/project/list?page=1&size=10" -Headers $h

# E7. 创建一个项目
$projectBody = @{
    projectName = "测试项目-前端B"
    projectType = 2
    leaderId = "2023010002"
    startDate = "2026-07-01"
    endDate = "2026-08-30"
    description = "E2E 测试"
    stages = @(
        @{ stageName = "建模"; stageOrder = 1; description = "建模阶段" },
        @{ stageName = "打印"; stageOrder = 2; description = "打印阶段" }
    )
}
Test-Api -Name "E7 POST /api/project (创建项目)" -Method "POST" -Url "$base/api/project" -Headers $h -Body $projectBody

# =====================================================
# 第 5 步：文档
# =====================================================
Write-Host ""
Write-Host ">>> [辅助] Knife4j 接口文档" -ForegroundColor Yellow
try {
    $doc = Invoke-WebRequest -Uri "$base/doc.html" -UseBasicParsing -ErrorAction Stop
    Write-Host "  ✅ /doc.html HTTP $($doc.StatusCode)" -ForegroundColor Green
    $script:pass++
} catch {
    Write-Host "  ❌ /doc.html EXCEPTION: $($_.Exception.Message)" -ForegroundColor Red
    $script:fail++
}

# =====================================================
# 汇总
# =====================================================
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   测试结果汇总" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  通过：$pass" -ForegroundColor Green
Write-Host "  失败：$fail" -ForegroundColor Red

if ($fail -eq 0) {
    Write-Host ""
    Write-Host "🎉 全部通过！后端 D + E 都正常" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️ 有 $fail 个接口失败，看上面红色日志排查" -ForegroundColor Yellow
}
Write-Host ""
