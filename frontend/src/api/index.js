import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 200) {
      return result.data
    } else {
      ElMessage.error(result.message || '请求失败')
      throw new Error(result.message || '请求失败')
    }
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 小说相关API
export const novelApi = {
  // 获取小说列表
  list(params = {}) {
    return api.get('/novels', { params })
  },
  
  // 获取小说详情
  detail(id) {
    return api.get(`/novels/${id}`)
  },
  
  // 创建小说
  create(data) {
    return api.post('/novels', data)
  },
  
  // 更新小说
  update(id, data) {
    return api.put(`/novels/${id}`, data)
  },
  
  // 删除小说
  delete(id) {
    return api.delete(`/novels/${id}`)
  }
}

// 章节相关API
export const chapterApi = {
  // 获取章节列表
  list(novelId, params = {}) {
    return api.get(`/novels/${novelId}/chapters`, { params })
  },
  
  // 获取章节详情
  detail(novelId, chapterId) {
    return api.get(`/novels/${novelId}/chapters/${chapterId}`)
  },
  
  // 创建章节
  create(novelId, data) {
    return api.post(`/novels/${novelId}/chapters`, data)
  },
  
  // 更新章节
  update(novelId, chapterId, data) {
    return api.put(`/novels/${novelId}/chapters/${chapterId}`, data)
  }
}

export default api