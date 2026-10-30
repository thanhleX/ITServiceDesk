import React from 'react';
import { TicketStatus } from '../../types';

export const StatusBadge: React.FC<{ status: TicketStatus }> = ({ status }) => {
  const map: Record<TicketStatus, { bg: string; text: string }> = {
    OPEN: { bg: 'bg-info text-dark', text: 'Mới tạo' },
    ASSIGNED: { bg: 'bg-primary', text: 'Đã phân công' },
    IN_PROGRESS: { bg: 'bg-warning text-dark', text: 'Đang xử lý' },
    PENDING: { bg: 'bg-secondary', text: 'Tạm dừng' },
    RESOLVED: { bg: 'bg-success', text: 'Đã giải quyết' },
    CLOSED: { bg: 'bg-dark', text: 'Đã đóng' },
    REOPENED: { bg: 'bg-danger', text: 'Mở lại' },
  };

  const item = map[status] || { bg: 'bg-light text-dark', text: status };

  return <span className={`badge ${item.bg} px-2 py-1`}>{item.text}</span>;
};
