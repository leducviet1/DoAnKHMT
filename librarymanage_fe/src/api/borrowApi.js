import axios from "axios";

const API_URL = "http://localhost:8080/api/borrow";

export const getBorrows = (page = 0, size = 10) =>
  axios.get(`${API_URL}?page=${page}&size=${size}`);

export const getMyBorrows = (page = 0, size = 10) =>
  axios.get(`${API_URL}/me?page=${page}&size=${size}`);

export const borrowBooks = (data) => axios.post(API_URL, data);

export const returnAllBooks = (borrowId) =>
  axios.post(`${API_URL}/return-all/${borrowId}`);

export const returnBorrowItem = (borrowDetailId) =>
  axios.post(`${API_URL}/return-item/${borrowDetailId}`);

export const downloadBorrowContract = (borrowId) =>
  axios.get(`${API_URL}/${borrowId}/contract`, {
    responseType: "blob",
  });
