<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-blue-50 p-6">
    <!-- 头部 -->
    <div class="max-w-7xl mx-auto mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-4">
          <router-link :to="`/novel/${novelId}`" class="text-gray-500 hover:text-gray-700">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </router-link>
          <div>
            <h1 class="text-3xl font-bold text-gray-800">🎭 伏笔管理</h1>
            <p class="text-sm text-gray-500 mt-1">管理故事伏笔的埋设、触发和解决</p>
          </div>
        </div>

        <div class="flex space-x-3">
          <button
            @click="viewMode = 'kanban'"
            :class="[
              'px-4 py-2 rounded-lg font-medium transition',
              viewMode === 'kanban'
                ? 'bg-purple-600 text-white shadow-lg'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            ]">
            📋 看板视图
          </button>
          <button
            @click="viewMode = 'timeline'"
            :class="[
              'px-4 py-2 rounded-lg font-medium transition',
              viewMode === 'timeline'
                ? 'bg-purple-600 text-white shadow-lg'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            ]">
            📈 时间线视图
          </button>
          <button
            @click="showCreateDialog = true"
            class="px-4 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg font-medium hover:shadow-lg transition">
            ➕ 新建伏笔
          </button>
        </div>
      </div>
    </div>

    <!-- 统计面板 -->
    <PlotHookStatPanel
      v-if="statistics"
      :statistics="statistics"
      :loading="statsLoading"
      @detect="detectHooks"
      class="max-w-7xl mx-auto mb-6"
    />

    <!-- 内容区域 -->
    <div class="max-w-7xl mx-auto">
      <!-- 看板视图 -->
      <PlotHookKanban
        v-if="viewMode === 'kanban'"
        :hooks="hooks"
        :loading="loading"
        @edit="editHook"
        @delete="deleteHook"
        @hint="hintHook"
        @trigger="showTriggerDialog"
        @resolve="showResolveDialog"
        @refresh="loadHooks"
      />

      <!-- 时间线视图 -->
      <PlotHookTimeline
        v-else
        :hooks="hooks"
        :loading="loading"
        @edit="editHook"
        @delete="deleteHook"
        @hint="hintHook"
        @trigger="showTriggerDialog"
        @resolve="showResolveDialog"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <PlotHookDialog
      v-model="showCreateDialog"
      :hook="editingHook"
      :novel-id="novelId"
      @save="saveHook"
    />

    <!-- 触发对话框 -->
    <div v-if="triggerDialogVisible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full">
        <h3 class="text-xl font-bold mb-4">触发伏笔</h3>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">触发章节号</label>
          <input
            v-model.number="triggerChapter"
            type="number"
            min="1"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="输入章节号"
          />
        </div>
        <div class="flex justify-end space-x-3">
          <button
            @click="triggerDialogVisible = false"
            class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300">
            取消
          </button>
          <button
            @click="confirmTrigger"
            :disabled="!triggerChapter"
            class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50">
            确认触发
          </button>
        </div>
      </div>
    </div>

    <!-- 解决对话框 -->
    <div v-if="resolveDialogVisible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full">
        <h3 class="text-xl font-bold mb-4">解决伏笔</h3>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">解决章节号</label>
          <input
            v-model.number="resolveChapter"
            type="number"
            min="1"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="输入章节号"
          />
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">解决说明</label>
          <textarea
            v-model="resolveNote"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="描述如何解决这个伏笔..."
          ></textarea>
        </div>
        <div class="flex justify-end space-x-3">
          <button
            @click="resolveDialogVisible = false"
            class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300">
            取消
          </button>
          <button
            @click="confirmResolve"
            :disabled="!resolveChapter"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50">
            确认解决
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api'
import PlotHookStatPanel from '../components/PlotHookStatPanel.vue'
import PlotHookKanban from '../components/PlotHookKanban.vue'
import PlotHookTimeline from '../components/PlotHookTimeline.vue'
import PlotHookDialog from '../components/PlotHookDialog.vue'

