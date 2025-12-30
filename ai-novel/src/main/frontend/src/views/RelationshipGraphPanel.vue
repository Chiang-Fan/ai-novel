<template>
  <div class="relationship-graph-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <div class="header-left">
        <h2>🕸️ 角色关系图谱</h2>
        <span class="subtitle">{{ novelTitle }}</span>
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
      <!-- Tab 1: 关系网络图 -->
      <div v-show="activeTab === 'graph'" class="tab-pane">
        <div class="graph-container">
          <div class="graph-toolbar">
            <div class="toolbar-left">
              <button class="btn-icon" @click="refreshGraph" title="刷新">
                🔄
              </button>
              <button class="btn-icon" @click="resetZoom" title="重置视图">
                🔍
              </button>
            </div>
            <div class="toolbar-right">
              <button class="btn-primary" @click="showAddRelationDialog">
                <i class="icon">+</i> 添加关系
              </button>
            </div>
          </div>

          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>加载关系图谱中...</p>
          </div>

          <div v-else-if="graphData.nodes.length === 0" class="empty-state">
            <i class="icon">🕸️</i>
            <p>暂无关系数据</p>
            <p class="hint">添加角色关系后即可查看图谱</p>
          </div>

          <div v-else ref="graphChartRef" class="graph-chart"></div>

          <!-- 图例 -->
          <div class="graph-legend" v-if="graphData.nodes.length > 0">
            <div class="legend-title">关系类型</div>
            <div class="legend-items">
              <div v-for="type in relationshipTypes" :key="type.value" class="legend-item">
                <span class="legend-line" :style="{ background: type.color }"></span>
                <span class="legend-text">{{ type.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 2: 关系列表 -->
      <div v-show="activeTab === 'list'" class="tab-pane">
        <div class="list-container">
          <div class="actions-bar">
            <div class="search-box">
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="搜索角色或关系..."
                @input="filterRelationships"
              />
            </div>
            <button class="btn-primary" @click="showAddRelationDialog">
              <i class="icon">+</i> 添加关系
            </button>
          </div>

          <div v-if="filteredRelationships.length === 0" class="empty-state">
            <i class="icon">📝</i>
            <p>暂无关系记录</p>
          </div>

          <div v-else class="relationships-grid">
            <div 
              v-for="rel in filteredRelationships" 
              :key="rel.id" 
              class="relationship-card"
            >
              <div class="card-header">
                <div class="relationship-info">
                  <span class="character-name">{{ rel.characterName }}</span>
                  <span class="relationship-arrow">→</span>
                  <span class="character-name">{{ rel.relatedCharacterName }}</span>
                </div>
                <div class="card-actions">
                  <button class="btn-icon-sm" @click="editRelationship(rel)" title="编辑">
                    ✏️
                  </button>
                  <button class="btn-icon-sm" @click="deleteRelationship(rel.id)" title="删除">
                    🗑️
                  </button>
                </div>
              </div>
              <div class="card-body">
                <div class="relationship-type" :class="getTypeClass(rel.relationshipType)">
                  {{ getTypeLabel(rel.relationshipType) }}
                </div>
                <div class="relationship-strength">
                  <span class="label">关系强度:</span>
                  <div class="strength-bar">
                    <div class="strength-fill" :style="{ width: rel.strength + '%' }"></div>
                  </div>
                  <span class="strength-value">{{ rel.strength }}</span>
                </div>
                <div class="relationship-desc" v-if="rel.description">
                  {{ rel.description }}
                </div>
                <button class="btn-history" @click="viewHistory(rel)">
                  查看演变历史
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 3: 关系演变 -->
      <div v-show="activeTab === 'history'" class="tab-pane">
        <div class="history-container">
          <div class="relationship-selector">
            <label>选择关系:</label>
            <select v-model="selectedRelationshipId" @change="loadHistoryForRelationship">
              <option value="">请选择</option>
              <option v-for="rel in allRelationships" :key="rel.id" :value="rel.id">
                {{ rel.characterName }} → {{ rel.relatedCharacterName }}
              </option>
            </select>
          </div>

          <div v-if="!selectedRelationshipId" class="empty-state">
            <i class="icon">📊</i>
            <p>请选择一个关系查看演变历史</p>
          </div>

          <div v-else-if="relationshipHistory.length === 0" class="empty-state">
            <i class="icon">📋</i>
            <p>该关系暂无历史记录</p>
            <button class="btn-secondary" @click="showAddEventDialog">添加事件</button>
          </div>

          <div v-else class="history-content">
            <div class="history-toolbar">
              <button class="btn-primary" @click="showAddEventDialog">
                <i class="icon">+</i> 添加事件
              </button>
            </div>

            <!-- 强度趋势图 -->
            <div class="chart-card">
              <h3>关系强度趋势</h3>
              <div ref="trendChartRef" class="trend-chart"></div>
            </div>

            <!-- 历史时间轴 -->
            <div class="history-timeline">
              <div v-for="(event, index) in relationshipHistory" :key="event.id" class="timeline-item">
                <div class="timeline-dot" :class="getEventTypeClass(event.changeType)"></div>
                <div class="timeline-content">
                  <div class="event-card">
                    <div class="event-header">
                      <div class="event-type" :class="getEventTypeClass(event.changeType)">
                        {{ getEventTypeLabel(event.changeType) }}
                      </div>
                      <div class="event-time">{{ formatDateTime(event.changeTime) }}</div>
                      <button class="btn-delete-sm" @click="deleteHistoryEvent(event.id)">删除</button>
                    </div>
                    <div class="event-body">
                      <div class="event-desc" v-if="event.eventDesc">
                        {{ event.eventDesc }}
                      </div>
                      <div class="event-changes">
                        <div v-if="event.oldStrength !== null && event.newStrength !== null" class="change-item">
                          <span class="change-label">强度变化:</span>
                          <span class="change-value">
                            {{ event.oldStrength }} → {{ event.newStrength }}
                            <span :class="event.newStrength > event.oldStrength ? 'trend-up' : 'trend-down'">
                              {{ event.newStrength > event.oldStrength ? '↑' : '↓' }}
                            </span>
                          </span>
                        </div>
                        <div v-if="event.oldType && event.newType" class="change-item">
                          <span class="change-label">类型变化:</span>
                          <span class="change-value">
                            {{ getTypeLabel(event.oldType) }} → {{ getTypeLabel(event.newType) }}
                          </span>
                        </div>
                        <div v-if="event.relatedChapterId" class="change-item">
                          <span class="change-label">关联章节:</span>
                          <span class="change-value">第 {{ event.relatedChapterId }} 章</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加关系对话框 -->
    <div v-if="showRelationDialog" class="modal-overlay" @click.self="showRelationDialog = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3>{{ editingRelation ? '编辑关系' : '添加关系' }}</h3>
          <button class="btn-close" @click="showRelationDialog = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>角色 <span class="required">*</span></label>
            <select v-model="relationForm.characterId" :disabled="editingRelation">
              <option value="">请选择</option>
              <option v-for="char in characters" :key="char.id" :value="char.id">
                {{ char.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>相关角色 <span class="required">*</span></label>
            <select v-model="relationForm.relatedCharacterId" :disabled="editingRelation">
              <option value="">请选择</option>
              <option v-for="char in characters" :key="char.id" :value="char.id" 
                :disabled="char.id === relationForm.characterId">
                {{ char.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>关系类型 <span class="required">*</span></label>
            <select v-model="relationForm.relationshipType">
              <option value="">请选择</option>
              <option v-for="type in relationshipTypes" :key="type.value" :value="type.value">
                {{ type.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>关系强度 (0-100) <span class="required">*</span></label>
            <input v-model.number="relationForm.strength" type="range" min="0" max="100" />
            <span class="strength-display">{{ relationForm.strength }}</span>
          </div>
          <div class="form-group">
            <label>关系描述</label>
            <textarea v-model="relationForm.description" rows="3" 
              placeholder="描述两个角色之间的关系..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showRelationDialog = false">取消</button>
          <button class="btn-primary" @click="saveRelationship">确定</button>
        </div>
      </div>
    </div>

    <!-- 添加事件对话框 -->
    <div v-if="showEventDialog" class="modal-overlay" @click.self="showEventDialog = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3>添加关系事件</h3>
          <button class="btn-close" @click="showEventDialog = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>事件类型 <span class="required">*</span></label>
            <select v-model="eventForm.changeType">
              <option value="EVENT">事件</option>
              <option value="STRENGTH_CHANGE">强度变化</option>
              <option value="TYPE_CHANGE">类型变化</option>
            </select>
          </div>
          <div class="form-group">
            <label>事件描述 <span class="required">*</span></label>
            <textarea v-model="eventForm.eventDesc" rows="3" 
              placeholder="描述发生的事件..."></textarea>
          </div>
          <div class="form-group" v-if="eventForm.changeType === 'STRENGTH_CHANGE' || eventForm.changeType === 'EVENT'">
            <label>新强度值</label>
            <input v-model.number="eventForm.newStrength" type="range" min="0" max="100" />
            <span class="strength-display">{{ eventForm.newStrength }}</span>
          </div>
          <div class="form-group">
            <label>关联章节ID</label>
            <input v-model.number="eventForm.relatedChapterId" type="number" placeholder="请输入章节ID" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showEventDialog = false">取消</button>
          <button class="btn-primary" @click="addEvent">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'RelationshipGraphPanel',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const novelId = ref(route.params.novelId)
    const novelTitle = ref('加载中...')

    // 标签页
    const tabs = [
      { key: 'graph', label: '关系网络图', icon: '🌐' },
      { key: 'list', label: '关系列表', icon: '📋' },
      { key: 'history', label: '关系演变', icon: '📊' }
    ]
    const activeTab = ref('graph')

    // 数据
    const loading = ref(false)
    const graphData = ref({ nodes: [], edges: [] })
    const characters = ref([])
    const allRelationships = ref([])
    const filteredRelationships = ref([])
    const searchKeyword = ref('')

    // 图表引用
    const graphChartRef = ref(null)
    const trendChartRef = ref(null)
    let graphChart = null
    let trendChart = null

    // 关系对话框
    const showRelationDialog = ref(false)
    const editingRelation = ref(null)
    const relationForm = ref({
      characterId: '',
      relatedCharacterId: '',
      relationshipType: '',
      strength: 50,
      description: ''
    })

    // 历史相关
    const selectedRelationshipId = ref('')
    const relationshipHistory = ref([])
    const showEventDialog = ref(false)
    const eventForm = ref({
      changeType: 'EVENT',
      eventDesc: '',
      newStrength: 50,
      relatedChapterId: null
    })

    // 关系类型配置
    const relationshipTypes = [
      { value: 'FAMILY', label: '亲情', color: '#ff6b6b' },
      { value: 'FRIENDSHIP', label: '友情', color: '#4ecdc4' },
      { value: 'LOVE', label: '爱情', color: '#ff69b4' },
      { value: 'HOSTILITY', label: '敌对', color: '#ff4757' },
      { value: 'COOPERATION', label: '合作', color: '#5f27cd' },
      { value: 'MENTOR', label: '师徒', color: '#48dbfb' },
      { value: 'RIVALRY', label: '竞争', color: '#ff9ff3' },
      { value: 'OTHER', label: '其他', color: '#b2bec3' }
    ]

    // 加载关系图谱
    const loadGraph = async () => {
      loading.value = true
      try {
        const response = await axios.get(`/api/relationships/novel/${novelId.value}/graph`)
        graphData.value = response.data.data || { nodes: [], edges: [] }
        await nextTick()
        renderGraph()
      } catch (error) {
        console.error('加载关系图谱失败:', error)
        alert('加载关系图谱失败')
      } finally {
        loading.value = false
      }
    }

    // 加载角色列表
    const loadCharacters = async () => {
      try {
        const response = await axios.get(`/api/characters?novelId=${novelId.value}`)
        characters.value = response.data.data || []
      } catch (error) {
        console.error('加载角色列表失败:', error)
      }
    }

    // 加载关系列表
    const loadRelationships = async () => {
      try {
        // 这里简化处理，通过图谱数据获取所有关系
        await loadGraph()
        // 将edges转换为关系列表
        allRelationships.value = graphData.value.edges.map(edge => {
          const sourceNode = graphData.value.nodes.find(n => n.id === edge.source)
          const targetNode = graphData.value.nodes.find(n => n.id === edge.target)
          return {
            id: edge.id,
            characterId: edge.source,
            characterName: sourceNode?.name || '未知',
            relatedCharacterId: edge.target,
            relatedCharacterName: targetNode?.name || '未知',
            relationshipType: edge.type,
            strength: edge.strength,
            description: edge.description
          }
        })
        filteredRelationships.value = allRelationships.value
      } catch (error) {
        console.error('加载关系列表失败:', error)
      }
    }

    // 渲染关系网络图
    const renderGraph = () => {
      if (!graphChartRef.value || graphData.value.nodes.length === 0) return

      if (graphChart) {
        graphChart.dispose()
      }

      graphChart = echarts.init(graphChartRef.value)

      const option = {
        title: {
          text: '角色关系网络图',
          left: 'center',
          top: 20,
          textStyle: {
            fontSize: 18,
            color: '#2c3e50'
          }
        },
        tooltip: {
          formatter: (params) => {
            if (params.dataType === 'node') {
              return `${params.data.name}<br/>类型: ${params.data.roleType}<br/>重要性: ${params.data.importance}`
            } else if (params.dataType === 'edge') {
              const sourceNode = graphData.value.nodes.find(n => n.id === params.data.source)
              const targetNode = graphData.value.nodes.find(n => n.id === params.data.target)
              return `${sourceNode?.name} → ${targetNode?.name}<br/>关系: ${getTypeLabel(params.data.type)}<br/>强度: ${params.data.strength}`
            }
            return ''
          }
        },
        series: [{
          type: 'graph',
          layout: 'force',
          data: graphData.value.nodes.map(node => ({
            ...node,
            symbolSize: Math.max(20, node.importance / 2),
            label: {
              show: true,
              formatter: '{b}'
            }
          })),
          edges: graphData.value.edges.map(edge => ({
            ...edge,
            lineStyle: {
              color: getTypeColor(edge.type),
              width: Math.max(1, edge.strength / 20),
              curveness: 0.2
            }
          })),
          roam: true,
          force: {
            repulsion: 1000,
            edgeLength: [100, 200],
            gravity: 0.1
          },
          emphasis: {
            focus: 'adjacency',
            lineStyle: {
              width: 5
            }
          }
        }]
      }

      graphChart.setOption(option)
    }

    // 渲染趋势图
    const renderTrendChart = () => {
      if (!trendChartRef.value || relationshipHistory.value.length === 0) return

      if (trendChart) {
        trendChart.dispose()
      }

      trendChart = echarts.init(trendChartRef.value)

      const data = relationshipHistory.value
        .filter(h => h.newStrength !== null)
        .reverse()
        .map(h => ({
          time: new Date(h.changeTime).toLocaleDateString(),
          value: h.newStrength
        }))

      const option = {
        xAxis: {
          type: 'category',
          data: data.map(d => d.time)
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100
        },
        series: [{
          data: data.map(d => d.value),
          type: 'line',
          smooth: true,
          areaStyle: {
            opacity: 0.3
          },
          itemStyle: {
            color: '#667eea'
          }
        }],
        tooltip: {
          trigger: 'axis'
        }
      }

      trendChart.setOption(option)
    }

    // 过滤关系列表
    const filterRelationships = () => {
      if (!searchKeyword.value) {
        filteredRelationships.value = allRelationships.value
      } else {
        const keyword = searchKeyword.value.toLowerCase()
        filteredRelationships.value = allRelationships.value.filter(rel =>
          rel.characterName.toLowerCase().includes(keyword) ||
          rel.relatedCharacterName.toLowerCase().includes(keyword) ||
          getTypeLabel(rel.relationshipType).includes(keyword)
        )
      }
    }

    // 加载关系历史
    const loadHistoryForRelationship = async () => {
      if (!selectedRelationshipId.value) {
        relationshipHistory.value = []
        return
      }

      try {
        const response = await axios.get(`/api/relationships/${selectedRelationshipId.value}/history`)
        relationshipHistory.value = response.data.data || []
        await nextTick()
        renderTrendChart()
      } catch (error) {
        console.error('加载关系历史失败:', error)
      }
    }

    // 显示添加关系对话框
    const showAddRelationDialog = () => {
      editingRelation.value = null
      relationForm.value = {
        characterId: '',
        relatedCharacterId: '',
        relationshipType: '',
        strength: 50,
        description: ''
      }
      showRelationDialog.value = true
    }

    // 编辑关系
    const editRelationship = (rel) => {
      editingRelation.value = rel
      relationForm.value = {
        characterId: rel.characterId,
        relatedCharacterId: rel.relatedCharacterId,
        relationshipType: rel.relationshipType,
        strength: rel.strength,
        description: rel.description
      }
      showRelationDialog.value = true
    }

    // 保存关系
    const saveRelationship = async () => {
      if (!relationForm.value.characterId || !relationForm.value.relatedCharacterId || !relationForm.value.relationshipType) {
        alert('请填写必填项')
        return
      }

      try {
        if (editingRelation.value) {
          await axios.put(`/api/relationships/${editingRelation.value.id}`, relationForm.value)
        } else {
          await axios.post('/api/relationships', relationForm.value)
        }
        showRelationDialog.value = false
        await loadGraph()
        await loadRelationships()
        alert('保存成功')
      } catch (error) {
        console.error('保存关系失败:', error)
        alert('保存失败: ' + (error.response?.data?.message || error.message))
      }
    }

    // 删除关系
    const deleteRelationship = async (id) => {
      if (!confirm('确定要删除这个关系吗？')) return

      try {
        await axios.delete(`/api/relationships/${id}`)
        await loadGraph()
        await loadRelationships()
        alert('删除成功')
      } catch (error) {
        console.error('删除关系失败:', error)
        alert('删除失败')
      }
    }

    // 显示添加事件对话框
    const showAddEventDialog = () => {
      eventForm.value = {
        changeType: 'EVENT',
        eventDesc: '',
        newStrength: 50,
        relatedChapterId: null
      }
      showEventDialog.value = true
    }

    // 添加事件
    const addEvent = async () => {
      if (!eventForm.value.eventDesc) {
        alert('请填写事件描述')
        return
      }

      try {
        await axios.post('/api/relationships/history', {
          relationshipId: selectedRelationshipId.value,
          ...eventForm.value
        })
        showEventDialog.value = false
        await loadHistoryForRelationship()
        alert('添加事件成功')
      } catch (error) {
        console.error('添加事件失败:', error)
        alert('添加事件失败')
      }
    }

    // 删除历史事件
    const deleteHistoryEvent = async (historyId) => {
      if (!confirm('确定要删除这条历史记录吗？')) return

      try {
        await axios.delete(`/api/relationships/history/${historyId}`)
        await loadHistoryForRelationship()
        alert('删除成功')
      } catch (error) {
        console.error('删除历史失败:', error)
        alert('删除失败')
      }
    }

    // 查看历史
    const viewHistory = (rel) => {
      activeTab.value = 'history'
      selectedRelationshipId.value = rel.id
      nextTick(() => {
        loadHistoryForRelationship()
      })
    }

    // 工具函数
    const getTypeLabel = (type) => {
      const found = relationshipTypes.find(t => t.value === type)
      return found ? found.label : type
    }

    const getTypeColor = (type) => {
      const found = relationshipTypes.find(t => t.value === type)
      return found ? found.color : '#b2bec3'
    }

    const getTypeClass = (type) => {
      return `type-${type.toLowerCase()}`
    }

    const getEventTypeLabel = (type) => {
      const labels = {
        CREATE: '创建',
        DELETE: '删除',
        STRENGTH_CHANGE: '强度变化',
        TYPE_CHANGE: '类型变化',
        EVENT: '事件',
        MULTIPLE_CHANGE: '多项变化'
      }
      return labels[type] || type
    }

    const getEventTypeClass = (type) => {
      return `event-${type.toLowerCase().replace('_', '-')}`
    }

    const formatDateTime = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }

    const refreshGraph = () => {
      loadGraph()
    }

    const resetZoom = () => {
      if (graphChart) {
        graphChart.setOption({
          series: [{
            zoom: 1,
            center: [0, 0]
          }]
        })
      }
    }

    const goBack = () => {
      router.push(`/novel/${novelId.value}`)
    }

    // 初始化
    onMounted(async () => {
      await loadCharacters()
      await loadGraph()
      await loadRelationships()
    })

    return {
      tabs,
      activeTab,
      loading,
      novelTitle,
      graphData,
      characters,
      allRelationships,
      filteredRelationships,
      searchKeyword,
      relationshipTypes,
      graphChartRef,
      trendChartRef,
      showRelationDialog,
      editingRelation,
      relationForm,
      selectedRelationshipId,
      relationshipHistory,
      showEventDialog,
      eventForm,
      loadGraph,
      filterRelationships,
      loadHistoryForRelationship,
      showAddRelationDialog,
      editRelationship,
      saveRelationship,
      deleteRelationship,
      showAddEventDialog,
      addEvent,
      deleteHistoryEvent,
      viewHistory,
      getTypeLabel,
      getTypeColor,
      getTypeClass,
      getEventTypeLabel,
      getEventTypeClass,
      formatDateTime,
      refreshGraph,
      resetZoom,
      goBack
    }
  }
}
</script>

<style scoped>
.relationship-graph-panel {
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
  min-height: 600px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 图表容器 */
.graph-container {
  position: relative;
  height: 100%;
}

.graph-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 10px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border: none;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s;
}

.btn-icon:hover {
  background: #e9ecef;
  transform: scale(1.1);
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

.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #6c757d;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

.graph-chart {
  width: 100%;
  height: 600px;
}

.graph-legend {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.legend-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #2c3e50;
}

.legend-items {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-line {
  width: 30px;
  height: 3px;
  border-radius: 2px;
}

.legend-text {
  font-size: 13px;
  color: #495057;
}

/* 列表容器 */
.list-container,
.history-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box input {
  width: 300px;
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 14px;
}

.relationships-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.relationship-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.relationship-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.relationship-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.character-name {
  font-weight: bold;
  color: #2c3e50;
}

.relationship-arrow {
  color: #667eea;
  font-weight: bold;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.btn-icon-sm {
  padding: 4px 8px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  opacity: 0.7;
  transition: all 0.3s;
}

.btn-icon-sm:hover {
  opacity: 1;
  transform: scale(1.2);
}

.card-body {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.relationship-type {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-family { background: #ffe5e5; color: #c92a2a; }
.type-friendship { background: #e0f7fa; color: #00838f; }
.type-love { background: #fce4ec; color: #c2185b; }
.type-hostility { background: #ffebee; color: #b71c1c; }
.type-cooperation { background: #ede7f6; color: #4527a0; }
.type-mentor { background: #e1f5fe; color: #01579b; }
.type-rivalry { background: #fff3e0; color: #e65100; }
.type-other { background: #f5f5f5; color: #616161; }

.relationship-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.relationship-strength .label {
  color: #6c757d;
  min-width: 70px;
}

.strength-bar {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
}

.strength-value {
  font-weight: bold;
  color: #667eea;
  min-width: 30px;
  text-align: right;
}

.relationship-desc {
  color: #495057;
  font-size: 13px;
  line-height: 1.5;
}

.btn-history {
  margin-top: 8px;
  padding: 8px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  color: #495057;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-history:hover {
  background: #e9ecef;
}

/* 历史容器 */
.relationship-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.relationship-selector label {
  font-weight: 500;
  color: #495057;
}

.relationship-selector select {
  flex: 1;
  padding: 10px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
}

.history-toolbar {
  display: flex;
  justify-content: flex-end;
}

.chart-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.chart-card h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.trend-chart {
  width: 100%;
  height: 300px;
}

.history-timeline {
  position: relative;
  padding-left: 40px;
}

.timeline-item {
  position: relative;
  padding-bottom: 30px;
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

.event-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f3f5;
  margin-bottom: 10px;
}

.event-type {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.event-create { background: #d4edda; color: #155724; }
.event-delete { background: #f8d7da; color: #721c24; }
.event-strength-change { background: #d1ecf1; color: #0c5460; }
.event-type-change { background: #fff3cd; color: #856404; }
.event-event { background: #e2e3e5; color: #383d41; }
.event-multiple-change { background: #f3e5f5; color: #7b1fa2; }

.event-time {
  font-size: 13px;
  color: #6c757d;
}

.btn-delete-sm {
  padding: 4px 8px;
  background: #fff;
  color: #dc3545;
  border: 1px solid #dc3545;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-delete-sm:hover {
  background: #dc3545;
  color: #fff;
}

.event-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.event-desc {
  color: #212529;
  font-size: 14px;
  line-height: 1.5;
}

.event-changes {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.change-item {
  display: flex;
  gap: 10px;
  font-size: 13px;
}

.change-label {
  color: #6c757d;
  min-width: 80px;
}

.change-value {
  color: #212529;
  font-weight: 500;
}

.trend-up {
  color: #28a745;
  font-weight: bold;
}

.trend-down {
  color: #dc3545;
  font-weight: bold;
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

.form-group input[type="text"],
.form-group input[type="number"],
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

.form-group input[type="range"] {
  width: calc(100% - 50px);
  margin-right: 10px;
}

.strength-display {
  display: inline-block;
  min-width: 30px;
  font-weight: bold;
  color: #667eea;
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

.btn-secondary {
  padding: 10px 20px;
  background: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: #e9ecef;
}

/* 响应式 */
@media (max-width: 768px) {
  .relationships-grid {
    grid-template-columns: 1fr;
  }
  
  .search-box input {
    width: 100%;
  }
  
  .actions-bar {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
