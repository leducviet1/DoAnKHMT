import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Input, Modal, Popconfirm, Select, Table, Tag, message } from "antd";
import { getBorrows } from "../api/borrowApi";
import { getAuthSession, normalizeRoles } from "../api/authApi";
import { createFine, getFines, payFine } from "../api/fineApi";

const initialFineForm = {
  borrowDetailId: null,
  type: "LOST",
  reason: "",
};

const typeOptions = [
  { value: "LOST", label: "Mất sách" },
  { value: "LATE", label: "Trả muộn" },
];

const statusOptions = [
  { value: "ALL", label: "Tất cả trạng thái" },
  { value: "PENDING", label: "Chờ thanh toán" },
  { value: "PAID", label: "Đã thanh toán" },
];

const canManageRoles = ["ADMIN", "LIBRARIAN"];

const isPaidStatus = (status) => ["PAID", "PAIDED"].includes(status);

const getTypeLabel = (type) =>
  typeOptions.find((option) => option.value === type)?.label || type;

const getStatusMeta = (status) =>
  isPaidStatus(status)
    ? { color: "green", label: "Đã thanh toán" }
    : { color: "gold", label: "Chờ thanh toán" };

const repairVietnameseText = (value) => {
  if (typeof value !== "string" || !value) return value || "";
  if (!/[ÃÂÆÄ]/.test(value)) return value;

  try {
    return decodeURIComponent(escape(value));
  } catch {
    return value;
  }
};

async function fetchAllFineItems() {
  const pageSize = 100;
  const firstRes = await getFines(0, pageSize);
  const firstPage = firstRes.data;
  const totalPages = firstPage.totalPages || 1;
  const allItems = [...(firstPage.content || [])];

  if (totalPages <= 1) {
    return allItems;
  }

  const requests = [];
  for (let nextPage = 1; nextPage < totalPages; nextPage += 1) {
    requests.push(getFines(nextPage, pageSize));
  }

  const responses = await Promise.all(requests);
  responses.forEach((res) => {
    allItems.push(...(res.data.content || []));
  });

  return allItems;
}

