const User = require('../models/User');

/**
 * 微信登录
 */
exports.login = async (req, res) => {
  try {
    const { code, userInfo } = req.body;

    // TODO: 通过 code 获取 openid（需要调用微信API）
    // 这里简化处理，实际需要调用微信接口
    const openid = `mock_openid_${Date.now()}`; // 临时模拟

    // 查找或创建用户
    let user = await User.findOne({ openid });
    
    if (!user) {
      user = new User({
        openid,
        nickname: userInfo?.nickName || '玩家',
        avatar: userInfo?.avatarUrl || ''
      });
      await user.save();
    } else {
      user.lastLoginAt = new Date();
      await user.save();
    }

    res.json({
      success: true,
      data: {
        userId: user._id,
        openid: user.openid,
        nickname: user.nickname,
        avatar: user.avatar,
        points: user.points,
        rank: user.rank
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
 * 获取用户信息
 */
exports.getUserInfo = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    user.calculateWinRate();

    res.json({
      success: true,
      data: {
        userId: user._id,
        openid: user.openid,
        nickname: user.nickname,
        avatar: user.avatar,
        totalGames: user.totalGames,
        wins: user.wins,
        losses: user.losses,
        draws: user.draws,
        winRate: user.winRate,
        points: user.points,
        rank: user.rank,
        maxWinStreak: user.maxWinStreak,
        currentWinStreak: user.currentWinStreak
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
 * 更新用户信息
 */
exports.updateUserInfo = async (req, res) => {
  try {
    const { userId } = req.params;
    const { nickname, avatar } = req.body;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    if (nickname) user.nickname = nickname;
    if (avatar) user.avatar = avatar;

    await user.save();

    res.json({
      success: true,
      data: user
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

/**
 * 获取用户统计
 */
exports.getUserStats = async (req, res) => {
  try {
    const { userId } = req.params;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    user.calculateWinRate();

    res.json({
      success: true,
      data: {
        totalGames: user.totalGames,
        wins: user.wins,
        losses: user.losses,
        draws: user.draws,
        winRate: user.winRate,
        points: user.points,
        rank: user.rank,
        maxWinStreak: user.maxWinStreak,
        currentWinStreak: user.currentWinStreak,
        skillsUsed: Object.fromEntries(user.skillsUsed)
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};

