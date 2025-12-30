<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50">
    <!-- 头部导航 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <button 
              @click="goBack"
              class="text-gray-500 hover:text-gray-700 transition"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <h1 class="text-2xl font-bold text-gray-800">
              📝 章节编辑 - 第{{ currentChapter?.chapterNumber }}章
            </h1>
          </div>
          <button
            @click="saveChapter"
            :disabled="saving"
            class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition shadow-sm"
          >
            {{ saving ? '保存中...' : '💾 保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="max-w-7xl mx-auto px-6 py-6">
      <!-- Tab 切换 -->
      <div class="bg-white rounded-lg shadow-sm mb-6">
        <div class="border-b border-gray-200">
          <nav class="flex space-x-8 px-6" aria-label="Tabs">
            <button
              v-for="tab in tabs"
              :key="tab.name"
              @click="activeTab = tab.name"
              :class="[
                'py-4 px-1 border-b-2 font-medium text-sm transition',
                activeTab === tab.name
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              ]"
            >
              {{ tab.label }}
            </button>
          </nav>
        </div>
        
        <!-- 编辑器 Tab -->
        <div v-show="activeTab === 'editor'" class="p-6">
          <!-- 章节信息 -->
          <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6 mb-6">
            <div class="grid grid-cols-3 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">章节号</label>
                <input
                  type="number"
                  v-model="chapterForm.chapterNumber"
                  disabled
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-gray-600"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">状态</label>
                <select
                  v-model="chapterForm.status"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="DRAFT">草稿</option>
                  <option value="PUBLISHED">已发布</option>
                  <option value="COMPLETED">已完成</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">字数统计</label>
                <div class="px-3 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium">
                  {{ wordCount }} 字
                </div>
              </div>
              <div class="col-span-3">
                <label class="block text-sm font-medium text-gray-700 mb-2">章节标题</label>
                <input
                  type="text"
                  v-model="chapterForm.title"
                  placeholder="请输入章节标题"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
          </div>

          <!-- 文本编辑器 -->
          <div class="bg-white rounded-lg border border-gray-200 p-4">
            <div class="flex items-center justify-between mb-4">
              <h3 class="font-semibold text-gray-900">正文编辑</h3>
              <div class="text-sm text-gray-500">
                光标位置: {{ cursorPosition }}
              </div>
            </div>
            
            <textarea
              v-model="chapterForm.content"
              ref="contentEditor"
              placeholder="在此输入章节内容..."
              @input="updateWordCount"
              @click="updateCursorPosition"
              @keyup="updateCursorPosition"
              class="w-full h-96 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none font-serif text-base leading-relaxed"
              style="min-height: 600px;"
            />
            
            <!-- 快捷操作 -->
            <div class="flex space-x-3 mt-4">
              <button
                @click="insertAtCursor('　　')"
                class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition"
              >
                缩进
              </button>
              <button
                @click="openContinuationTab"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
              >
                🤖 智能续写
              </button>
            </div>
          </div>
        </div>

        <!-- 智能续写 Tab -->
        <div v-show="activeTab === 'continuation'" class="p-6">
          <ContinuationPanel 
            v-if="currentChapter"
            :chapter-id="currentChapter.id"
            :position="cursorPosition"
            @content-generated="handleContentGenerated"
          />
        </div>

        <!-- 章节分析 Tab -->
        <div v-show="activeTab === 'analysis'" class="p-6">
          <button
            @click="analyzeChapter"
            :disabled="analyzing"
            class="mb-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            {{ analyzing ? '分析中...' : '🔍 分析章节' }}
          </button>
          
          <div v-if="analysisResult" class="bg-white rounded-lg border border-gray-200 p-6">
            <div class="grid grid-cols-2 gap-6">
              <div>
                <div class="text-sm font-medium text-gray-700 mb-1">视角角色</div>
                <div class="text-base text-gray-900">{{ analysisResult.viewpointCharacter }}</div>
              </div>
              <div>
                <div class="text-sm font-medium text-gray-700 mb-1">叙述视角</div>
                <div class="text-base text-gray-900">{{ analysisResult.narrativePerspective }}</div>
              </div>
              <div>
                <div class="text-sm font-medium text-gray-700 mb-1">情感基调</div>
                <div class="text-base text-gray-900">{{ analysisResult.emotionalTone }}</div>
              </div>
              <div>
                <div class="text-sm font-medium text-gray-700 mb-1">当前冲突</div>
                <div class="text-base text-gray-900">{{ analysisResult.currentConflict }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 伏笔管理 Tab -->
        <div v-show="activeTab === 'hooks'" class="p-6">
          <div class="text-center py-12 text-gray-500">
            <p>章节中的伏笔管理功能...</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'
import ContinuationPanel from './ContinuationPanel.vue'

// 简单的消息提示
const showMessage = (message, type = 'success') => {
  alert(`${type === 'error' ? '错误' : '成功'}: ${message}`)
}

const route = useRoute()
const router = useRouter()

// 路由参数
const novelId = ref(Number(route.params.novelId))
const chapterId = ref(Number(route.params.chapterId))

// Tab 定义
const tabs = [
  { name: 'editor', label: '📝 章节编辑' },
  { name: 'continuation', label: '🤖 智能续写' },
  { name: 'analysis', label: '📊 章节分析' },
  { name: 'hooks', label: '🎭 伏笔管理' }
]

// 状态
const activeTab = ref('editor')
const currentChapter = ref(null)
const saving = ref(false)
const analyzing = ref(false)
const analysisResult = ref(null)

// 章节表单
const chapterForm = reactive({
  chapterNumber: 1,
  title: '',
  content: '',
  status: 'DRAFT'
})

// 编辑器状态
const contentEditor = ref(null)
const cursorPosition = ref(0)
const wordCount = computed(() => {
  return chapterForm.content ? chapterForm.content.length : 0
})

// 返回
const goBack = () => {
  router.push(`/novel/${novelId.value}`)
}

// 加载章节
const loadChapter = async () => {
  try {
    const response = await api.get(`/chapters/${chapterId.value}`)
    if (response.data.code === 0) {
      currentChapter.value = response.data.data
      
      // 填充表单
      chapterForm.chapterNumber = currentChapter.value.chapterNumber
      chapterForm.title = currentChapter.value.title
      chapterForm.content = currentChapter.value.content || ''
      chapterForm.status = currentChapter.value.status
    } else {
      showMessage('加载章节失败', 'error')
    }
  } catch (error) {
    console.error('加载章节失败:', error)
    showMessage('加载章节失败', 'error')
  }
}

// 保存章节
const saveChapter = async () => {
  saving.value = true
  try {
    const response = await api.put(`/chapters/${chapterId.value}`, {
      title: chapterForm.title,
      content: chapterForm.content,
      status: chapterForm.status,
      wordCount: wordCount.value
    })

    if (response.data.code === 0) {
      showMessage('保存成功！')
      currentChapter.value = response.data.data
    } else {
      showMessage(response.data.message || '保存失败', 'error')
    }
  } catch (error) {
    console.error('保存失败:', error)
    showMessage('保存失败', 'error')
  } finally {
    saving.value = false
  }
}

// 更新字数统计
const updateWordCount = () => {
  // 字数已通过 computed 自动计算
}

// 更新光标位置
const updateCursorPosition = () => {
  if (contentEditor.value) {
    cursorPosition.value = contentEditor.value.selectionStart
  }
}

// 在光标处插入文本
const insertAtCursor = (text) => {
  if (contentEditor.value) {
    const textarea = contentEditor.value
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const content = chapterForm.content
    
    chapterForm.content = content.substring(0, start) + text + content.substring(end)
    
    // 恢复光标位置
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + text.length
      textarea.focus()
      updateCursorPosition()
    })
  }
}

// 打开续写 Tab
const openContinuationTab = () => {
  activeTab.value = 'continuation'
}

// 处理续写生成的内容
const handleContentGenerated = (content) => {
  // 在光标位置插入续写内容
  insertAtCursor(content)
  
  // 切回编辑器
  activeTab.value = 'editor'
  
  showMessage('续写内容已插入！')
}

// 分析章节
const analyzeChapter = async () => {
  analyzing.value = true
  try {
    const response = await api.post(`/chapter-analysis/analyze/${chapterId.value}`)
    if (response.data.code === 0) {
      analysisResult.value = response.data.data
      showMessage('分析完成！')
    } else {
      showMessage(response.data.message || '分析失败', 'error')
    }
  } catch (error) {
    console.error('分析失败:', error)
    showMessage('分析失败', 'error')
  } finally {
    analyzing.value = false
  }
}

// 初始化
onMounted(() => {
  loadChapter()
})
</script>

<style scoped>
/* 所有样式已内联到模板中，使用 Tailwind CSS */
</style>
