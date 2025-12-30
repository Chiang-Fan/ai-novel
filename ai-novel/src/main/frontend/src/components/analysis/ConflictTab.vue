<template>
  <div class="conflict-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">⚔️ 冲突分析</h3>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <!-- 冲突类型 -->
      <div class="bg-white rounded-lg p-4 border shadow-sm">
        <div class="text-sm text-gray-600 mb-2">冲突类型</div>
        <div class="text-xl font-bold text-blue-600">{{ analysis.conflictType || '-' }}</div>
      </div>

      <!-- 冲突强度 -->
      <div class="bg-white rounded-lg p-4 border shadow-sm">
        <div class="text-sm text-gray-600 mb-2">冲突强度</div>
        <div class="text-xl font-bold text-red-600">{{ analysis.conflictIntensity || '-' }}/10</div>
        <div class="mt-2 w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-red-500 h-2 rounded-full transition-all"
            :style="{ width: `${(analysis.conflictIntensity || 0) * 10}%` }"></div>
        </div>
      </div>

      <!-- 冲突方向 -->
      <div class="bg-white rounded-lg p-4 border shadow-sm">
        <div class="text-sm text-gray-600 mb-2">冲突方向</div>
        <div class="text-xl font-bold text-purple-600">{{ getDirectionLabel(analysis.conflictDirection) }}</div>
      </div>
    </div>

    <!-- 分析说明 -->
    <div class="bg-blue-50 rounded-lg p-4 border border-blue-200">
      <h4 class="font-semibold text-gray-800 mb-2">💡 分析说明</h4>
      <div class="text-gray-700 space-y-1">
        <p><strong>类型：</strong>{{ getTypeDescription(analysis.conflictType) }}</p>
        <p><strong>强度：</strong>{{ getIntensityDescription(analysis.conflictIntensity) }}</p>
        <p><strong>方向：</strong>{{ getDirectionDescription(analysis.conflictDirection) }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConflictTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  methods: {
    getDirectionLabel(direction) {
      const labels = {
        ESCALATING: '升级 ↗️',
        DE_ESCALATING: '降级 ↘️',
        STABLE: '稳定 →'
      }
      return labels[direction] || '-'
    },
    getTypeDescription(type) {
      const descriptions = {
        INTERNAL: '内心冲突（角色内心的挣扎）',
        EXTERNAL: '外部冲突（角色与外界的对抗）',
        MIXED: '混合冲突（内外交织）'
      }
      return descriptions[type] || '未知'
    },
    getIntensityDescription(intensity) {
      if (!intensity) return '未评分'
      if (intensity >= 8) return '非常强烈，情节高度紧张'
      if (intensity >= 6) return '较强，有明显的对抗'
      if (intensity >= 4) return '中等，存在一定矛盾'
      return '较弱，冲突不明显'
    },
    getDirectionDescription(direction) {
      const descriptions = {
        ESCALATING: '冲突正在逐步升级，张力增强',
        DE_ESCALATING: '冲突正在缓和，向解决方向发展',
        STABLE: '冲突保持稳定，未见明显变化'
      }
      return descriptions[direction] || '未知'
    }
  }
}
</script>
