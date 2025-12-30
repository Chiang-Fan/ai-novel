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
              🤖 AI 创作助手
            </h1>
          </div>
          <button
            @click="createNewSession"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition shadow-sm"
          >
            ➕ 新建对话
          </button>
        </div>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="max-w-7xl mx-auto px-6 py-6">
      <div class="grid grid-cols-12 gap-6">
        <!-- 左侧：会话列表 -->
        <div class="col-span-3">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-4 border-b border-gray-200">
              <h3 class="font-semibold text-gray-900">对话历史</h3>
            </div>
            <div class="overflow-y-auto" style="max-height: calc(100vh - 250px);">
              <div v-if="sessions.length === 0" class="p-6 text-center text-gray-500">
                <p>暂无对话</p>
              </div>
              <div
                v-for="session in sessions"
                :key="session.id"
                @click="selectSession(session)"
                :class="[
                  'p-4 border-b border-gray-100 cursor-pointer transition',
                  currentSessionId === session.id
                    ? 'bg-blue-50 border-l-4 border-l-blue-500'
                    : 'hover:bg-gray-50'
                ]"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <h4 class="text-sm font-medium text-gray-900 truncate">
                      {{ session.sessionTitle || '未命名对话' }}
                    </h4>
                    <p class="text-xs text-gray-500 mt-1">
                      {{ formatSessionType(session.sessionType) }}
                    </p>
                    <p class="text-xs text-gray-400 mt-1">
                      {{ session.totalMessages }} 条消息
                    </p>
                  </div>
                  <button
                    @click.stop="deleteSession(session.id)"
                    class="text-gray-400 hover:text-red-600 ml-2"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：对话区域 -->
        <div class="col-span-9">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 flex flex-col" style="height: calc(100vh - 200px);">
            <!-- 会话头部 -->
            <div v-if="currentSession" class="p-4 border-b border-gray-200 flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-gray-900">
                  {{ currentSession.sessionTitle || '未命名对话' }}
                </h3>
                <p class="text-sm text-gray-500 mt-1">
                  {{ formatSessionType(currentSession.sessionType) }}
                </p>
              </div>
              <div class="flex space-x-2">
                <button
                  @click="archiveSession"
                  class="px-3 py-1 text-sm text-gray-600 hover:text-gray-800 border border-gray-300 rounded hover:bg-gray-50 transition"
                >
                  📦 归档
                </button>
                <button
                  @click="clearMessages"
                  class="px-3 py-1 text-sm text-gray-600 hover:text-gray-800 border border-gray-300 rounded hover:bg-gray-50 transition"
                >
                  🗑️ 清空
                </button>
              </div>
            </div>

            <!-- 消息区域 -->
            <div 
              ref="messagesContainer"
              class="flex-1 overflow-y-auto p-6 space-y-4"
            >
              <div v-if="!currentSession" class="h-full flex items-center justify-center text-gray-400">
                <div class="text-center">
                  <svg class="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                  <p class="text-lg">选择或创建一个对话开始</p>
                </div>
              </div>

              <div v-else-if="messages.length === 0" class="h-full flex items-center justify-center text-gray-400">
                <div class="text-center">
                  <p class="text-lg mb-4">👋 你好！我是你的 AI 创作助手</p>
                  <p class="text-sm">有什么我可以帮助你的吗？</p>
                </div>
              </div>

              <!-- 消息列表 -->
              <div
                v-for="message in messages"
                :key="message.id"
                :class="[
                  'flex',
                  message.role === 'USER' ? 'justify-end' : 'justify-start'
                ]"
              >
                <div
                  :class="[
                    'max-w-3xl rounded-lg p-4',
                    message.role === 'USER'
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-900'
                  ]"
                >
                  <div class="flex items-start space-x-3">
                    <div class="flex-shrink-0 mt-1">
                      <div
                        :class="[
                          'w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium',
                          message.role === 'USER'
                            ? 'bg-blue-500 text-white'
                            : 'bg-gray-300 text-gray-700'
                        ]"
                      >
                        {{ message.role === 'USER' ? '👤' : '🤖' }}
                      </div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm whitespace-pre-wrap break-words">{{ message.content }}</p>
                      <p
                        :class="[
                          'text-xs mt-2',
                          message.role === 'USER' ? 'text-blue-200' : 'text-gray-500'
                        ]"
                      >
                        {{ formatTime(message.createdAt) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 加载中 -->
              <div v-if="loading" class="flex justify-start">
                <div class="bg-gray-100 rounded-lg p-4">
                  <div class="flex items-center space-x-2">
                    <div class="flex space-x-1">
                      <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms;"></div>
                      <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms;"></div>
                      <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms;"></div>
                    </div>
                    <span class="text-sm text-gray-600">AI 正在思考...</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 输入区域 -->
            <div class="p-4 border-t border-gray-200">
              <!-- 快捷模板 -->
              <div class="mb-3 flex flex-wrap gap-2">
                <button
                  v-for="template in quickTemplates"
                  :key="template.text"
                  @click="useTemplate(template)"
                  class="px-3 py-1 text-xs bg-gray-100 text-gray-700 rounded-full hover:bg-gray-200 transition"
                >
                  {{ template.text }}
                </button>
              </div>

              <!-- 输入框 -->
              <div class="flex space-x-3">
                <textarea
                  v-model="inputMessage"
                  @keydown.enter.prevent="handleEnter"
                  placeholder="输入你的问题..."
                  rows="3"
                  class="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                  :disabled="loading || !currentSession"
                ></textarea>
                <button
                  @click="sendMessage"
                  :disabled="loading || !inputMessage.trim() || !currentSession"
                  class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition self-end"
                >
                  <svg v-if="!loading" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                  <div v-else class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建会话对话框 -->
    <div
      v-if="showCreateDialog"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showCreateDialog = false"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <h3 class="text-xl font-bold text-gray-900 mb-4">创建新对话</h3>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">对话标题</label>
            <input
              v-model="newSession.title"
              type="text"
              placeholder="给这个对话起个名字..."
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">对话类型</label>
            <select
              v-model="newSession.type"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="GENERAL">💬 通用对话</option>
              <option value="WRITING_ADVICE">✍️ 写作建议</option>
              <option value="PLOT_CONSULTATION">📖 情节咨询</option>
              <option value="STYLE_GUIDANCE">🎨 文风指导</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">初始消息（可选）</label>
            <textarea
              v-model="newSession.initialMessage"
              placeholder="可以输入一个问题或需求..."
              rows="3"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            ></textarea>
          </div>
        </div>

        <div class="flex justify-end space-x-3 mt-6">
          <button
            @click="showCreateDialog = false"
            class="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
          >
            取消
          </button>
          <button
            @click="confirmCreateSession"
            :disabled="!newSession.title.trim()"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            创建
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'

const route = useRoute()
const router = useRouter()

// 路由参数
const novelId = ref(route.params.novelId)

// 状态
const sessions = ref([])
const currentSession = ref(null)
const currentSessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const showCreateDialog = ref(false)

// 新建会话表单
const newSession = reactive({
  title: '',
  type: 'GENERAL',
  initialMessage: ''
})

// 快捷模板
const quickTemplates = [
  { text: '💡 帮我想情节发展', prompt: '请帮我分析当前情节，并提供几个可能的发展方向。' },
  { text: '✍️ 优化这段文字', prompt: '请帮我优化一下最近写的内容，让它更生动。' },
  { text: '👥 角色性格分析', prompt: '请帮我分析一下主要角色的性格特征和发展空间。' },
  { text: '🎭 制造冲突', prompt: '如何在当前情节中制造更强烈的戏剧冲突？' },
  { text: '📊 章节结构建议', prompt: '请给我一些关于章节结构和节奏的建议。' }
]

// 消息容器引用
const messagesContainer = ref(null)

// 加载会话列表
const loadSessions = async () => {
  try {
    const response = await api.get(`/conversation/sessions/novel/${novelId.value}`)
    if (response.data.code === 0) {
      sessions.value = response.data.data
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 选择会话
const selectSession = async (session) => {
  currentSessionId.value = session.id
  currentSession.value = session
  await loadMessages(session.id)
}

// 加载消息
const loadMessages = async (sessionId) => {
  try {
    const response = await api.get(`/conversation/sessions/${sessionId}`)
    if (response.data.code === 0) {
      messages.value = response.data.data.messages || []
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

// 创建新会话
const createNewSession = () => {
  newSession.title = ''
  newSession.type = 'GENERAL'
  newSession.initialMessage = ''
  showCreateDialog.value = true
}

// 确认创建会话
const confirmCreateSession = async () => {
  if (!newSession.title.trim()) return

  try {
    const response = await api.post('/conversation/sessions', {
      novelId: parseInt(novelId.value),
      sessionTitle: newSession.title,
      sessionType: newSession.type,
      initialMessage: newSession.initialMessage || null
    })

    if (response.data.code === 0) {
      showCreateDialog.value = false
      await loadSessions()
      
      const createdSession = response.data.data
      await selectSession(createdSession)
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    alert('创建会话失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSessionId.value || loading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  loading.value = true

  try {
    const response = await api.post('/conversation/messages', {
      sessionId: currentSessionId.value,
      content: userMessage
    })

    if (response.data.code === 0) {
      const newMessage = response.data.data
      messages.value.push(newMessage)
      
      // 更新会话消息数
      if (currentSession.value) {
        currentSession.value.totalMessages = messages.value.length
      }
      
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    alert('发送消息失败')
  } finally {
    loading.value = false
  }
}

// 使用模板
const useTemplate = (template) => {
  inputMessage.value = template.prompt
}

// 处理 Enter 键
const handleEnter = (e) => {
  if (!e.shiftKey) {
    sendMessage()
  } else {
    // Shift+Enter 换行
    inputMessage.value += '\n'
  }
}

// 归档会话
const archiveSession = async () => {
  if (!currentSessionId.value) return

  if (!confirm('确定要归档这个对话吗？')) return

  try {
    const response = await api.put(`/conversation/sessions/${currentSessionId.value}/archive`)
    if (response.data.code === 0) {
      await loadSessions()
      currentSession.value = null
      currentSessionId.value = null
      messages.value = []
    }
  } catch (error) {
    console.error('归档会话失败:', error)
    alert('归档会话失败')
  }
}

// 删除会话
const deleteSession = async (sessionId) => {
  if (!confirm('确定要删除这个对话吗？此操作不可恢复！')) return

  try {
    const response = await api.delete(`/conversation/sessions/${sessionId}`)
    if (response.data.code === 0) {
      await loadSessions()
      if (currentSessionId.value === sessionId) {
        currentSession.value = null
        currentSessionId.value = null
        messages.value = []
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    alert('删除会话失败')
  }
}

// 清空消息
const clearMessages = () => {
  if (!confirm('确定要清空所有消息吗？')) return
  messages.value = []
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化会话类型
const formatSessionType = (type) => {
  const types = {
    GENERAL: '💬 通用对话',
    WRITING_ADVICE: '✍️ 写作建议',
    PLOT_CONSULTATION: '📖 情节咨询',
    STYLE_GUIDANCE: '🎨 文风指导'
  }
  return types[type] || type
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 返回
const goBack = () => {
  router.back()
}

// 监听消息变化，自动滚动
watch(() => messages.value.length, () => {
  nextTick(() => scrollToBottom())
})

// 初始化
onMounted(async () => {
  await loadSessions()
  
  // 如果有会话，自动选择第一个
  if (sessions.value.length > 0) {
    await selectSession(sessions.value[0])
  }
})
</script>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
