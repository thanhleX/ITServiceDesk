import axiosClient from './axiosClient';
import { ApiResponse, LoginResponse, User } from '../types';

export const authApi = {
  login: (data: { usernameOrEmail: string; password: string }) =>
    axiosClient.post<any, ApiResponse<LoginResponse>>('/auth/login', data),

  getMe: () => axiosClient.get<any, ApiResponse<User>>('/auth/me'),

  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    axiosClient.put<any, ApiResponse<void>>('/auth/change-password', data),

  logout: () => axiosClient.post<any, ApiResponse<void>>('/auth/logout'),
};
