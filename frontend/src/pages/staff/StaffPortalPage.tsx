import React, { useState, useEffect } from 'react';
import { ticketApi } from '../../api/ticketApi';
import { Ticket } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityBadge } from '../../components/common/PriorityBadge';
import { Link } from 'react-router-dom';

export const StaffPortalPage: React.FC = () => {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

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
      <div className="mb-4">
        <h4 className="fw-bold text-dark mb-1">
          <i className="fa-solid fa-screwdriver-wrench text-primary me-2"></i>
          Hàng đợi công việc (Kỹ thuật viên IT)
        </h4>
        <p className="text-muted small mb-0">Các Ticket được phân công cho bạn xử lý và theo dõi tiến độ SLA</p>
      </div>

      {/* Quick Status Buttons */}
      <div className="d-flex gap-2 mb-3">
        <button
          className={`btn btn-sm ${status === '' ? 'btn-primary' : 'btn-outline-secondary'}`}
          onClick={() => { setStatus(''); setPage(0); }}
        >
          Tất cả
        </button>
        <button
          className={`btn btn-sm ${status === 'ASSIGNED' ? 'btn-primary' : 'btn-outline-secondary'}`}
          onClick={() => { setStatus('ASSIGNED'); setPage(0); }}
        >
          Mới được giao
        </button>
        <button
          className={`btn btn-sm ${status === 'IN_PROGRESS' ? 'btn-warning text-dark' : 'btn-outline-secondary'}`}
          onClick={() => { setStatus('IN_PROGRESS'); setPage(0); }}
        >
          Đang xử lý
        </button>
        <button
          className={`btn btn-sm ${status === 'PENDING' ? 'btn-secondary' : 'btn-outline-secondary'}`}
          onClick={() => { setStatus('PENDING'); setPage(0); }}
        >
          Tạm dừng
        </button>
        <button
          className={`btn btn-sm ${status === 'RESOLVED' ? 'btn-success' : 'btn-outline-secondary'}`}
          onClick={() => { setStatus('RESOLVED'); setPage(0); }}
        >
          Đã giải quyết
        </button>
      </div>

      {/* Search Bar */}
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <form onSubmit={handleSearch} className="row g-2">
            <div className="col-md-9">
              <input
                type="text"
                className="form-control"
                placeholder="Tìm theo mã ticket, tiêu đề hoặc tên người báo cáo..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
            <div className="col-md-3">
              <button type="submit" className="btn btn-secondary w-100">
                <i className="fa-solid fa-search me-1"></i> Tìm kiếm
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
                <th>Tiêu đề & Người tạo</th>
                <th>Danh mục</th>
                <th>Ưu tiên</th>
                <th>Trạng thái</th>
                <th>Hạn xử lý SLA</th>
                <th className="text-end">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} className="text-center py-4">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Đang tải danh sách công việc...
                  </td>
                </tr>
              ) : tickets.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-5 text-muted">
                    <i className="fa-solid fa-check-double fs-2 mb-2 d-block text-success"></i>
                    Không có Ticket nào trong hàng đợi
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
                      <div className={t.isOverdue ? 'text-danger fw-bold' : ''}>
                        {new Date(t.dueAt).toLocaleString('vi-VN')}
                      </div>
                      {t.isOverdue && <span className="badge bg-danger">QUÁ HẠN</span>}
                    </td>
                    <td className="text-end">
                      <Link to={`/tickets/${t.id}`} className="btn btn-primary btn-sm">
                        Xử lý <i className="fa-solid fa-arrow-right ms-1"></i>
                      </Link>
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
    </div>
  );
};
