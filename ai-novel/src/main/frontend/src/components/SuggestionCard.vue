<template>
  <div class="suggestion-card bg-white rounded-lg shadow-sm border-l-4 p-5 hover:shadow-md transition"
       :class="getPriorityBorderColor(suggestion.priority)">
    <div class="flex items-start justify-between mb-3">
      <div class="flex-1">
        <div class="flex items-center space-x-2 mb-2">
          <span class="text-2xl">{{ getTypeEmoji(suggestion.type) }}</span>
          <span class="font-semibold text-gray-800">{{ suggestion.title }}</span>
          <span
            :class="[
              'px-2 py-0.5 rounded text-xs font-medium',
              getPriorityClass(suggestion.priority)
            ]">
            优先级: {{ suggestion.priority }}
          </span>
          <span class="text-xs text-gray-500">
            相关性: {{ (suggestion.relevanceScore * 100).toFixed(0) }}%
          </span>
        </div>
        <p class="text-gray-700 whitespace-pre-wrap">{{ suggestion.content }}</p>
      </div>
    </div>

    <!-- 推荐依据 -->
    <div v-if="suggestion.basis && suggestion.basis.length > 0" class="mt-3 pt-3 border-t border-gray-200">
      <div class="text-sm font-medium text-gray-700 mb-1">📋 推荐依据：</div>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="(item, index) in suggestion.basis"
          :key="index"
          class="px-2 py-1 bg-blue-50 text-blue-700 rounded text-xs">
          {{ item }}
        </span>
      </div>
    </div>

    <!-- 预期影响 -->
    <div v-if="suggestion.expectedImpact" class="mt-3 pt-3 border-t border-gray-200">
      <div class="text-sm font-medium text-gray-700 mb-1">💡 预期影响：</div>
      <div class="text-sm text-gray-600">
        <span class="font-medium">{{ suggestion.expectedImpact.dimension }}：</span>
        {{ suggestion.expectedImpact.description }}
        <span v-if="suggestion.expectedImpact.scoreChange" class="text-green-600 font-medium">
          (+{{ suggestion.expectedImpact.scoreChange }})
        </span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="mt-4 pt-4 border-t border-gray-200 flex justify-end space-x-3">
      <button
        @click="onReject"
        class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition text-sm">
        ❌ 拒绝
      </button>
      <button
        @click="onAccept"
        class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition text-sm">
        ✅ 采纳
      </button>
    </div>

    <!-- 元信息 -->
    <div class="mt-3 text-xs text-gray-400">
      创建时间: {{ formatDate(suggestion.createdAt) }}
      <span v-if="suggestion.expiresAt"> • 过期时间: {{ formatDate(suggestion.expiresAt) }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SuggestionCard',
  props: {
    suggestion: {
      type: Object,
      required: true
    }
  },
  methods: {
    getTypeEmoji(type) {
      const emojis = {
        PLOT: '📍',
        CHARACTER: '👤',
        STYLE: '🎨',
        PACING: '⏱️',
        HOOK: '🎭',
        CONFLICT: '⚔️',
        THEME: '🎯',
        QUALITY: '⭐',
        WORLD_BUILDING: '🌍',
        EMOTION: '💓'
      }
      return emojis[type] || '💡'
    },
    getPriorityClass(priority) {
      if (priority >= 8) return 'bg-red-100 text-red-700'
      if (priority >= 5) return 'bg-yellow-100 text-yellow-700'
      return 'bg-green-100 text-green-700'
    },
    getPriorityBorderColor(priority) {
      if (priority >= 8) return 'border-red-500'
      if (priority >= 5) return 'border-yellow-500'
      return 'border-green-500'
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    onAccept() {
      const feedback = prompt('请输入采纳反馈（可选）:')
      this.$emit('accept', this.suggestion.id, feedback)
    },
    onReject() {
      const reason = prompt('请输入拒绝理由（可选）:')
      if (reason !== null) {
        this.$emit('reject', this.suggestion.id, reason)
      }
    }
  }
}
</script>
