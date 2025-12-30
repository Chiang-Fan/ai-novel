<template>
  <div 
    class="mt-6 p-6 rounded-lg border-2"
    :class="resultClass"
  >
    <div class="flex items-center mb-4">
      <div class="text-4xl mr-3">{{ result.isConsistent ? '✅' : '⚠️' }}</div>
      <div>
        <h3 class="text-lg font-bold" :class="result.isConsistent ? 'text-green-700' : 'text-orange-700'">
          {{ result.isConsistent ? '风格一致' : '风格不一致' }}
        </h3>
        <p class="text-sm text-gray-600">
          相似度: {{ Math.round((result.similarity || 0) * 100) }}%
        </p>
      </div>
    </div>
    
    <!-- 详细反馈 -->
    <div class="space-y-3">
      <div v-for="(item, index) in result.differences" :key="index" class="flex items-start">
        <span class="text-orange-500 mr-2">•</span>
        <div class="flex-1">
          <span class="font-medium text-gray-700">{{ item.aspect }}:</span>
          <span class="text-gray-600 ml-2">{{ item.description }}</span>
        </div>
      </div>
    </div>
    
    <!-- AI 建议 -->
    <div v-if="result.suggestions" class="mt-4 pt-4 border-t border-gray-200">
      <h4 class="font-medium text-gray-700 mb-2">💡 改进建议</h4>
      <p class="text-sm text-gray-600">{{ result.suggestions }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: Object
})

const resultClass = computed(() => {
  return props.result.isConsistent
    ? 'bg-green-50 border-green-300'
    : 'bg-orange-50 border-orange-300'
})
</script>
