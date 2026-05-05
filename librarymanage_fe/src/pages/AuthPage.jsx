import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { Button, Input, message } from "antd";
import {
  getAuthSession,
  login,
  normalizeRoles,
  register,
  saveAuthSession,
  updateStoredSession,
} from "../api/authApi";
import heroImage from "../assets/hero.png";
import { getCurrentUser } from "../api/userApi";

const initialLoginForm = {
  email: "",
  password: "",
};

const initialRegisterForm = {
  username: "",
  email: "",
  phoneNumber: "",
  password: "",
};

function AuthPage({ mode = "login" }) {
  const navigate = useNavigate();
  const location = useLocation();
  const session = getAuthSession();
  const [loginForm, setLoginForm] = useState({
    ...initialLoginForm,
    email: location.state?.registeredEmail || "",
  });
  const [registerForm, setRegisterForm] = useState(initialRegisterForm);
  const [loading, setLoading] = useState(false);

  const isLogin = mode === "login";
  const from = location.state?.from?.pathname || "/";

  if (session?.token) {
    return <Navigate to={from} replace />;
  }

  const handleLoginChange = (field, value) => {
    setLoginForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleRegisterChange = (field, value) => {
    setRegisterForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const getErrorMessage = (err, fallback) =>
    err.response?.data?.message ||
    err.response?.data?.error ||
    err.response?.data ||
    fallback;

  const handleLogin = async (event) => {
    event.preventDefault();

    if (!loginForm.email.trim() || !loginForm.password) {
      message.warning("Vui lòng nhập email và mật khẩu");
      return;
    }

    try {
      setLoading(true);
      const res = await login({
        email: loginForm.email.trim(),
        password: loginForm.password,
      });

      saveAuthSession(res.data);
      const currentUserRes = await getCurrentUser();
      updateStoredSession({
        userId: currentUserRes.data.userId,
        email: currentUserRes.data.email,
        username: currentUserRes.data.username,
        roles: normalizeRoles(currentUserRes.data.roles),
      });
      message.success("Đăng nhập thành công");
      navigate(from, { replace: true });
    } catch (err) {
      console.error(err);
      message.error(String(getErrorMessage(err, "Đăng nhập thất bại")));
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (event) => {
    event.preventDefault();

    if (
      !registerForm.username.trim() ||
      !registerForm.email.trim() ||
      !registerForm.phoneNumber.trim() ||
      !registerForm.password
    ) {
      message.warning("Vui lòng nhập đầy đủ thông tin đăng ký");
      return;
    }

    try {
      setLoading(true);
      await register({
        username: registerForm.username.trim(),
        email: registerForm.email.trim(),
        phoneNumber: registerForm.phoneNumber.trim(),
        password: registerForm.password,
      });

      message.success("Đăng ký thành công, vui lòng đăng nhập lại");
      navigate("/login", {
        replace: true,
        state: {
          registeredEmail: registerForm.email.trim(),
        },
      });
    } catch (err) {
      console.error(err);
      message.error(String(getErrorMessage(err, "Đăng ký thất bại")));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-shell">
        <div className="auth-visual">
          <img src={heroImage} alt="Không gian thư viện" />
          <div>
            <span className="eyebrow">Library Admin</span>
            <h1>Quản lý thư viện an toàn hơn với JWT</h1>
            <p>Theo dõi sách, tác giả, nhà xuất bản và phiếu mượn trong một khu vực quản trị.</p>
          </div>
        </div>

        <div className="auth-panel">
          <div className="auth-tabs">
            <Link className={isLogin ? "is-active" : ""} to="/login">
              Đăng nhập
            </Link>
            <Link className={!isLogin ? "is-active" : ""} to="/register">
              Đăng ký
            </Link>
          </div>

          {isLogin ? (
            <form className="auth-form" onSubmit={handleLogin}>
              <div>
                <span className="eyebrow">Tài khoản admin</span>
                <h2>Đăng nhập</h2>
                <p>Dùng email và mật khẩu để vào hệ thống quản trị.</p>
              </div>

              <label>
                <span>Email</span>
                <Input
                  type="email"
                  placeholder="admin@example.com"
                  value={loginForm.email}
                  onChange={(e) => handleLoginChange("email", e.target.value)}
                />
              </label>

              <label>
                <span>Mật khẩu</span>
                <Input.Password
                  placeholder="Nhập mật khẩu"
                  value={loginForm.password}
                  onChange={(e) => handleLoginChange("password", e.target.value)}
                />
              </label>

              <Button type="primary" htmlType="submit" loading={loading} block>
                Đăng nhập
              </Button>
            </form>
          ) : (
            <form className="auth-form" onSubmit={handleRegister}>
              <div>
                <span className="eyebrow">Tạo tài khoản</span>
                <h2>Đăng ký</h2>
                <p>Thông tin này sẽ dùng để tạo tài khoản truy cập hệ thống.</p>
              </div>

              <label>
                <span>Tên người dùng</span>
                <Input
                  placeholder="Nhập tên người dùng"
                  value={registerForm.username}
                  onChange={(e) => handleRegisterChange("username", e.target.value)}
                />
              </label>

              <label>
                <span>Email</span>
                <Input
                  type="email"
                  placeholder="user@example.com"
                  value={registerForm.email}
                  onChange={(e) => handleRegisterChange("email", e.target.value)}
                />
              </label>

              <label>
                <span>Số điện thoại</span>
                <Input
                  placeholder="Nhập số điện thoại"
                  value={registerForm.phoneNumber}
                  onChange={(e) => handleRegisterChange("phoneNumber", e.target.value)}
                />
              </label>

              <label>
                <span>Mật khẩu</span>
                <Input.Password
                  placeholder="Nhập mật khẩu"
                  value={registerForm.password}
                  onChange={(e) => handleRegisterChange("password", e.target.value)}
                />
              </label>

              <Button type="primary" htmlType="submit" loading={loading} block>
                Đăng ký
              </Button>
            </form>
          )}
        </div>
      </section>
    </main>
  );
}

export default AuthPage;
