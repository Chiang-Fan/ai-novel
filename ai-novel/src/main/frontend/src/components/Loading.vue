<template>
  <div class="flex items-center justify-center" :class="fullscreen ? 'fixed inset-0 bg-white bg-opacity-90 z-50' : 'py-12'">
    <div class="flex flex-col items-center gap-4">
      <!-- Spinner -->
      <div class="relative">
        <div class="w-16 h-16 border-4 border-blue-200 rounded-full"></div>
        <div class="w-16 h-16 border-4 border-blue-600 rounded-full animate-spin border-t-transparent absolute top-0 left-0"></div>
      </div>
      
      <!-- Text -->
      <p class="text-gray-600 text-lg">{{ text }}</p>
      
      <!-- Subtext -->
      <p v-if="subtext" class="text-gray-400 text-sm">{{ subtext }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '加载中...'
  },
  subtext: String,
  fullscreen: Boolean
})
</script>
