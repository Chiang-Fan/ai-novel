import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import NovelList from '../views/NovelList.vue'
import NovelCreate from '../views/NovelCreate.vue'
import NovelDetail from '../views/NovelDetail.vue'
import ChapterWriteEnhanced from '../views/ChapterWriteEnhanced.vue'
import ChapterSmartCreate from '../views/ChapterSmartCreate.vue'
import SmartWriting from '../views/SmartWriting.vue'
import CharacterList from '../views/CharacterList.vue'
import SceneList from '../views/SceneList.vue'
import OutlineEditor from '../views/OutlineEditor.vue'
import EditHistory from '../views/EditHistory.vue'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'NovelList',
    component: NovelList,
    meta: { title: '小说列表' }
  },
  {
    path: '/create',
    name: 'NovelCreate',
    component: NovelCreate,
    meta: { title: '创建小说' }
  },
  {
    path: '/novel/:id',
    name: 'NovelDetail',
    component: NovelDetail,
    meta: { title: '小说详情' }
  },
  {
    path: '/novel/:id/write',
    name: 'ChapterWrite',
    component: ChapterWriteEnhanced,
    meta: { title: 'AI续写' }
  },
  {
    path: '/novel/:id/smart-create',
    name: 'ChapterSmartCreate',
    component: ChapterSmartCreate,
    meta: { title: '智能章节创建' }
  },
  {
    path: '/novel/:id/smart-writing',
    name: 'SmartWriting',
    component: SmartWriting,
    meta: { title: '智能创作工作台' }
  },
  {
    path: '/novel/:id/characters',
    name: 'CharacterList',
    component: CharacterList,
    meta: { title: '角色管理' }
  },
  {
    path: '/novel/:id/scenes',
    name: 'SceneList',
    component: SceneList,
    meta: { title: '场景管理' }
  },
  {
    path: '/novel/:id/outline',
    name: 'OutlineEditor',
    component: OutlineEditor,
    meta: { title: '大纲管理' }
  },
  {
    path: '/novel/:id/history',
    name: 'EditHistory',
    component: EditHistory,
    meta: { title: '编辑历史' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - AI智能小说创作` : 'AI智能小说创作系统'
  next()
})

export default router
