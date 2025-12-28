<template>
  <div class="create-novel">
    <div class="page-header">
      <h1>创建新小说</h1>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="小说标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入小说标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="小说类型" prop="genre">
              <el-select v-model="form.genre" placeholder="请选择小说类型">
                <el-option label="玄幻" value="玄幻" />
                <el-option label="都市" value="都市" />
                <el-option label="历史" value="历史" />
                <el-option label="科幻" value="科幻" />
                <el-option label="武侠" value="武侠" />
                <el-option label="仙侠" value="仙侠" />
                <el-option label="言情" value="言情" />
                <el-option label="悬疑" value="悬疑" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="小说简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请简要描述您的小说内容、主要情节和发展方向"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="故事大纲">
          <el-input
            v-model="form.outline"
            type="textarea"
            :rows="6"
            placeholder="建议简要概括故事的主线和主要情节走向"
          />
        </el-form-item>

        <el-form-item label="样本正文">
          <el-input
            v-model="form.sampleText"
            type="textarea"
            :rows="8"
            placeholder="粘贴一段您的创作样本，或输入您期望的写作风格示例。这将用于AI学习您的创作风格..."
          />
          <div class="form-tip">
            💡 提示：如果您不会写，可以先创建小说，然后在详情页让AI帮您生成样本正文
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            创建小说
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { novelApi } from '../api'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  title: '',
  genre: '',
  description: '',
  outline: '',
  sampleText: ''
})

const rules = {
  title: [
    { required: true, message: '请输入小说标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度应在 1 到 100 个字符', trigger: 'blur' }
  ],
  genre: [
    { required: true, message: '请选择小说类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入小说简介', trigger: 'blur' },
    { min: 10, max: 500, message: '简介长度应在 10 到 500 个字符', trigger: 'blur' }
  ]
}

const submitForm = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true
    
    const novelData = {
      title: form.title,
      genre: form.genre,
      description: form.description,
      outline: form.outline,
      sampleText: form.sampleText,
      status: 'ONGOING'
    }

    const result = await novelApi.create(novelData)
    
    ElMessage.success('小说创建成功！')
    router.push(`/novels/${result.id}`)
    
  } catch (error) {
    console.error('创建小说失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-novel {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.el-form-item {
  margin-bottom: 24px;
}
</style>