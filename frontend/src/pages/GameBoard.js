import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { message } from 'antd';
import GameBoard from '../components/GameBoard';
import { useAuth } from '../contexts/AuthContext';

const GameBoardPage = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (!user) {
      message.error('请先登录');
      navigate('/login');
      return;
    }

    if (!gameId) {
      message.error('游戏ID无效');
      navigate('/hall');
      return;
    }
  }, [user, gameId, navigate]);

  return <GameBoard gameId={gameId} />;
};

export default GameBoardPage;