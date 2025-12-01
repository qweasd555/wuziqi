import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Space, Tooltip, Modal, message } from 'antd';
import { 
  ThunderboltOutlined, 
  SafetyCertificateOutlined, 
  RocketOutlined,
  FireOutlined,
  EyeOutlined,
  ToolOutlined
} from '@ant-design/icons';
import styled from 'styled-components';
import { skillAPI } from '../services/api';
import { SkillType } from '../utils/constants';

const { Title, Text } = Typography;

const SkillPanelContainer = styled(Card)`
  .ant-card-head {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
    color: white;
    border-radius: 8px 8px 0 0;
  }
  
  .ant-card-head-title {
    color: white;
  }
`;

const SkillsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
`;

const SkillButton = styled(Button)`
  height: auto;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
  overflow: hidden;
  
  &:disabled {
    opacity: 0.6;
  }
  
  &.attack {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
    border: none;
    color: white;
    
    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #ff5252 0%, #d84315 100%);
    }
  }
  
  &.defense {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border: none;
    color: white;
    
    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #29b6f6 0%, #0288d1 100%);
    }
  }
  
  &.utility {
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
    border: none;
    color: #333;
    
    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #81c784 0%, #4fc3f7 100%);
    }
  }
  
  .skill-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  .skill-name {
    font-weight: bold;
    font-size: 12px;
    margin-bottom: 4px;
  }
  
  .skill-desc {
    font-size: 10px;
    opacity: 0.9;
    line-height: 1.2;
  }
  
  .cooldown-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    border-radius: 6px;
  }
  
  .cooldown-time {
    font-size: 24px;
    margin-bottom: 4px;
  }
  
  .cooldown-text {
    font-size: 10px;
  }
