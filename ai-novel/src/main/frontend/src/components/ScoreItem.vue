<template>
  <div class="score-item bg-white rounded-lg p-4 shadow-sm text-center">
    <div class="text-3xl mb-2">{{ icon }}</div>
    <div :class="`text-2xl font-bold text-${color}-600 mb-1`">
      {{ score || '-' }}<span class="text-sm text-gray-500">/10</span>
    </div>
    <div class="text-sm text-gray-600">{{ label }}</div>
    
    <!-- 进度条 -->
    <div class="mt-3 w-full bg-gray-200 rounded-full h-2">
      <div
        :class="`bg-${color}-500 h-2 rounded-full transition-all`"
        :style="{ width: `${(score || 0) * 10}%` }"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ScoreItem',
  props: {
    icon: String,
    label: String,
    score: Number,
    color: {
      type: String,
      default: 'blue'
    }
  }
}
</script>
