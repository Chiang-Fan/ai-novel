<template>
  <div class="continuation-panel">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">🤖 智能续写助手</h2>
    
    <!-- 配置面板 -->
    <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6 mb-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">⚙️ 续写配置</h3>
      
      <div class="grid grid-cols-2 gap-4">
        <!-- 续写长度 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">续写长度</label>
          <select
            v-model="settings.lengthType"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="SHORT">短续写 (200-500字)</option>
            <option value="MEDIUM">中续写 (500-1000字)</option>
            <option value="LONG">长续写 (1000-2000字)</option>
          </select>
        </div>

        <!-- 文风类型 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">文风类型</label>
          <select
            v-model="settings.styleType"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="CONSISTENT">保持一致</option>
            <option value="CREATIVE">创意发挥</option>
            <option value="FORMAL">正式严肃</option>
            <option value="CASUAL">轻松活泼</option>
          </select>
        </div>

        <!-- 方案数量 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">方案数量: {{ settings.variantCount }}</label>
          <input
            type="range"
            v-model="settings.variantCount"
            min="1"
            max="3"
            class="w-full"
          />
        </div>

        <!-- 创意温度 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">创意温度: {{ settings.temperature }}</label>
          <input
            type="range"
            v-model="settings.temperature"
            min="0"
            max="1"
            step="0.1"
            class="w-full"
          />
        </div>

        <!-- 情节提示 -->
        <div class="col-span-2">
          <label class="block text-sm font-medium text-gray-700 mb-2">情节提示</label>
          <textarea
            v-model="settings.plotHint"
            rows="2"
            placeholder="可选：描述接下来想要发展的情节，如'主角遇到危机'、'发现重要线索'等"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- 情感基调 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">情感基调</label>
          <select
            v-model="settings.emotionTone"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="">不指定</option>
            <option value="紧张">紧张</option>
            <option value="轻松">轻松</option>
            <option value="悲伤">悲伤</option>
            <option value="欢快">欢快</option>
            <option value="神秘">神秘</option>
            <option value="激动">激动</option>
          </select>
        </div>

        <!-- 上下文长度 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">上下文长度</label>
          <input
            type="number"
            v-model="settings.contextLength"
            min="500"
            max="2000"
            step="100"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      <!-- 生成按钮 -->
      <div class="mt-6 flex space-x-4">
        <button
          @click="generateContinuation"
          :disabled="generating"
          class="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition font-semibold"
        >
          {{ generating ? '生成中...' : '✨ 生成续写' }}
        </button>
        <button
          v-if="results.length > 0"
          @click="regenerate"
          :disabled="generating"
          class="px-6 py-3 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          🔄 重新生成
        </button>
      </div>
    </div>

    <!-- 生成结果 -->
    <div v-if="results.length > 0" class="space-y-4">
      <h3 class="text-lg font-semibold text-gray-900">
        📝 生成结果（{{ results.length }} 个方案）
      </h3>

      <div :class="results.length === 1 ? 'grid-cols-1' : 'grid-cols-2'" class="grid gap-4">
        <div
          v-for="(variant, index) in results"
          :key="variant.id"
          class="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-lg transition"
        >
          <!-- 头部 -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center space-x-2">
              <span class="px-3 py-1 rounded-full text-sm font-semibold" :class="getScoreClass(variant.qualityScore)">
                方案 {{ index + 1 }}
              </span>
              <span class="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">
                {{ variant.wordCount }} 字
              </span>
            </div>
            <div class="flex space-x-2">
              <button
                @click="acceptVariant(variant)"
                :disabled="accepting === variant.id"
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 text-sm transition"
              >
                {{ accepting === variant.id ? '处理中...' : '✓ 采纳' }}
              </button>
              <button
                @click="copyVariant(variant)"
                class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-sm transition"
              >
                📋 复制
              </button>
            </div>
          </div>

          <!-- 质量评分 -->
          <div class="flex space-x-3 mb-4 text-sm">
            <span class="px-2 py-1 bg-blue-100 text-blue-800 rounded">
              质量: {{ (variant.qualityScore * 100).toFixed(0) }}%
            </span>
            <span class="px-2 py-1 bg-green-100 text-green-800 rounded">
              文风: {{ (variant.styleConsistency * 100).toFixed(0) }}%
            </span>
            <span class="px-2 py-1 bg-yellow-100 text-yellow-800 rounded">
              连贯: {{ (variant.plotCoherence * 100).toFixed(0) }}%
            </span>
          </div>

          <!-- 生成内容 -->
          <div class="bg-gray-50 rounded-lg p-4 max-h-80 overflow-y-auto mb-4">
            <p class="text-gray-800 leading-relaxed whitespace-pre-wrap" style="text-indent: 2em;">
              {{ variant.content }}
            </p>
          </div>

          <!-- 推荐理由 -->
          <div v-if="variant.recommendationReason" class="bg-blue-50 border-l-4 border-blue-400 p-3 rounded">
            <p class="text-sm text-blue-800">💡 {{ variant.recommendationReason }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载提示 -->
    <div v-if="generating" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-500 border-t-transparent"></div>
      <p class="mt-4 text-gray-600">正在生成续写方案，请稍候...</p>
    </div>

    <!-- 空状态 -->
    <div v-if="!generating && results.length === 0" class="text-center py-12 text-gray-500">
      <p class="text-lg">✨ 配置好参数后，点击"生成续写"开始创作</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import api from '@/api'

const props = defineProps({
  chapterId: {
    type: Number,
    required: true
  },
  position: {
    type: Number,
    default: -1
  }
})

const emit = defineEmits(['content-generated'])

// 配置
const settings = reactive({
  lengthType: 'MEDIUM',
  styleType: 'CONSISTENT',
  variantCount: 2,
  temperature: 0.7,
  plotHint: '',
  emotionTone: '',
  contextLength: 1000
})

// 状态
const generating = ref(false)
const accepting = ref(null)
const results = ref([])
const currentBatchId = ref(null)

// 生成续写
const generateContinuation = async () => {
  if (!props.chapterId) {
    alert('请先选择章节')
    return
  }

  generating.value = true
  results.value = []
  
  try {
    const response = await api.post('/continuation/generate', {
      chapterId: props.chapterId,
      position: props.position,
      ...settings
    })

    if (response.data.code === 0) {
      const data = response.data.data
      currentBatchId.value = data.batchId
      results.value = data.variants || []
      
      alert(`成功生成 ${results.value.length} 个续写方案！`)
    } else {
      alert('生成失败: ' + (response.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('生成续写失败:', error)
    alert('生成失败: ' + (error.response?.data?.message || error.message))
  } finally {
    generating.value = false
  }
}

// 重新生成
const regenerate = async () => {
  if (!currentBatchId.value) {
    generateContinuation()
    return
  }

  generating.value = true
  results.value = []
  
  try {
    const response = await api.post('/continuation/regenerate', {
      batchId: currentBatchId.value,
      ...settings
    })

    if (response.data.code === 0) {
      const data = response.data.data
      currentBatchId.value = data.batchId
      results.value = data.variants || []
      
      alert(`重新生成 ${results.value.length} 个方案！`)
    } else {
      alert('生成失败: ' + (response.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('重新生成失败:', error)
    alert('生成失败: ' + (error.response?.data?.message || error.message))
  } finally {
    generating.value = false
  }
}

// 采纳方案
const acceptVariant = async (variant) => {
  const feedback = prompt('确认采纳这个续写方案吗？可选：添加反馈意见', '')
  if (feedback === null) return // 用户取消

  accepting.value = variant.id
  try {
    const response = await api.put(`/continuation/${variant.id}/accept`, {
      feedback: feedback || ''
    })

    if (response.data.code === 0) {
      alert('续写已采纳！')
      emit('content-generated', variant.content)
      variant.accepted = true
    } else {
      alert('采纳失败: ' + (response.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('采纳失败:', error)
    alert('采纳失败')
  } finally {
    accepting.value = null
  }
}

// 复制内容
const copyVariant = async (variant) => {
  try {
    await navigator.clipboard.writeText(variant.content)
    alert('内容已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    alert('复制失败')
  }
}

// 获取分数样式类
const getScoreClass = (score) => {
  if (score >= 0.8) return 'bg-green-100 text-green-800'
  if (score >= 0.6) return 'bg-yellow-100 text-yellow-800'
  return 'bg-red-100 text-red-800'
}
</script>

<style scoped>
/* 所有样式已内联，使用 Tailwind CSS */
</style>
