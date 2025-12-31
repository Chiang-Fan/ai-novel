<template>
  <div class="min-h-screen bg-gray-50 pb-12">
    <!-- 页面头部 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex justify-between items-center mb-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">🎨 文风管理</h1>
          <p class="mt-2 text-gray-600">分析和管理小说的写作风格</p>
        </div>
        <Button @click="$router.back()" variant="secondary">
          ← 返回
        </Button>
      </div>

      <!-- 加载状态 -->
      <Loading v-if="loading" />
      
      <!-- 内容区域 -->
      <div v-else-if="style || novelStyle" class="space-y-6">
        <!-- 🔥 四维风格画像雷达图（Qwen重构新增） -->
        <Card v-if="novelStyle">
          <template #header>
            <div class="flex justify-between items-center">
              <h2 class="text-xl font-bold text-gray-900">🎯 四维风格画像 <span class="text-sm text-blue-600 font-normal ml-2">Qwen AI分析</span></h2>
              <div class="flex items-center space-x-4">
                <span class="text-sm text-gray-500">
                  已分析: {{ novelStyle.analyzedChapterCount || 0 }} 章
                </span>
                <Button 
                  @click="analyzeStyle" 
                  :disabled="analyzingStyle"
                  variant="secondary"
                  size="sm"
                >
                  {{ analyzingStyle ? '分析中...' : '重新分析' }}
                </Button>
              </div>
            </div>
          </template>
          
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- 雷达图 -->
            <div>
              <StyleRadarChart :styleData="novelStyle" />
            </div>
            
            <!-- 四维评分详情 -->
            <div class="space-y-4">
              <div class="bg-blue-50 p-4 rounded-lg">
                <h3 class="text-sm font-semibold text-blue-900 mb-3">📊 维度评分说明</h3>
                <div class="space-y-2 text-sm text-blue-800">
                  <div><span class="font-medium">叙事节奏:</span> {{ narrativePaceLabel }}</div>
                  <div><span class="font-medium">对话风格:</span> {{ dialogueStyleLabel }}</div>
                  <div><span class="font-medium">描写深度:</span> {{ descriptionDepthLabel }}</div>
                  <div><span class="font-medium">情感基调:</span> {{ emotionalToneLabel }}</div>
                </div>
              </div>
              
              <!-- 描写子系统 -->
              <div class="bg-green-50 p-4 rounded-lg">
                <h3 class="text-sm font-semibold text-green-900 mb-2">🎨 描写子系统</h3>
                <div class="space-y-1 text-xs text-green-800">
                  <div v-if="sceneDescProfile">
                    <span class="font-medium">场景:</span> {{ sceneDescProfile.density || 'medium' }} 密度, 侧重{{ sceneDescProfile.focus || 'environment' }}
                  </div>
                  <div v-if="charDescProfile">
                    <span class="font-medium">人物:</span> {{ charDescProfile.detail_level || 'medium' }} 细节, 侧重{{ charDescProfile.focus || 'appearance' }}
                  </div>
                  <div v-if="actionDescProfile">
                    <span class="font-medium">动作:</span> {{ actionDescProfile.pace || 'medium' }} 节奏, {{ actionDescProfile.granularity || 'coarse' }} 粒度
                  </div>
                  <div v-if="psychologyDescProfile">
                    <span class="font-medium">心理:</span> {{ psychologyDescProfile.method || 'direct' }} 表达, {{ psychologyDescProfile.depth || 'shallow' }} 深度
                  </div>
                </div>
              </div>
              
              <!-- 语言特征 -->
              <div class="bg-purple-50 p-4 rounded-lg">
                <h3 class="text-sm font-semibold text-purple-900 mb-2">📝 语言特征</h3>
                <div class="text-xs text-purple-800">
                  <div v-if="highFreqVerbs.length > 0" class="mb-2">
                    <span class="font-medium">高频动词:</span> 
                    <span class="ml-1">{{ highFreqVerbs.join('、') }}</span>
                  </div>
                  <div v-if="bannedWords.length > 0">
                    <span class="font-medium">需避免:</span> 
                    <span class="ml-1">{{ bannedWords.join('、') }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Card>
        
        <!-- 原有的旧版风格展示（保留兼容） -->
        <div v-if="style && !novelStyle" class="bg-yellow-50 border-l-4 border-yellow-400 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-yellow-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm text-yellow-700">
                当前使用旧版风格数据。建议点击上方"重新分析"按钮生成新的四维风格画像。
              </p>
            </div>
          </div>
        </div>
        
        <!-- 顶部统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <StatCard 
            title="叙述视角" 
            :value="perspectiveLabel" 
            icon="👁️"
            :color="'blue'"
          />
          <StatCard 
            title="语气风格" 
            :value="style.tone" 
            icon="🎭"
            :color="'purple'"
          />
          <StatCard 
            title="句式特点" 
            :value="style.sentenceStyle" 
            icon="📝"
            :color="'green'"
          />
          <StatCard 
            title="平均句长" 
            :value="`${style.avgSentenceLength || 0} 字`" 
            icon="📏"
            :color="'orange'"
          />
        </div>

        <!-- 风格详情卡片 -->
        <Card>
          <template #header>
            <div class="flex justify-between items-center">
              <h2 class="text-xl font-bold text-gray-900">📊 文风特征</h2>
              <div class="text-sm text-gray-500">
                置信度: {{ Math.round((style.confidenceScore || 0) * 100) }}%
              </div>
            </div>
          </template>
          
          <div class="space-y-4">
            <!-- 关键词标签 -->
            <div>
              <h3 class="text-sm font-medium text-gray-700 mb-2">风格关键词</h3>
              <div class="flex flex-wrap gap-2">
                <span 
                  v-for="keyword in keywords" 
                  :key="keyword"
                  class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium"
                >
                  {{ keyword }}
                </span>
              </div>
            </div>

            <!-- 对话占比 -->
            <div>
              <h3 class="text-sm font-medium text-gray-700 mb-2">对话占比</h3>
              <div class="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
                <div 
                  class="bg-blue-600 h-4 transition-all duration-300"
                  :style="{ width: `${(style.dialogueRatio || 0) * 100}%` }"
                ></div>
              </div>
              <div class="text-sm text-gray-600 mt-1">
                {{ Math.round((style.dialogueRatio || 0) * 100) }}% 对话内容
              </div>
            </div>

            <!-- 描写密度 -->
            <div>
              <h3 class="text-sm font-medium text-gray-700 mb-2">描写密度</h3>
              <div class="flex items-center space-x-2">
                <span :class="densityClass">{{ densityLabel }}</span>
              </div>
            </div>

            <!-- AI 描述 -->
            <div v-if="style.styleDescription">
              <h3 class="text-sm font-medium text-gray-700 mb-2">AI 风格分析</h3>
              <p class="text-gray-600 leading-relaxed">{{ style.styleDescription }}</p>
            </div>

            <!-- 提取时间 -->
            <div class="text-sm text-gray-500 pt-4 border-t">
              提取时间: {{ formatDate(style.extractedAt) }}
              <span v-if="style.lastValidatedAt" class="ml-4">
                最后验证: {{ formatDate(style.lastValidatedAt) }}
              </span>
            </div>
          </div>
        </Card>

        <!-- 风格模板库 -->
        <Card>
          <template #header>
            <h2 class="text-xl font-bold text-gray-900">📚 预设模板</h2>
          </template>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <TemplateCard 
              v-for="template in templates" 
              :key="template.id"
              :template="template"
              @apply="applyTemplate"
            />
          </div>
        </Card>

        <!-- 风格验证工具 -->
        <Card>
          <template #header>
            <h2 class="text-xl font-bold text-gray-900">✅ 内容验证</h2>
          </template>
          
          <div class="space-y-4">
            <Textarea 
              v-model="testContent" 
              placeholder="输入待验证的内容，系统将分析其是否符合当前小说的写作风格..."
              rows="8"
            />
            <div class="flex justify-end">
              <Button 
                @click="validateContent" 
                :disabled="!testContent || validating"
              >
                {{ validating ? '验证中...' : '验证风格一致性' }}
              </Button>
            </div>
            
            <!-- 验证结果 -->
            <ValidationResult 
              v-if="validationResult" 
              :result="validationResult" 
            />
          </div>
        </Card>

        <!-- 风格对比工具 -->
        <Card>
          <template #header>
            <h2 class="text-xl font-bold text-gray-900">📈 章节风格对比</h2>
          </template>
          
          <div class="space-y-4">
            <div class="flex items-center space-x-4">
              <select 
                v-model="selectedChapterId"
                class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">选择要对比的章节</option>
                <option 
                  v-for="chapter in chapters" 
                  :key="chapter.id" 
                  :value="chapter.id"
                >
                  第{{ chapter.chapterNumber }}章 - {{ chapter.title }}
                </option>
              </select>
              <Button 
                @click="compareStyle" 
                :disabled="!selectedChapterId || comparing"
              >
                {{ comparing ? '对比中...' : '对比分析' }}
              </Button>
            </div>
            
            <!-- 对比结果 -->
            <StyleComparison 
              v-if="comparison" 
              :comparison="comparison" 
            />
          </div>
        </Card>
      </div>

      <!-- 无风格数据时显示 -->
      <div v-else class="text-center py-12">
        <div class="text-6xl mb-4">🎨</div>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">尚未提取写作风格</h3>
        <p class="text-gray-600 mb-6">
          系统会在创建或更新章节时自动提取文风特征<br>
          请先创建一些章节内容，文风数据将自动生成
        </p>
        
        <Button @click="loadData" variant="secondary">
          刷新数据
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import Navbar from '@/components/Navbar.vue'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Textarea from '@/components/Textarea.vue'
import Loading from '@/components/Loading.vue'
import StatCard from '@/components/StatCard.vue'
import TemplateCard from '@/components/TemplateCard.vue'
import ValidationResult from '@/components/ValidationResult.vue'
import StyleComparison from '@/components/StyleComparison.vue'
import StyleRadarChart from '@/components/StyleRadarChart.vue'

const route = useRoute()
const novelId = ref(route.params.id)

const loading = ref(true)
const comparing = ref(false)
const analyzingStyle = ref(false)

const style = ref(null)
const novelStyle = ref(null) // 🔥 新增：四维风格画像
const templates = ref([])
const chapters = ref([])
const comparison = ref(null)

// 计算属性
const keywords = computed(() => {
  if (!style.value?.keywords) return []
  try {
    return typeof style.value.keywords === 'string' 
      ? JSON.parse(style.value.keywords) 
      : style.value.keywords
  } catch {
    return []
  }
})

const perspectiveLabel = computed(() => {
  const map = {
    'first': '第一人称',
    'third_limited': '第三人称限制',
    'third_omniscient': '第三人称全知'
  }
  return map[style.value?.perspective] || style.value?.perspective || '未知'
})

const densityLabel = computed(() => {
  const map = {
    'sparse': '简洁',
    'moderate': '适中',
    'rich': '丰富'
  }
  return map[style.value?.descriptionDensity] || '未知'
})

const densityClass = computed(() => {
  const base = 'px-3 py-1 rounded-full text-sm font-medium '
  const colorMap = {
    'sparse': 'bg-gray-100 text-gray-800',
    'moderate': 'bg-blue-100 text-blue-800',
    'rich': 'bg-purple-100 text-purple-800'
  }
  return base + (colorMap[style.value?.descriptionDensity] || 'bg-gray-100 text-gray-800')
})

// 🔥 新增：四维风格画像计算属性
const narrativePaceLabel = computed(() => {
  const score = novelStyle.value?.narrativePace || 5
  if (score <= 3) return `${score}/10 - 节奏舒缓，铺垫充分`
  if (score <= 7) return `${score}/10 - 节奏适中，张弛有度`
  return `${score}/10 - 节奏紧凑，情节推进快`
})

const dialogueStyleLabel = computed(() => {
  const score = novelStyle.value?.dialogueStyle || 5
  if (score <= 3) return `${score}/10 - 以叙述为主，对话较少`
  if (score <= 7) return `${score}/10 - 叙述与对话平衡`
  return `${score}/10 - 对话丰富，驱动情节`
})

const descriptionDepthLabel = computed(() => {
  const score = novelStyle.value?.descriptionDepth || 5
  if (score <= 3) return `${score}/10 - 简洁白描，点到为止`
  if (score <= 7) return `${score}/10 - 描写适中，详略得当`
  return `${score}/10 - 细腻详尽，层次丰富`
})

const emotionalToneLabel = computed(() => {
  const score = novelStyle.value?.emotionalTone || 5
  if (score <= 3) return `${score}/10 - 冷峻疏离，克制理性`
  if (score <= 7) return `${score}/10 - 情感中性，客观平和`
  return `${score}/10 - 情感浓烈，感染力强`
})

// 🔥 描写子系统解析
const sceneDescProfile = computed(() => {
  try {
    return novelStyle.value?.sceneDescriptionProfile ? 
      JSON.parse(novelStyle.value.sceneDescriptionProfile) : null
  } catch { return null }
})

const charDescProfile = computed(() => {
  try {
    return novelStyle.value?.characterDescriptionProfile ? 
      JSON.parse(novelStyle.value.characterDescriptionProfile) : null
  } catch { return null }
})

const actionDescProfile = computed(() => {
  try {
    return novelStyle.value?.actionDescriptionProfile ? 
      JSON.parse(novelStyle.value.actionDescriptionProfile) : null
  } catch { return null }
})

const psychologyDescProfile = computed(() => {
  try {
    return novelStyle.value?.psychologyDescriptionProfile ? 
      JSON.parse(novelStyle.value.psychologyDescriptionProfile) : null
  } catch { return null }
})

// 🔥 语言特征解析
const highFreqVerbs = computed(() => {
  try {
    return novelStyle.value?.highFrequencyVerbs ? 
      JSON.parse(novelStyle.value.highFrequencyVerbs) : []
  } catch { return [] }
})

const bannedWords = computed(() => {
  try {
    return novelStyle.value?.bannedWords ? 
      JSON.parse(novelStyle.value.bannedWords) : []
  } catch { return [] }
})

// 方法
const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    // 🔥 优先加载四维风格画像
    try {
      novelStyle.value = await api.novelWritingStyle.getByNovelId(novelId.value)
      console.log('加载四维风格画像成功:', novelStyle.value)
    } catch (err) {
      console.log('尚未生成四维风格画像:', err.message)
      novelStyle.value = null
    }
    
    // 加载旧版风格数据（兼容）
    try {
      style.value = await api.writingStyles.getByNovelId(novelId.value)
    } catch (err) {
      console.log('尚未提取旧版风格:', err.message)
      style.value = null
    }
    
    // 加载模板
    templates.value = await api.writingStyles.getTemplates()
    
    // 加载章节列表
    chapters.value = await api.chapters.list(novelId.value)
  } catch (err) {
    console.error('加载数据失败:', err)
    alert('加载数据失败: ' + err.message)
  } finally {
    loading.value = false
  }
}

