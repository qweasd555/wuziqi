/**
 * 游戏核心逻辑
 * 包括：落子验证、胜负判定、棋盘状态管理
 */

class GameLogic {
  constructor(boardSize = 15) {
    this.boardSize = boardSize;
  }

  /**
   * 检查落子是否合法
   */
  isValidMove(board, row, col, sealedCells = []) {
    // 边界检查
    if (row < 0 || row >= this.boardSize || col < 0 || col >= this.boardSize) {
      return { valid: false, reason: '超出棋盘范围' };
    }

    // 检查是否已有棋子
    if (board[row][col] !== 0) {
      return { valid: false, reason: '该位置已有棋子' };
    }

    // 检查是否被封印
    const isSealed = sealedCells.some(cell => cell.row === row && cell.col === col);
    if (isSealed) {
      return { valid: false, reason: '该位置被封印' };
    }

    return { valid: true };
  }

  /**
   * 落子
   */
  makeMove(board, row, col, player) {
    if (!this.isValidMove(board, row, col).valid) {
      return { success: false, board };
    }

    const newBoard = board.map(row => [...row]);
    newBoard[row][col] = player;
    return { success: true, board: newBoard };
  }

  /**
   * 检查是否五连（胜负判定）
   * 返回: { win: boolean, player: number, line: array }
   */
  checkWin(board, row, col, player) {
    const directions = [
      [[0, 1], [0, -1]],   // 横向
      [[1, 0], [-1, 0]],   // 纵向
      [[1, 1], [-1, -1]],  // 主对角线
      [[1, -1], [-1, 1]]   // 副对角线
    ];

    for (const [forward, backward] of directions) {
      let count = 1; // 当前棋子
      const line = [{ row, col }];

      // 向前检查
      let r = row + forward[0];
      let c = col + forward[1];
      while (
        r >= 0 && r < this.boardSize &&
        c >= 0 && c < this.boardSize &&
        board[r][c] === player
      ) {
        count++;
        line.push({ row: r, col: c });
        r += forward[0];
        c += forward[1];
      }

      // 向后检查
      r = row + backward[0];
      c = col + backward[1];
      while (
        r >= 0 && r < this.boardSize &&
        c >= 0 && c < this.boardSize &&
        board[r][c] === player
      ) {
        count++;
        line.unshift({ row: r, col: c });
        r += backward[0];
        c += backward[1];
      }

      if (count >= 5) {
        return {
          win: true,
          player,
          line: line.slice(0, 5) // 只返回五连的棋子
        };
      }
    }

    return { win: false };
  }

  /**
   * 检查是否平局（棋盘已满）
   */
  checkDraw(board) {
    for (let i = 0; i < this.boardSize; i++) {
      for (let j = 0; j < this.boardSize; j++) {
        if (board[i][j] === 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 获取棋盘上所有威胁点（四连、活三等）
   * 用于 AI 决策
   */
  getThreats(board, player) {
    const threats = {
      win: [],      // 可以立即获胜的位置
      block: [],    // 需要阻止对手获胜的位置
      four: [],     // 形成四连的位置
      three: []     // 形成活三的位置
    };

    for (let i = 0; i < this.boardSize; i++) {
      for (let j = 0; j < this.boardSize; j++) {
        if (board[i][j] !== 0) continue;

        // 模拟落子
        const testBoard = board.map(row => [...row]);
        testBoard[i][j] = player;

        // 检查是否获胜
        if (this.checkWin(testBoard, i, j, player).win) {
          threats.win.push({ row: i, col: j });
        }

        // 检查对手是否获胜（需要阻止）
        testBoard[i][j] = player === 1 ? 2 : 1;
        if (this.checkWin(testBoard, i, j, player === 1 ? 2 : 1).win) {
          threats.block.push({ row: i, col: j });
        }
      }
    }

    return threats;
  }

  /**
   * 评估位置价值（用于 AI）
   */
  evaluatePosition(board, row, col, player) {
    let score = 0;
    const directions = [
      [[0, 1], [0, -1]],   // 横向
      [[1, 0], [-1, 0]],   // 纵向
      [[1, 1], [-1, -1]],  // 主对角线
      [[1, -1], [-1, 1]]   // 副对角线
    ];

    for (const [forward, backward] of directions) {
      let myCount = 0;
      let myBlocked = 0;
      let oppCount = 0;
      let oppBlocked = 0;

      // 向前检查
      let r = row + forward[0];
      let c = col + forward[1];
      while (
        r >= 0 && r < this.boardSize &&
        c >= 0 && c < this.boardSize &&
        board[r][c] !== (player === 1 ? 2 : 1)
      ) {
        if (board[r][c] === player) myCount++;
        else if (board[r][c] === 0) break;
        r += forward[0];
        c += forward[1];
      }
      if (r < 0 || r >= this.boardSize || c < 0 || c >= this.boardSize || 
          board[r] && board[r][c] === (player === 1 ? 2 : 1)) {
        myBlocked = 1;
      }

      // 向后检查
      r = row + backward[0];
      c = col + backward[1];
      while (
        r >= 0 && r < this.boardSize &&
        c >= 0 && c < this.boardSize &&
        board[r][c] !== (player === 1 ? 2 : 1)
      ) {
        if (board[r][c] === player) myCount++;
        else if (board[r][c] === 0) break;
        r += backward[0];
        c += backward[1];
      }
      if (r < 0 || r >= this.boardSize || c < 0 || c >= this.boardSize ||
          board[r] && board[r][c] === (player === 1 ? 2 : 1)) {
        myBlocked = 1;
      }

      // 评分
      const totalMy = myCount + 1; // +1 是当前位置
      if (totalMy >= 5) score += 100000;
      else if (totalMy === 4 && myBlocked === 0) score += 10000;
      else if (totalMy === 4 && myBlocked === 1) score += 1000;
      else if (totalMy === 3 && myBlocked === 0) score += 100;
      else if (totalMy === 3 && myBlocked === 1) score += 10;
      else if (totalMy === 2 && myBlocked === 0) score += 5;
    }

    return score;
  }
}

module.exports = new GameLogic(15);

