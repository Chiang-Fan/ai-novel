<template>
  <div class="bg-white rounded-xl shadow-lg p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-gray-800">📊 伏笔统计</h2>
      <button
        @click="$emit('detect')"
        :disabled="loading"
        class="px-4 py-2 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg hover:shadow-lg transition disabled:opacity-50">
        🤖 AI 自动检测
      </button>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
      <p class="text-gray-500 mt-2">加载中...</p>
    </div>

    <div v-else-if="statistics" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
      <!-- 总数 -->
      <StatCard
        icon="🎭"
        :value="statistics.totalCount"
        label="伏笔总数"
        color="purple"
      />

      <!-- 待埋设 -->
      <StatCard
        icon="📌"
        :value="statistics.pendingCount"
        label="待埋设"
        color="gray"
      />

      <!-- 已铺垫 -->
      <StatCard
        icon="✨"
        :value="statistics.hintedCount"
        label="已铺垫"
        color="blue"
      />

      <!-- 已触发 -->
      <StatCard
        icon="⚡"
        :value="statistics.triggeredCount"
        label="已触发"
        color="yellow"
      />

      <!-- 已解决 -->
      <StatCard
        icon="✅"
        :value="statistics.resolvedCount"
        label="已解决"
        color="green"
      />

      <!-- 超期 -->
      <StatCard
        icon="⏰"
        :value="statistics.overdueCount"
        label="超期未触发"
        color="red"
      />
    </div>

    <!-- 超期警告 -->
    <div v-if="statistics && statistics.overdueCount > 0" class="mt-4 p-4 bg-red-50 border-l-4 border-red-500 rounded">
      <div class="flex items-center">
        <span class="text-2xl mr-2">⚠️</span>
        <div>
          <p class="font-bold text-red-800">超期警告</p>
          <p class="text-sm text-red-600">有 {{ statistics.overdueCount }} 个伏笔已超过预期揭示时间，请及时处理！</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import StatCard from './StatCard.vue'

export default {
  name: 'PlotHookStatPanel',
  components: {
    StatCard
  },
  props: {
    statistics: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>
