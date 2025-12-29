<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-3xl w-full max-h-[90vh] overflow-hidden flex flex-col">
      <!-- 对话框头部 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between bg-gradient-to-r from-green-50 to-emerald-50">
        <div>
          <div class="flex items-center gap-3 mb-1">
            <span class="text-3xl">{{ getSceneTypeIcon(scene.sceneType) }}</span>
            <h2 class="text-2xl font-bold text-gray-900">{{ scene.name }}</h2>
          </div>
          <span :class="getSceneTypeClass(scene.sceneType)" class="px-3 py-1 text-sm rounded-full font-medium">
            {{ getSceneTypeName(scene.sceneType) }}
          </span>
        </div>
        <div class="flex gap-2">
          <button
            @click="$emit('edit')"
            class="p-2 text-green-600 hover:bg-green-100 rounded-lg transition-colors"
            title="编辑"
          >
            ✏️
          </button>
          <button
            @click="$emit('close')"
            class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <span class="text-2xl text-gray-500">×</span>
          </button>
        </div>
      </div>

      <!-- 详情内容 -->
      <div class="flex-1 overflow-y-auto p-6 space-y-6">
        <!-- 基本属性 -->
        <div class="grid grid-cols-2 gap-4">
          <DetailItem v-if="scene.location" icon="📍" label="地点" :value="scene.location" />
          <DetailItem v-if="scene.timePeriod" icon="🕐" label="时间段" :value="scene.timePeriod" />
          <DetailItem v-if="scene.weather" icon="🌤️" label="天气" :value="scene.weather" />
        </div>

        <DetailSection title="场景描述" :content="scene.description" icon="📝" />
        <DetailSection title="氛围描述" :content="scene.atmosphere" icon="🎭" />
        <DetailSection title="重要道具" :content="scene.props" icon="🎁" />
        <DetailSection title="相关角色" :content="scene.involvedCharacters" icon="👥" />
        <DetailSection title="出现章节" :content="scene.chapterReferences" icon="📚" />
        <DetailSection title="备注" :content="scene.notes" icon="📌" />
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  scene: {
    type: Object,
    required: true
  }
})

defineEmits(['close', 'edit'])

const getSceneTypeName = (type) => {
  const names = {
    LOCATION: '地点',
    EVENT: '事件',
    TIME_PERIOD: '时间段'
  }
  return names[type] || type
}

const getSceneTypeIcon = (type) => {
  const icons = {
    LOCATION: '📍',
    EVENT: '⚡',
    TIME_PERIOD: '🕐'
  }
  return icons[type] || '🎬'
}

const getSceneTypeClass = (type) => {
  const classes = {
    LOCATION: 'bg-blue-100 text-blue-800',
    EVENT: 'bg-amber-100 text-amber-800',
    TIME_PERIOD: 'bg-indigo-100 text-indigo-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}
</script>

<script>
// DetailSection 组件
export const DetailSection = {
  props: ['title', 'content', 'icon'],
  template: `
    <div v-if="content" class="bg-gray-50 rounded-lg p-4">
      <h3 class="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
        <span>{{ icon }}</span>
        {{ title }}
      </h3>
      <p class="text-gray-800 whitespace-pre-wrap leading-relaxed">{{ content }}</p>
    </div>
  `
}

// DetailItem 组件
export const DetailItem = {
  props: ['icon', 'label', 'value'],
  template: `
    <div class="bg-gray-50 rounded-lg p-3">
      <div class="text-xs text-gray-500 font-medium mb-1 flex items-center gap-1">
        <span>{{ icon }}</span>
        {{ label }}
      </div>
      <div class="text-sm font-medium text-gray-900">{{ value }}</div>
    </div>
  `
}
</script>
