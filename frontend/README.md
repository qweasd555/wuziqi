# 五子棋游戏前端

这是一个基于React的五子棋游戏前端应用，支持技能系统和实时对战。

## 功能特性

### 1. 用户系统
- 模拟微信登录（可扩展真实微信登录）
- 用户个人信息管理
- 积分和段位系统
- 游戏统计和历史记录

### 2. 游戏功能
- **游戏模式**：
  - 常规模式：传统五子棋玩法
  - 技能模式：增加技能系统的创新玩法
- **对战方式**：
  - 联机对战：与其他玩家实时对战
  - 人机对战：与AI对手对战
- **实时通信**：基于WebSocket的实时游戏同步

### 3. 技能系统
- 多种技能类型：攻击、防御、实用
- 技能冷却机制
- 积分消耗系统
- 视觉效果和交互反馈

### 4. 用户界面
- **登录界面**：简洁的用户登录页面
- **游戏大厅**：游戏创建、加入、管理
- **游戏棋盘**：15×15标准五子棋棋盘
- **技能面板**：技能选择和使用界面
- **个人中心**：用户统计、游戏历史
- **排行榜**：多维度排名展示

## 技术栈

- **框架**：React 18.2.0
- **路由**：React Router DOM 6.3.0
- **UI库**：Ant Design 5.6.0
- **样式**：Styled Components 6.0.0
- **HTTP客户端**：Axios 1.4.0
- **WebSocket**：SockJS + STOMP.js
- **构建工具**：Create React App

## 项目结构

```
frontend/
├── public/
│   └── index.html          # HTML模板
├── src/
│   ├── components/         # React组件
│   │   ├── GameBoard.js    # 游戏棋盘组件
│   │   └── SkillPanel.js   # 技能面板组件
│   ├── contexts/           # React Context
│   │   ├── AuthContext.js  # 用户认证上下文
│   │   └── WebSocketContext.js # WebSocket上下文
│   ├── pages/              # 页面组件
│   │   ├── Login.js        # 登录页面
│   │   ├── GameHall.js     # 游戏大厅
│   │   ├── GameBoard.js    # 游戏页面
│   │   ├── Profile.js      # 个人中心
│   │   └── Ranking.js      # 排行榜
│   ├── services/           # API服务
│   │   └── api.js         # API接口封装
│   ├── utils/              # 工具函数
│   │   └── constants.js    # 常量定义
│   ├── App.js             # 应用主组件
│   ├── App.css            # 全局样式
│   └── index.js           # 应用入口
├── package.json           # 项目依赖
└── README.md             # 项目说明
```

## 快速开始

### 1. 环境要求
- Node.js >= 14.0.0
- npm >= 6.0.0

### 2. 安装依赖
```bash
cd frontend
npm install
```

### 3. 启动开发服务器
```bash
npm start
```

应用将在 `http://localhost:3000` 启动。

### 4. 构建生产版本
```bash
npm run build
```

## 使用说明

### 1. 登录系统
- 访问 `http://localhost:3000`
- 输入昵称进行登录（支持快速登录按钮）
- 系统自动生成用户ID并保存到本地

### 2. 游戏大厅
- 查看个人统计数据
- 创建新游戏（选择模式和类型）
- 加入其他玩家的游戏
- 查看活跃游戏列表

### 3. 游戏对战
- 点击棋盘格子下棋
- 技能模式下可使用特殊技能
- 实时查看对手操作
- 支持投降功能

### 4. 个人中心
- 查看详细游戏统计
- 浏览游戏历史记录
- 查看段位进度

### 5. 排行榜
- 多维度排名查看
- 搜索特定玩家
- 前三名特殊展示

## API配置

前端默认连接到 `http://localhost:8080/api`，确保后端服务在此地址运行。

如果需要修改后端地址，可以在环境变量中设置：
```bash
REACT_APP_API_URL=http://your-backend-url:port/api
```

## WebSocket配置

WebSocket连接地址为 `http://localhost:8080/ws`，支持：
- 实时游戏状态同步
- 玩家移动广播
- 技能使用通知
- 游戏结果推送

## 样式主题

项目采用现代化的渐变色设计：
- 主色调：蓝紫渐变 (#667eea → #764ba2)
- 技能卡片：红色系渐变
- 统计卡片：多彩渐变
- 响应式设计，支持移动端适配

## 开发说明

### 1. 组件开发
- 使用函数式组件和Hooks
- 遵循单一职责原则
- 组件可复用和可测试

### 2. 状态管理
- 使用React Context进行全局状态管理
- 本地状态使用useState
- 异步操作使用useEffect

### 3. 样式开发
- 使用Styled Components进行组件样式封装
- 响应式设计优先
- 支持主题定制

### 4. 错误处理
- API请求统一错误处理
- 用户友好的错误提示
- 网络异常自动重连

## 扩展功能

### 1. 真实微信登录
替换模拟登录为真实微信小程序登录：
```javascript
// 在Login.js中
const handleWechatLogin = async () => {
  try {
    const result = await wechatAPI.login(code);
    // 处理登录结果
  } catch (error) {
    // 处理错误
  }
};
```

### 2. 更多游戏模式
- 围棋模式
- 象棋模式
- 自定义规则

### 3. 社交功能
- 好友系统
- 聊天功能
- 游戏观战

### 4. 数据持久化
- 游戏录像
- 战绩分析
- 数据导出

## 常见问题

### 1. WebSocket连接失败
确保后端WebSocket服务正常运行，检查防火墙设置。

### 2. API请求失败
检查后端服务是否启动，确认API地址配置正确。

### 3. 样式显示异常
清除浏览器缓存，确保CSS文件正确加载。

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证

MIT License