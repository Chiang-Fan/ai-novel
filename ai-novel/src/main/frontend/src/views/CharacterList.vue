<template>
  <div class="character-list min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4 max-w-7xl">
      <!-- 页面头部 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">角色管理</h1>
            <p class="mt-1 text-sm text-gray-600">管理小说中的所有角色信息</p>
          </div>
          <button
            @click="showCreateDialog = true"
            class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium shadow-lg shadow-blue-500/30"
          >
            <span class="mr-2">+</span> 添加角色
          </button>
        </div>
        
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6">
          <div class="bg-white rounded-lg p-4 shadow">
            <div class="text-sm text-gray-600">总角色数</div>
            <div class="text-2xl font-bold text-gray-900">{{ characters.length }}</div>
          </div>
          <div class="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">主角</div>
            <div class="text-2xl font-bold">{{ getCharactersByType('PROTAGONIST').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-red-500 to-red-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">反派</div>
            <div class="text-2xl font-bold">{{ getCharactersByType('ANTAGONIST').length }}</div>
          </div>
          <div class="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-lg p-4 shadow">
            <div class="text-sm opacity-90">配角</div>
            <div class="text-2xl font-bold">{{ getCharactersByType('SUPPORTING').length }}</div>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="bg-white rounded-lg shadow p-4 mb-6">
        <div class="flex gap-4">
          <select
            v-model="filterType"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">全部类型</option>
            <option value="PROTAGONIST">主角</option>
            <option value="ANTAGONIST">反派</option>
            <option value="SUPPORTING">配角</option>
            <option value="MINOR">次要角色</option>
          </select>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索角色名称..."
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
      </div>

      <!-- 角色卡片列表 -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-gray-300 border-t-blue-600"></div>
        <p class="mt-4 text-gray-600">加载中...</p>
      </div>

      <div v-else-if="filteredCharacters.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
        <div class="text-gray-400 text-5xl mb-4">📝</div>
        <p class="text-gray-600">暂无角色，点击"添加角色"创建第一个角色</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="character in filteredCharacters"
          :key="character.id"
          class="bg-white rounded-lg shadow-md hover:shadow-xl transition-shadow p-6"
        >
          <!-- 角色头部 -->
          <div class="flex items-start justify-between mb-4">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <h3 class="text-xl font-bold text-gray-900">{{ character.name }}</h3>
                <span :class="getRoleTypeClass(character.roleType)" class="px-2 py-1 text-xs rounded-full">
                  {{ getRoleTypeName(character.roleType) }}
                </span>
              </div>
              <div class="text-sm text-gray-600 flex items-center gap-3">
                <span v-if="character.gender">{{ getGenderName(character.gender) }}</span>
                <span v-if="character.age">{{ character.age }}岁</span>
              </div>
            </div>
            <div class="flex gap-2">
              <button
                @click="editCharacter(character)"
                class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                title="编辑"
              >
                ✏️
              </button>
              <button
                @click="deleteCharacter(character.id)"
                class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                title="删除"
              >
                🗑️
              </button>
            </div>
          </div>

          <!-- 角色属性 -->
          <div class="space-y-3">
            <div v-if="character.personality">
              <div class="text-xs text-gray-500 font-medium mb-1">性格特征</div>
              <div class="text-sm text-gray-700 line-clamp-2">{{ character.personality }}</div>
            </div>
            <div v-if="character.background">
              <div class="text-xs text-gray-500 font-medium mb-1">背景故事</div>
              <div class="text-sm text-gray-700 line-clamp-2">{{ character.background }}</div>
            </div>
            <div v-if="character.motivation">
              <div class="text-xs text-gray-500 font-medium mb-1">动机目标</div>
              <div class="text-sm text-gray-700 line-clamp-2">{{ character.motivation }}</div>
            </div>
          </div>

          <!-- 查看详情按钮 -->
          <button
            @click="viewCharacter(character)"
            class="mt-4 w-full py-2 text-sm text-blue-600 hover:bg-blue-50 rounded-lg transition-colors font-medium"
          >
            查看完整信息
          </button>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <CharacterDialog
      v-if="showCreateDialog || showEditDialog"
      :character="editingCharacter"
      :novel-id="novelId"
      @close="closeDialogs"
      @save="handleSave"
    />

    <!-- 查看详情对话框 -->
    <CharacterDetailDialog
      v-if="showDetailDialog"
      :character="viewingCharacter"
      @close="showDetailDialog = false"
      @edit="editCharacter(viewingCharacter)"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import CharacterDialog from '../components/CharacterDialog.vue'
import CharacterDetailDialog from '../components/CharacterDetailDialog.vue'

const route = useRoute()
const novelId = ref(parseInt(route.params.id))

const characters = ref([])
const loading = ref(false)
const filterType = ref('')
const searchQuery = ref('')

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDialog = ref(false)
const editingCharacter = ref(null)
const viewingCharacter = ref(null)

// 计算属性
const filteredCharacters = computed(() => {
  let result = characters.value

  if (filterType.value) {
    result = result.filter(c => c.roleType === filterType.value)
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c => 
      c.name.toLowerCase().includes(query) ||
      c.personality?.toLowerCase().includes(query) ||
      c.background?.toLowerCase().includes(query)
    )
  }

  return result
})

const getCharactersByType = (type) => {
  return characters.value.filter(c => c.roleType === type)
}

// 加载角色列表
const loadCharacters = async () => {
  loading.value = true
  try {
    characters.value = await api.characters.list(novelId.value)
  } catch (error) {
    console.error('加载角色列表失败:', error)
    alert('加载角色列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 编辑角色
const editCharacter = (character) => {
  editingCharacter.value = { ...character }
  showEditDialog.value = true
  showDetailDialog.value = false
}

// 查看角色
const viewCharacter = (character) => {
  viewingCharacter.value = character
  showDetailDialog.value = true
}

// 删除角色
const deleteCharacter = async (id) => {
  if (!confirm('确定要删除这个角色吗？此操作不可撤销。')) {
    return
  }

  try {
    await api.characters.delete(id)
    await loadCharacters()
  } catch (error) {
    console.error('删除角色失败:', error)
    alert('删除角色失败: ' + error.message)
  }
}

// 保存角色
const handleSave = async (characterData) => {
  try {
    if (editingCharacter.value?.id) {
      await api.characters.update(editingCharacter.value.id, characterData)
    } else {
      await api.characters.create({ ...characterData, novelId: novelId.value })
    }
    await loadCharacters()
    closeDialogs()
  } catch (error) {
    console.error('保存角色失败:', error)
    throw error
  }
}

// 关闭对话框
const closeDialogs = () => {
  showCreateDialog.value = false
  showEditDialog.value = false
  editingCharacter.value = null
}

// 工具函数
const getRoleTypeName = (type) => {
  const names = {
    PROTAGONIST: '主角',
    ANTAGONIST: '反派',
    SUPPORTING: '配角',
    MINOR: '次要'
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

onMounted(() => {
  loadCharacters()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
