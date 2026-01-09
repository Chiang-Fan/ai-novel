<template>
  <div class="smart-novel-creation">
    <div class="header">
      <h1>智能新建小说</h1>
      <p>让我们一步步帮您完善小说框架</p>
    </div>

    <div class="creation-container" v-if="!isComplete">
      <!-- 显示当前阶段 -->
      <div class="stage-indicator">
        <span :class="{ active: currentStage === 'IDEA' }">创意</span>
        <span :class="{ active: currentStage === 'GENRE' }">类型</span>
        <span :class="{ active: currentStage === 'OUTLINE' }">大纲</span>
        <span :class="{ active: currentStage === 'CHARACTERS' }">角色</span>
        <span :class="{ active: currentStage === 'WORLD_SETTING' }">世界观</span>
      </div>

      <!-- 问题卡片 -->
      <div class="question-card">
        <h3>{{ currentQuestion }}</h3>
        
        <!-- 显示AI提供的建议选项 -->
        <div class="suggestions" v-if="suggestions && suggestions.length > 0">
          <div 
            class="suggestion-item" 
            v-for="(suggestion, index) in suggestions" 
            :key="index"
            @click="selectSuggestion(suggestion)"
          >
            {{ suggestion }}
          </div>
        </div>

        <!-- 或手动输入 -->
        <div class="manual-input">
          <textarea 
            v-model="manualInput" 
            placeholder="或者您可以手动输入自己的想法..."
            rows="4"
          ></textarea>
        </div>

        <!-- 下一步按钮 -->
        <div class="actions">
          <button 
            class="btn-primary" 
            @click="submitResponse"
            :disabled="!canSubmit"
          >
            {{ getNextButtonText() }}
          </button>
        </div>
      </div>
    </div>

    <!-- 完成页面 -->
    <div class="completion-container" v-else>
      <div class="success-card">
        <h2>🎉 小说创建完成！</h2>
        <div class="novel-info">
          <p><strong>标题：</strong>{{ novelInfo.title }}</p>
          <p><strong>类型：</strong>{{ novelInfo.genre }}</p>
          <p><strong>简介：</strong>{{ novelInfo.description }}</p>
        </div>
        <div class="actions">
          <button class="btn-primary" @click="viewNovel">查看小说</button>
          <button class="btn-secondary" @click="startNewNovel">创建新小说</button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-overlay" v-if="isLoading">
      <div class="loading-spinner">
        <span>AI正在思考...</span>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'SmartNovelCreation',
  data() {
    return {
      sessionId: null,
      currentQuestion: '',
      suggestions: [],
      manualInput: '',
      currentStage: 'IDEA',
      isComplete: false,
      isLoading: false,
      novelInfo: {
        title: '',
        genre: '',
        description: '',
        id: null
      }
    }
  },
  computed: {
    canSubmit() {
      return this.manualInput.trim() !== '' || this.selectedSuggestion !== null
    }
  },
  data() {
    return {
      sessionId: null,
      currentQuestion: '',
      suggestions: [],
      manualInput: '',
      currentStage: 'IDEA',
      isComplete: false,
      isLoading: false,
      novelInfo: {
        title: '',
        genre: '',
        description: '',
        id: null
      },
      selectedSuggestion: null,
      // 用于存储每个阶段的用户选择
      stageData: {
        initialIdea: '',
        genre: '',
        outline: '',
        characters: '',
        worldSetting: ''
      }
    }
  },
  async mounted() {
    await this.startCreation()
  },
  methods: {
    async startCreation() {
      this.isLoading = true
      try {
        const response = await api.smartNovelCreation.start({
          initialIdea: '用户想要创建一部新的小说',
          genre: '',
          targetAudience: '青少年',
          writingStyle: '轻松幽默',
          currentStage: 'IDEA'
        })

        this.sessionId = response.context?.sessionId
        this.currentQuestion = response.question
        this.suggestions = response.suggestions || []
        this.currentStage = response.currentStage
        this.isComplete = response.isComplete || false
      } catch (error) {
        console.error('启动创作会话失败:', error)
        alert('启动创作会话失败: ' + error.message)
      } finally {
        this.isLoading = false
      }
    },
    
    selectSuggestion(suggestion) {
      this.selectedSuggestion = suggestion
      this.manualInput = suggestion // 也可以将选中的建议填入输入框
    },

    async submitResponse() {
      if (!this.canSubmit) {
        return
      }

      this.isLoading = true
      try {
        // 根据当前阶段准备数据
        let requestData = {
          initialIdea: this.selectedSuggestion || this.manualInput,
          genre: this.stageData.genre,
          targetAudience: '青少年',
          writingStyle: '轻松幽默',
          currentStage: this.currentStage
        }
        
        // 根据当前阶段更新数据
        switch(this.currentStage) {
          case 'IDEA':
            requestData.initialIdea = this.selectedSuggestion || this.manualInput
            this.stageData.initialIdea = this.selectedSuggestion || this.manualInput
            break
          case 'GENRE':
            requestData.genre = this.selectedSuggestion || this.manualInput
            this.stageData.genre = this.selectedSuggestion || this.manualInput
            break
          case 'OUTLINE':
            requestData.initialIdea = this.selectedSuggestion || this.manualInput
            this.stageData.outline = this.selectedSuggestion || this.manualInput
            break
          case 'CHARACTERS':
            requestData.initialIdea = this.selectedSuggestion || this.manualInput
            this.stageData.characters = this.selectedSuggestion || this.manualInput
            break
          case 'WORLD_SETTING':
            requestData.initialIdea = this.selectedSuggestion || this.manualInput
            this.stageData.worldSetting = this.selectedSuggestion || this.manualInput
            break
        }
        
        const response = await api.smartNovelCreation.processReply(requestData, this.sessionId)

        this.currentQuestion = response.question
        this.suggestions = response.suggestions || []
        this.currentStage = response.currentStage
        this.isComplete = response.isComplete || false

        // 如果完成，保存小说信息
        if (this.isComplete && response.novelId) {
          this.novelInfo.id = response.novelId
          // 提取小说信息
          if (response.suggestions && response.suggestions.length > 0) {
            const titleMatch = response.suggestions.find(s => s.includes('标题：'))
            if (titleMatch) {
              this.novelInfo.title = titleMatch.replace('标题：', '').trim()
            }
            
            const genreMatch = response.suggestions.find(s => s.includes('类型：'))
            if (genreMatch) {
              this.novelInfo.genre = genreMatch.replace('类型：', '').trim()
            }
            
            const descMatch = response.suggestions.find(s => s.includes('简介：'))
            if (descMatch) {
              this.novelInfo.description = descMatch.replace('简介：', '').trim()
            }
          }
        }

        // 重置选择
        this.manualInput = ''
        this.selectedSuggestion = null
        
        // 如果是特定阶段，预填充输入框
        if (this.currentStage === 'OUTLINE' && this.stageData.outline) {
          this.manualInput = this.stageData.outline
        } else if (this.currentStage === 'CHARACTERS' && this.stageData.characters) {
          this.manualInput = this.stageData.characters
        } else if (this.currentStage === 'WORLD_SETTING' && this.stageData.worldSetting) {
          this.manualInput = this.stageData.worldSetting
        }
      } catch (error) {
        console.error('提交响应失败:', error)
        alert('提交响应失败: ' + error.message)
      } finally {
        this.isLoading = false
      }
    },

    getNextButtonText() {
      if (this.currentStage === 'WORLD_SETTING') {
        return '完成创作'
      }
      return '下一步'
    },

    viewNovel() {
      if (this.novelInfo.id) {
        this.$router.push(`/novel/${this.novelInfo.id}`)
      }
    },

    startNewNovel() {
      this.resetSession()
      setTimeout(() => {
        this.startCreation()
      }, 100)
    },

    async resetSession() {
      if (this.sessionId) {
        try {
          await api.smartNovelCreation.reset(this.sessionId)
        } catch (error) {
          console.error('重置会话失败:', error)
        }
      }
      
      // 重置本地状态
      this.sessionId = null
      this.currentQuestion = ''
      this.suggestions = []
      this.manualInput = ''
      this.currentStage = 'IDEA'
      this.isComplete = false
      this.novelInfo = {
        title: '',
        genre: '',
        description: '',
        id: null
      }
      this.selectedSuggestion = null
    }
  }
}
</script>

