import axios from "axios";

const API_URL = "http://localhost:8080/api/categories";

export const getCategories = (page = 0, size = 5) =>
  axios.get(`${API_URL}?page=${page}&size=${size}`);

export const createCategory = (data) =>
  axios.post(`${API_URL}/create`, data);

export const updateCategory = (id, data) =>
  axios.put(`${API_URL}/update/${id}`, data);

export const deleteCategory = (id) =>
  axios.delete(`${API_URL}/delete/${id}`);