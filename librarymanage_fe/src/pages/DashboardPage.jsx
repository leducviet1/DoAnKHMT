import { useEffect, useMemo, useState } from "react";
import { Button, DatePicker, Empty, Select, Spin, Table, message } from "antd";
import { getDashboardStatistics } from "../api/statisticsApi";

const { RangePicker } = DatePicker;

const granularityOptions = [
  { value: "DAY", label: "Theo ngày" },
  { value: "MONTH", label: "Theo tháng" },
];

const initialStatistics = {
  totalTitles: 0,
  totalUsers: 0,
  totalBorrowRecords: 0,
  currentlyBorrowedBooks: 0,
  currentlyBorrowingRecords: 0,
  overdueBorrowItems: 0,
  pendingFines: 0,
  paidFines: 0,
  totalBookCopies: 0,
  availableBookCopies: 0,
  fineRevenue: 0,
  pendingFineAmount: 0,
  topBorrowedBooks: [],
  topBorrowingUsers: [],
  chart: [],
};

const toCurrency = (value) => `${Number(value || 0).toLocaleString("vi-VN")} đ`;

const getChartPeak = (chart) =>
  chart.reduce((max, item) => {
    const values = [
      Number(item.borrowCount || 0),
      Number(item.borrowedBookQuantity || 0),
      Number(item.fineRevenue || 0),
      Number(item.pendingFineAmount || 0),
    ];
    return Math.max(max, ...values);
  }, 0);

