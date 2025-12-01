import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Layout, 
  Menu, 
  Card, 
  Button, 
  Avatar, 
  Typography, 
  Row, 
  Col, 
  Space, 
  Badge,
  message,
  Modal,
  Form,
  Select,
  Tag
} from 'antd';
import {
  UserOutlined,
  TrophyOutlined,
  PlusOutlined,
  PlayCircleOutlined,
  RobotOutlined,
  UsergroupAddOutlined,
  LogoutOutlined
} from '@ant-design/icons';
import styled from 'styled-components';
import { useAuth } from '../contexts/AuthContext';
import { gameAPI, userAPI } from '../services/api';
import { GameMode, GameType } from '../utils/constants';

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;
const { Option } = Select;

const HallContainer = styled(Layout)`
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
`;

const HeaderStyled = styled(Header)`
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
`;

const UserSection = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
`;

const GameCard = styled(Card)`
  height: 180px;
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 2px solid transparent;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #667eea;
  }
  
  .ant-card-body {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
`;

const StatsCard = styled(Card)`
  border-radius: 12px;
  text-align: center;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  
  .ant-card-body {
    padding: 24px;
  }
  
  .stats-number {
    font-size: 32px;
    font-weight: bold;
    margin-bottom: 8px;
  }
`;

const CreateGameModal = ({ visible, onCancel, onOk, loading }) => {
  const [form] = Form.useForm();

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onOk(values);
    } catch (error) {
      console.error('Validation failed:', error);
    }
  };

  return (
    <Modal
      title="创建新游戏"
      visible={visible}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      okText="创建游戏"
      cancelText="取消"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          mode: 'REGULAR',
          type: 'ONLINE_PVP'
        }}
      >
        <Form.Item
          name="mode"
          label="游戏模式"
          rules={[{ required: true, message: '请选择游戏模式！' }]}
        >
          <Select placeholder="选择游戏模式">
            <Option value="REGULAR">常规模式</Option>
            <Option value="SKILL">技能模式</Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="type"
          label="对战类型"
          rules={[{ required: true, message: '请选择对战类型！' }]}
        >
          <Select placeholder="选择对战类型">
            <Option value="ONLINE_PVP">联机对战</Option>
            <Option value="VS_AI">人机对战</Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};

const GameHall = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [availableGames, setAvailableGames] = useState([]);
  const [userStats, setUserStats] = useState(null);
  const [activeGames, setActiveGames] = useState([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [selectedMenu, setSelectedMenu] = useState('hall');

  // 加载可用游戏
  const loadAvailableGames = async (mode = 'REGULAR') => {
    try {
      const games = await gameAPI.getAvailableGames(mode);
      setAvailableGames(games || []);
    } catch (error) {
      console.error('Failed to load available games:', error);
      message.error('加载游戏列表失败');
    }
  };

  // 加载用户统计
  const loadUserStats = async () => {
    if (!user) return;
    
    try {
      const stats = await userAPI.getUserStats(user.id);
      setUserStats(stats);
    } catch (error) {
      console.error('Failed to load user stats:', error);
    }
  };

  // 加载活跃游戏
  const loadActiveGames = async () => {
    if (!user) return;
    
    try {
      const games = await gameAPI.getUserActiveGames(user.id);
      setActiveGames(games || []);
    } catch (error) {
      console.error('Failed to load active games:', error);
    }
  };

  // 创建游戏
  const handleCreateGame = async (values) => {
    setLoading(true);
    try {
      const game = await gameAPI.createGame(
        user.id,
        values.mode,
        values.type
      );
      
      message.success('游戏创建成功！');
      setCreateModalVisible(false);
      
      // 跳转到游戏页面
      navigate(`/game/${game.id}`);
    } catch (error) {
      console.error('Failed to create game:', error);
      message.error('创建游戏失败：' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };

  // 加入游戏
  const handleJoinGame = async (gameId) => {
    setLoading(true);
    try {
      const game = await gameAPI.joinGame(gameId, user.id);
      message.success('成功加入游戏！');
      navigate(`/game/${game.id}`);
    } catch (error) {
      console.error('Failed to join game:', error);
      message.error('加入游戏失败：' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };

  // 处理登出
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // 监听菜单选择变化
  const handleMenuSelect = ({ key }) => {
    setSelectedMenu(key);
    if (key === 'regular') {
      loadAvailableGames('REGULAR');
    } else if (key === 'skill') {
      loadAvailableGames('SKILL');
    }
  };

  useEffect(() => {
    loadUserStats();
    loadActiveGames();
    loadAvailableGames();
  }, [user]);

  return (
    <HallContainer>
      <HeaderStyled>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <Title level={3} style={{ color: 'white', margin: 0 }}>
            五子棋对战大厅
          </Title>
        </div>
        
        <UserSection>
          <Space>
            <Avatar icon={<UserOutlined />} />
            <span>{user?.nickname}</span>
            <Badge count={user?.score || 0} style={{ backgroundColor: '#52c41a' }}>
              <TrophyOutlined style={{ fontSize: '20px' }} />
            </Badge>
          </Space>
          <Button
            type="text"
            icon={<LogoutOutlined />}
            onClick={handleLogout}
            style={{ color: 'white' }}
          >
            退出
          </Button>
        </UserSection>
      </HeaderStyled>

      <Layout>
        <Sider width={250} style={{ background: 'white' }}>
          <Menu
            mode="inline"
            selectedKeys={[selectedMenu]}
            onSelect={handleMenuSelect}
            style={{ height: '100%', borderRight: 0 }}
          >
            <Menu.Item key="hall" icon={<PlayCircleOutlined />}>
              游戏大厅
            </Menu.Item>
            <Menu.Item key="regular" icon={<UsergroupAddOutlined />}>
              常规模式
            </Menu.Item>
            <Menu.Item key="skill" icon={<TrophyOutlined />}>
              技能模式
            </Menu.Item>
            <Menu.Item key="ai" icon={<RobotOutlined />}>
              人机对战
            </Menu.Item>
          </Menu>
        </Sider>

        <Layout style={{ padding: '24px' }}>
          <Content>
            {/* 用户统计 */}
            {userStats && (
              <Row gutter={16} style={{ marginBottom: '24px' }}>
                <Col span={6}>
                  <StatsCard>
                    <div className="stats-number">{userStats.score}</div>
                    <div>总积分</div>
                  </StatsCard>
                </Col>
                <Col span={6}>
                  <StatsCard style={{ background: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }}>
                    <div className="stats-number">{userStats.wins}</div>
                    <div>胜利场次</div>
                  </StatsCard>
                </Col>
                <Col span={6}>
                  <StatsCard style={{ background: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' }}>
                    <div className="stats-number">{userStats.totalGames}</div>
                    <div>总场次</div>
                  </StatsCard>
                </Col>
                <Col span={6}>
                  <StatsCard style={{ background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' }}>
                    <div className="stats-number">{userStats.winRate}%</div>
                    <div>胜率</div>
                  </StatsCard>
                </Col>
              </Row>
            )}

            {/* 操作按钮 */}
            <Row gutter={16} style={{ marginBottom: '24px' }}>
              <Col span={12}>
                <Button
                  type="primary"
                  size="large"
                  icon={<PlusOutlined />}
                  block
                  onClick={() => setCreateModalVisible(true)}
                  loading={loading}
                >
                  创建新游戏
                </Button>
              </Col>
              <Col span={12}>
                <Button
                  size="large"
                  block
                  onClick={() => loadAvailableGames()}
                >
                  刷新游戏列表
                </Button>
              </Col>
            </Row>

            {/* 活跃游戏 */}
            {activeGames.length > 0 && (
              <Card title="我的游戏" style={{ marginBottom: '24px' }}>
                <Row gutter={16}>
                  {activeGames.map(game => (
                    <Col span={8} key={game.id} style={{ marginBottom: '16px' }}>
                      <GameCard
                        onClick={() => navigate(`/game/${game.id}`)}
                        hoverable
                      >
                        <div>
                          <Title level={4}>游戏 #{game.id}</Title>
                          <Space>
                            <Tag color="blue">{game.mode}</Tag>
                            <Tag color="green">{game.status}</Tag>
                          </Space>
                        </div>
                        <Button type="primary" block>
                          继续游戏
                        </Button>
                      </GameCard>
                    </Col>
                  ))}
                </Row>
              </Card>
            )}

            {/* 可加入的游戏 */}
            <Card title={`可加入的游戏 (${availableGames.length})`}>
              {availableGames.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px' }}>
                  <Text type="secondary">暂无可加入的游戏，创建新游戏开始对战吧！</Text>
                </div>
              ) : (
                <Row gutter={16}>
                  {availableGames.map(game => (
                    <Col span={8} key={game.id} style={{ marginBottom: '16px' }}>
                      <GameCard
                        onClick={() => handleJoinGame(game.id)}
                        hoverable
                      >
                        <div>
                          <Title level={4}>游戏 #{game.id}</Title>
                          <Space direction="vertical" size="small">
                            <Space>
                              <Tag color="blue">{game.mode}</Tag>
                              <Tag color="green">{game.type}</Tag>
                            </Space>
                            <Space>
                              <Avatar icon={<UserOutlined />} size="small" />
                              <Text>玩家1</Text>
                              {game.player2 ? (
                                <>
                                  <Text>VS</Text>
                                  <Avatar icon={<UserOutlined />} size="small" />
                                  <Text>玩家2</Text>
                                </>
                              ) : (
                                <Tag color="orange">等待加入</Tag>
                              )}
                            </Space>
                          </Space>
                        </div>
                        <Button 
                          type={game.player2 ? "default" : "primary"} 
                          block
                          disabled={game.player2 !== null}
                        >
                          {game.player2 ? "游戏已满" : "加入游戏"}
                        </Button>
                      </GameCard>
                    </Col>
                  ))}
                </Row>
              )}
            </Card>
          </Content>
        </Layout>
      </Layout>

      <CreateGameModal
        visible={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onOk={handleCreateGame}
        loading={loading}
      />
    </HallContainer>
  );
};

export default GameHall;