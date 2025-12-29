<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 py-8 px-4">
    <div class="max-w-5xl mx-auto">
      <!-- 头部 -->
      <div class="mb-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 flex items-center">
              <span class="text-4xl mr-3">✍️</span>
              智能章节创建
            </h1>
            <p class="mt-2 text-gray-600">
              输入章节正文，AI将自动提取标题、摘要、角色、场景等元数据
            </p>
          </div>
          <router-link
            :to="`/novel/${novelId}`"
            class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition">
            ← 返回小说详情
          </router-link>
        </div>
      </div>

      <!-- 主表单 -->
      <div class="bg-white rounded-2xl shadow-xl p-6 mb-6">
        <!-- 小说信息 -->
        <div v-if="novel" class="mb-6 p-4 bg-blue-50 rounded-lg">
          <div class="flex items-center">
            <span class="text-2xl mr-3">📖</span>
            <div>
              <h3 class="font-semibold text-lg text-gray-800">{{ novel.title }}</h3>
              <p class="text-sm text-gray-600">
                已有 {{ chaptersCount }} 章 · 共 {{ novel.totalWordCount || 0 }} 字
              </p>
            </div>
          </div>
        </div>

        <!-- 章节标题（可选） -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            章节标题（可选）
            <span class="text-gray-500 text-xs ml-2">留空则自动提取</span>
          </label>
          <input
            v-model="formData.title"
            type="text"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="如：危机四伏、绝地反击、真相大白...">
        </div>

        <!-- 章节正文 -->
        <div class="mb-6">
          <div class="flex items-center justify-between mb-2">
            <label class="block text-sm font-medium text-gray-700">
              章节正文 <span class="text-red-500">*</span>
            </label>
            <span class="text-sm text-gray-500">
              已输入 {{ wordCount }} 字
            </span>
          </div>
          <textarea
            v-model="formData.content"
            rows="15"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
            placeholder="在此粘贴或输入章节内容...

