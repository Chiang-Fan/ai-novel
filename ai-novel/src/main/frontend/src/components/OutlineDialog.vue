<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
      <!-- 对话框头部 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h2 class="text-2xl font-bold text-gray-900">
          {{ node ? '编辑大纲节点' : (parentId ? '添加子节点' : '添加根节点') }}
        </h2>
        <div class="flex items-center gap-2">
          <button
            v-if="!node"
            @click="handleAIRecommend"
            :disabled="recommending"
            type="button"
            class="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-lg hover:from-indigo-700 hover:to-purple-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="recommending">🤖 AI生成中...</span>
            <span v-else>✨ AI智能推荐</span>
          </button>
          <button
            @click="$emit('close')"
            class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <span class="text-2xl text-gray-500">×</span>
          </button>
        </div>
      </div>

      <!-- 表单内容 -->
      <div class="flex-1 overflow-y-auto p-6">
        <!-- AI推荐结果展示 -->
        <div v-if="recommendations.length > 0" class="mb-6">
          <div class="bg-gradient-to-r from-indigo-50 to-purple-50 rounded-lg p-4 border border-indigo-200">
            <h3 class="text-lg font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span>🤖</span>
              <span>AI推荐大纲（点击应用）</span>
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div
                v-for="(rec, index) in recommendations"
                :key="index"
                @click="applyRecommendation(rec)"
                class="bg-white rounded-lg p-3 cursor-pointer hover:shadow-lg transition-shadow border-2 border-transparent hover:border-indigo-500"
              >
                <div class="font-semibold text-gray-900 mb-1">{{ rec.title }}</div>
                <div class="text-xs text-gray-600 mb-2">{{ translateNodeType(rec.nodeType) }} | {{ rec.targetWordCount }}字</div>
                <div class="text-xs text-gray-600 line-clamp-2">{{ rec.summary }}</div>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 基本信息 -->
          <div class="bg-indigo-50 rounded-lg p-4">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">基本信息</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  节点类型 <span class="text-red-500">*</span>
                </label>
                <select
                  v-model="formData.nodeType"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                >
                  <option value="">请选择</option>
                  <option value="ARC">故事弧</option>
                  <option value="VOLUME">卷</option>
                  <option value="CHAPTER">章</option>
                  <option value="SECTION">节</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  序号 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model.number="formData.sequenceNumber"
                  type="number"
                  min="1"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="1"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">状态</label>
                <select
                  v-model="formData.status"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                >
                  <option value="PLANNED">计划中</option>
                  <option value="IN_PROGRESS">进行中</option>
                  <option value="COMPLETED">已完成</option>
                </select>
              </div>
            </div>
            
            <div class="mt-4">
              <label class="block text-sm font-medium text-gray-700 mb-2">
                标题 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="formData.title"
                type="text"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="节点标题"
              />
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">概要</label>
              <textarea
                v-model="formData.summary"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="简要描述这个节点的内容..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">目标字数</label>
              <input
                v-model.number="formData.targetWordCount"
                type="number"
                min="0"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="3000"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">关键事件</label>
              <textarea
                v-model="keyEventsText"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="每行一个关键事件"
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">每行一个关键事件</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">焦点角色</label>
              <input
                v-model="characterFocusText"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="角色名或ID，用逗号分隔"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">情节点</label>
              <textarea
                v-model="plotPointsText"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="每行一个情节点"
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">每行一个情节点</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">主题标签</label>
              <input
                v-model="themesText"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="标签，用逗号分隔"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">关联章节ID</label>
              <input
                v-model.number="formData.chapterId"
                type="number"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="如果已创建章节，输入章节ID"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">备注</label>
              <textarea
                v-model="formData.notes"
                rows="2"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="其他需要记录的信息..."
              ></textarea>
            </div>
          </div>
        </form>
      </div>

      <!-- 对话框底部 -->
      <div class="px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
        <button
          @click="$emit('close')"
          type="button"
          class="px-6 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors font-medium"
        >
          取消
        </button>
        <button
          @click="handleSubmit"
          type="submit"
          :disabled="saving"
          class="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import api from '../api'

