import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, LoginResponse } from '../types';
import { authApi } from '../api/authApi';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isManager: boolean;
  isStaff: boolean;
  isEmployee: boolean;
  login: (data: LoginResponse) => void;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    if (token) {
      authApi
        .getMe()
        .then((res) => {
          setUser(res.data);
          localStorage.setItem('user', JSON.stringify(res.data));
        })
        .catch(() => {
          logout();
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = (data: LoginResponse) => {
    setToken(data.accessToken);
    localStorage.setItem('token', data.accessToken);
    const userObj: User = {
      id: data.userId,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
      departmentId: data.departmentId,
      departmentName: data.departmentName,
      status: 'ACTIVE',
      roles: data.roles,
      permissions: data.permissions,
    };
    setUser(userObj);
    localStorage.setItem('user', JSON.stringify(userObj));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const isManager = Boolean(user?.roles.some((r) => r === 'ROLE_IT_MANAGER'));
  const isStaff = Boolean(user?.roles.some((r) => r === 'ROLE_IT_STAFF'));
  const isEmployee = Boolean(user?.roles.some((r) => r === 'ROLE_EMPLOYEE'));

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: Boolean(token && user),
        isManager,
        isStaff,
        isEmployee,
        login,
        logout,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
