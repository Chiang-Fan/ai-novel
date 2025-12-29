<template>
  <div class="outline-node">
    <!-- 节点本体 -->
    <div 
      class="flex items-start gap-3 p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors"
      :style="{ paddingLeft: `${level * 2 + 1}rem` }"
    >
      <!-- 展开/收起按钮 -->
      <button
        v-if="hasChildren"
        @click="$emit('toggle', node.id)"
        class="mt-1 p-1 hover:bg-gray-200 rounded transition-colors flex-shrink-0"
      >
        <span class="text-sm">{{ isExpanded ? '▼' : '▶' }}</span>
      </button>
      <div v-else class="w-6"></div>

      <!-- 节点图标 -->
      <div class="mt-1 text-xl flex-shrink-0">
        {{ getNodeIcon(node.nodeType) }}
      </div>

      <!-- 节点内容 -->
      <div class="flex-1 min-w-0">
        <div class="flex items-start justify-between gap-3">
          <div class="flex-1">
            <!-- 标题和类型 -->
            <div class="flex items-center gap-2 mb-1">
              <h4 class="text-base font-semibold text-gray-900">
                {{ node.sequenceNumber }}. {{ node.title }}
              </h4>
              <span :class="getNodeTypeClass(node.nodeType)" class="px-2 py-0.5 text-xs rounded-full flex-shrink-0">
                {{ getNodeTypeName(node.nodeType) }}
              </span>
              <span v-if="node.status" :class="getStatusClass(node.status)" class="px-2 py-0.5 text-xs rounded-full flex-shrink-0">
                {{ getStatusName(node.status) }}
              </span>
            </div>

            <!-- 概要 -->
            <p v-if="node.summary" class="text-sm text-gray-600 mb-2 line-clamp-2">
              {{ node.summary }}
            </p>

            <!-- 元信息 -->
            <div class="flex items-center gap-4 text-xs text-gray-500">
              <span v-if="node.targetWordCount">📊 目标 {{ node.targetWordCount }} 字</span>
              <span v-if="node.chapterId">🔗 已关联章节 #{{ node.chapterId }}</span>
              <span v-if="childCount > 0">📁 {{ childCount }} 个子节点</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-1 flex-shrink-0">
            <button
              @click="$emit('add-child', node)"
              class="p-1.5 text-green-600 hover:bg-green-50 rounded transition-colors"
              title="添加子节点"
            >
              ➕
            </button>
            <button
              @click="$emit('edit', node)"
              class="p-1.5 text-blue-600 hover:bg-blue-50 rounded transition-colors"
              title="编辑"
            >
              ✏️
            </button>
            <button
              @click="$emit('delete', node.id)"
              class="p-1.5 text-red-600 hover:bg-red-50 rounded transition-colors"
              title="删除"
            >
              🗑️
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 子节点（递归） -->
    <div v-if="hasChildren && isExpanded">
      <OutlineNode
        v-for="child in children"
        :key="child.id"
        :node="child"
        :all-outlines="allOutlines"
        :expanded-nodes="expandedNodes"
        :level="level + 1"
        @toggle="$emit('toggle', $event)"
        @edit="$emit('edit', $event)"
        @add-child="$emit('add-child', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  node: {
    type: Object,
    required: true
  },
  allOutlines: {
    type: Array,
    required: true
  },
  expandedNodes: {
    type: Set,
    required: true
  },
  level: {
    type: Number,
    default: 0
  }
})

defineEmits(['toggle', 'edit', 'add-child', 'delete'])

// 计算子节点
const children = computed(() => {
  return props.allOutlines
    .filter(n => n.parentId === props.node.id)
    .sort((a, b) => a.sequenceNumber - b.sequenceNumber)
})

const hasChildren = computed(() => children.value.length > 0)
const isExpanded = computed(() => props.expandedNodes.has(props.node.id))
const childCount = computed(() => children.value.length)

// 工具函数
const getNodeTypeName = (type) => {
  const names = {
    ARC: '故事弧',
    VOLUME: '卷',
    CHAPTER: '章',
    SECTION: '节'
  }
  return names[type] || type
}

const getNodeIcon = (type) => {
  const icons = {
    ARC: '📚',
    VOLUME: '📖',
    CHAPTER: '📄',
    SECTION: '📝'
  }
  return icons[type] || '📋'
}

const getNodeTypeClass = (type) => {
  const classes = {
    ARC: 'bg-purple-100 text-purple-800',
    VOLUME: 'bg-blue-100 text-blue-800',
    CHAPTER: 'bg-green-100 text-green-800',
    SECTION: 'bg-amber-100 text-amber-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

const getStatusName = (status) => {
  const names = {
    PLANNED: '计划中',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成'
  }
  return names[status] || status
}

const getStatusClass = (status) => {
  const classes = {
    PLANNED: 'bg-gray-100 text-gray-700',
    IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
    COMPLETED: 'bg-green-100 text-green-700'
  }
  return classes[status] || 'bg-gray-100 text-gray-700'
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
