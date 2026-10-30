import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { authApi } from '../../api/authApi';
import { useNavigate } from 'react-router-dom';

export const LoginPage: React.FC = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!usernameOrEmail.trim() || !password.trim()) {
      setError('Vui lòng nhập tên đăng nhập và mật khẩu');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const res = await authApi.login({ usernameOrEmail, password });
      login(res.data);
      navigate('/');
    } catch (err: any) {
      setError(err?.message || 'Tên đăng nhập hoặc mật khẩu không chính xác');
    } finally {
      setLoading(false);
    }
  };

  const fillQuickAccount = (user: string, pass: string) => {
    setUsernameOrEmail(user);
    setPassword(pass);
    setError(null);
  };

  return (
    <div className="container d-flex align-items-center justify-content-center vh-100">
      <div className="col-12 col-md-8 col-lg-5">
        <div className="card shadow-lg border-0 rounded-4">
          <div className="card-body p-4 p-md-5">
            <div className="text-center mb-4">
              <div className="bg-primary text-white rounded-circle d-inline-flex p-3 mb-2 shadow">
                <i className="fa-solid fa-headset fs-2"></i>
              </div>
              <h3 className="fw-bold text-dark">IT Service Desk</h3>
              <p className="text-muted small">Hệ thống Quản lý Yêu cầu Hỗ trợ IT Doanh nghiệp</p>
            </div>

            {error && <div className="alert alert-danger py-2">{error}</div>}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label fw-semibold">Tên đăng nhập hoặc Email</label>
                <div className="input-group">
                  <span className="input-group-text bg-light border-end-0">
                    <i className="fa-solid fa-user text-muted"></i>
                  </span>
                  <input
                    type="text"
                    className="form-control border-start-0"
                    placeholder="manager, staff_nam, employee_lan..."
                    value={usernameOrEmail}
                    onChange={(e) => setUsernameOrEmail(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="mb-4">
                <label className="form-label fw-semibold">Mật khẩu</label>
                <div className="input-group">
                  <span className="input-group-text bg-light border-end-0">
                    <i className="fa-solid fa-lock text-muted"></i>
                  </span>
                  <input
                    type="password"
                    className="form-control border-start-0"
                    placeholder="Nhập mật khẩu..."
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary w-100 py-2 fw-semibold mb-3" disabled={loading}>
                {loading ? 'Đang xác thực...' : 'Đăng nhập hệ thống'}
              </button>
            </form>

            <div className="border-top pt-3 mt-3">
              <div className="small fw-bold text-muted mb-2 text-center">Tài khoản demo sẵn có:</div>
              <div className="d-flex flex-wrap gap-2 justify-content-center">
                <button
                  type="button"
                  className="btn btn-sm btn-outline-dark"
                  onClick={() => fillQuickAccount('manager', 'Password@123')}
                >
                  <i className="fa-solid fa-user-shield me-1"></i> IT Manager
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary"
                  onClick={() => fillQuickAccount('staff_nam', 'Password@123')}
                >
                  <i className="fa-solid fa-wrench me-1"></i> IT Staff
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-success"
                  onClick={() => fillQuickAccount('employee_lan', 'Password@123')}
                >
                  <i className="fa-solid fa-user me-1"></i> Employee
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
