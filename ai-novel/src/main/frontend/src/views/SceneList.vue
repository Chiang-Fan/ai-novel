<template>
  <div class="scene-list min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4 max-w-7xl">
      <!-- 页面头部 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">场景管理</h1>
            <p class="mt-1 text-sm text-gray-600">管理小说中的所有场景和环境设定</p>
          </div>
          <button
            @click="showCreateDialog = true"
            class="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium shadow-lg shadow-green-500/30"
          >
            <span class="mr-2">+</span> 添加场景
          </button>
        </div>
        
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6">
          <div class="bg-white rounded-lg p-4 shadow">
            <div class="text-sm text-gray-600">总场景数</div>
            <div class="text-2xl font-bold text-gray-900">{{ scenes.length }}</div>
          </div>
          <div class="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">地点</div>
            <div class="text-2xl font-bold">{{ getScenesByType('LOCATION').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-amber-500 to-amber-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">事件</div>
            <div class="text-2xl font-bold">{{ getScenesByType('EVENT').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-indigo-500 to-indigo-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">时间段</div>
            <div class="text-2xl font-bold">{{ getScenesByType('TIME_PERIOD').length }}</div>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="bg-white rounded-lg shadow p-4 mb-6">
        <div class="flex gap-4">
          <select
            v-model="filterType"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
          >
            <option value="">全部类型</option>
            <option value="LOCATION">地点</option>
            <option value="EVENT">事件</option>
            <option value="TIME_PERIOD">时间段</option>
          </select>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索场景名称、描述..."
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
          />
        </div>
      </div>

      <!-- 场景卡片列表 -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-gray-300 border-t-green-600"></div>
        <p class="mt-4 text-gray-600">加载中...</p>
      </div>

      <div v-else-if="filteredScenes.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
        <div class="text-gray-400 text-5xl mb-4">🎬</div>
        <p class="text-gray-600">暂无场景，点击"添加场景"创建第一个场景</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="scene in filteredScenes"
          :key="scene.id"
          class="bg-white rounded-lg shadow-md hover:shadow-xl transition-shadow overflow-hidden"
        >
          <!-- 场景头部 - 带颜色条 -->
          <div :class="getSceneTypeHeaderClass(scene.sceneType)" class="h-2"></div>
          
          <div class="p-6">
            <!-- 场景标题 -->
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-2xl">{{ getSceneTypeIcon(scene.sceneType) }}</span>
                  <h3 class="text-xl font-bold text-gray-900">{{ scene.name }}</h3>
                </div>
                <span :class="getSceneTypeClass(scene.sceneType)" class="px-2 py-1 text-xs rounded-full">
                  {{ getSceneTypeName(scene.sceneType) }}
                </span>
              </div>
              <div class="flex gap-2">
                <button
                  @click="editScene(scene)"
                  class="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                  title="编辑"
                >
                  ✏️
                </button>
                <button
                  @click="deleteScene(scene.id)"
                  class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  title="删除"
                >
                  🗑️
                </button>
              </div>
            </div>

            <!-- 场景属性 -->
            <div class="space-y-3">
              <div v-if="scene.description">
                <div class="text-xs text-gray-500 font-medium mb-1">描述</div>
                <div class="text-sm text-gray-700 line-clamp-2">{{ scene.description }}</div>
              </div>
              
              <div class="grid grid-cols-2 gap-3">
                <div v-if="scene.location">
                  <div class="text-xs text-gray-500 font-medium mb-1">📍 地点</div>
                  <div class="text-sm text-gray-700">{{ scene.location }}</div>
                </div>
                <div v-if="scene.timePeriod">
                  <div class="text-xs text-gray-500 font-medium mb-1">🕐 时间</div>
                  <div class="text-sm text-gray-700">{{ scene.timePeriod }}</div>
                </div>
                <div v-if="scene.weather">
                  <div class="text-xs text-gray-500 font-medium mb-1">🌤️ 天气</div>
                  <div class="text-sm text-gray-700">{{ scene.weather }}</div>
                </div>
                <div v-if="scene.atmosphere">
                  <div class="text-xs text-gray-500 font-medium mb-1">🎭 氛围</div>
                  <div class="text-sm text-gray-700 line-clamp-1">{{ scene.atmosphere }}</div>
                </div>
              </div>
            </div>

            <!-- 查看详情按钮 -->
            <button
              @click="viewScene(scene)"
              class="mt-4 w-full py-2 text-sm text-green-600 hover:bg-green-50 rounded-lg transition-colors font-medium"
            >
              📊 场景管理面板
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <SceneDialog
      v-if="showCreateDialog || showEditDialog"
      :scene="editingScene"
      :novel-id="novelId"
      @close="closeDialogs"
      @save="handleSave"
    />

    <!-- 查看详情对话框 -->
    <SceneDetailDialog
      v-if="showDetailDialog"
      :scene="viewingScene"
      @close="showDetailDialog = false"
      @edit="editScene(viewingScene)"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import SceneDialog from '../components/SceneDialog.vue'
import SceneDetailDialog from '../components/SceneDetailDialog.vue'

const route = useRoute()
const novelId = ref(parseInt(route.params.id))

const scenes = ref([])
const loading = ref(false)
const filterType = ref('')
const searchQuery = ref('')

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDialog = ref(false)
const editingScene = ref(null)
const viewingScene = ref(null)

// 计算属性
const filteredScenes = computed(() => {
  let result = scenes.value

  if (filterType.value) {
    result = result.filter(s => s.sceneType === filterType.value)
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(s => 
      s.name.toLowerCase().includes(query) ||
      s.description?.toLowerCase().includes(query) ||
      s.location?.toLowerCase().includes(query)
    )
  }

  return result
})

const getScenesByType = (type) => {
  return scenes.value.filter(s => s.sceneType === type)
}

// 加载场景列表
const loadScenes = async () => {
  loading.value = true
  try {
    scenes.value = await api.scenes.list(novelId.value)
  } catch (error) {
    console.error('加载场景列表失败:', error)
    alert('加载场景列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 编辑场景
const editScene = (scene) => {
  editingScene.value = { ...scene }
  showEditDialog.value = true
  showDetailDialog.value = false
}

// 查看场景
const viewScene = (scene) => {
  // 跳转到场景管理面板
  router.push(`/scene/${scene.id}/management`)
}

// 删除场景
const deleteScene = async (id) => {
  if (!confirm('确定要删除这个场景吗？此操作不可撤销。')) {
    return
  }

  try {
    await api.scenes.delete(id)
    await loadScenes()
  } catch (error) {
    console.error('删除场景失败:', error)
    alert('删除场景失败: ' + error.message)
  }
}

// 保存场景
const handleSave = async (sceneData) => {
  try {
    if (editingScene.value?.id) {
      await api.scenes.update(editingScene.value.id, sceneData)
    } else {
      await api.scenes.create({ ...sceneData, novelId: novelId.value })
    }
    await loadScenes()
    closeDialogs()
  } catch (error) {
    console.error('保存场景失败:', error)
    throw error
  }
}

// 关闭对话框
const closeDialogs = () => {
  showCreateDialog.value = false
  showEditDialog.value = false
  editingScene.value = null
}

// 工具函数
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

const getSceneTypeHeaderClass = (type) => {
  const classes = {
    LOCATION: 'bg-gradient-to-r from-blue-500 to-blue-600',
    EVENT: 'bg-gradient-to-r from-amber-500 to-amber-600',
    TIME_PERIOD: 'bg-gradient-to-r from-indigo-500 to-indigo-600'
  }
  return classes[type] || 'bg-gray-400'
}

onMounted(() => {
  loadScenes()
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
