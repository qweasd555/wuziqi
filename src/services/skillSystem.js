/**
 * 技能系统
 * 实现10种技能的效果逻辑
 */

const { v4: uuidv4 } = require('uuid');

class SkillSystem {
  constructor() {
    this.skills = {
      // 进攻型
      'chain-move': {
        id: 'chain-move',
        name: '连环落子',
        type: 'offensive',
        description: '本回合可以连续落两颗己方棋子（不能相邻）',
        icon: '⚡'
      },
      'x-ray': {
        id: 'x-ray',
        name: '透视眼',
        type: 'offensive',
        description: '放置一颗透明棋子，对手看不见，下回合显形',
        icon: '👁️'
      },
      'force-move': {
        id: 'force-move',
        name: '强制落子',
        type: 'offensive',
        description: '指定对手下一步必须下在某个3×3区域内',
        icon: '🎯'
      },
      'swap': {
        id: 'swap',
        name: '交换棋子',
        type: 'offensive',
        description: '黑白棋子位置互换',
        icon: '🔄'
      },
      
      // 防守型
      'seal': {
        id: 'seal',
        name: '封印',
        type: 'defensive',
        description: '封印棋盘上3x3位置，对手不能下在此位置',
        icon: '🔒'
      },
      'remove': {
        id: 'remove',
        name: '悔棋',
        type: 'defensive',
        description: '移除对手任意一个棋子',
        icon: '↩️'
      },
      'shield': {
        id: 'shield',
        name: '护盾',
        type: 'defensive',
        description: '免疫对方使用的技能',
        icon: '🛡️'
      },
      
      // 辅助型
      'prophecy': {
        id: 'prophecy',
        name: '预言术',
        type: 'support',
        description: '查看对手当前拥有的技能及使用情况',
        icon: '🔮'
      },
      'clear': {
        id: 'clear',
        name: '清空术',
        type: 'support',
        description: '清空所有棋子',
        icon: '🧹'
      },
      'blind': {
        id: 'blind',
        name: '蒙蔽术',
        type: 'support',
        description: '让对方看见的棋子都变成一个颜色',
        icon: '👻'
      }
    };
  }

  /**
   * 随机选择3个技能
   */
  randomSelectSkills(count = 3) {
    const skillIds = Object.keys(this.skills);
    const selected = [];
    const used = new Set();

    while (selected.length < count && selected.length < skillIds.length) {
      const randomIndex = Math.floor(Math.random() * skillIds.length);
      const skillId = skillIds[randomIndex];
      
      if (!used.has(skillId)) {
        used.add(skillId);
        selected.push({
          skillId,
          skillName: this.skills[skillId].name,
          used: false
        });
      }
    }

    return selected;
  }

  /**
   * 获取技能信息
   */
  getSkillInfo(skillId) {
    return this.skills[skillId] || null;
  }

  /**
   * 使用技能：连环落子
   */
  useChainMove(game, player, moves) {
    if (moves.length !== 2) {
      return { success: false, message: '连环落子需要连续下两颗棋子' };
    }

    const [move1, move2] = moves;
    
    // 检查两颗棋子是否相邻
    const distance = Math.abs(move1.row - move2.row) + Math.abs(move1.col - move2.col);
    if (distance <= 1) {
      return { success: false, message: '两颗棋子不能相邻' };
    }

    // 执行两次落子
    const result1 = game.makeMove(move1.row, move1.col, player);
    if (!result1.success) {
      return result1;
    }

    const result2 = game.makeMove(move2.row, move2.col, player);
    if (!result2.success) {
      // 回滚第一次落子
      game.board[move1.row][move1.col] = 0;
      return result2;
    }

    return {
      success: true,
      moves: [
        { row: move1.row, col: move1.col, player },
        { row: move2.row, col: move2.col, player }
      ]
    };
  }

  /**
   * 使用技能：透视眼
   */
  useXRay(game, player, row, col) {
    // 检查位置是否合法
    if (!game.isValidMove(row, col)) {
      return { success: false, message: '无效的位置' };
    }

    // 添加透明棋子
    game.transparentCells.push({
      row,
      col,
      player,
      id: uuidv4()
    });

    return {
      success: true,
      transparentCell: { row, col, player }
    };
  }

  /**
   * 使用技能：强制落子
   */
  useForceMove(game, player, centerRow, centerCol) {
    // 设置强制落子区域（3x3）
    game.forcedArea = {
      centerRow,
      centerCol,
      player: player === 1 ? 2 : 1, // 对手
      expiresAt: new Date(Date.now() + 60000) // 1分钟后失效
    };

    return {
      success: true,
      area: {
        minRow: Math.max(0, centerRow - 1),
        maxRow: Math.min(14, centerRow + 1),
        minCol: Math.max(0, centerCol - 1),
        maxCol: Math.min(14, centerCol + 1)
      }
    };
  }

