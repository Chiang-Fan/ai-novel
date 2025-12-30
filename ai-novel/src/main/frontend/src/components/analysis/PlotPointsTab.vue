<template>
  <div class="plot-points-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">📍 情节点分析</h3>
    
    <div v-if="!plotPoints || plotPoints.length === 0" class="text-center py-10 text-gray-500">
      暂无情节点数据
    </div>

    <div v-else class="space-y-4">
      <!-- 时间线可视化 -->
      <div class="bg-gray-50 rounded-lg p-6">
        <div class="relative">
          <!-- 时间线 -->
          <div class="absolute left-0 right-0 top-1/2 h-2 bg-blue-200 rounded-full"></div>
          
          <!-- 情节点 -->
          <div class="relative flex justify-between items-center">
            <div
              v-for="(point, index) in plotPoints"
              :key="index"
              class="relative flex flex-col items-center"
              :style="{ left: `${point.position * 100}%` }">
              <div
                :class="[
                  'w-6 h-6 rounded-full border-4 border-white shadow-lg z-10',
                  getTypeColor(point.type)
                ]"
                :title="`${point.type} - 重要性: ${point.importance}/10`"></div>
              <div class="mt-2 text-xs text-gray-600 text-center max-w-20">
                {{ getTypeLabel(point.type) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 情节点列表 -->
      <div class="space-y-3">
        <div
          v-for="(point, index) in plotPoints"
          :key="index"
          class="bg-white rounded-lg p-4 border-l-4 shadow-sm"
          :class="getTypeBorderColor(point.type)">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center space-x-2 mb-2">
                <span class="text-lg">{{ getTypeEmoji(point.type) }}</span>
                <span class="font-semibold text-gray-800">{{ getTypeLabel(point.type) }}</span>
                <span class="text-sm text-gray-500">位置: {{ (point.position * 100).toFixed(0) }}%</span>
              </div>
              <p class="text-gray-700">{{ point.description }}</p>
            </div>
            <div class="ml-4 text-center">
              <div class="text-2xl font-bold text-blue-600">{{ point.importance }}</div>
              <div class="text-xs text-gray-500">重要性</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlotPointsTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  computed: {
    plotPoints() {
      try {
        return JSON.parse(this.analysis.plotPoints || '[]')
      } catch {
        return []
      }
    }
  },
  methods: {
    getTypeLabel(type) {
      const labels = {
        INCITING_INCIDENT: '引发事件',
        RISING_ACTION: '上升动作',
        MIDPOINT: '中点转折',
        CLIMAX: '高潮',
        FALLING_ACTION: '下降动作',
        RESOLUTION: '结局'
      }
      return labels[type] || type
    },
    getTypeEmoji(type) {
      const emojis = {
        INCITING_INCIDENT: '🚀',
        RISING_ACTION: '📈',
        MIDPOINT: '⚡',
        CLIMAX: '🔥',
        FALLING_ACTION: '📉',
        RESOLUTION: '✨'
      }
      return emojis[type] || '📍'
    },
    getTypeColor(type) {
      const colors = {
        INCITING_INCIDENT: 'bg-green-500',
        RISING_ACTION: 'bg-blue-500',
        MIDPOINT: 'bg-yellow-500',
        CLIMAX: 'bg-red-500',
        FALLING_ACTION: 'bg-purple-500',
        RESOLUTION: 'bg-pink-500'
      }
      return colors[type] || 'bg-gray-500'
    },
    getTypeBorderColor(type) {
      const colors = {
        INCITING_INCIDENT: 'border-green-500',
        RISING_ACTION: 'border-blue-500',
        MIDPOINT: 'border-yellow-500',
        CLIMAX: 'border-red-500',
        FALLING_ACTION: 'border-purple-500',
        RESOLUTION: 'border-pink-500'
      }
      return colors[type] || 'border-gray-500'
    }
  }
}
</script>
