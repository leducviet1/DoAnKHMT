import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Button,
  DatePicker,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  message,
} from "antd";
import { getBooks } from "../api/bookApi";
import {
  borrowBooks,
  downloadBorrowContract,
  getBorrows,
  getMyBorrows,
  returnAllBooks,
  returnBorrowItem,
} from "../api/borrowApi";
import { getAuthSession, normalizeRoles } from "../api/authApi";
import { getUsers } from "../api/userApi";

const { RangePicker } = DatePicker;
const { TextArea } = Input;

const MANAGER_ROLES = ["ADMIN", "LIBRARIAN"];

const emptyBorrowItem = {
  bookId: null,
  quantity: 1,
  note: "",
};

const initialBorrowForm = {
  userId: null,
  dueDate: null,
  items: [{ ...emptyBorrowItem }],
};

const initialFilterValue = {
  keyword: "",
  borrowStatus: "ALL",
  returnState: "ALL",
  dateRange: null,
};

const getErrorMessage = (error, fallbackMessage) => {
  const payload = error?.response?.data;

  if (typeof payload === "string" && payload.trim()) return payload;
  if (payload?.message) return payload.message;
  if (payload?.error) return payload.error;

  return fallbackMessage;
};

const getFileNameFromDisposition = (disposition, fallbackName) => {
  if (!disposition) return fallbackName;

  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const asciiMatch = disposition.match(/filename="?([^"]+)"?/i);
  return asciiMatch?.[1] || fallbackName;
};

