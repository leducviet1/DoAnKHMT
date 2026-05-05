import { useEffect, useMemo, useState } from "react";
import {
  AutoComplete,
  Button,
  Input,
  InputNumber,
  Modal,
  Pagination,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  message,
} from "antd";
import { getAuthors } from "../api/authorApi";
import { getCategories } from "../api/categoryApi";
import {
  createBook,
  deleteBook,
  exportBooksExcel,
  getBooks,
  searchBooks,
  suggestBooks,
  updateBook,
} from "../api/bookApi";
import { getAuthSession, normalizeRoles } from "../api/authApi";
import { getPublishers } from "../api/publisherApi";

const { TextArea } = Input;

const initialFormValue = {
  title: "",
  totalQuantity: null,
  price: null,
  description: "",
  authorIds: [],
  newAuthorNames: [],
  categoryId: null,
  publisherId: null,
};

const initialSearchValue = {
  title: "",
  categoryName: "",
  authorName: "",
};

const getErrorMessage = (error, fallbackMessage) => {
  const payload = error?.response?.data;

  if (typeof payload === "string" && payload.trim()) return payload;
  if (payload?.message) return payload.message;
  if (payload?.error) return payload.error;

  return fallbackMessage;
};

function BookPage() {
  const session = getAuthSession();
  const roles = normalizeRoles(session?.roles);
  const canManage = roles.some((role) => ["ADMIN", "LIBRARIAN"].includes(role));

  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [categories, setCategories] = useState([]);
  const [publishers, setPublishers] = useState([]);
  const [formValue, setFormValue] = useState(initialFormValue);
  const [searchValue, setSearchValue] = useState(initialSearchValue);
  const [appliedSearchValue, setAppliedSearchValue] = useState(initialSearchValue);
  const [bookSuggestions, setBookSuggestions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [isSearching, setIsSearching] = useState(false);
  const [exporting, setExporting] = useState(false);
  const pageSize = 5;

  const formatBooks = (items = []) =>
    items.map((book) => ({
      id: book.bookId,
      title: book.title,
      price: book.price,
      availableQuantity: book.availableQuantity,
      description: book.description || "",
      categoryName: book.categoryName || "",
      publisherName: book.publisherName || "",
      authorNames: book.authorNames || [],
      bookStatus: book.bookStatus,
    }));

  const loadBooks = async (pageIndex = page, searchState = appliedSearchValue, searching = isSearching) => {
    const normalizedSearch = {
      title: searchState.title.trim(),
      categoryName: searchState.categoryName.trim(),
      authorName: searchState.authorName.trim(),
    };

    const response = searching
      ? await searchBooks({ ...normalizedSearch, page: pageIndex, size: pageSize })
      : await getBooks(pageIndex, pageSize);

    setBooks(formatBooks(response.data.content || []));
    setTotal(response.data.totalElements || 0);
  };

  useEffect(() => {
    let ignore = false;

    const load = async () => {
      try {
        const normalizedSearch = {
          title: appliedSearchValue.title.trim(),
          categoryName: appliedSearchValue.categoryName.trim(),
          authorName: appliedSearchValue.authorName.trim(),
        };

        const response = isSearching
          ? await searchBooks({ ...normalizedSearch, page, size: pageSize })
          : await getBooks(page, pageSize);

        if (ignore) return;
        setBooks(formatBooks(response.data.content || []));
        setTotal(response.data.totalElements || 0);
      } catch (error) {
        if (ignore) return;
        console.error(error);
        message.error("Không tải được danh sách sách");
      }
    };

    load();

    return () => {
      ignore = true;
    };
  }, [appliedSearchValue, isSearching, page]);

  useEffect(() => {
    if (!canManage) return undefined;

    let ignore = false;

    Promise.all([getCategories(0, 100), getAuthors(0, 100), getPublishers(0, 100)])
      .then(([categoryResponse, authorResponse, publisherResponse]) => {
        if (ignore) return;
        setCategories(categoryResponse.data.content || []);
        setAuthors(authorResponse.data.content || []);
        setPublishers(publisherResponse.content || []);
      })
      .catch((error) => {
        if (ignore) return;
        console.error(error);
        message.error("Không tải được dữ liệu hỗ trợ thêm sách");
      });

    return () => {
      ignore = true;
    };
  }, [canManage]);

  useEffect(() => {
    const timerId = window.setTimeout(() => {
      const nextSearch = {
        title: searchValue.title.trim(),
        categoryName: searchValue.categoryName.trim(),
        authorName: searchValue.authorName.trim(),
      };
      const hasKeyword = Boolean(nextSearch.title || nextSearch.categoryName || nextSearch.authorName);
      setAppliedSearchValue(nextSearch);
      setIsSearching(hasKeyword);
      setPage(0);
    }, 350);

    return () => window.clearTimeout(timerId);
  }, [searchValue]);

  useEffect(() => {
    const keyword = searchValue.title.trim();

    let ignore = false;
    const timerId = window.setTimeout(() => {
      if (!keyword) {
        setBookSuggestions([]);
        return;
      }

      suggestBooks(keyword, 8)
        .then((response) => {
          if (ignore) return;

          setBookSuggestions(
            (response.data || []).map((book) => ({
              value: book.title,
              label: (
                <div className="book-suggestion-option">
                  <strong>{book.title}</strong>
                  <span>
                    {book.categoryName || "Chưa phân loại"} -{" "}
                    {(book.authorNames || []).join(", ") || "Chưa có tác giả"}
                  </span>
                  <small>
                    Còn {book.availableQuantity ?? 0} - {book.bookStatus || "UNKNOWN"}
                  </small>
                </div>
              ),
            })),
          );
        })
        .catch((error) => {
          if (ignore) return;
          console.error(error);
        });
    }, 220);

    return () => {
      ignore = true;
      window.clearTimeout(timerId);
    };
  }, [searchValue.title]);

  const authorOptions = useMemo(
    () =>
      authors.map((author) => ({
        value: author.authorId,
        label: author.authorName,
      })),
    [authors],
  );

  const handleFormChange = (field, value) => {
    setFormValue((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleSearchChange = (field, value) => {
    setSearchValue((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleSelectSuggestion = (title) => {
    setSearchValue((current) => ({
      ...current,
      title,
    }));
    setAppliedSearchValue((current) => ({
      ...current,
      title,
    }));
    setIsSearching(true);
    setPage(0);
  };

  const getCategoryIdByName = (name) =>
    categories.find((category) => category.categoryName === name)?.categoryId || null;

  const getPublisherIdByName = (name) =>
    publishers.find((publisher) => publisher.publisherName === name)?.publisherId || null;

  const getAuthorIdsByNames = (names = []) =>
    names
      .map((name) => authors.find((author) => author.authorName === name)?.authorId)
      .filter(Boolean);

  const normalizeAuthorPayload = () => {
    const selectedAuthorIds = new Set((formValue.authorIds || []).filter(Boolean));
    const newAuthorNames = [];

    (formValue.newAuthorNames || []).forEach((name) => {
      const normalizedName = String(name || "").trim();
      if (!normalizedName) return;

      const matchedAuthor = authors.find(
        (author) => author.authorName?.trim().toLowerCase() === normalizedName.toLowerCase(),
      );

      if (matchedAuthor?.authorId) {
        selectedAuthorIds.add(matchedAuthor.authorId);
        return;
      }

      if (!newAuthorNames.some((item) => item.toLowerCase() === normalizedName.toLowerCase())) {
        newAuthorNames.push(normalizedName);
      }
    });

    return {
      authorIds: [...selectedAuthorIds],
      newAuthorNames,
    };
  };

  const handleOpenCreate = () => {
    setSelected(null);
    setFormValue(initialFormValue);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (record) => {
    setSelected(record);
    setFormValue({
      title: record.title,
      totalQuantity: null,
      price: record.price,
      description: record.description,
      authorIds: getAuthorIdsByNames(record.authorNames),
      newAuthorNames: [],
      categoryId: getCategoryIdByName(record.categoryName),
      publisherId: getPublisherIdByName(record.publisherName),
    });
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setSelected(null);
    setFormValue(initialFormValue);
    setIsFormOpen(false);
  };

  const handleSubmit = async () => {
    const normalizedAuthors = normalizeAuthorPayload();
    const payload = {
      ...formValue,
      title: formValue.title.trim(),
      description: formValue.description.trim(),
      totalQuantity: formValue.totalQuantity,
      price: formValue.price,
      authorIds: normalizedAuthors.authorIds,
      newAuthorNames: normalizedAuthors.newAuthorNames,
    };

    if (!payload.title) {
      message.warning("Vui lòng nhập tên sách");
      return;
    }

    if (payload.totalQuantity === null || payload.totalQuantity === undefined) {
      message.warning("Vui lòng nhập tổng số lượng sách");
      return;
    }

    if (
      !payload.categoryId ||
      !payload.publisherId ||
      (payload.authorIds.length === 0 && payload.newAuthorNames.length === 0)
    ) {
      message.warning("Vui lòng chọn danh mục, nhà xuất bản và nhập ít nhất một tác giả");
      return;
    }

    try {
      if (selected) {
        await updateBook(selected.id, payload);
        message.success("Cập nhật sách thành công");
      } else {
        await createBook(payload);
        message.success("Thêm sách thành công");
      }

      handleCloseForm();
      await loadBooks();
    } catch (error) {
      console.error(error);
      message.error(getErrorMessage(error, "Thao tác thất bại"));
    }
  };

  const handleSearch = () => {
    const nextSearch = {
      title: searchValue.title.trim(),
      categoryName: searchValue.categoryName.trim(),
      authorName: searchValue.authorName.trim(),
    };

    setAppliedSearchValue(nextSearch);
    setIsSearching(Boolean(nextSearch.title || nextSearch.categoryName || nextSearch.authorName));
    setPage(0);
  };

  const handleClearSearch = () => {
    setSearchValue(initialSearchValue);
    setAppliedSearchValue(initialSearchValue);
    setBookSuggestions([]);
    setIsSearching(false);
    setPage(0);
  };

  const handleDelete = async (id) => {
    try {
      await deleteBook(id);
      message.success("Xóa sách thành công");
      await loadBooks();
    } catch (error) {
      console.error(error);
      message.error(getErrorMessage(error, "Xóa sách thất bại"));
    }
  };

  const handleExportExcel = async () => {
    try {
      setExporting(true);
      const exportSize = Math.max(total || books.length || 0, 50);
      const result = await exportBooksExcel(0, exportSize);

      if (result.downloaded) {
        message.success("Đã tải file Excel");
        return;
      }

      message.info("Backend đã xử lý export nhưng chưa trả file trực tiếp về trình duyệt");
    } catch (error) {
      console.error(error);
      message.error("Xuất Excel thất bại");
    } finally {
      setExporting(false);
    }
  };

  const renderUserBookCards = () => (
    <div className="user-book-grid">
      {books.length ? (
        books.map((book) => (
          <article className="user-book-card" key={book.id}>
            <div className="user-book-card__top">
              <span className="user-book-card__id">#{book.id}</span>
              <Tag color={book.bookStatus === "AVAILABLE" ? "green" : "red"}>
                {book.bookStatus === "AVAILABLE" ? "Có sẵn" : book.bookStatus || "UNKNOWN"}
              </Tag>
            </div>

            <h3>{book.title}</h3>

            <div className="user-book-card__authors">
              {book.authorNames.length ? (
                book.authorNames.map((name) => (
                  <span className="author-chip" key={name}>
                    {name}
                  </span>
                ))
              ) : (
                <span className="muted-text">Chưa có tác giả</span>
              )}
            </div>

            <div className="user-book-card__meta">
              <span>Danh mục</span>
              <strong>{book.categoryName || "Chưa phân loại"}</strong>
            </div>
            <div className="user-book-card__meta">
              <span>Nhà xuất bản</span>
              <strong>{book.publisherName || "Chưa có"}</strong>
            </div>

            <p>{book.description || "Chưa có mô tả cho đầu sách này."}</p>

            <div className="user-book-card__footer">
              <div>
                <span>Còn lại</span>
                <strong>{book.availableQuantity ?? 0}</strong>
              </div>
              <div>
                <span>Giá sách</span>
                <strong>{Number(book.price || 0).toLocaleString("vi-VN")} đ</strong>
              </div>
            </div>
          </article>
        ))
      ) : (
        <div className="empty-state">Không tìm thấy sách phù hợp</div>
      )}
    </div>
  );

  const columns = [
    {
      title: "Mã",
      dataIndex: "id",
      width: 90,
    },
    {
      title: "Tên sách",
      dataIndex: "title",
      width: 220,
    },
    {
      title: "Tác giả",
      dataIndex: "authorNames",
      width: 260,
      render: (names) =>
        names.length ? (
          <div className="author-chip-list">
            {names.map((name) => (
              <span className="author-chip" key={name}>
                {name}
              </span>
            ))}
          </div>
        ) : (
          <span className="muted-text">Chưa có tác giả</span>
        ),
    },
    {
      title: "Danh mục",
      dataIndex: "categoryName",
      width: 160,
    },
    {
      title: "Nhà xuất bản",
      dataIndex: "publisherName",
      width: 180,
    },
    {
      title: "Còn lại",
      dataIndex: "availableQuantity",
      width: 110,
    },
    {
      title: "Giá",
      dataIndex: "price",
      width: 130,
      render: (price) => (price ? Number(price).toLocaleString("vi-VN") : "0"),
    },
    {
      title: "Trạng thái",
      dataIndex: "bookStatus",
      width: 140,
      render: (status) => (
        <Tag color={status === "AVAILABLE" ? "green" : "red"}>
          {status || "UNKNOWN"}
        </Tag>
      ),
    },
  ];

  if (canManage) {
    columns.push({
      title: "Hành động",
      width: 220,
      render: (_, record) => (
        <Space>
          <Button type="primary" onClick={() => handleOpenEdit(record)}>
            Sửa
          </Button>
          <Popconfirm
            title="Xóa sách"
            description="Bạn có chắc chắn muốn xóa sách này?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button danger>Xóa</Button>
          </Popconfirm>
        </Space>
      ),
    });
  }

  return (
    <section className="page-shell">
      <div className="page-hero">
        <div>
          <span className="eyebrow">Kho sách</span>
          <h2>{canManage ? "Quản lý danh sách sách" : "Danh sách sách"}</h2>
          <p>
            {canManage
              ? "Theo dõi thông tin sách, tác giả, danh mục, nhà xuất bản và tình trạng còn trong kho."
              : "Xem thông tin sách, tác giả, danh mục, nhà xuất bản và tình trạng còn trong kho."}
          </p>
        </div>
      </div>

      <div className="metric-row">
        <div className="metric-card">
          <strong>{total}</strong>
          <span>Tổng sách</span>
        </div>
        <div className="metric-card">
          <strong>{books.length}</strong>
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
            <h3>Danh sách sách</h3>
            <p>
              {canManage
                ? "Tìm kiếm, thêm mới và cập nhật thông tin sách."
                : "Tìm kiếm và xem thông tin sách trong thư viện."}
            </p>
          </div>
          {canManage ? (
            <Space>
              <Button onClick={handleExportExcel} loading={exporting}>
                Xuất Excel
              </Button>
              <Button type="primary" onClick={handleOpenCreate}>
                Thêm mới
              </Button>
            </Space>
          ) : null}
        </div>

        <div className="search-panel">
          <AutoComplete
            placeholder="Tìm theo tên sách"
            value={searchValue.title}
            options={bookSuggestions}
            onChange={(value) => handleSearchChange("title", value)}
            onSelect={handleSelectSuggestion}
            onKeyDown={(event) => {
              if (event.key === "Enter") handleSearch();
            }}
            allowClear
          />
          <Input
            placeholder="Tìm theo danh mục"
            value={searchValue.categoryName}
            onChange={(event) => handleSearchChange("categoryName", event.target.value)}
            onPressEnter={handleSearch}
          />
          <Input
            placeholder="Tìm theo tác giả"
            value={searchValue.authorName}
            onChange={(event) => handleSearchChange("authorName", event.target.value)}
            onPressEnter={handleSearch}
          />
          <Button type="primary" onClick={handleSearch}>
            Tìm ngay
          </Button>
          <Button onClick={handleClearSearch}>Xóa lọc</Button>
        </div>

        {canManage ? (
          <Modal
            title={selected ? "Cập nhật sách" : "Thêm sách"}
            open={isFormOpen}
            onCancel={handleCloseForm}
            footer={null}
            width={760}
            destroyOnHidden
          >
            <div className="book-form-grid">
              <label>
                <span>Tên sách</span>
                <Input
                  placeholder="Nhập tên sách"
                  value={formValue.title}
                  onChange={(event) => handleFormChange("title", event.target.value)}
                />
              </label>

              <label>
                <span>Tổng số lượng</span>
                <InputNumber
                  min={0}
                  placeholder="Nhập tổng số lượng"
                  value={formValue.totalQuantity}
                  onChange={(value) => handleFormChange("totalQuantity", value)}
                />
              </label>

              <label>
                <span>Giá</span>
                <InputNumber
                  min={0}
                  placeholder="Nhập giá sách"
                  value={formValue.price}
                  onChange={(value) => handleFormChange("price", value)}
                />
              </label>

              <label>
                <span>Danh mục</span>
                <Select
                  placeholder="Chọn danh mục"
                  value={formValue.categoryId}
                  onChange={(value) => handleFormChange("categoryId", value)}
                  options={categories.map((category) => ({
                    value: category.categoryId,
                    label: category.categoryName,
                  }))}
                />
              </label>

              <label>
                <span>Nhà xuất bản</span>
                <Select
                  placeholder="Chọn nhà xuất bản"
                  value={formValue.publisherId}
                  onChange={(value) => handleFormChange("publisherId", value)}
                  options={publishers.map((publisher) => ({
                    value: publisher.publisherId,
                    label: publisher.publisherName,
                  }))}
                />
              </label>

              <label className="wide-field">
                <span>Tác giả có sẵn</span>
                <Select
                  mode="multiple"
                  placeholder="Chọn tác giả đã có trong hệ thống"
                  value={formValue.authorIds}
                  onChange={(value) => handleFormChange("authorIds", value)}
                  options={authorOptions}
                />
              </label>

              <label className="wide-field">
                <span>Thêm tác giả mới</span>
                <Select
                  mode="tags"
                  placeholder="Nhập tên tác giả chưa có trong hệ thống"
                  value={formValue.newAuthorNames}
                  onChange={(value) => handleFormChange("newAuthorNames", value)}
                  tokenSeparators={[","]}
                  open={false}
                />
              </label>

              <p className="form-note wide-field">
                Bạn có thể chọn tác giả có sẵn, nhập thêm tác giả mới, hoặc dùng cả hai. Tên nhập mới nếu trùng tác giả đang có sẽ được tự gộp vào danh sách tác giả hiện có.
              </p>

              <label className="wide-field">
                <span>Mô tả</span>
                <TextArea
                  rows={4}
                  placeholder="Nhập mô tả sách"
                  value={formValue.description}
                  onChange={(event) => handleFormChange("description", event.target.value)}
                />
              </label>

              {selected ? (
                <p className="form-note wide-field">
                  Khi cập nhật, nhập lại tổng số lượng sách vì dữ liệu danh sách hiện chưa trả về trường này.
                </p>
              ) : null}

              <div className="modal-actions wide-field">
                <Button type="primary" onClick={handleSubmit}>
                  {selected ? "Cập nhật" : "Thêm mới"}
                </Button>
                <Button onClick={handleCloseForm}>Hủy</Button>
              </div>
            </div>
          </Modal>
        ) : null}

        {canManage ? (
          <Table
            className="book-table"
            dataSource={books}
            columns={columns}
            rowKey="id"
            scroll={{ x: 1320 }}
            pagination={{
              current: page + 1,
              pageSize,
              total,
              onChange: (nextPage) => setPage(nextPage - 1),
            }}
          />
        ) : (
          <>
            {renderUserBookCards()}
            <div className="user-book-pagination">
              <Pagination
                current={page + 1}
                pageSize={pageSize}
                total={total}
                onChange={(nextPage) => setPage(nextPage - 1)}
              />
            </div>
          </>
        )}
      </div>
    </section>
  );
}

export default BookPage;
