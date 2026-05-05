import { useEffect, useState } from "react";
import {
  getPublishers,
  createPublisher,
  updatePublisher,
  deletePublisher,
} from "../api/publisherApi";
import { Modal } from "antd";
import PublisherForm from "../components/PublisherForm";

const PublisherPage = () => {
  const [publishers, setPublishers] = useState([]);
  const [editingPublisher, setEditingPublisher] = useState(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  const fetchPublishers = async () => {
    try {
      const data = await getPublishers();
      setPublishers(data.content || []);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    let ignore = false;

    getPublishers()
      .then((data) => {
        if (!ignore) {
          setPublishers(data.content || []);
        }
      })
      .catch((error) => {
        if (!ignore) {
          console.error(error);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  const handleSubmit = async (publisher) => {
    try {
      if (editingPublisher) {
        await updatePublisher(editingPublisher.publisherId, publisher);
      } else {
        await createPublisher(publisher);
      }

      setEditingPublisher(null);
      setIsFormOpen(false);
      fetchPublishers();
    } catch (error) {
      console.error(error);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Bạn có chắc chắn muốn xóa nhà xuất bản này?")) {
      await deletePublisher(id);
      fetchPublishers();
    }
  };

  const handleOpenCreate = () => {
    setEditingPublisher(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (publisher) => {
    setEditingPublisher(publisher);
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setEditingPublisher(null);
    setIsFormOpen(false);
  };

  return (
    <section className="page-shell">
      <div className="page-hero">
        <div>
          <span className="eyebrow">Đối tác xuất bản</span>
          <h2>Quản lý nhà xuất bản</h2>
          <p>
            Lưu thông tin nhà xuất bản để gắn với sách, báo cáo nhập kho và dữ
            liệu tìm kiếm.
          </p>
        </div>
      </div>

      <div className="metric-row">
        <div className="metric-card">
          <strong>{publishers.length}</strong>
          <span>Nhà xuất bản</span>
        </div>
        <div className="metric-card">
          <strong>{editingPublisher ? "1" : "0"}</strong>
          <span>Đang chỉnh sửa</span>
        </div>
        <div className="metric-card">
          <strong>5</strong>
          <span>Số dòng mỗi trang</span>
        </div>
      </div>

      <div className="content-panel">
        <div className="panel-heading">
          <div>
            <h3>Danh sách nhà xuất bản</h3>
            <p>Theo dõi và cập nhật thông tin nhà xuất bản.</p>
          </div>
          <button type="button" onClick={handleOpenCreate} className="btn btn-primary">
            Thêm mới
          </button>
        </div>

        <Modal
          title={editingPublisher ? "Cập nhật nhà xuất bản" : "Thêm nhà xuất bản"}
          open={isFormOpen}
          onCancel={handleCloseForm}
          footer={null}
          destroyOnHidden
        >
          <PublisherForm
            onSubmit={handleSubmit}
            editingPublisher={editingPublisher}
            onCancel={handleCloseForm}
          />
        </Modal>

        <table className="data-table">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Tên nhà xuất bản</th>
              <th className="text-center">Hành động</th>
            </tr>
          </thead>

          <tbody>
            {publishers.length === 0 ? (
              <tr>
                <td colSpan="3" className="empty-state">
                  Chưa có nhà xuất bản nào.
                </td>
              </tr>
            ) : (
              publishers.map((publisher) => (
                <tr key={publisher.publisherId}>
                  <td>{publisher.publisherId}</td>
                  <td>{publisher.publisherName}</td>
                  <td>
                    <div className="table-actions">
                      <button
                        type="button"
                        onClick={() => handleOpenEdit(publisher)}
                        className="btn btn-warning"
                      >
                        Sửa
                      </button>

                      <button
                        type="button"
                        onClick={() => handleDelete(publisher.publisherId)}
                        className="btn btn-danger"
                      >
                        Xóa
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
};

export default PublisherPage;
