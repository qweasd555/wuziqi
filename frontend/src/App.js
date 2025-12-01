import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthProvider } from './contexts/AuthContext';
import { WebSocketProvider } from './contexts/WebSocketContext';
import Login from './pages/Login';
import GameHall from './pages/GameHall';
import GameBoard from './pages/GameBoard';
import Profile from './pages/Profile';
import Ranking from './pages/Ranking';
import './App.css';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <AuthProvider>
        <WebSocketProvider>
          <Router>
            <div className="App">
              <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/hall" element={<GameHall />} />
                <Route path="/game/:gameId" element={<GameBoard />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/ranking" element={<Ranking />} />
                <Route path="/" element={<Navigate to="/login" replace />} />
              </Routes>
            </div>
          </Router>
        </WebSocketProvider>
      </AuthProvider>
    </ConfigProvider>
  );
}

export default App;