import axios from 'axios'
import { ElMessage } from 'element-plus'

export const apiOrigin = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60000
})

const PUBLIC_AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/reset-password']

http.interceptors.request.use((config) => {
  const url = config.url || ''
  const isPublicAuth = PUBLIC_AUTH_PATHS.some((path) => url.includes(path))
  const token = localStorage.getItem('paa_token')
  if (token && !isPublicAuth) {
    config.headers.Authorization = `Bearer ${token}`
  } else {
    delete config.headers.Authorization
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.success === false) {
      ElMessage.error(body.message || '操作失败')
      return Promise.reject(new Error(body.message || '操作失败'))
    }
    return body || response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('paa_token')
      localStorage.removeItem('paa_user')
      location.href = '/login'
      return Promise.reject(error)
    }
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 403 && !message) {
      ElMessage.error('请求被拒绝，请确认后端已重启并包含找回密码接口')
    } else {
      ElMessage.error(message || error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export function fullAssetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return `${apiOrigin}${url}`
}

export default http