// 🔥 新增：触发AI风格分析
const analyzeStyle = async () => {
  if (!confirm('确定要重新分析小说风格吗？这将使用AI分析所有章节，可能需要一些时间。')) {
    return
  }
  
  analyzingStyle.value = true
  try {
    novelStyle.value = await api.novelWritingStyle.analyzeBatch(novelId.value)
    alert('风格分析完成！')
    console.log('分析结果:', novelStyle.value)
  } catch (err) {
    console.error('风格分析失败:', err)
    alert('风格分析失败: ' + err.message)
  } finally {
    analyzingStyle.value = false
  }
}

const compareStyle = async () => {
  if (!selectedChapterId.value) {
    alert('请选择章节')
    return
  }
  
  comparing.value = true
  comparison.value = null
  
  try {
    comparison.value = await api.writingStyles.compare(
      novelId.value,
      selectedChapterId.value
    )
  } catch (err) {
    console.error('对比失败:', err)
    alert('对比失败: ' + err.message)
  } finally {
    comparing.value = false
  }
}

const applyTemplate = async (template) => {
  if (!confirm(`确定要应用「${template.name}」模板吗？这将覆盖当前的风格设置。`)) {
    return
  }
  
  try {
    style.value = await api.writingStyles.update(novelId.value, {
      perspective: template.perspective,
      tone: template.tone,
      sentenceStyle: template.sentenceStyle,
      keywords: template.keywords
    })
    alert('模板应用成功！')
  } catch (err) {
    console.error('应用模板失败:', err)
    alert('应用模板失败: ' + err.message)
  }
}

onMounted(() => {
  loadData()
})
</script>
