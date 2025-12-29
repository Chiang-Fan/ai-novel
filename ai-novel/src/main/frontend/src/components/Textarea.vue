<template>
  <div class="flex flex-col gap-2">
    <label v-if="label" :for="id" class="text-sm font-medium text-gray-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <textarea
      :id="id"
      :value="modelValue"
      :placeholder="placeholder"
      :required="required"
      :disabled="disabled"
      :rows="rows"
      :class="[
        'w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition-all resize-none',
        error 
          ? 'border-red-300 focus:border-red-500 focus:ring-red-200' 
          : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200',
        disabled ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'
      ]"
      @input="$emit('update:modelValue', $event.target.value)"
      @blur="$emit('blur')"
    ></textarea>
    
    <div class="flex items-center justify-between">
      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
      <p v-else-if="hint" class="text-sm text-gray-500">{{ hint }}</p>
      <span v-if="showCount" class="text-sm text-gray-400 ml-auto">
        {{ modelValue?.length || 0 }}{{ maxLength ? `/${maxLength}` : '' }}
      </span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  id: String,
  label: String,
  modelValue: String,
  placeholder: String,
  required: Boolean,
  disabled: Boolean,
  rows: {
    type: Number,
    default: 4
  },
  error: String,
  hint: String,
  showCount: Boolean,
  maxLength: Number
})

defineEmits(['update:modelValue', 'blur'])
</script>
