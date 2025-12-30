<template>
  <div class="bg-white rounded-xl shadow-lg p-6">
    <h2 class="text-xl font-bold text-gray-800 mb-6">📈 伏笔时间线</h2>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
      <p class="text-gray-500 mt-4">加载中...</p>
    </div>

    <div v-else-if="sortedHooks.length === 0" class="text-center py-12">
      <p class="text-gray-400 text-lg">还没有任何伏笔</p>
      <p class="text-gray-400 text-sm mt-2">点击右上角「新建伏笔」开始创建</p>
    </div>

    <div v-else class="relative">
      <!-- 时间线主轴 -->
      <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-purple-300 via-blue-300 to-green-300"></div>

      <!-- 伏笔节点 -->
      <div
        v-for="(hook, index) in sortedHooks"
        :key="hook.id"
        class="relative pl-20 pb-8">
        <!-- 节点圆圈 -->
        <div
          :class="[
            'absolute left-6 w-5 h-5 rounded-full border-4 border-white shadow-lg',
            getNodeColor(hook.status)
          ]"
        ></div>

        <!-- 章节号标签 -->
        <div class="absolute left-0 top-0 text-xs font-bold text-gray-500">
          Ch.{{ hook.plantedInChapter }}
        </div>

        <!-- 伏笔卡片 -->
        <div
          :class="[
            'bg-white rounded-lg shadow-md border-2 p-4 hover:shadow-xl transition',
            getBorderColor(hook.status),
            hook.isOverdue ? 'ring-2 ring-red-500' : ''
          ]">
          <!-- 标题行 -->
          <div class="flex items-start justify-between mb-2">
            <div class="flex-1">
              <div class="flex items-center space-x-2">
                <h3 class="font-bold text-gray-800">{{ hook.title }}</h3>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs rounded-full font-bold',
                    getStatusBadgeClass(hook.status)
                  ]">
                  {{ getStatusText(hook.status) }}
                </span>
                <span v-if="hook.isAutoDetected" class="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded-full">
                  🤖 AI检测
                </span>
                <span v-if="hook.isOverdue" class="px-2 py-0.5 text-xs bg-red-100 text-red-700 rounded-full animate-pulse">
                  ⏰ 超期
                </span>
              </div>
              <p class="text-sm text-gray-600 mt-1">{{ hook.description }}</p>
            </div>

            <!-- 优先级 -->
            <div class="ml-4">
              <div class="flex items-center space-x-1">
                <span
                  v-for="i in hook.priority"
                  :key="i"
                  class="text-yellow-400">⭐</span>
              </div>
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="grid grid-cols-2 gap-2 text-xs text-gray-600 mt-3 pt-3 border-t">
            <div>
              <span class="font-semibold">类型:</span>
              {{ getTypeText(hook.type) }}
            </div>
            <div>
              <span class="font-semibold">预期揭示:</span>
              Ch.{{ hook.expectedChapter || '?' }}
            </div>
            <div v-if="hook.triggeredInChapter">
              <span class="font-semibold">实际触发:</span>
              Ch.{{ hook.triggeredInChapter }}
            </div>
            <div v-if="hook.resolvedInChapter">
              <span class="font-semibold">解决章节:</span>
              Ch.{{ hook.resolvedInChapter }}
            </div>
          </div>

          <!-- 相关角色 -->
          <div v-if="hook.relatedCharacters && hook.relatedCharacters.length > 0" class="mt-2">
            <span class="text-xs text-gray-500">相关角色: </span>
            <span
              v-for="char in hook.relatedCharacters"
              :key="char"
              class="inline-block px-2 py-0.5 bg-purple-100 text-purple-700 rounded-full text-xs mr-1">
              {{ char }}
            </span>
          </div>

          <!-- 操作按钮 -->
          <div class="flex space-x-2 mt-4">
            <button
              @click="$emit('edit', hook)"
              class="px-3 py-1 bg-blue-100 text-blue-700 rounded hover:bg-blue-200 text-sm">
              编辑
            </button>
            <button
              v-if="hook.status === 'PENDING'"
              @click="$emit('hint', hook.id)"
              class="px-3 py-1 bg-indigo-100 text-indigo-700 rounded hover:bg-indigo-200 text-sm">
              标记铺垫
            </button>
            <button
              v-if="hook.status === 'HINTED'"
              @click="$emit('trigger', hook.id)"
              class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded hover:bg-yellow-200 text-sm">
              触发伏笔
            </button>
            <button
              v-if="hook.status === 'TRIGGERED'"
              @click="$emit('resolve', hook.id)"
              class="px-3 py-1 bg-green-100 text-green-700 rounded hover:bg-green-200 text-sm">
              解决伏笔
            </button>
            <button
              @click="$emit('delete', hook.id)"
              class="px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200 text-sm">
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlotHookTimeline',
  props: {
    hooks: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    sortedHooks() {
      return [...this.hooks].sort((a, b) => a.plantedInChapter - b.plantedInChapter)
    }
  },
  methods: {
    getNodeColor(status) {
      const colors = {
        PENDING: 'bg-gray-400',
        HINTED: 'bg-blue-500',
        TRIGGERED: 'bg-yellow-500',
        RESOLVED: 'bg-green-500'
      }
      return colors[status] || 'bg-gray-400'
    },

    getBorderColor(status) {
      const colors = {
        PENDING: 'border-gray-300',
        HINTED: 'border-blue-300',
        TRIGGERED: 'border-yellow-300',
        RESOLVED: 'border-green-300'
      }
      return colors[status] || 'border-gray-300'
    },

    getStatusBadgeClass(status) {
      const classes = {
        PENDING: 'bg-gray-100 text-gray-700',
        HINTED: 'bg-blue-100 text-blue-700',
        TRIGGERED: 'bg-yellow-100 text-yellow-700',
        RESOLVED: 'bg-green-100 text-green-700'
      }
      return classes[status] || 'bg-gray-100 text-gray-700'
    },

    getStatusText(status) {
      const texts = {
        PENDING: '待埋设',
        HINTED: '已铺垫',
        TRIGGERED: '已触发',
        RESOLVED: '已解决'
      }
      return texts[status] || status
    },

    getTypeText(type) {
      const texts = {
        EXPLICIT: '明示伏笔',
        IMPLICIT: '暗示伏笔',
        CHEKHOV_GUN: '契诃夫的枪'
      }
      return texts[type] || type
    }
  }
}
</script>
