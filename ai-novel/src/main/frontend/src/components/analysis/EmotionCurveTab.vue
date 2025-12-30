<template>
  <div class="emotion-curve-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">💓 情感曲线</h3>
    
    <!-- 情感趋势 -->
    <div class="bg-white rounded-lg p-6 border shadow-sm mb-6">
      <div class="text-center">
        <div class="text-3xl mb-2">{{ getTrendEmoji(analysis.emotionTrend) }}</div>
        <div class="text-2xl font-bold text-pink-600">{{ getTrendLabel(analysis.emotionTrend) }}</div>
        <div class="text-sm text-gray-600 mt-2">{{ getTrendDescription(analysis.emotionTrend) }}</div>
      </div>
    </div>

    <!-- 情感曲线数据 -->
    <div v-if="emotionCurve && emotionCurve.length > 0" class="bg-gray-50 rounded-lg p-6">
      <h4 class="font-semibold text-gray-800 mb-4">📈 情感变化轨迹</h4>
      
      <div class="space-y-3">
        <div
          v-for="(point, index) in emotionCurve"
          :key="index"
          class="bg-white rounded-lg p-3 border shadow-sm">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <div class="flex items-center space-x-2">
                <span class="text-lg">{{ getEmotionEmoji(point.emotion) }}</span>
                <span class="font-medium text-gray-800">{{ point.emotion }}</span>
                <span class="text-xs text-gray-500">位置: {{ (point.position * 100).toFixed(0) }}%</span>
              </div>
              <div class="text-sm text-gray-600 mt-1">{{ point.description }}</div>
            </div>
            <div class="ml-4">
              <div class="text-xl font-bold text-pink-600">{{ point.intensity }}</div>
              <div class="text-xs text-gray-500">强度</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-10 text-gray-500">
      暂无情感曲线数据
    </div>
  </div>
</template>

<script>
export default {
  name: 'EmotionCurveTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  computed: {
    emotionCurve() {
      try {
        return JSON.parse(this.analysis.emotionCurve || '[]')
      } catch {
        return []
      }
    }
  },
  methods: {
    getTrendLabel(trend) {
      const labels = {
        RISING: '上升',
        FALLING: '下降',
        FLAT: '平坦',
        FLUCTUATING: '波动'
      }
      return labels[trend] || '-'
    },
    getTrendEmoji(trend) {
      const emojis = {
        RISING: '📈',
        FALLING: '📉',
        FLAT: '➡️',
        FLUCTUATING: '〰️'
      }
      return emojis[trend] || '💓'
    },
    getTrendDescription(trend) {
      const descriptions = {
        RISING: '情感整体向上发展，积极正面',
        FALLING: '情感逐渐下沉，趋于消极',
        FLAT: '情感变化平缓，缺乏起伏',
        FLUCTUATING: '情感起伏明显，富有变化'
      }
      return descriptions[trend] || ''
    },
    getEmotionEmoji(emotion) {
      const emojis = {
        '喜悦': '😊',
        '悲伤': '😢',
        '愤怒': '😠',
        '恐惧': '😨',
        '惊讶': '😲',
        '平静': '😐',
        '兴奋': '🤩',
        '紧张': '😰'
      }
      return emojis[emotion] || '💓'
    }
  }
}
</script>
