# EV Forklift Hub 原生 App 接口文档

本文档面向 Android / iOS 原生 App 开发，接口来自当前后端源码与前端调用封装。

## 1. 接入说明

### 1.1 环境地址

线上推荐访问地址：

```text
http://111.170.36.78:8888/api
```

本地/服务器内网网关地址：

```text
http://127.0.0.1:8080
```

下文接口均按线上地址说明，例如：

```text
GET http://111.170.36.78:8888/api/user/api/info
```

### 1.2 通用请求头

除登录、注册、公开列表等公开接口外，其余接口需要登录态：

```http
Content-Type: application/json
Authorization: Bearer <token>
```

`token` 由登录或注册接口返回。客户端只需要传 `Authorization`，`X-User-Id` 由网关解析 token 后自动传给后端服务，App 不要自己传 `X-User-Id`。

文件上传接口使用：

```http
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

### 1.3 通用响应结构

普通 JSON 接口统一返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

常见状态：

```text
200 业务成功
401 未登录或 token 无效
500 系统异常或业务失败
```

分页返回常见结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 0,
    "size": 10,
    "current": 1,
    "pages": 0
  }
}
```

### 1.4 公共字段

多数实体继承以下字段：

```json
{
  "id": 1,
  "createTime": "2026-07-08T08:21:26",
  "updateTime": "2026-07-08T08:21:26"
}
```

## 2. 鉴权与用户

### 2.1 用户注册

```http
POST /user/api/register
```

是否需要登录：否

请求体：

```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "phone": "13800138000"
}
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "JWT_TOKEN"
  }
}
```

### 2.2 用户名密码登录

```http
POST /user/api/login
```

是否需要登录：否

请求体：

```json
{
  "username": "testuser",
  "password": "123456"
}
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "JWT_TOKEN"
  }
}
```

### 2.3 发送短信登录验证码

```http
POST /user/api/sms/login
```

是否需要登录：否

请求体：

```json
{
  "phone": "13800138000"
}
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "message": "验证码已发送"
  }
}
```

演示模式下可能返回：

```json
{
  "mockCode": "123456",
  "message": "演示模式：验证码为123456"
}
```

### 2.4 短信验证码登录

```http
POST /user/api/login/sms
```

是否需要登录：否

请求体：

```json
{
  "phone": "13800138000",
  "smsCode": "123456"
}
```

响应同用户名密码登录。

### 2.5 获取当前用户信息

```http
GET /user/api/info
```

是否需要登录：是

响应 data 字段：

```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "avatar": null,
  "phone": "13800138000",
  "email": null,
  "gender": 0,
  "userType": 1,
  "status": 1,
  "points": 0,
  "createTime": "2026-07-08T08:21:26",
  "updateTime": "2026-07-08T08:21:26"
}
```

用户类型：

```text
1 普通用户
2 技师
3 商家
4 管理员
```

### 2.6 获取个人资料

```http
GET /user/api/profile
```

是否需要登录：是

响应同用户信息。

### 2.7 更新个人资料

```http
PUT /user/api/profile
```

是否需要登录：是

请求体可传用户字段，建议 App 只传允许用户修改的字段：

```json
{
  "nickname": "新昵称",
  "avatar": "https://example.com/avatar.png",
  "phone": "13800138000",
  "email": "test@example.com",
  "gender": 1
}
```

## 3. 收货地址

地址字段：

```json
{
  "receiverName": "张三",
  "phone": "13800138000",
  "province": "湖北省",
  "city": "武汉市",
  "district": "洪山区",
  "detail": "光谷一路1号",
  "isDefault": 1
}
```

### 3.1 地址列表

```http
GET /user/api/address/list
```

是否需要登录：是

### 3.2 默认地址

```http
GET /user/api/address/default
```

是否需要登录：是

### 3.3 新增地址

```http
POST /user/api/address
```

是否需要登录：是

请求体见地址字段。

### 3.4 更新地址

```http
PUT /user/api/address/{id}
```

是否需要登录：是

请求体见地址字段。

### 3.5 删除地址

```http
DELETE /user/api/address/{id}
```

是否需要登录：是

