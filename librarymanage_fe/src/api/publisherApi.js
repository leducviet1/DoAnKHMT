import axios from "axios";

const API_URL = "http://localhost:8080/api/publishers";

export const getPublishers = async (page = 0, size = 5) => {
  const res = await axios.get(`${API_URL}?page=${page}&size=${size}`);
  return res.data;
};

export const createPublisher = async (data) => {
  return await axios.post(`${API_URL}/create`, data);
};

export const updatePublisher = async (id, data) => {
  return await axios.put(`${API_URL}/update/${id}`, data);
};

export const deletePublisher = async (id) => {
  return await axios.delete(`${API_URL}/delete/${id}`);
};
