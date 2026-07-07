# 手机端 App 使用指南

本项目前端是 **Vue 3 响应式 Web 应用**，已针对手机浏览器优化（底部导航、侧滑菜单、触控友好）。你可以通过以下三种方式在手机上使用，**无需单独开发原生 App**。

---

## 方式一：浏览器访问（最简单）

### 生产环境（已部署服务器）

手机浏览器打开：

```
http://111.170.36.78:8888/
```

与电脑访问同一地址，自动切换移动端布局（宽度 ≤ 768px）。

### 局域网开发调试

```bash
cd efh-web
npm install
npm run dev
```

手机与电脑连同一 WiFi，访问 `http://你的电脑IP:3000`（Vite 已配置 `host: 0.0.0.0`）。

---

## 方式二：添加到主屏幕（推荐，像 App 一样使用）

这是**零成本**把网站变成「手机 App」的方式，图标会出现在桌面，打开后全屏、无浏览器地址栏。

### iPhone / iPad（Safari）

1. 用 **Safari** 打开 `http://111.170.36.78:8888/`
2. 点击底部分享按钮 **「分享」**
3. 选择 **「添加到主屏幕」**
4. 名称可改为「叉车社区」，点 **添加**
5. 桌面出现蓝色叉车图标，点击即可全屏打开

> 需用 Safari，微信内置浏览器请先点右上角「在 Safari 中打开」。

### Android（Chrome / 系统浏览器）

1. 用 **Chrome** 打开网站
2. 点击右上角 **⋮** 菜单
3. 选择 **「添加到主屏幕」** 或 **「安装应用」**
4. 确认后桌面出现图标

部分国产浏览器（小米、华为）路径为：菜单 → **添加到桌面** / **添加到主屏幕**。

### 已配置的 PWA 能力

项目已包含：

| 文件 | 作用 |
|------|------|
| `public/manifest.webmanifest` | 应用名称、图标、全屏模式 |
| `public/icon.svg` | 桌面图标 |
| `index.html` meta 标签 | iOS 全屏、主题色、安全区域 |

添加到主屏幕后，会以 **standalone** 模式运行，体验接近原生 App。

---

## 方式三：打包成真正的安装包（可选，进阶）

若需要上架应用商店或离线安装 APK/IPA，可选用以下方案：

### A. Capacitor（推荐，基于现有 Vue 项目）

```bash
cd efh-web
npm run build
npm install @capacitor/core @capacitor/cli @capacitor/android @capacitor/ios
npx cap init "叉车社区" com.efh.community --web-dir=dist
npx cap add android
npx cap add ios
npx cap sync
npx cap open android
```

优点：一套 Vue 代码，可发 Android / iOS；可调用相机、推送等原生能力。

### B. TWA（Trusted Web Activity，仅 Android）

将 PWA 封装为 Google Play 上的 Android App，本质仍是 WebView 加载你的网站。适合已有 HTTPS 域名的场景。

### C. 微信小程序 / uni-app 重写

若主要用户在微信内，可考虑小程序；需单独适配 API 与 UI，工作量较大。

**当前项目已满足方式一、二，大多数场景无需方式三。**

---

## 移动端界面说明

| 区域 | 功能 |
|------|------|
| 底部 Tab | 首页、商城、知识库、AI、我的 |
| 右上角菜单 | 侧滑：维修服务、订单、收藏、发帖、登录/退出 |
| 购物车 | 顶部购物车图标（登录后显示） |
| 登录 | 验证码登录 / 密码登录，深色渐变背景 |

结算、支付页会自动隐藏底部 Tab，避免误触。

---

## 设计规范（开发参考）

全局设计变量在 `src/styles/theme.css`：

- 主色：`#2563eb`
- 背景：`#f1f5f9`
- 圆角：12–20px
- 页面容器：`.efh-page`、`.efh-page-header`、`.efh-post-item`、`.efh-product-card`

移动端断点：`768px`（`src/styles/mobile.css`）

---

## 部署更新

修改前端后重新构建并重启 Web 容器：

```bash
cd efh-web && npm run build
docker restart efh-web
```

用户若已「添加到主屏幕」，刷新或重新打开即可看到新界面，无需重新安装。

---

## 常见问题

| 问题 | 解决 |
|------|------|
| 主屏幕图标是截图而非叉车图标 | 清除浏览器缓存后重新「添加到主屏幕」 |
| iOS 顶部留白 | 已配置 `viewport-fit=cover`，支持刘海屏安全区 |
| 接口请求失败 | 确认网关与后端服务运行正常 |
| 验证码收不到 | 见服务器 `sms.env` 短信配置文档 |
