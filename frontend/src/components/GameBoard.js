// 修复后的GameBoard.js文件
import React, { useEffect, useRef, useState } from 'react';
import { Layout, Card, Button, Typography, Space, Modal, message, Tag, Spin } from 'antd';
import { useWebSocket } from '../contexts/WebSocketContext';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { gameAPI } from '../services/api';
import SkillPanel from './SkillPanel';

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

const GameBoard = ({ gameId }) => {
  const canvasRef = useRef(null);
  const [gameState, setGameState] = useState({
    board: [],
    currentPlayer: 1,
    gameActive: false,
    player1: null,
    player2: null,
    winner: null
  });
  const [skillModalVisible, setSkillModalVisible] = useState(false);
  const [loading, setLoading] = useState(true); 
  const { user } = useAuth();
  const { sendMove, sendUseSkill, startGame, lastMessage } = useWebSocket();
  const navigate = useNavigate();

  // 初始化游戏
  useEffect(() => {
    if (!gameId) {
      console.error('Game ID is undefined');
      message.error('游戏ID无效');
      setLoading(false);
      return;
    }

    console.log('Loading game with ID:', gameId);
    setLoading(true);
    
    // 获取游戏状态
    gameAPI.getGame(gameId)
      .then(data => {
        console.log('Game data received:', data);
        // 从后端数据中提取玩家ID
        const player1Id = data.player1 ? data.player1.id : null;
        const player2Id = data.player2 ? data.player2.id : null;
        
        // 解析棋盘状态
        let board = Array(15).fill().map(() => Array(15).fill(0));
        if (data.boardState) {
          try {
            const parsedBoard = JSON.parse(data.boardState);
            if (Array.isArray(parsedBoard) && parsedBoard.length === 15) {
              board = parsedBoard;
            }
          } catch (e) {
            console.warn('Failed to parse board state, using empty board');
          }
        }
        
        setGameState(prev => ({
          ...prev,
          ...data,
          player1: player1Id,
          player2: player2Id,
          board: board,
          gameActive: data.status === 'IN_PROGRESS'
        }));
      })
      .catch(err => {
        console.error('获取游戏状态失败', err);
        message.error('获取游戏状态失败: ' + (err.message || '未知错误'));
        // 初始化空棋盘作为后备
        const board = Array(15).fill().map(() => Array(15).fill(0));
        setGameState(prev => ({ ...prev, board }));
      })
      .finally(() => {
        setLoading(false);
      });
  }, [gameId]);

  // 处理WebSocket消息
  useEffect(() => {
    if (!lastMessage) return;

    try {
      const data = JSON.parse(lastMessage.data);

      if (data.type === 'GAME_UPDATE') {
        // 从后端数据中提取玩家ID
        const player1Id = data.gameState.player1 ? data.gameState.player1.id : null;
        const player2Id = data.gameState.player2 ? data.gameState.player2.id : null;
        
        setGameState(prev => ({
          ...prev,
          ...data.gameState,
          player1: player1Id,
          player2: player2Id
        }));
      } else if (data.type === 'GAME_END') {
        setGameState(prev => ({
          ...prev,
          gameActive: false,
          winner: data.winner
        }));
      } else if (data.event === 'gameStarted') {
        // 游戏开始事件
        setGameState(prev => ({
          ...prev,
          gameActive: true
        }));
        message.success('游戏已开始！');
      }
    } catch (error) {
      console.error('处理WebSocket消息失败', error);
    }
  }, [lastMessage, user]);

  // 绘制棋盘
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const boardSize = 15;
    const cellSize = 30;
    const padding = 20;
    const canvasSize = cellSize * (boardSize - 1) + padding * 2;

    canvas.width = canvasSize;
    canvas.height = canvasSize;

    // 清空画布
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
    for (let i = 0; i < boardSize; i++) {
      for (let j = 0; j < boardSize; j++) {
        if (gameState.board[i] && gameState.board[i][j] !== 0) {     
          drawStone(ctx, i, j, gameState.board[i][j], cellSize, padding);
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
      gradient.addColorStop(1, '#cccccc');
      ctx.fillStyle = gradient;
    }

    ctx.fill();
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1;
    ctx.stroke();

    // 重置阴影
    ctx.shadowColor = 'transparent';
    ctx.shadowBlur = 0;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;
  };

  // 处理棋盘点击
  const handleCanvasClick = (event) => {
    if (!isMyTurn()) return;

    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const cellSize = 30;
    const padding = 20;

    // 计算点击的格子
    const col = Math.round((x - padding) / cellSize);
    const row = Math.round((y - padding) / cellSize);

    // 检查是否在有效范围内
    if (row < 0 || row >= 15 || col < 0 || col >= 15) return;
    if (gameState.board[row][col] !== 0) return;

    // 发送移动请求
    sendMove(gameId, user.id, row * 15 + col);
  };

  // 判断当前玩家是否是自己
  const isMyTurn = () => {
    if (!gameState.gameActive || !user?.id) return false;

    // 判断当前玩家是黑棋还是白棋
    const isPlayer1 = String(gameState.player1) === String(user.id);

    // 如果是玩家1，当前玩家为1时轮到自己；如果是玩家2，当前玩家为2时轮到自己
    return (isPlayer1 && gameState.currentPlayer === 1) ||
           (!isPlayer1 && gameState.currentPlayer === 2);
  };

  // 使用技能
  const handleUseSkill = (skillType) => {
    setSkillModalVisible(false);
    
    sendUseSkill(gameId, user.id, skillType);
  };

  // 退出游戏
  const handleExitGame = () => {
    navigate('/lobby');
  };

  if (loading) {
    return (
      <Layout style={{ height: '100vh', justifyContent: 'center', alignItems: 'center' }}>
        <Spin size="large" tip="加载游戏中..." />
      </Layout>
    );
  }

  return (
    <Layout style={{ height: '100vh' }}>
      <Header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={3} style={{ color: 'white', margin: 0 }}>五子棋对战</Title>
        <Button type="primary" onClick={handleExitGame}>退出游戏</Button>
      </Header>
      
      <Layout>
        <Content style={{ padding: '20px', display: 'flex', justifyContent: 'center' }}>
          <Card title="游戏棋盘" style={{ width: 'fit-content' }}>
            <canvas
              ref={canvasRef}
              onClick={handleCanvasClick}
              style={{ cursor: isMyTurn() ? 'pointer' : 'not-allowed' }}
            />
          </Card>
        </Content>
        
        <Sider width={300} style={{ padding: '20px', background: '#f0f2f5' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Card title="游戏信息" size="small">
              <Space direction="vertical" style={{ width: '100%' }}>
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
                {!gameState.gameActive && !gameState.winner && (
                  <Button 
                    type="primary" 
                    onClick={() => {
                      startGame(gameId, user.id).catch(err => {
                        console.error('开始游戏失败', err);
                        message.error('开始游戏失败: ' + (err.message || '未知错误'));
                      });
                    }}
                    style={{ width: '100%', marginTop: '10px' }}
                  >
                    开始游戏
                  </Button>
                )}
                <div>
                  <Text strong>玩家1 (黑棋): </Text>
                  <Tag color={String(gameState.player1) === String(user?.id) ? 'blue' : 'default'}>
                    {gameState.player1 == null ? 'AI' : (String(gameState.player1) === String(user?.id) ? '你' : '对手')}
                  </Tag>
                </div>
                <div>
                  <Text strong>玩家2 (白棋): </Text>
                  <Tag color={String(gameState.player2) === String(user?.id) ? 'blue' : 'default'}>
                    {gameState.player2 == null ? 'AI' : (String(gameState.player2) === String(user?.id) ? '你' : '对手')}
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
