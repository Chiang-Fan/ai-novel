<template>
  <div class="w-full h-full bg-gradient-to-br from-slate-50 to-slate-100 flex flex-col">
    <!-- Header -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 shadow-sm">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-slate-900">世界观设定</h1>
          <p class="text-sm text-slate-600 mt-1">构建小说的多维世界体系</p>
        </div>
        <button
          @click="showCreateDialog = true"
          class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition"
        >
          <Plus class="w-5 h-5" />
          创建世界观
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-auto p-6">
      <div v-if="worldSettings.length === 0" class="text-center py-12">
        <Globe class="w-12 h-12 text-slate-400 mx-auto mb-4" />
        <p class="text-slate-600">暂无世界观设定，立即创建一个</p>
      </div>

      <div v-else class="space-y-6">
        <div
          v-for="setting in worldSettings"
          :key="setting.id"
          class="bg-white rounded-lg shadow hover:shadow-lg transition p-6"
          @click="currentWorldSetting = setting; activeTab = 'overview'"
        >
          <div class="flex justify-between items-start mb-4">
            <div class="cursor-pointer flex-1">
              <h2 class="text-lg font-bold text-slate-900">{{ setting.name }}</h2>
              <p class="text-sm text-slate-600 mt-1">{{ setting.description }}</p>
            </div>
            <div class="flex gap-2">
              <span
                :class="[
                  'px-3 py-1 rounded-full text-xs font-medium',
                  setting.status === 'published'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-yellow-100 text-yellow-700'
                ]"
              >
                {{ setting.status === 'published' ? '已发布' : '草稿' }}
              </span>
            </div>
          </div>

          <div class="text-sm text-slate-600">
            <p>宇宙背景: {{ setting.cosmicBackground || '未设置' }}</p>
            <p>版本: v{{ setting.version }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Detail Panel -->
    <div v-if="currentWorldSetting" class="fixed inset-0 bg-black/50 flex items-end z-50">
      <div class="bg-white w-full md:w-2/3 h-3/4 md:h-full md:right-0 rounded-t-lg md:rounded-none overflow-hidden flex flex-col">
        <!-- Panel Header -->
        <div class="flex justify-between items-center p-6 border-b border-slate-200">
          <h3 class="text-xl font-bold text-slate-900">{{ currentWorldSetting.name }}</h3>
          <button
            @click="currentWorldSetting = null"
            class="text-slate-600 hover:text-slate-900 transition"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <!-- Tabs -->
        <div class="flex border-b border-slate-200 px-6">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="[
              'px-4 py-3 font-medium text-sm transition border-b-2',
              activeTab === tab.id
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            ]"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Tab Content -->
        <div class="flex-1 overflow-auto p-6">
          <!-- Overview Tab -->
          <div v-show="activeTab === 'overview'" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">世界观名称</label>
              <input
                v-model="currentWorldSetting.name"
                type="text"
                class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">世界观描述</label>
              <textarea
                v-model="currentWorldSetting.description"
                rows="4"
                class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">宇宙背景</label>
              <textarea
                v-model="currentWorldSetting.cosmicBackground"
                rows="4"
                class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <button
              @click="publishWorldSetting"
              class="w-full bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition"
            >
              发布世界观
            </button>
          </div>

          <!-- Geography Tab -->
          <div v-show="activeTab === 'geography'" class="space-y-4">
            <button
              @click="showGeographyForm = true"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center justify-center gap-2 transition"
            >
              <Plus class="w-5 h-5" />
              添加地理位置
            </button>
            <div v-for="geo in geographies" :key="geo.id" class="border border-slate-200 rounded-lg p-4">
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-slate-900">{{ geo.name }}</h4>
                <span class="text-xs bg-slate-100 text-slate-700 px-2 py-1 rounded">{{ geo.geographyType }}</span>
              </div>
              <p class="text-sm text-slate-600">{{ geo.description }}</p>
              <p class="text-xs text-slate-500 mt-2">地形: {{ geo.terrainType }} | 气候: {{ geo.climate }}</p>
            </div>
          </div>

          <!-- TimePeriod Tab -->
          <div v-show="activeTab === 'timePeriod'" class="space-y-4">
            <button
              @click="showTimePeriodForm = true"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center justify-center gap-2 transition"
            >
              <Plus class="w-5 h-5" />
              添加时代背景
            </button>
            <div v-for="time in timePeriods" :key="time.id" class="border border-slate-200 rounded-lg p-4">
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-slate-900">{{ time.name }}</h4>
                <span class="text-xs bg-slate-100 text-slate-700 px-2 py-1 rounded">{{ time.periodType }}</span>
              </div>
              <p class="text-sm text-slate-600">{{ time.historicalBackground }}</p>
              <p class="text-xs text-slate-500 mt-2">{{ time.startYear }} - {{ time.endYear }}</p>
            </div>
          </div>

          <!-- Race Tab -->
          <div v-show="activeTab === 'race'" class="space-y-4">
            <button
              @click="showRaceForm = true"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center justify-center gap-2 transition"
            >
              <Plus class="w-5 h-5" />
              添加种族
            </button>
            <div v-for="race in races" :key="race.id" class="border border-slate-200 rounded-lg p-4">
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-slate-900">{{ race.name }}</h4>
                <span class="text-xs bg-slate-100 text-slate-700 px-2 py-1 rounded">{{ race.raceCategory }}</span>
              </div>
              <p class="text-sm text-slate-600">{{ race.physicalCharacteristics }}</p>
              <p class="text-xs text-slate-500 mt-2">社会地位: {{ race.socialStatus }}</p>
            </div>
          </div>

          <!-- Magic System Tab -->
          <div v-show="activeTab === 'magicSystem'" class="space-y-4">
            <button
              @click="showMagicSystemForm = true"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center justify-center gap-2 transition"
            >
              <Plus class="w-5 h-5" />
              添加魔法系统
            </button>
            <div v-for="magic in magicSystems" :key="magic.id" class="border border-slate-200 rounded-lg p-4">
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-slate-900">{{ magic.name }}</h4>
                <span class="text-xs bg-slate-100 text-slate-700 px-2 py-1 rounded">{{ magic.systemType }}</span>
              </div>
              <p class="text-sm text-slate-600">{{ magic.coreRules }}</p>
              <p class="text-xs text-slate-500 mt-2">修炼者: {{ magic.practitioners }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Dialog -->
    <div v-if="showCreateDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
        <h2 class="text-xl font-bold text-slate-900 mb-4">创建世界观</h2>
        <div class="space-y-4">
          <input
            v-model="newWorldSetting.name"
            type="text"
            placeholder="世界观名称"
            class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <textarea
            v-model="newWorldSetting.description"
            placeholder="世界观描述"
            rows="3"
            class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <textarea
            v-model="newWorldSetting.cosmicBackground"
            placeholder="宇宙背景"
            rows="3"
            class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
        <div class="flex gap-2 mt-6">
          <button
            @click="showCreateDialog = false"
            class="flex-1 px-4 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 transition"
          >
            取消
          </button>
          <button
            @click="createWorldSetting"
            class="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition"
          >
            创建
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, X, Globe } from 'lucide-vue-next'

