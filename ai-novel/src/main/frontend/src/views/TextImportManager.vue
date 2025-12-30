<template>
  <div class="w-full h-full bg-gradient-to-br from-slate-50 to-slate-100 flex flex-col">
    <!-- Header -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 shadow-sm">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-slate-900">文本导入</h1>
          <p class="text-sm text-slate-600 mt-1">导入已有文本，系统将自动分章并提取要素</p>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-auto p-6">
      <!-- Upload Area -->
      <div
        @drop="handleDrop"
        @dragover="dragover = true"
        @dragleave="dragover = false"
        :class="[
          'border-2 border-dashed rounded-lg p-12 text-center transition mb-6',
          dragover
            ? 'border-blue-500 bg-blue-50'
            : 'border-slate-300 bg-white hover:border-slate-400'
        ]"
      >
        <input
          ref="fileInput"
          type="file"
          @change="handleFileSelect"
          accept=".txt,.docx,.pdf"
          class="hidden"
        />
        <div class="flex flex-col items-center gap-3 cursor-pointer" @click="$refs.fileInput.click()">
          <Upload class="w-12 h-12 text-slate-400" />
          <div>
            <p class="text-lg font-medium text-slate-900">拖拽文件到此处或点击选择</p>
            <p class="text-sm text-slate-600 mt-1">支持 TXT、DOCX、PDF 格式 (最大 50MB)</p>
          </div>
        </div>
      </div>

      <!-- Import History -->
      <div v-if="imports.length > 0" class="space-y-4">
        <h2 class="text-lg font-bold text-slate-900">导入记录</h2>
        <div v-for="imp in imports" :key="imp.id" class="bg-white rounded-lg shadow hover:shadow-lg transition p-6">
          <div class="flex justify-between items-start mb-4">
            <div class="flex-1 cursor-pointer" @click="selectedImport = imp">
              <h3 class="text-base font-bold text-slate-900">{{ imp.fileName }}</h3>
              <p class="text-sm text-slate-600 mt-1">{{ imp.chapterCount }} 章 · {{ imp.totalWords }} 字</p>
            </div>
            <div class="flex gap-2">
              <span
                :class="[
                  'px-3 py-1 rounded-full text-xs font-medium',
                  imp.status === 'parsed' ? 'bg-green-100 text-green-700' :
                  imp.status === 'parsing' ? 'bg-blue-100 text-blue-700' :
                  imp.status === 'failed' ? 'bg-red-100 text-red-700' :
                  'bg-yellow-100 text-yellow-700'
                ]"
              >
                {{ statusLabel(imp.status) }}
              </span>
              <span
                v-if="imp.confirmed"
                class="px-3 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-700"
              >
                已确认
              </span>
            </div>
          </div>

          <div class="text-sm text-slate-600 space-y-1">
            <p>地点: {{ imp.extractedLocations ? (imp.extractedLocations.split(',').length + '个') : '未提取' }}</p>
            <p>人物: {{ imp.extractedCharacters ? (imp.extractedCharacters.split(',').length + '个') : '未提取' }}</p>
            <p>事件: {{ imp.extractedEvents ? (imp.extractedEvents.split(',').length + '个') : '未提取' }}</p>
          </div>

          <div class="flex gap-2 mt-4">
            <button
              @click="selectedImport = imp"
              class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-lg text-sm transition"
            >
              预览
            </button>
            <button
              v-if="!imp.confirmed"
              @click="confirmImport(imp.id)"
              class="flex-1 bg-green-600 hover:bg-green-700 text-white px-3 py-2 rounded-lg text-sm transition"
            >
              确认导入
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <FileText class="w-12 h-12 text-slate-400 mx-auto mb-4" />
        <p class="text-slate-600">暂无导入记录</p>
      </div>
    </div>

    <!-- Preview Panel -->
    <div v-if="selectedImport" class="fixed inset-0 bg-black/50 flex items-end z-50">
      <div class="bg-white w-full md:w-2/3 h-3/4 md:h-full md:right-0 rounded-t-lg md:rounded-none overflow-hidden flex flex-col">
        <!-- Panel Header -->
        <div class="flex justify-between items-center p-6 border-b border-slate-200">
          <div>
            <h3 class="text-xl font-bold text-slate-900">{{ selectedImport.fileName }}</h3>
            <p class="text-sm text-slate-600 mt-1">{{ selectedImport.chapterCount }} 章 · {{ selectedImport.totalWords }} 字</p>
          </div>
          <button
            @click="selectedImport = null"
            class="text-slate-600 hover:text-slate-900 transition"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <!-- Tabs -->
        <div class="flex border-b border-slate-200 px-6">
          <button
            v-for="tab in previewTabs"
            :key="tab.id"
            @click="selectedTab = tab.id"
            :class="[
              'px-4 py-3 font-medium text-sm transition border-b-2',
              selectedTab === tab.id
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            ]"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Tab Content -->
        <div class="flex-1 overflow-auto p-6">
          <!-- Elements Tab -->
          <div v-show="selectedTab === 'elements'" class="space-y-6">
            <div>
              <h4 class="font-bold text-slate-900 mb-2">提取的地点 ({{ locations.length }})</h4>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="loc in locations.slice(0, 20)"
                  :key="loc"
                  class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm"
                >
                  {{ loc }}
                </span>
              </div>
            </div>

            <div>
              <h4 class="font-bold text-slate-900 mb-2">提取的人物 ({{ characters.length }})</h4>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="char in characters.slice(0, 20)"
                  :key="char"
                  class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm"
                >
                  {{ char }}
                </span>
              </div>
            </div>

            <div>
              <h4 class="font-bold text-slate-900 mb-2">提取的事件 ({{ events.length }})</h4>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="evt in events.slice(0, 20)"
                  :key="evt"
                  class="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm"
                >
                  {{ evt }}
                </span>
              </div>
            </div>
          </div>

          <!-- Chapters Tab -->
          <div v-show="selectedTab === 'chapters'" class="space-y-4">
            <div v-for="(chapter, index) in chapters" :key="index" class="border border-slate-200 rounded-lg p-4">
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-slate-900">{{ chapter.title }}</h4>
                <span class="text-xs bg-slate-100 text-slate-700 px-2 py-1 rounded">{{ chapter.wordCount }} 字</span>
              </div>
              <p class="text-sm text-slate-600 line-clamp-3">{{ chapter.contentPreview }}</p>
            </div>
          </div>

          <!-- Settings Tab -->
          <div v-show="selectedTab === 'settings'" class="space-y-4">
            <div class="space-y-3">
              <label class="flex items-center gap-3 cursor-pointer">
                <input type="checkbox" v-model="autoChapterize" class="w-4 h-4" />
                <span class="text-sm text-slate-700">自动分章</span>
              </label>
              <label class="flex items-center gap-3 cursor-pointer">
                <input type="checkbox" v-model="extractElements" class="w-4 h-4" />
                <span class="text-sm text-slate-700">提取关键要素</span>
              </label>
            </div>

            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 text-sm text-blue-700">
              <p class="font-medium mb-1">导入说明</p>
              <ul class="list-disc list-inside space-y-1 text-xs">
                <li>系统会自动识别章节标识（如"第X章"）</li>
                <li>如无章节标识，将按字数自动分割</li>
                <li>提取的要素可用于世界观映射</li>
                <li>确认后的章节将作为小说内容</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="border-t border-slate-200 p-6 flex gap-2 bg-slate-50">
          <button
            @click="selectedImport = null"
            class="flex-1 px-4 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-100 transition"
          >
            关闭
          </button>
          <button
            v-if="!selectedImport.confirmed"
            @click="confirmImport(selectedImport.id)"
            class="flex-1 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition"
          >
            确认导入
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Upload, X, FileText } from 'lucide-vue-next'

