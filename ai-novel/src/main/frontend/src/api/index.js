import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    console.log('请求:', config.method.toUpperCase(), config.url)
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一数据解包和错误处理
api.interceptors.response.use(
  response => {
    const apiResponse = response.data
    // 如果是标准ApiResponse格式，解包data
    if (apiResponse && typeof apiResponse.code !== 'undefined') {
      if (apiResponse.code === 200) {
        return apiResponse.data
      } else {
        const error = new Error(apiResponse.message || '请求失败')
        error.code = apiResponse.code
        throw error
      }
    }
    // 否则直接返回数据
    return apiResponse
  },
  error => {
    console.error('响应错误:', error)
    if (error.response?.status === 401) {
      // Token过期或无效
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
    
    // 提取错误消息
    const message = error.response?.data?.message || error.message || '网络错误'
    const customError = new Error(message)
    customError.status = error.response?.status
    customError.originalError = error
    
    return Promise.reject(customError)
  }
)

// API方法
export default {
  // 认证相关
  auth: {
    login: (credentials) => api.post('/auth/login', credentials),
    register: (userData) => api.post('/auth/register', userData),
    logout: () => api.post('/auth/logout'),
    getCurrentUser: () => api.get('/auth/me')
  },

  // 小说相关
  novels: {
    list: () => api.get('/novels'),
    get: (id) => api.get(`/novels/${id}`),
    create: (data) => api.post('/novels', data),
    update: (id, data) => api.put(`/novels/${id}`, data),
    delete: (id) => api.delete(`/novels/${id}`)
  },
  
  // 章节相关
  chapters: {
    list: (novelId) => api.get(`/chapters/novel/${novelId}`),
    get: (id) => api.get(`/chapters/${id}`),
    create: (data) => api.post('/chapters', data),
    createSmart: (data) => api.post('/chapters/smart', data),  // 智能创建
    continue: (data) => api.post('/chapters/continue', data),
    update: (id, data) => api.put(`/chapters/${id}`, data),
    delete: (id) => api.delete(`/chapters/${id}`),
    regenerate: (id, direction) => api.post(`/chapters/${id}/regenerate`, null, {
      params: { direction }
    })
  },
  
  // 健康检查
  health: () => api.get('/health'),
  
  // 智能创作相关
  smartWriting: {
    // 内容分析
    analyze: (data) => api.post('/smart-writing/analyze', data),
    getLatestAnalysis: (novelId) => api.get(`/smart-writing/analysis/novel/${novelId}/latest`),
    getAnalysisHistory: (novelId) => api.get(`/smart-writing/analysis/novel/${novelId}/history`),
    getChapterAnalysis: (chapterId) => api.get(`/smart-writing/analysis/chapter/${chapterId}`),
    
    // 续写建议
    generateSuggestions: (data) => api.post('/smart-writing/suggestions/generate', data),
    getUnadoptedSuggestions: (novelId) => api.get(`/smart-writing/suggestions/novel/${novelId}/unadopted`),
    getSuggestion: (id) => api.get(`/smart-writing/suggestions/${id}`),
    adoptSuggestion: (id, chapterId) => api.put(`/smart-writing/suggestions/${id}/adopt`, null, {
      params: { chapterId }
    }),
    
    // 组合操作
    analyzeAndSuggest: (data) => api.post('/smart-writing/analyze-and-suggest', data)
  },
  
  // 角色管理
  characters: {
    list: (novelId) => api.get(`/characters/novel/${novelId}`),
    get: (id) => api.get(`/characters/${id}`),
    create: (data) => api.post('/characters', data),
    update: (id, data) => api.put(`/characters/${id}`, data),
    delete: (id) => api.delete(`/characters/${id}`),
    recommend: (data) => api.post('/characters/recommend', data)
  },
  
  // 场景管理
  scenes: {
    list: (novelId) => api.get(`/scenes/novel/${novelId}`),
    get: (id) => api.get(`/scenes/${id}`),
    create: (data) => api.post('/scenes', data),
    update: (id, data) => api.put(`/scenes/${id}`, data),
    delete: (id) => api.delete(`/scenes/${id}`),
    recommend: (data) => api.post('/scenes/recommend', data)
  },
  
  // 大纲管理
  outlines: {
    list: (novelId) => api.get(`/outlines/novel/${novelId}`),
    getRoots: (novelId) => api.get(`/outlines/novel/${novelId}/root`),
    getChildren: (novelId, parentId) => api.get(`/outlines/novel/${novelId}/parent/${parentId}`),
    get: (id) => api.get(`/outlines/${id}`),
    create: (data) => api.post('/outlines', data),
    update: (id, data) => api.put(`/outlines/${id}`, data),
    delete: (id) => api.delete(`/outlines/${id}`),
    recommend: (data) => api.post('/outlines/recommend', data)
  },
  
  // 编辑历史
  editHistory: {
    getChapterHistory: (chapterId) => api.get(`/edit-history/chapter/${chapterId}`)
  },

  // 写作风格管理
  writingStyles: {
    getByNovelId: (novelId) => api.get(`/writing-styles/novel/${novelId}`),
    extract: (novelId, chapterId) => api.post('/writing-styles/extract', { novelId, chapterId }),
    update: (novelId, data) => api.put(`/writing-styles/novel/${novelId}`, data),
    getTemplates: () => api.get('/writing-styles/templates'),
    validate: (data) => api.post('/writing-styles/validate', data),
    compare: (novelId, chapterId) => api.get(`/writing-styles/compare/${novelId}/${chapterId}`)
  },

  // 伏笔管理
  plotHooks: {
    getByNovelId: (novelId) => api.get(`/plot-hooks/novel/${novelId}`),
    getByStatus: (novelId, status) => api.get(`/plot-hooks/novel/${novelId}/status/${status}`),
    get: (id) => api.get(`/plot-hooks/${id}`),
    create: (data) => api.post('/plot-hooks', data),
    update: (id, data) => api.put(`/plot-hooks/${id}`, data),
    delete: (id) => api.delete(`/plot-hooks/${id}`),
    hint: (id) => api.put(`/plot-hooks/${id}/hint`),
    trigger: (id, chapterNumber) => api.put(`/plot-hooks/${id}/trigger`, null, { params: { chapterNumber } }),
    resolve: (id, chapterNumber, note) => api.put(`/plot-hooks/${id}/resolve`, null, { params: { chapterNumber, note } }),
    detectFromChapter: (novelId, chapterNumber) => api.post(`/plot-hooks/detect/${novelId}/${chapterNumber}`),
    getOverdue: (novelId) => api.get(`/plot-hooks/novel/${novelId}/overdue`),
    getStatistics: (novelId) => api.get(`/plot-hooks/novel/${novelId}/statistics`)
  },

  // 章节分析
  chapterAnalysis: {
    analyze: (novelId, chapterId) => api.post(`/chapter-analysis/analyze/${novelId}/${chapterId}`),
    getByChapterId: (chapterId) => api.get(`/chapter-analysis/chapter/${chapterId}`),
    getByNovelId: (novelId) => api.get(`/chapter-analysis/novel/${novelId}`),
    getStatistics: (novelId) => api.get(`/chapter-analysis/novel/${novelId}/statistics`),
    delete: (id) => api.delete(`/chapter-analysis/${id}`)
  }
}
