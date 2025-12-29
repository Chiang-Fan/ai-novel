<template>
  <div class="max-w-7xl mx-auto">
    <!-- Page Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">我的小说作品</h1>
      <p class="text-gray-600">管理您的所有创作，继续未完成的故事</p>
    </div>

    <!-- Loading State -->
    <Loading v-if="novelStore.loading && novels.length === 0" text="加载中..." />

    <!-- Empty State -->
    <div v-else-if="!novelStore.loading && novels.length === 0" class="text-center py-20">
      <svg class="w-24 h-24 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
      </svg>
      <h3 class="text-xl font-semibold text-gray-900 mb-2">还没有作品</h3>
      <p class="text-gray-600 mb-6">开始您的第一部小说创作吧！</p>
      <Button @click="$router.push('/create')" variant="primary">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        创建新作品
      </Button>
    </div>

    <!-- Novels Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="novel in novels"
        :key="novel.id"
        class="bg-white rounded-lg shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden group cursor-pointer"
        @click="goToDetail(novel.id)"
      >
        <!-- Novel Card Header -->
        <div class="h-32 bg-gradient-to-br from-blue-500 to-purple-600 relative">
          <div class="absolute inset-0 bg-black bg-opacity-20 group-hover:bg-opacity-10 transition-all"></div>
          <div class="absolute top-4 right-4">
            <span :class="[
              'px-3 py-1 rounded-full text-xs font-medium',
              getStatusStyle(novel.status)
            ]">
              {{ getStatusText(novel.status) }}
            </span>
          </div>
        </div>

        <!-- Novel Card Body -->
        <div class="p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-2 line-clamp-1">
            {{ novel.title }}
          </h3>
          <p class="text-gray-600 text-sm mb-4 line-clamp-2">
            {{ novel.description || '暂无简介' }}
          </p>

          <!-- Stats -->
          <div class="flex items-center gap-4 text-sm text-gray-500 mb-4">
            <div class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <span>{{ novel.totalChapters || 0 }} 章</span>
            </div>
            <div class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              <span>{{ formatNumber(novel.totalWords || 0) }} 字</span>
            </div>
          </div>

          <!-- Metadata -->
          <div class="flex flex-wrap gap-2 mb-4">
            <span v-if="novel.genre" class="px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded">
              {{ novel.genre }}
            </span>
            <span v-if="novel.targetAudience" class="px-2 py-1 bg-purple-100 text-purple-700 text-xs rounded">
              {{ novel.targetAudience }}
            </span>
          </div>

          <!-- Actions -->
          <div class="flex gap-2">
            <Button 
              @click.stop="goToSmartWriting(novel.id)" 
              variant="primary" 
              size="sm"
              class="flex-1"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              智能创作
            </Button>
            <Button 
              @click.stop="showDeleteModal = true; novelToDelete = novel" 
              variant="ghost" 
              size="sm"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </Button>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-3 bg-gray-50 border-t text-xs text-gray-500">
          最后更新：{{ formatDate(novel.updatedAt) }}
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <Modal
      :show="showDeleteModal"
      title="确认删除"
      confirmText="删除"
      cancelText="取消"
      :loading="deleteLoading"
      @close="showDeleteModal = false; novelToDelete = null"
      @confirm="handleDelete"
    >
      <p class="text-gray-700">
        确定要删除小说 <span class="font-semibold">{{ novelToDelete?.title }}</span> 吗？
      </p>
      <p class="text-red-600 text-sm mt-2">此操作不可恢复！</p>
    </Modal>

    <!-- Toast notifications would go here -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useNovelStore } from '../stores/novel'
import Loading from '../components/Loading.vue'
import Button from '../components/Button.vue'
import Modal from '../components/Modal.vue'

const router = useRouter()
const novelStore = useNovelStore()

const showDeleteModal = ref(false)
const novelToDelete = ref(null)
const deleteLoading = ref(false)

const novels = computed(() => novelStore.novels)

onMounted(async () => {
  await novelStore.fetchNovels()
})

const goToDetail = (id) => {
  router.push(`/novel/${id}`)
}

const goToSmartWriting = (id) => {
  router.push(`/novel/${id}/smart-writing`)
}

const getStatusText = (status) => {
  const statusMap = {
    'planning': '策划中',
    'writing': '创作中',
    'completed': '已完成',
    'paused': '已暂停'
  }
  return statusMap[status] || '未知'
}

const getStatusStyle = (status) => {
  const styleMap = {
    'planning': 'bg-yellow-100 text-yellow-700',
    'writing': 'bg-green-100 text-green-700',
    'completed': 'bg-blue-100 text-blue-700',
    'paused': 'bg-gray-100 text-gray-700'
  }
  return styleMap[status] || 'bg-gray-100 text-gray-700'
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  
  return date.toLocaleDateString('zh-CN')
}

const handleDelete = async () => {
  if (!novelToDelete.value) return
  
  deleteLoading.value = true
  try {
    await novelStore.deleteNovel(novelToDelete.value.id)
    showDeleteModal.value = false
    novelToDelete.value = null
    // 这里应该显示成功提示
  } catch (error) {
    console.error('删除失败:', error)
    // 这里应该显示错误提示
  } finally {
    deleteLoading.value = false
  }
}
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
