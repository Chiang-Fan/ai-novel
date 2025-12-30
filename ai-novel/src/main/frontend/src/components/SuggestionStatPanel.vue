<template>
  <div class="stat-panel bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg shadow-sm p-6">
    <h2 class="text-xl font-bold text-gray-800 mb-4">📊 推荐统计</h2>
    
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
      <!-- 总数 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">📈</div>
        <div class="text-2xl font-bold text-blue-600">{{ statistics.totalCount }}</div>
        <div class="text-sm text-gray-600">总推荐数</div>
      </div>

      <!-- 活跃 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">✨</div>
        <div class="text-2xl font-bold text-green-600">{{ statistics.activeCount }}</div>
        <div class="text-sm text-gray-600">活跃</div>
      </div>

      <!-- 已采纳 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">✅</div>
        <div class="text-2xl font-bold text-purple-600">{{ statistics.acceptedCount }}</div>
        <div class="text-sm text-gray-600">已采纳</div>
      </div>

      <!-- 已拒绝 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">❌</div>
        <div class="text-2xl font-bold text-red-600">{{ statistics.rejectedCount }}</div>
        <div class="text-sm text-gray-600">已拒绝</div>
      </div>

      <!-- 已过期 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">⏰</div>
        <div class="text-2xl font-bold text-gray-600">{{ statistics.expiredCount }}</div>
        <div class="text-sm text-gray-600">已过期</div>
      </div>
    </div>

    <!-- 优先级分布 -->
    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm font-medium text-gray-700">🔴 高优先级</span>
          <span class="text-lg font-bold text-red-600">{{ statistics.highPriorityCount }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-red-500 h-2 rounded-full transition-all"
            :style="{ width: `${getPercentage(statistics.highPriorityCount, statistics.activeCount)}%` }"></div>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm font-medium text-gray-700">🟡 中优先级</span>
          <span class="text-lg font-bold text-yellow-600">{{ statistics.mediumPriorityCount }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-yellow-500 h-2 rounded-full transition-all"
            :style="{ width: `${getPercentage(statistics.mediumPriorityCount, statistics.activeCount)}%` }"></div>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm font-medium text-gray-700">🟢 低优先级</span>
          <span class="text-lg font-bold text-green-600">{{ statistics.lowPriorityCount }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-green-500 h-2 rounded-full transition-all"
            :style="{ width: `${getPercentage(statistics.lowPriorityCount, statistics.activeCount)}%` }"></div>
        </div>
      </div>
    </div>

    <!-- 质量指标 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-sm text-gray-600 mb-1">平均优先级</div>
        <div class="text-xl font-bold text-blue-600">{{ statistics.avgPriority?.toFixed(1) || '-' }}</div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-sm text-gray-600 mb-1">平均相关性</div>
        <div class="text-xl font-bold text-purple-600">{{ (statistics.avgRelevanceScore * 100)?.toFixed(0) || '-' }}%</div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-sm text-gray-600 mb-1">采纳率</div>
        <div class="text-xl font-bold text-green-600">{{ (statistics.acceptanceRate * 100)?.toFixed(0) || '-' }}%</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SuggestionStatPanel',
  props: {
    statistics: {
      type: Object,
      required: true
    }
  },
  methods: {
    getPercentage(value, total) {
      if (!total) return 0
      return (value / total * 100).toFixed(0)
    }
  }
}
</script>
