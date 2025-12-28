<template>
  <div class="home">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎使用AI小说创作助手</h1>
          <p>让AI助力您的创作之旅，从灵感到成书，全程智能陪伴</p>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="$router.push('/create')">
              <el-icon><Plus /></el-icon>
              开始创作
            </el-button>
            <el-button size="large" @click="$router.push('/novels')">
              <el-icon><Document /></el-icon>
              我的作品
            </el-button>
          </div>
        </div>
        <div class="welcome-image">
          <el-icon size="200" color="#409eff"><Edit /></el-icon>
        </div>
      </div>
    </el-card>

    <!-- 功能特色 -->
    <div class="features">
      <h2>核心功能</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="feature-card">
            <div class="feature-icon">
              <el-icon size="40" color="#67c23a"><Cpu /></el-icon>
            </div>
            <h3>AI智能创作</h3>
            <p>基于先进的AI技术，为您提供创意灵感、情节构思和文字润色</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="feature-card">
            <div class="feature-icon">
              <el-icon size="40" color="#e6a23c"><Collection /></el-icon>
            </div>
            <h3>章节管理</h3>
            <p>完善的章节组织系统，支持大纲规划、进度跟踪和版本管理</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="feature-card">
            <div class="feature-icon">
              <el-icon size="40" color="#f56c6c"><User /></el-icon>
            </div>
            <h3>角色设定</h3>
            <p>详细的角色档案管理，包括性格特征、关系网络和成长轨迹</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最近作品 -->
    <div class="recent-works" v-if="recentNovels.length > 0">
      <h2>最近编辑</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="novel in recentNovels" :key="novel.id">
          <el-card class="novel-card" @click="$router.push(`/novels/${novel.id}`)">
            <div class="novel-cover">
              <el-icon size="60"><Document /></el-icon>
            </div>
            <h4>{{ novel.title }}</h4>
            <p class="novel-desc">{{ novel.description || '暂无描述' }}</p>
            <div class="novel-meta">
              <span>{{ novel.wordCount || 0 }} 字</span>
              <span>{{ formatDate(novel.updatedAt) }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { novelApi } from '../api'

const recentNovels = ref([])

const loadRecentNovels = async () => {
  try {
    const data = await novelApi.list({ page: 0, size: 4 })
    recentNovels.value = data.content || []
  } catch (error) {
    console.error('加载最近作品失败:', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  loadRecentNovels()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 40px;
}

.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 20px;
}

.welcome-text h1 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 16px;
}

.welcome-text p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 32px;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.welcome-image {
  opacity: 0.1;
}

.features {
  margin-bottom: 40px;
}

.features h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #303133;
}

.feature-card {
  text-align: center;
  height: 200px;
  cursor: default;
}

.feature-icon {
  margin-bottom: 16px;
}

.feature-card h3 {
  margin-bottom: 12px;
  color: #303133;
}

.feature-card p {
  color: #606266;
  line-height: 1.6;
}

.recent-works h2 {
  margin-bottom: 24px;
  color: #303133;
}

.novel-card {
  cursor: pointer;
  transition: transform 0.2s;
  height: 180px;
}

.novel-card:hover {
  transform: translateY(-4px);
}

.novel-cover {
  text-align: center;
  margin-bottom: 12px;
  opacity: 0.6;
}

.novel-card h4 {
  margin-bottom: 8px;
  color: #303133;
}

.novel-desc {
  color: #909399;
  font-size: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.novel-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #c0c4cc;
}
</style>