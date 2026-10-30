import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const Sidebar: React.FC = () => {
  const { isManager, isStaff, isEmployee } = useAuth();

  return (
    <div className="bg-white border-end shadow-sm" style={{ width: '250px', minHeight: 'calc(100vh - 56px)' }}>
      <div className="p-3">
        <div className="small text-uppercase fw-bold text-muted mb-2">Chức năng chính</div>
        <ul className="nav nav-pills flex-column mb-auto">
          {/* IT Manager Section */}
          {isManager && (
            <>
              <li className="nav-item mb-1">
                <NavLink
                  to="/manager/dashboard"
                  className={({ isActive }) =>
                    `nav-link ${isActive ? 'active' : 'text-dark'}`
                  }
                >
                  <i className="fa-solid fa-chart-line me-2"></i>
                  Dashboard & SLA
                </NavLink>
              </li>
              <li className="nav-item mb-1">
                <NavLink
                  to="/manager/tickets"
                  className={({ isActive }) =>
                    `nav-link ${isActive ? 'active' : 'text-dark'}`
                  }
                >
                  <i className="fa-solid fa-ticket-alt me-2"></i>
                  Toàn bộ Ticket
                </NavLink>
              </li>
              <li className="nav-item mb-1">
                <NavLink
                  to="/manager/users"
                  className={({ isActive }) =>
                    `nav-link ${isActive ? 'active' : 'text-dark'}`
                  }
                >
                  <i className="fa-solid fa-users-cog me-2"></i>
                  Quản trị tài khoản
                </NavLink>
              </li>
            </>
          )}

          {/* IT Staff Section */}
          {isStaff && (
            <li className="nav-item mb-1">
              <NavLink
                to="/staff/tickets"
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : 'text-dark'}`
                }
              >
                <i className="fa-solid fa-screwdriver-wrench me-2"></i>
                Ticket được giao
              </NavLink>
            </li>
          )}

          {/* Employee Section */}
          {(isEmployee || isStaff || isManager) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/employee/tickets"
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : 'text-dark'}`
                }
              >
                <i className="fa-solid fa-user-tag me-2"></i>
                Yêu cầu của tôi
              </NavLink>
            </li>
          )}
        </ul>
      </div>
    </div>
  );
};
