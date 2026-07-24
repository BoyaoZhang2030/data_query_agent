# 橙选商城与电商问数智能体

## 项目简介

本项目是一个前后端分离的电商综合系统，由“用户商城端”和“管理员数据工作台”两部分组成。

普通访客打开网站后首先进入橙选商城，可以浏览商品、查看实时库存、搜索和筛选商品。用户完成注册或登录后，可以使用购物车、提交订单并查看自己的订单。

管理员需要从商城首页右上角的“管理员系统”入口登录。登录成功后进入电商数据工作台，可以管理商品、库存、分类和订单，还可以使用自然语言查询数据库、自动生成图表、导出查询结果，并调用 DeepSeek 对数据进行总结与分析。

项目的主要目标包括：

- 提供接近真实电商平台的用户购物流程；
- 提供独立的库存与订单管理后台；
- 让非技术用户通过自然语言查询电商数据；
- 将查询结果自动转换为图表和分析结论；
- 使商城下单与后台库存数据保持同步。

---

## 系统角色与入口

### 游客

游客访问 `http://localhost:3000` 后直接进入用户商城，可以：

- 浏览全部商品；
- 查看商品价格、描述和实时库存；
- 按分类筛选商品；
- 搜索商品；
- 按价格或库存排序；
- 将商品加入购物车。

提交订单前需要登录。游客购物车会保存在浏览器本地，登录后仍然保留。

### 普通用户

普通用户注册或登录后进入 `/shop`，除游客功能外还可以：

- 调整购物车商品数量；
- 填写收货地址；
- 选择支付方式；
- 提交销售订单；
- 查看自己的历史订单和商品明细。

普通用户不能访问管理员数据工作台。如果尝试进入管理员路由，前端会自动跳转回商城。

### 管理员

管理员从商城顶部的“管理员系统”进入专用登录页面，登录后进入 `/dashboard`。

管理员可以：

- 查看经营数据仪表盘；
- 查看和管理商品；
- 商品入库、出库及库存调整；
- 修改商品价格；
- 管理商品分类；
- 创建采购订单或销售订单；
- 修改订单状态；
- 查看自然语言查询和 SQL 查询结果；
- 自动生成柱状图、折线图和饼图；
- 导出 CSV 或 Excel；
- 使用 DeepSeek 生成数据总结和经营建议。

管理员登录入口会验证账号角色，普通用户账号不能从该入口进入后台。

---

## 页面访问流程

```text
访问 http://localhost:3000
        │
        ▼
用户商城首页 /
        │
        ├── 浏览商品、搜索、分类筛选、加入购物车
        │
        ├── 用户登录 /login
        │       └── 普通用户登录成功 → /shop
        │
        ├── 用户注册 /register
        │       └── 注册成功并自动登录 → /shop
        │
        └── 管理员系统 /login?mode=admin
                └── 管理员登录成功 → /dashboard
```

主要路由：

| 路径 | 页面 | 访问权限 |
| --- | --- | --- |
| `/` | 用户商城首页 | 公开 |
| `/login` | 普通用户登录 | 公开 |
| `/register` | 用户注册 | 公开 |
| `/login?mode=admin` | 管理员专用登录 | 公开 |
| `/shop` | 登录后的商城 | 普通用户 |
| `/shop/orders` | 我的订单 | 普通用户 |
| `/dashboard` | 管理员仪表盘 | 管理员 |
| `/query` | 智能数据查询 | 管理员 |
| `/products` | 商品与库存管理 | 管理员 |
| `/orders` | 订单管理 | 管理员 |
| `/categories` | 分类管理 | 管理员 |

---

## 核心功能

### 1. 用户注册与登录

- 支持用户名、邮箱和密码注册；
- 检查用户名是否重复；
- 检查邮箱是否重复；
- 校验用户名、邮箱和密码格式；
- 注册成功后自动登录；
- 新注册用户默认角色为 `user`；
- 密码使用 BCrypt 加密后存入数据库；
- 兼容旧版明文测试账号，旧账号首次成功登录后会自动升级为 BCrypt 密码；
- 用户信息保存在浏览器 `localStorage`，用于前端路由和页面状态判断。

### 2. 用户商城

- 淘宝风格橙色商城界面；
- 展示商品名称、描述、价格、库存和分类；
- 售罄商品禁止加入购物车；
- 低库存商品显示库存提醒；
- 支持商品名称和描述搜索；
- 支持一级分类及其子分类筛选；
- 支持综合排序、价格升序和库存优先；
- 使用响应式布局，适配常见桌面和移动端宽度。