### 3.6 设置默认地址

```http
POST /user/api/address/{id}/default
```

是否需要登录：是

## 4. 积分

### 4.1 查询积分

```http
GET /user/api/points
```

是否需要登录：是

响应 data：

```json
{
  "id": 1,
  "userId": 1,
  "totalPoints": 100,
  "usedPoints": 20,
  "availablePoints": 80
}
```

### 4.2 购买积分套餐

```http
POST /user/api/points/purchase
```

是否需要登录：是

请求体：

```json
{
  "packageId": 1
}
```

套餐：

```text
1 100积分
2 500积分
3 1000积分
```

响应 data：

```json
{
  "payNo": "PTPAY202607080001",
  "payForm": "<form ...>...</form>",
  "channel": "alipay"
}
```

说明：购买积分必须跳转支付宝支付。支付成功后由支付宝异步回调确认，系统再给用户账户增加积分，不允许前端直接判定到账。

### 4.3 查询积分支付状态

```http
GET /user/api/points/pay/status?payNo=PTPAY202607080001
```

是否需要登录：是

响应 data：

```json
{
  "payNo": "PTPAY202607080001",
  "packageId": 1,
  "points": 100,
  "amount": 10.00,
  "status": 1,
  "payChannel": "alipay",
  "thirdTradeNo": "202607082200..."
}
```

### 4.4 积分支付宝异步回调

```http
POST /user/api/points/pay/alipay/notify
```

是否需要登录：否

说明：该接口给支付宝服务器回调使用，App 不调用。

### 4.5 积分兑换

```http
POST /user/api/points/exchange/{exchangeId}
```

是否需要登录：是

### 4.6 积分兑换记录

```http
GET /user/api/points/exchanges?page=1&size=10
```

是否需要登录：是

响应记录字段：

```json
{
  "id": 1,
  "userId": 1,
  "itemName": "兑换物品",
  "points": 100,
  "status": "pending"
}
```

## 5. 社区帖子

帖子分类：

```text
1 技术交流
2 故障求助
3 经验分享
4 其他
```

### 5.1 帖子列表

```http
GET /community/api/post/list?page=1&size=10&category=1
```

是否需要登录：否。带 token 时后端可识别用户。

参数：

```text
page 页码，默认1
size 每页数量，默认10
category 分类，可选
```

响应记录字段：

```json
{
  "id": 1,
  "userId": 10,
  "title": "帖子标题",
  "content": "帖子内容",
  "category": 1,
  "viewCount": 0,
  "likeCount": 0,
  "commentCount": 0,
  "status": 1,
  "createTime": "2026-07-08T08:21:26",
  "updateTime": "2026-07-08T08:21:26"
}
```

### 5.2 帖子详情

```http
GET /community/api/post/{id}
```

是否需要登录：否

### 5.3 发布帖子

```http
POST /community/api/post
```

是否需要登录：是

请求体：

```json
{
  "title": "电池故障求助",
  "content": "车辆启动困难，仪表盘提示电池异常。",
  "category": 2
}
```

### 5.4 我的帖子

```http
GET /community/api/post/my?page=1&size=20
```

是否需要登录：是

### 5.5 帖子点赞

```http
POST /community/api/post/{id}/like
```

是否需要登录：是

说明：旧接口，直接增加点赞数。App 更推荐使用第 7 章的点赞 toggle 接口。

## 6. 社区评论

### 6.1 评论列表

```http
GET /community/api/comment/list?postId=1&page=1&size=10
```

是否需要登录：否

响应记录字段：

```json
{
  "id": 1,
  "postId": 1,
  "userId": 1,
  "parentId": 0,
  "content": "评论内容",
  "likeCount": 0,
  "createTime": "2026-07-08T08:21:26"
}
```

`parentId` 为 `0` 或 `null` 表示一级评论；有值表示回复某条评论。

### 6.2 发布评论/回复

```http
POST /community/api/comment
```

是否需要登录：是

请求体：

```json
{
  "postId": 1,
  "content": "我也遇到过，建议先检查电池接线。",
  "parentId": 0
}
```

回复评论时：

