<template>
  <div class="mt-6 p-6 bg-white rounded-lg border-2 border-gray-200">
    <h3 class="text-lg font-bold text-gray-900 mb-4">📊 风格对比结果</h3>
    
    <!-- 总体相似度 -->
    <div class="mb-6">
      <div class="flex justify-between items-center mb-2">
        <span class="text-sm font-medium text-gray-700">总体相似度</span>
        <span class="text-lg font-bold" :class="similarityColorClass">
          {{ Math.round((comparison.overallSimilarity || 0) * 100) }}%
        </span>
      </div>
      <div class="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
        <div 
          class="h-3 transition-all duration-500"
          :class="similarityBgClass"
          :style="{ width: `${(comparison.overallSimilarity || 0) * 100}%` }"
        ></div>
      </div>
    </div>
    
    <!-- 各项对比 -->
    <div class="space-y-4">
      <ComparisonItem 
        label="叙述视角"
        :original="comparison.originalPerspective"
        :current="comparison.chapterPerspective"
      />
      <ComparisonItem 
        label="语气风格"
        :original="comparison.originalTone"
        :current="comparison.chapterTone"
      />
      <ComparisonItem 
        label="句式特点"
        :original="comparison.originalSentenceStyle"
        :current="comparison.chapterSentenceStyle"
      />
      <ComparisonItem 
        label="平均句长"
        :original="`${comparison.originalAvgSentenceLength || 0} 字`"
        :current="`${comparison.chapterAvgSentenceLength || 0} 字`"
      />
      <ComparisonItem 
        label="对话占比"
        :original="`${Math.round((comparison.originalDialogueRatio || 0) * 100)}%`"
        :current="`${Math.round((comparison.chapterDialogueRatio || 0) * 100)}%`"
      />
    </div>
    
    <!-- 差异说明 -->
    <div v-if="comparison.differences && comparison.differences.length > 0" class="mt-6 pt-6 border-t border-gray-200">
      <h4 class="font-medium text-gray-700 mb-3">⚠️ 发现的差异</h4>
      <ul class="space-y-2">
        <li 
          v-for="(diff, index) in comparison.differences" 
          :key="index"
          class="flex items-start text-sm text-gray-600"
        >
          <span class="text-orange-500 mr-2">•</span>
          <span>{{ diff }}</span>
        </li>
      </ul>
    </div>
    
    <!-- AI 建议 -->
    <div v-if="comparison.suggestions" class="mt-4 p-4 bg-blue-50 rounded-lg">
      <h4 class="font-medium text-blue-900 mb-2">💡 AI 建议</h4>
      <p class="text-sm text-blue-700">{{ comparison.suggestions }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ComparisonItem from './ComparisonItem.vue'

const props = defineProps({
  comparison: Object
})

const similarityColorClass = computed(() => {
  const similarity = props.comparison.overallSimilarity || 0
  if (similarity >= 0.8) return 'text-green-600'
  if (similarity >= 0.6) return 'text-yellow-600'
  return 'text-red-600'
})

const similarityBgClass = computed(() => {
  const similarity = props.comparison.overallSimilarity || 0
  if (similarity >= 0.8) return 'bg-green-600'
  if (similarity >= 0.6) return 'bg-yellow-600'
  return 'bg-red-600'
})
</script>
