<template>
  <div class="edit-history min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4 max-w-5xl">
      <!-- 页面头部 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">编辑历史</h1>
            <p class="mt-1 text-sm text-gray-600">查看章节的所有修改记录</p>
          </div>
          <button
            @click="loadHistory"
            class="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
          >
            🔄 刷新
          </button>
        </div>
        
        <!-- 章节选择器 -->
        <div class="bg-white rounded-lg shadow p-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">选择章节</label>
          <select
            v-model="selectedChapterId"
            @change="loadHistory"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">请选择章节</option>
            <option v-for="chapter in chapters" :key="chapter.id" :value="chapter.id">
              第{{ chapter.chapterNumber }}章 - {{ chapter.title }}
            </option>
          </select>
        </div>

        <!-- 统计信息 -->
        <div v-if="selectedChapterId" class="grid grid-cols-1 md:grid-cols-4 gap-4 mt-4">
          <div class="bg-white rounded-lg p-4 shadow">
            <div class="text-sm text-gray-600">总编辑次数</div>
            <div class="text-2xl font-bold text-gray-900">{{ history.length }}</div>
          </div>
          <div class="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">创建</div>
            <div class="text-2xl font-bold">{{ getHistoryByType('CREATE').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">更新</div>
            <div class="text-2xl font-bold">{{ getHistoryByType('UPDATE').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">重新生成</div>
            <div class="text-2xl font-bold">{{ getHistoryByType('REGENERATE').length }}</div>
          </div>
        </div>
      </div>

      <!-- 历史记录时间轴 -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-gray-300 border-t-blue-600"></div>
        <p class="mt-4 text-gray-600">加载中...</p>
      </div>

      <div v-else-if="!selectedChapterId" class="bg-white rounded-lg shadow p-12 text-center">
        <div class="text-gray-400 text-5xl mb-4">📝</div>
        <p class="text-gray-600">请在上方选择一个章节查看其编辑历史</p>
      </div>

      <div v-else-if="history.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
        <div class="text-gray-400 text-5xl mb-4">📭</div>
        <p class="text-gray-600">该章节暂无编辑记录</p>
      </div>

      <div v-else class="relative">
        <!-- 时间轴线 -->
        <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-gray-200"></div>

        <!-- 历史记录项 -->
        <div
          v-for="(record, index) in history"
          :key="record.id"
          class="relative pl-20 pb-8"
        >
          <!-- 时间轴圆点 -->
          <div 
            :class="getOperationDotClass(record.operationType)"
            class="absolute left-6 w-5 h-5 rounded-full border-4 border-white"
          ></div>

          <!-- 记录卡片 -->
          <div class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden">
            <!-- 卡片头部 -->
            <div :class="getOperationHeaderClass(record.operationType)" class="px-6 py-4 text-white">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <span class="text-2xl">{{ getOperationIcon(record.operationType) }}</span>
                  <div>
                    <h3 class="font-semibold text-lg">{{ getOperationName(record.operationType) }}</h3>
                    <p class="text-sm opacity-90">{{ formatTime(record.createdAt) }}</p>
                  </div>
                </div>
                <button
                  v-if="record.operationType === 'UPDATE'"
                  @click="viewDiff(record)"
                  class="px-3 py-1 bg-white bg-opacity-20 hover:bg-opacity-30 rounded text-sm transition-colors"
                >
                  查看对比
                </button>
              </div>
            </div>

            <!-- 卡片内容 -->
            <div class="px-6 py-4 space-y-3">
              <!-- 变更摘要 -->
              <div v-if="record.changeSummary">
                <div class="text-xs text-gray-500 font-medium mb-1">变更摘要</div>
                <div class="text-sm text-gray-900">{{ record.changeSummary }}</div>
              </div>

              <!-- 字数变化 -->
              <div v-if="record.wordCountDiff !== null && record.wordCountDiff !== 0" class="flex items-center gap-2">
                <div class="text-xs text-gray-500 font-medium">字数变化:</div>
                <span 
                  :class="record.wordCountDiff > 0 ? 'text-green-600' : 'text-red-600'" 
                  class="text-sm font-semibold"
                >
                  {{ record.wordCountDiff > 0 ? '+' : '' }}{{ record.wordCountDiff }}
                </span>
              </div>

              <!-- 编辑原因 -->
              <div v-if="record.editReason">
                <div class="text-xs text-gray-500 font-medium mb-1">编辑原因</div>
                <div class="text-sm text-gray-700">{{ record.editReason }}</div>
              </div>

              <!-- 内容预览 -->
              <div v-if="record.contentAfter" class="mt-4">
                <div class="text-xs text-gray-500 font-medium mb-2">内容预览</div>
                <div class="text-sm text-gray-700 bg-gray-50 rounded p-3 max-h-32 overflow-y-auto whitespace-pre-wrap line-clamp-5">
                  {{ record.contentAfter.substring(0, 200) }}{{ record.contentAfter.length > 200 ? '...' : '' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 对比查看对话框 -->
    <DiffDialog
      v-if="showDiffDialog"
      :record="selectedRecord"
      @close="showDiffDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import DiffDialog from '../components/DiffDialog.vue'

const route = useRoute()
const novelId = ref(parseInt(route.params.id))

const chapters = ref([])
const selectedChapterId = ref('')
const history = ref([])
const loading = ref(false)

const showDiffDialog = ref(false)
const selectedRecord = ref(null)

// 加载章节列表
const loadChapters = async () => {
  try {
    chapters.value = await api.chapters.list(novelId.value)
    // 如果URL中有chapterId参数，自动选中
    const chapterId = route.query.chapterId
    if (chapterId) {
      selectedChapterId.value = parseInt(chapterId)
      await loadHistory()
    }
  } catch (error) {
    console.error('加载章节列表失败:', error)
    alert('加载章节列表失败: ' + error.message)
  }
}

// 加载编辑历史
const loadHistory = async () => {
  if (!selectedChapterId.value) {
    history.value = []
    return
  }

  loading.value = true
  try {
    history.value = await api.editHistory.getChapterHistory(selectedChapterId.value)
  } catch (error) {
    console.error('加载编辑历史失败:', error)
    alert('加载编辑历史失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 查看对比
const viewDiff = (record) => {
  selectedRecord.value = record
  showDiffDialog.value = true
}

// 工具函数
const getHistoryByType = (type) => {
  return history.value.filter(h => h.operationType === type)
}

const getOperationName = (type) => {
  const names = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    REGENERATE: '重新生成'
  }
  return names[type] || type
}

const getOperationIcon = (type) => {
  const icons = {
    CREATE: '✨',
    UPDATE: '📝',
    DELETE: '🗑️',
    REGENERATE: '🔄'
  }
  return icons[type] || '📄'
}

const getOperationDotClass = (type) => {
  const classes = {
    CREATE: 'bg-green-500',
    UPDATE: 'bg-blue-500',
    DELETE: 'bg-red-500',
    REGENERATE: 'bg-purple-500'
  }
  return classes[type] || 'bg-gray-500'
}

const getOperationHeaderClass = (type) => {
  const classes = {
    CREATE: 'bg-gradient-to-r from-green-500 to-green-600',
    UPDATE: 'bg-gradient-to-r from-blue-500 to-blue-600',
    DELETE: 'bg-gradient-to-r from-red-500 to-red-600',
    REGENERATE: 'bg-gradient-to-r from-purple-500 to-purple-600'
  }
  return classes[type] || 'bg-gray-500'
}

const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadChapters()
})
</script>

<style scoped>
.line-clamp-5 {
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
