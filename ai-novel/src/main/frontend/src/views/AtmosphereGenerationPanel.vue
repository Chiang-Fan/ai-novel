<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 to-pink-50 p-6">
    <!-- 头部 -->
    <div class="mb-6">
      <button
        @click="$router.back()"
        class="mb-4 text-purple-600 hover:text-purple-700 flex items-center gap-1"
      >
        ← 返回
      </button>
      <h1 class="text-3xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
        🎭 场景氛围生成
      </h1>
      <p v-if="scene" class="text-gray-600 mt-2">{{ scene.name }}</p>
    </div>

    <!-- 标签页 -->
    <div class="bg-white rounded-xl shadow-lg overflow-hidden">
      <div class="flex border-b">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'flex-1 py-4 px-6 font-medium transition-all',
            activeTab === tab.key
              ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
              : 'text-gray-600 hover:bg-gray-50'
          ]"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="p-6">
        <!-- Tab 1: AI生成 -->
        <div v-if="activeTab === 'generate'">
          <div class="max-w-3xl mx-auto">
            <!-- 生成配置 -->
            <div class="space-y-4 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">氛围类型</label>
                <select
                  v-model="generateForm.atmosphereType"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                >
                  <option value="PEACEFUL">平和 (Peaceful)</option>
                  <option value="TENSE">紧张 (Tense)</option>
                  <option value="ROMANTIC">浪漫 (Romantic)</option>
                  <option value="MYSTERIOUS">神秘 (Mysterious)</option>
                  <option value="EXCITING">激动 (Exciting)</option>
                  <option value="MELANCHOLIC">忧郁 (Melancholic)</option>
                  <option value="HORROR">恐怖 (Horror)</option>
                  <option value="JOYFUL">欢乐 (Joyful)</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">情节上下文</label>
                <textarea
                  v-model="generateForm.plotContext"
                  placeholder="请简要描述当前情节，AI会根据情节生成合适的氛围..."
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-none"
                  rows="3"
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">情感基调</label>
                <input
                  v-model="generateForm.emotionalTone"
                  type="text"
                  placeholder="如：温馨、紧张、悲伤..."
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">期望字数</label>
                  <input
                    v-model.number="generateForm.length"
                    type="number"
                    placeholder="200"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">参考模板</label>
                  <select
                    v-model="generateForm.templateId"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  >
                    <option :value="null">不使用模板</option>
                    <option
                      v-for="template in templates"
                      :key="template.id"
                      :value="template.id"
                    >
                      {{ template.name }}
                    </option>
                  </select>
                </div>
              </div>
            </div>

            <!-- 生成按钮 -->
            <button
              @click="generateAtmosphere"
              :disabled="generating"
              class="w-full py-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              {{ generating ? '🎨 生成中...' : '🎨 AI生成氛围描写' }}
            </button>

            <!-- 生成结果 -->
            <div v-if="latestGenerated" class="mt-6 p-6 bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg">
              <div class="flex justify-between items-start mb-4">
                <div>
                  <h3 class="font-semibold text-lg text-gray-800">生成结果</h3>
                  <p class="text-sm text-gray-500">版本 v{{ latestGenerated.version }}</p>
                </div>
                <div class="flex gap-2">
                  <button
                    @click="applyAtmosphere(latestGenerated.id)"
                    class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors text-sm"
                  >
                    ✅ 应用此版本
                  </button>
                  <button
                    @click="copyToClipboard(latestGenerated.generatedText)"
                    class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
                  >
                    📋 复制
                  </button>
                </div>
              </div>
              <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ latestGenerated.generatedText }}</p>
              
              <!-- 评分 -->
              <div class="mt-4 flex items-center gap-2">
                <span class="text-sm text-gray-600">评分：</span>
                <div class="flex gap-1">
                  <button
                    v-for="star in 5"
                    :key="star"
                    @click="rateAtmosphere(latestGenerated.id, star)"
                    class="text-2xl hover:scale-110 transition-transform"
                  >
                    {{ star <= (latestGenerated.rating || 0) ? '⭐' : '☆' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 2: 历史版本 -->
        <div v-if="activeTab === 'history'">
          <div class="space-y-4">
            <div
              v-for="atmosphere in atmospheres"
              :key="atmosphere.id"
              class="p-4 border border-gray-200 rounded-lg hover:border-purple-300 transition-colors"
            >
              <div class="flex justify-between items-start mb-3">
                <div class="flex items-center gap-3">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-sm font-medium',
                      getAtmosphereTypeColor(atmosphere.atmosphereType)
                    ]"
                  >
                    {{ getAtmosphereTypeLabel(atmosphere.atmosphereType) }}
                  </span>
                  <span class="text-sm text-gray-500">v{{ atmosphere.version }}</span>
                  <span v-if="atmosphere.isApplied" class="text-sm text-green-600 font-medium">✅ 已应用</span>
                </div>
                <div class="flex items-center gap-2">
                  <!-- 评分显示 -->
                  <div v-if="atmosphere.rating" class="flex items-center gap-1">
                    <span class="text-yellow-500">⭐</span>
                    <span class="text-sm text-gray-600">{{ atmosphere.rating }}/5</span>
                  </div>
                  <span class="text-sm text-gray-400">{{ formatDate(atmosphere.createdAt) }}</span>
                </div>
              </div>

              <p class="text-gray-700 leading-relaxed mb-3 whitespace-pre-wrap">{{ atmosphere.generatedText }}</p>

              <div class="flex justify-between items-center">
                <div v-if="atmosphere.templateName" class="text-sm text-gray-500">
                  📄 基于模板: {{ atmosphere.templateName }}
                </div>
                <div class="flex gap-2">
                  <button
                    v-if="!atmosphere.isApplied"
                    @click="applyAtmosphere(atmosphere.id)"
                    class="px-3 py-1 text-sm bg-green-100 text-green-700 rounded hover:bg-green-200 transition-colors"
                  >
                    应用
                  </button>
                  <button
                    @click="copyToClipboard(atmosphere.generatedText)"
                    class="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200 transition-colors"
                  >
                    复制
                  </button>
                </div>
              </div>
            </div>

            <div v-if="atmospheres.length === 0" class="text-center py-12 text-gray-400">
              暂无生成记录
            </div>
          </div>
        </div>

        <!-- Tab 3: 智能推荐 -->
        <div v-if="activeTab === 'recommend'">
          <div class="max-w-3xl mx-auto">
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 mb-2">情节描述</label>
              <textarea
                v-model="matchPlotContext"
                placeholder="请描述当前的情节内容，AI会为您推荐最合适的氛围类型..."
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-none"
                rows="4"
              ></textarea>
            </div>

            <button
              @click="getAtmosphereMatch"
              :disabled="analyzing"
              class="w-full py-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              {{ analyzing ? '🤔 分析中...' : '🤖 AI智能推荐' }}
            </button>

            <!-- 推荐结果 -->
            <div v-if="matchResult" class="mt-6 space-y-4">
              <!-- 推荐氛围 -->
              <div class="p-6 bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg">
                <h3 class="font-semibold text-lg mb-2">推荐氛围</h3>
                <div class="flex items-center gap-3 mb-3">
                  <span
                    :class="[
                      'px-4 py-2 rounded-full text-lg font-medium',
                      getAtmosphereTypeColor(matchResult.recommendedType)
                    ]"
                  >
                    {{ getAtmosphereTypeLabel(matchResult.recommendedType) }}
                  </span>
                </div>
                <p class="text-gray-700">{{ matchResult.reason }}</p>
              </div>

              <!-- 一致性检查 -->
              <div
                v-if="matchResult.consistencyCheck"
                :class="[
                  'p-4 rounded-lg',
                  matchResult.consistencyCheck.isConsistent
                    ? 'bg-green-50 border border-green-200'
                    : 'bg-yellow-50 border border-yellow-200'
                ]"
              >
                <div class="flex items-start gap-2">
                  <span class="text-xl">{{ matchResult.consistencyCheck.isConsistent ? '✅' : '⚠️' }}</span>
                  <div>
                    <h4 class="font-medium text-gray-800 mb-1">一致性检查</h4>
                    <p class="text-sm text-gray-600">{{ matchResult.consistencyCheck.suggestion }}</p>
                    <p v-if="matchResult.consistencyCheck.issue" class="text-sm text-gray-600 mt-1">
                      {{ matchResult.consistencyCheck.issue }}
                    </p>
                  </div>
                </div>
              </div>

              <!-- 推荐模板 -->
              <div v-if="matchResult.templates && matchResult.templates.length > 0">
                <h3 class="font-semibold text-lg mb-3">推荐模板</h3>
                <div class="space-y-3">
                  <div
                    v-for="template in matchResult.templates"
                    :key="template.templateId"
                    class="p-4 border border-gray-200 rounded-lg hover:border-purple-300 transition-colors cursor-pointer"
                    @click="selectTemplate(template.templateId)"
                  >
                    <div class="flex justify-between items-center mb-2">
                      <h4 class="font-medium text-gray-800">{{ template.templateName }}</h4>
                      <span class="text-sm text-purple-600 font-medium">匹配度: {{ template.matchScore }}%</span>
                    </div>
                    <p class="text-sm text-gray-600">{{ template.matchReason }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 4: 模板管理 -->
        <div v-if="activeTab === 'templates'">
          <div class="mb-4 flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-800">氛围模板库</h3>
            <button
              @click="showTemplateDialog = true"
              class="px-4 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:shadow-lg transition-all"
            >
              ➕ 创建模板
            </button>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="template in templates"
              :key="template.id"
              class="p-4 border border-gray-200 rounded-lg hover:border-purple-300 transition-colors"
            >
              <div class="flex justify-between items-start mb-3">
                <div>
                  <h4 class="font-medium text-gray-800">{{ template.name }}</h4>
                  <div class="flex gap-2 mt-2">
                    <span class="text-xs px-2 py-1 bg-purple-100 text-purple-700 rounded">
                      {{ template.category }}
                    </span>
                    <span
                      :class="[
                        'text-xs px-2 py-1 rounded',
                        getAtmosphereTypeColor(template.atmosphereType)
                      ]"
                    >
                      {{ getAtmosphereTypeLabel(template.atmosphereType) }}
                    </span>
                  </div>
                </div>
                <div class="text-sm text-gray-500">
                  使用 {{ template.usageCount }} 次
                </div>
              </div>

              <p class="text-sm text-gray-600 mb-3">{{ template.description }}</p>

              <div v-if="template.exampleText" class="p-3 bg-gray-50 rounded text-sm text-gray-700 italic">
                "{{ template.exampleText.substring(0, 100) }}..."
              </div>
            </div>
          </div>

          <div v-if="templates.length === 0" class="text-center py-12 text-gray-400">
            暂无模板
          </div>
        </div>
      </div>
    </div>

    <!-- 创建模板对话框 -->
    <div
      v-if="showTemplateDialog"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
      @click.self="showTemplateDialog = false"
    >
      <div class="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6">
        <h2 class="text-2xl font-bold mb-4 text-gray-800">创建氛围模板</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">模板名称 *</label>
            <input
              v-model="templateForm.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">分类 *</label>
              <select
                v-model="templateForm.category"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              >
                <option value="TIME">时间</option>
                <option value="WEATHER">天气</option>
                <option value="EMOTION">情感</option>
                <option value="ACTION">动作</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">氛围类型 *</label>
              <select
                v-model="templateForm.atmosphereType"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              >
                <option value="PEACEFUL">平和</option>
                <option value="TENSE">紧张</option>
                <option value="ROMANTIC">浪漫</option>
                <option value="MYSTERIOUS">神秘</option>
                <option value="EXCITING">激动</option>
                <option value="MELANCHOLIC">忧郁</option>
                <option value="HORROR">恐怖</option>
                <option value="JOYFUL">欢乐</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">描述</label>
            <textarea
              v-model="templateForm.description"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-none"
              rows="2"
            ></textarea>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">关键词（逗号分隔）</label>
            <input
              v-model="keywordsInput"
              type="text"
              placeholder="如：宁静, 温暖, 柔和"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">示例文本</label>
            <textarea
              v-model="templateForm.exampleText"
              placeholder="请输入一段示例性的氛围描写..."
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-none"
              rows="4"
            ></textarea>
          </div>
        </div>

        <div class="mt-6 flex gap-3 justify-end">
          <button
            @click="showTemplateDialog = false"
            class="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            取消
          </button>
          <button
            @click="createTemplate"
            class="px-6 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:shadow-lg transition-all"
          >
            创建
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const sceneId = computed(() => route.params.sceneId)

