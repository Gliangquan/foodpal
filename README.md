# FoodPal - 食堂管理系统

一个完整的食堂管理系统，包含后端服务、Web 前端和 UniApp 移动端。

## 项目结构

```text
FoodPal/
├── FoodPal-backend/      # Spring Boot 后端服务
├── FoodPal-frontend/     # Vue 3 + Vite Web 前端
└── FoodPal-uniapp/       # UniApp 移动端
```

## 技术栈

### 后端 (FoodPal-backend)
- Spring Boot 2.7.x
- MySQL 8.0
- MyBatis Plus
- JWT 认证
- MinIO 对象存储
- WebSocket 实时通信

### Web 前端 (FoodPal-frontend)
- Vue 3
- Vite
- Ant Design Vue
- TypeScript
- Axios

### 移动端 (FoodPal-uniapp)
- UniApp
- Vue 3
- uView UI

## 环境要求

- JDK 11+
- Node.js 16+
- MySQL 8.0+
- MinIO（可选，用于文件存储）

## 配置说明

### 1. 后端配置

配置文件：`FoodPal-backend/src/main/resources/application.yml`

当前本地默认数据库连接已固定为 Docker 映射到宿主机的 MySQL：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3307/foodpal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
    username: root
    password: root
```

如需修改，请直接编辑：

- `FoodPal-backend/src/main/resources/application.yml`

### 2. Web 前端配置

开发代理配置文件：

- `FoodPal-frontend/vite.config.js`

当前开发代理目标：

```text
http://localhost:19941
```

### 3. UniApp 配置

开发配置文件：

- `FoodPal-uniapp/vite.config.js`
- `FoodPal-uniapp/utils/request.js`

当前 H5 开发端口：

```text
19943
```

## 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE foodpal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入 SQL 文件：

```bash
mysql -h 127.0.0.1 -P 3307 -u root -p foodpal < FoodPal-backend/sql/init_foodpal.sql
```

## 运行项目

### 1. 启动后端

```bash
cd FoodPal-backend
mvn clean install
mvn spring-boot:run
```

后端服务运行地址：

```text
http://localhost:19941/api
```

### 2. 启动 Web 前端

```bash
cd FoodPal-frontend
npm install
npm run dev
```

Web 前端默认由 Vite 启动，具体端口以终端输出为准。

### 3. 启动 UniApp

使用 HBuilderX 打开：

```text
FoodPal-uniapp
```

或命令行：

```bash
cd FoodPal-uniapp
npm install --legacy-peer-deps
npm run dev:h5
```

UniApp H5 本地地址：

```text
http://localhost:19943/
```

### 4. 构建 UniApp H5

```bash
cd FoodPal-uniapp
npm install --legacy-peer-deps
npm run build:h5
```

本地预览：

```bash
npm run preview:h5
```

预览端口：

```text
19943
```

## Docker / Nginx 代理端口

当前项目内已统一为：

- 后端容器端口：`19941`
- UniApp H5 Nginx 监听端口：`19943`
- Web 前端 Nginx 反向代理后端：`foodpal-backend:19941`
- UniApp H5 Nginx 反向代理后端：`foodpal-backend:19941`

## 默认账号

系统预置测试账号以数据库实际导入数据为准。若未导入演示数据，请自行注册或插入测试账号。

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

## 开发说明

- 后端 API 文档：启动后访问 `http://localhost:19941/api/doc.html#/home`
- WebSocket 地址：`ws://localhost:19941/ws`
- 项目中历史目录名 `food-backend / food-frontend / food-uniapp` 已通过软链接兼容旧引用

## 许可证

MIT License
