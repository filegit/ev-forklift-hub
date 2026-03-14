# 快速启动脚本 - Docker 版本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  新能源叉车社区平台 - Docker 部署" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Docker
Write-Host "[1/5] 检查 Docker 环境..." -ForegroundColor Yellow
try {
    docker --version | Out-Null
    Write-Host "✅ Docker 已安装" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: 未检测到 Docker，请先安装 Docker Desktop" -ForegroundColor Red
    pause
    exit 1
}

# 检查 Docker Compose
Write-Host "[2/5] 检查 Docker Compose..." -ForegroundColor Yellow
try {
    docker-compose --version | Out-Null
    Write-Host "✅ Docker Compose 已安装" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: 未检测到 Docker Compose" -ForegroundColor Red
    pause
    exit 1
}

# 检查 Docker 是否运行
Write-Host "[3/5] 检查 Docker 服务..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✅ Docker 服务正在运行" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: Docker 服务未启动，请启动 Docker Desktop" -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""
Write-Host "[4/5] 启动服务..." -ForegroundColor Yellow
Write-Host "正在启动所有服务（首次启动需要 3-5 分钟）..." -ForegroundColor Cyan
Write-Host ""

# 启动服务
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ 服务启动成功！" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "[5/5] 服务信息" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  基础设施" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Nacos 控制台: http://localhost:8848/nacos" -ForegroundColor White
    Write-Host "  账号密码: nacos / nacos" -ForegroundColor Gray
    Write-Host ""
    Write-Host "MySQL: localhost:3306" -ForegroundColor White
    Write-Host "  账号密码: root / 123456" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Redis: localhost:6379" -ForegroundColor White
    Write-Host "  密码: 123456" -ForegroundColor Gray
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  微服务" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "API 网关:   http://localhost:8080" -ForegroundColor White
    Write-Host "用户服务:   http://localhost:8081" -ForegroundColor White
    Write-Host "社区服务:   http://localhost:8082" -ForegroundColor White
    Write-Host "配件服务:   http://localhost:8083" -ForegroundColor White
    Write-Host "维修服务:   http://localhost:8084" -ForegroundColor White
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  常用命令" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "查看服务状态: docker-compose ps" -ForegroundColor White
    Write-Host "查看日志:     docker-compose logs -f" -ForegroundColor White
    Write-Host "停止服务:     docker-compose down" -ForegroundColor White
    Write-Host "重启服务:     docker-compose restart" -ForegroundColor White
    Write-Host ""
    
    Write-Host "提示: 请等待 2-3 分钟让所有服务完全启动" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "❌ 服务启动失败，请查看错误信息" -ForegroundColor Red
    Write-Host ""
    Write-Host "常见问题:" -ForegroundColor Yellow
    Write-Host "1. 端口被占用 - 请关闭占用端口的程序" -ForegroundColor Gray
    Write-Host "2. 内存不足 - 请增加 Docker Desktop 内存限制" -ForegroundColor Gray
    Write-Host "3. 网络问题 - 请检查网络连接" -ForegroundColor Gray
    Write-Host ""
}

pause
