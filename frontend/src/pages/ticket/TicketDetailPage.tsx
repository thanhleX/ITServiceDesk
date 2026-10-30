import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ticketApi } from '../../api/ticketApi';
import { TicketDetail, TicketStatus } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityBadge } from '../../components/common/PriorityBadge';
import { useAuth } from '../../context/AuthContext';
import { AssignTicketModal } from '../../components/ticket/AssignTicketModal';
import { UpdateStatusModal } from '../../components/ticket/UpdateStatusModal';
import { ReopenTicketModal } from '../../components/ticket/ReopenTicketModal';

export const TicketDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const ticketId = Number(id);
  const { user, isManager } = useAuth();
  const navigate = useNavigate();

  const [detail, setDetail] = useState<TicketDetail | null>(null);
  const [comments, setComments] = useState<any[]>([]);
  const [attachments, setAttachments] = useState<any[]>([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [submittingComment, setSubmittingComment] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Modals state
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
  const [targetStatus, setTargetStatus] = useState<TicketStatus | null>(null);
  const [isReopenModalOpen, setIsReopenModalOpen] = useState(false);

  const loadData = async () => {
    try {
      const [detailRes, commentRes, attachRes] = await Promise.all([
        ticketApi.getTicketDetail(ticketId),
        ticketApi.getComments(ticketId),
        ticketApi.getAttachments(ticketId),
      ]);
      setDetail(detailRes.data);
      setComments(commentRes.data);
      setAttachments(attachRes.data);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải thông tin Ticket hoặc bạn không có quyền truy cập');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (ticketId) loadData();
  }, [ticketId]);

  if (loading) {
    return (
      <div className="p-4 text-center">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  if (error || !detail) {
    return (
      <div className="p-4">
        <div className="alert alert-danger">{error || 'Không tìm thấy Ticket'}</div>
        <button className="btn btn-outline-secondary" onClick={() => navigate(-1)}>
          <i className="fa-solid fa-arrow-left me-1"></i> Quay lại
        </button>
      </div>
    );
  }

  const ticket = detail.ticket;
  const isCreator = user?.id === ticket.createdById;
  const isAssignee = user?.id === ticket.assignedToId;

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    setSubmittingComment(true);
    try {
      await ticketApi.addComment(ticketId, newComment.trim());
      setNewComment('');
      const res = await ticketApi.getComments(ticketId);
      setComments(res.data);
    } catch (err: any) {
      alert(err?.message || 'Lỗi gửi bình luận');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingFile(true);
    try {
      await ticketApi.uploadAttachment(ticketId, file);
      const res = await ticketApi.getAttachments(ticketId);
      setAttachments(res.data);
    } catch (err: any) {
      alert(err?.message || 'Lỗi tải file lên');
    } finally {
      setUploadingFile(false);
      e.target.value = '';
    }
  };

  const handleDeleteAttachment = async (attachId: number) => {
    if (!window.confirm('Bạn có chắc muốn xóa file đính kèm này?')) return;
    try {
      await ticketApi.deleteAttachment(attachId);
      setAttachments((prev) => prev.filter((a) => a.id !== attachId));
    } catch (err: any) {
      alert(err?.message || 'Không thể xóa file');
    }
  };

  const openStatusChange = (status: TicketStatus) => {
    setTargetStatus(status);
    setIsStatusModalOpen(true);
  };

  return (
    <div className="p-4">
      {/* Top action bar */}
      <div className="d-flex flex-wrap justify-content-between align-items-center mb-3">
        <div>
          <button className="btn btn-outline-secondary btn-sm me-2" onClick={() => navigate(-1)}>
            <i className="fa-solid fa-arrow-left me-1"></i> Trở về
          </button>
          <span className="fw-bold fs-5 me-2">{ticket.ticketCode}</span>
          <StatusBadge status={ticket.status} />
          <span className="ms-2">
            <PriorityBadge priority={ticket.priority} />
          </span>
          {ticket.isOverdue && (
            <span className="badge bg-danger ms-2 px-2 py-1">
              <i className="fa-solid fa-triangle-exclamation me-1"></i> Quá hạn SLA
            </span>
          )}
        </div>

        {/* Action Buttons based on roles and workflow state */}
        <div className="d-flex gap-2 mt-2 mt-md-0">
          {/* IT Manager Actions */}
          {isManager && ticket.status !== 'CLOSED' && (
            <button className="btn btn-outline-dark btn-sm" onClick={() => setIsAssignModalOpen(true)}>
              <i className="fa-solid fa-user-plus me-1"></i> Phân công
            </button>
          )}

          {/* IT Staff Actions */}
          {(isAssignee || isManager) && (
            <>
              {ticket.status === 'ASSIGNED' && (
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => openStatusChange('IN_PROGRESS')}
                >
                  <i className="fa-solid fa-play me-1"></i> Bắt đầu xử lý
                </button>
              )}
              {ticket.status === 'IN_PROGRESS' && (
                <>
                  <button
                    className="btn btn-warning btn-sm"
                    onClick={() => openStatusChange('PENDING')}
                  >
                    <i className="fa-solid fa-pause me-1"></i> Tạm hoãn
                  </button>
                  <button
                    className="btn btn-success btn-sm"
                    onClick={() => openStatusChange('RESOLVED')}
                  >
                    <i className="fa-solid fa-check me-1"></i> Hoàn thành (Resolve)
                  </button>
                </>
              )}
              {ticket.status === 'PENDING' && (
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => openStatusChange('IN_PROGRESS')}
                >
                  <i className="fa-solid fa-play me-1"></i> Tiếp tục xử lý
                </button>
              )}
              {ticket.status === 'REOPENED' && (
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => openStatusChange('IN_PROGRESS')}
                >
                  <i className="fa-solid fa-wrench me-1"></i> Bắt đầu xử lý lại
                </button>
              )}
            </>
          )}

          {/* Creator Actions */}
          {(isCreator || isManager) && ticket.status === 'RESOLVED' && (
            <>
              <button
                className="btn btn-success btn-sm"
                onClick={() => openStatusChange('CLOSED')}
              >
                <i className="fa-solid fa-thumbs-up me-1"></i> Xác nhận & Đóng ticket
              </button>
              <button
                className="btn btn-outline-danger btn-sm"
                onClick={() => setIsReopenModalOpen(true)}
              >
                <i className="fa-solid fa-rotate-left me-1"></i> Yêu cầu mở lại
              </button>
            </>
          )}
        </div>
      </div>

      <div className="row g-4">
        {/* Left Column: Details, Resolution, Comments, Attachments */}
        <div className="col-lg-8">
          {/* Main Info Card */}
          <div className="card shadow-sm border-0 mb-4">
            <div className="card-body">
              <h4 className="fw-bold mb-3">{ticket.title}</h4>
              <div className="p-3 bg-light rounded-3 mb-3">
                <div className="fw-semibold text-muted small mb-1">Mô tả sự cố:</div>
                <div style={{ whiteSpace: 'pre-wrap' }}>{ticket.description}</div>
              </div>

              {ticket.resolution && (
                <div className="alert alert-success mt-3 mb-0">
                  <div className="fw-bold d-flex align-items-center mb-1">
                    <i className="fa-solid fa-check-circle me-2 fs-5"></i>
                    Giải pháp kỹ thuật (Resolution):
                  </div>
                  <div style={{ whiteSpace: 'pre-wrap' }}>{ticket.resolution}</div>
                  {ticket.resolvedAt && (
                    <div className="small text-muted mt-2">
                      Giải quyết lúc: {new Date(ticket.resolvedAt).toLocaleString('vi-VN')}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Attachments Section */}
          <div className="card shadow-sm border-0 mb-4">
            <div className="card-header bg-white d-flex justify-content-between align-items-center py-3">
              <h6 className="fw-bold mb-0">
                <i className="fa-solid fa-paperclip me-2 text-primary"></i>
                File đính kèm ({attachments.length})
              </h6>
              {ticket.status !== 'CLOSED' && (
                <label className="btn btn-sm btn-outline-primary mb-0 cursor-pointer">
                  <i className="fa-solid fa-upload me-1"></i>
                  {uploadingFile ? 'Đang tải...' : 'Thêm file'}
                  <input type="file" className="d-none" onChange={handleFileUpload} disabled={uploadingFile} />
                </label>
              )}
            </div>
            <div className="card-body">
              {attachments.length === 0 ? (
                <div className="text-muted small text-center py-2">Chưa có tập tin đính kèm nào</div>
              ) : (
                <div className="list-group list-group-flush">
                  {attachments.map((item) => (
                    <div
                      key={item.id}
                      className="list-group-item d-flex justify-content-between align-items-center px-0"
                    >
                      <div>
                        <i className="fa-regular fa-file me-2 text-muted"></i>
                        <a
                          href={`http://localhost:8080/api/attachments/${item.id}`}
                          target="_blank"
                          rel="noreferrer"
                          className="fw-semibold text-decoration-none"
                        >
                          {item.fileName}
                        </a>
                        <span className="text-muted small ms-2">
                          ({(item.fileSize / 1024).toFixed(1)} KB) - tải bởi {item.uploadedByName}
                        </span>
                      </div>
                      {(isManager || user?.id === item.uploadedById) && ticket.status !== 'CLOSED' && (
                        <button
                          className="btn btn-link text-danger p-0"
                          onClick={() => handleDeleteAttachment(item.id)}
                        >
                          <i className="fa-solid fa-trash"></i>
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Comments Section */}
          <div className="card shadow-sm border-0">
            <div className="card-header bg-white py-3">
              <h6 className="fw-bold mb-0">
                <i className="fa-solid fa-comments me-2 text-primary"></i>
                Trao đổi & Bình luận ({comments.length})
              </h6>
            </div>
            <div className="card-body">
              <div className="mb-4" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                {comments.length === 0 ? (
                  <div className="text-muted small text-center py-3">Chưa có trao đổi nào trên Ticket này</div>
                ) : (
                  comments.map((c) => (
                    <div key={c.id} className="d-flex mb-3">
                      <div
                        className="bg-secondary text-white rounded-circle d-flex align-items-center justify-content-center me-2 flex-shrink-0"
                        style={{ width: '36px', height: '36px', fontWeight: 'bold' }}
                      >
                        {c.userFullName ? c.userFullName.charAt(0).toUpperCase() : 'U'}
                      </div>
                      <div className="flex-grow-1 bg-light p-3 rounded-3">
                        <div className="d-flex justify-content-between align-items-center mb-1">
                          <span className="fw-bold small">{c.userFullName}</span>
                          <span className="text-muted" style={{ fontSize: '0.75rem' }}>
                            {new Date(c.createdAt).toLocaleString('vi-VN')}
                          </span>
                        </div>
                        <div style={{ whiteSpace: 'pre-wrap', fontSize: '0.9rem' }}>{c.content}</div>
                      </div>
                    </div>
                  ))
                )}
              </div>

              {ticket.status !== 'CLOSED' && (
                <form onSubmit={handleAddComment}>
                  <div className="mb-2">
                    <textarea
                      className="form-control"
                      rows={3}
                      placeholder="Nhập nội dung trao đổi..."
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                      required
                    ></textarea>
                  </div>
                  <div className="text-end">
                    <button type="submit" className="btn btn-primary btn-sm" disabled={submittingComment}>
                      {submittingComment ? 'Đang gửi...' : 'Gửi bình luận'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Meta Info, Assignee, History Timeline */}
        <div className="col-lg-4">
          {/* Metadata Card */}
          <div className="card shadow-sm border-0 mb-4">
            <div className="card-header bg-white py-3">
              <h6 className="fw-bold mb-0">Thông tin chi tiết</h6>
            </div>
            <div className="card-body small">
              <div className="mb-2">
                <span className="text-muted">Danh mục:</span>{' '}
                <span className="fw-semibold">{ticket.categoryName}</span>
              </div>
              <div className="mb-2">
                <span className="text-muted">Người tạo:</span>{' '}
                <span className="fw-semibold">
                  {ticket.createdByName} ({ticket.departmentName || 'N/A'})
                </span>
              </div>
              <div className="mb-2">
                <span className="text-muted">Kỹ thuật viên phụ trách:</span>{' '}
                <span className="fw-semibold text-primary">
                  {ticket.assignedToName || 'Chưa phân công'}
                </span>
              </div>
              <div className="mb-2">
                <span className="text-muted">Thời điểm tạo:</span>{' '}
                <span>{new Date(ticket.createdAt).toLocaleString('vi-VN')}</span>
              </div>
              <div className="mb-2">
                <span className="text-muted">Hạn chót SLA:</span>{' '}
                <span className={ticket.isOverdue ? 'text-danger fw-bold' : 'fw-semibold'}>
                  {new Date(ticket.dueAt).toLocaleString('vi-VN')}
                </span>
              </div>
              {ticket.closedAt && (
                <div className="mb-2">
                  <span className="text-muted">Nghiệm thu đóng:</span>{' '}
                  <span>{new Date(ticket.closedAt).toLocaleString('vi-VN')}</span>
                </div>
              )}
            </div>
          </div>

          {/* Audit History Timeline */}
          <div className="card shadow-sm border-0">
            <div className="card-header bg-white py-3">
              <h6 className="fw-bold mb-0">
                <i className="fa-solid fa-clock-rotate-left me-2 text-primary"></i>
                Nhật ký xử lý (Audit Log)
              </h6>
            </div>
            <div className="card-body p-3 small" style={{ maxHeight: '450px', overflowY: 'auto' }}>
              <div className="timeline">
                {detail.histories.map((h) => (
                  <div key={h.id} className="border-start border-2 border-primary ps-3 pb-3 position-relative">
                    <div
                      className="position-absolute bg-primary rounded-circle"
                      style={{ width: '8px', height: '8px', left: '-5px', top: '4px' }}
                    ></div>
                    <div className="d-flex justify-content-between">
                      <span className="fw-bold text-dark">{h.action}</span>
                      <span className="text-muted" style={{ fontSize: '0.7rem' }}>
                        {new Date(h.createdAt).toLocaleTimeString('vi-VN')}
                      </span>
                    </div>
                    <div className="text-muted">{h.description}</div>
                    <div className="text-secondary" style={{ fontSize: '0.75rem' }}>
                      Bởi: {h.performedByName}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modals */}
      <AssignTicketModal
        ticket={ticket}
        isOpen={isAssignModalOpen}
        onClose={() => setIsAssignModalOpen(false)}
        onSuccess={loadData}
      />
      <UpdateStatusModal
        ticket={ticket}
        targetStatus={targetStatus}
        isOpen={isStatusModalOpen}
        onClose={() => setIsStatusModalOpen(false)}
        onSuccess={loadData}
      />
      <ReopenTicketModal
        ticket={ticket}
        isOpen={isReopenModalOpen}
        onClose={() => setIsReopenModalOpen(false)}
        onSuccess={loadData}
      />
    </div>
  );
};
