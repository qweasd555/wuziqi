/**
 * AI 对战服务
 * 实现不同难度的 AI 算法
 */

const gameLogic = require('./gameLogic');

class AIService {
  constructor() {
    this.difficulties = {
      easy: {
        name: '简单',
        randomChance: 0.7, // 70% 随机落子
        depth: 1
      },
      medium: {
        name: '中等',
        randomChance: 0.3,
        depth: 2
      },
      hard: {
        name: '困难',
        randomChance: 0.1,
        depth: 3
      }
    };
  }

  /**
   * AI 落子决策
   */
  makeMove(board, aiPlayer, difficulty = 'medium', sealedCells = []) {
    const config = this.difficulties[difficulty] || this.difficulties.medium;
    const humanPlayer = aiPlayer === 1 ? 2 : 1;

    // 获取威胁分析
    const myThreats = gameLogic.getThreats(board, aiPlayer);
    const oppThreats = gameLogic.getThreats(board, humanPlayer);

    // 优先级1: 自己可以立即获胜
    if (myThreats.win.length > 0) {
      return myThreats.win[0];
    }

    // 优先级2: 阻止对手获胜
    if (oppThreats.win.length > 0) {
      return oppThreats.win[0];
    }

    // 随机决策（简单模式）
    if (Math.random() < config.randomChance) {
      return this.randomMove(board, sealedCells);
    }

    // 优先级3: 形成自己的四连
    if (myThreats.four.length > 0) {
      return myThreats.four[0];
    }

    // 优先级4: 阻止对手形成四连
    if (oppThreats.four.length > 0) {
      return oppThreats.four[0];
    }

    // 优先级5: 使用评估函数选择最佳位置
    return this.bestMove(board, aiPlayer, humanPlayer, sealedCells, config.depth);
  }

  /**
   * 随机落子
   */
  randomMove(board, sealedCells = []) {
    const availableMoves = [];
    
    for (let i = 0; i < 15; i++) {
      for (let j = 0; j < 15; j++) {
        const isValid = gameLogic.isValidMove(board, i, j, sealedCells);
        if (isValid.valid) {
          availableMoves.push({ row: i, col: j });
        }
      }
    }

    if (availableMoves.length === 0) {
      return null;
    }

    return availableMoves[Math.floor(Math.random() * availableMoves.length)];
  }

  /**
   * 使用评估函数选择最佳位置
   */
  bestMove(board, aiPlayer, humanPlayer, sealedCells, depth) {
    let bestScore = -Infinity;
    let bestMove = null;

    // 只评估中心区域和已有棋子周围的位置（优化性能）
    const candidateMoves = this.getCandidateMoves(board, sealedCells);

    for (const move of candidateMoves) {
      // 评估这个位置
      const score = this.evaluateMove(board, move.row, move.col, aiPlayer, humanPlayer, depth);
      
      if (score > bestScore) {
        bestScore = score;
        bestMove = move;
      }
    }

    return bestMove || this.randomMove(board, sealedCells);
  }

  /**
   * 获取候选落子位置（优化：只考虑重要位置）
   */
  getCandidateMoves(board, sealedCells) {
    const moves = [];
    const checked = new Set();

    // 1. 检查已有棋子周围的位置
    for (let i = 0; i < 15; i++) {
      for (let j = 0; j < 15; j++) {
        if (board[i][j] !== 0) {
          // 检查周围8个方向
          for (let di = -2; di <= 2; di++) {
            for (let dj = -2; dj <= 2; dj++) {
              if (di === 0 && dj === 0) continue;
              
              const ni = i + di;
              const nj = j + dj;
              
              if (ni >= 0 && ni < 15 && nj >= 0 && nj < 15) {
                const key = `${ni},${nj}`;
                if (!checked.has(key)) {
                  checked.add(key);
                  const isValid = gameLogic.isValidMove(board, ni, nj, sealedCells);
                  if (isValid.valid) {
                    moves.push({ row: ni, col: nj });
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. 如果棋盘为空，返回中心位置
    if (moves.length === 0) {
      return [{ row: 7, col: 7 }];
    }

    return moves;
  }

  /**
   * 评估落子位置
   */
  evaluateMove(board, row, col, aiPlayer, humanPlayer, depth) {
    // 基础评估
    let score = gameLogic.evaluatePosition(board, row, col, aiPlayer);
    score -= gameLogic.evaluatePosition(board, row, col, humanPlayer) * 0.8;

    // 位置价值（中心位置更有价值）
    const centerDistance = Math.abs(row - 7) + Math.abs(col - 7);
    score += (14 - centerDistance) * 2;

    // 如果深度允许，进行简单的前瞻
    if (depth > 1) {
      const testBoard = board.map(r => [...r]);
      testBoard[row][col] = aiPlayer;
      
      // 检查对手的最佳应对
      const oppThreats = gameLogic.getThreats(testBoard, humanPlayer);
      if (oppThreats.win.length > 0) {
        score -= 1000; // 这个位置会让对手获胜
      }
    }

    return score;
  }
}

module.exports = new AIService();

