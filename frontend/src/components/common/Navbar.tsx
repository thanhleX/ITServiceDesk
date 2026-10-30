import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { NotificationDropdown } from '../notification/NotificationDropdown';
import { Link, useNavigate } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const { user, logout, isManager, isStaff } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm px-3">
      <div className="container-fluid">
        <Link className="navbar-brand fw-bold d-flex align-items-center" to="/">
          <i className="fa-solid fa-headset text-primary me-2 fs-4"></i>
          <span>IT Service Desk</span>
        </Link>

        <div className="d-flex align-items-center ms-auto">
          <NotificationDropdown />

          {user && (
            <div className="dropdown">
              <button
                className="btn btn-dark d-flex align-items-center dropdown-toggle border-0"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <div
                  className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2"
                  style={{ width: '35px', height: '35px', fontWeight: 'bold' }}
                >
                  {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                </div>
                <div className="text-start d-none d-md-block me-2">
                  <div className="fw-semibold text-white small">{user.fullName}</div>
                  <div className="text-white" style={{ fontSize: '0.75rem' }}>
                    {isManager ? 'IT Manager' : isStaff ? 'IT Staff' : 'Employee'}
                  </div>
                </div>
              </button>
              <ul className="dropdown-menu dropdown-menu-end shadow">
                <li className="dropdown-header">
                  <strong>{user.username}</strong> ({user.departmentName || 'Không có phòng ban'})
                </li>
                <li>
                  <hr className="dropdown-divider" />
                </li>
                <li>
                  <button className="dropdown-item text-danger" onClick={handleLogout}>
                    <i className="fa-solid fa-right-from-bracket me-2"></i>
                    Đăng xuất
                  </button>
                </li>
              </ul>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};
