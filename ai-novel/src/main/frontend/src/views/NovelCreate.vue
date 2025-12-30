<template>
  <div class="flex gap-6 h-screen bg-gray-50">
    <!-- Left Panel: Form (60%) -->
    <div class="flex-1 overflow-y-auto p-6">
      <div class="max-w-2xl">
        <!-- Page Header -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">强化型小说创建</h1>
          <p class="text-gray-600">完整信息 • 大纲必填 • 初始场景必填 • AI推荐</p>
        </div>

        <!-- Form Card -->
        <Card>
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <!-- Title -->
            <Input
              id="title"
              label="小说标题"
              v-model="formData.title"
              placeholder="给您的小说起一个吸引人的名字"
              required
              :error="errors.title"
              @blur="validateTitle"
            />

            <!-- Description -->
            <Textarea
              id="description"
              label="小说简介"
              v-model="formData.description"
              placeholder="简要描述您的小说内容、主题和亮点"
              :rows="5"
              :show-count="true"
              :max-length="500"
              hint="500字以内，描述小说的核心内容和特色"
              :error="errors.description"
              @blur="validateDescription"
            />

            <!-- Genre and Target Audience Row -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Genre -->
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium text-gray-700">
                  小说类型 <span class="text-red-500">*</span>
                </label>
                <select
                  v-model="formData.genre"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:border-blue-500 focus:ring-blue-200 transition-all"
                  required
                  @change="() => errors.genre = ''"
                >
                  <option value="">请选择类型</option>
                  <option value="玄幻">玄幻</option>
                  <option value="武侠">武侠</option>
                  <option value="仙侠">仙侠</option>
                  <option value="都市">都市</option>
                  <option value="科幻">科幻</option>
                  <option value="灵异">灵异</option>
                  <option value="历史">历史</option>
                  <option value="军事">军事</option>
                  <option value="游戏">游戏</option>
                  <option value="竞技">竞技</option>
                  <option value="悬疑">悬疑</option>
                  <option value="惊悚">惊悚</option>
                  <option value="轻小说">轻小说</option>
                  <option value="其他">其他</option>
                </select>
                <p v-if="errors.genre" class="text-sm text-red-600">{{ errors.genre }}</p>
              </div>

              <!-- Target Audience -->
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium text-gray-700">
                  目标读者
                </label>
                <select
                  v-model="formData.targetAudience"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:border-blue-500 focus:ring-blue-200 transition-all"
                >
                  <option value="">请选择读者群</option>
                  <option value="男性向">男性向</option>
                  <option value="女性向">女性向</option>
                  <option value="全年龄">全年龄</option>
                  <option value="青少年">青少年</option>
                  <option value="成年人">成年人</option>
                </select>
              </div>
            </div>

            <!-- Writing Style -->
            <Textarea
              id="writingStyle"
              label="写作风格"
              v-model="formData.writingStyle"
              placeholder="描述您期望的写作风格和语言特点"
              :rows="3"
              hint="可选，帮助AI更好地理解您的创作偏好"
            />

            <!-- Initial Outline - Required -->
            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-gray-700">
                初始大纲 <span class="text-red-500">*</span>
              </label>
              <p class="text-xs text-gray-500">选择作为故事基础的大纲</p>
              <div class="border border-gray-300 rounded-lg p-3">
                <select
                  v-model="formData.initialOutlineId"
                  class="w-full px-3 py-2 border border-gray-200 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                  @change="() => errors.initialOutlineId = ''"
                >
                  <option value="">请选择大纲</option>
                  <option v-for="outline in availableOutlines" :key="outline.id" :value="outline.id">
                    {{ outline.title || '未命名大纲' }}
                  </option>
                </select>
              </div>
              <p v-if="errors.initialOutlineId" class="text-sm text-red-600">{{ errors.initialOutlineId }}</p>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                @click="showOutlineDialog = true"
                class="justify-start text-blue-600 hover:text-blue-700"
              >
                + 创建新大纲
              </Button>
            </div>

            <!-- Initial Scene - Required -->
            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-gray-700">
                初始场景 <span class="text-red-500">*</span>
              </label>
              <p class="text-xs text-gray-500">选择故事开始的场景</p>
              <div class="border border-gray-300 rounded-lg p-3">
                <select
                  v-model="formData.initialSceneId"
                  class="w-full px-3 py-2 border border-gray-200 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                  @change="() => errors.initialSceneId = ''"
                >
                  <option value="">请选择场景</option>
                  <option v-for="scene in availableScenes" :key="scene.id" :value="scene.id">
                    {{ scene.name || scene.title || '未命名场景' }}
                  </option>
                </select>
              </div>
              <p v-if="errors.initialSceneId" class="text-sm text-red-600">{{ errors.initialSceneId }}</p>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                @click="showSceneDialog = true"
                class="justify-start text-blue-600 hover:text-blue-700"
              >
                + 创建新场景
              </Button>
            </div>

            <!-- AI Recommendation Toggle -->
            <div class="flex items-center gap-3 p-3 bg-blue-50 rounded-lg border border-blue-200">
              <input
                type="checkbox"
                id="useAiRecommendation"
                v-model="formData.useAiRecommendation"
                class="w-4 h-4 text-blue-600 rounded"
              />
              <label for="useAiRecommendation" class="text-sm font-medium text-gray-700 cursor-pointer">
                启用 AI 智能推荐（创建后获得创意建议）
              </label>
            </div>

            <!-- Form Actions -->
            <div class="flex items-center justify-end gap-4 pt-6 border-t">
              <Button
                type="button"
                variant="ghost"
                @click="$router.push('/')"
              >
                取消
              </Button>
              <Button
                type="submit"
                variant="primary"
                :loading="loading"
              >
                创建小说
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>

    <!-- Right Panel: AI Recommendations (40%) -->
    <div v-if="recommendations" class="w-2/5 overflow-y-auto p-6 bg-white border-l border-gray-200">
      <div class="sticky top-6">
        <!-- Recommendation Header -->
        <div class="mb-6">
          <h2 class="text-xl font-bold text-gray-900 mb-2">✨ AI 创意推荐</h2>
          <p class="text-xs text-gray-500">基于您的设定生成的创作建议</p>
        </div>

        <!-- Story Framework -->
        <Card class="mb-4">
          <template #header>
            <h3 class="font-semibold text-gray-900">故事框架</h3>
          </template>
          <p class="text-sm text-gray-700">{{ recommendations.storyFramework }}</p>
        </Card>

        <!-- Three Act Structure -->
        <Card v-if="recommendations.threeActStructure" class="mb-4">
          <template #header>
            <h3 class="font-semibold text-gray-900">三幕结构</h3>
          </template>
          <div class="space-y-3 text-sm">
            <div>
              <p class="font-medium text-gray-800">铺垫（Setup）</p>
              <p class="text-gray-600">{{ recommendations.threeActStructure.setup }}</p>
            </div>
            <div>
              <p class="font-medium text-gray-800">冲突（Confrontation）</p>
              <p class="text-gray-600">{{ recommendations.threeActStructure.confrontation }}</p>
            </div>
            <div>
              <p class="font-medium text-gray-800">解决（Resolution）</p>
              <p class="text-gray-600">{{ recommendations.threeActStructure.resolution }}</p>
            </div>
          </div>
        </Card>

        <!-- Plot Points -->
        <Card v-if="recommendations.mainPlotPoints" class="mb-4">
          <template #header>
            <h3 class="font-semibold text-gray-900">主要情节点</h3>
          </template>
          <ul class="space-y-2 text-sm">
            <li v-for="(point, idx) in recommendations.mainPlotPoints" :key="idx" class="flex gap-2">
              <span class="font-bold text-blue-600 flex-shrink-0">{{ idx + 1 }}.</span>
              <span class="text-gray-700">{{ point }}</span>
            </li>
          </ul>
        </Card>

        <!-- Themes -->
        <Card v-if="recommendations.themes" class="mb-4">
          <template #header>
            <h3 class="font-semibold text-gray-900">主题建议</h3>
          </template>
          <div class="flex flex-wrap gap-2">
            <span v-for="theme in recommendations.themes" :key="theme"
              class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium">
              {{ theme }}
            </span>
          </div>
        </Card>

        <!-- Word Count Range -->
        <Card v-if="recommendations.wordCountRange">
          <template #header>
            <h3 class="font-semibold text-gray-900">字数建议</h3>
          </template>
          <div class="space-y-2 text-sm">
            <p><span class="font-medium text-gray-800">字数范围：</span>
              {{ recommendations.wordCountRange.minWords?.toLocaleString() }} - 
              {{ recommendations.wordCountRange.maxWords?.toLocaleString() }} 字
            </p>
            <p><span class="font-medium text-gray-800">预计章数：</span>
              {{ recommendations.wordCountRange.recommendedChapters }} 章
            </p>
          </div>
        </Card>
      </div>
    </div>

    <!-- Right Panel: Empty State -->
    <div v-else class="w-2/5 p-6 bg-gradient-to-br from-blue-50 to-indigo-50 border-l border-gray-200 flex flex-col items-center justify-center">
      <div class="text-center">
        <div class="text-4xl mb-4">✨</div>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">AI 创意推荐面板</h3>
        <p class="text-sm text-gray-600 mb-4">完成左侧表单后，将在此显示基于您创作设定的</p>
        <p class="text-sm text-gray-600">智能推荐，包括故事框架、情节点、人物建议等</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNovelStore } from '../stores/novel'
