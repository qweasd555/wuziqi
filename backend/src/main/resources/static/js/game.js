// 游戏核心逻辑
class Game {
    constructor() {
        this.canvas = document.getElementById('gameBoard');
        this.ctx = this.canvas.getContext('2d');
        this.boardSize = 15;
        this.cellSize = 40;
        this.board = [];
        this.currentPlayer = 1; // 1: 黑棋, 2: 白棋
        this.gameActive = false;
        this.currentGame = null;
        this.websocket = null;
        
        this.init();
    }

    init() {
        // 设置画布大小
        this.canvas.width = this.boardSize * this.cellSize + 40;
        this.canvas.height = this.boardSize * this.cellSize + 40;
        
        // 初始化棋盘数组
        this.board = Array(this.boardSize).fill().map(() => Array(this.boardSize).fill(0));
        
        // 绑定事件
        this.canvas.addEventListener('click', this.handleClick.bind(this));
        
        // 绘制棋盘
        this.drawBoard();
    }

    drawBoard() {
        const ctx = this.ctx;
        const cellSize = this.cellSize;
        const padding = 20;

        // 清空画布
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
        // 绘制棋盘背景
        ctx.fillStyle = '#daa520';
        ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // 绘制网格线
        ctx.strokeStyle = '#8b4513';
        ctx.lineWidth = 1;

        for (let i = 0; i < this.boardSize; i++) {
            // 横线
            ctx.beginPath();
            ctx.moveTo(padding, padding + i * cellSize);
            ctx.lineTo(padding + (this.boardSize - 1) * cellSize, padding + i * cellSize);
            ctx.stroke();

            // 竖线
            ctx.beginPath();
            ctx.moveTo(padding + i * cellSize, padding);
            ctx.lineTo(padding + i * cellSize, padding + (this.boardSize - 1) * cellSize);
            ctx.stroke();
        }

        // 绘制星位
        const starPoints = [
            [3, 3], [11, 3], [3, 11], [11, 11], [7, 7]
        ];

        ctx.fillStyle = '#8b4513';
        starPoints.forEach(([x, y]) => {
            ctx.beginPath();
            ctx.arc(padding + x * cellSize, padding + y * cellSize, 4, 0, Math.PI * 2);
            ctx.fill();
        });

        // 绘制棋子
        for (let i = 0; i < this.boardSize; i++) {
            for (let j = 0; j < this.boardSize; j++) {
                if (this.board[i][j] !== 0) {
                    this.drawStone(i, j, this.board[i][j]);
                }
            }
        }
    }

