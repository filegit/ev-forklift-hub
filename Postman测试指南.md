# 📮 Postman 测试指南

## 🚀 快速开始

### 第一步：导入 Postman 集合

1. 打开 Postman
2. 点击左上角 **Import** 按钮
3. 选择 **File** 标签
4. 选择项目根目录下的 `Postman_Collection.json` 文件
5. 点击 **Import** 导入

**导入后会看到：**
- 📁 用户服务（4个接口）
- 📁 社区服务（6个接口）

---

### 第二步：创建环境变量

1. 点击右上角的 **齿轮图标** ⚙️
2. 点击 **Add** 创建新环境
3. 环境名称：`EFH-Local`
4. 添加变量：

| 变量名 | 初始值 | 当前值 |
|--------|--------|--------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | 留空 | 留空 |

5. 点击 **Save** 保存
6. 在右上角下拉框选择 `EFH-Local` 环境

---

## 🧪 测试流程

### 方式一：自动化测试（推荐）⭐

**特点：** Token 自动保存，无需手动复制粘贴

#### 1. 用户注册

**接口：** `用户服务 > 1. 用户注册`

**请求体：**
```json
{
  "username": "testuser007",
  "password": "123456",
  "nickname": "测试用户007",
  "phone": "13800138007"
}
```

**点击 Send 发送请求**

**预期响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**✅ Token 会自动保存到环境变量中！**

---

#### 2. 获取用户信息

**接口：** `用户服务 > 3. 获取用户信息`

**请求头：**
- `Authorization: Bearer {{token}}`（已自动配置）

**点击 Send 发送请求**

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser007",
    "nickname": "测试用户007",
    "phone": "13800138007",
    ...
  }
}
```

---

#### 3. 发布帖子

**接口：** `社区服务 > 1. 发布帖子`

**请求头：**
- `Authorization: Bearer {{token}}`（已自动配置）

**请求体：**
```json
{
  "title": "新能源叉车电池保养技巧",
  "content": "大家好，今天分享一下新能源叉车电池的日常保养技巧...",
  "category": 3
}
```

**分类说明：**
- 1 - 技术交流
- 2 - 故障求助
- 3 - 经验分享
- 4 - 其他

**点击 Send 发送请求**

**预期响应：**
```json
{
  "code": 200,
  "message": "发帖成功",
  "data": null
}
```

---

#### 4. 查看帖子列表

**接口：** `社区服务 > 2. 帖子列表（全部）`

**查询参数：**
- `page`: 1
- `size`: 10

**点击 Send 发送请求**

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "title": "新能源叉车电池保养技巧",
        "content": "大家好，今天分享一下...",
        "category": 3,
        "viewCount": 0,
        "likeCount": 0,
        "commentCount": 0,
        "status": 1,
        "createTime": "2024-03-09T23:00:00",
        "updateTime": "2024-03-09T23:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

#### 5. 查看帖子详情

**接口：** `社区服务 > 4. 帖子详情`

**URL：** `{{baseUrl}}/community/api/post/1`

**点击 Send 发送请求**

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "title": "新能源叉车电池保养技巧",
    "content": "大家好，今天分享一下...",
    "category": 3,
    "viewCount": 1,
    "likeCount": 0,
    "commentCount": 0,
    "status": 1,
    "createTime": "2024-03-09T23:00:00",
    "updateTime": "2024-03-09T23:00:00"
  }
}
```

**注意：** `viewCount` 会自动 +1

---

#### 6. 点赞帖子

**接口：** `社区服务 > 5. 点赞帖子`

**请求头：**
- `Authorization: Bearer {{token}}`（已自动配置）

**URL：** `{{baseUrl}}/community/api/post/1/like`

**点击 Send 发送请求**

**预期响应：**
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null
}
```

**再次查看帖子详情，`likeCount` 会 +1**

---

### 方式二：手动测试

如果不想使用集合，可以手动创建请求：

#### 1. 用户注册

```
POST http://localhost:8080/user/api/register
Content-Type: application/json

{
  "username": "testuser008",
  "password": "123456",
  "nickname": "测试用户008",
  "phone": "13800138008"
}
```

#### 2. 用户登录

```
POST http://localhost:8080/user/api/login
Content-Type: application/json

{
  "username": "testuser008",
  "password": "123456"
}
```

**复制返回的 Token！**

#### 3. 获取用户信息

```
GET http://localhost:8080/user/api/info
Authorization: Bearer YOUR_TOKEN_HERE
```

#### 4. 发布帖子

```
POST http://localhost:8080/community/api/post
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE

