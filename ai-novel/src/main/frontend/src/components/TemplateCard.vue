<template>
  <div 
    class="p-6 bg-gradient-to-br from-white to-gray-50 rounded-lg border-2 border-gray-200 hover:border-blue-400 transition-all cursor-pointer hover:shadow-lg"
    @click="$emit('apply', template)"
  >
    <div class="flex items-start justify-between mb-3">
      <h3 class="text-lg font-bold text-gray-900">{{ template.name }}</h3>
      <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">模板</span>
    </div>
    
    <p class="text-sm text-gray-600 mb-4">{{ template.description }}</p>
    
    <div class="space-y-2 text-sm">
      <div class="flex items-center text-gray-700">
        <span class="font-medium w-20">视角:</span>
        <span>{{ perspectiveLabel }}</span>
      </div>
      <div class="flex items-center text-gray-700">
        <span class="font-medium w-20">语气:</span>
        <span>{{ template.tone }}</span>
      </div>
      <div class="flex items-center text-gray-700">
        <span class="font-medium w-20">句式:</span>
        <span>{{ template.sentenceStyle }}</span>
      </div>
      <div class="flex flex-wrap gap-1 mt-2">
        <span 
          v-for="keyword in keywords" 
          :key="keyword"
          class="px-2 py-0.5 bg-gray-100 text-gray-700 rounded text-xs"
        >
          {{ keyword }}
        </span>
      </div>
    </div>
    
    <div class="mt-4 pt-4 border-t border-gray-200 text-center">
      <span class="text-blue-600 text-sm font-medium">点击应用此模板</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  template: Object
})

defineEmits(['apply'])

const perspectiveLabel = computed(() => {
  const map = {
    'first': '第一人称',
    'third_limited': '第三人称限制',
    'third_omniscient': '第三人称全知'
  }
  return map[props.template.perspective] || props.template.perspective
})

const keywords = computed(() => {
  if (!props.template.keywords) return []
  try {
    return typeof props.template.keywords === 'string' 
      ? JSON.parse(props.template.keywords) 
      : props.template.keywords
  } catch {
    return []
  }
})
</script>
