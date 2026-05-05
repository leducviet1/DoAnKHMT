import { Link } from "react-router-dom";
import { getAuthSession, normalizeRoles } from "../api/authApi";

const adminRoles = ["ADMIN", "LIBRARIAN"];

function HomePage() {
  const session = getAuthSession();
  const roles = normalizeRoles(session?.roles);
  const canViewDashboard = roles.some((role) => adminRoles.includes(role));

  const quickLinks = [
    {
      title: "Sách",
      description: "Tra cứu đầu sách, tình trạng còn lại và thông tin xuất bản.",
      path: "/books",
    },
    {
      title: "Mượn trả",
      description: "Theo dõi phiếu mượn, hạn trả và lịch sử lưu thông sách.",
      path: "/borrows",
    },
    {
      title: "Tiền phạt",
      description: "Xem khoản phạt đang chờ xử lý hoặc đã thanh toán.",
      path: "/fines",
    },
    ...(canViewDashboard
      ? [
          {
            title: "Dashboard",
            description: "Theo dõi thống kê theo ngày, tháng và hiệu quả vận hành thư viện.",
            path: "/dashboard",
          },
        ]
      : []),
  ];

  return (
    <section className="page-shell">
      <div className="home-hero">
        <div className="home-hero__content">
          <span className="eyebrow">Library Manage</span>
          <h1>Không gian quản lý và tra cứu thư viện trong một nơi.</h1>
          <p>
            Hệ thống giúp theo dõi sách, mượn trả, tiền phạt và dữ liệu vận hành cơ bản.
            Bạn có thể đi đến bất kỳ khu vực nào từ đây mà không cần quay lại menu cũ.
          </p>

          <div className="home-hero__actions">
            <Link to="/books" className="btn btn-primary">
              Vào danh sách sách
            </Link>
            <Link to="/borrows" className="btn btn-secondary">
              Xem mượn trả
            </Link>
          </div>
        </div>

        <div className="home-hero__stats">
          <div className="home-stat-card">
            <strong>Tra cứu nhanh</strong>
            <span>Sách, mượn trả, tiền phạt đều có thể truy cập trực tiếp.</span>
          </div>
          <div className="home-stat-card">
            <strong>Quyền rõ ràng</strong>
            <span>Admin, thủ thư và người dùng thấy đúng phần việc của mình.</span>
          </div>
          <div className="home-stat-card">
            <strong>Dùng hằng ngày</strong>
            <span>Giao diện gọn, bám sát nghiệp vụ thư viện đang vận hành.</span>
          </div>
        </div>
      </div>

      <div className="home-link-grid">
        {quickLinks.map((link) => (
          <Link className="home-link-card" key={link.path} to={link.path}>
            <strong>{link.title}</strong>
            <span>{link.description}</span>
            <small>Mở trang</small>
          </Link>
        ))}
      </div>
    </section>
  );
}

export default HomePage;
