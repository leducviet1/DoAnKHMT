import axios from "axios";

const API_URL = "http://localhost:8080/api/fines";

export const getFines = (page = 0, size = 10) =>
  axios.get(`${API_URL}?page=${page}&size=${size}`);

export const createFine = (data) => axios.post(`${API_URL}/create`, data);

export const payFine = (fineId) => axios.put(`${API_URL}/pay/${fineId}`);