```json
{
  "postId": 1,
  "content": "谢谢，我去试一下。",
  "parentId": 12
}
```

### 6.3 我的评论

```http
GET /community/api/comment/my?page=1&size=20
```

是否需要登录：是

### 6.4 删除评论

```http
DELETE /community/api/comment/{id}
```

是否需要登录：是

## 7. 点赞与收藏

### 7.1 帖子点赞/取消点赞

```http
POST /community/api/like/post/{postId}
```

是否需要登录：是

说明：同一接口 toggle，未点赞时点赞，已点赞时取消。

### 7.2 检查帖子是否已点赞

```http
GET /community/api/like/post/{postId}/check
```

是否需要登录：可选。未登录返回 `false`。

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 7.3 评论点赞/取消点赞

```http
POST /community/api/like/comment/{commentId}
```

是否需要登录：是

### 7.4 检查评论是否已点赞

```http
GET /community/api/like/comment/{commentId}/check
```

是否需要登录：可选。未登录返回 `false`。

### 7.5 收藏/取消收藏帖子

```http
POST /community/api/collection/{postId}
```

是否需要登录：是

### 7.6 检查是否已收藏

```http
GET /community/api/collection/check/{postId}
```

是否需要登录：可选。未登录返回 `false`。

### 7.7 我的收藏

```http
GET /community/api/collection/my?page=1&size=20
```

是否需要登录：是

响应记录字段：

```json
{
  "id": 1,
  "postId": 1,
  "postTitle": "帖子标题",
  "postContent": "帖子内容",
  "createTime": "2026-07-08T08:21:26"
}
```

## 8. 配件商城

### 8.1 发布配件

```http
POST /parts/api/parts
```

是否需要登录：是

请求体：

```json
{
  "name": "叉车电池组",
  "description": "48V 新能源叉车电池组",
  "category": "电池",
  "brand": "宁德时代",
  "model": "48V-500Ah",
  "price": 12000.00,
  "stock": 10,
  "images": "https://example.com/1.png,https://example.com/2.png"
}
```

### 8.2 配件列表

```http
GET /parts/api/parts/list?page=1&size=12&category=电池&keyword=电池
```

是否需要登录：否

参数：

```text
page 默认1
size 默认12
category 可选
keyword 可选
```

响应记录字段：

```json
{
  "id": 1,
  "sellerId": 3,
  "name": "叉车电池组",
  "description": "48V 新能源叉车电池组",
  "category": "电池",
  "brand": "宁德时代",
  "model": "48V-500Ah",
  "price": 12000.00,
  "stock": 10,
  "images": "https://example.com/1.png",
  "status": 1,
  "salesCount": 0
}
```

### 8.3 配件详情

```http
GET /parts/api/parts/{id}
```

是否需要登录：否

## 9. 购物车

### 9.1 购物车列表

```http
GET /parts/api/parts/cart
```

是否需要登录：是

响应记录字段：

```json
{
  "id": 1,
  "partsId": 1,
  "name": "叉车电池组",
  "image": "https://example.com/1.png",
  "price": 12000.00,
  "stock": 10,
  "quantity": 2,
  "subtotal": 24000.00,
  "status": 1,
  "sellerId": 3
}
```

### 9.2 购物车数量

```http
GET /parts/api/parts/cart/count
```

是否需要登录：是

响应：

```json
{
  "count": 3
}
```

### 9.3 加入购物车

```http
POST /parts/api/parts/cart
```

是否需要登录：是

请求体：

```json
{
  "partsId": 1,
  "quantity": 2
}
```

### 9.4 修改购物车数量

```http
PUT /parts/api/parts/cart/{partsId}?quantity=3
```

是否需要登录：是

### 9.5 移除购物车商品

```http
DELETE /parts/api/parts/cart/{partsId}
```

是否需要登录：是

## 10. 下单、订单、物流

订单状态：

```text
0 待付款
1 待发货
2 待收货
3 已完成
4 已取消
```

物流状态：

```text
0 待揽收
1 运输中
2 派送中
3 已签收
```

### 10.1 预览订单

```http
POST /parts/api/parts/order/preview
```

是否需要登录：是

从购物车下单：

