# 🚀 新能源叉车社区平台 - 前端项目

## 📦 项目简介

基于 Vue 3 + Element Plus 的现代化前端应用，提供用户注册登录、帖子浏览、发布、评论等功能。

---

## 🛠️ 技术栈

- **框架：** Vue 3 (Composition API)
- **构建工具：** Vite 4
- **UI 组件库：** Element Plus
- **HTTP 客户端：** Axios
- **路由：** Vue Router 4
- **状态管理：** Pinia
- **部署：** Docker + Nginx

---

## 📁 项目结构

```
efh-web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口
│   │   ├── user.js       # 用户相关接口
│   │   ├── post.js       # 帖子相关接口
│   │   └── comment.js    # 评论相关接口
│   ├── assets/           # 资源文件
│   ├── components/       # 公共组件
│   ├── layouts/          # 布局组件
│   │   └── MainLayout.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # 状态管理
│   │   └── user.js
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios 封装
│   ├── views/            # 页面组件
│   │   ├── Home.vue      # 首页
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   └── PostDetail.vue # 帖子详情页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML 模板
├── vite.config.js        # Vite 配置
├── package.json          # 依赖配置
├── Dockerfile            # Docker 构建文件
├── nginx.conf            # Nginx 配置
└── .gitignore           # Git 忽略文件
```

---

## 🚀 本地开发

### 1. 安装依赖

```bash
cd efh-web
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

---

## 🐳 Docker 部署

### 方式一：单独构建前端

```bash
# 进入前端目录
cd efh-web

# 构建 Docker 镜像
docker build -t efh-web:latest .

# 运行容器
docker run -d -p 80:80 --name efh-web efh-web:latest
```

### 方式二：使用 docker-compose（推荐）

```bash
# 在项目根目录
docker-compose up -d web
```

---

## 🌐 功能特性

### 已实现功能

1. ✅ **用户系统**
   - 用户注册
   - 用户登录
   - 自动保存 Token
   - 退出登录

2. ✅ **帖子功能**
   - 浏览帖子列表
   - 分类筛选（技术交流、故障求助、经验分享、其他）
   - 查看帖子详情
   - 发布帖子
   - 点赞帖子

3. ✅ **评论功能**
   - 查看评论列表
   - 发表评论
   - 删除自己的评论

4. ✅ **响应式设计**
   - 适配桌面端和移动端
   - 现代化 UI 设计

---

## 📱 页面说明

### 1. 首页 (/)
- 展示所有帖子列表
- 支持分类筛选
- 分页加载
- 点击帖子进入详情页

### 2. 登录页 (/login)
- 用户名密码登录
- 登录成功自动跳转首页
- Token 自动保存到 localStorage

### 3. 注册页 (/register)
- 用户注册表单
- 表单验证
- 注册成功自动登录

### 4. 帖子详情页 (/post/:id)
- 查看帖子完整内容
- 点赞功能
- 评论列表
- 发表评论
- 删除自己的评论

---

## 🔧 配置说明

### Vite 配置 (vite.config.js)

```javascript
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端网关地址
        changeOrigin: true
      }
    }
  }
})
```

### Nginx 配置 (nginx.conf)

```nginx
server {
    listen 80;
    
    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理到后端网关
    location /api/ {
        proxy_pass http://gateway:8080/;
    }
}
```

---

## 🎨 UI 设计

### 颜色主题

- **主色：** #409eff (Element Plus 蓝)
- **成功：** #67c23a
- **警告：** #e6a23c
- **危险：** #f56c6c
- **信息：** #909399

### 布局特点

- 顶部导航栏固定
- 主体内容居中（最大宽度 1200px）
- 卡片式设计
- 响应式布局

---

## 📝 API 接口

### 用户接口

```javascript
// 登录
POST /api/user/api/login
{
  "username": "testuser",
  "password": "123456"
}

// 注册
POST /api/user/api/register
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "phone": "13800138000"
}

// 获取用户信息
GET /api/user/api/info
Headers: Authorization: Bearer {token}
```

### 帖子接口

```javascript
// 获取帖子列表
GET /api/community/api/post/list?page=1&size=10&category=1

// 获取帖子详情
GET /api/community/api/post/{id}

// 发布帖子
POST /api/community/api/post
Headers: Authorization: Bearer {token}
{
  "title": "帖子标题",
  "content": "帖子内容",
  "category": 1
}

// 点赞帖子
POST /api/community/api/post/{id}/like
Headers: Authorization: Bearer {token}
```

### 评论接口

```javascript
// 获取评论列表
GET /api/community/api/comment/list?postId=1

// 发表评论
POST /api/community/api/comment
Headers: Authorization: Bearer {token}
{
  "postId": 1,
  "content": "评论内容",
  "parentId": 0
}

// 删除评论
DELETE /api/community/api/comment/{id}
Headers: Authorization: Bearer {token}
```

---

## 🔐 认证流程

1. 用户登录/注册成功后，后端返回 JWT Token
2. 前端将 Token 保存到 localStorage
3. 后续请求自动在请求头添加 `Authorization: Bearer {token}`
4. 后端网关验证 Token，提取用户信息
5. 网关将用户ID通过 `X-User-Id` 请求头传递给微服务

---

## 🚀 部署到服务器

### 1. 准备服务器

```bash
# 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com | sh
sudo systemctl start docker
sudo systemctl enable docker
```

### 2. 上传项目文件

```bash
# 使用 scp 或 git clone
scp -r ev-forklift-hub user@server:/path/to/
```

### 3. 启动服务

```bash
cd /path/to/ev-forklift-hub

# 启动所有服务（包括前端）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f web
```

### 4. 访问应用

```
前端：http://your-server-ip
后端网关：http://your-server-ip:8080
```

---

## 🐛 常见问题

### Q: 本地开发时 API 请求失败？

**A:** 确保后端服务已启动，并且 vite.config.js 中的 proxy 配置正确。

### Q: Docker 构建失败？

**A:** 检查 Node.js 版本，确保使用 Node 18+。

### Q: Nginx 代理不生效？

**A:** 检查 nginx.conf 配置，确保 proxy_pass 地址正确。

### Q: 登录后刷新页面需要重新登录？

**A:** 检查 Token 是否正确保存到 localStorage。

---

## 📄 许可证

MIT License

---

## 👥 贡献者

EFH Team

---

**祝你使用愉快！** 🎉
