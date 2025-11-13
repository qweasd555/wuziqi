const mongoose = require('mongoose');

const recordSchema = new mongoose.Schema({
  // 对局信息
  gameId: {
    type: String,
    required: true,
    index: true
  },
  mode: {
    type: String,
    enum: ['normal', 'skill'],
    required: true
  },
  type: {
    type: String,
    enum: ['local', 'ai', 'online'],
    required: true
  },
  
  // 玩家信息
  player1: {
    userId: String,
    openid: String,
    nickname: String,
    isBlack: Boolean,
    isWinner: Boolean,
    skillsUsed: [String]
  },
  player2: {
    userId: String,
    openid: String,
    nickname: String,
    isBlack: Boolean,
    isWinner: Boolean,
    skillsUsed: [String]
  },
  
  // 对局结果
  winner: {
    type: Number, // 1=黑, 2=白, 0=平局
    default: null
  },
  result: {
    type: String,
    enum: ['win', 'loss', 'draw', 'surrender'],
    required: true
  },
  
  // 对局数据
  totalMoves: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number, // 秒
    default: 0
  },
  
  // 落子记录（用于回放）
  moves: [{
    player: Number,
    row: Number,
    col: Number,
    timestamp: Date,
    skillUsed: String
  }],
  
  // 时间戳
  createdAt: {
    type: Date,
    default: Date.now
  },
  finishedAt: {
    type: Date,
    default: Date.now
  }
});

// 索引
recordSchema.index({ 'player1.userId': 1, createdAt: -1 });
recordSchema.index({ 'player2.userId': 1, createdAt: -1 });
recordSchema.index({ createdAt: -1 });

module.exports = mongoose.model('Record', recordSchema);

