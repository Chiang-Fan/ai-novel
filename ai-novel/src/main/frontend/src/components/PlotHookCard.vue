<template>
  <div
    :class="[
      'bg-white rounded-lg shadow-md p-4 border-l-4 hover:shadow-lg transition cursor-pointer',
      getBorderColor(hook.status),
      hook.isOverdue ? 'ring-2 ring-red-400' : ''
    ]">
    <!-- 标题 -->
    <div class="flex items-start justify-between mb-2">
      <h4 class="font-bold text-gray-800 flex-1">{{ hook.title }}</h4>
      <div class="flex flex-col items-end space-y-1">
        <!-- 优先级 -->
        <div class="flex items-center">
          <span
            v-for="i in hook.priority"
            :key="i"
            class="text-yellow-400 text-xs">⭐</span>
        </div>
        <!-- 超期标志 -->
        <span v-if="hook.isOverdue" class="px-1.5 py-0.5 bg-red-100 text-red-700 text-xs rounded-full animate-pulse">
          超期
        </span>
      </div>
    </div>

    <!-- 描述 -->
    <p class="text-sm text-gray-600 mb-3 line-clamp-2">{{ hook.description }}</p>

    <!-- 信息标签 -->
    <div class="flex flex-wrap gap-1 mb-3">
      <span class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded">
        Ch.{{ hook.plantedInChapter }}
      </span>
      <span class="px-2 py-0.5 bg-purple-100 text-purple-700 text-xs rounded">
        {{ getTypeText(hook.type) }}
      </span>
      <span v-if="hook.isAutoDetected" class="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded">
        🤖 AI
      </span>
    </div>

    <!-- 章节信息 -->
    <div class="text-xs text-gray-500 mb-3">
      <div v-if="hook.expectedChapter">
        预期揭示: Ch.{{ hook.expectedChapter }}
      </div>
      <div v-if="hook.triggeredInChapter">
        触发于: Ch.{{ hook.triggeredInChapter }}
      </div>
      <div v-if="hook.resolvedInChapter">
        解决于: Ch.{{ hook.resolvedInChapter }}
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="flex flex-wrap gap-2">
      <button
        @click.stop="$emit('edit', hook)"
        class="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs hover:bg-blue-200">
        编辑
      </button>
      
      <button
        v-if="hook.status === 'PENDING'"
        @click.stop="$emit('hint')"
        class="px-2 py-1 bg-indigo-100 text-indigo-700 rounded text-xs hover:bg-indigo-200">
        标记铺垫
      </button>
      
      <button
        v-if="hook.status === 'HINTED'"
        @click.stop="$emit('trigger')"
        class="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-xs hover:bg-yellow-200">
        触发
      </button>
      
      <button
        v-if="hook.status === 'TRIGGERED'"
        @click.stop="$emit('resolve')"
        class="px-2 py-1 bg-green-100 text-green-700 rounded text-xs hover:bg-green-200">
        解决
      </button>
      
      <button
        @click.stop="$emit('delete')"
        class="px-2 py-1 bg-red-100 text-red-700 rounded text-xs hover:bg-red-200">
        删除
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlotHookCard',
  props: {
    hook: {
      type: Object,
      required: true
    }
  },
  methods: {
    getBorderColor(status) {
      const colors = {
        PENDING: 'border-gray-400',
        HINTED: 'border-blue-500',
        TRIGGERED: 'border-yellow-500',
        RESOLVED: 'border-green-500'
      }
      return colors[status] || 'border-gray-400'
    },

    getTypeText(type) {
      const texts = {
        EXPLICIT: '明示',
        IMPLICIT: '暗示',
        CHEKHOV_GUN: '契诃夫枪'
      }
      return texts[type] || type
    }
  }
}
</script>