const activeTab = ref('generate')
const tabs = [
  { key: 'generate', label: '🎨 AI生成' },
  { key: 'history', label: '📚 历史版本' },
  { key: 'recommend', label: '🤖 智能推荐' },
  { key: 'templates', label: '📝 模板管理' }
]

const scene = ref(null)
const atmospheres = ref([])
const templates = ref([])
const generating = ref(false)
const analyzing = ref(false)
const showTemplateDialog = ref(false)

const generateForm = ref({
  sceneId: sceneId.value,
  atmosphereType: 'PEACEFUL',
  plotContext: '',
  emotionalTone: '',
  length: 200,
  templateId: null
})

const matchPlotContext = ref('')
const matchResult = ref(null)

const templateForm = ref({
  name: '',
  category: 'TIME',
  atmosphereType: 'PEACEFUL',
  description: '',
  exampleText: ''
})

const keywordsInput = ref('')
const latestGenerated = ref(null)

// 氛围类型映射
const atmosphereTypeMap = {
  PEACEFUL: '平和',
  TENSE: '紧张',
  ROMANTIC: '浪漫',
  MYSTERIOUS: '神秘',
  EXCITING: '激动',
  MELANCHOLIC: '忧郁',
  HORROR: '恐怖',
  JOYFUL: '欢乐'
}

