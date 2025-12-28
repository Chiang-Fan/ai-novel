import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import NovelList from '../views/NovelList.vue'
import CreateNovel from '../views/CreateNovel.vue'
import NovelDetail from '../views/NovelDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/novels',
      name: 'NovelList',
      component: NovelList
    },
    {
      path: '/create',
      name: 'CreateNovel',
      component: CreateNovel
    },
    {
      path: '/novels/:id',
      name: 'NovelDetail',
      component: NovelDetail
    }
  ]
})

export default router