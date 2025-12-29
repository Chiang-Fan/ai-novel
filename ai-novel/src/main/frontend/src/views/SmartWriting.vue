<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto py-6 px-4">
      <!-- 头部 -->
      <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">智能创作工作台</h1>
            <p class="mt-2 text-gray-600">AI辅助小说续写与分析系统</p>
          </div>
          <button
            @click="goBack"
            class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
          >
            返回列表
          </button>
        </div>
        
        <!-- 小说信息 -->
        <div v-if="novel" class="mt-4 p-4 bg-blue-50 rounded-lg">
          <div class="flex items-center space-x-4">
            <div class="flex-1">
              <h2 class="text-xl font-semibold text-gray-900">{{ novel.title }}</h2>
              <p class="text-sm text-gray-600 mt-1">{{ novel.description }}</p>
            </div>
            <div class="text-right">
              <div class="text-sm text-gray-600">
                <span class="font-medium">{{ novel.totalChapters }}</span> 章 · 
                <span class="font-medium">{{ novel.totalWords }}</span> 字
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主体内容区 - 三栏布局 -->
      <div class="grid grid-cols-12 gap-6">
        
        <!-- 左侧：正文输入模块 -->
        <div class="col-span-12 lg:col-span-6">
          <div class="bg-white rounded-lg shadow-sm p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-xl font-semibold text-gray-900">
                📝 正文输入
              </h2>
              <button
                @click="analyzeContent"
                :disabled="!contentInput || analyzing"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                <span v-if="analyzing">分析中...</span>
                <span v-else>智能分析</span>
              </button>
            </div>
            
            <!-- 输入区域 -->
            <textarea
              v-model="contentInput"
              placeholder="在此输入或粘贴小说片段（至少50字）...&#10;&#10;例如：&#10;张明站在城墙上，望着远方的群山。夕阳西下，余晖洒在他坚毅的脸庞上。这场战斗已经持续了三天三夜，城内的粮草即将耗尽。他知道，必须做出抉择了——是坚守到底，还是突围求援？&#10;&#10;身后传来脚步声，副将李勇快步走来：将军，敌军又增援了两千人，现在已经把东门也围住了。张明眉头紧锁，陷入了沉思..."
              class="w-full h-96 p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            ></textarea>
            
            <div class="mt-2 flex items-center justify-between text-sm text-gray-500">
              <span>字数：{{ contentInput.length }}</span>
              <span v-if="contentInput.length < 50" class="text-orange-500">
                至少需要50字才能进行有效分析
              </span>
            </div>
            
            <!-- 续写建议区域 -->
            <div v-if="suggestions.length > 0" class="mt-6">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-lg font-semibold text-gray-900">
                  💡 续写建议（{{ suggestions.length }}个）
                </h3>
                <button
                  @click="generateMoreSuggestions"
                  :disabled="generatingSuggestions"
                  class="text-sm text-blue-600 hover:text-blue-800"
                >
                  {{ generatingSuggestions ? '生成中...' : '生成更多' }}
                </button>
              </div>
              
              <div class="space-y-3">
                <div
                  v-for="suggestion in suggestions"
                  :key="suggestion.id"
                  class="p-4 border rounded-lg hover:border-blue-400 cursor-pointer transition-colors"
                  @click="selectSuggestion(suggestion)"
                  :class="{
                    'border-blue-500 bg-blue-50': selectedSuggestion?.id === suggestion.id && !suggestion.isAdopted,
                    'border-green-500 bg-green-50': suggestion.isAdopted,
                    'border-gray-200': !suggestion.isAdopted && selectedSuggestion?.id !== suggestion.id
                  }"
                >
                  <div class="flex items-start justify-between">
                    <div class="flex-1">
                      <div class="flex items-center space-x-2">
                        <h4 class="font-semibold text-gray-900">{{ suggestion.title }}</h4>
                        <!-- 已采用标记 -->
                        <span v-if="suggestion.isAdopted" class="px-2 py-1 text-xs bg-green-600 text-white rounded">
                          ✓ 已采用
                        </span>
                        <span v-else class="px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded">
                          {{ suggestion.plotDirection }}
                        </span>
                        <span class="text-xs text-gray-500">
                          优先级: {{ suggestion.priority }}/10
                        </span>
                      </div>
                      <p class="mt-1 text-sm text-gray-600">{{ suggestion.description }}</p>
                    </div>
                  </div>
                  
                  <!-- 展开详情 -->
                  <div v-if="selectedSuggestion?.id === suggestion.id" class="mt-3 space-y-2 border-t pt-3">
                    <div>
                      <span class="text-sm font-medium text-gray-700">故事发展：</span>
                      <p class="text-sm text-gray-600 mt-1">{{ suggestion.storyDevelopment }}</p>
                    </div>
                    <div>
                      <span class="text-sm font-medium text-gray-700">影响分析：</span>
                      <p class="text-sm text-gray-600 mt-1">{{ suggestion.impact }}</p>
                    </div>
                    <div class="flex items-center space-x-4 text-sm">
                      <span class="text-gray-600">
                        涉及角色: {{ suggestion.involvedCharacters || '未指定' }}
                      </span>
                      <span class="text-gray-600">
                        难度: {{ suggestion.difficultyLevel }}/5
                      </span>
                      <span class="text-gray-600">
                        预计字数: {{ suggestion.expectedWordCount }}
                      </span>
                    </div>
                    
                    <button
                      @click.stop="adoptSuggestion(suggestion)"
                      :disabled="suggestion.isAdopted"
                      class="mt-2 px-4 py-2 text-white text-sm rounded-lg transition"
                      :class="suggestion.isAdopted ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700'"
                    >
                      {{ suggestion.isAdopted ? '已采用' : '采用此方向续写' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间：智能分析结果 -->
        <div class="col-span-12 lg:col-span-3">
          <div class="bg-white rounded-lg shadow-sm p-6 sticky top-6">
            <h2 class="text-xl font-semibold text-gray-900 mb-4">
              🔍 智能分析
            </h2>
            
            <div v-if="!analysisResult" class="text-center py-8 text-gray-500">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p class="mt-2">输入内容后点击"智能分析"</p>
            </div>
            
            <div v-else class="space-y-4">
              <!-- 内容片段显示 -->
              <div v-if="analysisResult.contentSnippet" class="mb-4 p-3 bg-gray-50 rounded-lg">
                <div class="text-sm font-medium text-gray-700 mb-2">📄 分析片段</div>
                <div class="text-xs text-gray-600 max-h-32 overflow-y-auto leading-relaxed">
                  {{ analysisResult.contentSnippet }}
                </div>
              </div>
              
              <!-- 完整性得分 -->
              <div class="p-3 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg">
                <div class="text-sm text-gray-600 mb-1">分析完整度</div>
                <div class="flex items-center">
                  <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div
                      class="h-full bg-gradient-to-r from-blue-500 to-purple-500"
                      :style="{ width: analysisResult.completenessScore + '%' }"
                    ></div>
                  </div>
                  <span class="ml-2 text-lg font-semibold text-gray-900">
                    {{ analysisResult.completenessScore }}%
                  </span>
                </div>
              </div>
              
              <!-- 主角信息 -->
              <div v-if="analysisResult.protagonistName || analysisResult.viewpointCharacter">
                <div class="text-sm font-medium text-gray-700 mb-1">🌟 角色视角</div>
                <div class="space-y-2">
                  <!-- 整体主角 -->
                  <div v-if="analysisResult.protagonistName" class="px-3 py-2 bg-purple-50 rounded-lg">
                    <span class="text-xs text-gray-600">整体主角</span>
                    <div class="font-semibold text-purple-900">{{ analysisResult.protagonistName }}</div>
                  </div>
                  <!-- 视角角色 -->
                  <div v-if="analysisResult.viewpointCharacter" class="px-3 py-2 bg-blue-50 rounded-lg">
                    <span class="text-xs text-gray-600">当前视角</span>
                    <div class="font-semibold text-blue-900">
                      {{ analysisResult.viewpointCharacter }}
                      <span v-if="analysisResult.isGlobalProtagonistPov === false" 
                            class="ml-2 text-xs bg-yellow-200 text-yellow-800 px-2 py-0.5 rounded">
                        临时主角
                      </span>
                    </div>
                  </div>
                  <!-- 叙述视角 -->
                  <div v-if="analysisResult.narrativePerspective" class="px-3 py-2 bg-gray-50 rounded-lg">
                    <span class="text-xs text-gray-600">叙述视角</span>
                    <div class="text-sm text-gray-900">{{ analysisResult.narrativePerspective }}</div>
                  </div>
                </div>
              </div>
              
              <!-- 提取的角色 -->
              <div v-if="analysisResult.extractedCharacters && analysisResult.extractedCharacters.length > 0">
                <div class="text-sm font-medium text-gray-700 mb-2">👥 角色信息</div>
                <div class="space-y-2">
                  <div
                    v-for="(char, index) in analysisResult.extractedCharacters"
                    :key="index"
                    class="p-2 bg-gray-50 rounded-lg text-sm"
                  >
                    <div class="font-medium text-gray-900">{{ char.name }}</div>
                    <div class="text-xs text-gray-600 mt-1">
                      <span class="px-2 py-0.5 bg-white rounded">{{ char.role }}</span>
                      <span class="ml-2">{{ char.traits }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 场景信息 -->
              <div v-if="analysisResult.sceneInfo">
                <div class="text-sm font-medium text-gray-700 mb-2">📍 场景信息</div>
                <div class="p-3 bg-green-50 rounded-lg space-y-1 text-sm">
                  <div v-if="analysisResult.sceneInfo.location">
                    <span class="text-gray-600">位置：</span>
                    <span class="text-gray-900">{{ analysisResult.sceneInfo.location }}</span>
                  </div>
                  <div v-if="analysisResult.sceneInfo.time">
                    <span class="text-gray-600">时间：</span>
                    <span class="text-gray-900">{{ analysisResult.sceneInfo.time }}</span>
                  </div>
                  <div v-if="analysisResult.sceneInfo.atmosphere">
                    <span class="text-gray-600">氛围：</span>
                    <span class="text-gray-900">{{ analysisResult.sceneInfo.atmosphere }}</span>
                  </div>
                  <div v-if="analysisResult.sceneInfo.description" class="mt-2 pt-2 border-t border-green-200">
                    <p class="text-gray-700">{{ analysisResult.sceneInfo.description }}</p>
                  </div>
                </div>
              </div>
              
              <!-- 写作风格 -->
              <div v-if="analysisResult.styleInfo">
                <div class="text-sm font-medium text-gray-700 mb-2">✍️ 写作风格</div>
                <div class="p-3 bg-purple-50 rounded-lg space-y-1 text-sm">
                  <div v-if="analysisResult.styleInfo.style">
                    <span class="px-2 py-1 bg-purple-100 text-purple-800 rounded">
                      {{ analysisResult.styleInfo.style }}
                    </span>
                  </div>
                  <p v-if="analysisResult.styleInfo.description" class="text-gray-700 mt-2">
                    {{ analysisResult.styleInfo.description }}
                  </p>
                  <div v-if="analysisResult.styleInfo.plotPace" class="mt-2">
                    <span class="text-gray-600">节奏：</span>
                    <span class="text-gray-900">{{ analysisResult.styleInfo.plotPace }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 当前冲突 -->
              <div v-if="analysisResult.currentConflict">
                <div class="text-sm font-medium text-gray-700 mb-1">⚡ 当前冲突</div>
                <div class="p-3 bg-red-50 rounded-lg text-sm text-gray-700">
                  {{ analysisResult.currentConflict }}
                </div>
              </div>
              
              <!-- 情感基调 -->
              <div v-if="analysisResult.emotionalTone">
                <div class="text-sm font-medium text-gray-700 mb-1">💭 情感基调</div>
                <div class="px-3 py-2 bg-yellow-50 rounded-lg text-sm">
                  <span :class="{
                    'text-green-700': analysisResult.emotionalTone === '正面',
                    'text-red-700': analysisResult.emotionalTone === '负面',
                    'text-gray-700': analysisResult.emotionalTone === '中性'
                  }">
                    {{ analysisResult.emotionalTone }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：状态面板 -->
        <div class="col-span-12 lg:col-span-3">
          <div class="bg-white rounded-lg shadow-sm p-6 sticky top-6">
            <h2 class="text-xl font-semibold text-gray-900 mb-4">
              📊 状态面板
            </h2>
            
            <!-- 实时状态 -->
            <div class="space-y-4">
              <div class="p-4 bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg">
                <div class="text-sm text-blue-600 mb-1">当前状态</div>
                <div class="text-2xl font-bold text-blue-900">
                  {{ novel?.status === 'planning' ? '策划中' : novel?.status === 'writing' ? '创作中' : '已完成' }}
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-3">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <div class="text-xs text-purple-600 mb-1">总章节</div>
                  <div class="text-xl font-bold text-purple-900">{{ novel?.totalChapters || 0 }}</div>
                </div>
                <div class="p-3 bg-green-50 rounded-lg">
                  <div class="text-xs text-green-600 mb-1">总字数</div>
                  <div class="text-xl font-bold text-green-900">{{ formatNumber(novel?.totalWords || 0) }}</div>
                </div>
              </div>
              
              <!-- 分析历史 -->
              <div class="mt-6">
                <div class="flex items-center justify-between mb-2">
                  <h3 class="text-sm font-medium text-gray-700">📚 分析历史</h3>
                  <button
                    @click="loadAnalysisHistory"
                    class="text-xs text-blue-600 hover:text-blue-800"
                  >
                    刷新
                  </button>
                </div>
                
                <div v-if="analysisHistory.length === 0" class="text-sm text-gray-500 text-center py-4">
                  暂无分析记录
                </div>
                
                <div v-else class="space-y-2 max-h-60 overflow-y-auto">
                  <div
                    v-for="history in analysisHistory.slice(0, 5)"
                    :key="history.id"
                    class="p-2 bg-gray-50 rounded-lg text-xs cursor-pointer hover:bg-gray-100"
                    @click="loadAnalysisDetail(history)"
                  >
                    <div class="font-medium text-gray-900">
                      {{ history.protagonistName || '分析记录' }}
                    </div>
                    <div class="text-gray-500 mt-1">
                      {{ formatDate(history.createdAt) }}
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 快捷操作 -->
              <div class="mt-6 pt-4 border-t">
                <h3 class="text-sm font-medium text-gray-700 mb-2">⚡ 快捷操作</h3>
                <div class="space-y-2">
                  <button
                    @click="quickAnalyze"
                    class="w-full px-3 py-2 text-sm bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100"
                  >
                    快速分析
                  </button>
                  <button
                    @click="quickSuggest"
                    class="w-full px-3 py-2 text-sm bg-green-50 text-green-700 rounded-lg hover:bg-green-100"
                  >
                    快速建议
                  </button>
                  <button
                    @click="oneClickAnalyzeAndSuggest"
                    class="w-full px-3 py-2 text-sm bg-purple-50 text-purple-700 rounded-lg hover:bg-purple-100"
                  >
                    一键分析+建议
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'

const route = useRoute()
const router = useRouter()

// 数据
const novel = ref(null)
const contentInput = ref('')
const analyzing = ref(false)
const analysisResult = ref(null)
const suggestions = ref([])
const selectedSuggestion = ref(null)
const generatingSuggestions = ref(false)
const analysisHistory = ref([])

// 加载小说信息
const loadNovel = async () => {
  try {
    novel.value = await api.novels.get(route.params.id)
  } catch (error) {
    console.error('加载小说失败:', error)
    alert('加载小说失败')
  }
}

// 分析内容
const analyzeContent = async () => {
  if (!contentInput.value || contentInput.value.length < 50) {
    alert('内容至少需要50个字符才能进行有效分析')
    return
  }
  
  analyzing.value = true
  try {
    analysisResult.value = await api.smartWriting.analyze({
      novelId: novel.value.id,
      content: contentInput.value,
      deepAnalysis: true
    })
    
    // 分析成功后自动生成建议
    await generateSuggestions(analysisResult.value.id)
    
    // 刷新历史
    await loadAnalysisHistory()
  } catch (error) {
    console.error('分析失败:', error)
    alert('分析失败: ' + (error.message || '未知错误'))
  } finally {
    analyzing.value = false
  }
}

// 生成续写建议
const generateSuggestions = async (analysisId = null) => {
  generatingSuggestions.value = true
  try {
    const result = await api.smartWriting.generateSuggestions({
      novelId: novel.value.id,
      analysisId: analysisId,
      count: 3
    })
    suggestions.value = result
  } catch (error) {
    console.error('生成建议失败:', error)
    alert('生成建议失败: ' + (error.message || '未知错误'))
  } finally {
    generatingSuggestions.value = false
  }
}

// 生成更多建议
const generateMoreSuggestions = async () => {
  await generateSuggestions(analysisResult.value?.id)
}

// 选择建议
const selectSuggestion = (suggestion) => {
  selectedSuggestion.value = suggestion
}

// 采用建议
const adoptSuggestion = async (suggestion) => {
  // 这里可以跳转到续写页面，并传递建议信息
  router.push({
    name: 'ChapterWrite',
    params: { id: novel.value.id },
    query: { suggestionId: suggestion.id }
  })
}

// 加载分析历史
const loadAnalysisHistory = async () => {
  try {
    analysisHistory.value = await api.smartWriting.getAnalysisHistory(novel.value.id)
  } catch (error) {
    console.error('加载分析历史失败:', error)
  }
}

// 加载历史分析详情
const loadAnalysisDetail = (history) => {
  analysisResult.value = history
}

// 快捷操作
const quickAnalyze = () => {
  if (contentInput.value.length >= 50) {
    analyzeContent()
  } else {
    alert('请先输入至少50个字符的内容')
  }
}

const quickSuggest = async () => {
  if (!analysisResult.value) {
    alert('请先进行内容分析')
    return
  }
  await generateSuggestions(analysisResult.value.id)
}

const oneClickAnalyzeAndSuggest = async () => {
  if (!contentInput.value || contentInput.value.length < 50) {
    alert('内容至少需要50个字符')
    return
  }
  
  analyzing.value = true
  try {
    const result = await api.smartWriting.analyzeAndSuggest({
      novelId: novel.value.id,
      content: contentInput.value,
      suggestionCount: 3
    })
    
    analysisResult.value = result.analysis
    suggestions.value = result.suggestions
    
    await loadAnalysisHistory()
  } catch (error) {
    console.error('一键操作失败:', error)
    alert('操作失败: ' + (error.message || '未知错误'))
  } finally {
    analyzing.value = false
  }
}

// 工具函数
const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const goBack = () => {
  router.push('/')
}

// 初始化
onMounted(async () => {
  await loadNovel()
  await loadAnalysisHistory()
})
</script>
