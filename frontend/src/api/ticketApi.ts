import axiosClient from './axiosClient';
import {
  ApiResponse,
  PagedResponse,
  Ticket,
  TicketDetail,
  TicketComment,
  TicketAttachment,
  TicketHistoryItem,
} from '../types';

export interface TicketFilterParams {
  keyword?: string;
  status?: string;
  priority?: string;
  categoryId?: number;
  assignedToId?: number;
  createdById?: number;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

export const ticketApi = {
  getTickets: (params?: TicketFilterParams) =>
    axiosClient.get<any, ApiResponse<PagedResponse<Ticket>>>('/tickets', { params }),

  getTicketDetail: (id: number) =>
    axiosClient.get<any, ApiResponse<TicketDetail>>(`/tickets/${id}`),

  createTicket: (data: {
    title: string;
    description: string;
    categoryId: number;
    priority: string;
  }) => axiosClient.post<any, ApiResponse<Ticket>>('/tickets', data),

  assignTicket: (
    id: number,
    data: { assigneeId: number; note?: string; version: number }
  ) => axiosClient.post<any, ApiResponse<Ticket>>(`/tickets/${id}/assign`, data),

  updateStatus: (
    id: number,
    data: { targetStatus: string; resolution?: string; note?: string; version: number }
  ) => axiosClient.patch<any, ApiResponse<Ticket>>(`/tickets/${id}/status`, data),

  reopenTicket: (id: number, data: { reason: string; version: number }) =>
    axiosClient.post<any, ApiResponse<Ticket>>(`/tickets/${id}/reopen`, data),

  // Comments
  getComments: (ticketId: number) =>
    axiosClient.get<any, ApiResponse<TicketComment[]>>(`/tickets/${ticketId}/comments`),

  addComment: (ticketId: number, content: string) =>
    axiosClient.post<any, ApiResponse<TicketComment>>(`/tickets/${ticketId}/comments`, { content }),

  // Attachments
  getAttachments: (ticketId: number) =>
    axiosClient.get<any, ApiResponse<TicketAttachment[]>>(`/tickets/${ticketId}/attachments`),

  uploadAttachment: (ticketId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosClient.post<any, ApiResponse<TicketAttachment>>(
      `/tickets/${ticketId}/attachments`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    );
  },

  deleteAttachment: (attachmentId: number) =>
    axiosClient.delete<any, ApiResponse<void>>(`/attachments/${attachmentId}`),

  // History
  getHistory: (ticketId: number) =>
    axiosClient.get<any, ApiResponse<TicketHistoryItem[]>>(`/tickets/${ticketId}/history`),
};
