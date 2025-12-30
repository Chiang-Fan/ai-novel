<template>
  <div class="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
    <span class="text-sm font-medium text-gray-700 w-24">{{ label }}</span>
    <div class="flex-1 flex items-center justify-between">
      <div class="flex-1 text-right">
        <span class="text-sm text-gray-600">{{ original }}</span>
        <span class="text-xs text-gray-400 ml-1">(原始)</span>
      </div>
      <div class="mx-4">
        <span v-if="original === current" class="text-green-500">✓</span>
        <span v-else class="text-orange-500">≠</span>
      </div>
      <div class="flex-1">
        <span class="text-sm text-gray-900 font-medium">{{ current }}</span>
        <span class="text-xs text-gray-400 ml-1">(当前)</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  original: [String, Number],
  current: [String, Number]
})
</script>
