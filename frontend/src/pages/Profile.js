import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Layout, 
  Card, 
  Typography, 
  Row, 
  Col, 
  Avatar, 
  Button, 
  Descriptions,
  Table,
  Tag,
  Progress,
  Statistic,
  Space,
  message
} from 'antd';
import {
  UserOutlined,
  TrophyOutlined,
  FireOutlined,
  CrownOutlined,
  HistoryOutlined,
  ArrowLeftOutlined
} from '@ant-design/icons';
import styled from 'styled-components';
import { useAuth } from '../contexts/AuthContext';
import { userAPI, gameAPI } from '../services/api';

const { Header, Content } = Layout;
const { Title, Text } = Typography;

const ProfileContainer = styled(Layout)`
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
`;

const ProfileHeader = styled(Header)`
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
`;

const ProfileContent = styled(Content)`
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
`;

const UserCard = styled(Card)`
  text-align: center;
  border-radius: 16px;
  overflow: hidden;
  
  .ant-card-cover {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 40px 20px;
  }
  
  .user-avatar {
    width: 120px;
    height: 120px;
    border: 4px solid white;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  }
`;

const StatsCard = styled(Card)`
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .ant-statistic-content {
    color: #667eea;
  }
`;

const Profile = () => {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [userStats, setUserStats] = useState(null);
  const [gameHistory, setGameHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);

  // 加载用户统计
  const loadUserStats = async () => {
    if (!user) return;
    
    setLoading(true);
    try {
      const stats = await userAPI.getUserStats(user.id);
      setUserStats(stats);
    } catch (error) {
      console.error('Failed to load user stats:', error);
      message.error('加载用户统计失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载游戏历史
  const loadGameHistory = async () => {
    if (!user) return;
    
    setHistoryLoading(true);
    try {
      // 这里应该调用实际的API
      // const history = await gameAPI.getGameHistory(user.id);
      
      // 暂时使用模拟数据
      const mockHistory = [
        {
          id: 1,
          opponent: '玩家二号',
          result: 'WIN',
          mode: 'REGULAR',
          type: 'ONLINE_PVP',
          duration: '15:32',
          date: '2024-01-20 14:30',
          scoreChange: '+100'
        },
        {
          id: 2,
          opponent: 'AI对手',
          result: 'LOSS',
          mode: 'SKILL',
          type: 'VS_AI',
          duration: '08:45',
          date: '2024-01-20 13:15',
          scoreChange: '-50'
        },
        {
          id: 3,
          opponent: '游戏高手',
          result: 'WIN',
          mode: 'SKILL',
          type: 'ONLINE_PVP',
          duration: '22:18',
          date: '2024-01-20 11:20',
          scoreChange: '+150'
        },
        {
          id: 4,
          opponent: '五子棋大师',
          result: 'DRAW',
          mode: 'REGULAR',
          type: 'ONLINE_PVP',
          duration: '18:55',
          date: '2024-01-20 10:10',
          scoreChange: '+20'
        }
      ];
      
      setGameHistory(mockHistory);
    } catch (error) {
      console.error('Failed to load game history:', error);
      message.error('加载游戏历史失败');
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      loadUserStats();
      loadGameHistory();
    }
  }, [user]);

  // 获取段位信息
  const getRankInfo = (rankLevel) => {
    const ranks = [
      { level: 1, name: '新手', color: '#52c41a', progress: 20 },
      { level: 2, name: '入门', color: '#1890ff', progress: 40 },
      { level: 3, name: '进阶', color: '#722ed1', progress: 60 },
      { level: 4, name: '高手', color: '#fa8c16', progress: 80 },
      { level: 5, name: '大师', color: '#eb2f96', progress: 100 }
    ];
    
    const rank = ranks.find(r => r.level === rankLevel) || ranks[0];
    return rank;
  };

  // 游戏历史表格列
  const historyColumns = [
    {
      title: '对手',
      dataIndex: 'opponent',
      key: 'opponent',
      render: (text) => (
        <Space>
          <Avatar icon={<UserOutlined />} size="small" />
          {text}
        </Space>
      )
    },
    {
      title: '结果',
      dataIndex: 'result',
      key: 'result',
      render: (result) => {
        const color = result === 'WIN' ? 'green' : result === 'LOSS' ? 'red' : 'orange';
        const text = result === 'WIN' ? '胜利' : result === 'LOSS' ? '失败' : '平局';
        return <Tag color={color}>{text}</Tag>;
      }
    },
    {
      title: '模式',
      dataIndex: 'mode',
      key: 'mode',
      render: (mode) => {
        const text = mode === 'REGULAR' ? '常规' : '技能';
        return <Tag>{text}</Tag>;
      }
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type) => {
        const text = type === 'VS_AI' ? '人机' : '联机';
        return <Tag color="blue">{text}</Tag>;
      }
    },
    {
      title: '时长',
      dataIndex: 'duration',
      key: 'duration'
    },
    {
      title: '积分变化',
      dataIndex: 'scoreChange',
      key: 'scoreChange',
      render: (change) => {
        const isPositive = change.startsWith('+');
        return (
          <Text style={{ color: isPositive ? '#52c41a' : '#f5222d' }}>
            {change}
          </Text>
        );
      }
    },
    {
      title: '时间',
      dataIndex: 'date',
      key: 'date'
    }
  ];

  if (!user) {
    return <div>请先登录</div>;
  }

  const rankInfo = getRankInfo(userStats?.rankLevel || 1);

  return (
    <ProfileContainer>
      <ProfileHeader>
        <Title level={3} style={{ color: 'white', margin: 0 }}>
          个人中心
        </Title>
        <Button 
          type="text" 
          icon={<ArrowLeftOutlined />} 
          onClick={() => navigate('/hall')}
          style={{ color: 'white' }}
        >
          返回大厅
        </Button>
      </ProfileHeader>

      <ProfileContent>
        <Row gutter={[24, 24]}>
          {/* 用户信息卡片 */}
          <Col span={8}>
            <UserCard>
              <div className="ant-card-cover">
                <Avatar 
                  size={120} 
                  icon={<UserOutlined />}
                  className="user-avatar"
                  src={user?.avatarUrl}
                />
                <Title level={3} style={{ color: 'white', marginTop: '16px' }}>
                  {user?.nickname}
                </Title>
                <Space style={{ marginTop: '8px' }}>
                  <Tag color={rankInfo.color} icon={<CrownOutlined />}>
                    {rankInfo.name}
                  </Tag>
                  <Tag icon={<TrophyOutlined />}>
                    Lv.{userStats?.rankLevel || 1}
                  </Tag>
                </Space>
              </div>
              
              <Descriptions column={1} style={{ padding: '20px' }}>
                <Descriptions.Item label="用户ID">{user?.id}</Descriptions.Item>
                <Descriptions.Item label="注册时间">
                  {user?.createTime ? new Date(user.createTime).toLocaleDateString() : '未知'}
                </Descriptions.Item>
                <Descriptions.Item label="总积分">
                  <Statistic value={userStats?.score || 0} suffix="分" />
                </Descriptions.Item>
              </Descriptions>
            </UserCard>
          </Col>

          {/* 统计数据卡片 */}
          <Col span={16}>
            <Row gutter={[16, 16]}>
              <Col span={6}>
                <StatsCard>
                  <Statistic
                    title="总场次"
                    value={userStats?.totalGames || 0}
                    prefix={<FireOutlined />}
                    valueStyle={{ color: '#3f8600' }}
                  />
                </StatsCard>
              </Col>
              <Col span={6}>
                <StatsCard>
                  <Statistic
                    title="胜利场次"
                    value={userStats?.wins || 0}
                    prefix={<TrophyOutlined />}
                    valueStyle={{ color: '#cf1322' }}
                  />
                </StatsCard>
              </Col>
              <Col span={6}>
                <StatsCard>
                  <Statistic
                    title="胜率"
                    value={userStats?.winRate || 0}
                    suffix="%"
                    precision={1}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </StatsCard>
              </Col>
              <Col span={6}>
                <StatsCard>
                  <Statistic
                    title="段位等级"
                    value={userStats?.rankLevel || 1}
                    prefix={<CrownOutlined />}
                    valueStyle={{ color: '#722ed1' }}
                  />
                </StatsCard>
              </Col>
            </Row>

            {/* 段位进度 */}
            <Card title="段位进度" style={{ marginTop: '16px' }}>
              <Space style={{ width: '100%' }} direction="vertical" size="large">
                <div>
                  <Text strong>当前段位：{rankInfo.name}</Text>
                  <Progress 
                    percent={rankInfo.progress} 
                    strokeColor={rankInfo.color}
                    showInfo={false}
                    style={{ marginTop: '8px' }}
                  />
                </div>
                <div>
                  <Text type="secondary">
                    还需 {Math.max(0, 100 - rankInfo.progress)} 经验升级到下一段位
                  </Text>
                </div>
              </Space>
            </Card>
          </Col>
        </Row>

        {/* 游戏历史 */}
        <Card 
          title={
            <Space>
              <HistoryOutlined />
              游戏历史记录
            </Space>
          } 
          style={{ marginTop: '24px' }}
        >
          <Table
            columns={historyColumns}
            dataSource={gameHistory}
            rowKey="id"
            loading={historyLoading}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条记录`
            }}
            scroll={{ x: 800 }}
          />
        </Card>
      </ProfileContent>
    </ProfileContainer>
  );
};

export default Profile;