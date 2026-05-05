import { useEffect, useState } from "react";
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from "../api/categoryApi";

import { Table, Button, Input, Space, Popconfirm, message, Modal } from "antd";

function CategoryPage() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState("");
  const [selected, setSelected] = useState(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);

  const fetchData = async (pageIndex = page) => {
    try {
      const res = await getCategories(pageIndex, 5);

      const formatted = res.data.content.map((category) => ({
        id: category.categoryId,
        name: category.categoryName,
      }));

      setCategories(formatted);
      setTotal(res.data.totalElements);
    } catch (err) {
      console.error(err);
      message.error("Không tải được danh mục");
    }
  };

  useEffect(() => {
    let ignore = false;

    getCategories(page, 5)
      .then((res) => {
        if (ignore) return;

        const formatted = res.data.content.map((category) => ({
          id: category.categoryId,
          name: category.categoryName,
        }));

        setCategories(formatted);
        setTotal(res.data.totalElements);
      })
      .catch((err) => {
        if (ignore) return;
        console.error(err);
        message.error("Không tải được danh mục");
      });

    return () => {
      ignore = true;
    };
  }, [page]);

  const handleSubmit = async () => {
    if (!name.trim()) {
      message.warning("Vui lòng nhập tên danh mục");
      return;
    }

    try {
      if (selected) {
        await updateCategory(selected.id, { categoryName: name });
        message.success("Cập nhật danh mục thành công");
      } else {
        await createCategory({ categoryName: name });
        message.success("Thêm danh mục thành công");
      }

      setName("");
      setSelected(null);
      setIsFormOpen(false);
      fetchData();
    } catch (err) {
      console.error(err);
      message.error("Thao tác thất bại");
    }
  };

  const handleOpenCreate = () => {
    setSelected(null);
    setName("");
    setIsFormOpen(true);
  };

  const handleEdit = (record) => {
    setSelected(record);
    setName(record.name);
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setSelected(null);
    setName("");
    setIsFormOpen(false);
  };

  const handleDelete = async (id) => {
    try {
      await deleteCategory(id);
      message.success("Xóa danh mục thành công");
      fetchData();
    } catch (err) {
      console.error(err);
      message.error("Xóa danh mục thất bại");
    }
  };

  const columns = [
    {
      title: "Mã",
      dataIndex: "id",
      width: 120,
    },
    {
      title: "Tên danh mục",
      dataIndex: "name",
    },
    {
      title: "Hành động",
      width: 220,
      render: (_, record) => (
        <Space>
          <Button type="primary" onClick={() => handleEdit(record)}>
            Sửa
          </Button>

          <Popconfirm
            title="Xóa danh mục"
            description="Bạn có chắc chắn muốn xóa danh mục này?"
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
          <span className="eyebrow">Kho dữ liệu</span>
          <h2>Quản lý danh mục sách</h2>
          <p>
            Tạo nhóm phân loại để việc tìm kiếm, nhập sách và thống kê được rõ
            ràng hơn.
          </p>
        </div>
      </div>

      <div className="metric-row">
        <div className="metric-card">
          <strong>{total}</strong>
          <span>Tổng danh mục</span>
        </div>
        <div className="metric-card">
          <strong>{categories.length}</strong>
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
            <h3>Danh sách danh mục</h3>
            <p>Theo dõi và cập nhật các nhóm phân loại sách.</p>
          </div>
          <Button type="primary" onClick={handleOpenCreate}>
            Thêm mới
          </Button>
        </div>

        <Modal
          title={selected ? "Cập nhật danh mục" : "Thêm danh mục"}
          open={isFormOpen}
          onCancel={handleCloseForm}
          footer={null}
          destroyOnHidden
        >
          <Space.Compact className="toolbar-form modal-form">
            <Input
              placeholder="Nhập tên danh mục"
              value={name}
              onChange={(e) => setName(e.target.value)}
              onPressEnter={handleSubmit}
            />

            <Button type="primary" onClick={handleSubmit}>
              {selected ? "Cập nhật" : "Thêm mới"}
            </Button>

            <Button onClick={handleCloseForm}>Hủy</Button>
          </Space.Compact>
        </Modal>

        <Table
          dataSource={categories}
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

export default CategoryPage;
