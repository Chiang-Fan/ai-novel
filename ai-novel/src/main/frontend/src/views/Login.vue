<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo & Title -->
      <div class="text-center">
        <div class="flex justify-center mb-4">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
        </div>
        <h2 class="text-3xl font-bold text-gray-900">AI 智能小说创作</h2>
        <p class="mt-2 text-sm text-gray-600">
          {{ isLogin ? '登录您的账户' : '创建新账户开始创作' }}
        </p>
      </div>

      <!-- Form Card -->
      <div class="bg-white rounded-2xl shadow-xl p-8">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Username -->
          <Input
            id="username"
            label="用户名"
            v-model="formData.username"
            placeholder="请输入用户名"
            :error="errors.username"
            required
            @blur="validateUsername"
          />

          <!-- Email (只在注册时显示) -->
          <Input
            v-if="!isLogin"
            id="email"
            label="邮箱"
            type="email"
            v-model="formData.email"
            placeholder="请输入邮箱地址"
            :error="errors.email"
            required
            @blur="validateEmail"
          />

          <!-- Password -->
          <Input
            id="password"
            label="密码"
            type="password"
            v-model="formData.password"
            :placeholder="isLogin ? '请输入密码' : '请设置密码（至少6位）'"
            :error="errors.password"
            required
            @blur="validatePassword"
          />

          <!-- Confirm Password (只在注册时显示) -->
          <Input
            v-if="!isLogin"
            id="confirmPassword"
            label="确认密码"
            type="password"
            v-model="formData.confirmPassword"
            placeholder="请再次输入密码"
            :error="errors.confirmPassword"
            required
            @blur="validateConfirmPassword"
          />

          <!-- Error Message -->
          <div v-if="authStore.error" class="bg-red-50 border border-red-200 rounded-lg p-3">
            <p class="text-sm text-red-600">{{ authStore.error }}</p>
          </div>

          <!-- Submit Button -->
          <Button
            type="submit"
            variant="primary"
            :loading="authStore.loading"
            class="w-full"
          >
            {{ isLogin ? '登录' : '注册' }}
          </Button>

          <!-- Toggle Login/Register -->
          <div class="text-center">
            <button
              type="button"
              @click="toggleMode"
              class="text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              {{ isLogin ? '还没有账户？立即注册' : '已有账户？立即登录' }}
            </button>
          </div>

          <!-- Guest Mode -->
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">或</span>
            </div>
          </div>

          <Button
            type="button"
            variant="ghost"
            @click="guestMode"
            class="w-full"
          >
            游客模式（无需登录）
          </Button>
        </form>
      </div>

      <!-- Footer -->
      <p class="text-center text-xs text-gray-500">
        使用 AI 技术助力您的小说创作之旅
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Input from '../components/Input.vue'
import Button from '../components/Button.vue'

const router = useRouter()
const authStore = useAuthStore()

const isLogin = ref(true)
const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateUsername = () => {
  if (!formData.username) {
    errors.username = '请输入用户名'
    return false
  }
  if (formData.username.length < 3) {
    errors.username = '用户名至少3个字符'
    return false
  }
  errors.username = ''
  return true
}

const validateEmail = () => {
  if (!isLogin.value) {
    if (!formData.email) {
      errors.email = '请输入邮箱'
      return false
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(formData.email)) {
      errors.email = '请输入有效的邮箱地址'
      return false
    }
  }
  errors.email = ''
  return true
}

const validatePassword = () => {
  if (!formData.password) {
    errors.password = '请输入密码'
    return false
  }
  if (formData.password.length < 6) {
    errors.password = '密码至少6个字符'
    return false
  }
  errors.password = ''
  return true
}

const validateConfirmPassword = () => {
  if (!isLogin.value) {
    if (!formData.confirmPassword) {
      errors.confirmPassword = '请再次输入密码'
      return false
    }
    if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = '两次输入的密码不一致'
      return false
    }
  }
  errors.confirmPassword = ''
  return true
}

const validateForm = () => {
  const validations = [
    validateUsername(),
    validatePassword()
  ]
  
  if (!isLogin.value) {
    validations.push(validateEmail(), validateConfirmPassword())
  }
  
  return validations.every(v => v === true)
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  let success
  if (isLogin.value) {
    success = await authStore.login({
      username: formData.username,
      password: formData.password
    })
  } else {
    success = await authStore.register({
      username: formData.username,
      email: formData.email,
      password: formData.password
    })
  }

  if (success) {
    router.push('/')
  }
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  // 清空表单
  Object.keys(errors).forEach(key => errors[key] = '')
}

const guestMode = () => {
  // 游客模式直接跳转到首页
  router.push('/')
}
</script>
