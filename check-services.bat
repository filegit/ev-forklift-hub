@echo off
chcp 65001 >nul
echo ========================================
echo   微服务启动状态检查
echo ========================================
echo.

echo [检查基础设施]
echo.

REM 检查 Nacos
echo 检查 Nacos (8848)...
curl -s http://localhost:8848/nacos/ >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Nacos 运行正常
) else (
    echo ❌ Nacos 未运行或无法访问
)

REM 检查 Elasticsearch
echo 检查 Elasticsearch (9200)...
curl -s http://localhost:9200 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Elasticsearch 运行正常
) else (
    echo ⚠️  Elasticsearch 未运行（可选）
)

echo.
echo [检查微服务端口]
echo.

REM 检查各服务端口
netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 网关服务 (8080) 正在运行
) else (
    echo ❌ 网关服务 (8080) 未运行
)

netstat -ano | findstr :8081 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 用户服务 (8081) 正在运行
) else (
    echo ❌ 用户服务 (8081) 未运行
)

netstat -ano | findstr :8082 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 社区服务 (8082) 正在运行
) else (
    echo ❌ 社区服务 (8082) 未运行
)

netstat -ano | findstr :8083 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 配件服务 (8083) 正在运行
) else (
    echo ❌ 配件服务 (8083) 未运行
)

netstat -ano | findstr :8084 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 维修服务 (8084) 正在运行
) else (
    echo ❌ 维修服务 (8084) 未运行
)

echo.
echo ========================================
echo   检查完成
echo ========================================
echo.
echo 提示：
echo - 在 IDEA 中启动各个 Application.java
echo - 访问 Nacos 控制台查看服务注册状态
echo   http://localhost:8848/nacos
echo.

pause
