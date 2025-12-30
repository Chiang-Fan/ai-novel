<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
    <!-- 待埋设列 -->
    <div class="bg-gray-50 rounded-lg p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-bold text-gray-700 flex items-center">
          <span class="text-2xl mr-2">📌</span>
          待埋设
        </h3>
        <span class="px-2 py-1 bg-gray-200 text-gray-700 rounded-full text-sm font-bold">
          {{ pendingHooks.length }}
        </span>
      </div>
      <div class="space-y-3 max-h-[600px] overflow-y-auto">
        <PlotHookCard
          v-for="hook in pendingHooks"
          :key="hook.id"
          :hook="hook"
          @edit="$emit('edit', hook)"
          @delete="$emit('delete', hook.id)"
          @hint="$emit('hint', hook.id)"
        />
        <div v-if="pendingHooks.length === 0" class="text-center text-gray-400 py-8">
          暂无待埋设伏笔
        </div>
      </div>
    </div>

    <!-- 已铺垫列 -->
    <div class="bg-blue-50 rounded-lg p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-bold text-blue-700 flex items-center">
          <span class="text-2xl mr-2">✨</span>
          已铺垫
        </h3>
        <span class="px-2 py-1 bg-blue-200 text-blue-700 rounded-full text-sm font-bold">
          {{ hintedHooks.length }}
        </span>
      </div>
      <div class="space-y-3 max-h-[600px] overflow-y-auto">
        <PlotHookCard
          v-for="hook in hintedHooks"
          :key="hook.id"
          :hook="hook"
          @edit="$emit('edit', hook)"
          @delete="$emit('delete', hook.id)"
          @trigger="$emit('trigger', hook.id)"
        />
        <div v-if="hintedHooks.length === 0" class="text-center text-gray-400 py-8">
          暂无已铺垫伏笔
        </div>
      </div>
    </div>

    <!-- 已触发列 -->
    <div class="bg-yellow-50 rounded-lg p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-bold text-yellow-700 flex items-center">
          <span class="text-2xl mr-2">⚡</span>
          已触发
        </h3>
        <span class="px-2 py-1 bg-yellow-200 text-yellow-700 rounded-full text-sm font-bold">
          {{ triggeredHooks.length }}
        </span>
      </div>
      <div class="space-y-3 max-h-[600px] overflow-y-auto">
        <PlotHookCard
          v-for="hook in triggeredHooks"
          :key="hook.id"
          :hook="hook"
          @edit="$emit('edit', hook)"
          @delete="$emit('delete', hook.id)"
          @resolve="$emit('resolve', hook.id)"
        />
        <div v-if="triggeredHooks.length === 0" class="text-center text-gray-400 py-8">
          暂无已触发伏笔
        </div>
      </div>
    </div>

    <!-- 已解决列 -->
    <div class="bg-green-50 rounded-lg p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-bold text-green-700 flex items-center">
          <span class="text-2xl mr-2">✅</span>
          已解决
        </h3>
        <span class="px-2 py-1 bg-green-200 text-green-700 rounded-full text-sm font-bold">
          {{ resolvedHooks.length }}
        </span>
      </div>
      <div class="space-y-3 max-h-[600px] overflow-y-auto">
        <PlotHookCard
          v-for="hook in resolvedHooks"
          :key="hook.id"
          :hook="hook"
          @edit="$emit('edit', hook)"
          @delete="$emit('delete', hook.id)"
        />
        <div v-if="resolvedHooks.length === 0" class="text-center text-gray-400 py-8">
          暂无已解决伏笔
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import PlotHookCard from './PlotHookCard.vue'

export default {
  name: 'PlotHookKanban',
  components: {
    PlotHookCard
  },
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
    pendingHooks() {
      return this.hooks.filter(h => h.status === 'PENDING')
    },
    hintedHooks() {
      return this.hooks.filter(h => h.status === 'HINTED')
    },
    triggeredHooks() {
      return this.hooks.filter(h => h.status === 'TRIGGERED')
    },
    resolvedHooks() {
      return this.hooks.filter(h => h.status === 'RESOLVED')
    }
  }
}
</script>