示例：
夜幕降临，整座城市陷入了诡异的寂静。李明站在废弃大楼的天台边缘，手中紧握着那张泛黄的羊皮纸。纸上的古怪符文在月光下微微发光，似乎在诉说着某个被遗忘已久的秘密..."></textarea>
        </div>

        <!-- 高级选项 -->
        <div class="border-t pt-6 mb-6">
          <button
            @click="showAdvanced = !showAdvanced"
            class="flex items-center text-gray-700 hover:text-blue-600 transition">
            <span class="mr-2">{{ showAdvanced ? '▼' : '▶' }}</span>
            <span class="font-medium">高级选项</span>
          </button>

          <div v-if="showAdvanced" class="mt-4 space-y-4">
            <!-- 关联场景 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                关联场景（可选）
              </label>
              <select
                v-model="formData.sceneId"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                <option :value="null">不关联场景</option>
                <option v-for="scene in scenes" :key="scene.id" :value="scene.id">
                  {{ scene.name }} - {{ scene.type }}
                </option>
              </select>
            </div>

            <!-- 关联大纲 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                关联大纲节点（可选）
              </label>
              <select
                v-model="formData.outlineNodeId"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                <option :value="null">不关联大纲</option>
                <option v-for="node in outlineNodes" :key="node.id" :value="node.id">
                  {{ '　'.repeat(node.level || 0) }}{{ node.title }}
                </option>
              </select>
            </div>

            <!-- 分析选项 -->
            <div class="flex items-center space-x-6">
              <label class="flex items-center cursor-pointer">
                <input
                  v-model="formData.deepAnalysis"
                  type="checkbox"
                  class="mr-2 w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500">
                <span class="text-sm text-gray-700">启用深度分析（提取角色关系、伏笔等）</span>
              </label>

              <label class="flex items-center cursor-pointer">
                <input
                  v-model="formData.generateContinuationDirection"
                  type="checkbox"
                  class="mr-2 w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500">
                <span class="text-sm text-gray-700">自动生成续写方向</span>
              </label>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center space-x-4">
          <button
            @click="handleSubmit"
            :disabled="loading || !formData.content"
            class="flex-1 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-lg hover:from-blue-700 hover:to-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition font-medium text-lg shadow-lg">
            <span v-if="!loading">🚀 智能创建章节</span>
            <span v-else>⏳ 分析中...</span>
          </button>

          <button
            @click="resetForm"
            type="button"
            class="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition">
            重置
          </button>
        </div>

        <!-- 提示信息 -->
        <div class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <div class="flex items-start">
            <span class="text-yellow-600 mr-2">💡</span>
            <div class="text-sm text-yellow-800">
              <p class="font-medium mb-1">智能提取功能说明：</p>
              <ul class="list-disc list-inside space-y-1 text-xs">
                <li>如果未填写标题，AI将自动从正文中提取</li>
                <li>自动分析并生成章节摘要</li>
                <li>提取关键词、角色、场景、情节等元数据</li>
                <li>启用深度分析可获得更详细的角色关系和伏笔信息</li>
                <li>自动生成续写方向，为下一章提供创作建议</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 分析结果 -->
      <div v-if="result" class="bg-white rounded-2xl shadow-xl p-6">
        <div class="mb-6">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-bold text-gray-900 flex items-center">
              <span class="text-3xl mr-3">✨</span>
              创建成功！
            </h2>
            <button
              @click="viewChapter"
              class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
              查看章节详情
            </button>
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div class="p-4 bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg">
            <div class="text-sm text-gray-600 mb-1">章节标题</div>
            <div class="font-semibold text-lg text-gray-900">
              {{ result.title }}
              <span v-if="result.extractedTitle" class="text-xs text-blue-600 ml-2">
                (AI提取)
              </span>
            </div>
          </div>

          <div class="p-4 bg-gradient-to-br from-green-50 to-green-100 rounded-lg">
            <div class="text-sm text-gray-600 mb-1">章节号 / 字数</div>
            <div class="font-semibold text-lg text-gray-900">
              第{{ result.chapterNumber }}章 / {{ result.wordCount }}字
            </div>
          </div>
        </div>

        <!-- 摘要 -->
        <div v-if="result.summary" class="mb-6 p-4 bg-gray-50 rounded-lg">
          <div class="text-sm font-medium text-gray-700 mb-2">📝 章节摘要</div>
          <div class="text-gray-800 whitespace-pre-wrap">{{ result.summary }}</div>
        </div>

        <!-- 元数据标签页 -->
        <div class="mb-6">
          <div class="flex border-b">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              @click="activeTab = tab.key"
              :class="[
                'px-4 py-2 font-medium transition',
                activeTab === tab.key
                  ? 'text-blue-600 border-b-2 border-blue-600'
                  : 'text-gray-600 hover:text-gray-900'
              ]">
              {{ tab.label }}
            </button>
          </div>

          <div class="mt-4">
            <!-- 关键词 -->
            <div v-if="activeTab === 'keywords' && result.keywords" class="space-y-2">
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="keyword in result.keywords"
                  :key="keyword"
                  class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
                  {{ keyword }}
                </span>
              </div>
            </div>

            <!-- 角色 -->
            <div v-if="activeTab === 'characters' && result.characters" class="space-y-3">
              <div
                v-for="char in result.characters"
                :key="char.name"
                class="p-4 bg-gray-50 rounded-lg">
                <div class="flex items-center mb-2">
                  <span class="text-lg font-semibold text-gray-900">{{ char.name }}</span>
                  <span class="ml-2 px-2 py-0.5 bg-purple-100 text-purple-700 text-xs rounded">
                    {{ char.role }}
                  </span>
                </div>
                <div v-if="char.description" class="text-sm text-gray-600">
                  {{ char.description }}
                </div>
              </div>
            </div>

            <!-- 场景 -->
            <div v-if="activeTab === 'scene' && result.scene" class="space-y-3">
              <div class="grid grid-cols-2 gap-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <div class="text-xs text-gray-600 mb-1">地点</div>
                  <div class="font-medium text-gray-900">{{ result.scene.location || '未知' }}</div>
                </div>
                <div class="p-3 bg-gray-50 rounded-lg">
                  <div class="text-xs text-gray-600 mb-1">时间</div>
                  <div class="font-medium text-gray-900">{{ result.scene.time || '未知' }}</div>
                </div>
              </div>
              <div class="p-4 bg-gray-50 rounded-lg">
                <div class="text-xs text-gray-600 mb-2">场景描述</div>
                <div class="text-sm text-gray-800">{{ result.scene.description }}</div>
              </div>
              <div class="p-4 bg-gray-50 rounded-lg">
                <div class="text-xs text-gray-600 mb-2">氛围</div>
                <div class="text-sm text-gray-800">{{ result.scene.atmosphere }}</div>
              </div>
            </div>

            <!-- 情节 -->
            <div v-if="activeTab === 'plot' && result.plot" class="space-y-3">
              <div class="p-4 bg-red-50 rounded-lg">
                <div class="text-sm font-medium text-red-700 mb-2">⚔️ 当前冲突</div>
                <div class="text-gray-800">{{ result.plot.conflict }}</div>
              </div>
              <div class="p-4 bg-yellow-50 rounded-lg">
                <div class="text-sm font-medium text-yellow-700 mb-2">💭 情感基调</div>
                <div class="text-gray-800">{{ result.plot.emotionalTone }}</div>
              </div>
              <div v-if="result.plot.foreshadowing && result.plot.foreshadowing.length" class="p-4 bg-purple-50 rounded-lg">
                <div class="text-sm font-medium text-purple-700 mb-2">🔮 潜在伏笔</div>
                <ul class="list-disc list-inside space-y-1">
                  <li v-for="(item, index) in result.plot.foreshadowing" :key="index" class="text-gray-800 text-sm">
                    {{ item }}
                  </li>
                </ul>
              </div>
            </div>

            <!-- 风格 -->
            <div v-if="activeTab === 'style' && result.style" class="space-y-3">
              <div class="grid grid-cols-2 gap-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <div class="text-xs text-gray-600 mb-1">风格类型</div>
                  <div class="font-medium text-gray-900">{{ result.style.type }}</div>
                </div>
                <div class="p-3 bg-gray-50 rounded-lg">
                  <div class="text-xs text-gray-600 mb-1">情节节奏</div>
                  <div class="font-medium text-gray-900">{{ result.style.pacing }}</div>
                </div>
              </div>
              <div class="p-4 bg-gray-50 rounded-lg">
                <div class="text-xs text-gray-600 mb-2">风格描述</div>
                <div class="text-sm text-gray-800">{{ result.style.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 续写方向 -->
        <div v-if="result.continuationDirection" class="p-4 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-lg border border-indigo-200">
          <div class="flex items-center mb-3">
            <span class="text-2xl mr-2">🎯</span>
            <div class="text-sm font-medium text-indigo-700">下一章续写建议</div>
          </div>
          <div class="text-sm text-gray-800 whitespace-pre-wrap">{{ result.continuationDirection }}</div>
        </div>

        <!-- 完整性评分 -->
        <div v-if="result.completenessScore" class="mt-6 p-4 bg-gradient-to-r from-green-50 to-teal-50 rounded-lg">
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <span class="text-2xl mr-2">📊</span>
              <span class="font-medium text-gray-700">内容完整性评分</span>
            </div>
            <div class="flex items-center">
              <div class="text-3xl font-bold text-green-600">{{ result.completenessScore }}</div>
              <div class="text-sm text-gray-600 ml-2">/ 100</div>
            </div>
          </div>
          <div class="mt-2 w-full bg-gray-200 rounded-full h-2">
            <div
              class="bg-gradient-to-r from-green-500 to-teal-500 h-2 rounded-full transition-all duration-500"
              :style="{ width: result.completenessScore + '%' }"></div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="mt-6 flex space-x-4">
          <button
            @click="createAnother"
            class="flex-1 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium">
            继续创建章节
          </button>
          <button
            @click="goToNovelDetail"
            class="flex-1 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">
            返回小说详情
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const novelId = ref(parseInt(route.params.id))
const novel = ref(null)
const chaptersCount = ref(0)
const scenes = ref([])
const outlineNodes = ref([])
const loading = ref(false)
const showAdvanced = ref(false)
const result = ref(null)
const activeTab = ref('keywords')

const tabs = [
  { key: 'keywords', label: '🏷️ 关键词' },
  { key: 'characters', label: '👥 角色' },
  { key: 'scene', label: '🎬 场景' },
  { key: 'plot', label: '📖 情节' },
  { key: 'style', label: '🎨 风格' }
]

const formData = ref({
  novelId: novelId.value,
  title: '',
  content: '',
  sceneId: null,
  outlineNodeId: null,
  deepAnalysis: true,
  generateContinuationDirection: true
})

const wordCount = computed(() => {
  return formData.value.content.length
})

onMounted(async () => {
  await loadNovel()
  await loadChapters()
  await loadScenes()
  await loadOutlineNodes()
})

async function loadNovel() {
  try {
    novel.value = await api.novels.get(novelId.value)
  } catch (error) {
    console.error('加载小说失败:', error)
  }
}

async function loadChapters() {
  try {
    const chapters = await api.chapters.list(novelId.value)
    chaptersCount.value = chapters.length
  } catch (error) {
    console.error('加载章节列表失败:', error)
  }
}

async function loadScenes() {
  try {
    scenes.value = await api.scenes.list(novelId.value)
  } catch (error) {
    console.error('加载场景列表失败:', error)
    scenes.value = []
  }
}

async function loadOutlineNodes() {
  try {
    outlineNodes.value = await api.outline.list(novelId.value)
  } catch (error) {
    console.error('加载大纲失败:', error)
    outlineNodes.value = []
  }
}

async function handleSubmit() {
  if (!formData.value.content.trim()) {
    alert('请输入章节内容')
    return
  }

  loading.value = true
  try {
    result.value = await api.chapters.createSmart(formData.value)
    // 滚动到结果区域
    setTimeout(() => {
      window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
    }, 100)
  } catch (error) {
    console.error('智能创建失败:', error)
    alert('创建失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function resetForm() {
  formData.value = {
    novelId: novelId.value,
    title: '',
    content: '',
    sceneId: null,
    outlineNodeId: null,
    deepAnalysis: true,
    generateContinuationDirection: true
  }
  result.value = null
}

function createAnother() {
  resetForm()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function viewChapter() {
  // 可以导航到章节详情页
  router.push(`/novel/${novelId.value}`)
}

function goToNovelDetail() {
  router.push(`/novel/${novelId.value}`)
}
</script>