`;

// 技能图标映射
const skillIcons = {
  'double_attack': <FireOutlined />,
  'shield': <SafetyCertificateOutlined />,
  'teleport': <RocketOutlined />,
  'revelation': <EyeOutlined />,
  'bomb': <ThunderboltOutlined />,
  'swap': <ToolOutlined />
};

// 模拟技能数据（实际项目中应该从后端获取）
const mockSkills = [
  {
    id: 1,
    name: '双重打击',
    description: '本回合可以连续下两颗棋子',
    type: SkillType.ATTACK,
    cooldown: 30,
    cost: 50,
    icon: 'double_attack'
  },
  {
    id: 2,
    name: '护盾防御',
    description: '阻止对手下一次技能使用',
    type: SkillType.DEFENSE,
    cooldown: 45,
    cost: 30,
    icon: 'shield'
  },
  {
    id: 3,
    name: '瞬间移动',
    description: '将一颗已下的棋子移动到空位',
    type: SkillType.UTILITY,
    cooldown: 60,
    cost: 40,
    icon: 'teleport'
  },
  {
    id: 4,
    name: '洞悉全局',
    description: '显示对手下一步的最佳落子位置',
    type: SkillType.UTILITY,
    cooldown: 25,
    cost: 20,
    icon: 'revelation'
  },
  {
    id: 5,
    name: '炸弹',
    description: '清除3x3范围内的所有棋子',
    type: SkillType.ATTACK,
    cooldown: 90,
    cost: 80,
    icon: 'bomb'
  },
  {
    id: 6,
    name: '棋子交换',
    description: '将对手的一颗棋子变为己方棋子',
    type: SkillType.UTILITY,
    cooldown: 75,
    cost: 60,
    icon: 'swap'
  }
];

const SkillPanel = ({ userId, onUseSkill, disabled }) => {
  const [skills, setSkills] = useState([]);
  const [skillCooldowns, setSkillCooldowns] = useState({});
  const [loading, setLoading] = useState(false);
  const [selectedSkill, setSelectedSkill] = useState(null);

  // 加载用户技能
  useEffect(() => {
    loadUserSkills();
  }, [userId]);

  // 加载技能数据
  const loadUserSkills = async () => {
    try {
      // 这里应该调用API获取用户的技能列表
      // const userSkills = await skillAPI.getUserSkills(userId);
      
      // 暂时使用模拟数据
      setSkills(mockSkills);
    } catch (error) {
      console.error('Failed to load user skills:', error);
      message.error('加载技能失败');
    }
  };

  // 处理技能使用
  const handleSkillUse = async (skill) => {
    if (disabled) {
      message.warning('当前无法使用技能');
      return;
    }

    const cooldown = skillCooldowns[skill.id];
    if (cooldown && cooldown > 0) {
      message.warning(`技能冷却中，还需等待 ${cooldown} 秒`);
      return;
    }

    setSelectedSkill(skill);
    
    Modal.confirm({
      title: '使用技能',
      content: (
        <div>
          <p><strong>{skill.name}</strong></p>
          <p>{skill.description}</p>
          <p>消耗积分：{skill.cost}</p>
          <p>冷却时间：{skill.cooldown} 秒</p>
        </div>
      ),
      okText: '使用',
      cancelText: '取消',
      onOk: () => executeSkill(skill)
    });
  };

  // 执行技能
  const executeSkill = async (skill) => {
    setLoading(true);
    try {
      // 调用父组件的技能使用函数
      if (onUseSkill) {
        onUseSkill(skill.id);
      }
      
      // 设置冷却时间
      setSkillCooldowns(prev => ({
        ...prev,
        [skill.id]: skill.cooldown
      }));
      
      // 开始冷却倒计时
      startCooldown(skill.id, skill.cooldown);
      
      message.success(`成功使用技能：${skill.name}`);
    } catch (error) {
      console.error('Failed to use skill:', error);
      message.error('技能使用失败');
    } finally {
      setLoading(false);
      setSelectedSkill(null);
    }
  };

  // 技能冷却倒计时
  const startCooldown = (skillId, cooldownTime) => {
    let remainingTime = cooldownTime;
    
    const timer = setInterval(() => {
      remainingTime--;
      
      setSkillCooldowns(prev => ({
        ...prev,
        [skillId]: remainingTime > 0 ? remainingTime : 0
      }));
      
      if (remainingTime <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  };

  // 获取技能样式类名
  const getSkillClassName = (skill) => {
    const baseClass = skill.type.toLowerCase();
    const cooldown = skillCooldowns[skill.id];
    return `${baseClass} ${cooldown > 0 ? 'cooldown' : ''}`;
  };

  // 获取技能图标
  const getSkillIcon = (skill) => {
    return skillIcons[skill.icon] || <ThunderboltOutlined />;
  };

  return (
    <SkillPanelContainer 
      title="技能面板" 
      size="small"
    >
      <SkillsGrid>
        {skills.map(skill => {
          const cooldown = skillCooldowns[skill.id];
          const isOnCooldown = cooldown > 0;
          
          return (
            <Tooltip 
              key={skill.id}
              title={
                <div>
                  <div><strong>{skill.name}</strong></div>
                  <div>{skill.description}</div>
                  <div>消耗：{skill.cost} 积分</div>
                  <div>冷却：{skill.cooldown} 秒</div>
                  {isOnCooldown && <div style={{ color: 'red' }}>冷却中：{cooldown} 秒</div>}
                </div>
              }
            >
              <SkillButton
                className={getSkillClassName(skill)}
                onClick={() => handleSkillUse(skill)}
                disabled={disabled || isOnCooldown}
                loading={loading && selectedSkill?.id === skill.id}
              >
                <div className="skill-icon">
                  {getSkillIcon(skill)}
                </div>
                <div className="skill-name">{skill.name}</div>
                <div className="skill-desc">{skill.description}</div>
                
                {isOnCooldown && (
                  <div className="cooldown-overlay">
                    <div className="cooldown-time">{cooldown}</div>
                    <div className="cooldown-text">冷却中</div>
                  </div>
                )}
              </SkillButton>
            </Tooltip>
          );
        })}
      </SkillsGrid>
      
      <div style={{ marginTop: '12px', textAlign: 'center' }}>
        <Text type="secondary" style={{ fontSize: '12px' }}>
          点击技能卡片使用技能，技能使用后有冷却时间
        </Text>
      </div>
    </SkillPanelContainer>
  );
};

export default SkillPanel;