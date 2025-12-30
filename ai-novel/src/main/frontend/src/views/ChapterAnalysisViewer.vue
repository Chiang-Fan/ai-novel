<template>
  <div class="chapter-analysis-viewer min-h-screen bg-gray-50 p-6">
    <!-- 顶部标题栏 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-4">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-lg transition">
          <span class="text-2xl">←</span>
        </button>
        <h1 class="text-3xl font-bold text-gray-800">📊 章节分析</h1>
      </div>
      <div class="flex items-center space-x-3">
        <button
          @click="showStatistics = !showStatistics"
          class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition">
          {{ showStatistics ? '隐藏' : '显示' }}统计
        </button>
        <button
          @click="refreshAnalyses"
          :disabled="loading"
          class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition disabled:opacity-50">
          🔄 刷新
        </button>
      </div>
    </div>

    <!-- 统计面板 -->
    <ChapterAnalysisStatPanel
      v-if="showStatistics && statistics"
      :statistics="statistics"
      class="mb-6" />

    <!-- 章节选择 -->
    <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
      <div class="flex items-center space-x-4">
        <label class="text-gray-700 font-medium">选择章节：</label>
        <select
          v-model="selectedChapterId"
          @change="onChapterChange"
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          <option value="">-- 请选择章节 --</option>
          <option v-for="chapter in chapters" :key="chapter.id" :value="chapter.id">
            第 {{ chapter.chapterNumber }} 章 - {{ chapter.title }}
          </option>
        </select>
        <button
          v-if="selectedChapterId"
          @click="analyzeChapter"
          :disabled="analyzing"
          class="px-6 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition disabled:opacity-50">
          {{ analyzing ? '分析中...' : '🤖 重新分析' }}
        </button>
      </div>
    </div>

    <!-- 分析结果展示 -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        <p class="mt-4 text-gray-600">加载分析结果...</p>
      </div>
    </div>

    <div v-else-if="!selectedChapterId" class="text-center py-20">
      <p class="text-gray-500 text-lg">👆 请先选择要查看的章节</p>
    </div>

    <div v-else-if="!currentAnalysis" class="text-center py-20">
      <p class="text-gray-500 text-lg">😕 该章节尚未分析</p>
      <button
        @click="analyzeChapter"
        class="mt-4 px-6 py-3 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition">
        🤖 立即分析
      </button>
    </div>

    <div v-else class="space-y-6">
      <!-- 质量评分卡片 -->
      <QualityScoreCard :analysis="currentAnalysis" />

      <!-- Tab 切换 -->
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="border-b border-gray-200">
          <div class="flex space-x-1 p-2">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="[
                'flex-1 px-4 py-3 rounded-lg font-medium transition',
                activeTab === tab.id
                  ? 'bg-blue-500 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
              ]">
              {{ tab.icon }} {{ tab.name }}
            </button>
          </div>
        </div>

        <!-- Tab 内容 -->
        <div class="p-6">
          <component :is="currentTabComponent" :analysis="currentAnalysis" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import ChapterAnalysisStatPanel from '../components/ChapterAnalysisStatPanel.vue'
import QualityScoreCard from '../components/QualityScoreCard.vue'
import PlotPointsTab from '../components/analysis/PlotPointsTab.vue'
import ConflictTab from '../components/analysis/ConflictTab.vue'
import CharacterArcTab from '../components/analysis/CharacterArcTab.vue'
import PacingTab from '../components/analysis/PacingTab.vue'
import EmotionCurveTab from '../components/analysis/EmotionCurveTab.vue'
import NarrativeTab from '../components/analysis/NarrativeTab.vue'
import ImprovementTab from '../components/analysis/ImprovementTab.vue'

export default {
  name: 'ChapterAnalysisViewer',
  components: {
    ChapterAnalysisStatPanel,
    QualityScoreCard,
    PlotPointsTab,
    ConflictTab,
    CharacterArcTab,
    PacingTab,
    EmotionCurveTab,
    NarrativeTab,
    ImprovementTab
  },
  setup() {
    const route = useRoute()
    const novelId = computed(() => parseInt(route.params.id))

    const loading = ref(false)
    const analyzing = ref(false)
    const showStatistics = ref(true)
    const chapters = ref([])
    const selectedChapterId = ref('')
    const currentAnalysis = ref(null)
    const statistics = ref(null)
    const activeTab = ref('plotPoints')

    const tabs = [
      { id: 'plotPoints', name: '情节点', icon: '📍' },
      { id: 'conflict', name: '冲突', icon: '⚔️' },
      { id: 'characterArc', name: '角色弧光', icon: '👤' },
      { id: 'pacing', name: '节奏', icon: '⏱️' },
      { id: 'emotion', name: '情感曲线', icon: '💓' },
      { id: 'narrative', name: '叙事技巧', icon: '📖' },
      { id: 'improvement', name: '改进建议', icon: '💡' }
    ]

    const currentTabComponent = computed(() => {
      const componentMap = {
        plotPoints: 'PlotPointsTab',
        conflict: 'ConflictTab',
        characterArc: 'CharacterArcTab',
        pacing: 'PacingTab',
        emotion: 'EmotionCurveTab',
        narrative: 'NarrativeTab',
        improvement: 'ImprovementTab'
      }
      return componentMap[activeTab.value]
    })

    const loadChapters = async () => {
      try {
        const data = await api.chapters.list(novelId.value)
        chapters.value = data || []
        console.log('加载到的章节:', chapters.value)
      } catch (error) {
        console.error('加载章节失败:', error)
        chapters.value = []
      }
    }

    const loadStatistics = async () => {
      try {
        const data = await api.chapterAnalysis.getStatistics(novelId.value)
        statistics.value = data
      } catch (error) {
        console.error('加载统计失败:', error)
      }
    }

    const onChapterChange = async () => {
      if (!selectedChapterId.value) {
        currentAnalysis.value = null
        return
      }

      loading.value = true
      try {
        const data = await api.chapterAnalysis.getByChapterId(selectedChapterId.value)
        currentAnalysis.value = data
      } catch (error) {
        console.error('加载分析结果失败:', error)
        currentAnalysis.value = null
      } finally {
        loading.value = false
      }
    }

    const analyzeChapter = async () => {
      if (!selectedChapterId.value) return

      analyzing.value = true
      try {
        const data = await api.chapterAnalysis.analyze(novelId.value, selectedChapterId.value)
        currentAnalysis.value = data
        alert('✅ 分析完成！')
        await loadStatistics()
      } catch (error) {
        console.error('分析失败:', error)
        alert('❌ 分析失败：' + (error.message || '未知错误'))
      } finally {
        analyzing.value = false
      }
    }

    const refreshAnalyses = async () => {
      await Promise.all([
        loadChapters(),
        loadStatistics(),
        selectedChapterId.value ? onChapterChange() : Promise.resolve()
      ])
    }

    onMounted(async () => {
      await loadChapters()
      await loadStatistics()
    })

    return {
      loading,
      analyzing,
      showStatistics,
      chapters,
      selectedChapterId,
      currentAnalysis,
      statistics,
      activeTab,
      tabs,
      currentTabComponent,
      onChapterChange,
      analyzeChapter,
      refreshAnalyses
    }
  }
}
</script>

<style scoped>
.chapter-analysis-viewer {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
