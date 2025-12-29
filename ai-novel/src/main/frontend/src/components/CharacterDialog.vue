<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
      <!-- 对话框头部 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h2 class="text-2xl font-bold text-gray-900">
          {{ character ? '编辑角色' : '创建角色' }}
        </h2>
        <div class="flex items-center gap-2">
          <button
            v-if="!character"
            @click="handleAIRecommend"
            :disabled="recommending"
            type="button"
            class="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="recommending">🤖 AI生成中...</span>
            <span v-else>✨ AI智能推荐</span>
          </button>
          <button
            @click="$emit('close')"
            class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <span class="text-2xl text-gray-500">×</span>
          </button>
        </div>
      </div>

      <!-- 表单内容 -->
      <div class="flex-1 overflow-y-auto p-6">
        <!-- AI推荐结果展示 -->
        <div v-if="recommendations.length > 0" class="mb-6">
          <div class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-lg p-4 border border-purple-200">
            <h3 class="text-lg font-semibold text-gray-900 mb-3 flex items-center gap-2">
              <span>🤖</span>
              <span>AI推荐角色（点击应用）</span>
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div
                v-for="(rec, index) in recommendations"
                :key="index"
                @click="applyRecommendation(rec)"
                class="bg-white rounded-lg p-3 cursor-pointer hover:shadow-lg transition-shadow border-2 border-transparent hover:border-purple-500"
              >
                <div class="font-semibold text-gray-900 mb-1">{{ rec.name }}</div>
                <div class="text-xs text-gray-600 mb-2">{{ translateRoleType(rec.roleType) }} | {{ translateGender(rec.gender) }} | {{ rec.age }}岁</div>
                <div class="text-xs text-gray-600 line-clamp-2">{{ rec.personality }}</div>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 基本信息 -->
          <div class="bg-blue-50 rounded-lg p-4">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">基本信息</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  角色名称 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="formData.name"
                  type="text"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="请输入角色名称"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  角色类型 <span class="text-red-500">*</span>
                </label>
                <select
                  v-model="formData.roleType"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">请选择</option>
                  <option value="PROTAGONIST">主角</option>
                  <option value="ANTAGONIST">反派</option>
                  <option value="SUPPORTING">配角</option>
                  <option value="MINOR">次要角色</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">性别</label>
                <select
                  v-model="formData.gender"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">未设置</option>
                  <option value="MALE">男</option>
                  <option value="FEMALE">女</option>
                  <option value="OTHER">其他</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">年龄</label>
                <input
                  v-model.number="formData.age"
                  type="number"
                  min="0"
                  max="999"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="年龄"
                />
              </div>
            </div>
          </div>

          <!-- 详细描述 -->
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">性格特征</label>
              <textarea
                v-model="formData.personality"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="描述角色的性格特点、行为方式等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">背景故事</label>
              <textarea
                v-model="formData.background"
                rows="4"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="角色的过去经历、家庭背景、重要事件等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">外貌描述</label>
              <textarea
                v-model="formData.appearance"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="角色的外貌特征、衣着打扮等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">能力特长</label>
              <textarea
                v-model="formData.abilities"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="角色的技能、特殊能力、擅长的领域等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">动机目标</label>
              <textarea
                v-model="formData.motivation"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="角色的动机、追求的目标、想要达成的事情..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">角色弧光</label>
              <textarea
                v-model="formData.arc"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="角色在故事中的成长变化、转折点等..."
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">备注</label>
              <textarea
                v-model="formData.notes"
                rows="2"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="其他需要记录的信息..."
              ></textarea>
            </div>
          </div>
        </form>
      </div>

      <!-- 对话框底部 -->
      <div class="px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
        <button
          @click="$emit('close')"
          type="button"
          class="px-6 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors font-medium"
        >
          取消
        </button>
        <button
          @click="handleSubmit"
          type="submit"
          :disabled="saving"
          class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import api from '../api'

const props = defineProps({
  character: Object,
  novelId: Number
})

const emit = defineEmits(['close', 'save'])

const formData = ref({
  name: '',
  roleType: '',
  gender: '',
  age: null,
  personality: '',
  background: '',
  appearance: '',
  abilities: '',
  motivation: '',
  arc: '',
  notes: ''
})

const saving = ref(false)
const recommending = ref(false)
const recommendations = ref([])

// 监听character变化，填充表单
watch(() => props.character, (newValue) => {
  if (newValue) {
    formData.value = { ...newValue }
  }
}, { immediate: true })

const handleSubmit = async () => {
  if (!formData.value.name || !formData.value.roleType) {
    alert('请填写必填项')
    return
  }

  saving.value = true
  try {
    await emit('save', formData.value)
  } catch (error) {
    alert('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const handleAIRecommend = async () => {
  if (!props.novelId) {
    alert('缺少小说ID')
    return
  }

  recommending.value = true
  try {
    const result = await api.characters.recommend({
      novelId: props.novelId,
      count: 3
    })
    recommendations.value = result || []
    if (recommendations.value.length === 0) {
      alert('AI推荐失败，请稍后重试')
    }
  } catch (error) {
    console.error('AI推荐失败:', error)
    alert('AI推荐失败: ' + error.message)
  } finally {
    recommending.value = false
  }
}

const applyRecommendation = (rec) => {
  formData.value = {
    name: rec.name,
    roleType: rec.roleType,
    gender: rec.gender,
    age: rec.age,
    personality: rec.personality,
    background: rec.background,
    appearance: rec.appearance,
    abilities: rec.abilities,
    motivation: rec.motivation,
    arc: rec.arc,
    notes: ''
  }
  recommendations.value = []
}

const translateRoleType = (type) => {
  const map = {
    'PROTAGONIST': '主角',
    'ANTAGONIST': '反派',
    'SUPPORTING': '配角',
    'MINOR': '次要角色'
  }
  return map[type] || type
}

const translateGender = (gender) => {
  const map = {
    'MALE': '男',
    'FEMALE': '女',
    'OTHER': '其他'
  }
  return map[gender] || '未知'
}
</script>
