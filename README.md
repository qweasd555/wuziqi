# SkillFive 后端服务

趣味技能五子棋游戏的后端服务，提供游戏逻辑、数据存储和实时对战功能。

## 项目结构

```
skillfive-backend/
├── src/
│   ├── app.js                 # 应用入口
│   ├── config/               # 配置文件
│   │   └── database.js       # 数据库配置
│   ├── models/               # 数据模型
│   │   ├── User.js          # 用户模型
│   │   ├── Game.js          # 对局模型
│   │   └── Record.js        # 战绩模型
│   ├── controllers/          # 控制器
│   │   ├── userController.js
│   │   ├── gameController.js
│   │   └── recordController.js
│   ├── services/             # 业务逻辑
│   │   ├── gameLogic.js     # 游戏核心逻辑
│   │   ├── skillSystem.js   # 技能系统
│   │   └── aiService.js     # AI 对战服务
│   ├── routes/              # 路由
│   │   ├── userRoutes.js
│   │   ├── gameRoutes.js
│   │   └── recordRoutes.js
│   ├── middleware/          # 中间件
│   │   ├── auth.js         # 认证中间件
│   │   └── errorHandler.js # 错误处理
│   └── utils/              # 工具函数
│       └── validators.js
├── .env.example            # 环境变量示例
├── .gitignore
└── package.json
```

## 功能模块

### 1. 用户系统
- 微信授权登录
- 用户信息管理
- 积分和段位系统

### 2. 游戏逻辑
- 棋盘状态管理（15x15）
- 落子合法性验证
- 胜负判定算法（横、竖、斜）
- 技能系统实现

### 3. 对战系统
- 本地双人对战
- AI 对战（可调节难度）
- 实时在线对战（WebSocket）

### 4. 数据存储
- 用户数据
- 对局记录
- 战绩统计

## 快速开始

### 安装依赖

```bash
npm install
```

### 配置环境变量

复制 `.env.example` 为 `.env` 并配置：

```env
PORT=3000
MONGODB_URI=mongodb://localhost:27017/skillfive
JWT_SECRET=your-secret-key
WECHAT_APPID=your-wechat-appid
WECHAT_SECRET=your-wechat-secret
```

### 启动服务

开发模式：
```bash
npm run dev
```

生产模式：
```bash
npm start
```

## API 文档

### 用户相关

- `POST /api/user/login` - 微信登录
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/info` - 更新用户信息
- `GET /api/user/stats` - 获取用户统计

### 游戏相关

- `POST /api/game/create` - 创建对局
- `GET /api/game/:id` - 获取对局信息
- `POST /api/game/:id/move` - 落子
- `POST /api/game/:id/skill` - 使用技能
- `POST /api/game/:id/surrender` - 认输

### 战绩相关

- `GET /api/record/list` - 获取对战记录
- `GET /api/record/:id` - 获取单条记录详情
- `GET /api/record/rank` - 获取排行榜

## WebSocket 事件

### 客户端发送

- `join-game` - 加入对局
- `make-move` - 落子
- `use-skill` - 使用技能
- `surrender` - 认输

### 服务端推送

- `game-state` - 对局状态更新
- `move-result` - 落子结果
- `skill-effect` - 技能效果
- `game-over` - 游戏结束

## 技能系统

支持10种技能，详见 `src/services/skillSystem.js`

## 技术栈

- Node.js + Express
- Socket.io (WebSocket)
- MongoDB (Mongoose)
- JWT (认证)

## 开发计划

- [x] 项目结构搭建
- [x] 数据库模型设计
- [ ] 游戏核心逻辑
- [ ] 技能系统实现
- [ ] AI 对战算法
- [ ] WebSocket 实时对战
- [ ] API 接口完善
- [ ] 测试与优化

