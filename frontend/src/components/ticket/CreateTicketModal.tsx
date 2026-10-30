import React, { useState, useEffect } from 'react';
import { masterDataApi } from '../../api/masterDataApi';
import { ticketApi } from '../../api/ticketApi';
import { Category, TicketPriority } from '../../types';

interface CreateTicketModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const CreateTicketModal: React.FC<CreateTicketModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      masterDataApi.getCategories().then((res) => {
        setCategories(res.data);
        if (res.data.length > 0) setCategoryId(res.data[0].id);
      });
      setTitle('');
      setDescription('');
      setError(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !description.trim() || !categoryId) {
      setError('Vui lòng điền đầy đủ các thông tin bắt buộc');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await ticketApi.createTicket({ title, description, categoryId, priority });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Không thể tạo ticket. Vui lòng thử lại');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header bg-primary text-white">
            <h5 className="modal-title fw-bold">
              <i className="fa-solid fa-plus-circle me-2"></i>
              Tạo mới yêu cầu hỗ trợ IT
            </h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}

              <div className="mb-3">
                <label className="form-label fw-semibold">
                  Tiêu đề sự cố <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Ví dụ: Máy tính không lên nguồn, mất kết nối Wi-Fi..."
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>

              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label fw-semibold">
                    Danh mục sự cố <span className="text-danger">*</span>
                  </label>
                  <select
                    className="form-select"
                    value={categoryId}
                    onChange={(e) => setCategoryId(Number(e.target.value))}
                    required
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="col-md-6">
                  <label className="form-label fw-semibold">
                    Mức độ ưu tiên <span className="text-danger">*</span>
                  </label>
                  <select
                    className="form-select"
                    value={priority}
                    onChange={(e) => setPriority(e.target.value as TicketPriority)}
                    required
                  >
                    <option value="LOW">Thấp (Hạn SLA: 72 giờ)</option>
                    <option value="MEDIUM">Trung bình (Hạn SLA: 48 giờ)</option>
                    <option value="HIGH">Cao (Hạn SLA: 24 giờ)</option>
                    <option value="CRITICAL">Khẩn cấp (Hạn SLA: 8 giờ)</option>
                  </select>
                </div>
              </div>

              <div className="mb-3">
                <label className="form-label fw-semibold">
                  Mô tả chi tiết sự cố <span className="text-danger">*</span>
                </label>
                <textarea
                  className="form-control"
                  rows={4}
                  placeholder="Mô tả hiện tượng, các bước đã thử khắc phục hoặc thông báo lỗi hiển thị trên màn hình..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                ></textarea>
              </div>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
                Hủy bỏ
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Đang gửi...' : 'Gửi yêu cầu'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
