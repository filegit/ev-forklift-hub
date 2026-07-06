@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
cd /d %~dp0

set ENV_FILE=.env.local
if not exist "%ENV_FILE%" (
  echo [错误] 未找到 %ENV_FILE%
  echo 请执行: copy .env.example .env.local
  echo 然后编辑 .env.local 填入 LLM_API_KEY
  exit /b 1
)

for /f "usebackq eol=# tokens=1,* delims==" %%a in ("%ENV_FILE%") do (
  set "key=%%a"
  set "val=%%b"
  if not "!key!"=="" set "!key!=!val!"
)

if "%LLM_API_KEY%"=="" (
  echo [错误] .env.local 中未设置 LLM_API_KEY
  exit /b 1
)

if "%LLM_ENABLED%"=="" set LLM_ENABLED=true
if "%LLM_BASE_URL%"=="" set LLM_BASE_URL=https://niceapi.ai/v1
if "%LLM_MODEL%"=="" set LLM_MODEL=gpt-5.4

echo 启动 AI Agent...
echo   LLM_MODEL=%LLM_MODEL%
echo   LLM_BASE_URL=%LLM_BASE_URL%
echo   LLM_API_KEY=***已加载***

java -jar efh-agent\target\efh-agent-1.0.0.jar
