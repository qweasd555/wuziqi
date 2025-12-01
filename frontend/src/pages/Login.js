import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, message, Space } from 'antd';
import { UserOutlined, LoginOutlined } from '@ant-design/icons';
import styled from 'styled-components';
import { useAuth } from '../contexts/AuthContext';

const { Title, Text } = Typography;

const LoginContainer = styled.div`
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
`;

const LoginCard = styled(Card)`
  width: 100%;
  max-width: 400px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
`;

const LoginTitle = styled(Title)`
  text-align: center;
  margin-bottom: 32px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
`;

const DemoButtons = styled.div`
  margin-top: 24px;
  text-align: center;
`;

const Login = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  // 处理登录提交
  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      // 使用时间戳作为模拟的openId
      const openId = `demo_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      const user = await login(openId, values.nickname);
      
      if (user) {
        navigate('/hall');
      }
    } catch (error) {
      console.error('Login error:', error);
    } finally {
      setLoading(false);
    }
  };

  // 快速登录函数
  const quickLogin = async (nickname) => {
    setLoading(true);
    try {
      const openId = `demo_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      const user = await login(openId, nickname);
      
      if (user) {
        navigate('/hall');
      }
    } catch (error) {
      console.error('Quick login error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <LoginContainer>
      <LoginCard>
        <LoginTitle level={2}>五子棋对战</LoginTitle>
        <Text type="secondary" style={{ display: 'block', textAlign: 'center', marginBottom: '24px' }}>
          支持技能系统和实时对战的五子棋游戏
        </Text>

        <Form
          form={form}
          name="login"
          onFinish={handleSubmit}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="nickname"
            label="玩家昵称"
            rules={[
              { required: true, message: '请输入您的昵称！' },
              { min: 2, max: 20, message: '昵称长度应在2-20个字符之间！' },
              { pattern: /^[a-zA-Z0-9\u4e00-\u9fa5_]+$/, message: '昵称只能包含字母、数字、中文和下划线！' }
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="请输入您的游戏昵称"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              icon={<LoginOutlined />}
              block
              style={{
                height: '48px',
                fontSize: '16px',
                fontWeight: 'bold'
              }}
            >
              进入游戏
            </Button>
          </Form.Item>
        </Form>

        <DemoButtons>
          <Text type="secondary" style={{ display: 'block', marginBottom: '12px' }}>
            快速体验：
          </Text>
          <Space wrap>
            <Button 
              size="small" 
              onClick={() => quickLogin('玩家一号')}
              loading={loading}
            >
              玩家一号
            </Button>
            <Button 
              size="small" 
              onClick={() => quickLogin('游戏达人')}
              loading={loading}
            >
              游戏达人
            </Button>
            <Button 
              size="small" 
              onClick={() => quickLogin('五子棋高手')}
              loading={loading}
            >
              五子棋高手
            </Button>
          </Space>
        </DemoButtons>

        <div style={{ textAlign: 'center', marginTop: '24px' }}>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            这是演示版本，使用模拟登录
          </Text>
        </div>
      </LoginCard>
    </LoginContainer>
  );
};

export default Login;