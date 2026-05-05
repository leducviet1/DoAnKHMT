import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button, Dropdown, Input, Modal, message } from "antd";
import {
  clearAuthSession,
  getAuthSession,
  normalizeRoles,
  updateStoredSession,
} from "../api/authApi";
import { getCurrentUser, updateCurrentUser } from "../api/userApi";
import heroImage from "../assets/hero.png";

const menu = [
  {
    name: "Trang chủ",
    path: "/",
    description: "Giới thiệu và điều hướng nhanh",
  },
  {
    name: "Dashboard",
    path: "/dashboard",
    description: "Thống kê toàn hệ thống",
    allowedRoles: ["ADMIN", "LIBRARIAN"],
  },
  {
    name: "Danh mục sách",
    path: "/categories",
    description: "Phân loại kho sách",
    allowedRoles: ["ADMIN", "LIBRARIAN"],
  },
  {
    name: "Sách",
    path: "/books",
    description: "Danh sách đầu sách",
  },
  {
    name: "Mượn trả",
    path: "/borrows",
    description: "Phiếu mượn sách",
  },
  {
    name: "Người dùng",
    path: "/users",
    description: "Tài khoản và quyền",
    allowedRoles: ["ADMIN"],
  },
  {
    name: "Tiền phạt",
    path: "/fines",
    description: "Quản lý khoản phạt",
    allowedRoles: ["ADMIN", "LIBRARIAN", "USER"],
  },
  {
    name: "Tác giả",
    path: "/authors",
    description: "Hồ sơ người viết",
    allowedRoles: ["ADMIN", "LIBRARIAN"],
  },
  {
    name: "Nhà xuất bản",
    path: "/publishers",
    description: "Quản lý đơn vị phát hành",
    allowedRoles: ["ADMIN", "LIBRARIAN"],
  },
];