function FinePage() {
  const session = getAuthSession();
  const roles = normalizeRoles(session?.roles);
  const canManage = roles.some((role) => canManageRoles.includes(role));

  const [fines, setFines] = useState([]);
  const [fineOverview, setFineOverview] = useState([]);
  const [borrowOptions, setBorrowOptions] = useState([]);
  const [fineForm, setFineForm] = useState(initialFineForm);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [loadingTable, setLoadingTable] = useState(false);
  const [loadingOverview, setLoadingOverview] = useState(false);
  const [payingFineId, setPayingFineId] = useState(null);
  const [filterKeyword, setFilterKeyword] = useState("");
  const [filterStatus, setFilterStatus] = useState("ALL");

  const fetchFinePage = useCallback(async (pageIndex) => {
    try {
      setLoadingTable(true);
      const res = await getFines(pageIndex, 10);
      setFines(res.data.content || []);
      setTotal(res.data.totalElements || 0);
    } catch (err) {
      console.error(err);
      message.error("Không tải được danh sách tiền phạt");
    } finally {
      setLoadingTable(false);
    }
  }, []);

  const fetchOverview = useCallback(async () => {
    try {
      setLoadingOverview(true);
      const allItems = await fetchAllFineItems();
      setFineOverview(allItems);
    } catch (err) {
      console.error(err);
      setFineOverview([]);
    } finally {
      setLoadingOverview(false);
    }
  }, []);

  const refreshData = useCallback(
    async (pageIndex = page) => {
      await Promise.all([fetchFinePage(pageIndex), fetchOverview()]);
    },
    [fetchFinePage, fetchOverview, page],
  );

  useEffect(() => {
    fetchFinePage(page);
  }, [fetchFinePage, page]);

  useEffect(() => {
    fetchOverview();
  }, [fetchOverview]);

  useEffect(() => {
    if (!canManage) return undefined;

    let ignore = false;

    getBorrows(0, 100)
      .then((res) => {
        if (ignore) return;

        const detailOptions = (res.data.content || []).flatMap((borrow) =>
          (borrow.details || [])
            .filter((detail) => detail.id || detail.borrowDetailId)
            .filter((detail) => detail.status !== "RETURNED")
            .map((detail) => ({
              value: detail.borrowDetailId ?? detail.id,
              label: `Phiếu #${borrow.borrowId} • ${detail.title} • SL ${detail.quantity}`,
            })),
        );

        setBorrowOptions(detailOptions);
      })
      .catch((err) => {
        if (ignore) return;
        console.error(err);
        message.error("Không tải được chi tiết mượn để tạo tiền phạt");
      });

    return () => {
      ignore = true;
    };
  }, [canManage]);

  const filteredFines = useMemo(() => {
    const keyword = filterKeyword.trim().toLowerCase();

    return fines.filter((fine) => {
      const normalizedStatus = isPaidStatus(fine.status) ? "PAID" : "PENDING";
      const matchesStatus = filterStatus === "ALL" || normalizedStatus === filterStatus;

      if (!matchesStatus) return false;
      if (!keyword) return true;

      const haystack = [
        fine.fineId,
        fine.borrowDetailId,
        fine.reason,
        fine.type,
        fine.status,
        fine.amount,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return haystack.includes(keyword);
    });
  }, [filterKeyword, filterStatus, fines]);

  const totalCount = fineOverview.length;
  const pendingCount = fineOverview.filter((fine) => !isPaidStatus(fine.status)).length;
  const paidCount = fineOverview.filter((fine) => isPaidStatus(fine.status)).length;

  const totalAmount = fineOverview.reduce((sum, fine) => sum + Number(fine.amount || 0), 0);
  const pendingAmount = fineOverview.reduce((sum, fine) => {
    if (isPaidStatus(fine.status)) return sum;
    return sum + Number(fine.amount || 0);
  }, 0);

  const paidAmount = Math.max(totalAmount - pendingAmount, 0);

  const handleOpenCreate = () => {
    setFineForm(initialFineForm);
    setIsCreateOpen(true);
  };

  const handleCloseCreate = () => {
    setFineForm(initialFineForm);
    setIsCreateOpen(false);
  };

  const handleFormChange = (field, value) => {
    setFineForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleCreateFine = async () => {
    if (!fineForm.borrowDetailId || !fineForm.reason.trim()) {
      message.warning("Vui lòng chọn chi tiết mượn và nhập lý do phạt");
      return;
    }

    try {
      await createFine({
        borrowDetailId: fineForm.borrowDetailId,
        reason: fineForm.reason.trim(),
        type: fineForm.type,
        status: "PENDING",
      });

      message.success("Tạo khoản phạt thành công");
      handleCloseCreate();
      setPage(0);
      await refreshData(0);
    } catch (err) {
      console.error(err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.response?.data ||
        "Tạo tiền phạt thất bại";
      message.error(String(errorMessage));
    }
  };

  const handlePayFine = async (fineId) => {
    try {
      setPayingFineId(fineId);
      await payFine(fineId);
      message.success("Đã cập nhật trạng thái thanh toán");
      await refreshData(page);
    } catch (err) {
      console.error(err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.response?.data ||
        "Thanh toán thất bại";
      message.error(String(errorMessage));
    } finally {
      setPayingFineId(null);
    }
  };

  const columns = [
    {
      title: "Mã phạt",
      dataIndex: "fineId",
      width: 110,
      render: (fineId) => <strong>#{fineId}</strong>,
    },
    {
      title: "Chi tiết mượn",
      dataIndex: "borrowDetailId",
      width: 140,
      render: (borrowDetailId) => <span className="muted-text">#{borrowDetailId}</span>,
    },
    {
      title: "Số tiền",
      dataIndex: "amount",
      width: 160,
      render: (amount) => (
        <strong className="fine-amount">
          {Number(amount || 0).toLocaleString("vi-VN")} đ
        </strong>
      ),
    },
    {
      title: "Loại phạt",
      dataIndex: "type",
      width: 150,
      render: (type) => (
        <Tag color={type === "LOST" ? "red" : "orange"}>{getTypeLabel(type)}</Tag>
      ),
    },
    {
      title: "Lý do",
      dataIndex: "reason",
      width: 240,
      render: (reason) => <span className="fine-reason">{repairVietnameseText(reason)}</span>,
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      width: 150,
      render: (status) => {
        const statusMeta = getStatusMeta(status);
        return <Tag color={statusMeta.color}>{statusMeta.label}</Tag>;
      },
    },
    {
      title: "Ngày tạo",
      dataIndex: "createdAt",
      width: 190,
      render: (createdAt) =>
        createdAt ? new Date(createdAt).toLocaleString("vi-VN") : "Chưa có",
    },
  ];

  if (canManage) {
    columns.push({
      title: "Hành động",
      width: 160,
      render: (_, record) => {
        if (isPaidStatus(record.status)) {
          return <span className="muted-text">Đã xử lý</span>;
        }

        return (
          <Popconfirm
            title="Xác nhận thanh toán"
            description="Đánh dấu khoản phạt này đã được thanh toán?"
            onConfirm={() => handlePayFine(record.fineId)}
            okText="Đã thanh toán"
            cancelText="Hủy"
          >
            <Button type="primary" loading={payingFineId === record.fineId}>
              Thanh toán
            </Button>
          </Popconfirm>
        );
      },
    });
  }

  return (
    <section className="page-shell">
      <div className="page-hero fine-hero">
        <div>
          <span className="eyebrow">Quản lý tài chính</span>
          <h2>{canManage ? "Tiền phạt thư viện" : "Tiền phạt của tôi"}</h2>
          <p>
            {canManage
              ? "Theo dõi tiền phạt của độc giả, cập nhật trạng thái thanh toán và xử lý các khoản phát sinh."
              : "Bạn chỉ nhìn thấy các khoản phạt gắn với tài khoản của mình. Trạng thái và số tiền được đồng bộ trực tiếp từ hệ thống."}
          </p>
        </div>
        <div className="fine-hero__badge">
          <span>Phạm vi hiển thị</span>
          <strong>{canManage ? "Toàn bộ hệ thống" : "Tài khoản của bạn"}</strong>
        </div>
      </div>

      <div className="metric-row fine-metric-row">
        <div className="metric-card fine-metric-card fine-metric-card--danger">
          <strong>{loadingOverview ? "..." : totalCount}</strong>
          <span>{canManage ? "Tổng khoản phạt" : "Khoản phạt của bạn"}</span>
          <small>{totalAmount.toLocaleString("vi-VN")} đ</small>
        </div>
        <div className="metric-card fine-metric-card fine-metric-card--warning">
          <strong>{loadingOverview ? "..." : pendingCount}</strong>
          <span>Chờ thanh toán</span>
          <small>{pendingAmount.toLocaleString("vi-VN")} đ</small>
        </div>
        <div className="metric-card fine-metric-card fine-metric-card--success">
          <strong>{loadingOverview ? "..." : paidCount}</strong>
          <span>Đã thanh toán</span>
          <small>{paidAmount.toLocaleString("vi-VN")} đ</small>
        </div>
      </div>

      <div className="content-panel fine-panel">
        <div className="panel-heading">
          <div>
            <h3>{canManage ? "Danh sách tiền phạt" : "Danh sách tiền phạt của bạn"}</h3>
            <p>
              {canManage
                ? "Admin và thủ thư có thể tạo khoản phạt mới, theo dõi trạng thái và xác nhận thanh toán."
                : "Bạn có thể theo dõi các khoản phạt đang chờ xử lý và những khoản đã thanh toán."}
            </p>
          </div>
          {canManage ? (
            <Button type="primary" onClick={handleOpenCreate}>
              Tạo tiền phạt
            </Button>
          ) : null}
        </div>

        <div className="fine-filter-bar">
          <Input
            placeholder="Tìm theo mã phạt, mã chi tiết mượn, lý do"
            value={filterKeyword}
            onChange={(e) => setFilterKeyword(e.target.value)}
            allowClear
          />
          <Select value={filterStatus} onChange={setFilterStatus} options={statusOptions} />
        </div>

        <Table
          className="book-table fine-table"
          dataSource={filteredFines}
          columns={columns}
          rowKey="fineId"
          loading={loadingTable}
          scroll={{ x: 1240 }}
          locale={{ emptyText: "Chưa có khoản phạt nào" }}
          pagination={{
            current: page + 1,
            pageSize: 10,
            total,
            onChange: (nextPage) => setPage(nextPage - 1),
          }}
        />
      </div>

      {canManage ? (
        <Modal
          title="Tạo tiền phạt"
          open={isCreateOpen}
          onCancel={handleCloseCreate}
          footer={null}
          destroyOnHidden
        >
          <div className="modal-fieldset">
            <label>
              <span>Chi tiết mượn</span>
              <Select
                showSearch
                optionFilterProp="label"
                placeholder="Chọn chi tiết mượn"
                value={fineForm.borrowDetailId}
                onChange={(value) => handleFormChange("borrowDetailId", value)}
                options={borrowOptions}
                notFoundContent="Không có chi tiết mượn phù hợp"
              />
            </label>

            <label>
              <span>Loại phạt</span>
              <Select
                value={fineForm.type}
                onChange={(value) => handleFormChange("type", value)}
                options={typeOptions}
              />
            </label>

            <label>
              <span>Lý do</span>
              <Input.TextArea
                rows={3}
                placeholder="Nhập lý do phạt"
                value={fineForm.reason}
                onChange={(e) => handleFormChange("reason", e.target.value)}
              />
            </label>

            <div className="modal-actions">
              <Button type="primary" onClick={handleCreateFine}>
                Tạo khoản phạt
              </Button>
              <Button onClick={handleCloseCreate}>Hủy</Button>
            </div>
          </div>
        </Modal>
      ) : null}
    </section>
  );
}

export default FinePage;
