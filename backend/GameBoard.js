import React, { useEffect, useRef, useState } from 'react';
import { Layout, Card, Button, Typography, Space, Badge, Modal, message, Tag } from 'antd';
import { useWebSocket } from '../contexts/WebSocketContext';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import SkillPanel from './SkillPanel';

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

const GameBoard = ({ gameId }) => {
  const canvasRef = useRef(null);
  const [gameState, setGameState] = useState({
    board: [],
    currentPlayer: 1,
    winner: null,
    player1: null,
    player2: null,
    gameActive: false
  });
  const [skillModalVisible, setSkillModalVisible] = useState(false);
  const { user } = useAuth();
  const { sendMessage, lastMessage } = useWebSocket();
  const navigate = useNavigate();
  const [isConnected, setIsConnected] = useState(false);

  // WebSocket初始化函数
  const initWebSocket = () => {
    if (window.stompClient && window.stompClient.connected) {
      setIsConnected(true);
      message.success('WebSocket已连接');
      
      // 订阅游戏更新
      window.stompClient.subscribe(`/topic/game/${gameId}`, (message) => {
        try {
          const data = JSON.parse(message.body);
          console.log('收到游戏更新:', data);
          
          if (data.type === 'game_update' || data.type === 'GAME_UPDATE') {
            setGameState(prev => ({
              ...prev,
              ...data.gameState,
              gameActive: data.gameState.status === 'IN_PROGRESS'
            }));
          } else if (data.type === 'game_end' || data.type === 'GAME_END') {
            setGameState(prev => ({
              ...prev,
              gameActive: false,
              winner: data.winner
            }));
            message.info('游戏结束：' + (data.winner === user.id ? ' 你赢了！' : ' 你输了！'));
          }
        } catch (error) {
          console.error('解析游戏更新失败:', error);
        }
      });
    } else {
      setIsConnected(false);
      message.warning('WebSocket未连接，使用HTTP API');
    }
  };

  // 初始化游戏
  useEffect(() => {
    if (!gameId) return;

    // 初始化WebSocket连接
    initWebSocket();

    // 获取游戏状态
    fetch('/api/game/' + gameId)
      .then(res => res.json())
      .then(data => {
        setGameState(prev => ({
          ...prev,
          ...data,
          gameActive: data.status === 'IN_PROGRESS',
          player1: data.player1 ? data.player1.id : null,
          player2: data.player2 ? data.player2.id : null
        }));
      })
      .catch(err => {
        console.error('获取游戏状态失败', err);
        message.error('获取游戏状态失败');
      });

    // 初始化棋盘
    const board = Array(15).fill().map(() => Array(15).fill(0));
    setGameState(prev => ({ ...prev, board }));
  }, [gameId]);

  // 处理WebSocket消息
  useEffect(() => {
    if (!lastMessage) return;

    try {
      const data = JSON.parse(lastMessage.data);
      
      if (data.type === 'GAME_UPDATE' || data.type === 'game_update') {
        const gameStateData = data.gameState || data.data;
        if (gameStateData) {
          // 转换boardState为board数组
          const boardArray = convertBoardStateToArray(gameStateData.boardState);
          
          setGameState(prev => ({
            ...prev,
            ...gameStateData,
            board: boardArray,
            gameActive: gameStateData.status === 'IN_PROGRESS',
            player1: gameStateData.player1Id || prev.player1,
            player2: gameStateData.player2Id || prev.player2
          }));
        }
      } else if (data.type === 'GAME_END' || data.type === 'game_end') {
        setGameState(prev => ({
          ...prev,
          gameActive: false,
          winner: data.winner
        }));
        message.info('游戏结束：' + (data.winner === user.id ? ' 你赢了！' : ' 你输了！'));
      }
    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
    }
  }, [lastMessage, user]);

  // 绘制棋盘
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const cellSize = 40;
    const padding = 20;
    const boardSize = 15;

    // 设置画布大小
    canvas.width = boardSize * cellSize + 2 * padding;
    canvas.height = boardSize * cellSize + 2 * padding;

    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // 绘制棋盘背景
    ctx.fillStyle = '#daa520';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 绘制网格线
    ctx.strokeStyle = '#8b4513';
    ctx.lineWidth = 1;

    for (let i = 0; i < boardSize; i++) {
      // 横线
      ctx.beginPath();
      ctx.moveTo(padding, padding + i * cellSize);
      ctx.lineTo(padding + (boardSize - 1) * cellSize, padding + i * cellSize);
      ctx.stroke();

      // 竖线
      ctx.beginPath();
      ctx.moveTo(padding + i * cellSize, padding);
      ctx.lineTo(padding + i * cellSize, padding + (boardSize - 1) * cellSize);
      ctx.stroke();
    }

    // 绘制棋子
    if (gameState.board) {
      for (let i = 0; i < boardSize; i++) {
        for (let j = 0; j < boardSize; j++) {
          if (gameState.board[i] && gameState.board[i][j] !== 0) {     
            drawStone(ctx, i, j, gameState.board[i][j], cellSize, padding);
          }
        }
      }
    }
  }, [gameState.board]);

  // 绘制棋子
  const drawStone = (ctx, row, col, player, cellSize, padding) => {
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
  };

  // 处理点击事件
  const handleCanvasClick = (event) => {
    if (!gameState.gameActive) return;
    if (!isMyTurn()) {
      message.warning('不是你的回合，请等待对手下棋');
      return;
    }

    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const cellSize = 40;
    const padding = 20;

    const col = Math.round((x - padding) / cellSize);
    const row = Math.round((y - padding) / cellSize);

    if (row >= 0 && row < 15 && col >= 0 && col < 15) {
      if (gameState.board[row] && gameState.board[row][col] === 0) { 
        // 发送移动到服务器
        const position = row * 15 + col; // 将行列转换为位置索引
        
        console.log('发送移动:', { gameId, position, userId: user.id });
        
        // 使用WebSocket发送移动消息 - 修正消息格式
        if (window.stompClient && window.stompClient.connected) {
          window.stompClient.send(`/app/game/message`, {}, JSON.stringify({
            type: 'move',
            userId: user.id,
            gameId: gameId,
            data: {
              position: position
            },
            timestamp: Date.now()
          }));
        } else {
          // 降级使用HTTP API
          fetch(`/api/game/${gameId}/move`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              userId: user.id,
              position: position
            })
          })
          .then(response => response.json())
          .then(data => {
            if (data.success) {
              message.success('移动成功');
            } else {
              message.error('移动失败: ' + data.message);
            }
          })
          .catch(error => {
            console.error('移动请求失败:', error);
            message.error('网络错误，请重试');
          });
        }
      } else {
        message.warning('该位置已有棋子');
      }
    }
  };

  // 退出游戏
  const handleExitGame = () => {
    Modal.confirm({
      title: '确认退出',
      content: '确定要退出当前游戏吗？',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        navigate('/lobby');
      }
    });
  };

  // 转换boardState字符串为board数组
  const convertBoardStateToArray = (boardState) => {
    if (!boardState) return Array(15).fill().map(() => Array(15).fill(0));
    
    const board = Array(15).fill().map(() => Array(15).fill(0));
    for (let i = 0; i < 15; i++) {
      for (let j = 0; j < 15; j++) {
        const pos = i * 15 + j;
        if (boardState.charAt(pos) === 'X') {
          board[i][j] = 1; // 黑棋
        } else if (boardState.charAt(pos) === 'O') {
          board[i][j] = 2; // 白棋
        }
      }
    }
    return board;
  };

  // 判断当前玩家是否是轮到自己
  const isMyTurn = () => {
    if (!gameState.gameActive) return false;
    
    // 判断当前玩家是黑棋还是白棋
    const isPlayer1 = gameState.player1 === user.id;
    
    // 如果是玩家1，当前玩家为1时轮到自己；如果是玩家2，当前玩家为2时轮到自己
    return (isPlayer1 && gameState.currentPlayer === 1) || 
           (!isPlayer1 && gameState.currentPlayer === 2);
  };

  // 使用技能
  const handleUseSkill = (skillType) => {
    if (!gameState.gameActive || !isMyTurn()) return;
    
    sendMessage('/app/game/skill', {
      gameId,
      skillType,
      playerId: user.id
    });
    
    setSkillModalVisible(false);
    message.success('技能使用成功');
  };

  return (
    <Layout style={{ height: '100vh' }}>
      <Header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={3} style={{ color: 'white', margin: 0 }}>五子棋对战</Title>
        <Button type="primary" onClick={handleExitGame}>退出游戏</Button>
      </Header>

      <Layout>
        <Content style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '20px' }}>
          <Card>
            <canvas
              ref={canvasRef}
              onClick={handleCanvasClick}
              style={{ cursor: gameState.gameActive && isMyTurn() ? 'pointer' : 'default' }}
            />
          </Card>
        </Content>

        <Sider width={300} style={{ padding: '20px', background: '#f0f2f5' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Card title="游戏信息" size="small">
              <Space direction="vertical">
                <div>
                  <Text strong>当前状态: </Text>
                  {gameState.gameActive ? (
                    <Tag color={isMyTurn() ? 'green' : 'red'}>
                      {isMyTurn() ? '你的回合' : '对手回合'}
                    </Tag>
                  ) : (
                    <Tag color="orange">
                      {gameState.winner ? '游戏结束' : '等待开始'}
                    </Tag>
                  )}
                </div>
                <div>
                  <Text strong>玩家1 (黑棋): </Text>
                  <Tag color={gameState.player1 === user.id ? 'blue' : 'default'}>
                    {gameState.player1 === user.id ? '你' : '对手'}
                  </Tag>
                </div>
                <div>
                  <Text strong>玩家2 (白棋): </Text>
                  <Tag color={gameState.player2 === user.id ? 'blue' : 'default'}>
                    {gameState.player2 === user.id ? '你' : '对手'}
                  </Tag>
                </div>
              </Space>
            </Card>

            <SkillPanel
              gameId={gameId}
              disabled={!gameState.gameActive || !isMyTurn()}        
            />

            <Button
              type="primary"
              onClick={() => setSkillModalVisible(true)}
              disabled={!gameState.gameActive || !isMyTurn()}        
              style={{ width: '100%' }}
            >
              使用技能
            </Button>
          </Space>
        </Sider>
      </Layout>

      <Modal
        title="选择技能"
        open={skillModalVisible}
        onCancel={() => setSkillModalVisible(false)}
        footer={null}
      >
        <Space direction="vertical" style={{ width: '100%' }}>       
          <Button block onClick={() => handleUseSkill('RESET_BOARD')}>
            重置棋盘 - 清除3个随机棋子
          </Button>   
          <Button block onClick={() => handleUseSkill('EXTRA_TURN')}>
            额外回合 - 再下一子
          </Button>
          <Button block onClick={() => handleUseSkill('REMOVE_PIECE')}>
            移除棋子 - 移除对手1个棋子
          </Button>   
        </Space>
      </Modal>
    </Layout>
  );
};

export default GameBoard;