```json
{
  "cartItemIds": [1, 2],
  "addressId": 1,
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverAddress": "湖北省武汉市洪山区光谷一路1号",
  "remark": "尽快发货"
}
```

直接购买：

```json
{
  "directItems": [
    {
      "partsId": 1,
      "quantity": 1
    }
  ],
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverAddress": "湖北省武汉市洪山区光谷一路1号",
  "remark": "尽快发货"
}
```

响应 data：

```json
{
  "items": [],
  "totalAmount": 12000.00,
  "freightAmount": 0.00,
  "payAmount": 12000.00,
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverAddress": "湖北省武汉市洪山区光谷一路1号"
}
```

### 10.2 提交订单

```http
POST /parts/api/parts/order/submit
```

是否需要登录：是

请求体同预览订单。

响应：

```json
{
  "orderNos": ["PO202607080001"],
  "orderNo": "PO202607080001"
}
```

### 10.3 订单列表

```http
GET /parts/api/parts/order/list?page=1&size=10&status=0
```

是否需要登录：是

`status` 可选。

### 10.4 订单详情

```http
GET /parts/api/parts/order/{id}
```

是否需要登录：是

响应 data：

```json
{
  "order": {},
  "items": [],
  "payment": {},
  "shipment": {},
  "traces": []
}
```

### 10.5 按订单号查询详情

```http
GET /parts/api/parts/order/no/{orderNo}
```

是否需要登录：是

### 10.6 取消订单

```http
POST /parts/api/parts/order/{id}/cancel
```

是否需要登录：是

### 10.7 确认收货

```http
POST /parts/api/parts/order/{id}/confirm
```

是否需要登录：是

### 10.8 商家发货

```http
POST /parts/api/parts/order/{id}/ship
```

是否需要登录：是

请求体：

```json
{
  "carrier": "顺丰速运",
  "trackingNo": "SF1234567890",
  "location": "武汉分拨中心"
}
```

### 10.9 追加物流轨迹

```http
POST /parts/api/parts/order/{id}/trace
```

是否需要登录：是

请求体：

```json
{
  "location": "武汉市洪山区",
  "description": "快件正在派送中"
}
```

## 11. 支付

支付状态：

```text
0 待支付
1 支付成功
2 支付失败
```

### 11.1 创建支付单

```http
POST /parts/api/parts/pay/create?orderId=1
```

是否需要登录：是

响应 data：

```json
{
  "id": 1,
  "orderId": 1,
  "orderNo": "PO202607080001",
  "payNo": "PAY202607080001",
  "payChannel": "alipay",
  "amount": 12000.00,
  "status": 0,
  "payTime": null,
  "thirdTradeNo": null
}
```

### 11.2 发起支付宝网页支付

```http
POST /parts/api/parts/pay/alipay/page?payNo=PAY202607080001
```

是否需要登录：是

响应 data：

```json
{
  "payNo": "PAY202607080001",
  "payForm": "<form ...>...</form>",
  "channel": "alipay"
}
```

App 端处理建议：

```text
1. 如果做 H5 支付：用 WebView 加载 payForm 自动提交。
2. 如果做支付宝 App 原生支付：后端需要另增 alipay appPay 接口，返回 orderString 给客户端调支付宝 SDK。
3. 当前已有接口是 pagePay，更适合网页/H5。
```

### 11.3 查询支付状态

```http
GET /parts/api/parts/pay/status?payNo=PAY202607080001
```

是否需要登录：是

响应 data：

```json
{
  "payNo": "PAY202607080001",
  "orderNo": "PO202607080001",
  "orderId": 1,
  "status": 1,
  "payChannel": "alipay",
  "thirdTradeNo": "202607082200..."
}
```

### 11.4 支付宝异步回调

```http
POST /parts/api/parts/pay/alipay/notify
```

是否需要登录：否

说明：该接口给支付宝服务器回调使用，App 不调用。

## 12. 维修服务工单

服务类型：

```text
1 维修
2 保养
3 咨询
```

工单状态：

```text
0 待接单
1 已接单
2 服务中
3 已完成
4 已取消
```

### 12.1 创建服务工单