const props = defineProps({
  node: Object,
  novelId: Number,
  parentId: Number
})

const emit = defineEmits(['close', 'save'])

const formData = ref({
  nodeType: '',
  sequenceNumber: 1,
  title: '',
  summary: '',
  status: 'PLANNED',
  targetWordCount: null,
  keyEvents: '',
  characterFocus: '',
  plotPoints: '',
  themes: '',
  chapterId: null,
  notes: ''
})

const saving = ref(false)
const recommending = ref(false)
const recommendations = ref([])

// 处理数组字段的文本转换
const keyEventsText = computed({
  get: () => {
    if (!formData.value.keyEvents) return ''
    try {
      const arr = JSON.parse(formData.value.keyEvents)
      return Array.isArray(arr) ? arr.join('\n') : ''
    } catch {
      return formData.value.keyEvents
    }
  },
  set: (val) => {
    const arr = val.split('\n').map(s => s.trim()).filter(s => s)
    formData.value.keyEvents = JSON.stringify(arr)
  }
})

const characterFocusText = computed({
  get: () => {
    if (!formData.value.characterFocus) return ''
    try {
      const arr = JSON.parse(formData.value.characterFocus)
      return Array.isArray(arr) ? arr.join(', ') : ''
    } catch {
      return formData.value.characterFocus
    }
  },
  set: (val) => {
    const arr = val.split(',').map(s => s.trim()).filter(s => s)
    formData.value.characterFocus = JSON.stringify(arr)
  }
})

const plotPointsText = computed({
  get: () => {
    if (!formData.value.plotPoints) return ''
    try {
      const arr = JSON.parse(formData.value.plotPoints)
      return Array.isArray(arr) ? arr.join('\n') : ''
    } catch {
      return formData.value.plotPoints
    }
  },
  set: (val) => {
    const arr = val.split('\n').map(s => s.trim()).filter(s => s)
    formData.value.plotPoints = JSON.stringify(arr)
  }
})

const themesText = computed({
  get: () => {
    if (!formData.value.themes) return ''
    try {
      const arr = JSON.parse(formData.value.themes)
      return Array.isArray(arr) ? arr.join(', ') : ''
    } catch {
      return formData.value.themes
    }
  },
  set: (val) => {
    const arr = val.split(',').map(s => s.trim()).filter(s => s)
    formData.value.themes = JSON.stringify(arr)
  }
})

// 监听node变化，填充表单
watch(() => props.node, (newValue) => {
  if (newValue) {
    formData.value = { ...newValue }
  }
}, { immediate: true })

const handleSubmit = async () => {
  if (!formData.value.nodeType || !formData.value.title || !formData.value.sequenceNumber) {
    alert('请填写必填项')
    return
  }

  saving.value = true
  try {
    await emit('save', formData.value)
  } catch (error) {
    alert('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const handleAIRecommend = async () => {
  if (!props.novelId) {
    alert('缺少小说ID')
    return
  }

  recommending.value = true
  try {
    const result = await api.outlines.recommend({
      novelId: props.novelId,
      parentId: props.parentId || null,
      nodeType: formData.value.nodeType || 'CHAPTER',
      count: 3
    })
    recommendations.value = result || []
    if (recommendations.value.length === 0) {
      alert('AI推荐失败，请稍后重试')
    }
  } catch (error) {
    console.error('AI推荐失败:', error)
    alert('AI推荐失败: ' + error.message)
  } finally {
    recommending.value = false
  }
}

const applyRecommendation = (rec) => {
  formData.value = {
    ...formData.value,
    nodeType: rec.nodeType,
    title: rec.title,
    summary: rec.summary,
    targetWordCount: rec.targetWordCount,
    keyEvents: JSON.stringify(rec.keyEvents || []),
    plotPoints: JSON.stringify(rec.plotPoints || []),
    themes: JSON.stringify(rec.themes || []),
    characterFocus: '',
    chapterId: null,
    notes: ''
  }
  recommendations.value = []
}

const translateNodeType = (type) => {
  const map = {
    'ARC': '故事弧',
    'VOLUME': '卷',
    'CHAPTER': '章',
    'SECTION': '节'
  }
  return map[type] || type
}
</script>
