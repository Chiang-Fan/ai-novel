<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50">
    <!-- 头部导航 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <button 
              @click="goBack"
              class="text-gray-500 hover:text-gray-700 transition"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <h1 class="text-2xl font-bold text-gray-800">
              📚 版本管理
            </h1>
          </div>
          <button
            @click="createVersion"
            :disabled="!chapterContent"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition shadow-sm"
          >
            💾 创建版本
          </button>
        </div>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="max-w-7xl mx-auto px-6 py-6">
      <div class="grid grid-cols-12 gap-6">
        <!-- 左侧：版本列表 -->
        <div class="col-span-3">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-4 border-b border-gray-200">
              <h3 class="font-semibold text-gray-900">版本历史</h3>
              <p class="text-xs text-gray-500 mt-1">共 {{ totalVersions }} 个版本</p>
            </div>
            <div class="overflow-y-auto" style="max-height: calc(100vh - 250px);">
              <div v-if="versions.length === 0" class="p-6 text-center text-gray-500">
                <p>暂无版本</p>
              </div>
              <div
                v-for="version in versions"
                :key="version.id"
                @click="selectVersion(version)"
                :class="[
                  'p-4 border-b border-gray-100 cursor-pointer transition',
                  selectedVersion?.id === version.id
                    ? 'bg-blue-50 border-l-4 border-l-blue-500'
                    : 'hover:bg-gray-50'
                ]"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center space-x-2">
                      <h4 class="text-sm font-medium text-gray-900">
                        v{{ version.versionNumber }}
                      </h4>
                      <span
                        v-if="version.isCurrent"
                        class="px-2 py-0.5 text-xs bg-green-100 text-green-700 rounded-full"
                      >
                        当前
                      </span>
                    </div>
                    <p v-if="version.versionTag" class="text-xs text-blue-600 mt-1 truncate">
                      {{ version.versionTag }}
                    </p>
                    <p class="text-xs text-gray-500 mt-1">
                      {{ version.wordCount }} 字
                    </p>
                    <p class="text-xs text-gray-400 mt-1">
                      {{ formatTime(version.createdAt) }}
                    </p>
                  </div>
                  <button
                    v-if="!version.isCurrent"
                    @click.stop="showVersionMenu(version)"
                    class="text-gray-400 hover:text-gray-600"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：版本详情/对比 -->
        <div class="col-span-9">
          <!-- Tab 切换 -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 mb-4">
            <div class="border-b border-gray-200">
              <nav class="flex space-x-8 px-6" aria-label="Tabs">
                <button
                  v-for="tab in tabs"
                  :key="tab.name"
                  @click="activeTab = tab.name"
                  :class="[
                    'py-4 px-1 border-b-2 font-medium text-sm transition',
                    activeTab === tab.name
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  ]"
                >
                  {{ tab.label }}
                </button>
              </nav>
            </div>
          </div>

          <!-- 版本详情 Tab -->
          <div v-show="activeTab === 'detail'" class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div v-if="!selectedVersion" class="p-12 text-center text-gray-500">
              <svg class="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p>选择一个版本查看详情</p>
            </div>
            <div v-else class="p-6">
              <!-- 版本信息 -->
              <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6 mb-6">
                <div class="flex items-start justify-between mb-4">
                  <div>
                    <h3 class="text-xl font-bold text-gray-900">
                      版本 {{ selectedVersion.versionNumber }}
                      <span v-if="selectedVersion.isCurrent" class="ml-2 px-2 py-1 text-sm bg-green-100 text-green-700 rounded-full">当前版本</span>
                    </h3>
                    <p v-if="selectedVersion.versionTag" class="text-blue-600 mt-1">
                      {{ selectedVersion.versionTag }}
                    </p>
                  </div>
                  <div class="flex space-x-2">
                    <button
                      v-if="!selectedVersion.isCurrent"
                      @click="rollbackToVersion(selectedVersion.id)"
                      class="px-3 py-1 text-sm bg-orange-600 text-white rounded hover:bg-orange-700 transition"
                    >
                      ⏪ 回滚
                    </button>
                    <button
                      @click="editVersionTag"
                      class="px-3 py-1 text-sm bg-gray-600 text-white rounded hover:bg-gray-700 transition"
                    >
                      ✏️ 编辑标签
                    </button>
                  </div>
                </div>
                <div class="grid grid-cols-4 gap-4 text-sm">
                  <div>
                    <span class="text-gray-600">字数:</span>
                    <span class="ml-2 font-medium">{{ selectedVersion.wordCount }}</span>
                  </div>
                  <div>
                    <span class="text-gray-600">类型:</span>
                    <span class="ml-2 font-medium">{{ formatCreatedType(selectedVersion.createdType) }}</span>
                  </div>
                  <div>
                    <span class="text-gray-600">创建者:</span>
                    <span class="ml-2 font-medium">{{ selectedVersion.createdBy }}</span>
                  </div>
                  <div>
                    <span class="text-gray-600">创建时间:</span>
                    <span class="ml-2 font-medium">{{ formatTime(selectedVersion.createdAt) }}</span>
                  </div>
                </div>
                <div v-if="selectedVersion.versionNote" class="mt-4 p-3 bg-white rounded border border-gray-200">
                  <p class="text-sm text-gray-700">{{ selectedVersion.versionNote }}</p>
                </div>
              </div>

              <!-- 版本内容 -->
              <div class="border border-gray-200 rounded-lg p-4">
                <h4 class="font-semibold text-gray-900 mb-3">版本内容</h4>
                <div class="prose max-w-none">
                  <pre class="whitespace-pre-wrap font-serif text-sm leading-relaxed text-gray-800">{{ versionContent }}</pre>
                </div>
              </div>
            </div>
          </div>

          <!-- 版本对比 Tab -->
          <div v-show="activeTab === 'compare'" class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-6">
              <!-- 选择对比版本 -->
              <div class="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">源版本</label>
                  <select
                    v-model="compareFrom"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option :value="null">选择版本...</option>
                    <option v-for="v in versions" :key="v.id" :value="v.id">
                      v{{ v.versionNumber }} - {{ v.versionTag || '未命名' }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">目标版本</label>
                  <select
                    v-model="compareTo"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option :value="null">选择版本...</option>
                    <option v-for="v in versions" :key="v.id" :value="v.id">
                      v{{ v.versionNumber }} - {{ v.versionTag || '未命名' }}
                    </option>
                  </select>
                </div>
              </div>

              <button
                @click="performCompare"
                :disabled="!compareFrom || !compareTo || comparing"
                class="w-full py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition mb-6"
              >
                {{ comparing ? '对比中...' : '🔍 开始对比' }}
              </button>

              <!-- 对比结果 -->
              <div v-if="compareResult">
                <!-- 统计信息 -->
                <div class="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg p-6 mb-6">
                  <h4 class="font-semibold text-gray-900 mb-4">差异统计</h4>
                  <div class="grid grid-cols-5 gap-4 text-center">
                    <div>
                      <div class="text-2xl font-bold text-green-600">+{{ compareResult.statistics.addedCount }}</div>
                      <div class="text-sm text-gray-600">新增行</div>
                    </div>
                    <div>
                      <div class="text-2xl font-bold text-red-600">-{{ compareResult.statistics.deletedCount }}</div>
                      <div class="text-sm text-gray-600">删除行</div>
                    </div>
                    <div>
                      <div class="text-2xl font-bold text-yellow-600">~{{ compareResult.statistics.modifiedCount }}</div>
                      <div class="text-sm text-gray-600">修改行</div>
                    </div>
                    <div>
                      <div class="text-2xl font-bold text-gray-600">={{ compareResult.statistics.unchangedCount }}</div>
                      <div class="text-sm text-gray-600">未变化</div>
                    </div>
                    <div>
                      <div class="text-2xl font-bold text-blue-600">{{ compareResult.statistics.wordDifference > 0 ? '+' : '' }}{{ compareResult.statistics.wordDifference }}</div>
                      <div class="text-sm text-gray-600">字数差</div>
                    </div>
                  </div>
                </div>

                <!-- 差异内容 -->
                <div class="border border-gray-200 rounded-lg overflow-hidden">
                  <div class="bg-gray-50 px-4 py-2 border-b border-gray-200">
                    <h4 class="font-semibold text-gray-900">详细差异</h4>
                  </div>
                  <div class="max-h-96 overflow-y-auto">
                    <div
                      v-for="(diff, index) in compareResult.diffs"
                      :key="index"
                      :class="[
                        'px-4 py-2 font-mono text-sm',
                        diff.type === 'INSERT' ? 'bg-green-50 text-green-800' : '',
                        diff.type === 'DELETE' ? 'bg-red-50 text-red-800' : '',
                        diff.type === 'EQUAL' ? 'bg-white text-gray-700' : ''
                      ]"
                    >
                      <span v-if="diff.type === 'INSERT'" class="mr-2 font-bold text-green-600">+</span>
                      <span v-if="diff.type === 'DELETE'" class="mr-2 font-bold text-red-600">-</span>
                      <span v-if="diff.type === 'EQUAL'" class="mr-2 text-gray-400"> </span>
                      <span>{{ diff.content }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计分析 Tab -->
          <div v-show="activeTab === 'statistics'" class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="p-6">
              <button
                @click="loadStatistics"
                :disabled="loadingStats"
                class="mb-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                {{ loadingStats ? '加载中...' : '📊 刷新统计' }}
              </button>

              <div v-if="statistics" class="space-y-6">
                <div class="grid grid-cols-3 gap-6">
                  <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-6">
                    <div class="text-3xl font-bold text-blue-600 mb-2">{{ statistics.totalVersions }}</div>
                    <div class="text-sm text-gray-600">总版本数</div>
                  </div>
                  <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-lg p-6">
                    <div class="text-3xl font-bold text-green-600 mb-2">{{ statistics.currentVersionNumber }}</div>
                    <div class="text-sm text-gray-600">当前版本号</div>
                  </div>
                  <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg p-6">
                    <div class="text-3xl font-bold text-purple-600 mb-2">{{ statistics.totalEdits }}</div>
                    <div class="text-sm text-gray-600">总编辑次数</div>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-6">
                  <div class="border border-gray-200 rounded-lg p-6">
                    <h4 class="font-semibold text-gray-900 mb-4">字数变化</h4>
                    <div class="space-y-2">
                      <div class="flex justify-between">
                        <span class="text-gray-600">总增加:</span>
                        <span class="font-medium text-green-600">+{{ statistics.totalWordsAdded || 0 }} 字</span>
                      </div>
                      <div class="flex justify-between">
                        <span class="text-gray-600">总删除:</span>
                        <span class="font-medium text-red-600">-{{ statistics.totalWordsDeleted || 0 }} 字</span>
                      </div>
                      <div class="flex justify-between pt-2 border-t border-gray-200">
                        <span class="text-gray-600">平均编辑:</span>
                        <span class="font-medium">{{ (statistics.averageEditSize || 0).toFixed(0) }} 字</span>
                      </div>
                    </div>
                  </div>
                  <div class="border border-gray-200 rounded-lg p-6">
                    <h4 class="font-semibold text-gray-900 mb-4">编辑习惯</h4>
                    <div class="text-center py-4">
                      <div class="text-2xl font-bold text-blue-600 mb-2">{{ statistics.mostActiveEditTime || '暂无数据' }}</div>
                      <div class="text-sm text-gray-600">最活跃编辑时段</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建版本对话框 -->
    <div
      v-if="showCreateDialog"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showCreateDialog = false"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <h3 class="text-xl font-bold text-gray-900 mb-4">创建新版本</h3>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">版本标签</label>
            <input
              v-model="newVersion.tag"
              type="text"
              placeholder="例如: 优化第二幕冲突"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">版本备注</label>
            <textarea
              v-model="newVersion.note"
              placeholder="记录本次修改的主要内容..."
              rows="3"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            ></textarea>
          </div>
        </div>

        <div class="flex justify-end space-x-3 mt-6">
          <button
            @click="showCreateDialog = false"
            class="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
          >
            取消
          </button>
          <button
            @click="confirmCreateVersion"
            :disabled="creating"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            {{ creating ? '创建中...' : '创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'

const route = useRoute()
const router = useRouter()

// 路由参数
const chapterId = ref(route.params.chapterId)

// Tab 定义
const tabs = [
  { name: 'detail', label: '📄 版本详情' },
  { name: 'compare', label: '🔍 版本对比' },
  { name: 'statistics', label: '📊 统计分析' }
]

// 状态
const activeTab = ref('detail')
const versions = ref([])
const selectedVersion = ref(null)
const versionContent = ref('')
const totalVersions = ref(0)
const chapterContent = ref('')

// 对比相关
const compareFrom = ref(null)
const compareTo = ref(null)
const comparing = ref(false)
const compareResult = ref(null)

// 统计相关
const statistics = ref(null)
const loadingStats = ref(false)

// 创建版本
const showCreateDialog = ref(false)
const creating = ref(false)
const newVersion = reactive({
  tag: '',
  note: ''
})

// 加载版本列表
const loadVersions = async () => {
  try {
    const response = await api.get(`/versions/chapter/${chapterId.value}?page=0&size=50`)
    if (response.data.code === 0) {
      versions.value = response.data.data.content || []
      totalVersions.value = response.data.data.totalElements || 0
      
      // 自动选择当前版本
      const current = versions.value.find(v => v.isCurrent)
      if (current && !selectedVersion.value) {
        await selectVersion(current)
      }
    }
  } catch (error) {
    console.error('加载版本列表失败:', error)
  }
}

// 选择版本
const selectVersion = async (version) => {
  selectedVersion.value = version
  await loadVersionContent(version.id)
}

// 加载版本内容
const loadVersionContent = async (versionId) => {
  try {
    const response = await api.get(`/versions/${versionId}`)
    if (response.data.code === 0) {
      versionContent.value = response.data.data.content || ''
    }
  } catch (error) {
    console.error('加载版本内容失败:', error)
  }
}

// 加载章节内容（用于创建版本）
const loadChapterContent = async () => {
  try {
    const response = await api.get(`/chapters/${chapterId.value}`)
    if (response.data.code === 0) {
      chapterContent.value = response.data.data.content || ''
    }
  } catch (error) {
    console.error('加载章节内容失败:', error)
  }
}

// 创建版本
const createVersion = () => {
  newVersion.tag = ''
  newVersion.note = ''
  showCreateDialog.value = true
}

// 确认创建版本
const confirmCreateVersion = async () => {
  creating.value = true
  try {
    const response = await api.post('/versions/create', {
      chapterId: parseInt(chapterId.value),
      content: chapterContent.value,
      versionTag: newVersion.tag,
      versionNote: newVersion.note,
      createdType: 'MANUAL'
    })

    if (response.data.code === 0) {
      showCreateDialog.value = false
      await loadVersions()
      alert('版本创建成功！')
    } else {
      alert('创建失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('创建版本失败:', error)
    alert('创建版本失败')
  } finally {
    creating.value = false
  }
}

// 回滚到版本
const rollbackToVersion = async (versionId) => {
  if (!confirm('确定要回滚到这个版本吗？这将创建一个新版本。')) return

  try {
    const response = await api.post(`/versions/${versionId}/rollback`)
    if (response.data.code === 0) {
      await loadVersions()
      alert('回滚成功！')
    } else {
      alert('回滚失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('回滚失败:', error)
    alert('回滚失败')
  }
}

// 编辑版本标签
const editVersionTag = () => {
  const tag = prompt('输入新标签:', selectedVersion.value.versionTag || '')
  if (tag === null) return

  const note = prompt('输入备注:', selectedVersion.value.versionNote || '')
  if (note === null) return

  updateVersionTag(selectedVersion.value.id, tag, note)
}

// 更新版本标签
const updateVersionTag = async (versionId, tag, note) => {
  try {
    const response = await api.put(`/versions/${versionId}/tag`, {
      versionTag: tag,
      versionNote: note
    })

    if (response.data.code === 0) {
      await loadVersions()
      await selectVersion(response.data.data)
      alert('更新成功！')
    }
  } catch (error) {
    console.error('更新标签失败:', error)
    alert('更新失败')
  }
}

// 执行对比
const performCompare = async () => {
  if (!compareFrom.value || !compareTo.value) return

  comparing.value = true
  try {
    const response = await api.post('/versions/compare', {
      versionIdFrom: compareFrom.value,
      versionIdTo: compareTo.value
    })

    if (response.data.code === 0) {
      compareResult.value = response.data.data
    } else {
      alert('对比失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('对比失败:', error)
    alert('对比失败')
  } finally {
    comparing.value = false
  }
}

// 加载统计
const loadStatistics = async () => {
  loadingStats.value = true
  try {
    const response = await api.get(`/versions/statistics/chapter/${chapterId.value}`)
    if (response.data.code === 0) {
      statistics.value = response.data.data
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  } finally {
    loadingStats.value = false
  }
}

// 显示版本菜单
const showVersionMenu = (version) => {
  const actions = ['回滚到此版本', '编辑标签', '删除版本']
  const choice = prompt(`选择操作:\n1. ${actions[0]}\n2. ${actions[1]}\n3. ${actions[2]}`)
  
  if (choice === '1') {
    rollbackToVersion(version.id)
  } else if (choice === '2') {
    editVersionTag()
  } else if (choice === '3') {
    deleteVersion(version.id)
  }
}

// 删除版本
const deleteVersion = async (versionId) => {
  if (!confirm('确定要删除这个版本吗？')) return

  try {
    const response = await api.delete(`/versions/${versionId}`)
    if (response.data.code === 0) {
      await loadVersions()
      alert('删除成功！')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败')
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化创建类型
const formatCreatedType = (type) => {
  const types = {
    MANUAL: '手动创建',
    AUTO_SAVE: '自动保存',
    AUTO_SNAPSHOT: '自动快照',
    ROLLBACK: '版本回滚'
  }
  return types[type] || type
}

// 返回
const goBack = () => {
  router.back()
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadVersions(),
    loadChapterContent()
  ])
})
</script>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
