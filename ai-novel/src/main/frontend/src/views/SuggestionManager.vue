<template>
  <div class="suggestion-manager min-h-screen bg-gray-50 p-6">
    <!-- 顶部标题栏 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-4">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-lg transition">
          <span class="text-2xl">←</span>
        </button>
        <h1 class="text-3xl font-bold text-gray-800">🤖 智能推荐</h1>
      </div>
      <div class="flex items-center space-x-3">
        <button
          @click="showStatistics = !showStatistics"
          class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition">
          {{ showStatistics ? '隐藏' : '显示' }}统计
        </button>
        <button
          @click="generateSuggestions"
          :disabled="generating"
          class="px-4 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition disabled:opacity-50">
          {{ generating ? '生成中...' : '🤖 生成推荐' }}
        </button>
        <button
          @click="cleanupExpired"
          class="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition">
          🗑️ 清理过期
        </button>
      </div>
    </div>

    <!-- 统计面板 -->
    <SuggestionStatPanel
      v-if="showStatistics && statistics"
      :statistics="statistics"
      class="mb-6" />

    <!-- 筛选栏 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex flex-wrap items-center gap-4">
        <div class="flex items-center space-x-2">
          <label class="text-sm font-medium text-gray-700">类型：</label>
          <select
            v-model="filterType"
            @change="loadSuggestions"
            class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
            <option value="">全部</option>
            <option value="PLOT">情节</option>
            <option value="CHARACTER">角色</option>
            <option value="STYLE">文风</option>
            <option value="PACING">节奏</option>
            <option value="HOOK">伏笔</option>
            <option value="CONFLICT">冲突</option>
            <option value="THEME">主题</option>
            <option value="QUALITY">质量</option>
            <option value="WORLD_BUILDING">世界观</option>
            <option value="EMOTION">情感</option>
          </select>
        </div>

        <div class="flex items-center space-x-2">
          <label class="text-sm font-medium text-gray-700">优先级：</label>
          <select
            v-model="filterPriority"
            @change="applyFilter"
            class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
            <option value="">全部</option>
            <option value="high">高 (≥8)</option>
            <option value="medium">中 (5-7)</option>
            <option value="low">低 (<5)</option>
          </select>
        </div>

        <div class="flex items-center space-x-2 ml-auto">
          <span class="text-sm text-gray-600">共 {{ filteredSuggestions.length }} 条推荐</span>
        </div>
      </div>
    </div>

    <!-- 推荐列表 -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        <p class="mt-4 text-gray-600">加载推荐...</p>
      </div>
    </div>

    <div v-else-if="filteredSuggestions.length === 0" class="text-center py-20">
      <p class="text-gray-500 text-lg">😕 暂无推荐</p>
      <button
        @click="generateSuggestions"
        class="mt-4 px-6 py-3 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition">
        🤖 生成智能推荐
      </button>
    </div>

    <div v-else class="space-y-4">
      <SuggestionCard
        v-for="suggestion in filteredSuggestions"
        :key="suggestion.id"
        :suggestion="suggestion"
        @accept="acceptSuggestion"
        @reject="rejectSuggestion" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import SuggestionStatPanel from '../components/SuggestionStatPanel.vue'
import SuggestionCard from '../components/SuggestionCard.vue'

export default {
  name: 'SuggestionManager',
  components: {
    SuggestionStatPanel,
    SuggestionCard
  },
  setup() {
    const route = useRoute()
    const novelId = computed(() => parseInt(route.params.id))

    const loading = ref(false)
    const generating = ref(false)
    const showStatistics = ref(true)
    const suggestions = ref([])
    const statistics = ref(null)
    const filterType = ref('')
    const filterPriority = ref('')

    const filteredSuggestions = computed(() => {
      let result = suggestions.value

      if (filterPriority.value) {
        result = result.filter(s => {
          if (filterPriority.value === 'high') return s.priority >= 8
          if (filterPriority.value === 'medium') return s.priority >= 5 && s.priority < 8
          if (filterPriority.value === 'low') return s.priority < 5
          return true
        })
      }

      return result
    })

    const loadSuggestions = async () => {
      loading.value = true
      try {
        let response
        if (filterType.value) {
          response = await api.suggestions.getByType(novelId.value, filterType.value)
        } else {
          response = await api.suggestions.getActive(novelId.value)
        }
        suggestions.value = response.data
      } catch (error) {
        console.error('加载推荐失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadStatistics = async () => {
      try {
        const response = await api.suggestions.getStatistics(novelId.value)
        statistics.value = response.data
      } catch (error) {
        console.error('加载统计失败:', error)
      }
    }

    const generateSuggestions = async () => {
      generating.value = true
      try {
        await api.suggestions.generateForNovel(novelId.value)
        alert('✅ 推荐生成成功！')
        await Promise.all([loadSuggestions(), loadStatistics()])
      } catch (error) {
        console.error('生成推荐失败:', error)
        alert('❌ 生成失败：' + (error.response?.data?.message || error.message))
      } finally {
        generating.value = false
      }
    }

    const acceptSuggestion = async (id, feedback) => {
      try {
        await api.suggestions.accept(id, feedback)
        alert('✅ 已采纳推荐')
        await Promise.all([loadSuggestions(), loadStatistics()])
      } catch (error) {
        console.error('采纳推荐失败:', error)
        alert('❌ 操作失败：' + (error.response?.data?.message || error.message))
      }
    }

    const rejectSuggestion = async (id, reason) => {
      try {
        await api.suggestions.reject(id, reason)
        alert('✅ 已拒绝推荐')
        await Promise.all([loadSuggestions(), loadStatistics()])
      } catch (error) {
        console.error('拒绝推荐失败:', error)
        alert('❌ 操作失败：' + (error.response?.data?.message || error.message))
      }
    }

    const cleanupExpired = async () => {
      try {
        const response = await api.suggestions.cleanup(novelId.value)
        alert(`✅ 已清理 ${response.data} 条过期推荐`)
        await Promise.all([loadSuggestions(), loadStatistics()])
      } catch (error) {
        console.error('清理失败:', error)
      }
    }

    const applyFilter = () => {
      // 触发响应式更新
    }

    onMounted(async () => {
      await Promise.all([loadSuggestions(), loadStatistics()])
    })

    return {
      loading,
      generating,
      showStatistics,
      suggestions,
      statistics,
      filterType,
      filterPriority,
      filteredSuggestions,
      loadSuggestions,
      generateSuggestions,
      acceptSuggestion,
      rejectSuggestion,
      cleanupExpired,
      applyFilter
    }
  }
}
</script>
