/**
 * 静态路由表
 *
 * 路由分类：
 * - 公开路由：/login, /403, /404
 * - 受保护路由：需要登录
 * - 角色路由：根据角色显示菜单
 *
 * 懒加载用 () => import('@/views/...')，按需加载，减小首屏
 */
import type { RouteRecordRaw } from 'vue-router'

const Layout = () => import('@/components/layout/AppLayout.vue')

export const routes: RouteRecordRaw[] = [
  // 公开路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', hidden: true },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true },
  },

  // 受保护路由（统一用 AppLayout 包裹）
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true },
      },
    ],
  },

  // ===== 打印任务（M2 - B 负责）=====
  {
    path: '/task',
    component: Layout,
    redirect: '/task/my',
    meta: { title: '打印任务', icon: 'Printer' },
    children: [
      {
        path: 'apply',
        name: 'TaskApply',
        component: () => import('@/views/task/apply/index.vue'),
        meta: { title: '提交申请', icon: 'Plus' },
      },
      {
        path: 'my',
        name: 'TaskMy',
        component: () => import('@/views/task/my/index.vue'),
        meta: { title: '我的任务', icon: 'List' },
      },
      {
        path: 'queue',
        name: 'TaskQueue',
        component: () => import('@/views/task/queue/index.vue'),
        meta: { title: '打印队列', icon: 'Operation' },
      },
      {
        path: ':id',
        name: 'TaskDetail',
        component: () => import('@/views/task/detail/index.vue'),
        meta: { title: '任务详情', hidden: true, activeMenu: '/task/my' },
      },
    ],
  },

  // ===== 项目管理（M3 - B 负责）=====
  {
    path: '/project',
    component: Layout,
    redirect: '/project/list',
    meta: { title: '项目管理', icon: 'Folder' },
    children: [
      {
        path: 'list',
        name: 'ProjectList',
        component: () => import('@/views/project/list/index.vue'),
        meta: { title: '项目列表', icon: 'Files' },
      },
      {
        path: ':id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/detail/index.vue'),
        meta: { title: '项目详情', hidden: true, activeMenu: '/project/list' },
      },
    ],
  },

  // ===== 作品库（M4 - C 负责）=====
  {
    path: '/artwork',
    component: Layout,
    redirect: '/artwork/list',
    meta: { title: '作品库', icon: 'Picture' },
    children: [
      {
        path: 'list',
        name: 'ArtworkList',
        component: () => import('@/views/artwork/list/index.vue'),
        meta: { title: '作品列表', icon: 'PictureFilled' },
      },
      {
        path: 'my',
        name: 'ArtworkMy',
        component: () => import('@/views/artwork/my/index.vue'),
        meta: { title: '我的作品', icon: 'User' },
      },
      {
        path: ':id',
        name: 'ArtworkDetail',
        component: () => import('@/views/artwork/detail/index.vue'),
        meta: { title: '作品详情', hidden: true, activeMenu: '/artwork/list' },
      },
    ],
  },

  // ===== 管理端 =====
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/dashboard',
    meta: { title: '管理后台', icon: 'Setting', roles: [1, 2] },  // 仅社长/技术骨干
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '统计看板', icon: 'DataAnalysis', roles: [1, 2] },
      },
      // 管理端任务（**B**）
      {
        path: 'task/pending',
        name: 'AdminTaskPending',
        component: () => import('@/views/admin/task/pending/index.vue'),
        meta: { title: '待审批任务', icon: 'Bell', roles: [1, 2], parent: '/admin/task' },
      },
      {
        path: 'task/active',
        name: 'AdminTaskActive',
        component: () => import('@/views/admin/task/active/index.vue'),
        meta: { title: '进行中任务', icon: 'Loading', roles: [1, 2], parent: '/admin/task' },
      },
      {
        path: 'task/history',
        name: 'AdminTaskHistory',
        component: () => import('@/views/admin/task/history/index.vue'),
        meta: { title: '历史任务', icon: 'Finished', roles: [1, 2], parent: '/admin/task' },
      },
      {
        path: 'task/stats',
        name: 'AdminTaskStats',
        component: () => import('@/views/admin/task/stats/index.vue'),
        meta: { title: '任务统计', icon: 'TrendCharts', roles: [1, 2], parent: '/admin/task' },
      },
      // 管理端项目（**B**）
      {
        path: 'project/create',
        name: 'AdminProjectCreate',
        component: () => import('@/views/admin/project/create/index.vue'),
        meta: { title: '创建项目', icon: 'Plus', roles: [1, 2] },
      },
      {
        path: 'project/manage',
        name: 'AdminProjectManage',
        component: () => import('@/views/admin/project/manage/index.vue'),
        meta: { title: '项目管理', icon: 'FolderOpened', roles: [1, 2] },
      },
      // 作品库管理（C）
      {
        path: 'artwork/recommend',
        name: 'AdminArtworkRecommend',
        component: () => import('@/views/admin/artwork/recommend/index.vue'),
        meta: { title: '作品推荐', icon: 'Star', roles: [1, 2] },
      },
      // 设备耗材（C）
      {
        path: 'printer',
        name: 'AdminPrinter',
        component: () => import('@/views/admin/printer/list/index.vue'),
        meta: { title: '打印机管理', icon: 'Box', roles: [1, 2] },
      },
      {
        path: 'material',
        name: 'AdminMaterial',
        component: () => import('@/views/admin/material/list/index.vue'),
        meta: { title: '耗材库存', icon: 'Goods', roles: [1, 2] },
      },
      {
        path: 'material/inbound',
        name: 'AdminMaterialInbound',
        component: () => import('@/views/admin/material/inbound/index.vue'),
        meta: { title: '耗材入库', icon: 'Upload', roles: [1, 2] },
      },
      // 成员管理（C）
      {
        path: 'member',
        name: 'AdminMember',
        component: () => import('@/views/admin/member/index.vue'),
        meta: { title: '成员管理', icon: 'UserFilled', roles: [1, 2] },
      },
      // 系统日志（C）
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/admin/log/index.vue'),
        meta: { title: '系统日志', icon: 'Document', roles: [1] },  // 仅社长
      },
    ],
  },

  // 兜底 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true },
  },
]