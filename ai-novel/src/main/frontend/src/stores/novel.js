import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useNovelStore = defineStore('novel', () => {
  // State
  const novels = ref([])
  const currentNovel = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Actions
  const fetchNovels = async () => {
    loading.value = true
    error.value = null
    try {
      // API拦截器已经解包，直接返回data
      novels.value = await api.novels.list()
    } catch (err) {
      error.value = '获取小说列表失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  const fetchNovel = async (id) => {
    loading.value = true
    error.value = null
    try {
      // API拦截器已经解包，直接返回data
      const novel = await api.novels.get(id)
      currentNovel.value = novel
      return novel
    } catch (err) {
      error.value = '获取小说详情失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  const createNovel = async (novelData) => {
    loading.value = true
    error.value = null
    try {
      // API拦截器已经解包，直接返回data
      const novel = await api.novels.create(novelData)
      novels.value.unshift(novel)
      return novel
    } catch (err) {
      error.value = '创建小说失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateNovel = async (id, novelData) => {
    loading.value = true
    error.value = null
    try {
      // API拦截器已经解包，直接返回data
      const novel = await api.novels.update(id, novelData)
      const index = novels.value.findIndex(n => n.id === id)
      if (index !== -1) {
        novels.value[index] = novel
      }
      if (currentNovel.value?.id === id) {
        currentNovel.value = novel
      }
      return novel
    } catch (err) {
      error.value = '更新小说失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteNovel = async (id) => {
    loading.value = true
    error.value = null
    try {
      // API拦截器已经解包，删除操作返回boolean或null
      await api.novels.delete(id)
      novels.value = novels.value.filter(n => n.id !== id)
      if (currentNovel.value?.id === id) {
        currentNovel.value = null
      }
      return true
    } catch (err) {
      error.value = '删除小说失败'
      console.error(err)
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    novels,
    currentNovel,
    loading,
    error,
    fetchNovels,
    fetchNovel,
    createNovel,
    updateNovel,
    deleteNovel
  }
})