// 氛围类型颜色
const getAtmosphereTypeColor = (type) => {
  const colorMap = {
    PEACEFUL: 'bg-green-100 text-green-700',
    TENSE: 'bg-red-100 text-red-700',
    ROMANTIC: 'bg-pink-100 text-pink-700',
    MYSTERIOUS: 'bg-purple-100 text-purple-700',
    EXCITING: 'bg-orange-100 text-orange-700',
    MELANCHOLIC: 'bg-blue-100 text-blue-700',
    HORROR: 'bg-gray-100 text-gray-700',
    JOYFUL: 'bg-yellow-100 text-yellow-700'
  }
  return colorMap[type] || 'bg-gray-100 text-gray-700'
}

const getAtmosphereTypeLabel = (type) => {
  return atmosphereTypeMap[type] || type
}

// 加载场景信息
const loadScene = async () => {
  try {
    const response = await axios.get(`/api/scenes/${sceneId.value}`)
    scene.value = response.data
  } catch (error) {
    console.error('加载场景失败:', error)
  }
}

// 加载氛围列表
const loadAtmospheres = async () => {
  try {
    const response = await axios.get(`/api/atmosphere/scene/${sceneId.value}`)
    atmospheres.value = response.data
  } catch (error) {
    console.error('加载氛围列表失败:', error)
  }
}

