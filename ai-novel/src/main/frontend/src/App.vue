<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Navbar (只在非登录页显示) -->
    <Navbar v-if="$route.path !== '/login'" />
    
    <!-- Main Content -->
    <main class="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view />
    </main>
    
    <!-- Footer -->
    <footer class="bg-white border-t mt-12">
      <div class="container mx-auto px-6 py-6 text-center text-gray-600 text-sm">
        <p>AI智能小说创作系统 v2.1.0 | 基于 Spring Boot 3 + Vue 3 + AI</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import Navbar from './components/Navbar.vue'

const authStore = useAuthStore()

onMounted(() => {
  // 初始化认证状态
  authStore.initAuth()
  console.log('AI智能小说创作系统启动成功')
})
</script>

<style>
@import './style.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