<style scoped>
.smart-novel-creation {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.header p {
  color: #7f8c8d;
}

.stage-indicator {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  position: relative;
}

.stage-indicator::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 0;
  right: 0;
  height: 2px;
  background: #ecf0f1;
  z-index: 1;
}

.stage-indicator span {
  background: white;
  padding: 5px 15px;
  border-radius: 20px;
  border: 2px solid #ecf0f1;
  z-index: 2;
  position: relative;
  cursor: default;
  transition: all 0.3s ease;
}

.stage-indicator span.active {
  border-color: #3498db;
  background: #e1f0fa;
  font-weight: bold;
}

.question-card {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.question-card h3 {
  margin-top: 0;
  color: #2c3e50;
  font-size: 1.2em;
  line-height: 1.5;
}

.suggestions {
  margin: 20px 0;
}

.suggestion-item {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
  margin: 10px 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.suggestion-item:hover {
  background: #e3f2fd;
  border-color: #bbdefb;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.manual-input {
  margin: 20px 0;
}

.manual-input textarea {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  font-size: 1em;
}

.actions {
  text-align: center;
  margin-top: 20px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  cursor: pointer;
  margin: 0 10px;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.completion-container {
  text-align: center;
}

.success-card {
  background: white;
  border-radius: 10px;
  padding: 40px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin: 20px 0;
}

.success-card h2 {
  color: #27ae60;
  margin-bottom: 20px;
}

.novel-info {
  text-align: left;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.novel-info p {
  margin: 10px 0;
  line-height: 1.5;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
  background: white;
  padding: 30px;
  border-radius: 10px;
  text-align: center;
  color: #3498db;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .smart-novel-creation {
    padding: 10px;
  }
  
  .stage-indicator {
    flex-direction: column;
  }
  
  .stage-indicator span {
    margin: 5px 0;
  }
  
  .question-card {
    padding: 20px;
  }
  
  .btn-primary, .btn-secondary {
    display: block;
    width: 100%;
    margin: 10px 0;
  }
}
</style>