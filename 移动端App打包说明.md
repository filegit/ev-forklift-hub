# 移动端 App 打包说明

本项目已使用 Capacitor 封装为 Android / iOS App，App 内置前端页面，接口连接线上服务器：

```text
http://111.170.36.78:8888
```

## 已生成内容

- Android 工程：`efh-web/android`
- iOS 工程：`efh-web/ios`
- Android 调试安装包：`release/叉车智联中台-debug.apk`
- Android SDK：`tools/android-sdk`

## 安卓手机安装

把下面这个 APK 传到安卓手机后直接安装：

```text
D:\LH\ev-forklift-hub\release\叉车智联中台-debug.apk
```

如果手机提示“禁止安装未知来源应用”，在系统设置中允许当前文件管理器或浏览器安装未知来源应用即可。

## 重新构建 Android APK

在 PowerShell 中执行：

```powershell
cd D:\LH\ev-forklift-hub\efh-web
$env:VITE_APP_PLATFORM='native'
$env:VITE_API_ORIGIN='http://111.170.36.78:8888'
& 'C:\Users\Administrator\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe' node_modules\vite\bin\vite.js build --mode mobile
& 'C:\Users\Administrator\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe' node_modules\@capacitor\cli\bin\capacitor sync android

cd D:\LH\ev-forklift-hub\efh-web\android
$env:JAVA_HOME='C:\Users\Administrator\Documents\Codex\2026-07-05\zai-m\work\jdk21-full\jdk-21.0.11+10'
$env:ANDROID_HOME='D:\LH\ev-forklift-hub\tools\android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
.\gradlew.bat assembleDebug --no-daemon
```

生成位置：

```text
D:\LH\ev-forklift-hub\efh-web\android\app\build\outputs\apk\debug\app-debug.apk
```

## iPhone / iOS

iOS 工程已生成：

```text
D:\LH\ev-forklift-hub\efh-web\ios
```

但 iOS 无法在 Windows 上直接打包安装，需要：

- macOS
- Xcode
- Apple Developer 账号或个人 Apple ID 签名

在 Mac 上打开 `ios/App/App.xcworkspace`，选择真机和签名团队后运行即可安装到 iPhone。

## 正式上线注意

当前服务器使用 HTTP，并已在 Android/iOS 工程里放开明文访问，适合测试安装。

如果要上架应用市场或长期给用户使用，建议给服务器配置 HTTPS 域名，然后把 `.env.mobile` 改为：

```text
VITE_API_ORIGIN=https://你的域名
```

再重新构建 App。
