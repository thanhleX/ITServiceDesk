import React, { useState, useEffect } from 'react';
import { masterDataApi } from '../../api/masterDataApi';
import { ticketApi } from '../../api/ticketApi';
import { Ticket, User } from '../../types';

interface AssignTicketModalProps {
  ticket: Ticket | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const AssignTicketModal: React.FC<AssignTicketModalProps> = ({
  ticket,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [staffs, setStaffs] = useState<User[]>([]);
  const [assigneeId, setAssigneeId] = useState<number>(0);
  const [note, setNote] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && ticket) {
      masterDataApi.getActiveStaffs().then((res) => {
        setStaffs(res.data);
        if (res.data.length > 0) {
          setAssigneeId(ticket.assignedToId || res.data[0].id);
        }
      });
      setNote('');
      setError(null);
    }
  }, [isOpen, ticket]);

  if (!isOpen || !ticket) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!assigneeId) {
      setError('Vui lòng chọn kỹ thuật viên IT');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await ticketApi.assignTicket(ticket.id, {
        assigneeId,
        note,
        version: ticket.version,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Lỗi phân công Ticket (có thể do phiên bản dữ liệu bị xung đột)');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header bg-dark text-white">
            <h5 className="modal-title fw-bold">
              <i className="fa-solid fa-user-plus me-2"></i>
              Phân công Ticket: {ticket.ticketCode}
            </h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}

              <div className="mb-3">
                <label className="form-label fw-semibold">Chọn Kỹ thuật viên phụ trách:</label>
                <select
                  className="form-select"
                  value={assigneeId}
                  onChange={(e) => setAssigneeId(Number(e.target.value))}
                  required
                >
                  {staffs.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.fullName} ({s.email})
                    </option>
                  ))}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label fw-semibold">Ghi chú phân công:</label>
                <textarea
                  className="form-control"
                  rows={3}
                  placeholder="Ghi chú yêu cầu hoặc thời hạn cụ thể..."
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
                {loading ? 'Đang lưu...' : 'Xác nhận phân công'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
