import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { message } from 'antd';

const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
  const [client, setClient] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [currentGame, setCurrentGame] = useState(null);
  const subscriptions = useRef(new Map());

  // 初始化WebSocket连接
  useEffect(() => {
    const socket = new SockJS(process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws-stomp');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        console.log('WebSocket Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = (frame) => {
      console.log('WebSocket连接成功:', frame);
      setIsConnected(true);
      message.success('实时连接已建立');
    };

    stompClient.onDisconnect = () => {
      console.log('WebSocket连接断开');
      setIsConnected(false);
      setCurrentGame(null);
      message.warning('实时连接已断开');
    };

    stompClient.onStompError = (frame) => {
      console.error('WebSocket错误:', frame);
      message.error('连接失败，请检查网络');
    };

    stompClient.activate();
    setClient(stompClient);

    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, []);

  // 订阅游戏主题
  const subscribeToGame = (gameId, callbacks) => {
    if (!client || !isConnected) {
      message.warning('WebSocket未连接，无法订阅游戏');
      return;
    }

    // 取消之前的订阅
    if (subscriptions.current.has(gameId)) {
      subscriptions.current.get(gameId).unsubscribe();
    }

    const subscription = client.subscribe(`/topic/game/${gameId}`, (message) => {
      try {
        const gameData = JSON.parse(message.body);
        console.log('收到游戏更新:', gameData);
        setCurrentGame(gameData);
        
        if (callbacks.onGameUpdate) {
          callbacks.onGameUpdate(gameData);
        }
      } catch (error) {
        console.error('解析游戏更新失败:', error);
      }
    });

    subscriptions.current.set(gameId, subscription);
    
    // 加入游戏房间
    if (client && client.connected) {
      client.publish({
        destination: `/app/game/${gameId}/join`,
        body: JSON.stringify({ action: 'join' })
      });
    }
  };

  // 取消订阅游戏
  const unsubscribeFromGame = (gameId) => {
    if (subscriptions.current.has(gameId)) {
      subscriptions.current.get(gameId).unsubscribe();
      subscriptions.current.delete(gameId);
    }
  };

  // 发送游戏动作
  const sendGameAction = (gameId, action, data = {}) => {
    if (!client || !isConnected) {
      message.error('WebSocket未连接，无法发送操作');
      return false;
    }

    const messageBody = {
      gameId,
      action,
      timestamp: Date.now(),
      ...data
    };

    client.publish({
      destination: `/app/game/${gameId}/action`,
      body: JSON.stringify(messageBody)
    });

    return true;
  };

  // 发送移动
  const sendMove = (gameId, userId, position) => {
    return sendGameAction(gameId, 'move', { userId, position });
  };

  // 发送技能使用
  const sendUseSkill = (gameId, userId, skillId) => {
    return sendGameAction(gameId, 'useSkill', { userId, skillId });
  };

  // 发送投降
  const surrender = (gameId, userId) => {
    return sendGameAction(gameId, 'surrender', { userId });
  };

  // 开始游戏
  const startGame = (gameId, userId) => {
    return sendGameAction(gameId, 'startGame', { userId });
  };

  const value = {
    client,
    isConnected,
    currentGame,
    subscribeToGame,
    unsubscribeFromGame,
    sendMove,
    sendUseSkill,
    surrender,
    startGame
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};
