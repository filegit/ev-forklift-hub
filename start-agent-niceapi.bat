@echo off
chcp 65001 >nul
echo 启动 AI Agent（从 .env.local 读取配置）
echo 若未配置，请先: copy .env.example .env.local
call "%~dp0start-agent.bat"
