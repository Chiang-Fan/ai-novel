<template>
  <div class="pacing-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">⏱️ 节奏分析</h3>
    
    <div class="bg-white rounded-lg p-6 border shadow-sm mb-6">
      <div class="text-center mb-4">
        <div class="text-3xl mb-2">{{ getPacingEmoji(analysis.pacing) }}</div>
        <div class="text-2xl font-bold text-blue-600">{{ getPacingLabel(analysis.pacing) }}</div>
      </div>
    </div>

    <!-- 内容占比 -->
    <div class="bg-gray-50 rounded-lg p-6">
      <h4 class="font-semibold text-gray-800 mb-4">📊 内容占比</h4>
      
      <div class="space-y-4">
        <!-- 动作 -->
        <div>
          <div class="flex justify-between mb-1">
            <span class="text-sm font-medium text-gray-700">⚡ 动作</span>
            <span class="text-sm font-bold text-blue-600">{{ (analysis.actionRatio * 100).toFixed(0) }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div
              class="bg-blue-500 h-3 rounded-full transition-all"
              :style="{ width: `${analysis.actionRatio * 100}%` }"></div>
          </div>
        </div>

        <!-- 对话 -->
        <div>
          <div class="flex justify-between mb-1">
            <span class="text-sm font-medium text-gray-700">💬 对话</span>
            <span class="text-sm font-bold text-green-600">{{ (analysis.dialogueRatio * 100).toFixed(0) }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div
              class="bg-green-500 h-3 rounded-full transition-all"
              :style="{ width: `${analysis.dialogueRatio * 100}%` }"></div>
          </div>
        </div>

        <!-- 描写 -->
        <div>
          <div class="flex justify-between mb-1">
            <span class="text-sm font-medium text-gray-700">🎨 描写</span>
            <span class="text-sm font-bold text-purple-600">{{ (analysis.descriptionRatio * 100).toFixed(0) }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div
              class="bg-purple-500 h-3 rounded-full transition-all"
              :style="{ width: `${analysis.descriptionRatio * 100}%` }"></div>
          </div>
        </div>

        <!-- 独白 -->
        <div>
          <div class="flex justify-between mb-1">
            <span class="text-sm font-medium text-gray-700">💭 独白</span>
            <span class="text-sm font-bold text-yellow-600">{{ (analysis.introspectionRatio * 100).toFixed(0) }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div
              class="bg-yellow-500 h-3 rounded-full transition-all"
              :style="{ width: `${analysis.introspectionRatio * 100}%` }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 建议 -->
    <div class="mt-6 bg-blue-50 rounded-lg p-4 border border-blue-200">
      <h4 class="font-semibold text-gray-800 mb-2">💡 节奏建议</h4>
      <p class="text-gray-700">{{ getPacingAdvice(analysis.pacing) }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PacingTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  methods: {
    getPacingLabel(pacing) {
      const labels = {
        TOO_FAST: '过快',
        FAST: '快速',
        MODERATE: '适中',
        SLOW: '缓慢',
        TOO_SLOW: '过慢'
      }
      return labels[pacing] || '-'
    },
    getPacingEmoji(pacing) {
      const emojis = {
        TOO_FAST: '🚀',
        FAST: '⚡',
        MODERATE: '🎯',
        SLOW: '🐢',
        TOO_SLOW: '🦥'
      }
      return emojis[pacing] || '⏱️'
    },
    getPacingAdvice(pacing) {
      const advice = {
        TOO_FAST: '节奏过快，建议增加场景描写和角色内心活动，让读者有喘息空间。',
        FAST: '节奏较快，适合动作场景，但注意适度放缓以保持平衡。',
        MODERATE: '节奏适中，动静结合，继续保持！',
        SLOW: '节奏较慢，适合铺垫和酝酿，但注意适度加快以保持吸引力。',
        TOO_SLOW: '节奏过慢，建议增加动作和对话，推进情节发展。'
      }
      return advice[pacing] || '暂无建议'
    }
  }
}
</script>
