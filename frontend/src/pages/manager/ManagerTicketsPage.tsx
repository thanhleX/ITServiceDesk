import React, { useState, useEffect } from 'react';
import { ticketApi } from '../../api/ticketApi';
import { masterDataApi } from '../../api/masterDataApi';
import { Category, Ticket, User } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityBadge } from '../../components/common/PriorityBadge';
import { AssignTicketModal } from '../../components/ticket/AssignTicketModal';
import { Link } from 'react-router-dom';

export const ManagerTicketsPage: React.FC = () => {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [staffs, setStaffs] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [assignedToId, setAssignedToId] = useState<number | undefined>();
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Modal
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);

  useEffect(() => {
    masterDataApi.getCategories().then((res) => setCategories(res.data));
    masterDataApi.getActiveStaffs().then((res) => setStaffs(res.data));
  }, []);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await ticketApi.getTickets({
        keyword: keyword || undefined,
        status: status || undefined,
        priority: priority || undefined,
        categoryId: categoryId || undefined,
        assignedToId: assignedToId || undefined,
        page,
        size: 10,
      });
      setTickets(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch {
      // Ignored
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, [page, status, priority, categoryId, assignedToId]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchTickets();
  };

  const openAssignModal = (ticket: Ticket) => {
    setSelectedTicket(ticket);
    setIsAssignModalOpen(true);
  };

  return (
    <div className="p-4">
      <div className="mb-4">
        <h4 className="fw-bold text-dark mb-1">
          <i className="fa-solid fa-list-check text-primary me-2"></i>
          Quản lý toàn bộ Ticket hệ thống
        </h4>
        <p className="text-muted small mb-0">Theo dõi, điều phối và phân công kỹ thuật viên cho mọi yêu cầu IT</p>
      </div>

      {/* Filter Card */}
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <form onSubmit={handleSearch} className="row g-2">
            <div className="col-md-3">
              <input
                type="text"
                className="form-control"
                placeholder="Tìm mã ticket, tiêu đề..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="">Trạng thái: Tất cả</option>
                <option value="OPEN">Mới tạo (OPEN)</option>
                <option value="ASSIGNED">Đã phân công</option>
                <option value="IN_PROGRESS">Đang xử lý</option>
                <option value="PENDING">Tạm dừng</option>
                <option value="RESOLVED">Đã giải quyết</option>
                <option value="CLOSED">Đã đóng</option>
                <option value="REOPENED">Mở lại</option>
              </select>
            </div>
            <div className="col-md-2">
              <select className="form-select" value={priority} onChange={(e) => setPriority(e.target.value)}>
                <option value="">Mức độ: Tất cả</option>
                <option value="LOW">Thấp</option>
                <option value="MEDIUM">Trung bình</option>
                <option value="HIGH">Cao</option>
                <option value="CRITICAL">Khẩn cấp</option>
              </select>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={categoryId || ''}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : undefined)}
              >
                <option value="">Danh mục: Tất cả</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={assignedToId || ''}
                onChange={(e) => setAssignedToId(e.target.value ? Number(e.target.value) : undefined)}
              >
                <option value="">Kỹ thuật: Tất cả</option>
                {staffs.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.fullName}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-1">
              <button type="submit" className="btn btn-secondary w-100">
                Lọc
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Tickets Table */}
      <div className="card shadow-sm border-0">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light small text-uppercase text-muted">
              <tr>
                <th>Mã Ticket</th>
                <th>Tiêu đề & Người tạo</th>
                <th>Danh mục</th>
                <th>Mức độ</th>
                <th>Trạng thái</th>
                <th>Phụ trách</th>
                <th>Hạn SLA</th>
                <th className="text-end">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={8} className="text-center py-4">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Đang tải danh sách...
                  </td>
                </tr>
              ) : tickets.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-5 text-muted">
                    Không tìm thấy Ticket nào phù hợp
                  </td>
                </tr>
              ) : (
                tickets.map((t) => (
                  <tr key={t.id} className={t.isOverdue ? 'table-danger' : ''}>
                    <td className="fw-bold">
                      <Link to={`/tickets/${t.id}`} className="text-decoration-none">
                        {t.ticketCode}
                      </Link>
                    </td>
                    <td>
                      <div className="fw-semibold text-dark">{t.title}</div>
                      <div className="text-muted small">
                        Người tạo: {t.createdByName} ({t.departmentName || 'N/A'})
                      </div>
                    </td>
                    <td>
                      <span className="badge bg-light text-dark border">{t.categoryName}</span>
                    </td>
                    <td>
                      <PriorityBadge priority={t.priority} />
                    </td>
                    <td>
                      <StatusBadge status={t.status} />
                    </td>
                    <td className="small">
                      {t.assignedToName ? (
                        <span className="fw-semibold text-primary">{t.assignedToName}</span>
                      ) : (
                        <span className="text-danger">Chưa giao</span>
                      )}
                    </td>
                    <td className="small">
                      <div className={t.isOverdue ? 'text-danger fw-bold' : ''}>
                        {new Date(t.dueAt).toLocaleString('vi-VN')}
                      </div>
                      {t.isOverdue && <span className="badge bg-danger">QUÁ HẠN</span>}
                    </td>
                    <td className="text-end">
                      <div className="btn-group">
                        {t.status !== 'CLOSED' && (
                          <button
                            className="btn btn-outline-dark btn-sm"
                            title="Phân công"
                            onClick={() => openAssignModal(t)}
                          >
                            <i className="fa-solid fa-user-plus"></i>
                          </button>
                        )}
                        <Link to={`/tickets/${t.id}`} className="btn btn-outline-primary btn-sm">
                          <i className="fa-solid fa-eye"></i>
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="card-footer bg-white d-flex justify-content-between align-items-center py-3">
            <span className="small text-muted">Trang {page + 1} / {totalPages}</span>
            <div className="btn-group">
              <button
                className="btn btn-outline-secondary btn-sm"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Trước
              </button>
              <button
                className="btn btn-outline-secondary btn-sm"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Sau
              </button>
            </div>
          </div>
        )}
      </div>

      <AssignTicketModal
        ticket={selectedTicket}
        isOpen={isAssignModalOpen}
        onClose={() => setIsAssignModalOpen(false)}
        onSuccess={fetchTickets}
      />
    </div>
  );
};
