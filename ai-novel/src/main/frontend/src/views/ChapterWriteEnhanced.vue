<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container mx-auto px-4 py-6">
      <!-- 头部面包屑 -->
      <div class="mb-6">
        <nav class="flex items-center space-x-2 text-sm text-gray-600">
          <router-link to="/" class="hover:text-blue-600">首页</router-link>
          <span>/</span>
          <router-link :to="`/novel/${novelId}`" class="hover:text-blue-600">
            {{ novel?.title || '加载中...' }}
          </router-link>
          <span>/</span>
          <span class="text-gray-900">AI智能续写</span>
        </nav>
      </div>

      <div class="grid grid-cols-12 gap-6">
        <!-- 左侧：章节列表 -->
        <div class="col-span-3">
          <div class="bg-white rounded-lg shadow-sm p-4 sticky top-4">
            <div class="flex items-center justify-between mb-4">
              <h3 class="font-semibold text-gray-900">📚 章节列表</h3>
              <button 
                @click="refreshChapters"
                class="text-blue-600 hover:text-blue-800 text-sm"
                :disabled="loadingChapters"
              >
                🔄
              </button>
            </div>
            
            <!-- 章节列表 -->
            <div v-if="loadingChapters" class="text-center py-8 text-gray-500">
              加载中...
            </div>
            
            <div v-else-if="chapters.length === 0" class="text-center py-8 text-gray-500">
              暂无章节
            </div>
            
            <div v-else class="space-y-2 max-h-96 overflow-y-auto">
              <div 
                v-for="chapter in chapters" 
                :key="chapter.id"
                class="p-3 rounded-lg cursor-pointer transition-colors hover:bg-gray-50"
                :class="{
                  'bg-blue-100 border border-blue-300': chapter.id === currentChapterId,
                  'border border-transparent': chapter.id !== currentChapterId
                }"
                @click="viewChapter(chapter.id)"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center space-x-2">
                      <span class="font-medium text-gray-900">
                        第{{ chapter.chapterNumber }}章
                      </span>
                      <span v-if="chapter.isAiGenerated" class="text-xs" title="AI生成">
                        🤖
                      </span>
                    </div>
                    <div class="text-sm text-gray-600 truncate mt-1">
                      {{ chapter.title }}
                    </div>
                    <div class="text-xs text-gray-500 mt-1">
                      {{ chapter.wordCount }} 字
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <button 
              @click="createNewChapter"
              class="mt-4 w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              ✏️ 新建章节
            </button>
          </div>
        </div>

        <!-- 右侧：续写表单 -->
        <div class="col-span-9">
          <div class="bg-white rounded-lg shadow-sm p-8">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-2xl font-bold text-gray-800">
                🤖 AI智能续写
                <span v-if="chapters.length > 0" class="text-lg text-gray-500 ml-2">
                  - 第{{ chapters.length + 1 }}章
                </span>
              </h2>
              <button
                @click="$router.back()"
                class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
              >
                返回
              </button>
            </div>

            <!-- 小说信息卡片 -->
            <div v-if="novel" class="mb-6 p-4 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg">
              <div class="flex items-center justify-between">
                <div>
                  <h3 class="text-lg font-semibold text-gray-900">{{ novel.title }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ novel.description }}</p>
                </div>
                <div class="text-right text-sm text-gray-600">
                  <div>章节: {{ novel.totalChapters }}</div>
                  <div>字数: {{ formatNumber(novel.totalWords) }}</div>
                </div>
              </div>
            </div>

            <!-- 上一章节信息（如果有） -->
            <div v-if="lastChapter" class="mb-6 p-4 bg-yellow-50 border-l-4 border-yellow-400 rounded">
              <h4 class="font-semibold text-gray-900 mb-2">
                📖 上一章节：第{{ lastChapter.chapterNumber }}章 {{ lastChapter.title }}
              </h4>
              <div v-if="lastChapter.summary" class="text-sm text-gray-700 mt-2">
                <span class="font-medium">摘要：</span>{{ lastChapter.summary }}
              </div>
              <div v-if="lastChapter.continuationDirection" class="text-sm text-gray-700 mt-2">
                <span class="font-medium">续写方向：</span>{{ lastChapter.continuationDirection }}
              </div>
              <div class="text-xs text-gray-500 mt-2">
                {{ lastChapter.wordCount }} 字 · {{ formatDate(lastChapter.createdAt) }}
              </div>
            </div>

            <!-- 当前状态信息 -->
            <div v-if="contextInfo" class="mb-6 p-4 bg-gradient-to-r from-purple-50 to-pink-50 border border-purple-200 rounded-lg">
              <h4 class="font-semibold text-gray-900 mb-3 flex items-center">
                📊 当前写作状态
              </h4>
              
              <!-- 写作风格信息 -->
              <div v-if="contextInfo.writingStyle" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">✍️ 写作风格</div>
                <div class="p-3 bg-white border border-purple-200 rounded-lg grid grid-cols-2 gap-2">
                  <div v-if="contextInfo.writingStyle.perspective" class="text-sm">
                    <span class="font-medium text-gray-700">视角:</span>
                    <span class="text-gray-800 ml-1">{{ contextInfo.writingStyle.perspective }}</span>
                  </div>
                  <div v-if="contextInfo.writingStyle.tone" class="text-sm">
                    <span class="font-medium text-gray-700">语气:</span>
                    <span class="text-gray-800 ml-1">{{ contextInfo.writingStyle.tone }}</span>
                  </div>
                  <div v-if="contextInfo.writingStyle.sentenceStyle" class="text-sm">
                    <span class="font-medium text-gray-700">句式:</span>
                    <span class="text-gray-800 ml-1">{{ contextInfo.writingStyle.sentenceStyle }}</span>
                  </div>
                  <div v-if="contextInfo.writingStyle.avgSentenceLength" class="text-sm">
                    <span class="font-medium text-gray-700">句长:</span>
                    <span class="text-gray-800 ml-1">{{ contextInfo.writingStyle.avgSentenceLength }}字</span>
                  </div>
                  <div v-if="contextInfo.writingStyle.description" class="text-sm col-span-2">
                    <span class="font-medium text-gray-700">描述:</span>
                    <span class="text-gray-800 ml-1">{{ contextInfo.writingStyle.description }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 核心角色信息 -->
              <div v-if="contextInfo.coreCharacters && contextInfo.coreCharacters.length > 0" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">🌟 核心角色</div>
                <div class="flex flex-wrap gap-2">
                  <span 
                    v-for="char in contextInfo.coreCharacters.slice(0, 5)" 
                    :key="char.id"
                    class="px-3 py-1 bg-white border border-purple-200 rounded-full text-sm"
                    :title="char.personality || char.background">
                    {{ char.name }}
                    <span v-if="char.roleType" class="text-xs text-gray-500 ml-1">
                      ({{ getRoleTypeLabel(char.roleType) }})
                    </span>
                  </span>
                </div>
              </div>
              
              <!-- 近期角色信息 -->
              <div v-if="contextInfo.recentCharacters && contextInfo.recentCharacters.length > 0" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">🎭 近期角色</div>
                <div class="flex flex-wrap gap-2">
                  <span 
                    v-for="char in contextInfo.recentCharacters.slice(0, 8)" 
                    :key="char.id"
                    class="px-2 py-1 bg-gray-100 border border-gray-300 rounded-full text-xs"
                    :title="char.personality || char.background">
                    {{ char.name }}
                    <span v-if="char.roleType" class="text-xs text-gray-500">
                      ({{ getRoleTypeLabel(char.roleType) }})
                    </span>
                  </span>
                </div>
              </div>
              
              <!-- 当前进度信息 -->
              <div v-if="contextInfo.progress" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">📈 当前进度</div>
                <div class="p-3 bg-white border border-purple-200 rounded-lg">
                  <div class="grid grid-cols-2 gap-4">
                    <div class="text-sm">
                      <span class="font-medium text-gray-700">章节:</span>
                      <span class="text-gray-800 ml-1">{{ contextInfo.progress.currentChapter }} / {{ contextInfo.progress.totalChapters }}</span>
                    </div>
                    <div class="text-sm">
                      <span class="font-medium text-gray-700">字数:</span>
                      <span class="text-gray-800 ml-1">{{ contextInfo.progress.currentWordCount }} / {{ contextInfo.progress.targetWordCount }}</span>
                    </div>
                    <div class="text-sm">
                      <span class="font-medium text-gray-700">进度:</span>
                      <span class="text-gray-800 ml-1">{{ (contextInfo.progress.progressPercentage || 0).toFixed(1) }}%</span>
                    </div>
                    <div class="text-sm">
                      <span class="font-medium text-gray-700">状态:</span>
                      <span class="text-gray-800 ml-1">{{ contextInfo.progress.status }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 建议续写方向 -->
              <div v-if="contextInfo.continuationSuggestions && contextInfo.continuationSuggestions.length > 0" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">
                  🎯 建议续写方向 ({{ contextInfo.continuationSuggestions.length }}条)
                </div>
                <div class="space-y-2 max-h-40 overflow-y-auto">
                  <div 
                    v-for="sugg in contextInfo.continuationSuggestions.slice(0, 3)" 
                    :key="sugg.id"
                    class="p-2 bg-white border border-purple-200 rounded text-sm">
                    <div class="font-medium text-gray-800 flex justify-between">
                      <span>{{ sugg.title }}</span>
                      <span class="text-xs text-blue-600">优先级: {{ sugg.priority }}</span>
                    </div>
                    <div class="text-gray-600 text-xs mt-1">{{ sugg.description }}</div>
                  </div>
                </div>
              </div>
              
              <!-- 当前场景信息 -->
              <div v-if="contextInfo.currentScene" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">🎬 当前场景</div>
                <div class="p-3 bg-white border border-purple-200 rounded-lg">
                  <div class="font-semibold text-gray-800">{{ contextInfo.currentScene.name }}</div>
                  <div v-if="contextInfo.currentScene.location" class="text-sm text-gray-600 mt-1">
                    📍 {{ contextInfo.currentScene.location }}
                  </div>
                  <div v-if="contextInfo.currentScene.sceneType" class="text-sm text-gray-600 mt-1">
                    🏗️ 类型: {{ contextInfo.currentScene.sceneType }}
                  </div>
                  <div v-if="contextInfo.currentScene.description" class="text-sm text-gray-600 mt-1">
                    📝 {{ contextInfo.currentScene.description }}
                  </div>
                  <div v-if="contextInfo.currentScene.atmosphere" class="text-sm text-gray-600 mt-1">
                    🎭 {{ contextInfo.currentScene.atmosphere }}
                  </div>
                </div>
              </div>
              
              <!-- 情节线索信息 -->
              <div v-if="contextInfo.plotThreads && contextInfo.plotThreads.length > 0" class="mb-3">
                <div class="text-sm font-medium text-gray-700 mb-2">🔗 情节线索 ({{ contextInfo.plotThreads.length }}条)</div>
                <div class="space-y-2 max-h-40 overflow-y-auto">
                  <div 
                    v-for="thread in contextInfo.plotThreads.slice(0, 3)" 
                    :key="thread.id"
                    class="p-2 bg-white border border-purple-200 rounded text-sm">
                    <div class="font-medium text-gray-800 flex justify-between">
                      <span>{{ thread.name }}</span>
                      <span class="text-xs" :class="{
                        'text-green-600': thread.status === 'RESOLVED',
                        'text-yellow-600': thread.status === 'ACTIVE',
                        'text-blue-600': thread.status === 'MAIN'
                      }">
                        {{ thread.status }}
                      </span>
                    </div>
                    <div class="text-gray-600 text-xs mt-1">{{ thread.description }}</div>
                  </div>
                </div>
              </div>
            </div>

            <form @submit.prevent="handleContinue" class="space-y-6">
              <!-- 章节标题 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  章节标题 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.title"
                  type="text"
                  placeholder="例如：初遇强敌"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>

              <!-- 续写方向 -->
              <div>
                <div class="flex items-center justify-between mb-2">
                  <label class="block text-sm font-medium text-gray-700">
                    续写方向（可选）
                  </label>
                  <button
                    type="button"
                    @click="loadSuggestions"
                    class="text-sm text-blue-600 hover:text-blue-800"
                    :disabled="loadingSuggestions"
                  >
                    {{ loadingSuggestions ? '加载中...' : '💡 查看智能建议' }}
                  </button>
                </div>
                <textarea
                  v-model="form.direction"
                  rows="4"
                  placeholder="例如：主角遇到强敌，展开一场激烈的战斗..."
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                ></textarea>
              </div>

              <!-- 智能建议列表 -->
              <div v-if="suggestions.length > 0" class="space-y-2">
                <h4 class="text-sm font-medium text-gray-700">
                  💡 续写建议（点击采用）
                </h4>
                <div class="space-y-2 max-h-60 overflow-y-auto">
                  <div
                    v-for="suggestion in suggestions"
                    :key="suggestion.id"
                    class="p-3 border border-gray-200 rounded-lg hover:border-blue-400 cursor-pointer transition-colors"
                    :class="{ 'border-blue-500 bg-blue-50': selectedSuggestion?.id === suggestion.id }"
                    @click="applySuggestion(suggestion)"
                  >
                    <div class="flex items-start justify-between">
                      <div class="flex-1">
                        <div class="flex items-center space-x-2">
                          <h5 class="font-semibold text-gray-900">{{ suggestion.title }}</h5>
                          <span class="px-2 py-0.5 text-xs bg-gray-100 text-gray-600 rounded">
                            {{ suggestion.plotDirection }}
                          </span>
                          <span class="text-xs text-gray-500">
                            优先级: {{ suggestion.priority }}/10
                          </span>
                        </div>
                        <p class="text-sm text-gray-600 mt-1">{{ suggestion.description }}</p>
                        <div class="mt-2 text-xs text-gray-500">
                          字数: {{ suggestion.expectedWordCount }} · 难度: {{ suggestion.difficultyLevel }}/5
                        </div>
                      </div>
                      <button
                        type="button"
                        @click.stop="applySuggestion(suggestion)"
                        class="ml-3 px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
                      >
                        采用
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 关联选择（可选） -->
              <div class="grid grid-cols-2 gap-4">
                <!-- 场景选择 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    关联场景（可选）
                  </label>
                  <select
                    v-model="form.sceneId"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option :value="null">无关联场景</option>
                    <option v-for="scene in scenes" :key="scene.id" :value="scene.id">
                      {{ scene.name }}
                    </option>
                  </select>
                </div>

                <!-- 大纲节点选择 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    关联大纲节点（可选）
                  </label>
                  <select
                    v-model="form.outlineNodeId"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option :value="null">无关联大纲</option>
                    <option v-for="node in outlineNodes" :key="node.id" :value="node.id">
                      {{ node.title }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- 目标字数滑块 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  目标字数: <span class="text-blue-600 font-semibold">{{ form.targetWordCount }}</span> 字
                </label>
                <input
                  v-model="form.targetWordCount"
                  type="range"
                  min="500"
                  max="10000"
                  step="500"
                  class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
                />
                <div class="flex justify-between text-xs text-gray-500 mt-1">
                  <span>500</span>
                  <span>2500</span>
                  <span>5000</span>
                  <span>7500</span>
                  <span>10000</span>
                </div>
              </div>

              <!-- 按钮组 -->
              <div class="flex justify-end space-x-4 pt-4">
                <button
                  type="button"
                  @click="$router.back()"
                  class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                >
                  取消
                </button>
                <button
                  type="submit"
                  :disabled="generating || !form.title"
                  class="px-6 py-2 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg hover:from-blue-700 hover:to-purple-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span v-if="generating">
                    <svg class="inline w-4 h-4 mr-2 animate-spin" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    AI创作中...
                  </span>
                  <span v-else>🤖 开始续写</span>
                </button>
              </div>
            </form>

            <!-- 生成结果展示 -->
            <div v-if="result" class="mt-8 p-6 border-2 border-green-300 bg-green-50 rounded-lg">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-xl font-bold text-gray-900 flex items-center">
                  <span class="mr-2">✅</span>
                  生成完成
                  <span v-if="result.isAiGenerated" class="ml-2 text-sm text-blue-600">🤖 AI生成</span>
                </h3>
                <div class="flex items-center space-x-2">
                  <button
                    @click="saveAndContinue"
                    class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                  >
                    💾 保存并继续
                  </button>
                  <button
                    @click="regenerateContent"
                    :disabled="regenerating"
                    class="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition disabled:opacity-50"
                  >
                    {{ regenerating ? '重新生成中...' : '🔄 重新生成' }}
                  </button>
                  <button
                    @click="editContent"
                    class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                  >
                    ✏️ 编辑
                  </button>
                </div>
              </div>

              <!-- 章节标题 -->
              <div class="mb-4">
                <h4 class="text-lg font-semibold text-gray-900">
                  第{{ result.chapterNumber }}章 {{ result.title }}
                </h4>
              </div>

              <!-- 可编辑内容区域 -->
              <div v-if="isEditing" class="space-y-4">
                <textarea
                  v-model="editableContent"
                  class="w-full min-h-96 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-serif text-gray-800 leading-relaxed"
                  placeholder="章节内容..."
                ></textarea>
                <div class="flex justify-end space-x-2">
                  <button
                    @click="cancelEdit"
                    class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
                  >
                    取消
                  </button>
                  <button
                    @click="saveEdit"
                    class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    保存修改
                  </button>
                </div>
              </div>

              <!-- 只读展示 -->
              <div v-else class="prose max-w-none">
                <div class="whitespace-pre-wrap text-gray-800 leading-relaxed font-serif">
                  {{ result.content }}
                </div>
              </div>

              <!-- 统计信息 -->
              <div class="mt-6 pt-4 border-t border-gray-200 space-y-3">
                <div class="flex items-center justify-between text-sm">
                  <div class="flex items-center space-x-6 text-gray-600">
                    <div>
                      <span class="font-medium">字数：</span>
                      <span class="text-lg font-semibold text-blue-600">{{ result.wordCount }}</span> 字
                    </div>
                  </div>
                  <div class="text-gray-500 text-xs">
                    创建时间：{{ formatDate(result.createdAt) }}
                  </div>
                </div>
                
                <!-- 摘要显示 -->
                <div v-if="result.summary" class="p-3 bg-blue-50 rounded-lg">
                  <span class="text-sm font-medium text-blue-900">📝 章节摘要：</span>
                  <span class="text-sm text-blue-800">{{ result.summary }}</span>
                </div>
                
                <!-- 续写方向显示 -->
                <div v-if="result.continuationDirection" class="p-3 bg-green-50 rounded-lg">
                  <span class="text-sm font-medium text-green-900">🎯 建议续写方向：</span>
                  <span class="text-sm text-green-800">{{ result.continuationDirection }}</span>
                </div>
              </div>

              <!-- 字数达成状态 -->
              <div class="mt-4 p-3 rounded-lg" :class="{
                'bg-green-100 text-green-800': wordCountStatus === 'perfect',
                'bg-yellow-100 text-yellow-800': wordCountStatus === 'under' || wordCountStatus === 'over',
                'bg-gray-100 text-gray-800': !wordCountStatus
              }">
                <div class="flex items-center">
                  <span v-if="wordCountStatus === 'perfect'" class="mr-2">✅</span>
                  <span v-else class="mr-2">⚠️</span>
                  <span class="font-medium">
                    {{ wordCountStatusText }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 字数统计悬浮栏（生成时显示） -->
    <div 
      v-if="generating" 
      class="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg p-4 z-50"
    >
      <div class="container mx-auto">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div class="flex items-center">
              <svg class="w-6 h-6 mr-2 text-blue-600 animate-spin" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span class="text-gray-700 font-medium">AI正在创作中...</span>
            </div>
            <div class="text-sm text-gray-600">
              目标字数：{{ form.targetWordCount }} 字
            </div>
          </div>
          <div class="text-sm text-gray-500">
            这可能需要 30-60 秒，请耐心等待
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'

const route = useRoute()
const router = useRouter()
const novelId = ref(parseInt(route.params.id))

// 数据
const novel = ref(null)
const chapters = ref([])
const lastChapter = ref(null)
const currentChapterId = ref(null)
const scenes = ref([])
const outlineNodes = ref([])
const suggestions = ref([])
const selectedSuggestion = ref(null)
const contextInfo = ref(null)

// 表单
const form = ref({
  novelId: novelId.value,
  title: '',
  direction: '',
  targetWordCount: 2000,
  sceneId: null,
  outlineNodeId: null
})

// 状态
const loading = ref(false)
const loadingChapters = ref(false)
const loadingSuggestions = ref(false)
const generating = ref(false)
const regenerating = ref(false)
const result = ref(null)
const isEditing = ref(false)
const editableContent = ref('')

// 计算属性
const wordCountStatus = computed(() => {
  if (!result.value) return null
  const diff = Math.abs(result.value.wordCount - form.value.targetWordCount)
  const percentage = diff / form.value.targetWordCount
  
  if (percentage < 0.1) return 'perfect' // 在目标的10%范围内
  if (result.value.wordCount < form.value.targetWordCount) return 'under'
  return 'over'
})

const wordCountStatusText = computed(() => {
  if (!result.value) return ''
  
  switch (wordCountStatus.value) {
    case 'perfect':
      return `已达到目标字数（${result.value.wordCount} / ${form.value.targetWordCount}）`
    case 'under':
      return `距离目标还差 ${form.value.targetWordCount - result.value.wordCount} 字`
    case 'over':
      return `已超出目标 ${result.value.wordCount - form.value.targetWordCount} 字`
    default:
      return ''
  }
})

// 方法
const loadNovel = async () => {
  try {
    novel.value = await api.novels.get(novelId.value)
  } catch (error) {
    console.error('加载小说失败:', error)
    alert('加载小说失败: ' + error.message)
  }
}

const loadChapters = async () => {
  loadingChapters.value = true
  try {
    chapters.value = await api.chapters.list(novelId.value)
    
    // 获取最后一章
    if (chapters.value.length > 0) {
      lastChapter.value = chapters.value[chapters.value.length - 1]
      
      // 如果上一章有续写方向，自动填充
      if (lastChapter.value.continuationDirection && !form.value.direction) {
        form.value.direction = lastChapter.value.continuationDirection
      }
    }
  } catch (error) {
    console.error('加载章节失败:', error)
  } finally {
    loadingChapters.value = false
  }
}

const refreshChapters = () => {
  loadChapters()
}

const viewChapter = async (chapterId) => {
  try {
    const chapter = await api.chapters.get(chapterId)
    result.value = chapter
    
    // 同步表单数据
    form.value.title = chapter.title
    form.value.direction = chapter.continuationDirection || ''
    form.value.sceneId = chapter.sceneId
    form.value.outlineNodeId = chapter.outlineNodeId
    
    // 滚动到结果区域
    setTimeout(() => {
      document.querySelector('.border-green-300')?.scrollIntoView({ 
        behavior: 'smooth' 
      })
    }, 100)
  } catch (error) {
    console.error('加载章节失败:', error)
    alert('加载章节失败: ' + error.message)
  }
}

const createNewChapter = () => {
  // 清空表单
  result.value = null
  form.value.title = ''
  form.value.direction = ''
  form.value.sceneId = null
  form.value.outlineNodeId = null
}

const loadSuggestions = async () => {
  loadingSuggestions.value = true
  try {
    // 获取最新的上下文信息
    const contextualRequest = {
      novelId: novelId.value,
      currentContent: lastChapter.value?.content || '',
      currentScene: contextInfo.value?.currentScene?.name || '',
      currentConflict: contextInfo.value?.latestAnalysis?.currentConflict || '',
      protagonistName: contextInfo.value?.latestAnalysis?.protagonistName || '',
      emotionalTone: contextInfo.value?.latestAnalysis?.emotionalTone || '',
      activeCharacters: contextInfo.value?.characters?.map(c => c.name) || [],
      expectedWordCount: form.value.targetWordCount,
      count: 3
    }
    
    // 基于当前上下文生成新的建议
    suggestions.value = await api.smartWriting.getContextualSuggestions(contextualRequest)
    
  } catch (error) {
    console.error('加载建议失败:', error)
    alert('加载建议失败: ' + error.message)
  } finally {
    loadingSuggestions.value = false
  }
}

const applySuggestion = (suggestion) => {
  selectedSuggestion.value = suggestion
  form.value.direction = suggestion.storyDevelopment || suggestion.description
  form.value.targetWordCount = suggestion.expectedWordCount || 2000
  
  // 自动生成标题
  if (!form.value.title) {
    form.value.title = suggestion.title
  }
}

const handleContinue = async () => {
  if (!form.value.title) {
    alert('请输入章节标题')
    return
  }
  
  generating.value = true
  result.value = null

  try {
    const requestData = {
      novelId: form.value.novelId,
      direction: form.value.direction,
      targetWordCount: form.value.targetWordCount
    }
    
    // 添加可选字段
    if (form.value.sceneId) {
      requestData.sceneId = form.value.sceneId
    }
    
    // 添加大纲节点ID
    if (form.value.outlineNodeId) {
      requestData.outlineNodeId = form.value.outlineNodeId
    }
    
    const chapter = await api.chapters.continue(requestData)
    
    // 如果标题不同，更新章节
    if (chapter.title !== form.value.title) {
      const updated = await api.chapters.update(chapter.id, {
        title: form.value.title,
        content: chapter.content,
        wordCount: chapter.wordCount
      })
      result.value = updated
    } else {
      result.value = chapter
    }
    
    // 如果采用了建议，标记为已采用
    if (selectedSuggestion.value) {
      try {
        await api.smartWriting.adoptSuggestion(selectedSuggestion.value.id, chapter.id)
      } catch (error) {
        console.error('标记建议失败:', error)
      }
    }
    
    // 刷新章节列表
    await loadChapters()
  } catch (error) {
    console.error('续写失败:', error)
    alert('续写失败: ' + error.message)
  } finally {
    generating.value = false
  }
}

const regenerateContent = async () => {
  if (!result.value) return
  
  regenerating.value = true
  try {
    const regenerated = await api.chapters.regenerate(result.value.id, form.value.direction)
    result.value = regenerated
    alert('重新生成成功')
  } catch (error) {
    console.error('重新生成失败:', error)
    alert('重新生成失败: ' + error.message)
  } finally {
    regenerating.value = false
  }
}

const editContent = () => {
  isEditing.value = true
  editableContent.value = result.value.content
}

const cancelEdit = () => {
  isEditing.value = false
  editableContent.value = ''
}

const saveEdit = async () => {
  try {
    const updated = await api.chapters.update(result.value.id, {
      ...result.value,
      content: editableContent.value,
      wordCount: editableContent.value.length
    })
    result.value = updated
    isEditing.value = false
    alert('修改已保存')
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败: ' + error.message)
  }
}

const saveAndContinue = () => {
  alert('章节已保存！')
  // 重置表单，准备创建下一章
  createNewChapter()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 工具函数
const formatNumber = (num) => {
  return num?.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '0'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 加载场景数据
const loadScenes = async () => {
  try {
    scenes.value = await api.scenes.list(novelId.value)
  } catch (error) {
    console.error('加载场景失败:', error)
  }
}

// 加载大纲数据
const loadOutlineNodes = async () => {
  try {
    outlineNodes.value = await api.outlines.list(novelId.value)
  } catch (error) {
    console.error('加载大纲失败:', error)
  }
}

// 加载上下文信息（角色、场景、分析、建议）
const loadContextInfo = async () => {
  try {
    // 使用新的API获取完整的写作状态信息
    const writingStatus = await api.smartWriting.getWritingStatus(novelId.value).catch(() => null)
    
    if (writingStatus) {
      contextInfo.value = {
        writingStyle: writingStatus.writingStyle,
        coreCharacters: writingStatus.coreCharacters,
        recentCharacters: writingStatus.recentCharacters,
        progress: writingStatus.progress,
        continuationSuggestions: writingStatus.continuationSuggestions,
        currentScene: writingStatus.currentScene,
        plotThreads: writingStatus.plotThreads
      }
      
      console.log('完整写作状态加载完成:', contextInfo.value)
    } else {
      // 如果新的API不可用，回退到旧的加载方式
      const [characters, scenes, latestAnalysis, unadoptedSuggestions] = await Promise.all([
        api.characters.list(novelId.value),
        api.scenes.list(novelId.value),
        api.smartWriting.getLatestAnalysis(novelId.value).catch(() => null),
        api.smartWriting.getUnadoptedSuggestions(novelId.value).catch(() => [])
      ])
      
      // 获取当前场景
      let currentScene = null
      if (lastChapter.value?.sceneId) {
        currentScene = scenes.find(s => s.id === lastChapter.value.sceneId)
      }
      
      contextInfo.value = {
        characters,
        currentScene,
        latestAnalysis,
        unadoptedSuggestions
      }
      
      console.log('上下文信息加载完成:', contextInfo.value)
    }
  } catch (error) {
    console.error('加载上下文信息失败:', error)
  }
}

// 获取角色类型标签
const getRoleTypeLabel = (roleType) => {
  if (!roleType) return ''
  
  const labels = {
    'PROTAGONIST': '主角',
    'ANTAGONIST': '反派',
    'SUPPORTING': '配角',
    'MINOR': '次要',
    'PRIMARY': '主要',
    'SECONDARY': '次要',
    'TERTIARY': '边缘'
  }
  return labels[roleType] || roleType
}

// 初始化
onMounted(async () => {
  await loadNovel()
  await loadChapters()
  await loadScenes()
  await loadOutlineNodes()
  await loadContextInfo()
})
</script>

<style scoped>
/* 自定义滚动条 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* prose样式增强 */
.prose {
  line-height: 2;
  font-size: 16px;
}

.font-serif {
  font-family: 'Songti SC', 'STSong', 'SimSun', serif;
}
</style>