{
  "title": "测试帖子",
  "content": "测试内容",
  "category": 1
}
```

#### 5. 查看帖子列表

```
GET http://localhost:8080/community/api/post/list?page=1&size=10
```

#### 6. 查看帖子详情

```
GET http://localhost:8080/community/api/post/1
```

#### 7. 点赞帖子

```
POST http://localhost:8080/community/api/post/1/like
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 🎯 推荐测试顺序

### 完整流程测试

1. ✅ **用户服务 > 4. 测试接口** - 验证服务运行
2. ✅ **用户服务 > 1. 用户注册** - 注册新用户（Token 自动保存）
3. ✅ **用户服务 > 3. 获取用户信息** - 验证 Token 有效
4. ✅ **社区服务 > 6. 测试接口** - 验证社区服务运行
5. ✅ **社区服务 > 1. 发布帖子** - 发布帖子
6. ✅ **社区服务 > 2. 帖子列表（全部）** - 查看所有帖子
7. ✅ **社区服务 > 4. 帖子详情** - 查看帖子详情
8. ✅ **社区服务 > 5. 点赞帖子** - 点赞
9. ✅ **社区服务 > 4. 帖子详情** - 再次查看，验证点赞数

---

## 📊 接口列表

### 用户服务（4个接口）

| 接口 | 方法 | 路径 | 需要认证 |
|------|------|------|---------|
| 用户注册 | POST | `/user/api/register` | ❌ |
| 用户登录 | POST | `/user/api/login` | ❌ |
| 获取用户信息 | GET | `/user/api/info` | ✅ |
| 测试接口 | GET | `/user/api/test` | ❌ |

### 社区服务（6个接口）

| 接口 | 方法 | 路径 | 需要认证 |
|------|------|------|---------|
| 发布帖子 | POST | `/community/api/post` | ✅ |
| 帖子列表（全部） | GET | `/community/api/post/list` | ❌ |
| 帖子列表（分类） | GET | `/community/api/post/list?category=1` | ❌ |
| 帖子详情 | GET | `/community/api/post/{id}` | ❌ |
| 点赞帖子 | POST | `/community/api/post/{id}/like` | ✅ |
| 测试接口 | GET | `/community/api/post/test` | ❌ |

---

## 🔧 环境变量说明

### baseUrl
- **用途：** API 基础地址
- **本地开发：** `http://localhost:8080`
- **生产环境：** `https://api.yourdomain.com`

### token
- **用途：** JWT Token
- **自动设置：** 注册或登录后自动保存
- **手动设置：** 复制 Token 粘贴到环境变量

---

## 🐛 常见问题

### Q: Token 自动保存不生效？

**A:** 检查以下几点：
1. 是否选择了正确的环境（右上角下拉框）
2. 是否导入了完整的集合（包含 Test Scripts）
3. 查看 Console（View > Show Postman Console）查看日志

---

### Q: 请求返回 401 Unauthorized？

**A:** Token 无效或过期
1. 重新登录获取新 Token
2. 检查 Authorization 头是否正确：`Bearer {{token}}`
3. 确保 Token 前面有 `Bearer ` 前缀

---

### Q: 请求返回 404 Not Found？

**A:** 检查以下几点：
1. 服务是否启动（查看 IDEA 控制台）
2. URL 是否正确（注意路径前缀）
3. 网关是否启动（8080端口）

---

### Q: 如何查看请求详情？

**A:** 
1. 点击请求后的 **Console** 按钮
2. 或者打开 Postman Console（View > Show Postman Console）
3. 可以看到完整的请求和响应信息

---

## 🎉 开始测试吧！

**推荐流程：**
1. 导入 Postman 集合
2. 创建环境变量
3. 按顺序测试所有接口
4. 验证功能正常

**所有请求都通过网关（8080端口）！** 🚀

---

## 📝 测试数据

### 测试用户

| 用户名 | 密码 | 昵称 | 手机号 |
|--------|------|------|--------|
| testuser007 | 123456 | 测试用户007 | 13800138007 |
| testuser008 | 123456 | 测试用户008 | 13800138008 |
| testuser009 | 123456 | 测试用户009 | 13800138009 |

### 测试帖子

**标题：** 新能源叉车电池保养技巧  
**内容：** 大家好，今天分享一下新能源叉车电池的日常保养技巧...  
**分类：** 3（经验分享）

**标题：** 叉车故障求助  
**内容：** 我的叉车出现了充电问题，请问有人遇到过吗？  
**分类：** 2（故障求助）

**标题：** 新能源叉车技术讨论  
**内容：** 关于新能源叉车的技术发展趋势...  
**分类：** 1（技术交流）

---

**祝测试顺利！** 🎊
