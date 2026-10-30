import React, { useState, useEffect } from 'react';
import { ticketApi } from '../../api/ticketApi';
import { Ticket, TicketStatus } from '../../types';

interface UpdateStatusModalProps {
  ticket: Ticket | null;
  targetStatus: TicketStatus | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const UpdateStatusModal: React.FC<UpdateStatusModalProps> = ({
  ticket,
  targetStatus,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [resolution, setResolution] = useState('');
  const [note, setNote] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setResolution('');
      setNote('');
      setError(null);
    }
  }, [isOpen]);

  if (!isOpen || !ticket || !targetStatus) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (targetStatus === 'RESOLVED' && !resolution.trim()) {
      setError('Bắt buộc phải nhập giải pháp xử lý trước khi đánh dấu đã giải quyết');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await ticketApi.updateStatus(ticket.id, {
        targetStatus,
        resolution: resolution.trim(),
        note: note.trim(),
        version: ticket.version,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Lỗi cập nhật trạng thái');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header bg-primary text-white">
            <h5 className="modal-title fw-bold">
              Chuyển trạng thái sang: <span className="badge bg-light text-dark">{targetStatus}</span>
            </h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}

              {targetStatus === 'RESOLVED' && (
                <div className="mb-3">
                  <label className="form-label fw-bold text-danger">
                    Nguyên nhân & Giải pháp xử lý <span className="text-danger">*</span>
                  </label>
                  <textarea
                    className="form-control border-danger"
                    rows={4}
                    placeholder="Mô tả nguyên nhân sự cố và các biện pháp kỹ thuật đã thực hiện để khắc phục..."
                    value={resolution}
                    onChange={(e) => setResolution(e.target.value)}
                    required
                  ></textarea>
                  <div className="form-text">
                    Thông tin này sẽ được lưu vào cơ sở dữ liệu và gửi thông báo nghiệm thu tới người tạo yêu cầu.
                  </div>
                </div>
              )}

              <div className="mb-3">
                <label className="form-label fw-semibold">Ghi chú bổ sung:</label>
                <textarea
                  className="form-control"
                  rows={2}
                  placeholder="Ghi chú nội bộ quá trình xử lý (nếu có)..."
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                ></textarea>
              </div>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
                Hủy
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Đang cập nhật...' : 'Xác nhận chuyển'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
