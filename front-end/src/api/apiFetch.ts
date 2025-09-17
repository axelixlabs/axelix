import axios from "axios";

export const API_URL = "http://158.160.200.59/api/axile/"; // todo replace this in future to correct url

const apiFetch = axios.create({
  baseURL: API_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

apiFetch.interceptors.request.use(async (config) => {
  const token = localStorage.getItem("accessToken");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiFetch.interceptors.response.use(
  (response) => response.data,

  async (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("accessToken");
      window.location.href = "/login";
    }

    return Promise.reject(err);
  }
);

export default apiFetch;