import Card from '../components/Card.vue'
import Input from '../components/Input.vue'
import Textarea from '../components/Textarea.vue'
import Button from '../components/Button.vue'

const router = useRouter()
const novelStore = useNovelStore()

const loading = ref(false)
const showOutlineDialog = ref(false)
const showSceneDialog = ref(false)
const availableOutlines = ref([])
const availableScenes = ref([])
const recommendations = ref(null)

const formData = reactive({
  title: '',
  description: '',
  genre: '',
  targetAudience: '',
  writingStyle: '',
  initialOutlineId: null,
  initialSceneId: null,
  useAiRecommendation: true
})

const errors = reactive({
  title: '',
  description: '',
  genre: '',
  initialOutlineId: '',
  initialSceneId: ''
})

// 加载大纲和场景列表
onMounted(async () => {
  try {
    // 获取大纲列表
    const outlinesResponse = await fetch('/api/outlines')
    if (outlinesResponse.ok) {
      const data = await outlinesResponse.json()
      availableOutlines.value = data.data || []
    }
    
    // 获取场景列表
    const scenesResponse = await fetch('/api/scenes')
    if (scenesResponse.ok) {
      const data = await scenesResponse.json()
      availableScenes.value = data.data || []
    }
  } catch (error) {
    console.error('加载大纲或场景失败:', error)
  }
})

