import React, { createContext, useContext, useState, useEffect } from 'react';
import * as authApi from '../api/authApi';
import { useToast } from './ToastContext';

const AuthContext = createContext();

/**
 * Hook to use auth context
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

/**
 * AuthProvider component
 * Manages authentication state and provides auth operations
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();
  
  /**
   * Check if user is authenticated on mount
   */
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        try {
          const isValid = await authApi.introspect();
          if (isValid) {
            // Token is valid, set user as authenticated
            // You could fetch user profile here if needed
            setUser({ token });
          } else {
            // Token is invalid, clear it
            localStorage.removeItem('accessToken');
            setUser(null);
          }
        } catch (error) {
          // Token validation failed, clear it
          localStorage.removeItem('accessToken');
          setUser(null);
        }
      }
      setLoading(false);
    };
    
    checkAuth();
  }, []);
  
  /**
   * Register new user
   * @param {Object} userData - { email, fullName, password }
   * @returns {Promise<Object>} - Registration response with OTP info
   */
  const register = async (userData) => {
    try {
      const response = await authApi.register(userData);
      return response;
    } catch (error) {
      showToast('error', error.message || 'Đăng ký thất bại');
      throw error;
    }
  };
  
  /**
   * Verify OTP after registration
   * @param {string} email - User email
   * @param {string} otpCode - 6-digit OTP code
   */
  const verifyOtp = async (email, otpCode) => {
    try {
      const response = await authApi.verifyOtp(email, otpCode);
      
      // Store access token
      if (response.accessToken) {
        localStorage.setItem('accessToken', response.accessToken);
        setUser({ token: response.accessToken });
        showToast('success', 'Đăng ký thành công!');
      }
      
      return response;
    } catch (error) {
      showToast('error', error.message || 'Xác thực OTP thất bại');
      throw error;
    }
  };
  
  /**
   * Login user
   * @param {string} email - User email
   * @param {string} password - User password
   */
  const login = async (email, password) => {
    try {
      const response = await authApi.login(email, password);
      
      // Store access token
      if (response.accessToken) {
        localStorage.setItem('accessToken', response.accessToken);
        setUser({ token: response.accessToken });
        showToast('success', 'Đăng nhập thành công!');
      }
      
      return response;
    } catch (error) {
      showToast('error', error.message || 'Đăng nhập thất bại');
      throw error;
    }
  };
  
  /**
   * Logout user
   */
  const logout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('Logout error:', error);
      // Continue with logout even if API call fails
    } finally {
      localStorage.removeItem('accessToken');
      setUser(null);
      showToast('success', 'Đã đăng xuất');
    }
  };
  
  /**
   * Refresh access token
   */
  const refreshToken = async () => {
    try {
      const response = await authApi.refresh();
      if (response.accessToken) {
        localStorage.setItem('accessToken', response.accessToken);
        setUser({ token: response.accessToken });
        return response.accessToken;
      }
    } catch (error) {
      // Refresh failed, logout user
      localStorage.removeItem('accessToken');
      setUser(null);
      throw error;
    }
  };
  
  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    register,
    verifyOtp,
    login,
    logout,
    refreshToken,
  };
  
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
