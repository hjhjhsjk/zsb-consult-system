import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 统一请求层（学生端）
 * 约定同管理端：只处理传输层错误，业务码由调用方判断。
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

let redirecting = false
const toLogin = () => {
  if (redirecting) return
  redirecting = true
  localStorage.removeItem('token')
  localStorage.removeItem('uid')
  localStorage.removeItem('userInfo')
  ElMessage.warning('请先登录')
  const current = router.currentRoute.value
  router.push({
    path: '/login',
    query: current.path === '/login' ? {} : { redirect: current.fullPath }
  }).finally(() => {
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
      ElMessage.error('请求超时，请稍后重试')
    } else if (!err.response) {
      ElMessage.error('无法连接服务器，请确认后端已启动')
    } else if (status === 403) {
      ElMessage.error(err.response?.data?.msg || '没有操作权限')
    } else {
      ElMessage.error(err.response?.data?.msg || `请求失败（${status}）`)
    }
    return Promise.reject(err)
  }
)

export default request
