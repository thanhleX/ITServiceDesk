import axiosClient from './axiosClient';
import {
  ApiResponse,
  ChartData,
  ReportOverview,
  StaffPerformance,
} from '../types';

export const reportApi = {
  getOverview: (params?: { fromDate?: string; toDate?: string }) =>
    axiosClient.get<any, ApiResponse<ReportOverview>>('/reports/overview', { params }),

  getByStatus: (params?: { fromDate?: string; toDate?: string }) =>
    axiosClient.get<any, ApiResponse<ChartData[]>>('/reports/by-status', { params }),

  getByPriority: (params?: { fromDate?: string; toDate?: string }) =>
    axiosClient.get<any, ApiResponse<ChartData[]>>('/reports/by-priority', { params }),

  getByCategory: (params?: { fromDate?: string; toDate?: string }) =>
    axiosClient.get<any, ApiResponse<ChartData[]>>('/reports/by-category', { params }),

  getByStaff: (params?: { fromDate?: string; toDate?: string }) =>
    axiosClient.get<any, ApiResponse<StaffPerformance[]>>('/reports/by-staff', { params }),
};
