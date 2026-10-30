import React from 'react';
import { TicketPriority } from '../../types';

export const PriorityBadge: React.FC<{ priority: TicketPriority }> = ({ priority }) => {
  const map: Record<TicketPriority, { bg: string; text: string; icon: string }> = {
    LOW: { bg: 'bg-secondary', text: 'Thấp (72h)', icon: 'fa-arrow-down' },
    MEDIUM: { bg: 'bg-info text-dark', text: 'Trung bình (48h)', icon: 'fa-minus' },
    HIGH: { bg: 'bg-warning text-dark', text: 'Cao (24h)', icon: 'fa-arrow-up' },
    CRITICAL: { bg: 'bg-danger', text: 'Khẩn cấp (8h)', icon: 'fa-bolt' },
  };

  const item = map[priority] || { bg: 'bg-light text-dark', text: priority, icon: 'fa-circle' };

  return (
    <span className={`badge ${item.bg} px-2 py-1`}>
      <i className={`fa-solid ${item.icon} me-1`}></i>
      {item.text}
    </span>
  );
};
