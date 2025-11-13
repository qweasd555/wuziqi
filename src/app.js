const express = require('express');
const cors = require('cors');
const http = require('http');
const socketIo = require('socket.io');
require('dotenv').config();

const connectDB = require('./config/database');
const errorHandler = require('./middleware/errorHandler');

// 路由
const userRoutes = require('./routes/userRoutes');
const gameRoutes = require('./routes/gameRoutes');
const recordRoutes = require('./routes/recordRoutes');

// Socket 处理
const socketHandler = require('./services/socketHandler');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: process.env.CORS_ORIGIN || '*',
    methods: ['GET', 'POST']
  }
});

// 中间件
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 健康检查
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API 路由
app.use('/api/user', userRoutes);
app.use('/api/game', gameRoutes);
app.use('/api/record', recordRoutes);

// Socket.IO 连接处理
io.on('connection', (socket) => {
  socketHandler(io, socket);
});

// 错误处理
app.use(errorHandler);

// 连接数据库
connectDB();

// 启动服务器
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`🚀 SkillFive 后端服务启动成功！`);
  console.log(`📡 服务器运行在: http://localhost:${PORT}`);
  console.log(`🔌 WebSocket 服务已启动`);
});

module.exports = { app, server, io };