function DashboardPage() {
  const [statistics, setStatistics] = useState(initialStatistics);
  const [loading, setLoading] = useState(true);
  const [granularity, setGranularity] = useState("DAY");
  const [rangeValue, setRangeValue] = useState([]);

  const fetchStatistics = async (filters = {}) => {
    try {
      setLoading(true);
      const res = await getDashboardStatistics(filters);
      setStatistics({
        ...initialStatistics,
        ...res.data,
      });
    } catch (err) {
      console.error(err);
      message.error("Không tải được dữ liệu thống kê");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatistics({ granularity });
  }, [granularity]);

  const handleApplyFilters = () => {
    const [startDate, endDate] = rangeValue || [];

    fetchStatistics({
      granularity,
      startDate: startDate ? startDate.format("YYYY-MM-DD") : undefined,
      endDate: endDate ? endDate.format("YYYY-MM-DD") : undefined,
    });
  };

  const handleResetFilters = () => {
    setRangeValue([]);
    fetchStatistics({ granularity });
  };

  const chartPeak = useMemo(() => getChartPeak(statistics.chart || []), [statistics.chart]);

  const topBookColumns = [
    {
      title: "Mã sách",
      dataIndex: "bookId",
      width: 110,
      render: (bookId) => <strong>#{bookId}</strong>,
    },
    {
      title: "Tên sách",
      dataIndex: "title",
    },
    {
      title: "Lượt mượn",
      dataIndex: "borrowedQuantity",
      width: 140,
    },
  ];

  const topUserColumns = [
    {
      title: "Mã user",
      dataIndex: "userId",
      width: 110,
      render: (userId) => <strong>#{userId}</strong>,
    },
    {
      title: "Tên người mượn",
      dataIndex: "username",
    },
    {
      title: "Email",
      dataIndex: "email",
    },
    {
      title: "Số sách đã mượn",
      dataIndex: "borrowedQuantity",
      width: 160,
    },
  ];

  return (
    <section className="page-shell">
      <div className="page-hero dashboard-hero">
        <div>
          <span className="eyebrow">Dashboard</span>
          <h2>Thống kê thư viện</h2>
          <p>
            Theo dõi hoạt động mượn trả, sách được quan tâm nhiều nhất, người mượn
            nhiều nhất và doanh thu tiền phạt theo thời gian.
          </p>
        </div>
        <div className="dashboard-hero__badge">
          <span>Phạm vi</span>
          <strong>ADMIN / THỦ THƯ</strong>
        </div>
      </div>

      <div className="content-panel dashboard-filter-panel">
        <div className="panel-heading">
          <div>
            <h3>Bộ lọc thống kê</h3>
            <p>Chọn khoảng thời gian và độ chi tiết theo ngày hoặc theo tháng.</p>
          </div>
        </div>

        <div className="dashboard-filter-bar">
          <RangePicker value={rangeValue} onChange={setRangeValue} format="DD/MM/YYYY" />
          <Select
            value={granularity}
            onChange={setGranularity}
            options={granularityOptions}
          />
          <Button type="primary" onClick={handleApplyFilters}>
            Áp dụng
          </Button>
          <Button onClick={handleResetFilters}>Đặt lại</Button>
        </div>
      </div>

      {loading ? (
        <div className="dashboard-loading">
          <Spin size="large" />
        </div>
      ) : (
        <>
          <div className="metric-row dashboard-metric-row">
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.totalTitles}</strong>
              <span>Tổng đầu sách</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.totalUsers}</strong>
              <span>Tổng người dùng</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.totalBorrowRecords}</strong>
              <span>Phiếu mượn trong kỳ</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.currentlyBorrowedBooks}</strong>
              <span>Sách đang mượn</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.currentlyBorrowingRecords}</strong>
              <span>Phiếu đang mượn</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.overdueBorrowItems}</strong>
              <span>Sách quá hạn</span>
            </div>
            <div className="metric-card dashboard-metric-card dashboard-metric-card--money">
              <strong>{toCurrency(statistics.fineRevenue)}</strong>
              <span>Doanh thu tiền phạt</span>
            </div>
            <div className="metric-card dashboard-metric-card dashboard-metric-card--money">
              <strong>{toCurrency(statistics.pendingFineAmount)}</strong>
              <span>Tiền phạt chờ thu</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.totalBookCopies}</strong>
              <span>Tổng bản sách</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.availableBookCopies}</strong>
              <span>Bản còn sẵn</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.pendingFines}</strong>
              <span>Khoản phạt chờ thu</span>
            </div>
            <div className="metric-card dashboard-metric-card">
              <strong>{statistics.paidFines}</strong>
              <span>Khoản phạt đã thu</span>
            </div>
          </div>

          <div className="dashboard-grid">
            <div className="content-panel">
              <div className="panel-heading">
                <div>
                  <h3>Biểu đồ hoạt động</h3>
                  <p>
                    Mượn sách, số lượng sách mượn, doanh thu tiền phạt và tiền phạt
                    chờ thu theo {granularity === "DAY" ? "ngày" : "tháng"}.
                  </p>
                </div>
              </div>

              {statistics.chart?.length ? (
                <div className="dashboard-chart">
                  {statistics.chart.map((point) => (
                    <div key={point.label} className="dashboard-chart__row">
                      <div className="dashboard-chart__label">{point.label}</div>
                      <div className="dashboard-chart__bars">
                        <div className="dashboard-chart__bar-group">
                          <span>Mượn</span>
                          <div className="dashboard-chart__track">
                            <div
                              className="dashboard-chart__bar dashboard-chart__bar--borrow"
                              style={{
                                width: `${chartPeak ? (Number(point.borrowCount || 0) / chartPeak) * 100 : 0}%`,
                              }}
                            />
                          </div>
                          <strong>{point.borrowCount}</strong>
                        </div>

                        <div className="dashboard-chart__bar-group">
                          <span>Sách</span>
                          <div className="dashboard-chart__track">
                            <div
                              className="dashboard-chart__bar dashboard-chart__bar--quantity"
                              style={{
                                width: `${chartPeak ? (Number(point.borrowedBookQuantity || 0) / chartPeak) * 100 : 0}%`,
                              }}
                            />
                          </div>
                          <strong>{point.borrowedBookQuantity}</strong>
                        </div>

                        <div className="dashboard-chart__bar-group">
                          <span>Thu</span>
                          <div className="dashboard-chart__track">
                            <div
                              className="dashboard-chart__bar dashboard-chart__bar--revenue"
                              style={{
                                width: `${chartPeak ? (Number(point.fineRevenue || 0) / chartPeak) * 100 : 0}%`,
                              }}
                            />
                          </div>
                          <strong>{toCurrency(point.fineRevenue)}</strong>
                        </div>

                        <div className="dashboard-chart__bar-group">
                          <span>Chờ thu</span>
                          <div className="dashboard-chart__track">
                            <div
                              className="dashboard-chart__bar dashboard-chart__bar--pending"
                              style={{
                                width: `${chartPeak ? (Number(point.pendingFineAmount || 0) / chartPeak) * 100 : 0}%`,
                              }}
                            />
                          </div>
                          <strong>{toCurrency(point.pendingFineAmount)}</strong>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <Empty description="Chưa có dữ liệu thống kê trong khoảng thời gian này" />
              )}
            </div>

            <div className="content-panel">
              <div className="panel-heading">
                <div>
                  <h3>Sách mượn nhiều nhất</h3>
                  <p>Top đầu sách được mượn nhiều nhất trong khoảng thời gian đã chọn.</p>
                </div>
              </div>
              <Table
                dataSource={statistics.topBorrowedBooks || []}
                columns={topBookColumns}
                rowKey="bookId"
                pagination={false}
                locale={{ emptyText: "Chưa có dữ liệu" }}
              />
            </div>
          </div>

          <div className="content-panel">
            <div className="panel-heading">
              <div>
                <h3>Người mượn nhiều nhất</h3>
                <p>Top người dùng có số lượng sách mượn nhiều nhất trong kỳ thống kê.</p>
              </div>
            </div>
            <Table
              dataSource={statistics.topBorrowingUsers || []}
              columns={topUserColumns}
              rowKey="userId"
              pagination={false}
              locale={{ emptyText: "Chưa có dữ liệu" }}
            />
          </div>
        </>
      )}
    </section>
  );
}

export default DashboardPage;
