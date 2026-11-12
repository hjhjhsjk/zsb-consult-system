import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 统一请求层（管理端）
 * 约定：
 *  1. baseURL=/api，由 Vite 代理去掉前缀转发到后端
 *  2. 自动带 Bearer token
 *  3. 只在这里处理「传输层」错误（401 / 超时 / 断网 / 5xx）；
 *     业务错误（code !== 200）交给调用方判断，避免同一个错误弹两次
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 401 可能同时来好几个请求，只跳一次登录页
let redirecting = false
const toLogin = () => {
  if (redirecting) return
  redirecting = true
  localStorage.removeItem('token')
  localStorage.removeItem('uid')
  localStorage.removeItem('admin_userInfo')
  ElMessage.warning('登录已过期，请重新登录')
  router.replace('/login').finally(() => {
    setTimeout(() => { redirecting = false }, 1000)
  })
}

request.interceptors.response.use(
  res => res.data,
  err => {
    const status = err.response?.status
    if (status === 401) {
      toLogin()
    } else if (err.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络后重试')
    } else if (!err.response) {
      ElMessage.error('无法连接服务器，请确认后端已启动')
    } else {
      ElMessage.error(err.response?.data?.msg || `请求失败（${status}）`)
    }
    return Promise.reject(err)
  }
)

export default request
