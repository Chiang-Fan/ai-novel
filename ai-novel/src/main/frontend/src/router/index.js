import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import NovelList from '../views/NovelList.vue'
import NovelCreate from '../views/NovelCreate.vue'
import NovelDetail from '../views/NovelDetail.vue'
import ChapterWriteEnhanced from '../views/ChapterWriteEnhanced.vue'
import ChapterEditWithContinuation from '../views/ChapterEditWithContinuation.vue'
import ChapterSmartCreate from '../views/ChapterSmartCreate.vue'
import SmartWriting from '../views/SmartWriting.vue'
import ConversationPanel from '../views/ConversationPanel.vue'
import VersionManager from '../views/VersionManager.vue'
import CharacterGrowthPanel from '../views/CharacterGrowthPanel.vue'
import SceneManagementPanel from '../views/SceneManagementPanel.vue'
import RelationshipGraphPanel from '../views/RelationshipGraphPanel.vue'
import AtmosphereGenerationPanel from '../views/AtmosphereGenerationPanel.vue'
import CharacterList from '../views/CharacterList.vue'
import SceneList from '../views/SceneList.vue'
import OutlineEditor from '../views/OutlineEditor.vue'
import EditHistory from '../views/EditHistory.vue'
import WritingStyleManager from '../views/WritingStyleManager.vue'
import PlotHookManager from '../views/PlotHookManager.vue'
import ChapterAnalysisViewer from '../views/ChapterAnalysisViewer.vue'
import WorldSettingManager from '../views/WorldSettingManager.vue'
import TextImportManager from '../views/TextImportManager.vue'
import MigrationManager from '../views/MigrationManager.vue'
import Login from '../views/Login.vue'
import OptimizationManager from '../views/OptimizationManager.vue'
import PaceControlPanel from '../views/PaceControlPanel.vue'
import PlotSimulationPanel from '../views/PlotSimulationPanel.vue'
import SuggestionManager from '../views/SuggestionManager.vue'

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
  },
  {
    path: '/novel/:id/writing-style',
    name: 'WritingStyleManager',
    component: WritingStyleManager,
    meta: { title: '文风管理' }
  },
  {
    path: '/novel/:id/plot-hooks',
    name: 'PlotHookManager',
    component: PlotHookManager,
    meta: { title: '伏笔管理' }
  },
  {
    path: '/novel/:id/chapter-analysis',
    name: 'ChapterAnalysisViewer',
    component: ChapterAnalysisViewer,
    meta: { title: '章节分析' }
  },
  {
    path: '/novel/:novelId/chapter/:chapterId/edit',
    name: 'ChapterEditWithContinuation',
    component: ChapterEditWithContinuation,
    meta: { title: '章节编辑' }
  },
  {
    path: '/novel/:novelId/conversation',
    name: 'ConversationPanel',
    component: ConversationPanel,
    meta: { title: 'AI 对话助手' }
  },
  {
    path: '/chapter/:chapterId/versions',
    name: 'VersionManager',
    component: VersionManager,
    meta: { title: '版本管理' }
  },
  {
    path: '/character/:characterId/growth',
    name: 'CharacterGrowthPanel',
    component: CharacterGrowthPanel,
    meta: { title: '角色成长系统' }
  },
  {
    path: '/scene/:sceneId/management',
    name: 'SceneManagementPanel',
    component: SceneManagementPanel,
    meta: { title: '场景动态管理' }
  },
  {
    path: '/scene/:sceneId/atmosphere',
    name: 'AtmosphereGenerationPanel',
    component: AtmosphereGenerationPanel,
    meta: { title: '场景氛围生成' }
  },
  {
    path: '/novel/:id/world-settings',
    name: 'WorldSettingManager',
    component: WorldSettingManager,
    meta: { title: '世界设定管理' }
  },
  {
    path: '/novel/:id/text-import',
    name: 'TextImportManager',
    component: TextImportManager,
    meta: { title: '文本导入管理' }
  },
  {
    path: '/migration',
    name: 'MigrationManager',
    component: MigrationManager,
    meta: { title: '风格迁移管理' }
  },
  {
    path: '/novel/:id/optimization',
    name: 'OptimizationManager',
    component: OptimizationManager,
    meta: { title: '优化管理' }
  },
  {
    path: '/novel/:id/pace-control',
    name: 'PaceControlPanel',
    component: PaceControlPanel,
    meta: { title: '节奏控制' }
  },
  {
    path: '/novel/:id/plot-simulation',
    name: 'PlotSimulationPanel',
    component: PlotSimulationPanel,
    meta: { title: '情节模拟' }
  },
  {
    path: '/novel/:id/suggestions',
    name: 'SuggestionManager',
    component: SuggestionManager,
    meta: { title: '建议管理' }
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