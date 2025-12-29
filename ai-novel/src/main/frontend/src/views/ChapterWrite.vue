<template>
  <div class="chapter-write">
    <div class="bg-white rounded-lg shadow-md p-8 max-w-4xl mx-auto">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">🤖 AI智能续写</h2>

      <form @submit.prevent="handleContinue" class="space-y-6">
        <!-- 续写方向 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            续写方向（可选）
          </label>
          <textarea
            v-model="form.direction"
            rows="3"
            placeholder="例如：主角遇到强敌，展开一场激烈的战斗..."
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
          ></textarea>
        </div>

        <!-- 目标字数 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            目标字数: {{ form.targetWordCount }} 字
          </label>
          <input
            v-model="form.targetWordCount"
            type="range"
            min="500"
            max="10000"
            step="500"
            class="w-full"
          />
          <div class="flex justify-between text-xs text-gray-500">
            <span>500</span>
            <span>10000</span>
          </div>
        </div>

        <!-- 按钮 -->
        <div class="flex justify-end space-x-4">
          <button
            type="button"
            @click="$router.back()"
            class="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition">
            返回
          </button>
          <button
            type="submit"
            :disabled="generating"
            class="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition disabled:opacity-50">
            {{ generating ? '生成中...' : '🤖 开始续写' }}
          </button>
        </div>
      </form>

      <!-- 生成结果 -->
      <div v-if="result" class="mt-8 p-6 border border-gray-200 rounded-lg">
        <h3 class="font-bold text-gray-800 mb-4">生成结果</h3>
        <div class="prose max-w-none">
          <p class="whitespace-pre-wrap text-gray-700">{{ result.content }}</p>
        </div>
        <div class="mt-4 text-sm text-gray-500">
          字数: {{ result.wordCount }} 字
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'

const route = useRoute()
const router = useRouter()
const novelId = ref(parseInt(route.params.id))

const form = ref({
  novelId: novelId.value,
  direction: '',
  targetWordCount: 2000
})

const generating = ref(false)
const result = ref(null)

const handleContinue = async () => {
  generating.value = true
  result.value = null

  try {
    const chapter = await api.chapters.continue(form.value)
    result.value = chapter
    alert('续写成功！')
    setTimeout(() => {
      router.push(`/novel/${novelId.value}`)
    }, 1500)
  } catch (error) {
    console.error('续写失败:', error)
    alert('续写失败: ' + error.message)
  } finally {
    generating.value = false
  }
}
</script>
