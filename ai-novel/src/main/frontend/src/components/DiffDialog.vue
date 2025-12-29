<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-6xl w-full max-h-[90vh] overflow-hidden flex flex-col">
      <!-- 对话框头部 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between bg-gradient-to-r from-blue-50 to-indigo-50">
        <div>
          <h2 class="text-2xl font-bold text-gray-900">内容对比</h2>
          <p class="text-sm text-gray-600 mt-1">
            {{ formatTime(record.createdAt) }} • 字数变化: 
            <span :class="record.wordCountDiff > 0 ? 'text-green-600' : 'text-red-600'" class="font-semibold">
              {{ record.wordCountDiff > 0 ? '+' : '' }}{{ record.wordCountDiff }}
            </span>
          </p>
        </div>
        <button
          @click="$emit('close')"
          class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <span class="text-2xl text-gray-500">×</span>
        </button>
      </div>

      <!-- 对比内容 -->
      <div class="flex-1 overflow-hidden flex">
        <!-- 修改前 -->
        <div class="flex-1 border-r border-gray-200 flex flex-col">
          <div class="px-6 py-3 bg-red-50 border-b border-red-100">
            <h3 class="font-semibold text-red-800">修改前</h3>
            <p class="text-xs text-red-600 mt-1">
              字数: {{ record.contentBefore ? record.contentBefore.length : 0 }}
            </p>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-4">
            <pre v-if="record.contentBefore" class="text-sm text-gray-800 whitespace-pre-wrap leading-relaxed font-sans">{{ record.contentBefore }}</pre>
            <div v-else class="text-gray-400 text-center py-12">无内容</div>
          </div>
        </div>

        <!-- 修改后 -->
        <div class="flex-1 flex flex-col">
          <div class="px-6 py-3 bg-green-50 border-b border-green-100">
            <h3 class="font-semibold text-green-800">修改后</h3>
            <p class="text-xs text-green-600 mt-1">
              字数: {{ record.contentAfter ? record.contentAfter.length : 0 }}
            </p>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-4">
            <pre v-if="record.contentAfter" class="text-sm text-gray-800 whitespace-pre-wrap leading-relaxed font-sans">{{ record.contentAfter }}</pre>
            <div v-else class="text-gray-400 text-center py-12">无内容</div>
          </div>
        </div>
      </div>

      <!-- 对话框底部 -->
      <div class="px-6 py-4 border-t border-gray-200 flex justify-between items-center bg-gray-50">
        <div class="text-sm text-gray-600">
          <span v-if="record.editReason">编辑原因: {{ record.editReason }}</span>
        </div>
        <button
          @click="$emit('close')"
          class="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors font-medium"
        >
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

defineEmits(['close'])

const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>
