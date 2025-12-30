<template>
  <div class="novel-detail">
    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>

    <div v-else>
      <!-- 小说信息 -->
      <div v-if="novel" class="bg-white rounded-lg shadow-md p-6 mb-6">
        <h1 class="text-3xl font-bold text-gray-800 mb-4">{{ novel.title || '未命名小说' }}</h1>
        <p class="text-gray-600 mb-4">{{ novel.description || '暂无简介' }}</p>
        
        <div class="flex items-center space-x-6 text-sm text-gray-500">
          <span>{{ novel.genre || '未分类' }}</span>
          <span>{{ novel.totalChapters || 0 }} 章</span>
          <span>{{ formatWords(novel.totalWords || 0) }}</span>
        </div>

        <div class="mt-6">
          <div v-if="novel && novel.id" class="flex flex-wrap gap-3">
            <!-- AI续写 -->
            <router-link
              :to="`/novel/${novel.id}/write`"
              class="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition flex items-center space-x-2 shadow-md">
              <span>🤖</span>
              <span>AI续写</span>
            </router-link>

            <!-- 智能创建章节 -->
            <router-link
              :to="`/novel/${novel.id}/smart-create`"
              class="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition flex items-center space-x-2 shadow-md">
              <span>✍️</span>
              <span>智能创建章节</span>
            </router-link>

            <!-- 智能创作工作台 -->
            <router-link
              :to="`/novel/${novel.id}/smart-writing`"
              class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition flex items-center space-x-2 shadow-md">
              <span>⚡</span>
              <span>智能创作工作台</span>
            </router-link>
          </div>

          <!-- 内容管理工具 -->
          <div class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-3">
            <!-- 文风管理 -->
            <router-link
              :to="`/novel/${novel.id}/writing-style`"
              class="p-4 bg-gradient-to-br from-pink-50 to-pink-100 border-2 border-pink-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">🎨</div>
              <div class="font-semibold text-gray-800 group-hover:text-pink-600">文风管理</div>
              <div class="text-xs text-gray-500 mt-1">分析写作风格</div>
            </router-link>

            <!-- 伏笔管理 -->
            <router-link
              :to="`/novel/${novel.id}/plot-hooks`"
              class="p-4 bg-gradient-to-br from-purple-50 to-purple-100 border-2 border-purple-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">🎭</div>
              <div class="font-semibold text-gray-800 group-hover:text-purple-600">伏笔管理</div>
              <div class="text-xs text-gray-500 mt-1">埋设和跟踪伏笔</div>
            </router-link>

            <!-- 章节分析 (新增) -->
            <router-link
              :to="`/novel/${novel.id}/chapter-analysis`"
              class="p-4 bg-gradient-to-br from-cyan-50 to-cyan-100 border-2 border-cyan-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">📊</div>
              <div class="font-semibold text-gray-800 group-hover:text-cyan-600">章节分析</div>
              <div class="text-xs text-gray-500 mt-1">11维度深度分析</div>
            </router-link>

            <!-- 角色管理 -->
            <router-link
              :to="`/novel/${novel.id}/characters`"
              class="p-4 bg-gradient-to-br from-blue-50 to-blue-100 border-2 border-blue-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">👥</div>
              <div class="font-semibold text-gray-800 group-hover:text-blue-600">角色管理</div>
              <div class="text-xs text-gray-500 mt-1">创建和管理角色</div>
            </router-link>

            <!-- 场景管理 -->
            <router-link
              :to="`/novel/${novel.id}/scenes`"
              class="p-4 bg-gradient-to-br from-green-50 to-green-100 border-2 border-green-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">🎬</div>
              <div class="font-semibold text-gray-800 group-hover:text-green-600">场景管理</div>
              <div class="text-xs text-gray-500 mt-1">设计故事场景</div>
            </router-link>

            <!-- 大纲管理 -->
            <router-link
              :to="`/novel/${novel.id}/outline`"
              class="p-4 bg-gradient-to-br from-yellow-50 to-yellow-100 border-2 border-yellow-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">📋</div>
              <div class="font-semibold text-gray-800 group-hover:text-yellow-600">大纲管理</div>
              <div class="text-xs text-gray-500 mt-1">规划故事结构</div>
            </router-link>

            <!-- 编辑历史 -->
            <router-link
              :to="`/novel/${novel.id}/history`"
              class="p-4 bg-gradient-to-br from-gray-50 to-gray-100 border-2 border-gray-200 rounded-lg hover:shadow-lg transition group">
              <div class="text-3xl mb-2">📜</div>
              <div class="font-semibold text-gray-800 group-hover:text-gray-600">编辑历史</div>
              <div class="text-xs text-gray-500 mt-1">查看修改记录</div>
            </router-link>
          </div>
        </div>
      </div>

      <!-- 章节列表 -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-xl font-bold text-gray-800 mb-4">📑 章节列表</h2>
        
        <div v-if="chapters.length > 0" class="space-y-3">
          <div
            v-for="chapter in chapters"
            :key="chapter.id"
            class="border border-gray-200 rounded-lg hover:border-indigo-300 transition cursor-pointer overflow-hidden"
            :class="{ 'border-indigo-400 shadow-md': expandedChapterId === chapter.id }">
            
            <!-- 章节头部 -->
            <div 
              class="flex items-center justify-between p-4 hover:bg-gray-50"
              @click="toggleChapter(chapter.id)">
              <div class="flex-1">
                <h3 class="font-medium text-gray-800">
                  第{{ chapter.chapterNumber }}章 {{ chapter.title }}
                </h3>
                <div class="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                  <span>{{ chapter.wordCount }} 字</span>
                  <span v-if="chapter.isAiGenerated" class="text-indigo-600">🤖 AI生成</span>
                  <span v-if="chapter.summary" class="text-gray-400">📝 有摘要</span>
                </div>
              </div>
              <div class="text-gray-400 transition-transform" :class="{ 'rotate-180': expandedChapterId === chapter.id }">
                ▼
              </div>
            </div>
            
            <!-- 章节内容展开区 -->
            <div v-if="expandedChapterId === chapter.id" class="border-t border-gray-200">
              <!-- 加载中 -->
              <div v-if="loadingChapterContent" class="p-6 text-center text-gray-500">
                <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mb-2"></div>
                <p>加载中...</p>
              </div>
              
              <!-- 章节详情 -->
              <div v-else-if="expandedChapter" class="p-6 bg-gray-50">
                <!-- 摘要 -->
                <div v-if="expandedChapter.summary" class="mb-4 p-4 bg-blue-50 rounded-lg">
                  <h4 class="text-sm font-semibold text-blue-900 mb-2">📝 章节摘要</h4>
                  <p class="text-sm text-blue-800 whitespace-pre-wrap">{{ expandedChapter.summary }}</p>
                </div>
                
                <!-- 续写方向 -->
                <div v-if="expandedChapter.continuationDirection" class="mb-4 p-4 bg-green-50 rounded-lg">
                  <h4 class="text-sm font-semibold text-green-900 mb-2">🎯 续写方向</h4>
                  <p class="text-sm text-green-800 whitespace-pre-wrap">{{ expandedChapter.continuationDirection }}</p>
                </div>
                
                <!-- 正文内容 -->
                <div class="mb-4">
                  <h4 class="text-sm font-semibold text-gray-700 mb-3">📖 正文内容</h4>
                  <div class="prose max-w-none bg-white p-6 rounded-lg border border-gray-200">
                    <div class="whitespace-pre-wrap text-gray-800 leading-relaxed font-serif text-base">
                      {{ expandedChapter.content }}
                    </div>
                  </div>
                </div>
                
                <!-- 操作按钮 -->
                <div class="flex justify-end space-x-3">
                  <router-link
                    :to="`/novel/${novel.id}/smart-create?chapterId=${expandedChapter.id}`"
                    class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm">
                    ✏️ 编辑
                  </router-link>
                  <button
                    @click="analyzeChapter(expandedChapter.id)"
                    class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition text-sm">
                    🔍 分析
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-gray-500">
          还没有章节，点击"AI续写"开始创作
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()
const novelId = ref(parseInt(route.params.id))

