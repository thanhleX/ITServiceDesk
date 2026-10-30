import axiosClient from './axiosClient';
import { ApiResponse, Category, Department, PagedResponse, User } from '../types';

export const masterDataApi = {
  // Departments
  getDepartments: () => axiosClient.get<any, ApiResponse<Department[]>>('/departments'),

  // Categories
  getCategories: (all = false) =>
    axiosClient.get<any, ApiResponse<Category[]>>('/categories', { params: { all } }),

  // Users
  getUsers: (params?: { keyword?: string; roleCode?: string; status?: string; page?: number; size?: number }) =>
    axiosClient.get<any, ApiResponse<PagedResponse<User>>>('/users', { params }),

  getActiveStaffs: () => axiosClient.get<any, ApiResponse<User[]>>('/users/staffs'),

  createUser: (data: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    phone?: string;
    departmentId?: number;
    roleCodes: string[];
  }) => axiosClient.post<any, ApiResponse<User>>('/users', data),

  changeUserStatus: (id: number, status: string) =>
    axiosClient.patch<any, ApiResponse<void>>(`/users/${id}/status`, null, { params: { status } }),
};
