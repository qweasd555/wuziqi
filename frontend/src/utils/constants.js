// 游戏模式
export const GameMode = {
  REGULAR: 'REGULAR',
  SKILL: 'SKILL'
};

// 游戏类型
export const GameType = {
  GOMOKU: 'GOMOKU',
  WEIQI: 'WEIQI', 
  CHESS: 'CHESS',
  VS_AI: 'VS_AI',
  ONLINE_PVP: 'ONLINE_PVP',
  LOCAL_PVP: 'LOCAL_PVP'
};

// 游戏状态
export const GameStatus = {
  PENDING: 'PENDING',
  IN_PROGRESS: 'IN_PROGRESS',
  FINISHED: 'FINISHED'
};

// 技能类型
export const SkillType = {
  ATTACK: 'ATTACK',
  DEFENSE: 'DEFENSE',
  UTILITY: 'UTILITY'
};

// 棋盘大小
export const BOARD_SIZE = 15;

// 棋子类型
export const PieceType = {
  EMPTY: '-',
  PLAYER1: 'X',
  PLAYER2: 'O'
};

// 游戏结果
export const GameResult = {
  PLAYER1_WIN: 'player1',
  PLAYER2_WIN: 'player2',
  DRAW: 'draw'
};

// WebSocket消息类型
export const WSMessageTypes = {
  JOIN_GAME: 'join',
  LEAVE_GAME: 'leave',
  MAKE_MOVE: 'move',
  USE_SKILL: 'useSkill',
  SURRENDER: 'surrender',
  GAME_UPDATE: 'gameUpdate',
  CHAT_MESSAGE: 'chat'
};

// API错误消息
export const ErrorMessages = {
  NETWORK_ERROR: '网络连接失败，请检查网络设置',
  UNAUTHORIZED: '登录已过期，请重新登录',
  GAME_NOT_FOUND: '游戏不存在或已结束',
  INVALID_MOVE: '无效的移动操作',
  SKILL_COOLDOWN: '技能冷却中，请稍后再试',
  NOT_YOUR_TURN: '当前不是您的回合',
  GAME_FULL: '游戏房间已满'
};