export default {
  name: 'PlotHookManager',
  components: {
    PlotHookStatPanel,
    PlotHookKanban,
    PlotHookTimeline,
    PlotHookDialog
  },
  data() {
    return {
      novelId: null,
      hooks: [],
      statistics: null,
      loading: false,
      statsLoading: false,
      viewMode: 'kanban', // 'kanban' or 'timeline'
      showCreateDialog: false,
      editingHook: null,
      triggerDialogVisible: false,
      triggerHookId: null,
      triggerChapter: null,
      resolveDialogVisible: false,
      resolveHookId: null,
      resolveChapter: null,
      resolveNote: ''
    }
  },
  created() {
    this.novelId = parseInt(this.$route.params.id)
    this.loadHooks()
    this.loadStatistics()
  },
  methods: {
    async loadHooks() {
      this.loading = true
      try {
        this.hooks = await api.plotHooks.getByNovelId(this.novelId)
      } catch (error) {
        console.error('加载伏笔失败:', error)
        alert('加载伏笔失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    async loadStatistics() {
      this.statsLoading = true
      try {
        this.statistics = await api.plotHooks.getStatistics(this.novelId)
      } catch (error) {
        console.error('加载统计失败:', error)
      } finally {
        this.statsLoading = false
      }
    },

    async detectHooks() {
      if (!confirm('确定要自动检测当前章节的伏笔吗？\n这将调用 AI 分析章节内容。')) {
        return
      }

      this.statsLoading = true
      try {
        // 获取最新章节号
        const chapters = await api.chapters.list(this.novelId)
        if (chapters.length === 0) {
          alert('还没有章节，无法检测伏笔')
          return
        }
        const latestChapter = Math.max(...chapters.map(c => c.chapterNumber))
        
        const detected = await api.plotHooks.detectFromChapter(this.novelId, latestChapter)
        alert(`成功检测到 ${detected.length} 个伏笔！`)
        await this.loadHooks()
        await this.loadStatistics()
      } catch (error) {
        console.error('自动检测失败:', error)
        alert('自动检测失败: ' + error.message)
      } finally {
        this.statsLoading = false
      }
    },

    editHook(hook) {
      this.editingHook = { ...hook }
      this.showCreateDialog = true
    },

    async deleteHook(hookId) {
      if (!confirm('确定要删除这个伏笔吗？')) {
        return
      }

      try {
        await api.plotHooks.delete(hookId)
        await this.loadHooks()
        await this.loadStatistics()
      } catch (error) {
        console.error('删除伏笔失败:', error)
        alert('删除伏笔失败: ' + error.message)
      }
    },

    async hintHook(hookId) {
      try {
        await api.plotHooks.hint(hookId)
        await this.loadHooks()
        await this.loadStatistics()
      } catch (error) {
        console.error('标记铺垫失败:', error)
        alert('标记铺垫失败: ' + error.message)
      }
    },

    showTriggerDialog(hookId) {
      this.triggerHookId = hookId
      this.triggerChapter = null
      this.triggerDialogVisible = true
    },

    async confirmTrigger() {
      try {
        await api.plotHooks.trigger(this.triggerHookId, this.triggerChapter)
        this.triggerDialogVisible = false
        await this.loadHooks()
        await this.loadStatistics()
      } catch (error) {
        console.error('触发伏笔失败:', error)
        alert('触发伏笔失败: ' + error.message)
      }
    },

    showResolveDialog(hookId) {
      this.resolveHookId = hookId
      this.resolveChapter = null
      this.resolveNote = ''
      this.resolveDialogVisible = true
    },

    async confirmResolve() {
      try {
        await api.plotHooks.resolve(this.resolveHookId, this.resolveChapter, this.resolveNote)
        this.resolveDialogVisible = false
        await this.loadHooks()
        await this.loadStatistics()
      } catch (error) {
        console.error('解决伏笔失败:', error)
        alert('解决伏笔失败: ' + error.message)
      }
    },

    async saveHook(hookData) {
      try {
        if (hookData.id) {
          await api.plotHooks.update(hookData.id, hookData)
        } else {
          await api.plotHooks.create(hookData)
        }
        this.showCreateDialog = false
        this.editingHook = null
        await this.loadHooks()
        await this.loadStatistics()
      } catch (error) {
        console.error('保存伏笔失败:', error)
        alert('保存伏笔失败: ' + error.message)
      }
    }
  }
}
</script>
