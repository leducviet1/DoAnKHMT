import axios from "axios";

const API_URL = "http://localhost:8080/api/statistics";

export const getDashboardStatistics = (params = {}) =>
  axios.get(`${API_URL}/dashboard`, {
    params,
  });
