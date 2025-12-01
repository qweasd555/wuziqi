import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { message } from 'antd';
import { userAPI } from '../services/api';

// 初始状态
const initialState = {
  user: null,
  isLoading: false,
  isAuthenticated: false
};

// Action类型
const authActions = {
  LOGIN_START: 'LOGIN_START',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  LOGIN_FAILURE: 'LOGIN_FAILURE',
  LOGOUT: 'LOGOUT',
  UPDATE_USER: 'UPDATE_USER'
};

// Reducer
const authReducer = (state, action) => {
  switch (action.type) {
    case authActions.LOGIN_START:
      return { ...state, isLoading: true };
    case authActions.LOGIN_SUCCESS:
      return {
        ...state,
        user: action.payload,
        isAuthenticated: true,
        isLoading: false
      };
    case authActions.LOGIN_FAILURE:
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false
      };
    case authActions.LOGOUT:
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false
      };
    case authActions.UPDATE_USER:
      return {
        ...state,
        user: { ...state.user, ...action.payload }
      };
    default:
      return state;
  }
};

// Context
const AuthContext = createContext();

// Provider组件
export const AuthProvider = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // 检查本地存储的用户信息
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        dispatch({ type: authActions.LOGIN_SUCCESS, payload: user });
      } catch (error) {
        console.error('Failed to parse saved user:', error);
        localStorage.removeItem('user');
      }
    }
  }, []);

  // 登录函数
  const login = async (openId, nickname) => {
    dispatch({ type: authActions.LOGIN_START });
    try {
      const user = await userAPI.login(openId, nickname);
      localStorage.setItem('user', JSON.stringify(user));
      dispatch({ type: authActions.LOGIN_SUCCESS, payload: user });
      message.success('登录成功！');
      return user;
    } catch (error) {
      dispatch({ type: authActions.LOGIN_FAILURE });
      message.error('登录失败：' + (error.message || '未知错误'));
      throw error;
    }
  };

  // 登出函数
  const logout = () => {
    localStorage.removeItem('user');
    dispatch({ type: authActions.LOGOUT });
    message.info('已退出登录');
  };

  // 更新用户信息
  const updateUser = (userData) => {
    const updatedUser = { ...state.user, ...userData };
    localStorage.setItem('user', JSON.stringify(updatedUser));
    dispatch({ type: authActions.UPDATE_USER, payload: userData });
  };

  const value = {
    ...state,
    login,
    logout,
    updateUser
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};