const novel = ref({})
const chapters = ref([])
const loading = ref(true)
const expandedChapterId = ref(null)
const expandedChapter = ref(null)
const loadingChapterContent = ref(false)

onMounted(async () => {
  try {
    novel.value = await api.novels.get(novelId.value)
    chapters.value = await api.chapters.list(novelId.value)
  } catch (error) {
    console.error('加载失败:', error)
    alert('加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
})

const toggleChapter = async (chapterId) => {
  if (expandedChapterId.value === chapterId) {
    // 收起
    expandedChapterId.value = null
    expandedChapter.value = null
  } else {
    // 展开
    expandedChapterId.value = chapterId
    loadingChapterContent.value = true
    try {
      expandedChapter.value = await api.chapters.get(chapterId)
    } catch (error) {
      console.error('加载章节详情失败:', error)
      alert('加载章节详情失败: ' + error.message)
    } finally {
      loadingChapterContent.value = false
    }
  }
}

const analyzeChapter = async (chapterId) => {
  try {
    const analysis = await api.smartWriting.getChapterAnalysis(chapterId)
    alert('分析完成！\n' + 
      '主角: ' + (analysis.protagonistName || '未识别') + '\n' +
      '场景: ' + (analysis.sceneInfo?.description || '未识别') + '\n' +
      '完整性得分: ' + (analysis.completenessScore || 0))
  } catch (error) {
    console.error('分析失败:', error)
    alert('分析失败: ' + error.message)
  }
}

const formatWords = (words) => {
  if (words >= 10000) {
    return (words / 10000).toFixed(1) + '万字'
  }
  return words + '字'
}
</script>