```http
POST /service/api/service/order
```

是否需要登录：是

请求体：

```json
{
  "serviceType": 1,
  "title": "叉车无法启动",
  "description": "车辆早上无法启动，仪表盘有故障码。",
  "images": "https://example.com/a.png,https://example.com/b.png",
  "address": "湖北省武汉市洪山区光谷一路1号",
  "phone": "13800138000"
}
```

### 12.2 我的服务工单

```http
GET /service/api/service/order/list
```

是否需要登录：是

### 12.3 工单详情

```http
GET /service/api/service/order/{id}
```

是否需要登录：是

### 12.4 取消工单

```http
POST /service/api/service/order/{id}/cancel
```

是否需要登录：是

### 12.5 技师接单

```http
POST /service/api/service/order/{id}/accept
```

是否需要登录：是

### 12.6 技师完成工单

```http
POST /service/api/service/order/{id}/complete
```

是否需要登录：是

## 13. 知识库

文档访问类型：

```text
0 免费
1 积分解锁
2 付费解锁
3 积分或付费解锁
```

文档状态：

```text
0 草稿
1 已发布
2 已下架
```

### 13.1 文档列表

```http
GET /knowledge/api/knowledge/doc/list?page=1&size=12&category=维修手册&keyword=电池
```

是否需要登录：否。带 token 时可返回当前用户是否已解锁。

响应记录字段：

```json
{
  "id": 1,
  "title": "电池维修手册",
  "summary": "新能源叉车电池检修说明",
  "category": "维修手册",
  "fileName": "manual.pdf",
  "fileSize": 102400,
  "fileType": "pdf",
  "accessType": 0,
  "pointsPrice": 0,
  "moneyPrice": 0.00,
  "status": 1,
  "downloadCount": 0,
  "viewCount": 0,
  "createTime": "2026-07-08T08:21:26",
  "unlocked": true,
  "accessLabel": "免费"
}
```

### 13.2 文档分类

```http
GET /knowledge/api/knowledge/doc/categories
```

是否需要登录：否

响应 data：

```json
["维修手册", "技术规范", "培训资料", "其他"]
```

### 13.3 文档详情

```http
GET /knowledge/api/knowledge/doc/{id}
```

是否需要登录：否。带 token 时可返回解锁状态。

### 13.4 积分解锁文档

```http
POST /knowledge/api/knowledge/doc/{id}/unlock/points
```

是否需要登录：是

### 13.5 支付宝解锁文档

```http
POST /knowledge/api/knowledge/doc/{id}/unlock/alipay
```

是否需要登录：是

响应 data：

```json
{
  "payNo": "KNPAY202607080001",
  "payForm": "<form ...>...</form>"
}
```

### 13.6 下载文档

```http
GET /knowledge/api/knowledge/doc/{id}/download
```

是否需要登录：可选。付费/积分权限文档需要登录并已解锁。

响应：文件流，`Content-Disposition: attachment`。

### 13.7 预览文档

```http
GET /knowledge/api/knowledge/doc/{id}/preview
```

是否需要登录：可选。付费/积分权限文档需要登录并已解锁。

响应：文件流，`Content-Disposition: inline`。

支持的预览类型由后端 Content-Type 判断：

```text
pdf, png, jpg, jpeg, gif, txt, md, csv, log, json, html, htm
```

其他类型可能返回 `application/octet-stream`，App 可选择下载或交给系统打开。

### 13.8 知识库支付宝回调

```http
POST /knowledge/api/knowledge/pay/alipay/notify
```

是否需要登录：否

说明：给支付宝服务器回调使用，App 不调用。

## 14. 知识库管理

说明：这些接口用于后台管理端或管理员 App，普通用户端可以不做。

### 14.1 管理端文档列表

```http
GET /knowledge/api/knowledge/admin/doc/list?page=1&size=20&status=1
```

是否需要登录：是

### 14.2 上传文档

```http
POST /knowledge/api/knowledge/admin/doc
Content-Type: multipart/form-data
```

是否需要登录：是

表单字段：

```text
file 文件
meta JSON对象
```

`meta` 示例：

