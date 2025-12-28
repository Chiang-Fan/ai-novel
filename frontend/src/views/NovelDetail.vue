<template>
  <div class="novel-detail" v-loading="loading">
    <div v-if="novel" class="detail-content">
      <!-- 小说信息 -->
      <el-card class="novel-info">
        <div class="novel-header">
          <div class="novel-meta">
            <h1>{{ novel.title }}</h1>
            <div class="meta-tags">
              <el-tag>{{ novel.genre }}</el-tag>
              <el-tag :type="getStatusType(novel.status)">
                {{ getStatusText(novel.status) }}
              </el-tag>
            </div>
            <p class="description">{{ novel.description }}</p>
            <div class="stats">
              <span>字数：{{ formatWordCount(novel.wordCount) }}</span>
              <span>创建时间：{{ formatDate(novel.createdAt) }}</span>
              <span>更新时间：{{ formatDate(novel.updatedAt) }}</span>
            </div>
          </div>
          <div class="novel-actions">
            <el-button type="primary" @click="editNovel">
              <el-icon><Edit /></el-icon>
              编辑信息
            </el-button>
            <el-button @click="createChapter">
              <el-icon><Plus /></el-icon>
              新建章节
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 章节列表 -->
      <el-card class="chapters-section">
        <template #header>
          <div class="section-header">
            <h2>章节列表</h2>
            <el-button type="primary" size="small" @click="createChapter">
              <el-icon><Plus /></el-icon>
              新建章节
            </el-button>
          </div>
        </template>

        <div v-if="chapters.length === 0" class="empty-state">
          <el-empty description="还没有章节，开始创作第一章吧！">
            <el-button type="primary" @click="createChapter">创建第一章</el-button>
          </el-empty>
        </div>

        <el-table v-else :data="chapters" style="width: 100%">
          <el-table-column prop="chapterNumber" label="章节" width="80">
            <template #default="{ row }">
              第{{ row.chapterNumber }}章
            </template>
          </el-table-column>
          
          <el-table-column prop="title" label="标题" min-width="200" />
          
          <el-table-column prop="wordCount" label="字数" width="100">
            <template #default="{ row }">
              {{ row.wordCount || 0 }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
                {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="updatedAt" label="更新时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="editChapter(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteChapter(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { novelApi, chapterApi } from '../api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const novel = ref(null)
const chapters = ref([])

const loadNovelDetail = async () => {
  try {
    loading.value = true
    const novelId = route.params.id
    
    // 并行加载小说信息和章节列表
    const [novelData, chaptersData] = await Promise.all([
      novelApi.detail(novelId),
      chapterApi.list(novelId)
    ])
    
    novel.value = novelData
    chapters.value = chaptersData.content || []
  } catch (error) {
    console.error('加载小说详情失败:', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

const editNovel = () => {
  // 这里可以打开编辑对话框或跳转到编辑页面
  ElMessage.info('编辑功能开发中...')
}

const createChapter = () => {
  // 这里可以打开创建章节对话框或跳转到创建页面
  ElMessage.info('创建章节功能开发中...')
}

const editChapter = (chapter) => {
  // 跳转到章节编辑页面
  ElMessage.info('章节编辑功能开发中...')
}

const deleteChapter = async (chapter) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除第${chapter.chapterNumber}章《${chapter.title}》吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 调用删除API
    ElMessage.success('删除成功')
    loadNovelDetail() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'ONGOING': 'success',
    'COMPLETED': 'info',
    'PAUSED': 'warning'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'ONGOING': '连载中',
    'COMPLETED': '已完结',
    'PAUSED': '暂停'
  }
  return statusMap[status] || '未知'
}

const formatWordCount = (count) => {
  if (!count) return '0'
  if (count < 10000) return count.toString()
  return (count / 10000).toFixed(1) + '万'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  loadNovelDetail()
})
</script>

<style scoped>
.novel-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.novel-info {
  margin-bottom: 0;
}

.novel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.novel-meta h1 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 28px;
}

.meta-tags {
  margin-bottom: 16px;
}

.meta-tags .el-tag {
  margin-right: 8px;
}

.description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;
}

.novel-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapters-section {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  color: #303133;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}
</style>