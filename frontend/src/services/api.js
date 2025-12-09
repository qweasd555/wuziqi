import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证token等
    const user = localStorage.getItem('user');
    if (user) {
      const userData = JSON.parse(user);
      config.headers['X-User-ID'] = userData.id;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error('API Error:', error);
    
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // 未授权，清除本地用户信息
          localStorage.removeItem('user');
          window.location.href = '/login';
          break;
        case 404:
          console.error('资源不存在');
          break;
        case 500:
          console.error('服务器错误');
          break;
        default:
          console.error('请求失败:', data?.message || error.message);
      }
    } else if (error.request) {
      console.error('网络错误，无法连接到服务器');
    } else {
      console.error('请求配置错误:', error.message);
    }
    
    return Promise.reject(error);
  }
);

// 用户相关API
export const userAPI = {
  // 用户登录
  login: (openId, nickname) => 
    api.post('/user/login', null, { params: { openId, nickname } }),
  
  // 获取用户信息
  getUser: (userId) => 
    api.get(`/user/${userId}`),
  
  // 更新用户信息
  updateUser: (userId, userData) => 
    api.put(`/user/${userId}`, userData),
  
  // 获取用户统计
  getUserStats: (userId) => 
    api.get(`/user/${userId}/stats`),
  
  // 更新用户积分
  updateScore: (userId, scoreChange) => 
    api.post(`/user/${userId}/score`, { scoreChange }),
  
  // 获取排行榜
  getRanking: (page = 1, size = 20) => 
    api.get('/user/ranking', { params: { page, size } })
};

// 游戏相关API
export const gameAPI = {
  // 创建游戏
  createGame: (userId, mode, type = 'ONLINE_PVP') => 
    api.post('/game/create', { userId, mode, type }),
  
  // 加入游戏
  joinGame: (gameId, userId) => 
    api.post(`/game/${gameId}/join`, { userId }),
  
  // 获取游戏信息
  getGame: (gameId) => 
    api.get(`/game/${gameId}`),
  
  // 执行移动
  makeMove: (gameId, userId, position) => 
    api.post(`/game/${gameId}/move`, { userId, position }),
  
  // 使用技能
  useSkill: (gameId, userId, skillId) => 
    api.post(`/game/${gameId}/skill`, { userId, skillId }),
  
  // 放弃游戏
  giveUp: (gameId, userId) => 
    api.post(`/game/${gameId}/giveup`, { userId }),
  
  // 获取用户活跃游戏
  getUserActiveGames: (userId) => 
    api.get(`/game/user/${userId}/active`),
  
  // 获取可加入的游戏
  getAvailableGames: (mode) => 
    api.get('/game/available', { params: { mode } }),
  
  // 获取游戏历史
  getGameHistory: (userId, page = 1, size = 10) => 
    api.get(`/game/user/${userId}/history`, { params: { page, size } }),
  
  // AI相关
  makeAiMove: (gameId) => 
    api.post('/game/ai-move', { gameId }),
  
  setAiDifficulty: (difficulty) => 
    api.post('/game/ai-difficulty', { difficulty })
};

// 技能相关API
export const skillAPI = {
  // 获取所有技能
  getAllSkills: () => 
    api.get('/skill/all'),
  
  // 获取用户可用技能
  getUserSkills: (userId) => 
    api.get(`/skill/user/${userId}`),
  
  // 获取技能效果
  getSkillEffect: (skillId) => 
    api.get(`/skill/${skillId}/effect`)
};

// 微信相关API
export const wechatAPI = {
  // 微信登录
  login: (code) => 
    api.post('/wechat/login', { code }),
  
  // 获取用户信息
  getUserInfo: (openId) => 
    api.get(`/wechat/user/${openId}`)
};

export default api;
