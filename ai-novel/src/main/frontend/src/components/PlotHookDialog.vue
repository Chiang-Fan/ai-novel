<template>
  <div v-if="modelValue" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
      <!-- 头部 -->
      <div class="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
        <h2 class="text-2xl font-bold text-gray-800">
          {{ hook ? '编辑伏笔' : '新建伏笔' }}
        </h2>
        <button
          @click="close"
          class="text-gray-500 hover:text-gray-700">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 表单内容 -->
      <div class="px-6 py-4 space-y-4">
        <!-- 标题 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">标题 *</label>
          <input
            v-model="formData.title"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="简短描述伏笔内容"
            required
          />
        </div>

        <!-- 描述 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">详细描述 *</label>
          <textarea
            v-model="formData.description"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="详细说明伏笔的背景和目的"
            required
          ></textarea>
        </div>

        <!-- 两列布局 -->
        <div class="grid grid-cols-2 gap-4">
          <!-- 伏笔类型 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">伏笔类型 *</label>
            <select
              v-model="formData.type"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent">
              <option value="EXPLICIT">明示伏笔</option>
              <option value="IMPLICIT">暗示伏笔</option>
              <option value="CHEKHOV_GUN">契诃夫的枪</option>
            </select>
            <p class="text-xs text-gray-500 mt-1">
              明示: 明确提及 | 暗示: 隐晦暗示 | 契诃夫枪: 必然出现
            </p>
          </div>

          <!-- 优先级 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              优先级 ({{ formData.priority }})
            </label>
            <input
              v-model.number="formData.priority"
              type="range"
              min="1"
              max="10"
              class="w-full"
            />
            <div class="flex justify-between text-xs text-gray-500">
              <span>低</span>
              <span>高</span>
            </div>
          </div>
        </div>

        <!-- 章节信息 -->
        <div class="grid grid-cols-2 gap-4">
          <!-- 埋设章节 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">埋设章节号 *</label>
            <input
              v-model.number="formData.plantedInChapter"
              type="number"
              min="1"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              placeholder="在哪章埋设"
              required
            />
          </div>

          <!-- 预期揭示章节 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">预期揭示章节</label>
            <input
              v-model.number="formData.expectedChapter"
              type="number"
              min="1"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              placeholder="建议揭示时间"
            />
          </div>
        </div>

        <!-- 相关角色 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">相关角色</label>
          <input
            v-model="relatedCharactersInput"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="用逗号分隔，例如：张三, 李四, 王五"
          />
          <p class="text-xs text-gray-500 mt-1">多个角色请用逗号分隔</p>
        </div>

        <!-- 内容引用 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">内容引用</label>
          <textarea
            v-model="formData.contentReference"
            rows="2"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="粘贴原文中埋设伏笔的片段（可选）"
          ></textarea>
        </div>

        <!-- 备注 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">备注</label>
          <textarea
            v-model="formData.notes"
            rows="2"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            placeholder="其他说明（可选）"
          ></textarea>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="sticky bottom-0 bg-gray-50 px-6 py-4 flex justify-end space-x-3 border-t">
        <button
          @click="close"
          class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium">
          取消
        </button>
        <button
          @click="save"
          :disabled="!isValid"
          class="px-6 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:shadow-lg transition disabled:opacity-50 font-medium">
          {{ hook ? '保存' : '创建' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlotHookDialog',
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    hook: {
      type: Object,
      default: null
    },
    novelId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      formData: {
        novelId: this.novelId,
        title: '',
        description: '',
        type: 'EXPLICIT',
        priority: 5,
        plantedInChapter: 1,
        expectedChapter: null,
        contentReference: '',
        notes: '',
        relatedCharacters: []
      },
      relatedCharactersInput: ''
    }
  },
  computed: {
    isValid() {
      return this.formData.title && this.formData.description && this.formData.plantedInChapter > 0
    }
  },
  watch: {
    hook: {
      immediate: true,
      handler(newHook) {
        if (newHook) {
          this.formData = {
            ...newHook,
            novelId: this.novelId
          }
          this.relatedCharactersInput = newHook.relatedCharacters?.join(', ') || ''
        } else {
          this.resetForm()
        }
      }
    },
    modelValue(val) {
      if (!val) {
        this.resetForm()
      }
    }
  },
  methods: {
    close() {
      this.$emit('update:modelValue', false)
    },

    save() {
      // 处理角色列表
      if (this.relatedCharactersInput) {
        this.formData.relatedCharacters = this.relatedCharactersInput
          .split(',')
          .map(s => s.trim())
          .filter(s => s.length > 0)
      } else {
        this.formData.relatedCharacters = []
      }

      this.$emit('save', this.formData)
    },

    resetForm() {
      this.formData = {
        novelId: this.novelId,
        title: '',
        description: '',
        type: 'EXPLICIT',
        priority: 5,
        plantedInChapter: 1,
        expectedChapter: null,
        contentReference: '',
        notes: '',
        relatedCharacters: []
      }
      this.relatedCharactersInput = ''
    }
  }
}
</script>
