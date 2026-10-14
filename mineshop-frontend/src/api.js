import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('mineshop_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 && !String(error.config?.url || '').includes('/auth/login')) {
      localStorage.removeItem('mineshop_token')
      localStorage.removeItem('mineshop_username')
      window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(error)
  }
)

export function errorMessage(error) {
  const data = error?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  if (data?.message) return data.message
  if (data?.error) return data.error
  if (error?.response?.status === 401) return 'Неверный логин, пароль или сессия истекла.'
  if (error?.response?.status === 404) return 'Ресурс не найден (404).'
  if (error?.message === 'Network Error') return 'Не удалось подключиться к API. Проверьте сервер и VITE_API_URL.'
  return error?.message || 'Неизвестная ошибка'
}
