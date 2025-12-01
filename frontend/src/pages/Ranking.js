import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Layout, 
  Card, 
  Typography, 
  Table, 
  Avatar, 
  Tag, 
  Button,
  Space,
  Statistic,
  Row,
  Col,
  Select,
  Input,
  message,
  Badge
} from 'antd';
import {
  TrophyOutlined,
  UserOutlined,
  FireOutlined,
  SearchOutlined,
  ArrowLeftOutlined,
  CrownOutlined,
  StarOutlined
} from '@ant-design/icons';
import styled from 'styled-components';
import { useAuth } from '../contexts/AuthContext';
import { userAPI } from '../services/api';

const { Header, Content } = Layout;
const { Title, Text } = Typography;
const { Option } = Select;
const { Search } = Input;

const RankingContainer = styled(Layout)`
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
`;

const RankingHeader = styled(Header)`
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
`;

const RankingContent = styled(Content)`
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
`;

const TopPlayersCard = styled(Card)`
  margin-bottom: 24px;
  
  .ant-card-head {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
    
    .ant-card-head-title {
      color: white;
    }
  }
  
  .top-players-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 16px;
  }
`;

const PlayerRankCard = styled(Card)`
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  &.rank-1 {
    border: 2px solid #ffd700;
    background: linear-gradient(135deg, #fff9e6 0%, #fffbf0 100%);
  }
  
  &.rank-2 {
    border: 2px solid #c0c0c0;
    background: linear-gradient(135deg, #f8f8f8 0%, #fafafa 100%);
  }
  
  &.rank-3 {
    border: 2px solid #cd7f32;
    background: linear-gradient(135deg, #fff8f0 0%, #fff5e6 100%);
  }
  
  .rank-number {
    font-size: 36px;
    font-weight: bold;
    margin-bottom: 16px;
  }
  
  &.rank-1 .rank-number { color: #ffd700; }
  &.rank-2 .rank-number { color: #c0c0c0; }
  &.rank-3 .rank-number { color: #cd7f32; }
  
  .player-avatar {
    width: 80px;
    height: 80px;
    border: 3px solid #667eea;
    margin-bottom: 12px;
  }
`;

const RankingTableCard = styled(Card)`
  .ant-table-tbody > tr:hover > td {
    background: #f0f8ff !important;
  }
  
  .rank-cell {
    font-weight: bold;
    font-size: 16px;
  }
  
  .rank-1 .rank-cell { color: #ffd700; }
  .rank-2 .rank-cell { color: #c0c0c0; }
  .rank-3 .rank-cell { color: #cd7f32; }
`;