### 3. 购物车和结算

- 购物车保存在浏览器本地；
- 支持增加、减少和删除商品；
- 购买数量不能超过数据库当前库存；
- 自动计算商品数量和订单总金额；
- 提交订单时填写收货地址；
- 支持支付宝、微信支付和货到付款选项；
- 下单时后端再次检查库存，避免库存出现负数；
- 下单成功后自动扣减库存；
- 商城库存和管理员后台库存使用同一份数据库数据。

### 4. 用户订单

- 普通用户只能请求和展示自己的订单；
- 支持按订单状态切换查看；
- 显示订单号、创建时间、状态和总金额；
- 显示订单中的商品名称、数量、单价和小计；
- 显示收货信息和支付方式。

### 5. 管理员数据工作台

- 数据概览仪表盘；
- 商品列表和库存管理；
- 商品分类管理；
- 采购入库与销售出库；
- 订单搜索、状态修改和删除；
- 库存、订单和商城数据实时关联。

### 6. 智能问数

管理员可以使用两种方式查询数据：

1. 自然语言查询：输入“查询销量最高的前 10 个商品”等问题，由 DeepSeek 转换为只读 SQL；
2. SQL 查询：直接输入 `SELECT` 查询语句。

安全限制：

- 只允许执行 `SELECT`；
- 拦截 `DELETE`、`UPDATE`、`DROP`、`INSERT` 等危险操作；
- 限制 SQL 长度；
- 拦截注释和部分特殊字符；
- 只允许查询项目业务表；
- 避免在查询结果中返回用户密码。

### 7. 自动图表和结果导出

查询成功后，前端会自动识别：

- 一个分类或文本字段；
- 一个数值字段。

识别成功后自动生成图表，并支持：

- 柱状图；
- 折线图；
- 饼图；
- CSV 导出；
- Excel 兼容格式导出。

### 8. AI 数据分析

点击查询结果区域的“AI 分析”按钮后：

- 后端截取最多 50 行查询数据；
- 将用户问题和查询结果发送给 DeepSeek；
- 生成核心结论、数据洞察和业务建议；
- 不允许 AI 编造查询结果中不存在的数据。

如果没有配置 DeepSeek API Key，或者远程调用失败，系统会自动退回本地统计摘要，计算数值字段的合计、平均值、最小值和最大值。

---

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.5
- Spring Web
- Spring Security
- MyBatis-Plus 3.5.8
- MySQL 8.0+
- Spring JDBC
- BCrypt
- Lombok
- Maven
- DeepSeek Chat Completions API

### 前端

- Vue 3
- Vue Router 4
- Element Plus
- Axios
- ECharts
- Vite 5
- HTML / CSS / JavaScript

---

## 项目结构

```text
data_query_agent_shop_home_v2/
├── backend/
│   ├── pom.xml                         # Maven 项目配置
│   └── src/main/
│       ├── java/com/example/dataquery/
│       │   ├── DataQueryAgentApplication.java  # Spring Boot 启动类
│       │   ├── annotation/
│       │   │   └── RateLimit.java      # 限流注解
│       │   ├── config/
│       │   │   ├── DefaultAccountConfig.java   # 默认管理员账号
│       │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│       │   │   ├── SecurityConfig.java         # 安全及 BCrypt 配置
│       │   │   ├── SpringAiConfig.java         # AI 配置说明
│       │   │   └── WebConfig.java              # Web 与跨域配置
│       │   ├── controller/
│       │   │   ├── AuthController.java         # 注册、登录、用户资料
│       │   │   ├── DataManageController.java   # 商品、分类、订单接口
│       │   │   ├── DataQueryController.java    # 问数、SQL、AI 分析接口
│       │   │   └── StatisticsController.java   # 仪表盘统计接口
│       │   ├── dao/
│       │   │   ├── UserMapper.java
│       │   │   ├── ProductMapper.java
│       │   │   ├── CategoryMapper.java
│       │   │   ├── OrderMapper.java
│       │   │   ├── OrderItemMapper.java
│       │   │   └── QueryHistoryMapper.java
│       │   ├── dto/
│       │   │   ├── CreateOrderRequest.java     # 创建订单请求
│       │   │   └── OrderProductRequest.java    # 订单商品请求
│       │   ├── interceptor/
│       │   │   └── RateLimitInterceptor.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Product.java
│       │   │   ├── Category.java
│       │   │   ├── Order.java
│       │   │   ├── OrderItem.java
│       │   │   └── QueryHistory.java
│       │   ├── service/
│       │   │   ├── impl/                # Service 实现
│       │   │   └── *.java               # Service 接口
│       │   └── util/
│       │       ├── Result.java           # 统一响应结构
│       │       └── SqlFieldMapper.java   # 查询字段中文映射
│       └── resources/
│           ├── application.yml          # 数据库、DeepSeek、端口配置
│           └── init.sql                 # 数据库建表及测试数据
│
├── frontend/
│   ├── package.json                     # 前端依赖和命令
│   ├── vite.config.js                   # Vite、端口及 API 代理
│   ├── index.html
│   └── src/
│       ├── main.js                      # Vue 入口
│       ├── App.vue                      # 根布局和管理员侧边栏
│       ├── api/
│       │   └── index.js                 # Axios 与全部 API 封装
│       ├── router/
│       │   └── index.js                 # 路由、登录检查和角色分流
│       └── views/
│           ├── Shop.vue                 # 公开商城、购物车和结算
│           ├── ShopOrders.vue           # 普通用户订单
│           ├── Login.vue                # 用户/管理员双模式登录
│           ├── Register.vue             # 注册并自动登录
│           ├── Dashboard.vue            # 管理员仪表盘
│           ├── Products.vue             # 商品和库存管理
│           ├── Categories.vue           # 分类管理
│           ├── Orders.vue               # 订单与入出库管理
│           ├── Query.vue                # 查询、图表、导出和 AI 分析
│           └── Welcome.vue              # 早期欢迎页，当前不作为根路由
│
└── README.md
```