// 加载模板列表
const loadTemplates = async () => {
  try {
    const response = await axios.get('/api/atmosphere/templates')
    templates.value = response.data
  } catch (error) {
    console.error('加载模板列表失败:', error)
  }
}

// 生成氛围
const generateAtmosphere = async () => {
  generating.value = true
  try {
    generateForm.value.sceneId = sceneId.value
    const response = await axios.post('/api/atmosphere/generate', generateForm.value)
    latestGenerated.value = response.data
    await loadAtmospheres()
  } catch (error) {
    console.error('生成氛围失败:', error)
    alert('生成失败，请重试')
  } finally {
    generating.value = false
  }
}

// 应用氛围
const applyAtmosphere = async (atmosphereId) => {
  try {
    await axios.put(`/api/atmosphere/${atmosphereId}/apply`)
    await loadAtmospheres()
    alert('应用成功！')
  } catch (error) {
    console.error('应用氛围失败:', error)
    alert('应用失败，请重试')
  }
}

// 评分
const rateAtmosphere = async (atmosphereId, rating) => {
  try {
    await axios.put(`/api/atmosphere/${atmosphereId}/rate`, { rating })
    await loadAtmospheres()
    if (latestGenerated.value && latestGenerated.value.id === atmosphereId) {
      latestGenerated.value.rating = rating
    }
  } catch (error) {
    console.error('评分失败:', error)
  }
}

// 获取智能推荐
const getAtmosphereMatch = async () => {
  analyzing.value = true
  try {
    const response = await axios.post('/api/atmosphere/match', null, {
      params: {
        sceneId: sceneId.value,
        plotContext: matchPlotContext.value
      }
    })
    matchResult.value = response.data
  } catch (error) {
    console.error('获取推荐失败:', error)
    alert('分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

// 选择模板
const selectTemplate = (templateId) => {
  generateForm.value.templateId = templateId
  activeTab.value = 'generate'
}

// 创建模板
const createTemplate = async () => {
  if (!templateForm.value.name) {
    alert('请输入模板名称')
    return
  }

  try {
    const keywords = keywordsInput.value
      .split(',')
      .map(k => k.trim())
      .filter(k => k)

    await axios.post('/api/atmosphere/templates', {
      ...templateForm.value,
      keywords
    })

    showTemplateDialog.value = false
    await loadTemplates()
    
    // 重置表单
    templateForm.value = {
      name: '',
      category: 'TIME',
      atmosphereType: 'PEACEFUL',
      description: '',
      exampleText: ''
    }
    keywordsInput.value = ''
  } catch (error) {
    console.error('创建模板失败:', error)
    alert('创建失败，请重试')
  }
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    alert('已复制到剪贴板')
  }).catch(err => {
    console.error('复制失败:', err)
  })
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadScene()
  loadAtmospheres()
  loadTemplates()
})
</script>
