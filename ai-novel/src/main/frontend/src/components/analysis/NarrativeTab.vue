<template>
  <div class="narrative-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">📖 叙事技巧分析</h3>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <!-- 转折点 -->
      <div class="bg-white rounded-lg p-4 border shadow-sm">
        <div class="text-3xl mb-2">🔄</div>
        <div class="text-sm text-gray-600 mb-1">转折点数量</div>
        <div class="text-2xl font-bold text-blue-600">{{ analysis.turningPointsCount || 0 }}</div>
      </div>

      <!-- 悬念强度 -->
      <div class="bg-white rounded-lg p-4 border shadow-sm">
        <div class="text-3xl mb-2">🎭</div>
        <div class="text-sm text-gray-600 mb-1">悬念强度</div>
        <div class="text-2xl font-bold text-purple-600">{{ analysis.suspenseLevel || 0 }}/10</div>
        <div class="mt-2 w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-purple-500 h-2 rounded-full transition-all"
            :style="{ width: `${(analysis.suspenseLevel || 0) * 10}%` }"></div>
        </div>
      </div>
    </div>

    <!-- 叙事技巧列表 -->
    <div v-if="narrativeTechniques && narrativeTechniques.length > 0" class="bg-gray-50 rounded-lg p-6">
      <h4 class="font-semibold text-gray-800 mb-4">✨ 使用的叙事技巧</h4>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div
          v-for="(technique, index) in narrativeTechniques"
          :key="index"
          class="bg-white rounded-lg p-3 border shadow-sm">
          <div class="font-medium text-gray-800">{{ technique }}</div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-10 text-gray-500">
      暂无叙事技巧数据
    </div>

    <!-- 世界观元素 -->
    <div v-if="worldBuildingElements && worldBuildingElements.length > 0" class="mt-6 bg-blue-50 rounded-lg p-6 border border-blue-200">
      <h4 class="font-semibold text-gray-800 mb-3">🌍 世界观元素</h4>
      <div class="flex items-center justify-between mb-3">
        <span class="text-sm text-gray-600">世界观完整度</span>
        <span class="text-lg font-bold text-blue-600">{{ analysis.worldBuildingScore || 0 }}/10</span>
      </div>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="(element, index) in worldBuildingElements"
          :key="index"
          class="px-3 py-1 bg-white border border-blue-300 rounded-full text-sm text-gray-700">
          {{ element }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NarrativeTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  computed: {
    narrativeTechniques() {
      try {
        return JSON.parse(this.analysis.narrativeTechniques || '[]')
      } catch {
        return []
      }
    },
    worldBuildingElements() {
      try {
        return JSON.parse(this.analysis.worldBuildingElements || '[]')
      } catch {
        return []
      }
    }
  }
}
</script>