interface WorldSetting {
  id: number
  novelId: number
  name: string
  description: string
  cosmicBackground: string
  aiAssisted: boolean
  version: number
  status: string
}

interface Geography {
  id: number
  worldSettingId: number
  name: string
  geographyType: string
  terrainType: string
  description: string
  importanceLevel: number
  climate: string
  specialFeatures: string
}

interface TimePeriod {
  id: number
  worldSettingId: number
  name: string
  periodType: string
  historicalBackground: string
  startYear: number
  endYear: number
}

interface Race {
  id: number
  worldSettingId: number
  name: string
  raceCategory: string
  physicalCharacteristics: string
  socialStatus: string
}

interface MagicSystem {
  id: number
  worldSettingId: number
  name: string
  systemType: string
  coreRules: string
  practitioners: string
}

const worldSettings = ref<WorldSetting[]>([])
const currentWorldSetting = ref<WorldSetting | null>(null)
const activeTab = ref('overview')
const showCreateDialog = ref(false)
const showGeographyForm = ref(false)
const showTimePeriodForm = ref(false)
const showRaceForm = ref(false)
const showMagicSystemForm = ref(false)

const geographies = ref<Geography[]>([])
const timePeriods = ref<TimePeriod[]>([])
const races = ref<Race[]>([])
const magicSystems = ref<MagicSystem[]>([])

const newWorldSetting = ref({
  name: '',
  description: '',
  cosmicBackground: ''
})

const tabs = [
  { id: 'overview', label: '概览' },
  { id: 'geography', label: '地理位置' },
  { id: 'timePeriod', label: '时代背景' },
  { id: 'race', label: '种族系统' },
  { id: 'magicSystem', label: '魔法系统' }
]

onMounted(async () => {
  // Mock data for now
  worldSettings.value = [
    {
      id: 1,
      novelId: 1,
      name: '东方仙侠世界',
      description: '一个融合了传统仙侠元素的东方幻想世界',
      cosmicBackground: '天地灵气充沛，修炼成仙成圣可期',
      aiAssisted: true,
      version: 1,
      status: 'draft'
    }
  ]
})

const createWorldSetting = async () => {
  // Implementation
  showCreateDialog.value = false
}

const publishWorldSetting = async () => {
  // Implementation
}
</script>

<style scoped>
</style>
