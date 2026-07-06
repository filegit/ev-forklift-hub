@echo off
chcp 65001 >nul
echo 启动 AI Agent（阿里云百炼 / 通义 Qwen，从 .env.local 读取配置）
echo 请在 .env.local 中设置:
echo   LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
echo   LLM_MODEL=qwen-plus
echo   LLM_API_KEY=你的百炼Key
call "%~dp0start-agent.bat"
