import React, { useState, useEffect } from 'react';
import { notificationApi } from '../../api/notificationApi';
import { NotificationItem } from '../../types';
import { useNavigate } from 'react-router-dom';

export const NotificationDropdown: React.FC = () => {
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const navigate = useNavigate();

  const loadNotifications = async () => {
    try {
      const [countRes, listRes] = await Promise.all([
        notificationApi.getUnreadCount(),
        notificationApi.getMyNotifications(0, 5),
      ]);
      setUnreadCount(countRes.data);
      setNotifications(listRes.data.content);
    } catch {
      // Ignored
    }
  };

  useEffect(() => {
    loadNotifications();
    const interval = setInterval(loadNotifications, 60000); // 1 phút refresh một lần
    return () => clearInterval(interval);
  }, []);

  const handleItemClick = async (item: NotificationItem) => {
    if (!item.isRead) {
      await notificationApi.markAsRead(item.id);
      setUnreadCount((prev) => Math.max(0, prev - 1));
    }
    setIsOpen(false);
    if (item.referenceId) {
      navigate(`/tickets/${item.referenceId}`);
    }
  };

  const handleMarkAllAsRead = async () => {
    await notificationApi.markAllAsRead();
    setUnreadCount(0);
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  return (
    <div className="dropdown position-relative me-3">
      <button
        className="btn btn-outline-secondary position-relative rounded-circle"
        style={{ width: '40px', height: '40px' }}
        onClick={() => setIsOpen(!isOpen)}
      >
        <i className="fa-solid fa-bell"></i>
        {unreadCount > 0 && (
          <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div
          className="dropdown-menu dropdown-menu-end show shadow p-0"
          style={{ width: '340px', right: 0, marginTop: '8px' }}
        >
          <div className="p-3 border-bottom d-flex justify-content-between align-items-center bg-white">
            <h6 className="mb-0 fw-bold">Thông báo</h6>
            {unreadCount > 0 && (
              <button
                className="btn btn-link btn-sm p-0 text-decoration-none"
                onClick={handleMarkAllAsRead}
              >
                Đánh dấu đã đọc
              </button>
            )}
          </div>
          <div style={{ maxHeight: '350px', overflowY: 'auto' }}>
            {notifications.length === 0 ? (
              <div className="p-3 text-center text-muted small">Không có thông báo mới</div>
            ) : (
              notifications.map((item) => (
                <div
                  key={item.id}
                  className={`p-3 border-bottom list-group-item-action cursor-pointer ${
                    !item.isRead ? 'bg-light' : ''
                  }`}
                  style={{ cursor: 'pointer' }}
                  onClick={() => handleItemClick(item)}
                >
                  <div className="d-flex justify-content-between align-items-start">
                    <span className="fw-semibold small">{item.title}</span>
                    {!item.isRead && <span className="badge bg-primary rounded-pill">Mới</span>}
                  </div>
                  <div className="small text-muted text-truncate mt-1">{item.message}</div>
                  <div className="text-muted" style={{ fontSize: '0.75rem' }}>
                    {new Date(item.createdAt).toLocaleString('vi-VN')}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
