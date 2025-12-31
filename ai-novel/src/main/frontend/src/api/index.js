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
  },

  // 🔥 Qwen风格画像相关（新增）
  novelWritingStyle: {
    // 获取小说的四维风格画像
    getByNovelId: (novelId) => api.get(`/novel-writing-styles/novel/${novelId}`),
    // 批量分析小说章节生成风格画像
    analyzeBatch: (novelId) => api.post(`/novel-writing-styles/analyze/${novelId}`),
    // 异步分析单章节
    analyzeChapter: (chapterId) => api.post(`/novel-writing-styles/analyze-chapter/${chapterId}`),
    // 更新风格画像
    update: (novelId, data) => api.put(`/novel-writing-styles/novel/${novelId}`, data)
  },

  // 🔥 数据迁移管理（新增）
  migration: {
    // 执行批量迁移
    execute: () => api.post('/migration/execute'),
    // 验证迁移结果
    validate: () => api.get('/migration/validate'),
    // 回滚迁移
    rollback: () => api.post('/migration/rollback')
  },

  // 🔥 文本导入管理（新增）
  textImport: {
    // 上传并解析文本文件
    uploadAndParse: (novelId, file) => {
      const formData = new FormData()
      formData.append('file', file)
      return api.post(`/text-import/${novelId}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    // 获取导入记录
    get: (importId) => api.get(`/text-import/${importId}`),
    // 获取导入的章节列表
    getChapters: (importId) => api.get(`/text-import/${importId}/chapters`),
    // 确认导入（触发AI深度分析）
    confirm: (importId) => api.post(`/text-import/${importId}/confirm`),
    // 提取关键要素
    extractElements: (importId) => api.post(`/text-import/${importId}/extract`)
  },

  // 🔥 世界设定管理（新增）
  worldSettings: {
    list: (novelId) => api.get(`/world-settings/novel/${novelId}`),
    get: (id) => api.get(`/world-settings/${id}`),
    create: (data) => api.post('/world-settings', data),
    update: (id, data) => api.put(`/world-settings/${id}`, data),
    delete: (id) => api.delete(`/world-settings/${id}`)
  },
  
  // 🔥 优化管理（新增）
  optimization: {
    // 检查优化
    check: (data) => api.post('/optimization/check', data),
    // 优化内容
    optimize: (data) => api.post('/optimization/optimize', data),
    // 获取优化历史
    getHistory: (chapterId) => api.get(`/optimization/history/${chapterId}`),
    // 获取优化规则
    getRules: () => api.get('/optimization/rules'),
    // 应用优化
    apply: (optimizationId) => api.post(`/optimization/${optimizationId}/apply`),
    // 评分优化
    rate: (optimizationId, rating) => api.post(`/optimization/${optimizationId}/rate`, { rating })
  },
  
  // 🔥 节奏控制（新增）
  paceControl: {
    // 获取小说综合节奏分析
    getComprehensiveAnalysis: (novelId) => api.get(`/pace-control/comprehensive/${novelId}`),
    // 获取章节反馈
    getFeedbackByChapter: (chapterId) => api.get(`/pace-control/feedback/chapter/${chapterId}`),
    // 获取小说反馈
    getFeedbackByNovel: (novelId) => api.get(`/pace-control/feedback/novel/${novelId}`),
    // 获取反馈报告
    getFeedbackReport: () => api.get('/pace-control/feedback/report'),
    // 获取小说节奏分析
    getRhythmAnalysis: (novelId) => api.get(`/pace-control/rhythm/novel/${novelId}`),
    // 获取节奏建议
    getRhythmRecommendations: (novelId) => api.get(`/pace-control/rhythm/recommendations/${novelId}`),
    // 获取场景节奏
    getSceneRhythm: (sceneId) => api.get(`/pace-control/rhythm/scene/${sceneId}`),
    // 获取小说字数分析
    getWordCountAnalysis: (novelId) => api.get(`/pace-control/word-count/novel/${novelId}`),
    // 获取字数优化建议
    getWordCountOptimizationSuggestions: (novelId) => api.get(`/pace-control/word-count/optimization-suggestions/${novelId}`),
    // 获取场景字数分析
    getSceneWordCount: (sceneId) => api.get(`/pace-control/word-count/scene/${sceneId}`),
    // 获取字数总结
    getWordCountSummary: (novelId) => api.get(`/pace-control/word-count/summary/${novelId}`),
    // 验证字数
    validateWordCount: (data) => api.post('/pace-control/word-count/validate', data),
    // 设置目标字数
    setTargetWordCount: (data) => api.post('/pace-control/word-count/target', data)
  },
  
  // 🔥 情节模拟（新增）
  plotSimulation: {
    // 创建模拟
    create: (data) => api.post('/plot-simulation/create', data),
    // 获取小说模拟
    getByNovelId: (novelId) => api.get(`/plot-simulation/novel/${novelId}`),
    // 获取模拟详情
    get: (simulationId) => api.get(`/plot-simulation/${simulationId}`),
    // 获取模拟分支
    getBranch: (branchId) => api.get(`/plot-simulation/branch/${branchId}/predict`),
    // 获取结局分析
    getEndingsAnalysis: () => api.get('/plot-simulation/endings/analyze'),
    // 获取模拟树
    getTree: (simulationId) => api.get(`/plot-simulation/${simulationId}/tree`),
    // 创建分支
    createBranch: (simulationId, data) => api.post(`/plot-simulation/${simulationId}/branch`, data)
  },
  
  // 🔥 通用建议管理（新增）
  suggestions: {
    // 为章节生成建议
    generateForChapter: (chapterId) => api.post(`/suggestions/generate/chapter/${chapterId}`),
    // 为小说生成建议
    generateForNovel: (novelId) => api.post(`/suggestions/generate/novel/${novelId}`),
    // 获取小说建议
    getByNovelId: (novelId) => api.get(`/suggestions/novel/${novelId}`),
    // 获取小说活跃建议
    getActiveByNovelId: (novelId) => api.get(`/suggestions/novel/${novelId}/active`),
    // 获取小说高优先级建议
    getHighPriorityByNovelId: (novelId) => api.get(`/suggestions/novel/${novelId}/high-priority`),
    // 获取小说建议统计
    getStatistics: (novelId) => api.get(`/suggestions/novel/${novelId}/statistics`),
    // 按类型获取建议
    getByType: (novelId, type) => api.get(`/suggestions/novel/${novelId}/type/${type}`),
    // 清理建议
    cleanup: (novelId) => api.post(`/suggestions/novel/${novelId}/cleanup`),
    // 接受建议
    accept: (suggestionId) => api.post(`/suggestions/${suggestionId}/accept`),
    // 拒绝建议
    reject: (suggestionId) => api.post(`/suggestions/${suggestionId}/reject`)
  }
}