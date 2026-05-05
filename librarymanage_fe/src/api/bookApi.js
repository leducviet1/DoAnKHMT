import axios from "axios";
import { getAuthSession } from "./authApi";

const API_URL = "http://localhost:8080/api/books";

export const getBooks = (page = 0, size = 5) =>
  axios.get(`${API_URL}?page=${page}&size=${size}`);

export const searchBooks = ({ page = 0, size = 5, title, categoryName, authorName }) => {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  if (title) params.set("title", title);
  if (categoryName) params.set("categoryName", categoryName);
  if (authorName) params.set("authorName", authorName);

  return axios.get(`${API_URL}/search?${params.toString()}`);
};

export const suggestBooks = (keyword, limit = 8) =>
  axios.get(`${API_URL}/suggest`, {
    params: {
      keyword,
      limit,
    },
  });

export const exportBooksExcel = async (page = 0, size = 200) => {
  const session = getAuthSession();
  const response = await axios.get(`${API_URL}/export`, {
    params: {
      page,
      size,
    },
    responseType: "blob",
    validateStatus: (status) => status >= 200 && status < 500,
  });

  if (response.status >= 400) {
    throw new Error("Không thể xuất Excel");
  }

  const blob = response.data;
  const contentDisposition = response.headers["content-disposition"] || "";
  const matchedFileName = contentDisposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/i);
  const fallbackName = `books_export_${new Date().toISOString().slice(0, 10)}.xlsx`;
  const fileName = matchedFileName
    ? decodeURIComponent(matchedFileName[1].replace(/"/g, ""))
    : fallbackName;

  const contentType = response.headers["content-type"] || blob?.type || "";
  const isFileResponse =
    contentDisposition.toLowerCase().includes("attachment") ||
    contentType.includes(
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    );

  if (!isFileResponse || !(blob instanceof Blob) || blob.size === 0) {
    return {
      downloaded: false,
      fileName,
      serverHandled: true,
    };
  }

  const blobUrl = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(blobUrl);

  return {
    downloaded: true,
    fileName,
    serverHandled: Boolean(session?.token),
  };
};

export const createBook = (data) => axios.post(`${API_URL}/create`, data);

export const updateBook = (id, data) =>
  axios.put(`${API_URL}/update/${id}`, data);

export const deleteBook = (id) => axios.delete(`${API_URL}/delete/${id}`);
