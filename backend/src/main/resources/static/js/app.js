// 应用程序主逻辑
class App {
    constructor() {
        this.currentUser = null;
        this.stompClient = null;
        this.currentPage = 'home';
        this.init();
    }

    init() {
        // 检查用户登录状态
        this.checkUserStatus();
        
        // 初始化事件监听器
        this.initEventListeners();
        
        // 加载首页
        this.loadPage('home');
        
        // 初始化WebSocket连接
        this.initWebSocket();
    }

    initEventListeners() {
        // 导航点击事件
        document.querySelectorAll('[data-page]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = e.currentTarget.getAttribute('data-page');
                this.loadPage(page);
            });
        });

        // 登录表单提交
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.login();
        });

        // 退出登录
        document.getElementById('logoutBtn').addEventListener('click', (e) => {
            e.preventDefault();
            this.logout();
        });

        // 聊天消息发送
        document.getElementById('sendChatBtn').addEventListener('click', () => {
            this.sendChatMessage();
        });

        document.getElementById('chatInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendChatMessage();
            }
        });
    }

    async checkUserStatus() {
        const userId = localStorage.getItem('userId');
        const openId = localStorage.getItem('openId');
        const nickname = localStorage.getItem('nickname');

        if (userId && openId && nickname) {
            this.currentUser = {
                id: userId,
                openId: openId,
                nickname: nickname
            };
            this.updateUserUI();
        }
    }

    updateUserUI() {
        if (this.currentUser) {
            document.getElementById('userDropdown').style.display = 'block';
            document.getElementById('loginBtn').style.display = 'none';
            document.getElementById('userNickname').textContent = this.currentUser.nickname;
            
            // 获取用户详细信息
            this.getUserInfo(this.currentUser.id);
        } else {
            document.getElementById('userDropdown').style.display = 'none';
            document.getElementById('loginBtn').style.display = 'block';
        }
    }

    async login() {
        const openId = document.getElementById('loginOpenId').value.trim();
        const nickname = document.getElementById('loginNickname').value.trim();

        if (!openId || !nickname) {
            this.showAlert('请填写完整信息', 'warning');
            return;
        }

        try {
            const response = await fetch('/api/user/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `openId=${encodeURIComponent(openId)}&nickname=${encodeURIComponent(nickname)}`
            });

            if (response.ok) {
                const user = await response.json();
                this.currentUser = user;
                
                // 保存到本地存储
                localStorage.setItem('userId', user.id);
                localStorage.setItem('openId', user.openId);
                localStorage.setItem('nickname', user.nickname);

                this.updateUserUI();
                this.showAlert(`欢迎回来，${user.nickname}！`, 'success');
                
                // 关闭登录模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
                modal.hide();
                
                // 清空表单
                document.getElementById('loginForm').reset();
            } else {
                throw new Error('登录失败');
            }
        } catch (error) {
            console.error('登录错误:', error);
            this.showAlert('登录失败，请重试', 'danger');
        }
    }

    logout() {
        this.currentUser = null;
        localStorage.removeItem('userId');
        localStorage.removeItem('openId');
        localStorage.removeItem('nickname');
        this.updateUserUI();
        this.showAlert('已退出登录', 'info');
        this.loadPage('home');
    }

    async getUserInfo(userId) {
        try {
            const response = await fetch(`/api/user/${userId}`);
            if (response.ok) {
                const user = await response.json();
                this.currentUser = user;
                localStorage.setItem('nickname', user.nickname);
                document.getElementById('userNickname').textContent = user.nickname;
                
                // 更新头像（如果有）
                if (user.avatarUrl) {
                    document.getElementById('userAvatar').src = user.avatarUrl;
                }
            }
        } catch (error) {
            console.error('获取用户信息失败:', error);
        }
    }

    loadPage(pageName) {
        this.currentPage = pageName;
        
        // 更新导航状态
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });
        document.querySelector(`[data-page="${pageName}"]`).classList.add('active');

        const mainContent = document.getElementById('mainContent');
        
        switch (pageName) {
            case 'home':
                this.loadHomePage();
                break;
            case 'lobby':
                this.loadLobbyPage();
                break;
            case 'history':
                this.loadHistoryPage();
                break;
            case 'ranking':
                this.loadRankingPage();
                break;
            case 'profile':
                this.loadProfilePage();
                break;
            default:
                this.loadHomePage();
        }
    }

    loadHomePage() {
        const mainContent = document.getElementById('mainContent');
        mainContent.innerHTML = `
            <div class="main-container">
                <div class="text-center mb-5">
                    <h1 class="display-4 fw-bold mb-3">
                        <i class="fas fa-chess-board me-3 text-primary"></i>五棋
                    </h1>
                    <p class="lead text-muted">经典的五子棋游戏，支持在线对战和AI对战</p>
                </div>

                <div class="row g-4 mb-5">
                    <div class="col-md-4">
                        <div class="card h-100 game-card">
                            <div class="card-body text-center">
                                <div class="mb-3">
                                    <i class="fas fa-robot fa-3x text-primary"></i>
                                </div>
                                <h5 class="card-title">人机对战</h5>
                                <p class="card-text">与AI对战，提升棋艺水平</p>
                                <button class="btn btn-primary btn-animate" onclick="app.startAIGame()">
                                    <i class="fas fa-play me-1"></i>开始游戏
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card h-100 game-card">
                            <div class="card-body text-center">
                                <div class="mb-3">
                                    <i class="fas fa-users fa-3x text-success"></i>
                                </div>
                                <h5 class="card-title">在线对战</h5>
                                <p class="card-text">与其他玩家实时对战</p>
                                <button class="btn btn-success btn-animate" onclick="app.loadPage('lobby')">
                                    <i class="fas fa-door-open me-1"></i>进入大厅
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card h-100 game-card">
                            <div class="card-body text-center">
                                <div class="mb-3">
                                    <i class="fas fa-magic fa-3x text-warning"></i>
                                </div>
                                <h5 class="card-title">技能模式</h5>
                                <p class="card-text">使用特殊技能的对战模式</p>
                                <button class="btn btn-warning btn-animate" onclick="app.startSkillGame()">
                                    <i class="fas fa-sparkles me-1"></i>体验技能
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                ${this.currentUser ? this.loadUserStats() : this.loadWelcomeSection()}
            </div>
        `;
    }

    loadUserStats() {
        return `
            <div class="row g-4">
                <div class="col-md-3">
                    <div class="stat-card card text-center p-3">
                        <div class="stat-icon mx-auto bg-primary text-white">
                            <i class="fas fa-trophy"></i>
                        </div>
                        <h4 class="mt-2">${this.currentUser.score || 0}</h4>
                        <p class="text-muted mb-0">积分</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card card text-center p-3">
                        <div class="stat-icon mx-auto bg-success text-white">
                            <i class="fas fa-crown"></i>
                        </div>
                        <h4 class="mt-2">${this.currentUser.winCount || 0}</h4>
                        <p class="text-muted mb-0">胜场</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card card text-center p-3">
                        <div class="stat-icon mx-auto bg-info text-white">
                            <i class="fas fa-gamepad"></i>
                        </div>
                        <h4 class="mt-2">${this.currentUser.totalCount || 0}</h4>
                        <p class="text-muted mb-0">总场次</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card card text-center p-3">
                        <div class="stat-icon mx-auto bg-warning text-white">
                            <i class="fas fa-percentage"></i>
                        </div>
                        <h4 class="mt-2">${this.currentUser.winRate ? this.currentUser.winRate.toFixed(1) : 0}%</h4>
                        <p class="text-muted mb-0">胜率</p>
                    </div>
                </div>
            </div>
        `;
    }

    loadWelcomeSection() {
        return `
            <div class="text-center">
                <div class="card bg-light p-4">
                    <h4 class="mb-3">
                        <i class="fas fa-sign-in-alt me-2"></i>开始您的游戏之旅
                    </h4>
                    <p class="mb-4">登录后即可开始游戏，记录您的战绩，挑战其他玩家</p>
                    <button class="btn btn-lg btn-primary" data-bs-toggle="modal" data-bs-target="#loginModal">
                        <i class="fas fa-user-plus me-1"></i>立即登录
                    </button>
                </div>
            </div>
        `;
    }

    loadLobbyPage() {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            document.getElementById('loginModal').modal('show');
            return;
        }

        const mainContent = document.getElementById('mainContent');
        mainContent.innerHTML = `
            <div class="main-container">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>
                        <i class="fas fa-door-open me-2"></i>游戏大厅
                    </h2>
                    <div>
                        <button class="btn btn-success" onclick="app.createGame('REGULAR')">
                            <i class="fas fa-plus me-1"></i>创建常规房间
                        </button>
                        <button class="btn btn-warning" onclick="app.createGame('SKILL')">
                            <i class="fas fa-plus me-1"></i>创建技能房间
                        </button>
                    </div>
                </div>

                <div class="row" id="gameRooms">
                    <div class="col-12 text-center">
                        <div class="loading-spinner mx-auto"></div>
                        <p class="mt-2">加载游戏房间...</p>
                    </div>
                </div>
            </div>
        `;

        this.loadGameRooms();
    }

    loadHistoryPage() {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            return;
        }

        const mainContent = document.getElementById('mainContent');
        mainContent.innerHTML = `
            <div class="main-container">
                <h2 class="mb-4">
                    <i class="fas fa-history me-2"></i>游戏记录
                </h2>
                <div class="alert alert-info">
                    <i class="fas fa-info-circle me-2"></i>游戏记录功能正在开发中...
                </div>
            </div>
        `;
    }

    loadRankingPage() {
        const mainContent = document.getElementById('mainContent');
        mainContent.innerHTML = `
            <div class="main-container">
                <h2 class="mb-4">
                    <i class="fas fa-trophy me-2"></i>排行榜
                </h2>
                <div class="alert alert-info">
                    <i class="fas fa-info-circle me-2"></i>排行榜功能正在开发中...
                </div>
            </div>
        `;
    }

    loadProfilePage() {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            return;
        }

        const mainContent = document.getElementById('mainContent');
        mainContent.innerHTML = `
            <div class="main-container">
                <h2 class="mb-4">
                    <i class="fas fa-user me-2"></i>个人信息
                </h2>
                <div class="row">
                    <div class="col-md-4">
                        <div class="card text-center">
                            <div class="card-body">
                                <img src="${this.currentUser.avatarUrl || 'https://via.placeholder.com/100'}" 
                                     class="rounded-circle mb-3" width="100" height="100">
                                <h4>${this.currentUser.nickname}</h4>
                                <p class="text-muted">玩家ID: ${this.currentUser.id}</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="card">
                            <div class="card-header">
                                <h5>游戏统计</h5>
                            </div>
                            <div class="card-body">
                                ${this.loadUserStats()}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    async loadGameRooms() {
        try {
            const response = await fetch('/api/game/available?mode=REGULAR');
            if (response.ok) {
                const rooms = await response.json();
                this.displayGameRooms(rooms);
            }
        } catch (error) {
            console.error('加载游戏房间失败:', error);
            this.displayGameRooms([]);
        }
    }

    displayGameRooms(rooms) {
        const roomsContainer = document.getElementById('gameRooms');
        
        if (rooms.length === 0) {
            roomsContainer.innerHTML = `
                <div class="col-12 text-center">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle me-2"></i>当前没有可加入的游戏房间
                    </div>
                </div>
            `;
            return;
        }

        roomsContainer.innerHTML = rooms.map(room => `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card room-card" onclick="app.joinGame(${room.id})">
                    <div class="card-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <span><i class="fas fa-chess me-1"></i>房间 #${room.id}</span>
                            <span class="room-status ${room.player2 ? 'full' : 'waiting'}">
                                ${room.player2 ? '已满' : '等待中'}
                            </span>
                        </div>
                    </div>
                    <div class="card-body">
                        <p><strong>模式:</strong> ${room.mode === 'REGULAR' ? '常规模式' : '技能模式'}</p>
                        <p><strong>创建者:</strong> ${room.player1 ? room.player1.nickname : '未知'}</p>
                        <p><strong>状态:</strong> ${room.status === 'PENDING' ? '等待中' : '进行中'}</p>
                    </div>
                </div>
            </div>
        `).join('');
    }

    async createGame(mode) {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            return;
        }

        try {
            const response = await fetch('/api/game/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: this.currentUser.id,
                    mode: mode,
                    type: 'ONLINE_PVP'
                })
            });

            if (response.ok) {
                const game = await response.json();
                this.showAlert('游戏房间创建成功！', 'success');
                this.openGameModal(game);
            } else {
                throw new Error('创建游戏失败');
            }
        } catch (error) {
            console.error('创建游戏失败:', error);
            this.showAlert('创建游戏失败，请重试', 'danger');
        }
    }

    async joinGame(gameId) {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            return;
        }

        try {
            const response = await fetch(`/api/game/${gameId}/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: this.currentUser.id
                })
            });

            if (response.ok) {
                const game = await response.json();
                this.showAlert('成功加入游戏！', 'success');
                this.openGameModal(game);
            } else {
                const error = await response.json();
                throw new Error(error.message || '加入游戏失败');
            }
        } catch (error) {
            console.error('加入游戏失败:', error);
            this.showAlert(error.message || '加入游戏失败，请重试', 'danger');
        }
    }

    async startAIGame() {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            return;
        }

        try {
            const response = await fetch('/api/game/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: this.currentUser.id,
                    mode: 'REGULAR',
                    type: 'VS_AI'
                })
            });

            if (response.ok) {
                const game = await response.json();
                this.openGameModal(game);
            } else {
                throw new Error('创建AI游戏失败');
            }
        } catch (error) {
            console.error('创建AI游戏失败:', error);
            this.showAlert('创建游戏失败，请重试', 'danger');
        }
    }

    async startSkillGame() {
        if (!this.currentUser) {
            this.showAlert('请先登录', 'warning');
            return;
        }

        try {
            const response = await fetch('/api/game/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: this.currentUser.id,
                    mode: 'SKILL',
                    type: 'VS_AI'
                })
            });

            if (response.ok) {
                const game = await response.json();
                this.openGameModal(game);
            } else {
                throw new Error('创建技能游戏失败');
            }
        } catch (error) {
            console.error('创建技能游戏失败:', error);
            this.showAlert('创建游戏失败，请重试', 'danger');
        }
    }

    openGameModal(game) {
        const modal = new bootstrap.Modal(document.getElementById('gameModal'));
        
        // 设置游戏信息
        document.getElementById('gameTitle').textContent = 
            `${game.mode === 'REGULAR' ? '常规模式' : '技能模式'} - ${game.type === 'VS_AI' ? '人机对战' : '联机对战'}`;
        document.getElementById('gameMode').textContent = game.mode === 'REGULAR' ? '常规模式' : '技能模式';
        document.getElementById('gameType').textContent = 
            game.type === 'VS_AI' ? '人机对战' : 
            game.type === 'ONLINE_PVP' ? '联机对战' : '本地对战';

        // 设置玩家信息
        if (game.player1) {
            document.getElementById('player1Name').textContent = game.player1.nickname;
            document.getElementById('player1Avatar').src = game.player1.avatarUrl || 'https://via.placeholder.com/40';
        }

        // 修复：正确判断玩家2
        if (game.type === 'VS_AI') {
            // AI对战模式，始终显示AI玩家
            document.getElementById('player2Name').textContent = 'AI';
            document.getElementById('player2Avatar').src = 'https://via.placeholder.com/40';
        } else if (game.player2) {
            // 人类玩家
            document.getElementById('player2Name').textContent = game.player2.nickname;
            document.getElementById('player2Avatar').src = game.player2.avatarUrl || 'https://via.placeholder.com/40';
        } else {
            // 没有玩家2，显示等待中
            document.getElementById('player2Name').textContent = '等待玩家加入...';
            document.getElementById('player2Avatar').src = 'https://via.placeholder.com/40';
        }

        // 启动游戏
        if (gameInstance) {
            gameInstance.startGame(game);
        }

        modal.show();
    }

    initWebSocket() {
        // WebSocket连接将在游戏开始时建立
    }

    sendChatMessage() {
        const chatInput = document.getElementById('chatInput');
        const message = chatInput.value.trim();
        
        if (!message) return;

        const chatMessages = document.getElementById('chatMessages');
        const time = new Date().toLocaleTimeString();
        
        const messageElement = document.createElement('div');
        messageElement.className = 'chat-message';
        messageElement.innerHTML = `
            <div class="sender">${this.currentUser ? this.currentUser.nickname : '游客'}</div>
            <div>${message}</div>
            <div class="time">${time}</div>
        `;
        
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        chatInput.value = '';

        // TODO: 发送消息到服务器（如果实现了聊天功能）
    }

    showAlert(message, type = 'info') {
        const alertId = 'alert-' + Date.now();
        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show alert-custom" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', alertHtml);
        
        // 自动消失
        setTimeout(() => {
            const alertElement = document.getElementById(alertId);
            if (alertElement) {
                alertElement.remove();
            }
        }, 5000);
    }
}

// 全局变量
let app;

// 初始化应用
document.addEventListener('DOMContentLoaded', function() {
    app = new App();
    window.app = app;
    window.currentUser = app.currentUser;
});