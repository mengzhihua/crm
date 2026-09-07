import axios from "axios";
import { ElMessage } from "element-plus";

const api = axios.create({
  baseURL: "/api",
  timeout: 10000,
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
    ElMessage.error(error.message || "网络错误");
    return Promise.reject(error);
  },
);

export default api;
