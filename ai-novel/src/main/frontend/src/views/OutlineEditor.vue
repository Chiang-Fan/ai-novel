<template>
  <div class="outline-editor min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4 max-w-7xl">
      <!-- 页面头部 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">大纲管理</h1>
            <p class="mt-1 text-sm text-gray-600">规划小说的结构和情节发展</p>
          </div>
          <div class="flex gap-3">
            <button
              @click="expandAll"
              class="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
            >
              🔽 展开全部
            </button>
            <button
              @click="collapseAll"
              class="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
            >
              🔼 收起全部
            </button>
            <button
              @click="createRootNode"
              class="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-medium shadow-lg shadow-indigo-500/30"
            >
              <span class="mr-2">+</span> 添加根节点
            </button>
          </div>
        </div>
        
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-5 gap-4 mt-6">
          <div class="bg-white rounded-lg p-4 shadow">
            <div class="text-sm text-gray-600">总节点数</div>
            <div class="text-2xl font-bold text-gray-900">{{ outlines.length }}</div>
          </div>
          <div class="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">故事弧</div>
            <div class="text-2xl font-bold">{{ getNodesByType('ARC').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">卷</div>
            <div class="text-2xl font-bold">{{ getNodesByType('VOLUME').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">章</div>
            <div class="text-2xl font-bold">{{ getNodesByType('CHAPTER').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-amber-500 to-amber-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">节</div>
            <div class="text-2xl font-bold">{{ getNodesByType('SECTION').length }}</div>
          </div>
        </div>
      </div>

      <!-- 大纲树形视图 -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-gray-300 border-t-indigo-600"></div>
        <p class="mt-4 text-gray-600">加载中...</p>
      </div>

      <div v-else-if="rootNodes.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
        <div class="text-gray-400 text-5xl mb-4">📋</div>
        <p class="text-gray-600">暂无大纲，点击"添加根节点"创建第一个大纲节点</p>
      </div>

      <div v-else class="bg-white rounded-lg shadow">
        <OutlineNode
          v-for="node in rootNodes"
          :key="node.id"
          :node="node"
          :all-outlines="outlines"
          :expanded-nodes="expandedNodes"
          @toggle="toggleNode"
          @edit="editNode"
          @add-child="addChildNode"
          @delete="deleteNode"
        />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <OutlineDialog
      v-if="showDialog"
      :node="editingNode"
      :parent-id="parentId"
      :novel-id="novelId"
      @close="closeDialog"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import OutlineNode from '../components/OutlineNode.vue'
import OutlineDialog from '../components/OutlineDialog.vue'

const route = useRoute()
const novelId = ref(parseInt(route.params.id))

const outlines = ref([])
const loading = ref(false)
const expandedNodes = ref(new Set())

const showDialog = ref(false)
const editingNode = ref(null)
const parentId = ref(null)

// 计算根节点
const rootNodes = computed(() => {
  return outlines.value
    .filter(n => !n.parentId)
    .sort((a, b) => a.sequenceNumber - b.sequenceNumber)
})

const getNodesByType = (type) => {
  return outlines.value.filter(n => n.nodeType === type)
}

// 加载大纲列表
const loadOutlines = async () => {
  loading.value = true
  try {
    outlines.value = await api.outlines.list(novelId.value)
    // 默认展开根节点
    rootNodes.value.forEach(node => {
      expandedNodes.value.add(node.id)
    })
  } catch (error) {
    console.error('加载大纲失败:', error)
    alert('加载大纲失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 展开/收起节点
const toggleNode = (nodeId) => {
  if (expandedNodes.value.has(nodeId)) {
    expandedNodes.value.delete(nodeId)
  } else {
    expandedNodes.value.add(nodeId)
  }
  // 触发响应式更新
  expandedNodes.value = new Set(expandedNodes.value)
}

// 展开全部
const expandAll = () => {
  outlines.value.forEach(node => {
    expandedNodes.value.add(node.id)
  })
  expandedNodes.value = new Set(expandedNodes.value)
}

// 收起全部
const collapseAll = () => {
  expandedNodes.value.clear()
  // 保持根节点展开
  rootNodes.value.forEach(node => {
    expandedNodes.value.add(node.id)
  })
  expandedNodes.value = new Set(expandedNodes.value)
}

// 创建根节点
const createRootNode = () => {
  editingNode.value = null
  parentId.value = null
  showDialog.value = true
}

// 添加子节点
const addChildNode = (node) => {
  editingNode.value = null
  parentId.value = node.id
  showDialog.value = true
}

// 编辑节点
const editNode = (node) => {
  editingNode.value = { ...node }
  parentId.value = node.parentId
  showDialog.value = true
}

// 删除节点
const deleteNode = async (nodeId) => {
  if (!confirm('确定要删除这个节点吗？其子节点也会被删除。')) {
    return
  }

  try {
    await api.outlines.delete(nodeId)
    await loadOutlines()
  } catch (error) {
    console.error('删除节点失败:', error)
    alert('删除节点失败: ' + error.message)
  }
}

// 保存节点
const handleSave = async (nodeData) => {
  try {
    if (editingNode.value?.id) {
      await api.outlines.update(editingNode.value.id, nodeData)
    } else {
      await api.outlines.create({ 
        ...nodeData, 
        novelId: novelId.value,
        parentId: parentId.value
      })
    }
    await loadOutlines()
    closeDialog()
  } catch (error) {
    console.error('保存节点失败:', error)
    throw error
  }
}

// 关闭对话框
const closeDialog = () => {
  showDialog.value = false
  editingNode.value = null
  parentId.value = null
}

onMounted(() => {
  loadOutlines()
})
</script>
