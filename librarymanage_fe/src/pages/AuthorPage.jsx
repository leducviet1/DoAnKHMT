import { useEffect, useState } from "react";
import {
  getAuthors,
  createAuthor,
  updateAuthor,
  deleteAuthor,
} from "../api/authorApi";
import { Button, Input, Modal, Popconfirm, Space, Table, message } from "antd";

const { TextArea } = Input;

function AuthorPage() {
  const [authors, setAuthors] = useState([]);
  const [formValue, setFormValue] = useState({
    authorName: "",
    description: "",
  });
  const [selected, setSelected] = useState(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);

  const fetchAuthors = async (pageIndex = page) => {
    try {
      const res = await getAuthors(pageIndex, 5);

      const formatted = res.data.content.map((author) => ({
        id: author.authorId,
        authorName: author.authorName,
        description: author.description || "",
      }));

      setAuthors(formatted);
      setTotal(res.data.totalElements);
    } catch (err) {
      console.error(err);
      message.error("Không tải được danh sách tác giả");
    }
  };

  useEffect(() => {
    let ignore = false;

    getAuthors(page, 5)
      .then((res) => {
        if (ignore) return;

        const formatted = res.data.content.map((author) => ({
          id: author.authorId,
          authorName: author.authorName,
          description: author.description || "",
        }));

        setAuthors(formatted);
        setTotal(res.data.totalElements);
      })
      .catch((err) => {
        if (ignore) return;
        console.error(err);
        message.error("Không tải được danh sách tác giả");
      });

    return () => {
      ignore = true;
    };
  }, [page]);

  const handleChange = (field, value) => {
    setFormValue((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleOpenCreate = () => {
    setSelected(null);
    setFormValue({
      authorName: "",
      description: "",
    });
    setIsFormOpen(true);
  };

  const handleOpenEdit = (record) => {
    setSelected(record);
    setFormValue({
      authorName: record.authorName,
      description: record.description || "",
    });
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setSelected(null);
    setFormValue({
      authorName: "",
      description: "",
    });
    setIsFormOpen(false);
  };

  const handleSubmit = async () => {
    const payload = {
      authorName: formValue.authorName.trim(),
      description: formValue.description.trim(),
    };

    if (!payload.authorName) {
      message.warning("Vui lòng nhập tên tác giả");
      return;
    }

    try {
      if (selected) {
        await updateAuthor(selected.id, payload);
        message.success("Cập nhật tác giả thành công");
      } else {
        await createAuthor(payload);
        message.success("Thêm tác giả thành công");
      }

      handleCloseForm();
      fetchAuthors();
    } catch (err) {
      console.error(err);
      message.error("Thao tác thất bại");
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteAuthor(id);
      message.success("Xóa tác giả thành công");
      fetchAuthors();
    } catch (err) {
      console.error(err);
      message.error("Xóa tác giả thất bại");
    }
  };

  const columns = [
    {
      title: "Mã",
      dataIndex: "id",
      width: 120,
    },
    {
      title: "Tên tác giả",
      dataIndex: "authorName",
      width: 240,
    },
    {
      title: "Mô tả",
      dataIndex: "description",
      render: (description) => description || "Chưa có mô tả",
    },
    {
      title: "Hành động",
      width: 220,
      render: (_, record) => (
        <Space>
          <Button type="primary" onClick={() => handleOpenEdit(record)}>
            Sửa
          </Button>

          <Popconfirm
            title="Xóa tác giả"
            description="Bạn có chắc chắn muốn xóa tác giả này?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button danger>Xóa</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <section className="page-shell">
      <div className="page-hero">
        <div>
          <span className="eyebrow">Hồ sơ tác giả</span>
          <h2>Quản lý tác giả</h2>
          <p>
            Lưu tên và mô tả tác giả để gắn với sách trong hệ thống thư viện.
          </p>
        </div>
      </div>

      <div className="metric-row">
        <div className="metric-card">
          <strong>{total}</strong>
          <span>Tổng tác giả</span>
        </div>
        <div className="metric-card">
          <strong>{authors.length}</strong>
          <span>Đang hiển thị</span>
        </div>
        <div className="metric-card">
          <strong>{page + 1}</strong>
          <span>Trang hiện tại</span>
        </div>
      </div>

      <div className="content-panel">
        <div className="panel-heading">
          <div>
            <h3>Danh sách tác giả</h3>
            <p>Theo dõi và cập nhật thông tin tác giả.</p>
          </div>
          <Button type="primary" onClick={handleOpenCreate}>
            Thêm mới
          </Button>
        </div>

        <Modal
          title={selected ? "Cập nhật tác giả" : "Thêm tác giả"}
          open={isFormOpen}
          onCancel={handleCloseForm}
          footer={null}
          destroyOnHidden
        >
          <div className="modal-fieldset">
            <label>
              <span>Tên tác giả</span>
              <Input
                placeholder="Nhập tên tác giả"
                value={formValue.authorName}
                onChange={(e) => handleChange("authorName", e.target.value)}
                onPressEnter={handleSubmit}
              />
            </label>

            <label>
              <span>Mô tả</span>
              <TextArea
                rows={4}
                placeholder="Nhập mô tả tác giả"
                value={formValue.description}
                onChange={(e) => handleChange("description", e.target.value)}
              />
            </label>

            <div className="modal-actions">
              <Button type="primary" onClick={handleSubmit}>
                {selected ? "Cập nhật" : "Thêm mới"}
              </Button>
              <Button onClick={handleCloseForm}>Hủy</Button>
            </div>
          </div>
        </Modal>

        <Table
          dataSource={authors}
          columns={columns}
          rowKey="id"
          pagination={{
            current: page + 1,
            pageSize: 5,
            total,
            onChange: (p) => setPage(p - 1),
          }}
        />
      </div>
    </section>
  );
}

export default AuthorPage;
