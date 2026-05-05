import axios from "axios";

const API_URL = "http://localhost:8080/api/users";

export const getUsers = (page = 0, size = 100) =>
  axios.get(`${API_URL}?page=${page}&size=${size}`);

export const getUserById = (id) => axios.get(`${API_URL}/${id}`);

export const updateUserByAdmin = (id, data) => axios.put(`${API_URL}/${id}`, data);

export const getCurrentUser = () => axios.get(`${API_URL}/me`);

export const updateCurrentUser = (data) => axios.put(`${API_URL}/me`, data);