---

## 数据库结构

核心数据表：

| 表名 | 作用 |
| --- | --- |
| `users` | 用户、管理员账号及角色 |
| `categories` | 商品分类及层级关系 |
| `products` | 商品、价格、描述和实时库存 |
| `orders` | 订单主信息 |
| `order_items` | 订单商品明细 |
| `query_history` | 用户的问数与 SQL 查询历史 |

库存变更逻辑：

- 商城销售订单：减少商品库存；
- 管理员销售出库：减少商品库存；
- 管理员采购入库：增加商品库存；
- 删除订单时根据订单类型恢复或扣回库存；
- 后端使用事务处理订单和库存，失败时整体回滚。

---

## API 概览

所有后端接口统一使用 `/api` 作为上下文路径。

### 认证接口

| 方法 | 地址 | 功能 |
| --- | --- | --- |
| `POST` | `/api/auth/register` | 用户注册 |
| `POST` | `/api/auth/login` | 用户或管理员登录 |
| `GET` | `/api/auth/profile` | 获取用户资料 |
| `PUT` | `/api/auth/profile` | 修改用户资料 |

### 商品、分类和订单

| 方法 | 地址 | 功能 |
| --- | --- | --- |
| `GET` | `/api/data/products` | 查询商品 |
| `POST` | `/api/data/products` | 新增商品 |
| `PUT` | `/api/data/products/{id}/stock` | 调整库存 |
| `PUT` | `/api/data/products/{id}/price` | 修改价格 |
| `GET` | `/api/data/categories` | 查询分类 |
| `GET` | `/api/data/orders` | 查询订单 |
| `GET` | `/api/data/orders/{id}/items` | 查询订单明细 |
| `POST` | `/api/data/orders` | 创建订单 |
| `PUT` | `/api/data/orders/{id}/status` | 修改订单状态 |
| `DELETE` | `/api/data/orders/{id}` | 删除订单 |

### 智能查询

| 方法 | 地址 | 功能 |
| --- | --- | --- |
| `POST` | `/api/query/natural-language` | 自然语言生成 SQL 并查询 |
| `POST` | `/api/query/sql` | 执行只读 SQL |
| `POST` | `/api/query/analyze` | AI 分析查询结果 |
| `GET` | `/api/query/history` | 查询历史 |
| `GET` | `/api/query/templates` | 常用查询模板 |

---

## Quick Start

### 1. 数据库初始化

1. 安装 MySQL 8.0 或更高版本；
2. 启动 MySQL；
3. 执行 `backend/src/main/resources/init.sql`；
4. 确认已创建 `data_query_agent` 数据库及相关数据表。

### 2. 后端配置

打开：

```text
backend/src/main/resources/application.yml
```