const Ranking = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [rankingData, setRankingData] = useState([]);
  const [topPlayers, setTopPlayers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [rankingType, setRankingType] = useState('score');
  const [searchText, setSearchText] = useState('');
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0
  });

  // 加载排行榜数据
  const loadRankingData = async (page = 1, size = 20, type = 'score', search = '') => {
    setLoading(true);
    try {
      // 这里应该调用实际的API
      // const data = await userAPI.getRanking(page, size);
      
      // 暂时使用模拟数据
      const mockData = generateMockRankingData();
      
      // 搜索过滤
      let filteredData = mockData;
      if (search) {
        filteredData = mockData.filter(item => 
          item.nickname.toLowerCase().includes(search.toLowerCase())
        );
      }
      
      // 排序
      const sortedData = [...filteredData].sort((a, b) => {
        switch (type) {
          case 'score':
            return b.score - a.score;
          case 'wins':
            return b.winCount - a.winCount;
          case 'winRate':
            return b.winRate - a.winRate;
          case 'totalGames':
            return b.totalCount - a.totalCount;
          default:
            return b.score - a.score;
        }
      });
      
      // 设置分页数据
      const startIndex = (page - 1) * size;
      const paginatedData = sortedData.slice(startIndex, startIndex + size);
      
      setRankingData(paginatedData);
      setPagination({
        current: page,
        pageSize: size,
        total: sortedData.length
      });
      
      // 设置前三名
      setTopPlayers(sortedData.slice(0, 3));
      
    } catch (error) {
      console.error('Failed to load ranking data:', error);
      message.error('加载排行榜失败');
    } finally {
      setLoading(false);
    }
  };

  // 生成模拟排行榜数据
  const generateMockRankingData = () => {
    const names = [
      '五子棋大师', '棋圣', '游戏达人', '玩家一号', '棋王', '棋仙', '棋神',
      '高手', '新手村村长', '快乐玩家', '竞技王者', '休闲玩家', '策略大师',
      '战术专家', '业余选手', '职业选手', '游戏爱好者', '棋坛新秀', '老玩家'
    ];
    
    return names.map((name, index) => ({
      id: index + 1,
      nickname: name,
      avatarUrl: null,
      score: Math.floor(Math.random() * 5000) + 1000,
      winCount: Math.floor(Math.random() * 100) + 10,
      totalCount: Math.floor(Math.random() * 200) + 20,
      winRate: Math.floor(Math.random() * 80) + 20,
      rankLevel: Math.floor(Math.random() * 5) + 1
    }));
  };

  useEffect(() => {
    loadRankingData(1, 20, rankingType, searchText);
  }, [rankingType]);

  // 处理表格分页变化
  const handleTableChange = (paginationConfig) => {
    loadRankingData(
      paginationConfig.current,
      paginationConfig.pageSize,
      rankingType,
      searchText
    );
  };

  // 处理搜索
  const handleSearch = (value) => {
    setSearchText(value);
    loadRankingData(1, pagination.pageSize, rankingType, value);
  };

  // 获取排名标签颜色
  const getRankColor = (rank) => {
    switch (rank) {
      case 1: return '#ffd700';
      case 2: return '#c0c0c0';
      case 3: return '#cd7f32';
      default: return undefined;
    }
  };

  // 获取段位信息
  const getRankInfo = (rankLevel) => {
    const ranks = [
      { level: 1, name: '新手', color: 'green' },
      { level: 2, name: '入门', color: 'blue' },
      { level: 3, name: '进阶', color: 'purple' },
      { level: 4, name: '高手', color: 'orange' },
      { level: 5, name: '大师', color: 'red' }
    ];
    
    return ranks.find(r => r.level === rankLevel) || ranks[0];
  };

  // 排行榜表格列
  const columns = [
    {
      title: '排名',
      key: 'rank',
      width: 80,
      render: (_, record, index) => {
        const rank = (pagination.current - 1) * pagination.pageSize + index + 1;
        const rankClass = rank <= 3 ? `rank-${rank}` : '';
        
        return (
          <div className={`rank-cell ${rankClass}`}>
            {rank <= 3 ? <TrophyOutlined /> : null}
            {' '}{rank}
          </div>
        );
      }
    },
    {
      title: '玩家',
      key: 'player',
      render: (_, record) => (
        <Space>
          <Avatar 
            size="large" 
            icon={<UserOutlined />}
            src={record.avatarUrl}
          />
          <div>
            <div>
              <Text strong>{record.nickname}</Text>
              {user && record.id === user.id && (
                <Tag color="blue" size="small" style={{ marginLeft: 8 }}>您</Tag>
              )}
            </div>
            <Tag color={getRankInfo(record.rankLevel).color} size="small">
              {getRankInfo(record.rankLevel).name}
            </Tag>
          </div>
        </Space>
      )
    },
    {
      title: '总积分',
      dataIndex: 'score',
      key: 'score',
      sorter: (a, b) => a.score - b.score,
      render: (score) => (
        <Statistic value={score} valueStyle={{ fontSize: '16px' }} />
      )
    },
    {
      title: '胜场',
      dataIndex: 'winCount',
      key: 'winCount',
      sorter: (a, b) => a.winCount - b.winCount,
      render: (wins) => (
        <Text strong style={{ color: '#52c41a' }}>{wins}</Text>
      )
    },
    {
      title: '总场次',
      dataIndex: 'totalCount',
      key: 'totalCount',
      sorter: (a, b) => a.totalCount - b.totalCount,
      render: (total) => <Text>{total}</Text>
    },
    {
      title: '胜率',
      dataIndex: 'winRate',
      key: 'winRate',
      sorter: (a, b) => a.winRate - b.winRate,
      render: (rate) => (
        <Badge 
          count={`${rate}%`} 
          showZero 
          style={{ 
            backgroundColor: rate >= 70 ? '#52c41a' : rate >= 50 ? '#faad14' : '#f5222d' 
          }}
        />
      )
    }
  ];

  return (
    <RankingContainer>
      <RankingHeader>
        <Title level={3} style={{ color: 'white', margin: 0 }}>
          游戏排行榜
        </Title>
        <Button 
          type="text" 
          icon={<ArrowLeftOutlined />} 
          onClick={() => navigate('/hall')}
          style={{ color: 'white' }}
        >
          返回大厅
        </Button>
      </RankingHeader>

      <RankingContent>
        {/* 前三名展示 */}
        <TopPlayersCard 
          title={
            <Space>
              <CrownOutlined />
              榜单前三
            </Space>
          }
        >
          <div className="top-players-grid">
            {topPlayers.map((player, index) => {
              const rank = index + 1;
              return (
                <PlayerRankCard 
                  key={player.id}
                  className={`rank-${rank}`}
                  hoverable
                >
                  <div className="rank-number">
                    {rank === 1 ? <TrophyOutlined /> : rank === 2 ? <StarOutlined /> : <FireOutlined />}
                  </div>
                  <Avatar 
                    size={80} 
                    icon={<UserOutlined />}
                    src={player.avatarUrl}
                    className="player-avatar"
                  />
                  <Title level={4}>{player.nickname}</Title>
                  <Space direction="vertical" size="small">
                    <Statistic title="总积分" value={player.score} />
                    <Space>
                      <Text type="secondary">胜率: {player.winRate}%</Text>
                      <Tag color={getRankInfo(player.rankLevel).color}>
                        {getRankInfo(player.rankLevel).name}
                      </Tag>
                    </Space>
                  </Space>
                </PlayerRankCard>
              );
            })}
          </div>
        </TopPlayersCard>

        {/* 排行榜控制栏 */}
        <Card style={{ marginBottom: '16px' }}>
          <Row gutter={16} align="middle">
            <Col flex="auto">
              <Space size="large">
                <Text strong>排行榜类型：</Text>
                <Select 
                  value={rankingType} 
                  onChange={setRankingType}
                  style={{ width: 120 }}
                >
                  <Option value="score">总积分</Option>
                  <Option value="wins">胜场</Option>
                  <Option value="winRate">胜率</Option>
                  <Option value="totalGames">总场次</Option>
                </Select>
              </Space>
            </Col>
            <Col>
              <Search
                placeholder="搜索玩家"
                allowClear
                enterButton={<SearchOutlined />}
                style={{ width: 250 }}
                onSearch={handleSearch}
                onChange={(e) => !e.target.value && setSearchText('')}
              />
            </Col>
          </Row>
        </Card>

        {/* 排行榜表格 */}
        <RankingTableCard title="完整排行榜">
          <Table
            columns={columns}
            dataSource={rankingData}
            rowKey="id"
            loading={loading}
            pagination={{
              ...pagination,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total, range) => 
                `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`,
              onChange: (page, pageSize) => handleTableChange({ current: page, pageSize })
            }}
            rowClassName={(record, index) => {
              const rank = (pagination.current - 1) * pagination.pageSize + index + 1;
              return rank <= 3 ? `rank-${rank}` : '';
            }}
            scroll={{ x: 800 }}
          />
        </RankingTableCard>
      </RankingContent>
    </RankingContainer>
  );
};

export default Ranking;