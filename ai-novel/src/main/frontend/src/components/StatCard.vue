<template>
  <div 
    class="p-6 rounded-lg border-2 transition-all cursor-pointer"
    :class="colorClass"
    @click="$emit('click')"
  >
    <div class="flex items-center justify-between mb-2">
      <div class="text-4xl">{{ icon }}</div>
      <div :class="`text-3xl font-bold ${textColorClass}`">
        {{ value }}
      </div>
    </div>
    <div class="text-sm text-gray-600 font-medium">{{ title }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: String,
  value: [String, Number],
  icon: String,
  color: {
    type: String,
    default: 'blue'
  }
})

defineEmits(['click'])

const colorClass = computed(() => {
  const colors = {
    blue: 'bg-blue-50 border-blue-200 hover:bg-blue-100',
    purple: 'bg-purple-50 border-purple-200 hover:bg-purple-100',
    green: 'bg-green-50 border-green-200 hover:bg-green-100',
    orange: 'bg-orange-50 border-orange-200 hover:bg-orange-100',
    red: 'bg-red-50 border-red-200 hover:bg-red-100',
    gray: 'bg-gray-50 border-gray-200 hover:bg-gray-100'
  }
  return colors[props.color] || colors.blue
})

const textColorClass = computed(() => {
  const colors = {
    blue: 'text-blue-600',
    purple: 'text-purple-600',
    green: 'text-green-600',
    orange: 'text-orange-600',
    red: 'text-red-600',
    gray: 'text-gray-600'
  }
  return colors[props.color] || colors.blue
})
</script>
