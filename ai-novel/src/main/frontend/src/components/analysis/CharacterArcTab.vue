<template>
  <div class="character-arc-tab">
    <h3 class="text-lg font-bold text-gray-800 mb-4">👤 角色弧光分析</h3>
    
    <div v-if="!characterArcs || characterArcs.length === 0" class="text-center py-10 text-gray-500">
      暂无角色弧光数据
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="(arc, index) in characterArcs"
        :key="index"
        class="bg-white rounded-lg p-4 border shadow-sm">
        <div class="flex items-start justify-between mb-3">
          <div class="flex items-center space-x-3">
            <div class="text-2xl">{{ getTypeEmoji(arc.type) }}</div>
            <div>
              <div class="font-semibold text-gray-800">{{ arc.characterName }}</div>
              <div class="text-sm text-gray-600">{{ getTypeLabel(arc.type) }}</div>
            </div>
          </div>
          <div class="text-center">
            <div class="text-lg font-bold text-blue-600">{{ arc.growthScore }}</div>
            <div class="text-xs text-gray-500">成长评分</div>
          </div>
        </div>
        <p class="text-gray-700">{{ arc.description }}</p>
      </div>
    </div>

    <!-- 主角阶段 -->
    <div v-if="analysis.protagonistStage" class="mt-6 bg-purple-50 rounded-lg p-4 border border-purple-200">
      <h4 class="font-semibold text-gray-800 mb-2">🎯 主角成长阶段</h4>
      <div class="text-lg font-bold text-purple-600">{{ getStageLabel(analysis.protagonistStage) }}</div>
      <div class="text-sm text-gray-600 mt-1">{{ getStageDescription(analysis.protagonistStage) }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CharacterArcTab',
  props: {
    analysis: {
      type: Object,
      required: true
    }
  },
  computed: {
    characterArcs() {
      try {
        return JSON.parse(this.analysis.characterArcs || '[]')
      } catch {
        return []
      }
    }
  },
  methods: {
    getTypeLabel(type) {
      const labels = {
        POSITIVE: '正向成长',
        NEGATIVE: '负向堕落',
        FLAT: '平坦型',
        COMPLEX: '复杂型'
      }
      return labels[type] || type
    },
    getTypeEmoji(type) {
      const emojis = {
        POSITIVE: '📈',
        NEGATIVE: '📉',
        FLAT: '➡️',
        COMPLEX: '🔄'
      }
      return emojis[type] || '👤'
    },
    getStageLabel(stage) {
      const labels = {
        ORDINARY_WORLD: '平凡世界',
        CALL_TO_ADVENTURE: '冒险召唤',
        TRIALS: '试炼阶段',
        TRANSFORMATION: '蜕变阶段',
        RETURN: '归来阶段'
      }
      return labels[stage] || stage
    },
    getStageDescription(stage) {
      const descriptions = {
        ORDINARY_WORLD: '主角尚处在日常生活中，未经历重大变化',
        CALL_TO_ADVENTURE: '主角接到召唤，开始踏上旅程',
        TRIALS: '主角面临各种试炼和挑战',
        TRANSFORMATION: '主角经历重大转变，获得成长',
        RETURN: '主角带着收获回归，完成蜕变'
      }
      return descriptions[stage] || ''
    }
  }
}
</script>