    drawStone(row, col, player) {
        const ctx = this.ctx;
        const cellSize = this.cellSize;
        const padding = 20;
        const x = padding + col * cellSize;
        const y = padding + row * cellSize;
        const radius = cellSize * 0.4;

        // 绘制阴影
        ctx.shadowColor = 'rgba(0, 0, 0, 0.3)';
        ctx.shadowBlur = 5;
        ctx.shadowOffsetX = 2;
        ctx.shadowOffsetY = 2;

        // 绘制棋子
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        
        if (player === 1) {
            // 黑棋
            const gradient = ctx.createRadialGradient(x - radius/3, y - radius/3, 0, x, y, radius);
            gradient.addColorStop(0, '#4a4a4a');
            gradient.addColorStop(1, '#1a1a1a');
            ctx.fillStyle = gradient;
        } else {
            // 白棋
            const gradient = ctx.createRadialGradient(x - radius/3, y - radius/3, 0, x, y, radius);
            gradient.addColorStop(0, '#ffffff');
            gradient.addColorStop(1, '#e0e0e0');
            ctx.fillStyle = gradient;
        }
        
        ctx.fill();

        // 重置阴影
        ctx.shadowColor = 'transparent';
        ctx.shadowBlur = 0;
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 0;

        // 绘制棋子边框
        ctx.strokeStyle = player === 1 ? '#000000' : '#cccccc';
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    handleClick(event) {
        if (!this.gameActive) {
            return;
        }

        const rect = this.canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;

        const cellSize = this.cellSize;
        const padding = 20;

        const col = Math.round((x - padding) / cellSize);
        const row = Math.round((y - padding) / cellSize);

        if (row >= 0 && row < this.boardSize && col >= 0 && col < this.boardSize) {
            if (this.board[row][col] === 0) {
                this.makeMove(row, col);
            }
        }
    }

    makeMove(row, col) {
        if (!this.gameActive || this.board[row][col] !== 0) {
            return;
        }

        // 放置棋子
        this.board[row][col] = this.currentPlayer;
        this.drawBoard();

        // 发送移动到服务器
        this.sendMove(row, col);

        // 检查获胜
        if (this.checkWin(row, col, this.currentPlayer)) {
            this.endGame(this.currentPlayer);
            return;
        }

        // 检查平局
        if (this.checkDraw()) {
            this.endGame('draw');
            return;
        }

        // 切换玩家
        this.currentPlayer = this.currentPlayer === 1 ? 2 : 1;
        this.updateGameStatus();
    }

    checkWin(row, col, player) {
        const directions = [
            [[0, 1], [0, -1], [1, 0], [-1, 0]],   // 水平和垂直
            [[1, 1], [-1, -1], [1, -1], [-1, 1]]  // 对角线
        ];

        for (let dir of directions) {
            let count = 1;
            
            // 检查四个方向
            for (let [dr, dc] of dir) {
                let r = row + dr;
                let c = col + dc;
                
                while (r >= 0 && r < this.boardSize && c >= 0 && c < this.boardSize && 
                       this.board[r][c] === player) {
                    count++;
                    r += dr;
                    c += dc;
                }
            }

            if (count >= 5) {
                return true;
            }
        }

        return false;
    }

    checkDraw() {
        for (let i = 0; i < this.boardSize; i++) {
            for (let j = 0; j < this.boardSize; j++) {
                if (this.board[i][j] === 0) {
                    return false;
                }
            }
        }
        return true;
    }

    startGame(gameData) {
        this.currentGame = gameData;
        this.gameActive = true;
        this.board = Array(this.boardSize).fill().map(() => Array(this.boardSize).fill(0));
        this.currentPlayer = gameData.currentPlayer || 1;
        this.drawBoard();
        this.updateGameStatus();

        // 初始化WebSocket连接
        this.initWebSocket();

        // 显示游戏控制按钮
        document.getElementById('startGameBtn').style.display = 'none';
        document.getElementById('giveUpBtn').style.display = 'inline-block';
        document.getElementById('resetGameBtn').style.display = 'inline-block';

        // 如果有技能，显示技能区域
        if (gameData.mode === 'SKILL') {
            document.getElementById('skillsSection').style.display = 'block';
            this.loadSkills();
        }
        
        // 更新玩家信息显示
        this.updatePlayerInfo();
    }

    // 更新玩家信息显示
    updatePlayerInfo() {
        if (!this.currentGame) return;
        
        const player1Name = document.getElementById('player1Name');
        const player2Name = document.getElementById('player2Name');
        const player1Avatar = document.getElementById('player1Avatar');
        const player2Avatar = document.getElementById('player2Avatar');
        
        if (player1Name && player1Avatar) {
            player1Name.textContent = currentUser.nickname || `玩家${currentUser.id}`;
            player1Avatar.src = currentUser.avatarUrl || 'https://via.placeholder.com/40';
        }
        
        if (player2Name && player2Avatar) {
            if (this.currentGame.type === 'VS_AI') {
                player2Name.textContent = 'AI';
                player2Avatar.src = 'https://via.placeholder.com/40';
            } else {
                player2Name.textContent = '等待对手...';
                player2Avatar.src = 'https://via.placeholder.com/40';
            }
        }
    }

    endGame(winner) {
        this.gameActive = false;
        
        let message = '';
        if (winner === 'draw') {
            message = '平局！';
        } else if (winner === 1) {
            message = '黑棋获胜！';
        } else {
            message = '白棋获胜！';
        }

        // 显示结果
        setTimeout(() => {
            alert(message);
        }, 100);

        // 隐藏游戏控制按钮
        document.getElementById('giveUpBtn').style.display = 'none';
        document.getElementById('resetGameBtn').style.display = 'inline-block';
    }

    updateGameStatus() {
        if (this.currentGame) {
            document.getElementById('currentTurn').textContent = 
                this.currentPlayer === 1 ? '黑棋' : '白棋';
            document.getElementById('gameStatus').textContent = 
                this.gameActive ? '游戏中' : '已结束';
        }
    }

    sendMove(row, col) {
        if (this.currentGame) {
            // 优先使用WebSocket发送移动
            if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
                this.websocket.send(JSON.stringify({
                    type: 'move',
                    gameId: this.currentGame.id,
                    userId: currentUser.id,
                    position: row * this.boardSize + col
                }));
            } else {
                // WebSocket不可用时的备选方案
                const moveData = {
                    gameId: this.currentGame.id,
                    userId: currentUser.id,
                    position: row * this.boardSize + col
                };

                fetch('/api/game/' + this.currentGame.id + '/move', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(moveData)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.error) {
                        alert('移动失败: ' + data.error);
                    } else {
                        // 更新本地棋盘
                        if (data.boardState) {
                            this.board = data.boardState;
                            this.drawBoard();
                        }
                    }
                })
                .catch(error => {
                    console.error('发送移动失败:', error);
                });
            }
        }
    }

    resetGame() {
        // 关闭WebSocket连接
        this.closeWebSocket();
        
        this.board = Array(this.boardSize).fill().map(() => Array(this.boardSize).fill(0));
        this.currentPlayer = 1;
        this.gameActive = false;
        this.currentGame = null;
        this.drawBoard();
        this.updateGameStatus();

        // 重置按钮显示
        document.getElementById('startGameBtn').style.display = 'inline-block';
        document.getElementById('giveUpBtn').style.display = 'none';
        document.getElementById('resetGameBtn').style.display = 'none';
    }

    giveUp() {
        if (this.currentGame && this.gameActive) {
            if (confirm('确定要认输吗？')) {
                fetch('/api/game/' + this.currentGame.id + '/giveup', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        userId: currentUser.id
                    })
                })
                .then(response => response.json())
                .then(data => {
                    this.endGame(this.currentPlayer === 1 ? 2 : 1);
                })
                .catch(error => {
                    console.error('认输失败:', error);
                });
            }
        }
    }

    loadSkills() {
        // 加载技能（示例）
        const skills = [
            { id: 1, name: '悔棋', cooldown: 3, cost: 10 },
            { id: 2, name: '提示', cooldown: 5, cost: 5 },
            { id: 3, name: '阻挡', cooldown: 4, cost: 15 }
        ];

        const skillsContainer = document.getElementById('skillsContainer');
        skillsContainer.innerHTML = '';

        skills.forEach(skill => {
            const btn = document.createElement('button');
            btn.className = 'btn skill-btn btn-animate';
            btn.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <span>${skill.name}</span>
                    <span class="badge bg-secondary">💎 ${skill.cost}</span>
                </div>
            `;
            btn.onclick = () => this.useSkill(skill.id);
            skillsContainer.appendChild(btn);
        });
    }

    useSkill(skillId) {
        if (!this.gameActive || !this.currentGame) {
            return;
        }

        fetch('/api/game/' + this.currentGame.id + '/skill', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: currentUser.id,
                skillId: skillId
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                alert('技能使用失败: ' + data.error);
            } else {
                console.log('技能使用成功:', data);
            }
        })
        .catch(error => {
            console.error('技能使用失败:', error);
        });
    }

    // 初始化WebSocket连接
    initWebSocket() {
        // 如果已有连接，先关闭
        if (this.websocket) {
            this.websocket.close();
        }

        // 创建WebSocket连接
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws?userId=${currentUser.id}`;
        
        this.websocket = new WebSocket(wsUrl);
        
        this.websocket.onopen = (event) => {
            console.log('WebSocket连接已建立');
            
            // 发送加入游戏消息
            if (this.currentGame && currentUser) {
                this.websocket.send(JSON.stringify({
                    type: 'join_game',
                    gameId: this.currentGame.id,
                    userId: currentUser.id,
                    role: 'player',
                    nickname: currentUser.nickname,
                    avatarUrl: currentUser.avatarUrl
                }));
            }
        };
        
        this.websocket.onmessage = (event) => {
            const message = JSON.parse(event.data);
            this.handleWebSocketMessage(message);
        };
        
        this.websocket.onerror = (error) => {
            console.error('WebSocket错误:', error);
        };
        
        this.websocket.onclose = (event) => {
            console.log('WebSocket连接已关闭:', event.code, event.reason);
            
            // 如果是非正常关闭，尝试重连
            if (event.code !== 1000 && this.gameActive) {
                console.log('尝试重新连接WebSocket...');
                setTimeout(() => {
                    this.initWebSocket();
                }, 3000);
            }
        };
    }

    // 处理WebSocket消息
    handleWebSocketMessage(message) {
        console.log('收到WebSocket消息:', message);
        
        switch (message.type) {
            case 'game_update':
                this.handleGameUpdate(message.data);
                break;
            case 'user_joined':
                this.handleUserJoined(message.data);
                break;
            case 'user_left':
                this.handleUserLeft(message.data);
                break;
            case 'chat':
                this.handleChatMessage(message.data);
                break;
            case 'skill_used':
                this.handleSkillUsed(message.data);
                break;
            default:
                console.log('未知消息类型:', message.type);
        }
    }

    // 处理游戏更新
    handleGameUpdate(data) {
        console.log('游戏状态更新:', data);
        
        // 更新游戏状态
        if (data.boardState) {
            this.board = JSON.parse(data.boardState);
            this.drawBoard();
        }
        
        if (data.currentPlayer) {
            this.currentPlayer = data.currentPlayer;
            this.updateGameStatus();
        }
        
        // 更新玩家信息
        if (data.player1Nickname) {
            const player1Name = document.getElementById('player1Name');
            const player1Avatar = document.getElementById('player1Avatar');
            if (player1Name) {
                player1Name.textContent = data.player1Nickname;
            }
            if (player1Avatar && data.player1AvatarUrl) {
                player1Avatar.src = data.player1AvatarUrl;
            }
        }
        
        if (data.player2Nickname) {
            const player2Name = document.getElementById('player2Name');
            const player2Avatar = document.getElementById('player2Avatar');
            if (player2Name) {
                player2Name.textContent = data.player2Nickname;
            }
            if (player2Avatar && data.player2AvatarUrl) {
                player2Avatar.src = data.player2AvatarUrl;
            }
        }
        
        // 检查游戏是否结束
        if (data.status === 'FINISHED') {
            this.gameActive = false;
            let winnerText = '';
            
            if (data.winner === 'draw') {
                winnerText = '平局！';
            } else if ((data.winner === 'player1' && data.player1Id == currentUser.id) || 
                      (data.winner === 'player2' && data.player2Id == currentUser.id)) {
                winnerText = '恭喜你赢了！';
            } else {
                winnerText = '很遗憾，你输了！';
            }
            
            // 显示游戏结果
            setTimeout(() => {
                alert(winnerText);
                // 更新游戏状态显示
                document.getElementById('gameStatus').textContent = '已结束';
                // 隐藏认输按钮，显示重新开始按钮
                document.getElementById('giveUpBtn').style.display = 'none';
                document.getElementById('resetGameBtn').style.display = 'inline-block';
            }, 100);
        }
    }

    // 处理用户加入
    handleUserJoined(data) {
        console.log('用户加入游戏:', data);
        
        // 更新玩家信息
        if (data.userId !== currentUser.id) {
            // 更新对手信息
            const player2Name = document.getElementById('player2Name');
            const player2Avatar = document.getElementById('player2Avatar');
            
            if (player2Name && player2Avatar) {
                // 如果当前游戏是AI对战，不更新玩家信息
                if (this.currentGame && this.currentGame.type === 'VS_AI') {
                    return;
                }
                
                // 更新为实际加入的玩家信息
                if (data.nickname) {
                    player2Name.textContent = data.nickname;
                } else {
                    player2Name.textContent = `玩家${data.userId}`;
                }
                
                if (data.avatarUrl) {
                    player2Avatar.src = data.avatarUrl;
                } else {
                    player2Avatar.src = 'https://via.placeholder.com/40';
                }
            }
        }
    }

    // 处理用户离开
    handleUserLeft(data) {
        console.log('用户离开游戏:', data);
        
        // 如果是对手离开，显示提示
        if (data.userId !== currentUser.id) {
            alert('对手已离开游戏');
        }
    }

    // 处理聊天消息
    handleChatMessage(data) {
        console.log('收到聊天消息:', data);
        // 这里可以实现聊天功能
    }

    // 处理技能使用
    handleSkillUsed(data) {
        console.log('技能使用:', data);
        
        // 更新游戏状态
        if (data.gameState) {
            if (data.gameState.boardState) {
                this.board = JSON.parse(data.gameState.boardState);
                this.drawBoard();
            }
            
            if (data.gameState.currentPlayer) {
                this.currentPlayer = data.gameState.currentPlayer;
                this.updateGameStatus();
            }
            
            // 更新玩家信息
            if (data.player1Nickname) {
                const player1Name = document.getElementById('player1Name');
                const player1Avatar = document.getElementById('player1Avatar');
                if (player1Name) {
                    player1Name.textContent = data.player1Nickname;
                }
                if (player1Avatar && data.player1AvatarUrl) {
                    player1Avatar.src = data.player1AvatarUrl;
                }
            }
            
            if (data.player2Nickname) {
                const player2Name = document.getElementById('player2Name');
                const player2Avatar = document.getElementById('player2Avatar');
                if (player2Name) {
                    player2Name.textContent = data.player2Nickname;
                }
                if (player2Avatar && data.player2AvatarUrl) {
                    player2Avatar.src = data.player2AvatarUrl;
                }
            }
            
            // 检查游戏是否结束
            if (data.gameState.status === 'FINISHED') {
                this.gameActive = false;
                let winnerText = '';
                
                if (data.gameState.winner === 'draw') {
                    winnerText = '平局！';
                } else if ((data.gameState.winner === 'player1' && data.player1Id == currentUser.id) || 
                          (data.gameState.winner === 'player2' && data.player2Id == currentUser.id)) {
                    winnerText = '恭喜你赢了！';
                } else {
                    winnerText = '很遗憾，你输了！';
                }
                
                // 显示游戏结果
                setTimeout(() => {
                    alert(winnerText);
                    // 更新游戏状态显示
                    document.getElementById('gameStatus').textContent = '已结束';
                    // 隐藏认输按钮，显示重新开始按钮
                    document.getElementById('giveUpBtn').style.display = 'none';
                    document.getElementById('resetGameBtn').style.display = 'inline-block';
                }, 100);
            }
        }
        
        // 显示技能效果
        if (data.effectDescription) {
            this.showSkillEffect(data.effectDescription);
        }
    }
    
    // 显示技能效果
    showSkillEffect(effectDescription) {
        // 创建技能效果提示
        const effectDiv = document.createElement('div');
        effectDiv.className = 'skill-effect';
        effectDiv.textContent = effectDescription;
        effectDiv.style.position = 'absolute';
        effectDiv.style.top = '50%';
        effectDiv.style.left = '50%';
        effectDiv.style.transform = 'translate(-50%, -50%)';
        effectDiv.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
        effectDiv.style.color = 'white';
        effectDiv.style.padding = '10px 20px';
        effectDiv.style.borderRadius = '5px';
        effectDiv.style.zIndex = '1000';
        effectDiv.style.fontSize = '16px';
        
        // 添加到游戏容器
        const gameContainer = document.getElementById('gameContainer');
        gameContainer.appendChild(effectDiv);
        
        // 3秒后移除
        setTimeout(() => {
            gameContainer.removeChild(effectDiv);
        }, 3000);
    }

    // 关闭WebSocket连接
    closeWebSocket() {
        if (this.websocket) {
            // 发送离开游戏消息
            if (this.currentGame && currentUser) {
                this.websocket.send(JSON.stringify({
                    type: 'leave_game',
                    gameId: this.currentGame.id,
                    userId: currentUser.id
                }));
            }
            
            this.websocket.close();
            this.websocket = null;
        }
    }
}

// 创建游戏实例
let gameInstance;

// 游戏模态框事件
document.addEventListener('DOMContentLoaded', function() {
    gameInstance = new Game();

    // 开始游戏按钮
    document.getElementById('startGameBtn').addEventListener('click', function() {
        if (gameInstance.currentGame) {
            gameInstance.gameActive = true;
            gameInstance.updateGameStatus();
        }
    });

    // 认输按钮
    document.getElementById('giveUpBtn').addEventListener('click', function() {
        gameInstance.giveUp();
    });

    // 重新开始按钮
    document.getElementById('resetGameBtn').addEventListener('click', function() {
        gameInstance.resetGame();
    });

    // 关闭游戏模态框
    document.getElementById('closeGameBtn').addEventListener('click', function() {
        if (gameInstance.gameActive) {
            if (confirm('游戏正在进行中，确定要离开吗？')) {
                gameInstance.gameActive = false;
                gameInstance.closeWebSocket();
                gameInstance.resetGame();
            }
        } else {
            // 即使游戏未激活，也要确保关闭WebSocket连接
            gameInstance.closeWebSocket();
            gameInstance.resetGame();
        }
    });
});