const Record = require('../models/Record');
const User = require('../models/User');

/**
 * 保存对局记录
 */
exports.saveRecord = async (req, res) => {
  try {
    const { gameId, gameData } = req.body;

    const record = new Record({
      gameId,
      mode: gameData.mode,
      type: gameData.type,
      player1: {
        userId: gameData.player1.userId,
        openid: gameData.player1.openid,
        nickname: gameData.player1.nickname,
        isBlack: gameData.player1.isBlack,
        isWinner: gameData.winner === 1,
        skillsUsed: gameData.player1.skills
          .filter(s => s.used)
          .map(s => s.skillId)
      },
      player2: {
        userId: gameData.player2.userId,
        openid: gameData.player2.openid,
        nickname: gameData.player2.nickname,
        isBlack: gameData.player2.isBlack,
        isWinner: gameData.winner === 2,
        skillsUsed: gameData.player2.skills
          .filter(s => s.used)
          .map(s => s.skillId)
      },
      winner: gameData.winner,
      result: gameData.winner === 1 ? 'win' : gameData.winner === 2 ? 'loss' : 'draw',
      totalMoves: gameData.moves.length,
      duration: gameData.duration,
      moves: gameData.moves,
      finishedAt: new Date()
    });

    await record.save();

    // 更新用户数据
    if (gameData.player1.userId !== 'ai') {
      await updateUserStats(gameData.player1.userId, gameData.winner === 1);
    }
    if (gameData.player2.userId !== 'ai' && gameData.player2.userId) {
      await updateUserStats(gameData.player2.userId, gameData.winner === 2);
    }

    res.json({
      success: true,
      data: record
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 更新用户统计数据
 */
async function updateUserStats(userId, isWin) {
  try {
    const user = await User.findById(userId);
    if (!user) return;

    user.totalGames += 1;
    if (isWin) {
      user.wins += 1;
      user.points += 10;
      user.updateWinStreak(true);
    } else {
      user.losses += 1;
      user.points = Math.max(0, user.points - 5);
      user.updateWinStreak(false);
    }

    user.calculateWinRate();
    user.updateRank();
    await user.save();
  } catch (error) {
    console.error('更新用户统计失败:', error);
  }
}

/**
 * 获取对战记录列表
 */
exports.getRecordList = async (req, res) => {
  try {
    const { userId, page = 1, limit = 20 } = req.query;

    const query = {
      $or: [
        { 'player1.userId': userId },
        { 'player2.userId': userId }
      ]
    };

    const records = await Record.find(query)
      .sort({ createdAt: -1 })
      .limit(limit * 1)
      .skip((page - 1) * limit);

    const total = await Record.countDocuments(query);

    res.json({
      success: true,
      data: {
        records,
        total,
        page: parseInt(page),
        limit: parseInt(limit),
        totalPages: Math.ceil(total / limit)
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
 * 获取单条记录详情
 */
exports.getRecord = async (req, res) => {
  try {
    const { id } = req.params;

    const record = await Record.findById(id);
    if (!record) {
      return res.status(404).json({
        success: false,
        message: '记录不存在'
      });
    }

    res.json({
      success: true,
      data: record
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 获取排行榜
 */
exports.getRank = async (req, res) => {
  try {
    const { type = 'points', limit = 100 } = req.query;

    let sortField = 'points';
    if (type === 'wins') sortField = 'wins';
    if (type === 'winRate') sortField = 'winRate';
    if (type === 'streak') sortField = 'maxWinStreak';

    const users = await User.find()
      .sort({ [sortField]: -1 })
      .limit(parseInt(limit))
      .select('nickname avatar points rank wins winRate maxWinStreak');

    res.json({
      success: true,
      data: users
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

