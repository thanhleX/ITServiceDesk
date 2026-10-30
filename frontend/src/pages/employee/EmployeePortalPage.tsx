import React, { useState, useEffect } from 'react';
import { ticketApi } from '../../api/ticketApi';
import { Ticket } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityBadge } from '../../components/common/PriorityBadge';
import { CreateTicketModal } from '../../components/ticket/CreateTicketModal';
import { Link } from 'react-router-dom';

export const EmployeePortalPage: React.FC = () => {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await ticketApi.getTickets({
        keyword: keyword || undefined,
        status: status || undefined,
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
  }, [page, status]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchTickets();
  };

  return (
    <div className="p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h4 className="fw-bold text-dark mb-1">
            <i className="fa-solid fa-user-tag text-primary me-2"></i>
            Yêu cầu hỗ trợ của tôi
          </h4>
          <p className="text-muted small mb-0">Theo dõi tiến độ và kết quả xử lý các sự cố IT bạn đã báo cáo</p>
        </div>
        <button className="btn btn-primary shadow-sm" onClick={() => setIsCreateModalOpen(true)}>
          <i className="fa-solid fa-plus-circle me-2"></i>
          Tạo yêu cầu mới
        </button>
      </div>

      {/* Filter Bar */}
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <form onSubmit={handleSearch} className="row g-3 align-items-center">
            <div className="col-md-5">
              <div className="input-group">
                <span className="input-group-text bg-light">
                  <i className="fa-solid fa-search text-muted"></i>
                </span>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Tìm theo mã ticket hoặc tiêu đề..."
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />
              </div>
            </div>
            <div className="col-md-4">
              <select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="">Tất cả trạng thái</option>
                <option value="OPEN">Mới tạo (OPEN)</option>
                <option value="ASSIGNED">Đã phân công (ASSIGNED)</option>
                <option value="IN_PROGRESS">Đang xử lý (IN_PROGRESS)</option>
                <option value="PENDING">Tạm hoãn (PENDING)</option>
                <option value="RESOLVED">Đã giải quyết (RESOLVED)</option>
                <option value="CLOSED">Đã đóng (CLOSED)</option>
                <option value="REOPENED">Mở lại (REOPENED)</option>
              </select>
            </div>
            <div className="col-md-3 d-flex gap-2">
              <button type="submit" className="btn btn-secondary flex-grow-1">
                Lọc dữ liệu
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => {
                  setKeyword('');
                  setStatus('');
                  setPage(0);
                }}
              >
                Đặt lại
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Table */}
      <div className="card shadow-sm border-0">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light small text-uppercase text-muted">
              <tr>
                <th>Mã Ticket</th>
                <th>Tiêu đề sự cố</th>
                <th>Danh mục</th>
                <th>Mức độ</th>
                <th>Trạng thái</th>
                <th>Người phụ trách</th>
                <th>Hạn chót SLA</th>
                <th className="text-end">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={8} className="text-center py-4">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Đang tải dữ liệu...
                  </td>
                </tr>
              ) : tickets.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-5 text-muted">
                    <i className="fa-solid fa-inbox fs-2 mb-2 d-block"></i>
                    Bạn chưa có yêu cầu hỗ trợ nào
                  </td>
                </tr>
              ) : (
                tickets.map((t) => (
                  <tr key={t.id}>
                    <td className="fw-bold">
                      <Link to={`/tickets/${t.id}`} className="text-decoration-none">
                        {t.ticketCode}
                      </Link>
                    </td>
                    <td>
                      <div className="fw-semibold text-dark">{t.title}</div>
                      <div className="text-muted small text-truncate" style={{ maxWidth: '300px' }}>
                        {t.description}
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
                    <td className="small">{t.assignedToName || <span className="text-muted">Chờ tiếp nhận</span>}</td>
                    <td className="small">
                      <span className={t.isOverdue ? 'text-danger fw-bold' : ''}>
                        {new Date(t.dueAt).toLocaleString('vi-VN')}
                      </span>
                    </td>
                    <td className="text-end">
                      <Link to={`/tickets/${t.id}`} className="btn btn-outline-primary btn-sm">
                        Chi tiết <i className="fa-solid fa-chevron-right ms-1"></i>
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
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

      {/* Create Modal */}
      <CreateTicketModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={fetchTickets}
      />
    </div>
  );
};
