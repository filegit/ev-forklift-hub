@echo off
chcp 65001 >nul
echo ========================================
echo   EV-Forklift-Hub Docker 启动脚本
echo ========================================
echo.

:menu
echo 请选择操作：
echo 1. 启动基础设施（MySQL + Redis + Nacos）
echo 2. 启动所有服务（完整环境）
echo 3. 停止所有服务
echo 4. 查看服务状态
echo 5. 查看日志
echo 6. 重启服务
echo 7. 清理环境（删除容器和数据）
echo 0. 退出
echo.

set /p choice=请输入选择（0-7）：

if "%choice%"=="1" goto start_infra
if "%choice%"=="2" goto start_all
if "%choice%"=="3" goto stop_all
if "%choice%"=="4" goto status
if "%choice%"=="5" goto logs
if "%choice%"=="6" goto restart
if "%choice%"=="7" goto clean
if "%choice%"=="0" goto end

echo 无效的选择，请重新输入
goto menu

:start_infra
echo.
echo 正在启动基础设施...
docker-compose up -d mysql redis nacos elasticsearch
echo.
echo 基础设施启动完成！
echo MySQL: localhost:3306 (root/123456)
echo Redis: localhost:6379 (密码: 123456)
echo Nacos: http://localhost:8848/nacos (nacos/nacos)
echo Elasticsearch: http://localhost:9200
echo.
echo 提示：Nacos 需要 1-2 分钟完全启动
echo 现在可以在 IDEA 中启动微服务了
pause
goto menu

:start_all
echo.
echo 正在构建并启动所有服务...
echo 这可能需要 5-10 分钟，请耐心等待...
docker-compose up -d --build
echo.
echo 所有服务启动完成！
echo 网关: http://localhost:8080
echo.
docker-compose ps
pause
goto menu

:stop_all
echo.
echo 正在停止所有服务...
docker-compose stop
echo.
echo 所有服务已停止
pause
goto menu

:status
echo.
echo 服务状态：
docker-compose ps
pause
goto menu

:logs
echo.
echo 请选择要查看的服务日志：
echo 1. MySQL
echo 2. Redis
echo 3. Nacos
echo 4. Gateway
echo 5. User Service
echo 6. 所有服务
echo.
set /p log_choice=请输入选择（1-6）：

if "%log_choice%"=="1" docker-compose logs -f mysql
if "%log_choice%"=="2" docker-compose logs -f redis
if "%log_choice%"=="3" docker-compose logs -f nacos
if "%log_choice%"=="4" docker-compose logs -f gateway
if "%log_choice%"=="5" docker-compose logs -f user-service
if "%log_choice%"=="6" docker-compose logs -f

goto menu

:restart
echo.
echo 正在重启所有服务...
docker-compose restart
echo.
echo 所有服务已重启
pause
goto menu

:clean
echo.
echo ⚠️  警告：此操作将删除所有容器和数据！
echo.
set /p confirm=确认删除？(y/n)：

if /i "%confirm%"=="y" (
    echo 正在清理环境...
    docker-compose down -v
    echo 清理完成！
) else (
    echo 已取消
)
pause
goto menu

:end
echo.
echo 再见！
exit