function BorrowPage() {
  const session = getAuthSession();
  const roles = normalizeRoles(session?.roles);
  const canManage = roles.some((role) => MANAGER_ROLES.includes(role));

  const [allBorrows, setAllBorrows] = useState([]);
  const [books, setBooks] = useState([]);
  const [users, setUsers] = useState([]);
  const [borrowForm, setBorrowForm] = useState(initialBorrowForm);
  const [filters, setFilters] = useState(initialFilterValue);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [detailBorrow, setDetailBorrow] = useState(null);
  const [page, setPage] = useState(0);
  const [isSubmittingBorrow, setIsSubmittingBorrow] = useState(false);
  const [downloadingContractId, setDownloadingContractId] = useState(null);

  const pageSize = 10;

  const formatDate = (value) => {
    if (!value) return "Chưa có";
    return new Date(value).toLocaleString("vi-VN");
  };

  const formatBorrows = (items = []) =>
    items.map((borrow) => ({
      id: borrow.borrowId,
      userId: borrow.userId,
      userName: borrow.username || borrow.userName || borrow.fullName || "",
      userEmail: borrow.email || "",
      borrowDate: borrow.borrowDate,
      dueDate: borrow.dueDate,
      returnDate: borrow.returnDate,
      borrowStatus: borrow.borrowStatus,
      details: borrow.details || [],
    }));

  const formatBooks = (items = []) =>
    items.map((book) => ({
      id: book.bookId,
      title: book.title,
      availableQuantity: book.availableQuantity,
    }));

  const formatUsers = (items = []) =>
    items.map((user) => ({
      id: user.userId,
      username: user.username,
      email: user.email,
    }));

  const getUserLabel = useCallback((borrow) => {
    const linkedUser = users.find((item) => String(item.id) === String(borrow.userId));
    const isCurrentUser = String(session?.userId) === String(borrow.userId);
    const username = linkedUser?.username || borrow.userName || (isCurrentUser ? session?.username : "");
    const email = linkedUser?.email || borrow.userEmail || (isCurrentUser ? session?.email : "");

    if (username && email) return `${username} - ${email}`;
    if (username) return `${username} (#${borrow.userId})`;
    if (email) return `${email} (#${borrow.userId})`;

    return `Người dùng #${borrow.userId}`;
  }, [session?.email, session?.userId, session?.username, users]);

  const fetchBorrows = async () => {
    const response = canManage ? await getBorrows(0, 300) : await getMyBorrows(0, 300);
    const formattedBorrows = formatBorrows(response.data.content || []);
    setAllBorrows(formattedBorrows);

    setDetailBorrow((current) => {
      if (!current) return current;
      return formattedBorrows.find((item) => item.id === current.id) || null;
    });

    return formattedBorrows;
  };

  useEffect(() => {
    let ignore = false;

    const loadBorrows = async () => {
      try {
        const response = canManage ? await getBorrows(0, 300) : await getMyBorrows(0, 300);
        if (ignore) return;
        setAllBorrows(formatBorrows(response.data.content || []));
      } catch (error) {
        if (ignore) return;
        console.error(error);
        message.error("Không tải được danh sách phiếu mượn");
      }
    };

    loadBorrows();

    return () => {
      ignore = true;
    };
  }, [canManage]);

  useEffect(() => {
    if (!canManage) return undefined;

    let ignore = false;

    Promise.all([getBooks(0, 200), getUsers(0, 300)])
      .then(([bookResponse, userResponse]) => {
        if (ignore) return;
        setBooks(formatBooks(bookResponse.data.content || []));
        setUsers(formatUsers(userResponse.data.content || []));
      })
      .catch((error) => {
        if (ignore) return;
        console.error(error);
      });

    return () => {
      ignore = true;
    };
  }, [canManage]);

  const filteredBorrows = useMemo(() => {
    const keyword = filters.keyword.trim().toLowerCase();
    const [startDate, endDate] = filters.dateRange || [];

    return allBorrows.filter((borrow) => {
      if (filters.borrowStatus !== "ALL" && borrow.borrowStatus !== filters.borrowStatus) {
        return false;
      }

      const isReturned = borrow.borrowStatus === "RETURNED" || Boolean(borrow.returnDate);
      if (filters.returnState === "RETURNED" && !isReturned) return false;
      if (filters.returnState === "BORROWING" && isReturned) return false;

      if (startDate || endDate) {
        const borrowTime = borrow.borrowDate ? new Date(borrow.borrowDate).getTime() : null;
        if (!borrowTime) return false;

        if (startDate && borrowTime < startDate.startOf("day").valueOf()) return false;
        if (endDate && borrowTime > endDate.endOf("day").valueOf()) return false;
      }

      if (!keyword) return true;

      const detailTitles = (borrow.details || []).map((detail) => detail.title).join(" ");
      const haystack = [
        borrow.id,
        borrow.userId,
        getUserLabel(borrow),
        borrow.borrowStatus,
        detailTitles,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return haystack.includes(keyword);
    });
  }, [allBorrows, filters, getUserLabel]);

  const paginatedBorrows = useMemo(() => {
    const startIndex = page * pageSize;
    return filteredBorrows.slice(startIndex, startIndex + pageSize);
  }, [filteredBorrows, page]);

  const handleFilterChange = (field, value) => {
    setPage(0);
    setFilters((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleResetFilters = () => {
    setPage(0);
    setFilters(initialFilterValue);
  };

  const handleOpenCreate = () => {
    setBorrowForm(initialBorrowForm);
    setIsCreateOpen(true);
  };

  const handleCloseCreate = () => {
    setBorrowForm(initialBorrowForm);
    setIsCreateOpen(false);
  };

  const handleOpenDetail = (record) => {
    setDetailBorrow(record);
  };

  const handleCloseDetail = () => {
    setDetailBorrow(null);
  };

  const handleUserChange = (value) => {
    setBorrowForm((current) => ({
      ...current,
      userId: value,
    }));
  };

  const handleDueDateChange = (value) => {
    setBorrowForm((current) => ({
      ...current,
      dueDate: value,
    }));
  };

  const handleItemChange = (index, field, value) => {
    setBorrowForm((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index
          ? {
              ...item,
              [field]: value,
            }
          : item,
      ),
    }));
  };

  const handleAddItem = () => {
    setBorrowForm((current) => ({
      ...current,
      items: [...current.items, { ...emptyBorrowItem }],
    }));
  };

  const handleRemoveItem = (index) => {
    setBorrowForm((current) => ({
      ...current,
      items:
        current.items.length === 1
          ? [{ ...emptyBorrowItem }]
          : current.items.filter((_, itemIndex) => itemIndex !== index),
    }));
  };

  const getBookById = (bookId) => books.find((book) => book.id === bookId);

  const validateBorrowForm = () => {
    if (!borrowForm.userId) {
      message.warning("Vui lòng chọn người mượn");
      return false;
    }

    if (!borrowForm.dueDate) {
      message.warning("Vui lòng chọn hạn trả");
      return false;
    }

    const selectedBookIds = new Set();

    for (const item of borrowForm.items) {
      const book = getBookById(item.bookId);

      if (!item.bookId) {
        message.warning("Vui lòng chọn sách cần mượn");
        return false;
      }

      if (selectedBookIds.has(item.bookId)) {
        message.warning("Không chọn trùng một sách trong cùng phiếu");
        return false;
      }

      if (!item.quantity || item.quantity <= 0) {
        message.warning("Vui lòng nhập số lượng mượn hợp lệ");
        return false;
      }

      if (book && item.quantity > book.availableQuantity) {
        message.warning(`Sách "${book.title}" không đủ số lượng còn lại`);
        return false;
      }

      selectedBookIds.add(item.bookId);
    }

    return true;
  };

  const handleDownloadContract = async (borrowId) => {
    try {
      setDownloadingContractId(borrowId);
      const response = await downloadBorrowContract(borrowId);
      const fileName = getFileNameFromDisposition(
        response.headers["content-disposition"],
        `borrow_contract_${borrowId}.pdf`,
      );
      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");

      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error(error);
      message.error(getErrorMessage(error, "Không tải được hợp đồng PDF"));
    } finally {
      setDownloadingContractId(null);
    }
  };

  const handleCreateBorrow = async () => {
    if (!validateBorrowForm()) return;

    try {
      setIsSubmittingBorrow(true);
      const response = await borrowBooks({
        userId: borrowForm.userId,
        dueDate: borrowForm.dueDate.format("YYYY-MM-DD"),
        items: borrowForm.items.map((item) => ({
          bookId: item.bookId,
          quantity: item.quantity,
          note: item.note.trim(),
        })),
      });

      const createdBorrow = response.data;
      const createdBorrowId = createdBorrow?.borrowId;

      message.success("Tạo phiếu mượn thành công");
      handleCloseCreate();
      await fetchBorrows();
      setPage(0);

      if (createdBorrowId) {
        Modal.confirm({
          title: "Đã tạo phiếu mượn",
          content:
            "Phiếu mượn đã được lưu và hợp đồng PDF đã sẵn sàng trong tài khoản của người dùng.",
          okText: "Tải hợp đồng",
          cancelText: "Đóng",
          onOk: () => handleDownloadContract(createdBorrowId),
        });
      }
    } catch (error) {
      console.error(error);
      message.error(getErrorMessage(error, "Tạo phiếu mượn thất bại"));
    } finally {
      setIsSubmittingBorrow(false);
    }
  };

  const handleReturnAll = async (borrowId) => {
    try {
      await returnAllBooks(borrowId);
      message.success("Trả toàn bộ sách thành công");
      await fetchBorrows();
    } catch (error) {
      console.error(error);
      message.error(getErrorMessage(error, "Trả sách thất bại"));
    }
  };

  const handleReturnItem = async (borrowDetailId) => {
    try {
      await returnBorrowItem(borrowDetailId);
      message.success("Trả sách thành công");
      await fetchBorrows();
    } catch (error) {
      console.error(error);
      message.error(getErrorMessage(error, "Trả sách thất bại"));
    }
  };

  const handleConfirmReturnItem = (borrowDetailId) => {
    Modal.confirm({
      title: "Trả sách",
      content: "Xác nhận trả riêng cuốn sách này?",
      okText: "Trả sách",
      cancelText: "Hủy",
      onOk: () => handleReturnItem(borrowDetailId),
    });
  };

  const renderBorrowDetails = (details, record) => (
    <div className="borrow-detail-list">
      {details.length ? (
        details.map((detail) => {
          const borrowDetailId = detail.borrowDetailId ?? detail.id;
          const detailStatus = detail.status || record.borrowStatus;
          const isReturned = detailStatus === "RETURNED";

          return (
            <div className="borrow-detail-item" key={`${borrowDetailId || detail.title}-${detail.quantity}`}>
              <strong>{detail.title}</strong>
              <span>Số lượng: {detail.quantity}</span>
              {detail.note ? <span>Ghi chú: {detail.note}</span> : null}
              <Tag className="borrow-detail-status" color={isReturned ? "green" : "orange"}>
                {isReturned ? "Đã trả" : "Đang mượn"}
              </Tag>
              {canManage ? (
                <Button
                  size="small"
                  disabled={!borrowDetailId || isReturned}
                  onClick={() => handleConfirmReturnItem(borrowDetailId)}
                >
                  Trả sách này
                </Button>
              ) : null}
            </div>
          );
        })
      ) : (
        <span className="muted-text">Chưa có chi tiết</span>
      )}
    </div>
  );

  const columns = [
    {
      title: "Mã phiếu",
      dataIndex: "id",
      width: 120,
    },
    {
      title: "Người mượn",
      width: 240,
      render: (_, record) => getUserLabel(record),
    },
    {
      title: "Ngày mượn",
      dataIndex: "borrowDate",
      width: 180,
      render: formatDate,
    },
    {
      title: "Hạn trả",
      dataIndex: "dueDate",
      width: 180,
      render: formatDate,
    },
    {
      title: "Ngày trả",
      dataIndex: "returnDate",
      width: 180,
      render: formatDate,
    },
    {
      title: "Trạng thái",
      dataIndex: "borrowStatus",
      width: 140,
      render: (status) => (
        <Tag color={status === "BORROWING" ? "orange" : "green"}>
          {status === "BORROWING" ? "Chưa trả" : "Đã trả"}
        </Tag>
      ),
    },
    {
      title: "Chi tiết",
      dataIndex: "details",
      width: 240,
      render: (details, record) => (
        <div className="borrow-detail-summary">
          <span>{details.length ? `${details.length} sách` : "Chưa có chi tiết"}</span>
          {record.borrowStatus === "RETURNED" ? <Tag color="green">Đã trả tất cả</Tag> : null}
          <Button size="small" onClick={() => handleOpenDetail(record)}>
            Xem chi tiết
          </Button>
        </div>
      ),
    },
    {
      title: "Hợp đồng",
      width: 160,
      render: (_, record) => (
        <Button
          onClick={() => handleDownloadContract(record.id)}
          loading={downloadingContractId === record.id}
        >
          Tải PDF
        </Button>
      ),
    },
  ];

  if (canManage) {
    columns.push({
      title: "Hành động",
      width: 150,
      render: (_, record) =>
        record.borrowStatus === "BORROWING" ? (
          <Space>
            <Popconfirm
              title="Trả toàn bộ sách"
              description="Xác nhận trả toàn bộ sách trong phiếu này?"
              onConfirm={() => handleReturnAll(record.id)}
              okText="Trả sách"
              cancelText="Hủy"
            >
              <Button type="primary">Trả tất cả</Button>
            </Popconfirm>
          </Space>
        ) : (
          <span className="muted-text">Đã trả</span>
        ),
    });
  }

  return (
    <section className="page-shell">
      <div className="page-hero">
        <div>
          <span className="eyebrow">Lưu thông mượn trả</span>
          <h2>{canManage ? "Quản lý phiếu mượn" : "Phiếu mượn của tôi"}</h2>
          <p>
            {canManage
              ? "Tạo phiếu mượn nhiều sách, xuất hợp đồng PDF và theo dõi quá trình trả sách cho từng phiếu."
              : "Theo dõi các phiếu mượn thuộc tài khoản của bạn và tải lại hợp đồng PDF bất cứ lúc nào."}
          </p>
        </div>
      </div>

      <div className="metric-row">
        <div className="metric-card">
          <strong>{filteredBorrows.length}</strong>
          <span>{canManage ? "Phiếu phù hợp" : "Phiếu của bạn"}</span>
        </div>
        <div className="metric-card">
          <strong>{filteredBorrows.filter((borrow) => borrow.borrowStatus === "BORROWING").length}</strong>
          <span>Chưa trả</span>
        </div>
        <div className="metric-card">
          <strong>{filteredBorrows.filter((borrow) => borrow.borrowStatus === "RETURNED").length}</strong>
          <span>Đã trả</span>
        </div>
      </div>

      <div className="content-panel">
        <div className="panel-heading">
          <div>
            <h3>{canManage ? "Danh sách phiếu mượn" : "Lịch sử mượn của bạn"}</h3>
            <p>
              {canManage
                ? "Lọc theo nhiều tiêu chí, tải hợp đồng PDF và xử lý trả từng sách ngay trong cùng một nơi."
                : "Bạn chỉ nhìn thấy các phiếu mượn của chính mình, kèm hợp đồng PDF đã được lưu trên hệ thống."}
            </p>
          </div>
          {canManage ? (
            <Button type="primary" onClick={handleOpenCreate}>
              Tạo phiếu mượn
            </Button>
          ) : null}
        </div>

        <div className="borrow-filter-bar">
          <Input
            placeholder="Tìm theo mã phiếu, người mượn, tên sách"
            value={filters.keyword}
            onChange={(e) => handleFilterChange("keyword", e.target.value)}
            allowClear
          />
          <Select
            value={filters.borrowStatus}
            onChange={(value) => handleFilterChange("borrowStatus", value)}
            options={[
              { value: "ALL", label: "Tất cả trạng thái" },
              { value: "BORROWING", label: "Đang mượn" },
              { value: "RETURNED", label: "Đã trả" },
            ]}
          />
          <Select
            value={filters.returnState}
            onChange={(value) => handleFilterChange("returnState", value)}
            options={[
              { value: "ALL", label: "Tất cả phiếu" },
              { value: "BORROWING", label: "Chưa trả" },
              { value: "RETURNED", label: "Đã trả" },
            ]}
          />
          <RangePicker
            value={filters.dateRange}
            format="DD/MM/YYYY"
            onChange={(value) => handleFilterChange("dateRange", value)}
          />
          <Button onClick={handleResetFilters}>Xóa lọc</Button>
        </div>

        {canManage ? (
          <Modal
            title="Tạo phiếu mượn"
            open={isCreateOpen}
            onCancel={handleCloseCreate}
            footer={null}
            width={860}
            destroyOnHidden
          >
            <div className="borrow-form">
              <label className="borrow-user-field">
                <span>Người mượn</span>
                {users.length ? (
                  <Select
                    showSearch
                    placeholder="Chọn người mượn"
                    value={borrowForm.userId}
                    optionFilterProp="label"
                    onChange={handleUserChange}
                    options={users.map((user) => ({
                      value: user.id,
                      label: `${user.username || "Người dùng"} - ${user.email || "chưa có email"} (#${user.id})`,
                    }))}
                  />
                ) : (
                  <InputNumber
                    min={1}
                    placeholder="Nhập userId"
                    value={borrowForm.userId}
                    onChange={handleUserChange}
                  />
                )}
              </label>

              <label className="borrow-user-field">
                <span>Hạn trả</span>
                <DatePicker
                  value={borrowForm.dueDate}
                  format="DD/MM/YYYY"
                  placeholder="Chọn hạn trả"
                  onChange={handleDueDateChange}
                  disabledDate={(current) => current && current.endOf("day").valueOf() < Date.now()}
                />
              </label>

              <div className="borrow-items">
                <div className="borrow-items-heading">
                  <strong>Sách mượn</strong>
                  <Button onClick={handleAddItem}>Thêm sách</Button>
                </div>

                {borrowForm.items.map((item, index) => {
                  const selectedBook = getBookById(item.bookId);

                  return (
                    <div className="borrow-item-row" key={index}>
                      <label>
                        <span>Sách</span>
                        <Select
                          showSearch
                          placeholder="Chọn sách"
                          value={item.bookId}
                          optionFilterProp="label"
                          onChange={(value) => handleItemChange(index, "bookId", value)}
                          options={books.map((book) => ({
                            value: book.id,
                            label: `${book.title} - còn ${book.availableQuantity}`,
                            disabled: book.availableQuantity <= 0,
                          }))}
                        />
                      </label>

                      <label>
                        <span>Số lượng</span>
                        <InputNumber
                          min={1}
                          max={selectedBook?.availableQuantity || 1}
                          value={item.quantity}
                          onChange={(value) => handleItemChange(index, "quantity", value)}
                        />
                      </label>

                      <label>
                        <span>Ghi chú</span>
                        <TextArea
                          rows={1}
                          placeholder="Ghi chú"
                          value={item.note}
                          onChange={(e) => handleItemChange(index, "note", e.target.value)}
                        />
                      </label>

                      <Button danger onClick={() => handleRemoveItem(index)}>
                        Xóa
                      </Button>
                    </div>
                  );
                })}
              </div>

              <div className="form-note">
                Sau khi tạo phiếu, hệ thống sẽ sinh hợp đồng PDF. Người dùng có thể tải lại hợp đồng ngay trong tài khoản của họ.
              </div>

              <div className="modal-actions">
                <Button type="primary" onClick={handleCreateBorrow} loading={isSubmittingBorrow}>
                  Tạo phiếu
                </Button>
                <Button onClick={handleCloseCreate}>Hủy</Button>
              </div>
            </div>
          </Modal>
        ) : null}

        <Modal
          title={`Chi tiết phiếu mượn #${detailBorrow?.id || ""}`}
          open={Boolean(detailBorrow)}
          onCancel={handleCloseDetail}
          footer={null}
          width={720}
          destroyOnHidden
        >
          {detailBorrow ? (
            <div className="borrow-detail-modal">
              <div className="borrow-detail-meta">
                <span>Người mượn: {getUserLabel(detailBorrow)}</span>
                <span>Ngày mượn: {formatDate(detailBorrow.borrowDate)}</span>
                <span>Hạn trả: {formatDate(detailBorrow.dueDate)}</span>
                <Tag color={detailBorrow.borrowStatus === "BORROWING" ? "orange" : "green"}>
                  {detailBorrow.borrowStatus === "BORROWING" ? "Chưa trả" : "Đã trả"}
                </Tag>
                <Button
                  size="small"
                  onClick={() => handleDownloadContract(detailBorrow.id)}
                  loading={downloadingContractId === detailBorrow.id}
                >
                  Tải PDF hợp đồng
                </Button>
              </div>
              {renderBorrowDetails(detailBorrow.details, detailBorrow)}
            </div>
          ) : null}
        </Modal>

        <Table
          className="book-table"
          dataSource={paginatedBorrows}
          columns={columns}
          rowKey="id"
          scroll={{ x: canManage ? 1500 : 1320 }}
          locale={{ emptyText: "Không có phiếu mượn phù hợp" }}
          pagination={{
            current: page + 1,
            pageSize,
            total: filteredBorrows.length,
            onChange: (nextPage) => setPage(nextPage - 1),
          }}
        />
      </div>
    </section>
  );
}

export default BorrowPage;