interface TextImport {
  id: number
  fileName: string
  fileType: string
  status: string
  chapterCount: number
  totalWords: number
  extractedLocations: string
  extractedCharacters: string
  extractedEvents: string
  confirmed: boolean
}

interface Chapter {
  title: string
  wordCount: number
  contentPreview: string
}

const fileInput = ref(null)
const dragover = ref(false)
const imports = ref<TextImport[]>([])
const selectedImport = ref<TextImport | null>(null)
const selectedTab = ref('elements')
const autoChapterize = ref(true)
const extractElements = ref(true)

const previewTabs = [
  { id: 'elements', label: '提取要素' },
  { id: 'chapters', label: '章节预览' },
  { id: 'settings', label: '导入设置' }
]

const locations = computed(() => {
  if (!selectedImport.value?.extractedLocations) return []
  return selectedImport.value.extractedLocations.split(',')
})

const characters = computed(() => {
  if (!selectedImport.value?.extractedCharacters) return []
  return selectedImport.value.extractedCharacters.split(',')
})

const events = computed(() => {
  if (!selectedImport.value?.extractedEvents) return []
  return selectedImport.value.extractedEvents.split(',')
})

const chapters = ref<Chapter[]>([])

const handleFileSelect = async (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files) {
    const file = target.files[0]
    if (file) {
      await uploadFile(file)
    }
  }
  dragover.value = false
}

const handleDrop = async (e: DragEvent) => {
  e.preventDefault()
  dragover.value = false
  if (e.dataTransfer?.files) {
    const file = e.dataTransfer.files[0]
    if (file) {
      await uploadFile(file)
    }
  }
}

const uploadFile = async (file: File) => {
  // Mock implementation
  const mockImport: TextImport = {
    id: Date.now(),
    fileName: file.name,
    fileType: file.name.split('.').pop() || 'unknown',
    status: 'parsed',
    chapterCount: 15,
    totalWords: 45000,
    extractedLocations: '京城,山脉,魔法学院,皇宫',
    extractedCharacters: '主角,女主,反派,导师',
    extractedEvents: '初入学院,发现秘密,大战,最终决战',
    confirmed: false
  }
  imports.value.push(mockImport)
}

const confirmImport = (importId: number) => {
  const imp = imports.value.find(i => i.id === importId)
  if (imp) {
    imp.confirmed = true
  }
}

const statusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'uploaded': '已上传',
    'parsing': '处理中',
    'parsed': '已解析',
    'failed': '失败'
  }
  return labels[status] || status
}
</script>

<style scoped>
</style>
