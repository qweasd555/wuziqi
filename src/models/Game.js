const mongoose = require('mongoose');

const gameSchema = new mongoose.Schema({
  // 对局基本信息
  gameId: {
    type: String,
    required: true,
    unique: true,
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
    avatar: String,
    isBlack: {
      type: Boolean,
      default: true
    },
    skills: [{
      skillId: String,
      skillName: String,
      used: {
        type: Boolean,
        default: false
      }
    }]
  },
  player2: {
    userId: String,
    openid: String,
    nickname: String,
    avatar: String,
    isBlack: {
      type: Boolean,
      default: false
    },
    skills: [{
      skillId: String,
      skillName: String,
      used: {
        type: Boolean,
        default: false
      }
    }]
  },
  
  // 棋盘状态 (15x15)
  board: {
    type: [[Number]], // 0=空, 1=黑, 2=白
    default: Array(15).fill(null).map(() => Array(15).fill(0))
  },
  
  // 对局状态
  status: {
    type: String,
    enum: ['waiting', 'playing', 'paused', 'finished'],
    default: 'waiting'
  },
  currentPlayer: {
    type: Number, // 1=黑, 2=白
    default: 1
  },
  winner: {
    type: Number, // 1=黑, 2=白, 0=平局
    default: null
  },
  
  // 落子记录
  moves: [{
    player: Number,
    row: Number,
    col: Number,
    timestamp: Date,
    skillUsed: {
      type: String,
      default: null
    }
  }],
  
  // 技能效果状态
  skillEffects: [{
    skillId: String,
    skillName: String,
    player: Number,
    effect: Object,
    expiresAt: Date
  }],
  
  // 特殊状态（技能效果）
  sealedCells: [{ // 封印技能
    row: Number,
    col: Number
  }],
  transparentCells: [{ // 透视眼技能
    row: Number,
    col: Number,
    player: Number
  }],
  forcedArea: { // 强制落子区域
    centerRow: Number,
    centerCol: Number,
    player: Number
  },
  
  // 时间相关
  startedAt: Date,
  finishedAt: Date,
  duration: Number, // 秒
  
  // 创建时间
  createdAt: {
    type: Date,
    default: Date.now
  }
});

// 获取当前棋盘状态
gameSchema.methods.getBoardState = function() {
  return this.board;
};

// 检查位置是否合法
gameSchema.methods.isValidMove = function(row, col) {
  // 边界检查
  if (row < 0 || row >= 15 || col < 0 || col >= 15) {
    return false;
  }
  
  // 检查是否已有棋子
  if (this.board[row][col] !== 0) {
    return false;
  }
  
  // 检查是否被封印
  const isSealed = this.sealedCells.some(cell => 
    cell.row === row && cell.col === col
  );
  if (isSealed) {
    return false;
  }
  
  return true;
};

// 落子
gameSchema.methods.makeMove = function(row, col, player, skillId = null) {
  if (!this.isValidMove(row, col)) {
    return { success: false, message: '无效的落子位置' };
  }
  
  this.board[row][col] = player;
  this.moves.push({
    player,
    row,
    col,
    timestamp: new Date(),
    skillUsed: skillId
  });
  
  // 切换玩家
  this.currentPlayer = player === 1 ? 2 : 1;
  
  return { success: true, board: this.board };
};

module.exports = mongoose.model('Game', gameSchema);

