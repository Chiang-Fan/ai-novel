<template>
  <div class="inline-block">
    <button
      :disabled="loading"
      :class="[
        'px-4 py-2 rounded-lg font-medium transition-all duration-200 flex items-center gap-2',
        variantClasses,
        sizeClasses,
        loading ? 'opacity-70 cursor-not-allowed' : 'hover:shadow-lg'
      ]"
      @click="handleClick"
    >
      <!-- Loading Spinner -->
      <svg 
        v-if="loading" 
        class="animate-spin h-4 w-4" 
        xmlns="http://www.w3.org/2000/svg" 
        fill="none" 
        viewBox="0 0 24 24"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      
      <!-- Icon -->
      <component v-if="icon && !loading" :is="icon" class="w-5 h-5" />
      
      <!-- Content -->
      <slot></slot>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'success', 'danger', 'ghost'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  loading: {
    type: Boolean,
    default: false
  },
  icon: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['click'])

const variantClasses = computed(() => {
  const variants = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700',
    secondary: 'bg-gray-600 text-white hover:bg-gray-700',
    success: 'bg-green-600 text-white hover:bg-green-700',
    danger: 'bg-red-600 text-white hover:bg-red-700',
    ghost: 'bg-transparent text-gray-700 border border-gray-300 hover:bg-gray-50'
  }
  return variants[props.variant] || variants.primary
})

const sizeClasses = computed(() => {
  const sizes = {
    sm: 'text-sm px-3 py-1.5',
    md: 'text-base px-4 py-2',
    lg: 'text-lg px-6 py-3'
  }
  return sizes[props.size] || sizes.md
})

const handleClick = (event) => {
  if (!props.loading) {
    emit('click', event)
  }
}
</script>
