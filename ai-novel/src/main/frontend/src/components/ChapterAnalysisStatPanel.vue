<template>
  <div class="stat-panel bg-gradient-to-br from-blue-50 to-purple-50 rounded-lg shadow-sm p-6">
    <h2 class="text-xl font-bold text-gray-800 mb-4">📊 整体统计</h2>
    
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <!-- 总分析数 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">📈</div>
        <div class="text-2xl font-bold text-blue-600">{{ statistics.totalAnalyzedChapters }}</div>
        <div class="text-sm text-gray-600">已分析章节</div>
      </div>

      <!-- 平均质量 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">⭐</div>
        <div class="text-2xl font-bold text-green-600">{{ statistics.avgQualityScore?.toFixed(1) || '-' }}</div>
        <div class="text-sm text-gray-600">平均质量</div>
      </div>

      <!-- 平均冲突强度 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">⚔️</div>
        <div class="text-2xl font-bold text-red-600">{{ statistics.avgConflictIntensity?.toFixed(1) || '-' }}</div>
        <div class="text-sm text-gray-600">平均冲突</div>
      </div>

      <!-- 平均转折点 -->
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="text-3xl mb-2">🔄</div>
        <div class="text-2xl font-bold text-purple-600">{{ statistics.avgTurningPointsPerChapter || '-' }}</div>
        <div class="text-sm text-gray-600">平均转折点</div>
      </div>
    </div>

    <!-- 质量维度详情 -->
    <div class="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm font-medium text-gray-700">可读性</span>
          <span class="text-lg font-bold text-blue-600">{{ statistics.avgReadability?.toFixed(1) || '-' }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-blue-500 h-2 rounded-full transition-all"
            :style="{ width: `${(statistics.avgReadability || 0) * 10}%` }"></div>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm font-medium text-gray-700">创意性</span>
          <span class="text-lg font-bold text-green-600">{{ statistics.avgCreativity?.toFixed(1) || '-' }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-green-500 h-2 rounded-full transition-all"
            :style="{ width: `${(statistics.avgCreativity || 0) * 10}%` }"></div>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm font-medium text-gray-700">连贯性</span>
          <span class="text-lg font-bold text-purple-600">{{ statistics.avgCoherence?.toFixed(1) || '-' }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-purple-500 h-2 rounded-full transition-all"
            :style="{ width: `${(statistics.avgCoherence || 0) * 10}%` }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChapterAnalysisStatPanel',
  props: {
    statistics: {
      type: Object,
      required: true
    }
  }
}
</script>
