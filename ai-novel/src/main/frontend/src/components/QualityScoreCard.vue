<template>
  <div class="quality-score-card bg-gradient-to-br from-yellow-50 to-orange-50 rounded-lg shadow-md p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-4">⭐ 质量评分</h2>
    
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
      <!-- 整体质量 -->
      <ScoreItem
        icon="⭐"
        label="整体质量"
        :score="analysis.qualityScore"
        color="blue" />

      <!-- 可读性 -->
      <ScoreItem
        icon="📖"
        label="可读性"
        :score="analysis.readabilityScore"
        color="green" />

      <!-- 创意性 -->
      <ScoreItem
        icon="💡"
        label="创意性"
        :score="analysis.creativityScore"
        color="purple" />

      <!-- 连贯性 -->
      <ScoreItem
        icon="🔗"
        label="连贯性"
        :score="analysis.coherenceScore"
        color="indigo" />

      <!-- 悬念强度 -->
      <ScoreItem
        icon="🎭"
        label="悬念强度"
        :score="analysis.suspenseLevel"
        color="red" />
    </div>
  </div>
</template>

<script>
import ScoreItem from './ScoreItem.vue'

export default {
  name: 'QualityScoreCard',
  components: { ScoreItem },
  props: {
    analysis: {
      type: Object,
      required: true
    }
  }
}
</script>
