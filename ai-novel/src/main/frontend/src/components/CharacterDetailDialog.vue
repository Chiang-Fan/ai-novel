<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-3xl w-full max-h-[90vh] overflow-hidden flex flex-col">
      <!-- 对话框头部 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between bg-gradient-to-r from-blue-50 to-indigo-50">
        <div>
          <h2 class="text-2xl font-bold text-gray-900">{{ character.name }}</h2>
          <div class="flex items-center gap-3 mt-1">
            <span :class="getRoleTypeClass(character.roleType)" class="px-3 py-1 text-sm rounded-full font-medium">
              {{ getRoleTypeName(character.roleType) }}
            </span>
            <span v-if="character.gender" class="text-sm text-gray-600">
              {{ getGenderName(character.gender) }}
            </span>
            <span v-if="character.age" class="text-sm text-gray-600">
              {{ character.age }}岁
            </span>
          </div>
        </div>
        <div class="flex gap-2">
          <button
            @click="$emit('edit')"
            class="p-2 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors"
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
        <DetailSection title="性格特征" :content="character.personality" icon="🎭" />
        <DetailSection title="背景故事" :content="character.background" icon="📖" />
        <DetailSection title="外貌描述" :content="character.appearance" icon="👤" />
        <DetailSection title="能力特长" :content="character.abilities" icon="⚡" />
        <DetailSection title="动机目标" :content="character.motivation" icon="🎯" />
        <DetailSection title="角色弧光" :content="character.arc" icon="📈" />
        <DetailSection title="备注" :content="character.notes" icon="📝" />
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  character: {
    type: Object,
    required: true
  }
})

defineEmits(['close', 'edit'])

const getRoleTypeName = (type) => {
  const names = {
    PROTAGONIST: '主角',
    ANTAGONIST: '反派',
    SUPPORTING: '配角',
    MINOR: '次要角色'
  }
  return names[type] || type
}

const getRoleTypeClass = (type) => {
  const classes = {
    PROTAGONIST: 'bg-purple-100 text-purple-800',
    ANTAGONIST: 'bg-red-100 text-red-800',
    SUPPORTING: 'bg-green-100 text-green-800',
    MINOR: 'bg-gray-100 text-gray-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

const getGenderName = (gender) => {
  const names = {
    MALE: '♂ 男',
    FEMALE: '♀ 女',
    OTHER: '其他'
  }
  return names[gender] || gender
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
</script>
