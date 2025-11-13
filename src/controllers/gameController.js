const { v4: uuidv4 } = require('uuid');
const Game = require('../models/Game');
const gameLogic = require('../services/gameLogic');
const skillSystem = require('../services/skillSystem');
const aiService = require('../services/aiService');

/**
 * 创建对局
 */
exports.createGame = async (req, res) => {
  try {
    const { mode, type, player1, player2 } = req.body;

    const gameId = uuidv4();
    const game = new Game({
      gameId,
      mode: mode || 'normal',
      type: type || 'local',
      player1: {
        userId: player1.userId,
        openid: player1.openid,
        nickname: player1.nickname,
        avatar: player1.avatar,
        isBlack: true
      },
      player2: player2 ? {
        userId: player2.userId,
        openid: player2.openid,
        nickname: player2.nickname,
        avatar: player2.avatar,
        isBlack: false
      } : {
        userId: 'ai',
        nickname: 'AI',
        isBlack: false
      },
      status: 'playing',
      startedAt: new Date()
    });

    // 技能模式：随机分配技能
    if (mode === 'skill') {
      game.player1.skills = skillSystem.randomSelectSkills(3);
      if (player2) {
        game.player2.skills = skillSystem.randomSelectSkills(3);
      }
    }

    await game.save();

    res.json({
      success: true,
      data: {
        gameId: game.gameId,
        mode: game.mode,
        type: game.type,
        player1: game.player1,
        player2: game.player2,
        board: game.board
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 获取对局信息
 */
exports.getGame = async (req, res) => {
  try {
    const { id } = req.params;
    
    const game = await Game.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({
        success: false,
        message: '对局不存在'
      });
    }

    res.json({
      success: true,
      data: game
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 落子
 */
exports.makeMove = async (req, res) => {
  try {
    const { id } = req.params;
    const { row, col, player } = req.body;

    const game = await Game.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({
        success: false,
        message: '对局不存在'
      });
    }

    if (game.status !== 'playing') {
      return res.status(400).json({
        success: false,
        message: '对局已结束'
      });
    }

    // 验证轮次
    if (game.currentPlayer !== player) {
      return res.status(400).json({
        success: false,
        message: '不是你的回合'
      });
    }

    // 验证落子
    const isValid = gameLogic.isValidMove(
      game.board,
      row,
      col,
      game.sealedCells
    );

    if (!isValid.valid) {
      return res.status(400).json({
        success: false,
        message: isValid.reason
      });
    }

    // 执行落子
    const result = gameLogic.makeMove(game.board, row, col, player);
    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: '落子失败'
      });
    }

    game.board = result.board;
    game.moves.push({
      player,
      row,
      col,
      timestamp: new Date()
    });

    // 检查胜负
    const winResult = gameLogic.checkWin(game.board, row, col, player);
    if (winResult.win) {
      game.status = 'finished';
      game.winner = player;
      game.finishedAt = new Date();
      game.duration = Math.floor((new Date() - game.startedAt) / 1000);
    } else {
      // 检查平局
      if (gameLogic.checkDraw(game.board)) {
        game.status = 'finished';
        game.winner = 0;
        game.finishedAt = new Date();
        game.duration = Math.floor((new Date() - game.startedAt) / 1000);
      } else {
        // 切换玩家
        game.currentPlayer = player === 1 ? 2 : 1;

        // AI 自动落子
        if (game.type === 'ai' && game.currentPlayer === 2) {
          const aiMove = aiService.makeMove(
            game.board,
            2,
            'medium',
            game.sealedCells
          );

          if (aiMove) {
            const aiResult = gameLogic.makeMove(
              game.board,
              aiMove.row,
              aiMove.col,
              2
            );

            if (aiResult.success) {
              game.board = aiResult.board;
              game.moves.push({
                player: 2,
                row: aiMove.row,
                col: aiMove.col,
                timestamp: new Date()
              });

              // 检查 AI 是否获胜
              const aiWinResult = gameLogic.checkWin(
                game.board,
                aiMove.row,
                aiMove.col,
                2
              );

              if (aiWinResult.win) {
                game.status = 'finished';
                game.winner = 2;
                game.finishedAt = new Date();
                game.duration = Math.floor((new Date() - game.startedAt) / 1000);
              } else {
                game.currentPlayer = 1;
              }
            }
          }
        }
      }
    }

    await game.save();

    res.json({
      success: true,
      data: {
        board: game.board,
        currentPlayer: game.currentPlayer,
        status: game.status,
        winner: game.winner,
        winLine: winResult.win ? winResult.line : null
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 使用技能
 */
exports.useSkill = async (req, res) => {
  try {
    const { id } = req.params;
    const { skillId, player, params } = req.body;

    const game = await Game.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({
        success: false,
        message: '对局不存在'
      });
    }

    if (game.mode !== 'skill') {
      return res.status(400).json({
        success: false,
        message: '当前模式不支持技能'
      });
    }

    // 执行技能
    const result = skillSystem.executeSkill(game, skillId, player, params);

    if (!result.success) {
      return res.status(400).json({
        success: false,
        message: result.message
      });
    }

    await game.save();

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 认输
 */
exports.surrender = async (req, res) => {
  try {
    const { id } = req.params;
    const { player } = req.body;

    const game = await Game.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({
        success: false,
        message: '对局不存在'
      });
    }

    game.status = 'finished';
    game.winner = player === 1 ? 2 : 1;
    game.finishedAt = new Date();
    game.duration = Math.floor((new Date() - game.startedAt) / 1000);

    await game.save();

    res.json({
      success: true,
      data: {
        winner: game.winner,
        reason: 'surrender'
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

