<template>
  <div class="novel-list">
    <div class="page-header">
      <h1>我的小说</h1>
      <el-button type="primary" @click="$router.push('/create')">
        <el-icon><Plus /></el-icon>
        创建新小说
      </el-button>
    </div>

    <el-card>
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索小说标题..."
          style="width: 300px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px" @change="loadNovels">
          <el-option label="全部" value=""></el-option>
          <el-option label="连载中" value="ONGOING"></el-option>
          <el-option label="已完结" value="COMPLETED"></el-option>
          <el-option label="暂停" value="PAUSED"></el-option>
        </el-select>
      </div>

      <!-- 小说列表 -->
      <el-table
        v-loading="loading"
        :data="novels"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="novel-title">
              <strong>{{ row.title }}</strong>
              <div class="novel-desc">{{ row.description || '暂无描述' }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="genre" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.genre || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)" 
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="wordCount" label="字数" width="100">
          <template #default="{ row }">
            {{ formatWordCount(row.wordCount) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="updatedAt" label="更新时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click.stop="editNovel(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button size="small" type="danger" @click.stop="deleteNovel(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { novelApi } from '../api'

const router = useRouter()

const loading = ref(false)
const novels = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadNovels = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      title: searchKeyword.value,
      status: statusFilter.value
    }
    
    const data = await novelApi.list(params)
    novels.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    console.error('加载小说列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadNovels()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadNovels()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadNovels()
}

const handleRowClick = (row) => {
  router.push(`/novels/${row.id}`)
}

const editNovel = (novel) => {
  router.push(`/novels/${novel.id}`)
}

const deleteNovel = async (novel) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除小说《${novel.title}》吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await novelApi.delete(novel.id)
    ElMessage.success('删除成功')
    loadNovels()
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
  loadNovels()
})
</script>

<style scoped>
.novel-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.search-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.novel-title {
  line-height: 1.4;
}

.novel-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.el-table .el-table__row {
  cursor: pointer;
}

.el-table .el-table__row:hover {
  background-color: #f5f7fa;
}
</style>