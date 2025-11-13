/**
 * WebSocket 连接处理
 * 处理实时对战功能
 */

const Game = require('../models/Game');
const gameLogic = require('./gameLogic');
const skillSystem = require('./skillSystem');

// 存储在线游戏房间
const gameRooms = new Map(); // gameId -> { game, sockets: [socket1, socket2] }

module.exports = (io, socket) => {
  console.log(`🔌 新连接: ${socket.id}`);

  // 加入游戏房间
  socket.on('join-game', async (data) => {
    try {
      const { gameId } = data;
      
      // 获取游戏
      const game = await Game.findOne({ gameId });
      if (!game) {
        socket.emit('error', { message: '游戏不存在' });
        return;
      }

      // 加入房间
      socket.join(gameId);
      
      // 管理房间
      if (!gameRooms.has(gameId)) {
        gameRooms.set(gameId, { game, sockets: [] });
      }
      gameRooms.get(gameId).sockets.push(socket);

      // 发送当前游戏状态
      socket.emit('game-state', {
        gameId: game.gameId,
        board: game.board,
        currentPlayer: game.currentPlayer,
        status: game.status,
        player1: game.player1,
        player2: game.player2
      });

      // 通知房间内其他玩家
      socket.to(gameId).emit('player-joined', {
        playerId: socket.id
      });
    } catch (error) {
      socket.emit('error', { message: error.message });
    }
  });

  // 落子
  socket.on('make-move', async (data) => {
    try {
      const { gameId, row, col, player } = data;
      
      const room = gameRooms.get(gameId);
      if (!room) {
        socket.emit('error', { message: '游戏房间不存在' });
        return;
      }

      const game = room.game;

      // 验证轮次
      if (game.currentPlayer !== player) {
        socket.emit('error', { message: '不是你的回合' });
        return;
      }

      // 验证落子
      const isValid = gameLogic.isValidMove(
        game.board,
        row,
        col,
        game.sealedCells
      );

      if (!isValid.valid) {
        socket.emit('error', { message: isValid.reason });
        return;
      }

      // 执行落子
      const result = gameLogic.makeMove(game.board, row, col, player);
      if (!result.success) {
        socket.emit('error', { message: '落子失败' });
        return;
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
        await game.save();

        // 广播游戏结束
        io.to(gameId).emit('game-over', {
          winner: player,
          winLine: winResult.line
        });
      } else {
        // 切换玩家
        game.currentPlayer = player === 1 ? 2 : 1;
        await game.save();

        // 广播落子结果
        io.to(gameId).emit('move-result', {
          row,
          col,
          player,
          board: game.board,
          currentPlayer: game.currentPlayer
        });
      }
    } catch (error) {
      socket.emit('error', { message: error.message });
    }
  });

  // 使用技能
  socket.on('use-skill', async (data) => {
    try {
      const { gameId, skillId, player, params } = data;
      
      const room = gameRooms.get(gameId);
      if (!room) {
        socket.emit('error', { message: '游戏房间不存在' });
        return;
      }

      const game = room.game;

      // 执行技能
      const result = skillSystem.executeSkill(game, skillId, player, params);
      
      if (!result.success) {
        socket.emit('error', { message: result.message });
        return;
      }

      await game.save();

      // 广播技能效果
      io.to(gameId).emit('skill-effect', {
        skillId,
        player,
        effect: result
      });
    } catch (error) {
      socket.emit('error', { message: error.message });
    }
  });

  // 认输
  socket.on('surrender', async (data) => {
    try {
      const { gameId, player } = data;
      
      const room = gameRooms.get(gameId);
      if (!room) {
        socket.emit('error', { message: '游戏房间不存在' });
        return;
      }

      const game = room.game;
      game.status = 'finished';
      game.winner = player === 1 ? 2 : 1;
      game.finishedAt = new Date();
      await game.save();

      // 广播认输
      io.to(gameId).emit('game-over', {
        winner: game.winner,
        reason: 'surrender'
      });
    } catch (error) {
      socket.emit('error', { message: error.message });
    }
  });

  // 断开连接
  socket.on('disconnect', () => {
    console.log(`❌ 断开连接: ${socket.id}`);
    
    // 清理房间
    for (const [gameId, room] of gameRooms.entries()) {
      const index = room.sockets.findIndex(s => s.id === socket.id);
      if (index !== -1) {
        room.sockets.splice(index, 1);
        
        // 如果房间为空，清理
        if (room.sockets.length === 0) {
          gameRooms.delete(gameId);
        }
      }
    }
  });
};

