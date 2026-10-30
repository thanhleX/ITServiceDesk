import React, { useState, useEffect } from 'react';
import { masterDataApi } from '../../api/masterDataApi';
import { Department, User } from '../../types';

export const UserManagementPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [roleCode, setRoleCode] = useState('');

  // Create User Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
    departmentId: 1,
    roleCode: 'ROLE_IT_STAFF',
  });
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState<string | null>(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await masterDataApi.getUsers({
        keyword: keyword || undefined,
        roleCode: roleCode || undefined,
        size: 50,
      });
      setUsers(res.data.content);
    } catch {
      // Ignored
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    masterDataApi.getDepartments().then((res) => setDepartments(res.data));
    fetchUsers();
  }, [roleCode]);

  const handleToggleStatus = async (user: User) => {
    const nextStatus = user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    if (!window.confirm(`Bạn có chắc muốn chuyển trạng thái tài khoản "${user.username}" sang ${nextStatus}?`)) {
      return;
    }
    try {
      await masterDataApi.changeUserStatus(user.id, nextStatus);
      fetchUsers();
    } catch (err: any) {
      alert(err?.message || 'Lỗi cập nhật trạng thái');
    }
  };

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setModalError(null);
    try {
      await masterDataApi.createUser({
        username: formData.username,
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName,
        phone: formData.phone || undefined,
        departmentId: formData.departmentId,
        roleCodes: [formData.roleCode],
      });
      setIsModalOpen(false);
      setFormData({
        username: '',
        email: '',
        password: '',
        fullName: '',
        phone: '',
        departmentId: 1,
        roleCode: 'ROLE_IT_STAFF',
      });
      fetchUsers();
    } catch (err: any) {
      setModalError(err?.message || 'Không thể tạo tài khoản');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h4 className="fw-bold text-dark mb-1">
            <i className="fa-solid fa-users-cog text-primary me-2"></i>
            Quản trị tài khoản người dùng
          </h4>
          <p className="text-muted small mb-0">Quản lý danh sách nhân viên, gán vai trò và phòng ban</p>
        </div>
        <button className="btn btn-primary shadow-sm" onClick={() => setIsModalOpen(true)}>
          <i className="fa-solid fa-user-plus me-2"></i>
          Thêm người dùng mới
        </button>
      </div>

      {/* Filter */}
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-8">
              <input
                type="text"
                className="form-control"
                placeholder="Tìm theo username, họ tên hoặc email..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && fetchUsers()}
              />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={roleCode} onChange={(e) => setRoleCode(e.target.value)}>
                <option value="">Tất cả vai trò</option>
                <option value="ROLE_EMPLOYEE">Nhân viên (EMPLOYEE)</option>
                <option value="ROLE_IT_STAFF">Kỹ thuật viên (IT_STAFF)</option>
                <option value="ROLE_IT_MANAGER">Trưởng phòng (IT_MANAGER)</option>
              </select>
            </div>
            <div className="col-md-1">
              <button className="btn btn-secondary w-100" onClick={fetchUsers}>
                Tìm
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="card shadow-sm border-0">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light small text-uppercase text-muted">
              <tr>
                <th>Username</th>
                <th>Họ và tên</th>
                <th>Email</th>
                <th>Phòng ban</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th className="text-end">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} className="text-center py-4">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Đang tải danh sách tài khoản...
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-4 text-muted">
                    Không tìm thấy người dùng nào
                  </td>
                </tr>
              ) : (
                users.map((u) => (
                  <tr key={u.id}>
                    <td className="fw-bold">{u.username}</td>
                    <td>{u.fullName}</td>
                    <td className="text-muted small">{u.email}</td>
                    <td>
                      <span className="badge bg-light text-dark border">
                        {u.departmentName || 'Chưa phân bổ'}
                      </span>
                    </td>
                    <td>
                      {u.roles.map((r) => (
                        <span key={r} className="badge bg-info text-dark me-1">
                          {r.replace('ROLE_', '')}
                        </span>
                      ))}
                    </td>
                    <td>
                      {u.status === 'ACTIVE' ? (
                        <span className="badge bg-success">Đang hoạt động</span>
                      ) : (
                        <span className="badge bg-danger">Đã khóa</span>
                      )}
                    </td>
                    <td className="text-end">
                      <button
                        className={`btn btn-sm ${u.status === 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}`}
                        onClick={() => handleToggleStatus(u)}
                      >
                        {u.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create User Modal */}
      {isModalOpen && (
        <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-md modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header bg-primary text-white">
                <h5 className="modal-title fw-bold">
                  <i className="fa-solid fa-user-plus me-2"></i>
                  Thêm tài khoản người dùng
                </h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setIsModalOpen(false)}></button>
              </div>
              <form onSubmit={handleCreateUser}>
                <div className="modal-body">
                  {modalError && <div className="alert alert-danger py-2">{modalError}</div>}

                  <div className="mb-3">
                    <label className="form-label fw-semibold">Tên đăng nhập <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="vd: duc.it, hoa.hr..."
                      value={formData.username}
                      onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-semibold">Email nội bộ <span className="text-danger">*</span></label>
                    <input
                      type="email"
                      className="form-control"
                      placeholder="duc.it@itservicedesk.com"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-semibold">Mật khẩu khởi tạo <span className="text-danger">*</span></label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="Tối thiểu 6 ký tự"
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-semibold">Họ và tên <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Nguyễn Văn Đức"
                      value={formData.fullName}
                      onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                      required
                    />
                  </div>

                  <div className="row mb-3">
                    <div className="col-md-6">
                      <label className="form-label fw-semibold">Phòng ban</label>
                      <select
                        className="form-select"
                        value={formData.departmentId}
                        onChange={(e) => setFormData({ ...formData, departmentId: Number(e.target.value) })}
                      >
                        {departments.map((d) => (
                          <option key={d.id} value={d.id}>
                            {d.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-semibold">Vai trò <span className="text-danger">*</span></label>
                      <select
                        className="form-select"
                        value={formData.roleCode}
                        onChange={(e) => setFormData({ ...formData, roleCode: e.target.value })}
                      >
                        <option value="ROLE_EMPLOYEE">EMPLOYEE (Nhân viên)</option>
                        <option value="ROLE_IT_STAFF">IT_STAFF (Kỹ thuật viên)</option>
                        <option value="ROLE_IT_MANAGER">IT_MANAGER (Quản lý)</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)} disabled={submitting}>
                    Hủy
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Đang tạo...' : 'Tạo tài khoản'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
