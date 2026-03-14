@echo off
chcp 65001 >nul
echo ========================================
echo   新能源叉车社区平台 - 快速启动脚本
echo ========================================
echo.

REM 检查 Java 环境
echo [1/6] 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未检测到 Java 环境，请先安装 JDK 1.8+
    pause
    exit /b 1
)
echo ✅ Java 环境检查通过
echo.

REM 检查 Maven 环境
echo [2/6] 检查 Maven 环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未检测到 Maven 环境，请先安装 Maven 3.6+
    pause
    exit /b 1
)
echo ✅ Maven 环境检查通过
echo.

REM 编译项目
echo [3/6] 编译项目...
echo 正在执行: mvn clean install -DskipTests
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 错误: 项目编译失败，请检查错误信息
    pause
    exit /b 1
)
echo ✅ 项目编译成功
echo.

REM 提示启动基础设施
echo [4/6] 基础设施检查...
echo.
echo 请确保以下服务已启动：
echo   ✅ MySQL (端口: 3306)
echo   ✅ Redis (端口: 6379)
echo   ✅ Nacos (端口: 8848)
echo   ⚠️  Kafka (端口: 9092) - 可选
echo   ⚠️  Elasticsearch (端口: 9200) - 可选
echo.
echo 按任意键继续启动微服务...
pause >nul
echo.

REM 启动微服务
echo [5/6] 启动微服务...
echo.

echo 正在启动网关服务 (端口: 8080)...
start "efh-gateway" cmd /k "cd efh-gateway\target && java -jar efh-gateway-1.0.0.jar"
timeout /t 10 /nobreak >nul

echo 正在启动用户服务 (端口: 8081)...
start "efh-user" cmd /k "cd efh-user\target && java -jar efh-user-1.0.0.jar"
timeout /t 10 /nobreak >nul

echo 正在启动社区服务 (端口: 8082)...
start "efh-community" cmd /k "cd efh-community\target && java -jar efh-community-1.0.0.jar"
timeout /t 10 /nobreak >nul

echo 正在启动配件服务 (端口: 8083)...
start "efh-parts" cmd /k "cd efh-parts\target && java -jar efh-parts-1.0.0.jar"
timeout /t 10 /nobreak >nul

echo 正在启动维修服务 (端口: 8084)...
start "efh-service" cmd /k "cd efh-service\target && java -jar efh-service-1.0.0.jar"
echo.

REM 完成
echo [6/6] 启动完成！
echo.
echo ========================================
echo   所有微服务已启动
echo ========================================
echo.
echo 服务列表：
echo   - 网关服务:   http://localhost:8080
echo   - 用户服务:   http://localhost:8081
echo   - 社区服务:   http://localhost:8082
echo   - 配件服务:   http://localhost:8083
echo   - 维修服务:   http://localhost:8084
echo.
echo Nacos 控制台: http://localhost:8848/nacos
echo 默认账号密码: nacos/nacos
echo.
echo 提示: 请等待 30-60 秒让所有服务完全启动
echo.
pause