```json
{
  "title": "电池维修手册",
  "summary": "新能源叉车电池检修说明",
  "category": "维修手册",
  "accessType": 1,
  "pointsPrice": 50,
  "moneyPrice": 9.90
}
```

### 14.3 更新文档信息

```http
PUT /knowledge/api/knowledge/admin/doc/{id}
```

是否需要登录：是

请求体同 `meta`。

### 14.4 发布文档

```http
POST /knowledge/api/knowledge/admin/doc/{id}/publish
```

是否需要登录：是

### 14.5 下架文档

```http
POST /knowledge/api/knowledge/admin/doc/{id}/offline
```

是否需要登录：是

### 14.6 删除文档

```http
DELETE /knowledge/api/knowledge/admin/doc/{id}
```

是否需要登录：是

## 15. AI 助手

### 15.1 普通问答

```http
POST /agent/api/agent/chat
```

是否需要登录：否。带 token 时可结合当前用户权限和上下文。

请求体：

```json
{
  "question": "叉车电池充不进电怎么办？",
  "scope": "all",
  "sessionId": "session-001",
  "stream": false
}
```

字段说明：

```text
question 必填，最长500字
scope 可选：all / knowledge / community
sessionId 可选，多轮会话ID，下一轮带回
stream 可选，普通接口建议 false
```

响应 data：

```json
{
  "answer": "建议先检查充电器、电池接口和BMS告警...",
  "llmUsed": true,
  "sources": [
    {
      "type": "knowledge",
      "id": 1,
      "title": "电池维修手册",
      "snippet": "相关片段",
      "unlocked": true,
      "link": "/knowledge/1",
      "score": 0.82
    }
  ],
  "sessionId": "session-001",
  "traceId": "trace-xxx",
  "promptTokens": 100,
  "completionTokens": 200,
  "costYuan": 0.01
}
```

### 15.2 SSE 流式问答

```http
POST /agent/api/agent/chat/stream
Accept: text/event-stream
```

是否需要登录：否。带 token 时可增强权限判断。

请求体同普通问答。

App 端建议：

```text
Android 可用 OkHttp SSE / EventSource
iOS 可用 URLSession bytes 或第三方 EventSource
```

### 15.3 AI 指标

```http
GET /agent/api/agent/metrics
```

是否需要登录：否

### 15.4 AI 健康检查

```http
GET /agent/api/agent/health
```

是否需要登录：否

## 16. 健康检查

```http
GET /user/api/test
GET /agent/api/agent/health
```

其他服务也暴露 actuator，但 App 一般不需要调用。

## 17. App 开发建议

### 17.1 登录态

App 登录后保存 `token`，后续所有需要登录的接口统一加：

```http
Authorization: Bearer <token>
```

遇到 HTTP 401 或业务 `code=401` 时，清空本地 token 并跳转登录页。

### 17.2 支付

当前支付接口已支持支付宝网页支付 `pagePay`，返回 `payForm`。原生 App 有两种接法：

```text
方案A：WebView 加载 payForm，开发成本最低。
方案B：后端新增支付宝 App 支付接口，返回 orderString，App 调支付宝 SDK，体验最好。
```

如果要上架正式 App，建议最终使用方案B。

### 17.3 文件预览/下载

预览和下载接口返回文件流，不是统一 JSON。App 需要按 `Content-Type` 判断：

```text
application/pdf 用 PDF 预览
image/* 用图片预览
text/plain / application/json / text/html 可文本/WebView 预览
application/octet-stream 建议下载或系统打开
```

### 17.4 图片字段

当前部分业务图片字段是字符串，多个图片使用英文逗号分隔，例如：

```text
https://a.com/1.png,https://a.com/2.png
```

App 展示时按逗号切分。

## 18. 内部接口说明

以下接口主要给服务内部调用，App 正常不需要直接调用：

```http
GET  /user/api/internal/user/brief
POST /user/api/internal/points/consume
POST /user/api/internal/points/add
POST /parts/api/parts/pay/alipay/notify
POST /knowledge/api/knowledge/pay/alipay/notify
```

其中支付宝 notify 接口只给支付宝服务器回调使用。
