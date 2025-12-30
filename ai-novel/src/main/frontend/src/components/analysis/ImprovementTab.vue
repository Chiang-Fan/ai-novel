<template>
  <div class="improvement-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">💡 改进建议</h3>
    
    <div v-if="!improvements || improvements.length === 0" class="text-center py-10 text-gray-500">
      暂无改进建议
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="(item, index) in improvements"
        :key="index"
        class="bg-white rounded-lg p-4 border-l-4 shadow-sm"
        :class="getPriorityBorderColor(item.priority)">
        <div class="flex items-start justify-between mb-2">
          <div class="flex items-center space-x-2">
            <span class="text-lg">{{ getCategoryEmoji(item.category) }}</span>
            <span class="font-semibold text-gray-800">{{ item.category }}</span>
            <span
              :class="[
                'px-2 py-0.5 rounded text-xs font-medium',
                getPriorityClass(item.priority)
              ]">
              {{ getPriorityLabel(item.priority) }}
            </span>
          </div>
        </div>
        <p class="text-gray-700">{{ item.suggestion }}</p>
        
        <!-- 预期效果 -->
        <div v-if="item.expectedEffect" class="mt-3 pt-3 border-t border-gray-200">
          <div class="text-sm text-gray-600">
            <strong>预期效果：</strong>{{ item.expectedEffect }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ImprovementTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  computed: {
    improvements() {
      try {
        return JSON.parse(this.analysis.improvementSuggestions || '[]')
      } catch {
        return []
      }
    }
  },
  methods: {
    getCategoryEmoji(category) {
      const emojis = {
        '情节': '📍',
        '角色': '👤',
        '节奏': '⏱️',
        '冲突': '⚔️',
        '文笔': '✍️',
        '描写': '🎨',
        '对话': '💬',
        '世界观': '🌍'
      }
      return emojis[category] || '💡'
    },
    getPriorityLabel(priority) {
      const labels = {
        HIGH: '高优先级',
        MEDIUM: '中优先级',
        LOW: '低优先级'
      }
      return labels[priority] || priority
    },
    getPriorityClass(priority) {
      const classes = {
        HIGH: 'bg-red-100 text-red-700',
        MEDIUM: 'bg-yellow-100 text-yellow-700',
        LOW: 'bg-green-100 text-green-700'
      }
      return classes[priority] || 'bg-gray-100 text-gray-700'
    },
    getPriorityBorderColor(priority) {
      const colors = {
        HIGH: 'border-red-500',
        MEDIUM: 'border-yellow-500',
        LOW: 'border-green-500'
      }
      return colors[priority] || 'border-gray-500'
    }
  }
}
</script>