  /**
   * 使用技能：交换棋子
   */
  useSwap(game) {
    // 交换所有棋子颜色
    for (let i = 0; i < 15; i++) {
      for (let j = 0; j < 15; j++) {
        if (game.board[i][j] === 1) {
          game.board[i][j] = 2;
        } else if (game.board[i][j] === 2) {
          game.board[i][j] = 1;
        }
      }
    }

    // 交换玩家
    const temp = game.currentPlayer;
    game.currentPlayer = temp === 1 ? 2 : 1;

    return { success: true };
  }

  /**
   * 使用技能：封印
   */
  useSeal(game, player, centerRow, centerCol) {
    // 封印3x3区域
    const sealed = [];
    for (let i = Math.max(0, centerRow - 1); i <= Math.min(14, centerRow + 1); i++) {
      for (let j = Math.max(0, centerCol - 1); j <= Math.min(14, centerCol + 1); j++) {
        // 只封印空位置
        if (game.board[i][j] === 0) {
          game.sealedCells.push({ row: i, col: j });
          sealed.push({ row: i, col: j });
        }
      }
    }

    return {
      success: true,
      sealedCells: sealed
    };
  }

  /**
   * 使用技能：悔棋（移除对手棋子）
   */
  useRemove(game, player, row, col) {
    // 检查是否是对手的棋子
    const opponent = player === 1 ? 2 : 1;
    if (game.board[row][col] !== opponent) {
      return { success: false, message: '只能移除对手的棋子' };
    }

    // 移除棋子
    game.board[row][col] = 0;

    // 从落子记录中移除
    const moveIndex = game.moves.findLastIndex(
      m => m.row === row && m.col === col
    );
    if (moveIndex !== -1) {
      game.moves.splice(moveIndex, 1);
    }

    return {
      success: true,
      removed: { row, col }
    };
  }

  /**
   * 使用技能：护盾
   */
  useShield(game, player) {
    // 添加护盾效果
    game.skillEffects.push({
      skillId: 'shield',
      skillName: '护盾',
      player,
      effect: { immune: true },
      expiresAt: new Date(Date.now() + 300000) // 5分钟
    });

    return { success: true };
  }

  /**
   * 使用技能：预言术
   */
  useProphecy(game, player) {
    const opponent = player === 1 ? 2 : 1;
    const opponentPlayer = opponent === 1 ? game.player1 : game.player2;

    return {
      success: true,
      opponentSkills: opponentPlayer.skills.map(skill => ({
        skillId: skill.skillId,
        skillName: skill.skillName,
        used: skill.used
      }))
    };
  }

  /**
   * 使用技能：清空术
   */
  useClear(game) {
    // 清空所有棋子
    game.board = Array(15).fill(null).map(() => Array(15).fill(0));
    game.moves = [];
    game.transparentCells = [];
    game.sealedCells = [];

    return { success: true };
  }

  /**
   * 使用技能：蒙蔽术
   */
  useBlind(game, player) {
    // 添加蒙蔽效果
    game.skillEffects.push({
      skillId: 'blind',
      skillName: '蒙蔽术',
      player: player === 1 ? 2 : 1, // 对对手生效
      effect: { blind: true },
      expiresAt: new Date(Date.now() + 60000) // 1分钟
    });

    return { success: true };
  }

  /**
   * 执行技能
   */
  executeSkill(game, skillId, player, params) {
    // 检查技能是否已使用
    const playerData = player === 1 ? game.player1 : game.player2;
    const skill = playerData.skills.find(s => s.skillId === skillId);
    
    if (!skill) {
      return { success: false, message: '未拥有该技能' };
    }
    
    if (skill.used) {
      return { success: false, message: '该技能已使用' };
    }

    // 检查是否有护盾
    const hasShield = game.skillEffects.some(
      effect => effect.skillId === 'shield' && 
                 effect.player === (player === 1 ? 2 : 1) &&
                 new Date(effect.expiresAt) > new Date()
    );

    if (hasShield && ['force-move', 'remove', 'seal'].includes(skillId)) {
      return { success: false, message: '对方使用了护盾，技能无效' };
    }

    let result;

    switch (skillId) {
      case 'chain-move':
        result = this.useChainMove(game, player, params.moves);
        break;
      case 'x-ray':
        result = this.useXRay(game, player, params.row, params.col);
        break;
      case 'force-move':
        result = this.useForceMove(game, player, params.centerRow, params.centerCol);
        break;
      case 'swap':
        result = this.useSwap(game);
        break;
      case 'seal':
        result = this.useSeal(game, player, params.centerRow, params.centerCol);
        break;
      case 'remove':
        result = this.useRemove(game, player, params.row, params.col);
        break;
      case 'shield':
        result = this.useShield(game, player);
        break;
      case 'prophecy':
        result = this.useProphecy(game, player);
        break;
      case 'clear':
        result = this.useClear(game);
        break;
      case 'blind':
        result = this.useBlind(game, player);
        break;
      default:
        return { success: false, message: '未知技能' };
    }

    if (result.success) {
      skill.used = true;
    }

    return result;
  }
}

module.exports = new SkillSystem();

