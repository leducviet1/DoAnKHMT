import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button, Dropdown, Input, Modal, message } from "antd";
import { clearAuthSession, getAuthSession, updateStoredSession } from "../api/authApi";
import { getCurrentUser, updateCurrentUser } from "../api/userApi";

const userMenu = [
  { name: "Trang chủ", path: "/" },
  { name: "Sách", path: "/books" },
  { name: "Mượn trả", path: "/borrows" },
  { name: "Tiền phạt", path: "/fines" },
];

function UserLayout({ children }) {
  const location = useLocation();
  const navigate = useNavigate();
  const session = getAuthSession();
  const [profile, setProfile] = useState(null);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileForm, setProfileForm] = useState({
    email: "",
    phoneNumber: "",
    address: "",
    password: "",
  });

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
          roles: res.data.roles,
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

  const displayName = profile?.username || session?.username || session?.email || "Tài khoản";

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
        roles: res.data.user.roles,
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

  return (
    <div className="user-layout">
      <header className="user-topbar">
        <Link to="/" className="user-brand">
          <span className="brand-mark">LB</span>
          <span>
            <strong>Library</strong>
            <small>Không gian đọc của bạn</small>
          </span>
        </Link>

        <nav className="user-nav" aria-label="Điều hướng người dùng">
          {userMenu.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={location.pathname === item.path ? "is-active" : ""}
            >
              {item.name}
            </Link>
          ))}
        </nav>

        <Dropdown menu={profileMenu} trigger={["click"]} placement="bottomRight">
          <button type="button" className="user-profile">
            <span>{displayName}</span>
            <small>{profile?.email || session?.email || "Tài khoản thư viện"}</small>
          </button>
        </Dropdown>
      </header>

      <main className="user-content">{children}</main>

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

export default UserLayout;
