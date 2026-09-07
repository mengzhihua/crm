import axios from 'axios'
import { ElMessage } from 'element-plus'
const api=axios.create({baseURL:'/api',timeout:10000})
api.interceptors.response.use(r=>{if(r.data?.code!==0){ElMessage.error(r.data?.message||'请求失败');return Promise.reject(new Error(r.data?.message))}return r.data.data},e=>{ElMessage.error(e.message||'网络错误');return Promise.reject(e)})
export default api
