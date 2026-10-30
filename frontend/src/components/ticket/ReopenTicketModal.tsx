import React, { useState, useEffect } from 'react';
import { ticketApi } from '../../api/ticketApi';
import { Ticket } from '../../types';

interface ReopenTicketModalProps {
  ticket: Ticket | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const ReopenTicketModal: React.FC<ReopenTicketModalProps> = ({
  ticket,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setReason('');
      setError(null);
    }
  }, [isOpen]);

  if (!isOpen || !ticket) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) {
      setError('Vui lòng nêu rõ lý do chưa nghiệm thu / cần mở lại yêu cầu');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await ticketApi.reopenTicket(ticket.id, {
        reason: reason.trim(),
        version: ticket.version,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Không thể mở lại Ticket (quá hạn 3 ngày hoặc lỗi phiên bản)');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header bg-danger text-white">
            <h5 className="modal-title fw-bold">
              <i className="fa-solid fa-rotate-left me-2"></i>
              Yêu cầu mở lại Ticket: {ticket.ticketCode}
            </h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}

              <div className="alert alert-warning small">
                <i className="fa-solid fa-info-circle me-1"></i>
                Lưu ý: Bạn chỉ có thể yêu cầu mở lại Ticket trong vòng <strong>3 ngày (72 giờ)</strong> kể từ thời điểm kỹ thuật viên đánh dấu đã giải quyết.
              </div>

              <div className="mb-3">
                <label className="form-label fw-semibold">
                  Lý do chưa nghiệm thu / Sự cố chưa hết: <span className="text-danger">*</span>
                </label>
                <textarea
                  className="form-control"
                  rows={4}
                  placeholder="Mô tả cụ thể hiện tượng vẫn chưa được khắc phục để Kỹ thuật viên tiếp tục kiểm tra..."
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  required
                ></textarea>
              </div>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
                Hủy
              </button>
              <button type="submit" className="btn btn-danger" disabled={loading}>
                {loading ? 'Đang gửi...' : 'Gửi yêu cầu mở lại'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