function AdminLayout({ children }) {
  const location = useLocation();
  const navigate = useNavigate();
  const session = getAuthSession();
  const [profile, setProfile] = useState(null);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [profileForm, setProfileForm] = useState({
    email: "",
    phoneNumber: "",
    address: "",
    password: "",
  });
  const [savingProfile, setSavingProfile] = useState(false);

  const currentPage = menu.find((item) => item.path === location.pathname) || menu[0];

  useEffect(() => {
    let ignore = false;

    getCurrentUser()
      .then((res) => {
        if (ignore) return;

        setProfile(res.data);
        updateStoredSession({
          userId: res.data.userId,
          email: res.data.email,
          username: res.data.username,
          roles: normalizeRoles(res.data.roles),
        });
      })
      .catch((err) => {
        if (ignore) return;
        console.error(err);
      });

    return () => {
      ignore = true;
    };
  }, []);

  const handleLogout = () => {
    clearAuthSession();
    navigate("/login", { replace: true });
  };

  const handleOpenProfile = () => {
    setProfileForm({
      email: profile?.email || session?.email || "",
      phoneNumber: profile?.phoneNumber || "",
      address: profile?.address || "",
      password: "",
    });
    setIsProfileOpen(true);
  };

  const handleCloseProfile = () => {
    setIsProfileOpen(false);
  };

  const handleProfileChange = (field, value) => {
    setProfileForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleSaveProfile = async () => {
    if (!profileForm.email.trim() || !profileForm.phoneNumber.trim()) {
      message.warning("Vui lòng nhập email và số điện thoại");
      return;
    }

    try {
      setSavingProfile(true);

      const payload = {
        email: profileForm.email.trim(),
        phoneNumber: profileForm.phoneNumber.trim(),
        address: profileForm.address.trim(),
      };

      if (profileForm.password) {
        payload.password = profileForm.password;
      }

      const res = await updateCurrentUser(payload);
      setProfile(res.data.user);
      updateStoredSession({
        token: res.data.token,
        tokenType: res.data.tokenType,
        userId: res.data.user.userId,
        email: res.data.user.email,
        username: res.data.user.username,
        roles: normalizeRoles(res.data.user.roles),
      });
      message.success("Cập nhật thông tin thành công");
      setIsProfileOpen(false);
    } catch (err) {
      console.error(err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.response?.data ||
        "Cập nhật thông tin thất bại";
      message.error(String(errorMessage));
    } finally {
      setSavingProfile(false);
    }
  };

  const profileMenu = {
    items: [
      {
        key: "profile",
        label: "Xem thông tin",
        onClick: handleOpenProfile,
      },
      {
        type: "divider",
      },
      {
        key: "logout",
        label: "Đăng xuất",
        onClick: handleLogout,
      },
    ],
  };

  const displayName = profile?.username || session?.username || session?.email || "Tài khoản";
  const currentRoles = normalizeRoles(profile?.roles || session?.roles || []);
  const visibleMenu = menu.filter((item) => {
    if (!item.allowedRoles) return true;
    return currentRoles.some((role) => normalizeRoles(item.allowedRoles).includes(role));
  });

  return (
    <div className="app-layout">
      <aside className="app-sidebar">
        <Link to="/" className="brand">
          <span className="brand-mark">LB</span>
          <span>
            <strong>Library</strong>
            <small>Management</small>
          </span>
        </Link>

        <nav className="sidebar-nav" aria-label="Điều hướng quản trị">
          <span className="nav-label">Quản trị</span>
          {visibleMenu.map((item) => {
            const isActive = location.pathname === item.path;

            return (
              <Link
                key={item.path}
                to={item.path}
                className={`nav-item ${isActive ? "is-active" : ""}`}
              >
                <span>{item.name}</span>
                <small>{item.description}</small>
              </Link>
            );
          })}
        </nav>

        <div className="sidebar-library">
          <img src={heroImage} alt="Không gian đọc sách" />
          <div>
            <strong>Kho sách nội bộ</strong>
            <span>Sắp xếp dữ liệu gọn hơn cho từng nghiệp vụ thư viện.</span>
          </div>
        </div>
      </aside>

      <div className="app-main">
        <header className="app-header">
          <div>
            <span className="eyebrow">Bảng điều khiển</span>
            <h1>{currentPage.name}</h1>
          </div>

          <div className="header-actions">
            <Dropdown menu={profileMenu} trigger={["click"]} placement="bottomRight">
              <button type="button" className="profile-trigger">
                <span className="profile-trigger__name">{displayName}</span>
                <span className="profile-trigger__meta">
                  {profile?.email || session?.email || "Tài khoản hệ thống"}
                </span>
              </button>
            </Dropdown>
          </div>
        </header>

        <main className="app-content">{children}</main>
      </div>

      <Modal
        title="Thông tin tài khoản"
        open={isProfileOpen}
        onCancel={handleCloseProfile}
        footer={null}
        destroyOnHidden
      >
        <div className="modal-fieldset">
          <div className="profile-summary">
            <strong>{displayName}</strong>
            <span>Mã người dùng: {profile?.userId || session?.userId || "Chưa có"}</span>
          </div>

          <label>
            <span>Email</span>
            <Input
              type="email"
              value={profileForm.email}
              onChange={(e) => handleProfileChange("email", e.target.value)}
            />
          </label>

          <label>
            <span>Số điện thoại</span>
            <Input
              value={profileForm.phoneNumber}
              onChange={(e) => handleProfileChange("phoneNumber", e.target.value)}
            />
          </label>

          <label>
            <span>Địa chỉ</span>
            <Input
              value={profileForm.address}
              onChange={(e) => handleProfileChange("address", e.target.value)}
            />
          </label>

          <label>
            <span>Mật khẩu mới</span>
            <Input.Password
              placeholder="Để trống nếu không đổi"
              value={profileForm.password}
              onChange={(e) => handleProfileChange("password", e.target.value)}
            />
          </label>

          <div className="modal-actions">
            <Button type="primary" loading={savingProfile} onClick={handleSaveProfile}>
              Lưu thay đổi
            </Button>
            <Button onClick={handleCloseProfile}>Đóng</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default AdminLayout;
