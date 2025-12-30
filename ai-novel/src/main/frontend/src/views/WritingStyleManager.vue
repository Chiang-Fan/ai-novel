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
      <div v-else-if="style" class="space-y-6">
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

const route = useRoute()
const novelId = ref(route.params.id)

const loading = ref(true)
const comparing = ref(false)

const style = ref(null)
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

// 方法
const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    // 加载风格数据
    try {
      style.value = await api.writingStyles.getByNovelId(novelId.value)
    } catch (err) {
      console.log('尚未提取风格:', err.message)
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
