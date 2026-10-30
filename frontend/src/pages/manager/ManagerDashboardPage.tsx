import React, { useState, useEffect } from 'react';
import { reportApi } from '../../api/reportApi';
import { ChartData, ReportOverview, StaffPerformance } from '../../types';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
} from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title);

export const ManagerDashboardPage: React.FC = () => {
  const [overview, setOverview] = useState<ReportOverview | null>(null);
  const [statusData, setStatusData] = useState<ChartData[]>([]);
  const [categoryData, setCategoryData] = useState<ChartData[]>([]);
  const [staffData, setStaffData] = useState<StaffPerformance[]>([]);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const params = {
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
      };
      const [overRes, statRes, catRes, staffRes] = await Promise.all([
        reportApi.getOverview(params),
        reportApi.getByStatus(params),
        reportApi.getByCategory(params),
        reportApi.getByStaff(params),
      ]);
      setOverview(overRes.data);
      setStatusData(statRes.data);
      setCategoryData(catRes.data);
      setStaffData(staffRes.data);
    } catch {
      // Ignored
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleFilter = (e: React.FormEvent) => {
    e.preventDefault();
    fetchReports();
  };

  const doughnutChartConfig = {
    labels: statusData.map((s) => s.label),
    datasets: [
      {
        data: statusData.map((s) => s.count),
        backgroundColor: [
          '#0dcaf0', // OPEN
          '#0d6efd', // ASSIGNED
          '#ffc107', // IN_PROGRESS
          '#6c757d', // PENDING
          '#198754', // RESOLVED
          '#212529', // CLOSED
          '#dc3545', // REOPENED
        ],
      },
    ],
  };

  const barChartConfig = {
    labels: categoryData.map((c) => c.label),
    datasets: [
      {
        label: 'Số lượng sự cố',
        data: categoryData.map((c) => c.count),
        backgroundColor: '#0d6efd',
      },
    ],
  };

  return (
    <div className="p-4">
      {/* Header & Date Filter */}
      <div className="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
          <h4 className="fw-bold text-dark mb-1">
            <i className="fa-solid fa-chart-line text-primary me-2"></i>
            Báo cáo Dashboard & Vận hành SLA
          </h4>
          <p className="text-muted small mb-0">Giám sát tổng thể hoạt động Service Desk và hiệu suất kỹ thuật</p>
        </div>
        <form onSubmit={handleFilter} className="d-flex gap-2 align-items-center mt-3 mt-md-0">
          <input
            type="date"
            className="form-control form-control-sm"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
          <span className="text-muted small">đến</span>
          <input
            type="date"
            className="form-control form-control-sm"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
          <button type="submit" className="btn btn-primary btn-sm px-3">
            Lọc
          </button>
        </form>
      </div>

      {loading || !overview ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
        </div>
      ) : (
        <>
          {/* Metric Cards Row */}
          <div className="row g-3 mb-4">
            <div className="col-12 col-sm-6 col-xl-2">
              <div className="card shadow-sm border-0 border-start border-primary border-4 h-100">
                <div className="card-body p-3">
                  <div className="text-muted small fw-semibold">Tổng Ticket</div>
                  <div className="fs-3 fw-bold text-dark">{overview.totalTickets}</div>
                </div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-xl-2">
              <div className="card shadow-sm border-0 border-start border-info border-4 h-100">
                <div className="card-body p-3">
                  <div className="text-muted small fw-semibold">Mới tạo (Chờ giao)</div>
                  <div className="fs-3 fw-bold text-info">{overview.openTickets}</div>
                </div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-xl-2">
              <div className="card shadow-sm border-0 border-start border-warning border-4 h-100">
                <div className="card-body p-3">
                  <div className="text-muted small fw-semibold">Đang xử lý</div>
                  <div className="fs-3 fw-bold text-warning">{overview.inProgressTickets}</div>
                </div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-xl-2">
              <div className="card shadow-sm border-0 border-start border-success border-4 h-100">
                <div className="card-body p-3">
                  <div className="text-muted small fw-semibold">Đã hoàn thành</div>
                  <div className="fs-3 fw-bold text-success">{overview.resolvedTickets + overview.closedTickets}</div>
                </div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-xl-2">
              <div className="card shadow-sm border-0 border-start border-danger border-4 h-100">
                <div className="card-body p-3">
                  <div className="text-muted small fw-semibold">Quá hạn SLA</div>
                  <div className="fs-3 fw-bold text-danger">{overview.overdueTickets}</div>
                </div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-xl-2">
              <div className="card shadow-sm border-0 border-start border-dark border-4 h-100">
                <div className="card-body p-3">
                  <div className="text-muted small fw-semibold">Tuân thủ SLA</div>
                  <div className="fs-3 fw-bold text-dark">{overview.slaComplianceRate}%</div>
                </div>
              </div>
            </div>
          </div>

          {/* Charts Row */}
          <div className="row g-4 mb-4">
            <div className="col-lg-5">
              <div className="card shadow-sm border-0 h-100">
                <div className="card-header bg-white py-3">
                  <h6 className="fw-bold mb-0">Phân bổ Ticket theo Trạng thái</h6>
                </div>
                <div className="card-body d-flex align-items-center justify-content-center p-4">
                  <div style={{ maxWidth: '300px' }}>
                    <Doughnut data={doughnutChartConfig} options={{ responsive: true, plugins: { legend: { position: 'bottom' } } }} />
                  </div>
                </div>
              </div>
            </div>
            <div className="col-lg-7">
              <div className="card shadow-sm border-0 h-100">
                <div className="card-header bg-white py-3">
                  <h6 className="fw-bold mb-0">Phân loại sự cố theo Danh mục</h6>
                </div>
                <div className="card-body p-4">
                  <Bar data={barChartConfig} options={{ responsive: true }} />
                </div>
              </div>
            </div>
          </div>

          {/* Staff Performance Table */}
          <div className="card shadow-sm border-0">
            <div className="card-header bg-white py-3">
              <h6 className="fw-bold mb-0">
                <i className="fa-solid fa-user-check me-2 text-primary"></i>
                Đánh giá năng suất Kỹ thuật viên IT
              </h6>
            </div>
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-uppercase text-muted">
                  <tr>
                    <th>Kỹ thuật viên</th>
                    <th>Email</th>
                    <th className="text-center">Được giao</th>
                    <th className="text-center">Đã giải quyết</th>
                    <th className="text-center">Đã đóng</th>
                    <th className="text-center">Thời gian xử lý TB</th>
                    <th className="text-center">Vi phạm SLA</th>
                  </tr>
                </thead>
                <tbody>
                  {staffData.map((s) => (
                    <tr key={s.staffId}>
                      <td className="fw-semibold">{s.staffName}</td>
                      <td className="text-muted small">{s.staffEmail}</td>
                      <td className="text-center">{s.totalAssigned}</td>
                      <td className="text-center text-success fw-bold">{s.totalResolved}</td>
                      <td className="text-center">{s.totalClosed}</td>
                      <td className="text-center">
                        <span className="badge bg-light text-dark border">
                          {s.avgResolutionHours} giờ
                        </span>
                      </td>
                      <td className="text-center">
                        {s.slaBreaches > 0 ? (
                          <span className="badge bg-danger">{s.slaBreaches} lần</span>
                        ) : (
                          <span className="badge bg-success">0</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
