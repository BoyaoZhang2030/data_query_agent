# 电商问数智能体

## 项目简介

电商问数智能体是一个基于Spring AI Alibaba + Vue 3的智能数据分析系统，旨在通过自然语言交互，让用户能够轻松查询和分析电商数据，无需专业的SQL知识。

## 技术栈

### 后端技术
- Spring Boot 3.0+
- Spring AI Alibaba
- MySQL 8.0+
- MyBatis-Plus
- Spring Security

### 前端技术
- Vue 3
- Element Plus
- ECharts
- Axios
- Vite

## 项目结构

```
├── backend/            # 后端项目
│   ├── src/            # 源代码
│   │   ├── main/       # 主代码
│   │   │   ├── java/com/example/dataquery/  # Java代码
│   │   │   │   ├── controller/  # 控制器
│   │   │   │   ├── service/     # 服务层
│   │   │   │   ├── dao/         # 数据访问层
│   │   │   │   ├── model/       # 数据模型
│   │   │   │   └── config/      # 配置类
│   │   │   └── resources/  # 资源文件
│   │   │       ├── init.sql  # 数据库初始化脚本
│   │   │       └── application.yml  # 配置文件
│   │   └── test/        # 测试代码
│   └── pom.xml          # Maven配置文件
├── frontend/           # 前端项目
│   ├── src/            # 源代码
│   │   ├── components/  # 组件
│   │   ├── views/       # 页面
│   │   ├── router/      # 路由
│   │   ├── api/         # API请求
│   │   ├── assets/      # 静态资源
│   │   ├── App.vue      # 根组件
│   │   └── main.js      # 入口文件
│   ├── public/          # 公共文件
│   ├── index.html       # HTML模板
│   ├── package.json     # 依赖配置
│   └── vite.config.js   # Vite配置
└── README.md            # 项目说明
```

## 安装和运行

### 1. 数据库初始化

1. 安装MySQL数据库
2. 运行 `backend/src/main/resources/init.sql` 脚本创建数据库和表结构，并插入测试数据

### 2. 后端配置

1. 打开 `backend/src/main/resources/application.yml` 文件
2. 修改数据库连接信息（用户名、密码）
3. 修改Spring AI Alibaba的配置（access-key、secret-key）

### 3. 后端运行

1. 进入backend目录
2. 执行 `mvn clean install` 构建项目
3. 执行 `mvn spring-boot:run` 启动后端服务

### 4. 前端配置

1. 进入frontend目录
2. 执行 `npm install` 安装依赖

### 5. 前端运行

1. 进入frontend目录
2. 执行 `npm run dev` 启动前端开发服务器
3. 打开浏览器访问 `http://localhost:3000`

## 功能说明

### 1. 用户功能
- 注册/登录
- 个人设置
- 权限管理

### 2. 数据查询功能
- 自然语言查询：通过自然语言提问，系统自动转换为SQL查询
- SQL查询：支持直接输入SQL语句查询
- 查询历史：保存用户的查询历史
- 查询模板：提供常用查询模板

### 3. 数据可视化功能
- 图表展示：支持柱状图、折线图、饼图等多种图表类型
- 数据导出：支持导出查询结果为CSV格式
- 仪表盘：提供数据概览仪表盘

### 4. 数据管理功能
- 商品管理：查看和管理商品信息
- 订单管理：查看和管理订单信息
- 分类管理：查看和管理商品分类

## 测试账号

- 管理员账号：admin / 123456
- 普通用户账号：user1 / 123456
- 普通用户账号：user2 / 123456

## 注意事项

1. 确保MySQL数据库已启动
2. 确保Spring AI Alibaba的配置正确
3. 前端开发服务器默认运行在3000端口，后端服务默认运行在8080端口
4. 首次运行时，会自动创建数据库表结构并插入测试数据
