<template>
  <nav class="bg-white shadow-sm border-b sticky top-0 z-40">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <!-- Logo & Brand -->
        <div class="flex items-center">
          <router-link to="/" class="flex items-center gap-3">
            <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
            </div>
            <span class="text-xl font-bold text-gray-900">AI 小说创作</span>
          </router-link>
        </div>

        <!-- Navigation Links -->
        <div class="hidden sm:flex items-center gap-6">
          <router-link
            to="/"
            class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            active-class="text-blue-600"
          >
            我的作品
          </router-link>
          <router-link
            to="/create"
            class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            active-class="text-blue-600"
          >
            创建新作
          </router-link>
          <router-link
            to="/smart-novel-create"
            class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            active-class="text-blue-600"
          >
            智能新建
          </router-link>
        </div>

        <!-- User Menu -->
        <div class="flex items-center gap-4">
          <template v-if="authStore.isAuthenticated">
            <div class="relative" ref="userMenuRef">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <div class="w-8 h-8 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center text-white font-medium">
                  {{ authStore.userName[0].toUpperCase() }}
                </div>
                <span class="text-sm font-medium text-gray-700">{{ authStore.userName }}</span>
                <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>

              <!-- Dropdown Menu -->
              <transition name="dropdown">
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border py-2"
                >
                  <button
                    @click="handleLogout"
                    class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </transition>
            </div>
          </template>
          <template v-else>
            <router-link
              to="/login"
              class="px-4 py-2 text-blue-600 font-medium hover:text-blue-700 transition-colors"
            >
              登录
            </router-link>
          </template>
        </div>
      </div>
    </div>

    <!-- Mobile Menu Button (隐藏在此简化版本) -->
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const showUserMenu = ref(false)
const userMenuRef = ref(null)

const handleLogout = () => {
  authStore.logout()
  showUserMenu.value = false
  router.push('/login')
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.dropdown-enter-active, .dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