修改数据库用户名和密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/data_query_agent?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码
```

建议通过环境变量配置 DeepSeek API Key，不要将真实密钥写入代码：

```powershell
$env:DEEPSEEK_API_KEY="你的DeepSeek API Key"
```

### 3. 后端运行

进入后端目录：

```powershell
cd backend
```

构建项目：

```powershell
mvn clean install
```

启动后端：

```powershell
mvn spring-boot:run
```

启动成功后，终端会显示：

```text
Started DataQueryAgentApplication
```

后端默认地址：

```text
http://localhost:8080/api
```

### 4. 前端配置

另开一个终端并进入前端目录：

```powershell
cd frontend
```

安装依赖：

```powershell
npm install
```

### 5. 前端运行

```powershell
npm run dev
```

浏览器访问：

```text
http://localhost:3000
```

前端通过 Vite 将 `/api` 请求代理到 `http://localhost:8080`。

---

## 测试账号

默认管理员账号：

```text
用户名：bf
密码：123456
```

数据库初始化脚本中还可能包含：

```text
用户名：admin
密码：123456
```

管理员应从商城顶部的“管理员系统”入口登录。

普通用户可以使用注册页面自行创建。注册成功后会自动登录并进入商城。

---

## DeepSeek 配置

`application.yml` 默认配置：

```yaml
deepseek:
  api-key: ${DEEPSEEK_API_KEY:}
  base-url: https://api.deepseek.com
  model: deepseek-v4-flash
```

当前 PowerShell 临时配置：

```powershell
$env:DEEPSEEK_API_KEY="sk-xxxxxxxx"
mvn spring-boot:run
```

如果在 IDEA 中启动后端，可在运行配置的 `Environment variables` 中添加：

```text
DEEPSEEK_API_KEY=sk-xxxxxxxx
```

修改环境变量后需要重启后端。

---

## 局域网访问

前端 Vite 已监听 `0.0.0.0`。同一局域网中的其他设备可以使用本机 IPv4 地址访问：

```text
http://你的IPv4地址:3000
```

例如：

```text
http://192.168.1.100:3000
```

需要满足：

- 前后端保持运行；
- 双方连接同一局域网；
- Windows 防火墙允许 3000 端口；
- 路由器未开启客户端隔离。

不建议直接将 MySQL 的 3306 端口暴露到公网。

---

## 常见问题

### 1. `mvn` 无法识别

说明 Maven 没有加入环境变量。临时设置：

```powershell
$env:JAVA_HOME="D:\apps\Dev\JDK\17ins"
$env:Path="$env:JAVA_HOME\bin;D:\apps\Javamaven\apache-maven-3.6.3-bin\apache-maven-3.6.3\bin;$env:Path"
```

然后检查：

```powershell
java -version
mvn -version
```

### 2. npm 提示找不到 `package.json`

说明命令执行目录错误。必须进入 `frontend`：

```powershell
cd frontend
npm install
npm run dev
```

### 3. Maven 提示找不到 `pom.xml`

必须进入 `backend`：

```powershell
cd backend
mvn clean install
```

### 4. 8080 端口被占用

不要同时使用 IDEA 运行按钮和 `mvn spring-boot:run` 启动两个后端。

检查端口：

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

### 5. 页面可以打开，但接口请求失败

依次确认：

- MySQL 已启动；
- 后端终端显示 `Started DataQueryAgentApplication`；
- 后端运行在 8080；
- 前端运行在 3000；
- `vite.config.js` 的代理目标仍为 `http://localhost:8080`。

### 6. AI 查询失败

确认：

- 已设置 `DEEPSEEK_API_KEY`；
- DeepSeek 账户有可用额度；
- 修改环境变量后已重启后端；
- 当前网络能够访问 DeepSeek API。

即使没有 DeepSeek API Key，SQL 查询、商城、库存和订单功能仍然可以使用，查询结果分析会退回本地统计摘要。

---

## 安全与使用说明

- 不要将真实数据库密码或 API Key 提交到公开仓库；
- 生产环境应使用 HTTPS；
- 生产环境不应仅依赖前端 `localStorage` 判断权限，应增加 JWT 或服务端 Session；
- 管理员接口目前主要通过前端路由进行角色隔离，正式部署前建议在后端增加接口级权限检查；
- 当前 Excel 导出为 Excel 可打开的兼容格式，适合项目演示和常规数据导出；
- 商城商品图片目前使用类型图标和渐变占位图，后续可为 `products` 表增加图片 URL 字段。

---

## 后续可扩展方向

- 商品详情页和真实商品图片；
- 收货地址管理；
- 收藏夹和浏览记录；
- 模拟支付及支付状态流转；
- 订单取消、确认收货和退款；
- JWT 登录认证；
- 后端管理员权限校验；
- 商品分页和服务端搜索；
- Redis 购物车；
- 优惠券和促销活动；
- 销售预测与库存预警；
- Docker、Nginx 和云服务器部署。
