import { useEffect, useMemo, useState } from "react";
import { Button, Input, Modal, Select, Space, Table, Tag, message } from "antd";
import { getUsers, updateUserByAdmin } from "../api/userApi";

const defaultRoles = ["USER", "LIBRARIAN", "ADMIN"];

const initialFormValue = {
  username: "",
  email: "",
  phoneNumber: "",
  address: "",
  password: "",
  roles: [],
};

function UserPage() {
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState(null);
  const [formValue, setFormValue] = useState(initialFormValue);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [saving, setSaving] = useState(false);

  const fetchUsers = async () => {
    try {
      const res = await getUsers(0, 200);
      setUsers(res.data.content || []);
    } catch (err) {
      console.error(err);
      message.error("Không tải được danh sách người dùng");
    }
  };

  useEffect(() => {
    let ignore = false;

    getUsers(0, 200)
      .then((res) => {
        if (!ignore) {
          setUsers(res.data.content || []);
        }
      })
      .catch((err) => {
        if (!ignore) {
          console.error(err);
          message.error("Không tải được danh sách người dùng");
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  const filteredUsers = useMemo(() => {
    const keyword = search.trim().toLowerCase();

    if (!keyword) return users;

    return users.filter((user) => {
      const username = user.username?.toLowerCase() || "";
      const email = user.email?.toLowerCase() || "";
      return username.includes(keyword) || email.includes(keyword);
    });
  }, [search, users]);

  const handleOpenEdit = (user) => {
    setSelectedUser(user);
    setFormValue({
      username: user.username || "",
      email: user.email || "",
      phoneNumber: user.phoneNumber || "",
      address: user.address || "",
      password: "",
      roles: Array.from(user.roles || []),
    });
    setIsEditOpen(true);
  };

  const handleCloseEdit = () => {
    setSelectedUser(null);
    setFormValue(initialFormValue);
    setIsEditOpen(false);
  };

  const handleFormChange = (field, value) => {
    setFormValue((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleSaveUser = async () => {
    if (!selectedUser) return;

    if (!formValue.username.trim() || !formValue.email.trim()) {
      message.warning("Vui lòng nhập tên người dùng và email");
      return;
    }

    try {
      setSaving(true);
      const payload = {
        username: formValue.username.trim(),
        email: formValue.email.trim(),
        phoneNumber: formValue.phoneNumber.trim(),
        address: formValue.address.trim(),
        roles: formValue.roles.map((role) => role.trim().toUpperCase()).filter(Boolean),
      };

      if (formValue.password) {
        payload.password = formValue.password;
      }

      await updateUserByAdmin(selectedUser.userId, payload);
      message.success("Cập nhật người dùng thành công");
      handleCloseEdit();
      fetchUsers();
    } catch (err) {
      console.error(err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.response?.data ||
        "Cập nhật người dùng thất bại";
      message.error(String(errorMessage));
    } finally {
      setSaving(false);
    }
  };

  const columns = [
    {
      title: "Mã",
      dataIndex: "userId",
      width: 90,
    },
    {
      title: "Tên người dùng",
      dataIndex: "username",
      width: 190,
    },
    {
      title: "Email",
      dataIndex: "email",
      width: 240,
    },
    {
      title: "Số điện thoại",
      dataIndex: "phoneNumber",
      width: 150,
      render: (phoneNumber) => phoneNumber || "Chưa có",
    },
    {
      title: "Địa chỉ",
      dataIndex: "address",
      render: (address) => address || "Chưa có",
    },
    {
      title: "Quyền",
      dataIndex: "roles",
      width: 200,
      render: (roles) => (
        <div className="author-chip-list">
          {Array.from(roles || []).map((role) => (
            <Tag key={role} color={role === "ADMIN" ? "green" : "blue"}>
              {role}
            </Tag>
          ))}
        </div>
      ),
    },
    {
      title: "Hành động",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="primary" onClick={() => handleOpenEdit(record)}>
            Sửa
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <section className="page-shell">
      <div className="page-hero">
        <div>
          <span className="eyebrow">Quản trị tài khoản</span>
          <h2>Quản lý user</h2>
          <p>Tìm kiếm người dùng theo tên hoặc email, đồng thời chỉnh sửa thông tin và quyền truy cập.</p>
        </div>
      </div>

      <div className="metric-row">
        <div className="metric-card">
          <strong>{users.length}</strong>
          <span>Tổng user</span>
        </div>
        <div className="metric-card">
          <strong>{filteredUsers.length}</strong>
          <span>Kết quả tìm kiếm</span>
        </div>
        <div className="metric-card">
          <strong>{users.filter((user) => Array.from(user.roles || []).includes("ADMIN")).length}</strong>
          <span>Tài khoản admin</span>
        </div>
      </div>

      <div className="content-panel">
        <div className="panel-heading">
          <div>
            <h3>Danh sách người dùng</h3>
            <p>Tìm nhanh theo tên người dùng hoặc Gmail.</p>
          </div>
        </div>

        <div className="search-panel user-search-panel">
          <Input
            placeholder="Tìm theo tên người dùng hoặc Gmail"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <Table
          className="book-table"
          dataSource={filteredUsers}
          columns={columns}
          rowKey="userId"
          scroll={{ x: 1200 }}
          pagination={{
            pageSize: 10,
          }}
        />
      </div>

      <Modal
        title={`Cập nhật user #${selectedUser?.userId || ""}`}
        open={isEditOpen}
        onCancel={handleCloseEdit}
        footer={null}
        width={760}
        destroyOnHidden
      >
        <div className="book-form-grid user-form-grid">
          <label>
            <span>Tên người dùng</span>
            <Input
              value={formValue.username}
              onChange={(e) => handleFormChange("username", e.target.value)}
            />
          </label>

          <label>
            <span>Email</span>
            <Input
              type="email"
              value={formValue.email}
              onChange={(e) => handleFormChange("email", e.target.value)}
            />
          </label>

          <label>
            <span>Số điện thoại</span>
            <Input
              value={formValue.phoneNumber}
              onChange={(e) => handleFormChange("phoneNumber", e.target.value)}
            />
          </label>

          <label>
            <span>Mật khẩu mới</span>
            <Input.Password
              placeholder="Để trống nếu không đổi"
              value={formValue.password}
              onChange={(e) => handleFormChange("password", e.target.value)}
            />
          </label>

          <label className="wide-field">
            <span>Địa chỉ</span>
            <Input
              value={formValue.address}
              onChange={(e) => handleFormChange("address", e.target.value)}
            />
          </label>

          <label className="wide-field">
            <span>Quyền</span>
            <Select
              mode="tags"
              value={formValue.roles}
              onChange={(value) => handleFormChange("roles", value)}
              tokenSeparators={[",", " "]}
              options={defaultRoles.map((role) => ({
                value: role,
                label: role,
              }))}
            />
          </label>

          <div className="modal-actions wide-field">
            <Button type="primary" loading={saving} onClick={handleSaveUser}>
              Lưu thay đổi
            </Button>
            <Button onClick={handleCloseEdit}>Hủy</Button>
          </div>
        </div>
      </Modal>
    </section>
  );
}

export default UserPage;
