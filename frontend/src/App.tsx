import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import { Navbar } from './components/common/Navbar';
import { Sidebar } from './components/common/Sidebar';
import { LoginPage } from './pages/auth/LoginPage';
import { EmployeePortalPage } from './pages/employee/EmployeePortalPage';
import { StaffPortalPage } from './pages/staff/StaffPortalPage';
import { ManagerDashboardPage } from './pages/manager/ManagerDashboardPage';
import { ManagerTicketsPage } from './pages/manager/ManagerTicketsPage';
import { UserManagementPage } from './pages/manager/UserManagementPage';
import { TicketDetailPage } from './pages/ticket/TicketDetailPage';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <div className="d-flex flex-column min-vh-100">
      <Navbar />
      <div className="d-flex flex-grow-1">
        <Sidebar />
        <main className="flex-grow-1 bg-light">{children}</main>
      </div>
    </div>
  );
};

const HomeRedirect: React.FC = () => {
  const { isManager, isStaff } = useAuth();
  if (isManager) return <Navigate to="/manager/dashboard" replace />;
  if (isStaff) return <Navigate to="/staff/tickets" replace />;
  return <Navigate to="/employee/tickets" replace />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout>
                  <HomeRedirect />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/employee/tickets"
            element={
              <ProtectedRoute>
                <Layout>
                  <EmployeePortalPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/staff/tickets"
            element={
              <ProtectedRoute allowedRoles={['ROLE_IT_STAFF', 'ROLE_IT_MANAGER']}>
                <Layout>
                  <StaffPortalPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/manager/dashboard"
            element={
              <ProtectedRoute allowedRoles={['ROLE_IT_MANAGER']}>
                <Layout>
                  <ManagerDashboardPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/manager/tickets"
            element={
              <ProtectedRoute allowedRoles={['ROLE_IT_MANAGER']}>
                <Layout>
                  <ManagerTicketsPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/manager/users"
            element={
              <ProtectedRoute allowedRoles={['ROLE_IT_MANAGER']}>
                <Layout>
                  <UserManagementPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/tickets/:id"
            element={
              <ProtectedRoute>
                <Layout>
                  <TicketDetailPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
