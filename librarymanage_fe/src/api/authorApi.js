import axios from "axios";

const API_URL = "http://localhost:8080/api/authors";

export const getAuthors = (page = 0, size = 5) =>
  axios.get(`${API_URL}?page=${page}&size=${size}`);

export const createAuthor = (data) => axios.post(`${API_URL}/create`, data);

export const updateAuthor = (id, data) =>
  axios.put(`${API_URL}/update/${id}`, data);

export const deleteAuthor = (id) => axios.delete(`${API_URL}/delete/${id}`);