const validateTitle = () => {
  if (!formData.title) {
    errors.title = '请输入小说标题'
    return false
  }
  if (formData.title.length < 2) {
    errors.title = '标题至少2个字符'
    return false
  }
  if (formData.title.length > 200) {
    errors.title = '标题不能超过200个字符'
    return false
  }
  errors.title = ''
  return true
}

const validateDescription = () => {
  if (formData.description && formData.description.length > 500) {
    errors.description = '简介不能超过500字'
    return false
  }
  errors.description = ''
  return true
}

const validateGenre = () => {
  if (!formData.genre) {
    errors.genre = '请选择小说类型'
    return false
  }
  errors.genre = ''
  return true
}

const validateOutline = () => {
  if (!formData.initialOutlineId) {
    errors.initialOutlineId = '请选择初始大纲'
    return false
  }
  errors.initialOutlineId = ''
  return true
}

const validateScene = () => {
  if (!formData.initialSceneId) {
    errors.initialSceneId = '请选择初始场景'
    return false
  }
  errors.initialSceneId = ''
  return true
}

const validateForm = () => {
  const validations = [
    validateTitle(),
    validateDescription(),
    validateGenre(),
    validateOutline(),
    validateScene()
  ]
  return validations.every(v => v === true)
}

const fetchRecommendations = async () => {
  try {
    const response = await fetch('/api/novels/recommendations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: formData.title,
        description: formData.description,
        genre: formData.genre,
        targetAudience: formData.targetAudience,
        writingStyle: formData.writingStyle,
        useAiRecommendation: formData.useAiRecommendation
      })
    })
    
    if (response.ok) {
      const data = await response.json()
      recommendations.value = data.data
    }
  } catch (error) {
    console.error('获取推荐失败:', error)
  }
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  try {
    // 创建小说
    const novel = await novelStore.createNovel(formData)
    if (novel) {
      // 如果启用了AI推荐，保存推荐数据
      if (formData.useAiRecommendation && recommendations.value) {
        // 这里可以调用API保存推荐数据
        try {
          await fetch(`/api/novels/${novel.id}/recommendations`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(recommendations.value)
          })
        } catch (error) {
          console.error('保存推荐失败:', error)
        }
      }
      
      // 跳转到智能创作页面
      router.push(`/novel/${novel.id}/smart-writing`)
    }
  } catch (error) {
    console.error('创建小说失败:', error)
  } finally {
    loading.value = false
  }
}

// Watch title and description changes to fetch recommendations
import { watch } from 'vue'
watch([() => formData.title, () => formData.description], async () => {
  if (formData.title && formData.description) {
    await fetchRecommendations()
  }
}, { debounce: 1000 })
</script>
