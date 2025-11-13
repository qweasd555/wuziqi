const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  // 微信相关
  openid: {
    type: String,
    required: true,
    unique: true,
    index: true
  },
  unionid: {
    type: String,
    index: true
  },
  
  // 用户信息
  nickname: {
    type: String,
    default: '玩家'
  },
  avatar: {
    type: String,
    default: ''
  },
  
  // 游戏数据
  totalGames: {
    type: Number,
    default: 0
  },
  wins: {
    type: Number,
    default: 0
  },
  losses: {
    type: Number,
    default: 0
  },
  draws: {
    type: Number,
    default: 0
  },
  winRate: {
    type: Number,
    default: 0
  },
  
  // 积分和段位
  points: {
    type: Number,
    default: 0
  },
  rank: {
    type: String,
    enum: ['青铜', '白银', '黄金', '白金', '钻石', '大师', '王者'],
    default: '青铜'
  },
  maxWinStreak: {
    type: Number,
    default: 0
  },
  currentWinStreak: {
    type: Number,
    default: 0
  },
  
  // 技能统计
  skillsUsed: {
    type: Map,
    of: Number,
    default: {}
  },
  
  // 时间戳
  createdAt: {
    type: Date,
    default: Date.now
  },
  updatedAt: {
    type: Date,
    default: Date.now
  },
  lastLoginAt: {
    type: Date,
    default: Date.now
  }
});

// 更新时自动更新 updatedAt
userSchema.pre('save', function(next) {
  this.updatedAt = Date.now();
  next();
});

// 计算胜率
userSchema.methods.calculateWinRate = function() {
  if (this.totalGames === 0) return 0;
  this.winRate = (this.wins / this.totalGames * 100).toFixed(2);
  return this.winRate;
};

// 更新段位
userSchema.methods.updateRank = function() {
  const ranks = ['青铜', '白银', '黄金', '白金', '钻石', '大师', '王者'];
  const thresholds = [0, 100, 300, 600, 1000, 1500, 2500];
  
  for (let i = thresholds.length - 1; i >= 0; i--) {
    if (this.points >= thresholds[i]) {
      this.rank = ranks[i];
      break;
    }
  }
};

// 更新连胜
userSchema.methods.updateWinStreak = function(isWin) {
  if (isWin) {
    this.currentWinStreak += 1;
    if (this.currentWinStreak > this.maxWinStreak) {
      this.maxWinStreak = this.currentWinStreak;
    }
  } else {
    this.currentWinStreak = 0;
  }
};

module.exports = mongoose.model('User', userSchema);

