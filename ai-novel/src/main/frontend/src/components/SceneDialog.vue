<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
      <!-- 对话框头部 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h2 class="text-2xl font-bold text-gray-900">
          {{ scene ? '编辑场景' : '创建场景' }}
        </h2>
        <div class="flex items-center gap-2">
          <button
            v-if="!scene"
            @click="handleAIRecommend"
            :disabled="recommending"
            type="button"
            class="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-green-600 to-emerald-600 text-white rounded-lg hover:from-green-700 hover:to-emerald-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
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
          <div class="bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg p-4 border border-green-200">
            <h3 class="text-lg font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span>🤖</span>
              <span>AI推荐场景（点击应用）</span>
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div
                v-for="(rec, index) in recommendations"
                :key="index"
                @click="applyRecommendation(rec)"
                class="bg-white rounded-lg p-3 cursor-pointer hover:shadow-lg transition-shadow border-2 border-transparent hover:border-green-500"
              >
                <div class="font-semibold text-gray-900 mb-1">{{ rec.name }}</div>
                <div class="text-xs text-gray-600 mb-2">{{ translateSceneType(rec.sceneType) }} | {{ rec.location }}</div>
                <div class="text-xs text-gray-600 line-clamp-2">{{ rec.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 基本信息 -->
          <div class="bg-green-50 rounded-lg p-4">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">基本信息</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  场景名称 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="formData.name"
                  type="text"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                  placeholder="例如：紫禁城、夜袭计划、战国时代"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  场景类型 <span class="text-red-500">*</span>
                </label>
                <select
                  v-model="formData.sceneType"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                >
                  <option value="">请选择</option>
                  <option value="LOCATION">地点</option>
                  <option value="EVENT">事件</option>
                  <option value="TIME_PERIOD">时间段</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">地点</label>
                <input
                  v-model="formData.location"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                  placeholder="具体地点"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">时间段</label>
                <input
                  v-model="formData.timePeriod"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                  placeholder="例如：清晨、深夜、春季"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">天气</label>
                <input
                  v-model="formData.weather"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                  placeholder="例如：晴朗、暴雨、雪天"
                />
              </div>
            </div>
          </div>

          <!-- 详细描述 -->
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">场景描述</label>
              <textarea
                v-model="formData.description"
                rows="4"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="描述场景的外观、布局、特征等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">氛围描述</label>
              <textarea
                v-model="formData.atmosphere"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="场景的氛围、情绪、基调等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">重要道具</label>
              <textarea
                v-model="propsText"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="每行一个道具"
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">每行一个道具</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">相关角色ID</label>
              <input
                v-model="involvedCharactersText"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="角色ID，用逗号分隔"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">出现章节</label>
              <input
                v-model="chapterReferencesText"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="章节ID，用逗号分隔"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">备注</label>
              <textarea
                v-model="formData.notes"
                rows="2"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
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
          class="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
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
  scene: Object,
  novelId: Number
})

const emit = defineEmits(['close', 'save'])

const formData = ref({
  name: '',
  sceneType: '',
  location: '',
  timePeriod: '',
  weather: '',
  description: '',
  atmosphere: '',
  props: '',
  involvedCharacters: '',
  chapterReferences: '',
  notes: ''
})

const saving = ref(false)
const recommending = ref(false)
const recommendations = ref([])

// 处理props数组和文本转换
const propsText = computed({
  get: () => {
    if (!formData.value.props) return ''
    try {
      const arr = JSON.parse(formData.value.props)
      return Array.isArray(arr) ? arr.join('\n') : ''
    } catch {
      return formData.value.props
    }
  },
  set: (val) => {
    const arr = val.split('\n').map(s => s.trim()).filter(s => s)
    formData.value.props = JSON.stringify(arr)
  }
})

// 处理相关角色JSON
const involvedCharactersText = computed({
  get: () => {
    if (!formData.value.involvedCharacters) return ''
    try {
      const arr = JSON.parse(formData.value.involvedCharacters)
      return Array.isArray(arr) ? arr.join(', ') : ''
    } catch {
      return formData.value.involvedCharacters
    }
  },
  set: (val) => {
    const arr = val.split(',').map(s => s.trim()).filter(s => s)
    formData.value.involvedCharacters = JSON.stringify(arr)
  }
})

// 处理章节引用JSON
const chapterReferencesText = computed({
  get: () => {
    if (!formData.value.chapterReferences) return ''
    try {
      const arr = JSON.parse(formData.value.chapterReferences)
      return Array.isArray(arr) ? arr.join(', ') : ''
    } catch {
      return formData.value.chapterReferences
    }
  },
  set: (val) => {
    const arr = val.split(',').map(s => s.trim()).filter(s => s)
    formData.value.chapterReferences = JSON.stringify(arr)
  }
})

// 监听scene变化，填充表单
watch(() => props.scene, (newValue) => {
  if (newValue) {
    formData.value = { ...newValue }
  }
}, { immediate: true })

const handleSubmit = async () => {
  if (!formData.value.name || !formData.value.sceneType) {
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
    const result = await api.scenes.recommend({
      novelId: props.novelId,
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
    name: rec.name,
    sceneType: rec.sceneType,
    location: rec.location,
    timePeriod: rec.timePeriod,
    weather: rec.weather,
    description: rec.description,
    atmosphere: rec.atmosphere,
    props: JSON.stringify(rec.props || []),
    involvedCharacters: '',
    chapterReferences: '',
    notes: ''
  }
  recommendations.value = []
}

const translateSceneType = (type) => {
  const map = {
    'LOCATION': '地点',
    'EVENT': '事件',
    'TIME_PERIOD': '时间段'
  }
  return map[type] || type
}
</script>
