<template>
  <div class="scene-management-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <div class="header-left">
        <h2>🏞️ 场景动态管理</h2>
        <span class="subtitle">{{ sceneName }}</span>
      </div>
      <div class="header-right">
        <button class="btn-back" @click="goBack">
          <i class="icon">←</i> 返回
        </button>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-item', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        <i class="icon">{{ tab.icon }}</i>
        <span>{{ tab.label }}</span>
      </div>
    </div>

    <!-- 标签页内容 -->
    <div class="tab-content">
      <!-- Tab 1: 场景详情 -->
      <div v-show="activeTab === 'detail'" class="tab-pane">
        <div class="detail-container">
          <!-- 基本信息卡片 -->
          <div class="info-card">
            <div class="card-header">
              <h3>📋 基本信息</h3>
              <button class="btn-edit" @click="editScene">编辑</button>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">场景名称:</span>
                <span class="value">{{ sceneDetail.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">场景类型:</span>
                <span class="value">{{ getSceneTypeLabel(sceneDetail.sceneType) }}</span>
              </div>
              <div class="info-item">
                <span class="label">地点:</span>
                <span class="value">{{ sceneDetail.location || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">重要性:</span>
                <span class="value">
                  <div class="importance-stars">
                    <span v-for="i in 10" :key="i" 
                      :class="['star', { active: i <= sceneDetail.importance }]">★</span>
                  </div>
                </span>
              </div>
              <div class="info-item full-width">
                <span class="label">描述:</span>
                <span class="value">{{ sceneDetail.description || '暂无描述' }}</span>
              </div>
              <div class="info-item full-width">
                <span class="label">标签:</span>
                <span class="value">
                  <div class="tags">
                    <span v-for="tag in sceneDetail.tags" :key="tag" class="tag">{{ tag }}</span>
                    <span v-if="!sceneDetail.tags || sceneDetail.tags.length === 0" class="empty">暂无标签</span>
                  </div>
                </span>
              </div>
            </div>
          </div>

          <!-- 场景属性卡片 -->
          <div class="info-card">
            <div class="card-header">
              <h3>🌤️ 场景属性</h3>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">时间段:</span>
                <span class="value">{{ sceneDetail.timePeriod || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">天气:</span>
                <span class="value">{{ sceneDetail.weather || '未设置' }}</span>
              </div>
              <div class="info-item full-width">
                <span class="label">道具:</span>
                <span class="value">
                  <div class="tags">
                    <span v-for="prop in parseJsonArray(sceneDetail.props)" :key="prop" class="tag">{{ prop }}</span>
                    <span v-if="parseJsonArray(sceneDetail.props).length === 0" class="empty">暂无道具</span>
                  </div>
                </span>
              </div>
              <div class="info-item full-width">
                <span class="label">涉及角色:</span>
                <span class="value">
                  <div class="tags">
                    <span v-for="char in parseJsonArray(sceneDetail.involvedCharacters)" :key="char" class="tag character-tag">{{ char }}</span>
                    <span v-if="parseJsonArray(sceneDetail.involvedCharacters).length === 0" class="empty">暂无角色</span>
                  </div>
                </span>
              </div>
              <div class="info-item full-width">
                <span class="label">章节引用:</span>
                <span class="value">
                  <div class="tags">
                    <span v-for="ref in parseJsonArray(sceneDetail.chapterReferences)" :key="ref" class="tag chapter-tag">第{{ ref }}章</span>
                    <span v-if="parseJsonArray(sceneDetail.chapterReferences).length === 0" class="empty">暂未使用</span>
                  </div>
                </span>
              </div>
            </div>
          </div>

          <!-- 备注卡片 -->
          <div class="info-card" v-if="sceneDetail.notes">
            <div class="card-header">
              <h3>📝 备注</h3>
            </div>
            <div class="notes-content">
              {{ sceneDetail.notes }}
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 2: 使用历史 -->
      <div v-show="activeTab === 'usage'" class="tab-pane">
        <div class="usage-container">
          <div class="actions-bar">
            <button class="btn-primary" @click="showAddUsageDialog">
              <i class="icon">+</i> 记录使用
            </button>
          </div>

          <div v-if="usageRecords.length === 0" class="empty-state">
            <i class="icon">📝</i>
            <p>暂无使用记录</p>
            <button class="btn-secondary" @click="showAddUsageDialog">添加第一条记录</button>
          </div>

          <div v-else class="usage-timeline">
            <div v-for="usage in usageRecords" :key="usage.id" class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="usage-card">
                  <div class="usage-header">
                    <div class="usage-time">
                      <i class="icon">🕐</i>
                      {{ formatDateTime(usage.usageTime) }}
                    </div>
                    <button class="btn-delete" @click="deleteUsage(usage.id)">删除</button>
                  </div>
                  <div class="usage-info">
                    <div class="usage-item">
                      <span class="label">章节:</span>
                      <span class="value">第 {{ usage.chapterId }} 章</span>
                    </div>
                    <div class="usage-item">
                      <span class="label">时段:</span>
                      <span class="value">{{ usage.timeOfDay || '未设置' }}</span>
                    </div>
                    <div class="usage-item">
                      <span class="label">天气:</span>
                      <span class="value">{{ usage.weather || '未设置' }}</span>
                    </div>
                    <div class="usage-item full-width" v-if="usage.sceneState">
                      <span class="label">场景状态:</span>
                      <span class="value">{{ usage.sceneState }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 3: 变化记录 -->
      <div v-show="activeTab === 'changes'" class="tab-pane">
        <div class="changes-container">
          <div class="actions-bar">
            <button class="btn-primary" @click="showAddChangeDialog">
              <i class="icon">+</i> 记录变化
            </button>
          </div>

          <div v-if="changeRecords.length === 0" class="empty-state">
            <i class="icon">📋</i>
            <p>暂无变化记录</p>
            <button class="btn-secondary" @click="showAddChangeDialog">添加第一条记录</button>
          </div>

          <div v-else class="changes-list">
            <div v-for="change in changeRecords" :key="change.id" class="change-card">
              <div class="change-header">
                <div class="change-type" :class="getChangeTypeClass(change.changeType)">
                  {{ getChangeTypeLabel(change.changeType) }}
                </div>
                <div class="change-time">{{ formatDateTime(change.changeTime) }}</div>
                <button class="btn-delete" @click="deleteChange(change.id)">删除</button>
              </div>
              <div class="change-content">
                <div class="change-desc">
                  <strong>变化描述:</strong> {{ change.changeDesc }}
                </div>
                <div class="change-states" v-if="change.beforeState || change.afterState">
                  <div class="state-item" v-if="change.beforeState">
                    <span class="state-label">变化前:</span>
                    <span class="state-value">{{ change.beforeState }}</span>
                  </div>
                  <div class="state-arrow" v-if="change.beforeState && change.afterState">→</div>
                  <div class="state-item" v-if="change.afterState">
                    <span class="state-label">变化后:</span>
                    <span class="state-value">{{ change.afterState }}</span>
                  </div>
                </div>
                <div class="change-chapter" v-if="change.relatedChapterId">
                  <i class="icon">📖</i> 关联章节: 第 {{ change.relatedChapterId }} 章
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 4: 统计分析 -->
      <div v-show="activeTab === 'statistics'" class="tab-pane">
        <div class="statistics-container">
          <div class="stats-grid">
            <!-- 基础统计卡片 -->
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-content">
                <div class="stat-label">使用次数</div>
                <div class="stat-value">{{ statistics.usageCount || 0 }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">🔄</div>
              <div class="stat-content">
                <div class="stat-label">变化次数</div>
                <div class="stat-value">{{ statistics.changeCount || 0 }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📖</div>
              <div class="stat-content">
                <div class="stat-label">首次出现</div>
                <div class="stat-value">{{ statistics.firstChapter ? `第${statistics.firstChapter}章` : '未使用' }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📚</div>
              <div class="stat-content">
                <div class="stat-label">最后出现</div>
                <div class="stat-value">{{ statistics.lastChapter ? `第${statistics.lastChapter}章` : '未使用' }}</div>
              </div>
            </div>
          </div>

          <!-- 时段分布图表 -->
          <div class="chart-card" v-if="statistics.timeOfDayDistribution && Object.keys(statistics.timeOfDayDistribution).length > 0">
            <h3>🕐 时段分布</h3>
            <div ref="timeChartRef" class="chart-container"></div>
          </div>

          <!-- 天气分布图表 -->
          <div class="chart-card" v-if="statistics.weatherDistribution && Object.keys(statistics.weatherDistribution).length > 0">
            <h3>🌤️ 天气分布</h3>
            <div ref="weatherChartRef" class="chart-container"></div>
          </div>

          <!-- 空状态 -->
          <div v-if="!statistics.usageCount" class="empty-state">
            <i class="icon">📊</i>
            <p>暂无统计数据</p>
            <p class="hint">添加使用记录后即可查看统计信息</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加使用记录对话框 -->
    <div v-if="showUsageDialog" class="modal-overlay" @click.self="showUsageDialog = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3>记录场景使用</h3>
          <button class="btn-close" @click="showUsageDialog = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>章节ID <span class="required">*</span></label>
            <input v-model.number="usageForm.chapterId" type="number" placeholder="请输入章节ID" />
          </div>
          <div class="form-group">
            <label>时段</label>
            <select v-model="usageForm.timeOfDay">
              <option value="">请选择</option>
              <option value="黎明">黎明</option>
              <option value="上午">上午</option>
              <option value="中午">中午</option>
              <option value="下午">下午</option>
              <option value="傍晚">傍晚</option>
              <option value="夜晚">夜晚</option>
              <option value="深夜">深夜</option>
            </select>
          </div>
          <div class="form-group">
            <label>天气</label>
            <select v-model="usageForm.weather">
              <option value="">请选择</option>
              <option value="晴">晴</option>
              <option value="多云">多云</option>
              <option value="阴">阴</option>
              <option value="小雨">小雨</option>
              <option value="大雨">大雨</option>
              <option value="雪">雪</option>
              <option value="雾">雾</option>
            </select>
          </div>
          <div class="form-group">
            <label>场景状态</label>
            <textarea v-model="usageForm.sceneState" rows="3" placeholder="描述场景在此次使用时的状态..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showUsageDialog = false">取消</button>
          <button class="btn-primary" @click="addUsageRecord">确定</button>
        </div>
      </div>
    </div>

    <!-- 添加变化记录对话框 -->
    <div v-if="showChangeDialog" class="modal-overlay" @click.self="showChangeDialog = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3>记录场景变化</h3>
          <button class="btn-close" @click="showChangeDialog = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>变化类型 <span class="required">*</span></label>
            <select v-model="changeForm.changeType">
              <option value="">请选择</option>
              <option value="REBUILD">重建</option>
              <option value="DAMAGE">损坏</option>
              <option value="SEASONAL">季节变化</option>
              <option value="DECORATION">装饰变化</option>
              <option value="EXPANSION">扩建</option>
              <option value="OTHER">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>变化描述 <span class="required">*</span></label>
            <textarea v-model="changeForm.changeDesc" rows="3" placeholder="描述场景发生的变化..."></textarea>
          </div>
          <div class="form-group">
            <label>变化前状态</label>
            <textarea v-model="changeForm.beforeState" rows="2" placeholder="描述变化前的状态..."></textarea>
          </div>
          <div class="form-group">
            <label>变化后状态</label>
            <textarea v-model="changeForm.afterState" rows="2" placeholder="描述变化后的状态..."></textarea>
          </div>
          <div class="form-group">
            <label>关联章节ID</label>
            <input v-model.number="changeForm.relatedChapterId" type="number" placeholder="请输入章节ID" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showChangeDialog = false">取消</button>
          <button class="btn-primary" @click="addChangeRecord">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'SceneManagementPanel',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const sceneId = ref(route.params.sceneId)
    const sceneName = ref('加载中...')

    // 标签页
    const tabs = [
      { key: 'detail', label: '场景详情', icon: '📋' },
      { key: 'usage', label: '使用历史', icon: '📝' },
      { key: 'changes', label: '变化记录', icon: '🔄' },
      { key: 'statistics', label: '统计分析', icon: '📊' }
    ]
    const activeTab = ref('detail')

    // 场景详情
    const sceneDetail = ref({})

    // 使用记录
    const usageRecords = ref([])
    const showUsageDialog = ref(false)
    const usageForm = ref({
      chapterId: null,
      timeOfDay: '',
      weather: '',
      sceneState: ''
    })

    // 变化记录
    const changeRecords = ref([])
    const showChangeDialog = ref(false)
    const changeForm = ref({
      changeType: '',
      changeDesc: '',
      beforeState: '',
      afterState: '',
      relatedChapterId: null
    })

    // 统计数据
    const statistics = ref({})
    const timeChartRef = ref(null)
    const weatherChartRef = ref(null)

    // 加载场景详情
    const loadSceneDetail = async () => {
      try {
        const response = await axios.get(`/api/scenes/${sceneId.value}`)
        sceneDetail.value = response.data.data
        sceneName.value = sceneDetail.value.name
      } catch (error) {
        console.error('加载场景详情失败:', error)
        alert('加载场景详情失败')
      }
    }

    // 加载使用记录
    const loadUsageRecords = async () => {
      try {
        const response = await axios.get(`/api/scenes/${sceneId.value}/usages`)
        usageRecords.value = response.data.data || []
      } catch (error) {
        console.error('加载使用记录失败:', error)
      }
    }

    // 加载变化记录
    const loadChangeRecords = async () => {
      try {
        const response = await axios.get(`/api/scenes/${sceneId.value}/changes`)
        changeRecords.value = response.data.data || []
      } catch (error) {
        console.error('加载变化记录失败:', error)
      }
    }

    // 加载统计数据
    const loadStatistics = async () => {
      try {
        const response = await axios.get(`/api/scenes/${sceneId.value}/statistics`)
        statistics.value = response.data.data || {}
        await nextTick()
        renderCharts()
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }

    // 渲染图表
    const renderCharts = () => {
      // 时段分布图表
      if (timeChartRef.value && statistics.value.timeOfDayDistribution) {
        const timeChart = echarts.init(timeChartRef.value)
        const timeData = Object.entries(statistics.value.timeOfDayDistribution).map(([name, value]) => ({
          name, value
        }))
        timeChart.setOption({
          tooltip: { trigger: 'item' },
          series: [{
            type: 'pie',
            radius: '60%',
            data: timeData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        })
      }

      // 天气分布图表
      if (weatherChartRef.value && statistics.value.weatherDistribution) {
        const weatherChart = echarts.init(weatherChartRef.value)
        const weatherData = Object.entries(statistics.value.weatherDistribution).map(([name, value]) => ({
          name, value
        }))
        weatherChart.setOption({
          tooltip: { trigger: 'item' },
          series: [{
            type: 'pie',
            radius: '60%',
            data: weatherData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        })
      }
    }

    // 添加使用记录
    const showAddUsageDialog = () => {
      usageForm.value = {
        chapterId: null,
        timeOfDay: '',
        weather: '',
        sceneState: ''
      }
      showUsageDialog.value = true
    }

    const addUsageRecord = async () => {
      if (!usageForm.value.chapterId) {
        alert('请输入章节ID')
        return
      }

      try {
        await axios.post('/api/scenes/usages', {
          sceneId: sceneId.value,
          ...usageForm.value
        })
        showUsageDialog.value = false
        await loadUsageRecords()
        await loadStatistics()
        alert('添加使用记录成功')
      } catch (error) {
        console.error('添加使用记录失败:', error)
        alert('添加使用记录失败')
      }
    }

    // 删除使用记录
    const deleteUsage = async (usageId) => {
      if (!confirm('确定要删除这条使用记录吗？')) return

      try {
        await axios.delete(`/api/scenes/usages/${usageId}`)
        await loadUsageRecords()
        await loadStatistics()
        alert('删除成功')
      } catch (error) {
        console.error('删除使用记录失败:', error)
        alert('删除失败')
      }
    }

    // 添加变化记录
    const showAddChangeDialog = () => {
      changeForm.value = {
        changeType: '',
        changeDesc: '',
        beforeState: '',
        afterState: '',
        relatedChapterId: null
      }
      showChangeDialog.value = true
    }

    const addChangeRecord = async () => {
      if (!changeForm.value.changeType || !changeForm.value.changeDesc) {
        alert('请填写变化类型和描述')
        return
      }

      try {
        await axios.post('/api/scenes/changes', {
          sceneId: sceneId.value,
          ...changeForm.value
        })
        showChangeDialog.value = false
        await loadChangeRecords()
        await loadStatistics()
        alert('添加变化记录成功')
      } catch (error) {
        console.error('添加变化记录失败:', error)
        alert('添加变化记录失败')
      }
    }

    // 删除变化记录
    const deleteChange = async (changeId) => {
      if (!confirm('确定要删除这条变化记录吗？')) return

      try {
        await axios.delete(`/api/scenes/changes/${changeId}`)
        await loadChangeRecords()
        await loadStatistics()
        alert('删除成功')
      } catch (error) {
        console.error('删除变化记录失败:', error)
        alert('删除失败')
      }
    }

    // 编辑场景
    const editScene = () => {
      router.push(`/novel/${sceneDetail.value.novelId}/scenes`)
    }

    // 返回
    const goBack = () => {
      router.back()
    }

    // 工具函数
    const parseJsonArray = (jsonStr) => {
      if (!jsonStr) return []
      try {
        return JSON.parse(jsonStr)
      } catch {
        return []
      }
    }

    const getSceneTypeLabel = (type) => {
      const labels = {
        INDOOR: '室内',
        OUTDOOR: '室外',
        SPECIAL: '特殊',
        VIRTUAL: '虚拟'
      }
      return labels[type] || type
    }

    const getChangeTypeLabel = (type) => {
      const labels = {
        REBUILD: '重建',
        DAMAGE: '损坏',
        SEASONAL: '季节变化',
        DECORATION: '装饰变化',
        EXPANSION: '扩建',
        OTHER: '其他'
      }
      return labels[type] || type
    }

    const getChangeTypeClass = (type) => {
      const classes = {
        REBUILD: 'type-rebuild',
        DAMAGE: 'type-damage',
        SEASONAL: 'type-seasonal',
        DECORATION: 'type-decoration',
        EXPANSION: 'type-expansion',
        OTHER: 'type-other'
      }
      return classes[type] || 'type-other'
    }

    const formatDateTime = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }

    // 初始化
    onMounted(async () => {
      await loadSceneDetail()
      await loadUsageRecords()
      await loadChangeRecords()
      await loadStatistics()
    })

    return {
      tabs,
      activeTab,
      sceneName,
      sceneDetail,
      usageRecords,
      changeRecords,
      statistics,
      showUsageDialog,
      usageForm,
      showChangeDialog,
      changeForm,
      timeChartRef,
      weatherChartRef,
      showAddUsageDialog,
      addUsageRecord,
      deleteUsage,
      showAddChangeDialog,
      addChangeRecord,
      deleteChange,
      editScene,
      goBack,
      parseJsonArray,
      getSceneTypeLabel,
      getChangeTypeLabel,
      getChangeTypeClass,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.scene-management-panel {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 20px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 24px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 14px;
}

.btn-back {
  padding: 10px 20px;
  background: #fff;
  border: 2px solid #667eea;
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}

.btn-back:hover {
  background: #667eea;
  color: #fff;
}

/* 标签页 */
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.tab-item {
  flex: 1;
  padding: 15px;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: #495057;
}

.tab-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 标签页内容 */
.tab-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 30px;
  min-height: 500px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 场景详情 */
.detail-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f1f3f5;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
}

.btn-edit {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-edit:hover {
  background: #5568d3;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  color: #6c757d;
  font-size: 13px;
  font-weight: 500;
}

.info-item .value {
  color: #212529;
  font-size: 14px;
}

.importance-stars {
  display: flex;
  gap: 2px;
}

.star {
  color: #dee2e6;
  font-size: 18px;
}

.star.active {
  color: #ffc107;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 12px;
  background: #e7f3ff;
  color: #0066cc;
  border-radius: 12px;
  font-size: 12px;
}

.tag.character-tag {
  background: #fff3e0;
  color: #e65100;
}

.tag.chapter-tag {
  background: #f3e5f5;
  color: #7b1fa2;
}

.empty {
  color: #adb5bd;
  font-style: italic;
  font-size: 13px;
}

.notes-content {
  color: #495057;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 使用历史 */
.usage-container,
.changes-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.actions-bar {
  display: flex;
  justify-content: flex-end;
}

.btn-primary {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-state .icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state p {
  margin: 10px 0;
  font-size: 16px;
}

.empty-state .hint {
  font-size: 14px;
  color: #adb5bd;
}

.btn-secondary {
  padding: 10px 20px;
  background: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  margin-top: 20px;
}

.btn-secondary:hover {
  background: #e9ecef;
}

/* 时间轴 */
.usage-timeline {
  position: relative;
  padding-left: 40px;
}

.timeline-item {
  position: relative;
  padding-bottom: 30px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -40px;
  top: 8px;
  width: 12px;
  height: 12px;
  background: #667eea;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -35px;
  top: 20px;
  bottom: -10px;
  width: 2px;
  background: #e9ecef;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-content {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
}

.usage-card,
.change-card {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.usage-header,
.change-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f3f5;
}

.usage-time,
.change-time {
  color: #6c757d;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-delete {
  padding: 4px 12px;
  background: #fff;
  color: #dc3545;
  border: 1px solid #dc3545;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-delete:hover {
  background: #dc3545;
  color: #fff;
}

.usage-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.usage-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.usage-item.full-width {
  grid-column: 1 / -1;
}

.usage-item .label {
  color: #6c757d;
  font-size: 12px;
}

.usage-item .value {
  color: #212529;
  font-size: 13px;
}

/* 变化记录 */
.changes-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.change-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
}

.change-type {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-rebuild { background: #d4edda; color: #155724; }
.type-damage { background: #f8d7da; color: #721c24; }
.type-seasonal { background: #d1ecf1; color: #0c5460; }
.type-decoration { background: #fff3cd; color: #856404; }
.type-expansion { background: #e2e3e5; color: #383d41; }
.type-other { background: #f8f9fa; color: #495057; }

.change-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.change-desc {
  color: #212529;
  font-size: 14px;
  line-height: 1.5;
}

.change-states {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.state-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.state-label {
  color: #6c757d;
  font-size: 12px;
}

.state-value {
  color: #212529;
  font-size: 13px;
}

.state-arrow {
  color: #667eea;
  font-size: 20px;
  font-weight: bold;
}

.change-chapter {
  color: #6c757d;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 统计分析 */
.statistics-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 36px;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-label {
  color: #6c757d;
  font-size: 13px;
  margin-bottom: 5px;
}

.stat-value {
  color: #212529;
  font-size: 24px;
  font-weight: bold;
}

.chart-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
}

.chart-card h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 对话框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #adb5bd;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.btn-close:hover {
  background: #f8f9fa;
  color: #495057;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #495057;
  font-size: 14px;
  font-weight: 500;
}

.required {
  color: #dc3545;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .usage-info {
    grid-template-columns: 1fr;
  }
  
  .change-states {
    flex-direction: column;
  }
}
</style>
