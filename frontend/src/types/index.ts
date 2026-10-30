export type TicketStatus =
  | 'OPEN'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'PENDING'
  | 'RESOLVED'
  | 'CLOSED'
  | 'REOPENED';

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  departmentId?: number;
  departmentName?: string;
  status: string;
  roles: string[];
  permissions?: string[];
  createdAt?: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  fullName: string;
  departmentId?: number;
  departmentName?: string;
  roles: string[];
  permissions: string[];
}

export interface Department {
  id: number;
  code: string;
  name: string;
  status: string;
}

export interface Category {
  id: number;
  code: string;
  name: string;
  description?: string;
  status: string;
}

export interface Ticket {
  id: number;
  ticketCode: string;
  title: string;
  description: string;
  categoryId: number;
  categoryName: string;
  createdById: number;
  createdByName: string;
  createdByEmail: string;
  departmentName?: string;
  assignedToId?: number;
  assignedToName?: string;
  assignedToEmail?: string;
  priority: TicketPriority;
  status: TicketStatus;
  resolution?: string;
  dueAt: string;
  resolvedAt?: string;
  closedAt?: string;
  isOverdue: boolean;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface AssignmentHistory {
  id: number;
  ticketId: number;
  assignedById: number;
  assignedByName: string;
  assignedToId: number;
  assignedToName: string;
  note?: string;
  assignedAt: string;
}

export interface TicketHistoryItem {
  id: number;
  ticketId: number;
  performedById: number;
  performedByName: string;
  action: string;
  oldStatus?: string;
  newStatus?: string;
  description: string;
  createdAt: string;
}

export interface TicketDetail {
  ticket: Ticket;
  assignments: AssignmentHistory[];
  histories: TicketHistoryItem[];
  totalComments: number;
  totalAttachments: number;
}

export interface TicketComment {
  id: number;
  ticketId: number;
  userId: number;
  userFullName: string;
  userRole: string;
  content: string;
  createdAt: string;
}

export interface TicketAttachment {
  id: number;
  ticketId: number;
  uploadedById: number;
  uploadedByName: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  createdAt: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

export interface ReportOverview {
  totalTickets: number;
  openTickets: number;
  assignedTickets: number;
  inProgressTickets: number;
  pendingTickets: number;
  resolvedTickets: number;
  closedTickets: number;
  overdueTickets: number;
  slaComplianceRate: number;
}

export interface ChartData {
  key: string;
  label: string;
  count: number;
  percentage: number;
}

export interface StaffPerformance {
  staffId: number;
  staffName: string;
  staffEmail: string;
  totalAssigned: number;
  totalResolved: number;
  totalClosed: number;
  avgResolutionHours: number;
  slaBreaches: number;
}
