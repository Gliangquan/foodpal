# FoodPal - 食堂管理系统

一个完整的食堂管理系统，包含后端服务、Web 前端和 UniApp 移动端。

## 项目结构

```
FoodPal/
├── food-backend/      # Spring Boot 后端服务
├── food-frontend/     # Vue 3 + Vite Web 前端
└── food-uniapp/       # UniApp 移动端
```

## 技术栈

### 后端 (food-backend)
- Spring Boot 2.7.x
- MySQL 8.0
- MyBatis Plus
- JWT 认证
- MinIO 对象存储
- WebSocket 实时通信

### Web 前端 (food-frontend)
- Vue 3
- Vite
- Ant Design Vue
- TypeScript
- Axios

### 移动端 (food-uniapp)
- UniApp
- Vue 3
- uView UI

## 环境要求

- JDK 11+
- Node.js 16+
- MySQL 8.0+
- MinIO (可选，用于文件存储)

## 配置说明

### 1. 后端配置

修改 `food-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/foodpal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
    username: root
    password: 你的数据库密码  # 修改这里

minio:
  endpoint: http://localhost:9000
  access-key: 你的MinIO访问密钥  # 修改这里
  secret-key: 你的MinIO密钥      # 修改这里
  bucket-name: foodpal

jwt:
  secret: 你的JWT密钥（至少256位）  # 修改这里

wechat:
  app-id: 你的微信AppID
  secret: 你的微信AppSecret  # 修改这里
```

**或者使用环境变量（推荐）：**

```bash
export DATASOURCE_PASSWORD=你的数据库密码
export MINIO_ACCESS_KEY=你的MinIO访问密钥
export MINIO_SECRET_KEY=你的MinIO密钥
export JWT_SECRET=你的JWT密钥
export WECHAT_SECRET=你的微信AppSecret
```

### 2. 前端配置

修改 `food-frontend/.env`（如果需要修改后端地址）：

```env
VITE_API_BASE_URL=http://localhost:9901/api
```

### 3. 移动端配置

修改 `food-uniapp/utils/request.js` 中的 `baseURL`（如果需要）。

## 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE foodpal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入 SQL 文件：
```bash
mysql -u root -p foodpal < food-backend/sql/init_foodpal.sql
```

如需导入完整演示数据，可继续执行：
```bash
mysql -u root -p foodpal < food-backend/sql/seed_foodpal_full.sql
```

## 运行项目

### 1. 启动后端

```bash
cd food-backend
mvn clean install
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:9901`

### 2. 启动 Web 前端

```bash
cd food-frontend
npm install
npm run dev
```

前端将运行在 `http://localhost:9991`

### 3. 启动移动端

使用 HBuilderX 打开 `food-uniapp` 目录，运行到浏览器或手机。

### 4. 构建 UniApp H5

```bash
cd food-uniapp
npm install --legacy-peer-deps
npm run build:h5
```

如需本地预览：

```bash
npm run preview:h5
```

默认预览端口：`8080`

## 默认账号

系统预置了以下测试账号：

- **管理员**: admin / admin123
- **监管员**: supervisor001 / supervisor123
- **商户**: merchant001 / merchant123
- **学生**: student001 / student123

## 功能模块

### 管理员端
- 用户管理
- 商户管理（含审核功能）
- 菜品管理
- 投诉处理
- 动态内容管理
- 公告管理
- 数据统计

### 商户端
- 店铺信息管理
- 菜品管理
- 特价活动设置
- 投诉查看与整改
- 动态发布

### 学生端
- 浏览菜品
- 收藏/点赞菜品
- 提交投诉
- 查看动态和公告
- 查看投诉处理进度

## 注意事项

1. **数据库密码**: 必须修改 `application.yml` 中的数据库密码
2. **JWT 密钥**: 生产环境必须使用强密钥（至少 256 位）
3. **MinIO 配置**: 如果不使用 MinIO，需要修改文件上传逻辑
4. **微信配置**: 如果使用微信登录，需要配置微信 AppID 和 AppSecret
5. **端口冲突**: 确保 9901（后端）和 9991（前端）端口未被占用

## 开发说明

- 后端 API 文档: 启动后访问 `http://localhost:9901/doc.html`
- 前端开发服务器会自动代理 `/api` 请求到后端
- WebSocket 连接地址: `ws://localhost:9901/ws`

## 许可证

MIT License
