<template>
  <div class="max-w-3xl mx-auto">
    <!-- Page Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">创建新小说</h1>
      <p class="text-gray-600">填写基本信息，开始您的创作之旅</p>
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
          placeholder="简要描述您的小说内容、主题和亮点&#10;&#10;例如：这是一个关于时间旅行的科幻冒险故事，主人公意外获得穿越时空的能力..."
          :rows="6"
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
          placeholder="描述您期望的写作风格和语言特点&#10;&#10;例如：幽默诙谐，节奏明快，带有轻松的冒险感"
          :rows="4"
          hint="可选，帮助AI更好地理解您的创作偏好"
        />

        <!-- Status -->
        <div class="flex flex-col gap-2">
          <label class="text-sm font-medium text-gray-700">
            初始状态
          </label>
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                v-model="formData.status"
                value="planning"
                class="w-4 h-4 text-blue-600"
              />
              <span class="text-gray-700">策划中</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                v-model="formData.status"
                value="writing"
                class="w-4 h-4 text-blue-600"
              />
              <span class="text-gray-700">创作中</span>
            </label>
          </div>
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

    <!-- Tips Card -->
    <Card class="mt-6">
      <template #header>
        <h3 class="font-semibold text-gray-900">💡 创作小贴士</h3>
      </template>
      <ul class="space-y-2 text-sm text-gray-600">
        <li class="flex items-start gap-2">
          <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>详细的简介能帮助AI更好地理解您的创作意图</span>
        </li>
        <li class="flex items-start gap-2">
          <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>明确的类型和风格设定有助于生成更符合期望的内容</span>
        </li>
        <li class="flex items-start gap-2">
          <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>创建后可以使用智能创作工作台进行AI辅助写作</span>
        </li>
      </ul>
    </Card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNovelStore } from '../stores/novel'
import Card from '../components/Card.vue'
import Input from '../components/Input.vue'
import Textarea from '../components/Textarea.vue'
import Button from '../components/Button.vue'

const router = useRouter()
const novelStore = useNovelStore()

const loading = ref(false)

const formData = reactive({
  title: '',
  description: '',
  genre: '',
  targetAudience: '',
  writingStyle: '',
  status: 'planning'
})

const errors = reactive({
  title: '',
  description: '',
  genre: ''
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
  if (formData.title.length > 100) {
    errors.title = '标题不能超过100个字符'
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

const validateForm = () => {
  const validations = [
    validateTitle(),
    validateDescription(),
    validateGenre()
  ]
  return validations.every(v => v === true)
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  try {
    const novel = await novelStore.createNovel(formData)
    if (novel) {
      // 创建成功，跳转到智能创作页面
      router.push(`/novel/${novel.id}/smart-writing`)
    }
  } catch (error) {
    console.error('创建小说失败:', error)
    // 这里应该显示错误提示
  } finally {
    loading.value = false
  }
}
</script>
