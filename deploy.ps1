# PowerShell 部署脚本
# 部署到阿里云服务器

$SERVER_IP = "8.219.135.26"
$SERVER_USER = "root"
$SERVER_PASSWORD = $env:DEPLOY_PASSWORD
$DEPLOY_DIR = "/root/ev-forklift-hub"

if ([string]::IsNullOrWhiteSpace($SERVER_PASSWORD)) {
    throw "DEPLOY_PASSWORD is required"
}

Write-Host "开始部署到服务器 $SERVER_IP..." -ForegroundColor Green

# 1. 先在本地构建项目
Write-Host "`n1. 构建 Maven 项目..." -ForegroundColor Yellow
mvn clean install -DskipTests -q

# 2. 使用 SCP 上传文件（需要安装 OpenSSH 或使用 WinSCP）
Write-Host "`n2. 准备上传文件..." -ForegroundColor Yellow

# 创建临时目录
$tempDir = ".\deploy-temp"
if (Test-Path $tempDir) {
    Remove-Item -Recurse -Force $tempDir
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

# 复制必要的文件
Write-Host "   复制 docker-compose.yml..."
Copy-Item "docker-compose.yml" "$tempDir\"

Write-Host "   复制 docker 目录..."
Copy-Item -Recurse "docker" "$tempDir\"

Write-Host "   复制编译后的 JAR 文件..."
foreach ($module in @("efh-gateway", "efh-user", "efh-community", "efh-parts", "efh-service")) {
    $targetDir = "$tempDir\$module\target"
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    if (Test-Path "$module\target\*.jar") {
        Copy-Item "$module\target\*.jar" $targetDir
        Write-Host "   ✓ $module" -ForegroundColor Green
    }
}

Write-Host "   复制前端文件..."
Copy-Item -Recurse "efh-web" "$tempDir\"

Write-Host "`n3. 使用 SCP 上传到服务器..." -ForegroundColor Yellow
Write-Host "   请手动执行以下命令上传文件：" -ForegroundColor Cyan
Write-Host ""
Write-Host "   方法1: 使用 WinSCP 或 FileZilla" -ForegroundColor White
Write-Host "   - 连接到 $SERVER_IP" -ForegroundColor White
Write-Host "   - 用户名: $SERVER_USER" -ForegroundColor White
Write-Host "   - 密码: <DEPLOY_PASSWORD>" -ForegroundColor White
Write-Host "   - 上传 deploy-temp 目录中的所有文件到 /root/ev-forklift-hub" -ForegroundColor White
Write-Host ""
Write-Host "   方法2: 使用 scp 命令（如果已安装 OpenSSH）" -ForegroundColor White
Write-Host "   scp -r deploy-temp/* ${SERVER_USER}@${SERVER_IP}:${DEPLOY_DIR}/" -ForegroundColor White
Write-Host ""

$continue = Read-Host "文件上传完成后，按 Enter 继续部署到服务器..."

Write-Host "`n4. 连接服务器并启动服务..." -ForegroundColor Yellow
Write-Host "   请手动 SSH 连接到服务器并执行以下命令：" -ForegroundColor Cyan
Write-Host ""
Write-Host "   ssh root@$SERVER_IP" -ForegroundColor White
Write-Host "   cd /root/ev-forklift-hub" -ForegroundColor White
Write-Host "   docker-compose down" -ForegroundColor White
Write-Host "   docker-compose up -d --build" -ForegroundColor White
Write-Host "   docker-compose ps" -ForegroundColor White
Write-Host ""

Write-Host "`n部署准备完成！" -ForegroundColor Green
Write-Host "访问地址：" -ForegroundColor Yellow
Write-Host "  前端: http://$SERVER_IP" -ForegroundColor Cyan
Write-Host "  网关: http://$SERVER_IP:8080" -ForegroundColor Cyan
Write-Host "  Nacos: http://$SERVER_IP:8848/nacos" -ForegroundColor Cyan
