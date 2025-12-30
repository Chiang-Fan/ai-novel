<template>
  <div class="character-growth-panel">
    <!-- 顶部标题栏 -->
    <div class="header-bar">
      <div class="flex items-center space-x-3">
        <button @click="$router.back()" class="back-btn">
          ← 返回
        </button>
        <h2 class="text-2xl font-bold text-gray-800">
          👤 {{ characterName }} - 成长系统
        </h2>
      </div>
      <div class="flex space-x-2">
        <button @click="showAddRecordDialog = true" class="action-btn primary">
          📝 添加记录
        </button>
        <button @click="showAddMilestoneDialog = true" class="action-btn success">
          ⭐ 添加里程碑
        </button>
        <button @click="generateAnalysis" class="action-btn info" :disabled="loading">
          📊 生成分析
        </button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 主内容区 -->
    <div v-else class="content-wrapper">
      <!-- 左侧：时间轴 -->
      <div class="left-panel">
        <div class="panel-card">
          <h3 class="panel-title">📅 成长时间轴</h3>
          
          <!-- 里程碑和记录混合时间轴 -->
          <div class="timeline">
            <div v-for="item in timelineItems" :key="item.id" class="timeline-item">
              <!-- 里程碑 -->
              <div v-if="item.type === 'milestone'" class="milestone-card" :class="getMilestoneClass(item.milestoneType)">
                <div class="milestone-header">
                  <span class="milestone-icon">{{ getMilestoneIcon(item.milestoneType) }}</span>
                  <span class="milestone-name">{{ item.eventName }}</span>
                  <button @click="deleteMilestone(item.id)" class="delete-btn">×</button>
                </div>
                <div class="milestone-body">
                  <p class="text-sm text-gray-600">{{ item.description }}</p>
                  <div class="flex items-center justify-between mt-2">
                    <span class="text-xs text-gray-500">{{ item.chapterTitle || '未关联章节' }}</span>
                    <span class="impact-badge">影响: {{ item.impactLevel }}/10</span>
                  </div>
                </div>
              </div>

              <!-- 成长记录 -->
              <div v-else class="record-card">
                <div class="record-header">
                  <span class="record-icon">📝</span>
                  <span class="record-time">{{ formatTime(item.recordTime) }}</span>
                  <button @click="deleteRecord(item.id)" class="delete-btn">×</button>
                </div>
                <div class="record-body">
                  <div class="attributes-grid">
                    <div v-for="(value, key) in item.attributes" :key="key" class="attribute-item">
                      <span class="attr-name">{{ getAttributeName(key) }}</span>
                      <span class="attr-value">{{ formatAttributeValue(value) }}</span>
                    </div>
                  </div>
                  <p v-if="item.notes" class="text-sm text-gray-600 mt-2">{{ item.notes }}</p>
                  <span class="text-xs text-gray-500">{{ item.chapterTitle || '未关联章节' }}</span>
                </div>
              </div>
            </div>

            <div v-if="timelineItems.length === 0" class="empty-state">
              <p class="text-gray-500">暂无成长记录</p>
              <p class="text-sm text-gray-400">点击上方按钮添加记录或里程碑</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：图表和分析 -->
      <div class="right-panel">
        <!-- 成长曲线图 -->
        <div class="panel-card mb-4">
          <h3 class="panel-title">📈 成长曲线</h3>
          <div id="growthChart" style="width: 100%; height: 300px;"></div>
        </div>

        <!-- 多维度雷达图 -->
        <div class="panel-card mb-4">
          <h3 class="panel-title">🎯 多维度对比</h3>
          <div id="radarChart" style="width: 100%; height: 300px;"></div>
        </div>

        <!-- AI 分析结果 -->
        <div v-if="analysisResult" class="panel-card">
          <h3 class="panel-title">🤖 AI 成长分析</h3>
          
          <div class="analysis-section">
            <h4 class="section-title">📝 成长总结</h4>
            <p class="text-gray-700">{{ analysisResult.summary }}</p>
          </div>

          <div class="analysis-section">
            <h4 class="section-title">📊 趋势分析</h4>
            <div class="trends-list">
              <div v-for="trend in analysisResult.trends" :key="trend.attribute" class="trend-item">
                <div class="flex items-center justify-between">
                  <span class="font-medium">{{ trend.attributeName }}</span>
                  <span :class="getTrendClass(trend.trend)">
                    {{ getTrendLabel(trend.trend) }} {{ formatChange(trend.change) }}
                  </span>
                </div>
                <p class="text-sm text-gray-600 mt-1">{{ trend.analysis }}</p>
              </div>
            </div>
          </div>

          <div v-if="analysisResult.suggestions.length > 0" class="analysis-section">
            <h4 class="section-title">💡 改进建议</h4>
            <ul class="suggestions-list">
              <li v-for="(suggestion, index) in analysisResult.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>

          <div v-if="analysisResult.warnings.length > 0" class="analysis-section warning">
            <h4 class="section-title">⚠️ 警告</h4>
            <ul class="warnings-list">
              <li v-for="(warning, index) in analysisResult.warnings" :key="index">
                {{ warning }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加记录对话框 -->
    <div v-if="showAddRecordDialog" class="modal-overlay" @click.self="showAddRecordDialog = false">
      <div class="modal-content">
        <h3 class="modal-title">📝 添加成长记录</h3>
        
        <div class="form-group">
          <label>关联章节（可选）</label>
          <select v-model="newRecord.chapterId" class="form-input">
            <option :value="null">未关联章节</option>
            <option v-for="chapter in chapters" :key="chapter.id" :value="chapter.id">
              {{ chapter.title }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>记录时间</label>
          <input v-model="newRecord.recordTime" type="datetime-local" class="form-input" />
        </div>

        <div class="form-group">
          <label>属性快照</label>
          <div class="attributes-editor">
            <div v-for="(value, key) in newRecord.attributes" :key="key" class="attribute-row">
              <span class="attr-label">{{ getAttributeName(key) }}</span>
              <input v-model.number="newRecord.attributes[key]" type="number" min="0" max="10" class="attr-input" />
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>备注</label>
          <textarea v-model="newRecord.notes" class="form-textarea" rows="3" placeholder="记录备注..."></textarea>
        </div>

        <div class="modal-actions">
          <button @click="showAddRecordDialog = false" class="btn-cancel">取消</button>
          <button @click="addRecord" class="btn-confirm">确认添加</button>
        </div>
      </div>
    </div>

    <!-- 添加里程碑对话框 -->
    <div v-if="showAddMilestoneDialog" class="modal-overlay" @click.self="showAddMilestoneDialog = false">
      <div class="modal-content">
        <h3 class="modal-title">⭐ 添加里程碑</h3>
        
        <div class="form-group">
          <label>关联章节（可选）</label>
          <select v-model="newMilestone.chapterId" class="form-input">
            <option :value="null">未关联章节</option>
            <option v-for="chapter in chapters" :key="chapter.id" :value="chapter.id">
              {{ chapter.title }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>里程碑类型</label>
          <select v-model="newMilestone.milestoneType" class="form-input">
            <option value="POSITIVE">正面事件</option>
            <option value="NEGATIVE">负面事件</option>
            <option value="NEUTRAL">中性事件</option>
          </select>
        </div>

        <div class="form-group">
          <label>事件名称</label>
          <input v-model="newMilestone.eventName" class="form-input" placeholder="例如：获得神剑" />
        </div>

        <div class="form-group">
          <label>详细描述</label>
          <textarea v-model="newMilestone.description" class="form-textarea" rows="3" placeholder="描述事件详情..."></textarea>
        </div>

        <div class="form-group">
          <label>影响程度 (1-10): {{ newMilestone.impactLevel }}</label>
          <input v-model.number="newMilestone.impactLevel" type="range" min="1" max="10" class="range-input" />
        </div>

        <div class="form-group">
          <label>影响属性</label>
          <div class="attributes-editor">
            <div v-for="(value, key) in newMilestone.affectedAttributes" :key="key" class="attribute-row">
              <span class="attr-label">{{ getAttributeName(key) }}</span>
              <input v-model.number="newMilestone.affectedAttributes[key]" type="number" min="-10" max="10" class="attr-input" />
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showAddMilestoneDialog = false" class="btn-cancel">取消</button>
          <button @click="addMilestone" class="btn-confirm">确认添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import api from '../api'

export default {
  name: 'CharacterGrowthPanel',
  data() {
    return {
      loading: true,
      characterId: null,
      characterName: '',
      growthRecords: [],
      milestones: [],
      chapters: [],
      analysisResult: null,
      
      showAddRecordDialog: false,
      showAddMilestoneDialog: false,
      
      newRecord: {
        chapterId: null,
        recordTime: new Date().toISOString().slice(0, 16),
        attributes: {
          brave: 5,
          cautious: 5,
          empathy: 5,
          confidence: 5,
          swordPlay: 5,
          magic: 5
        },
        notes: ''
      },
      
      newMilestone: {
        chapterId: null,
        milestoneType: 'POSITIVE',
        eventName: '',
        description: '',
        impactLevel: 5,
        affectedAttributes: {
          brave: 0,
          cautious: 0,
          empathy: 0,
          confidence: 0,
          swordPlay: 0,
          magic: 0
        }
      },
      
      growthChart: null,
      radarChart: null
    }
  },
  computed: {
    timelineItems() {
      const items = [
        ...this.growthRecords.map(r => ({ ...r, type: 'record', sortTime: new Date(r.recordTime) })),
        ...this.milestones.map(m => ({ ...m, type: 'milestone', sortTime: new Date(m.createdAt) }))
      ]
      return items.sort((a, b) => b.sortTime - a.sortTime)
    }
  },
  async mounted() {
    this.characterId = parseInt(this.$route.params.characterId)
    await this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        // 加载角色信息
        const charRes = await api.get(`/api/characters/${this.characterId}`)
        this.characterName = charRes.data.name
        
        // 加载成长记录
        const recordsRes = await api.get(`/api/character-growth/records/character/${this.characterId}`)
        this.growthRecords = recordsRes.data
        
        // 加载里程碑
        const milestonesRes = await api.get(`/api/character-growth/milestones/character/${this.characterId}`)
        this.milestones = milestonesRes.data
        
        // 加载章节列表（用于选择）
        const novelsRes = await api.get('/api/novels')
        if (novelsRes.data && novelsRes.data.length > 0) {
          const chaptersRes = await api.get(`/api/novels/${novelsRes.data[0].id}/chapters`)
          this.chapters = chaptersRes.data
        }
        
        this.$nextTick(() => {
          this.renderCharts()
        })
      } catch (error) {
        console.error('加载数据失败:', error)
        alert('加载数据失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    
    async addRecord() {
      try {
        await api.post('/api/character-growth/records', {
          characterId: this.characterId,
          ...this.newRecord
        })
        
        this.showAddRecordDialog = false
        await this.loadData()
        alert('添加成功！')
      } catch (error) {
        console.error('添加记录失败:', error)
        alert('添加记录失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    async addMilestone() {
      if (!this.newMilestone.eventName.trim()) {
        alert('请输入事件名称')
        return
      }
      
      try {
        await api.post('/api/character-growth/milestones', {
          characterId: this.characterId,
          ...this.newMilestone
        })
        
        this.showAddMilestoneDialog = false
        this.newMilestone.eventName = ''
        this.newMilestone.description = ''
        await this.loadData()
        alert('添加成功！')
      } catch (error) {
        console.error('添加里程碑失败:', error)
        alert('添加里程碑失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    async deleteRecord(recordId) {
      if (!confirm('确认删除这条成长记录？')) return
      
      try {
        await api.delete(`/api/character-growth/records/${recordId}`)
        await this.loadData()
      } catch (error) {
        console.error('删除记录失败:', error)
        alert('删除失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    async deleteMilestone(milestoneId) {
      if (!confirm('确认删除这个里程碑？')) return
      
      try {
        await api.delete(`/api/character-growth/milestones/${milestoneId}`)
        await this.loadData()
      } catch (error) {
        console.error('删除里程碑失败:', error)
        alert('删除失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    async generateAnalysis() {
      this.loading = true
      try {
        const res = await api.post('/api/character-growth/analyze', {
          characterId: this.characterId
        })
        this.analysisResult = res.data
      } catch (error) {
        console.error('生成分析失败:', error)
        alert('生成分析失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    
    renderCharts() {
      if (this.growthRecords.length === 0) return
      
      this.renderGrowthChart()
      this.renderRadarChart()
    },
    
    renderGrowthChart() {
      const chartDom = document.getElementById('growthChart')
      if (!chartDom) return
      
      this.growthChart = echarts.init(chartDom)
      
      const times = this.growthRecords.map(r => this.formatTime(r.recordTime))
      const attributes = ['brave', 'cautious', 'empathy', 'confidence', 'swordPlay', 'magic']
      const series = attributes.map(attr => ({
        name: this.getAttributeName(attr),
        type: 'line',
        smooth: true,
        data: this.growthRecords.map(r => this.getAttributeValue(r.attributes, attr))
      }))
      
      this.growthChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: attributes.map(a => this.getAttributeName(a)) },
        xAxis: { type: 'category', data: times },
        yAxis: { type: 'value', min: 0, max: 10 },
        series: series
      })
    },
    
    renderRadarChart() {
      const chartDom = document.getElementById('radarChart')
      if (!chartDom || this.growthRecords.length === 0) return
      
      this.radarChart = echarts.init(chartDom)
      
      const firstRecord = this.growthRecords[0]
      const lastRecord = this.growthRecords[this.growthRecords.length - 1]
      
      const indicator = [
        { name: '勇气', max: 10 },
        { name: '谨慎', max: 10 },
        { name: '同理心', max: 10 },
        { name: '自信', max: 10 },
        { name: '剑术', max: 10 },
        { name: '魔法', max: 10 }
      ]
      
      this.radarChart.setOption({
        tooltip: {},
        legend: { data: ['初期', '当前'] },
        radar: { indicator: indicator },
        series: [{
          type: 'radar',
          data: [
            {
              value: [
                this.getAttributeValue(firstRecord.attributes, 'brave'),
                this.getAttributeValue(firstRecord.attributes, 'cautious'),
                this.getAttributeValue(firstRecord.attributes, 'empathy'),
                this.getAttributeValue(firstRecord.attributes, 'confidence'),
                this.getAttributeValue(firstRecord.attributes, 'swordPlay'),
                this.getAttributeValue(firstRecord.attributes, 'magic')
              ],
              name: '初期'
            },
            {
              value: [
                this.getAttributeValue(lastRecord.attributes, 'brave'),
                this.getAttributeValue(lastRecord.attributes, 'cautious'),
                this.getAttributeValue(lastRecord.attributes, 'empathy'),
                this.getAttributeValue(lastRecord.attributes, 'confidence'),
                this.getAttributeValue(lastRecord.attributes, 'swordPlay'),
                this.getAttributeValue(lastRecord.attributes, 'magic')
              ],
              name: '当前'
            }
          ]
        }]
      })
    },
    
    getAttributeValue(attrs, key) {
      if (typeof attrs === 'string') {
        try {
          attrs = JSON.parse(attrs)
        } catch {
          return 0
        }
      }
      return attrs[key] || 0
    },
    
    getAttributeName(key) {
      const names = {
        brave: '勇气',
        cautious: '谨慎',
        empathy: '同理心',
        confidence: '自信',
        swordPlay: '剑术',
        magic: '魔法'
      }
      return names[key] || key
    },
    
    formatAttributeValue(value) {
      if (typeof value === 'object') {
        return JSON.stringify(value)
      }
      return value
    },
    
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN', { 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    getMilestoneIcon(type) {
      const icons = {
        POSITIVE: '⭐',
        NEGATIVE: '💔',
        NEUTRAL: '📌'
      }
      return icons[type] || '📌'
    },
    
    getMilestoneClass(type) {
      return `milestone-${type.toLowerCase()}`
    },
    
    getTrendLabel(trend) {
      const labels = {
        RISING: '上升',
        FALLING: '下降',
        STABLE: '稳定'
      }
      return labels[trend] || trend
    },
    
    getTrendClass(trend) {
      return `trend-${trend.toLowerCase()}`
    },
    
    formatChange(change) {
      return change > 0 ? `+${change.toFixed(1)}` : change.toFixed(1)
    }
  }
}
</script>

<style scoped>
.character-growth-panel {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.back-btn {
  padding: 8px 16px;
  background: #f3f4f6;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #e5e7eb;
}

.action-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.action-btn.primary {
  background: #3b82f6;
  color: white;
}

.action-btn.primary:hover {
  background: #2563eb;
}

.action-btn.success {
  background: #10b981;
  color: white;
}

.action-btn.success:hover {
  background: #059669;
}

.action-btn.info {
  background: #8b5cf6;
  color: white;
}

.action-btn.info:hover {
  background: #7c3aed;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
}

.panel-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 700px;
  overflow-y: auto;
}

.timeline-item {
  position: relative;
}

.milestone-card {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 8px;
  padding: 12px;
  border-left: 4px solid #f59e0b;
}

.milestone-card.milestone-positive {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-left-color: #10b981;
}

.milestone-card.milestone-negative {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  border-left-color: #ef4444;
}

.milestone-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.milestone-icon {
  font-size: 20px;
}

.milestone-name {
  font-weight: 600;
  flex: 1;
}

.delete-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6b7280;
  padding: 0 4px;
}

.delete-btn:hover {
  color: #ef4444;
}

.impact-badge {
  background: rgba(0,0,0,0.1);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.record-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
}

.record-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.record-icon {
  font-size: 18px;
}

.record-time {
  font-weight: 500;
  color: #6b7280;
  flex: 1;
}

.attributes-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 8px;
}

.attribute-item {
  display: flex;
  justify-content: space-between;
  padding: 6px;
  background: #f9fafb;
  border-radius: 4px;
  font-size: 13px;
}

.attr-name {
  color: #6b7280;
}

.attr-value {
  font-weight: 600;
  color: #3b82f6;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.analysis-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.analysis-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.trends-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trend-item {
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.trend-rising {
  color: #10b981;
  font-weight: 600;
}

.trend-falling {
  color: #ef4444;
  font-weight: 600;
}

.trend-stable {
  color: #6b7280;
  font-weight: 600;
}

.suggestions-list, .warnings-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions-list li, .warnings-list li {
  padding: 8px 12px;
  background: #f0fdf4;
  border-left: 3px solid #10b981;
  border-radius: 4px;
  margin-bottom: 8px;
}

.warnings-list li {
  background: #fef2f2;
  border-left-color: #ef4444;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: #374151;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
}

.range-input {
  width: 100%;
}

.attributes-editor {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.attribute-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.attr-label {
  font-size: 14px;
  color: #6b7280;
  min-width: 60px;
}

.attr-input {
  flex: 1;
  padding: 6px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  text-align: center;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel, .btn-confirm {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-confirm {
  background: #3b82f6;
  color: white;
}

.btn-confirm:hover {
  background: #2563eb;
}
</style>
