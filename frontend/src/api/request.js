import axios from "axios";
import { ElMessage } from "element-plus";
import router from "../router";

const api = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("crm_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    if (response.data?.code !== 0) {
      const message = response.data?.message || "请求失败";
      ElMessage.error(message);
      return Promise.reject(new Error(message));
    }
    return response.data.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("crm_token");
      localStorage.removeItem("crm_user");
      if (router.currentRoute.value.path !== "/login") {
        router.push("/login");
      }
    }
    ElMessage.error(error.message || "网络错误");
    return Promise.reject(error);
  },
);

export default api;
