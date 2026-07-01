/**
 * 静态路由表
 *
 * 路由分类：
 * - 公开路由：/login, /403, /404
 * - 受保护路由：需要登录
 * - 角色路由：根据角色显示菜单
 *
 * 路由级 transition 动效（route.meta.transition）：
 * - page-fade        纯淡入（登录、错误页）
 * - page-slide-up    淡入+上滑（首页/统计看板）
 * - page-zoom        淡入+缩放（列表页）
 * - page-slide-left  从右滑入（详情页）
 * - page-flip        3D 翻转（申请/创建/编辑页）
 * - page-slide-down  从上滑入（设置/配置类）
 * - page             默认（fade-in-up）
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
    meta: { title: '登录', hidden: true, transition: 'page-fade' },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', hidden: true, transition: 'page-fade' },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true, transition: 'page-fade' },
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
        meta: { title: '首页', icon: 'HomeFilled', transition: 'page-slide-up' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true, transition: 'page-slide-left' },
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
        meta: { title: '提交申请', icon: 'Plus', transition: 'page-flip' },
      },
      {
        path: 'my',
        name: 'TaskMy',
        component: () => import('@/views/task/my/index.vue'),
        meta: { title: '我的任务', icon: 'List', transition: 'page-zoom' },
      },
      {
        path: 'queue',
        name: 'TaskQueue',
        component: () => import('@/views/task/queue/index.vue'),
        meta: { title: '打印队列', icon: 'Operation', transition: 'page-zoom' },
      },
      {
        path: ':id',
        name: 'TaskDetail',
        component: () => import('@/views/task/detail/index.vue'),
        meta: { title: '任务详情', hidden: true, activeMenu: '/task/my', transition: 'page-slide-left' },
      },
    ],
  },

  // ===== 项目管理（M3 - B 负责）=====
  // ✅ v2.2 round 4 修复（用户反馈）：合并"项目列表"和"项目管理（管理端）"为单一入口
  // ✅ v2.2 round 5 修复（用户反馈）：普通社员作为项目负责人能编辑自己项目，但 sidebar 没有"创建项目"菜单
  //    修复：把"创建项目"路由从 /admin/project/create 移到顶级 /project/create（roles [1, 2, 3]）
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
        meta: { title: '项目列表', icon: 'Files', transition: 'page-zoom' },
      },
      {
        path: 'create',
        name: 'ProjectCreate',
        component: () => import('@/views/admin/project/create/index.vue'),
        meta: { title: '创建项目', icon: 'Plus', roles: [1, 2, 3], transition: 'page-flip' },
      },
      {
        path: ':id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/detail/index.vue'),
        meta: { title: '项目详情', hidden: true, activeMenu: '/project/list', transition: 'page-slide-left' },
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
        meta: { title: '作品列表', icon: 'PictureFilled', transition: 'page-zoom' },
      },
      {
        path: 'my',
        name: 'ArtworkMy',
        component: () => import('@/views/artwork/my/index.vue'),
        meta: { title: '我的作品', icon: 'User', transition: 'page-zoom' },
      },
      {
        path: 'create',
        name: 'ArtworkCreate',
        component: () => import('@/views/artwork/create/index.vue'),
        meta: { title: '登记作品', icon: 'Plus', activeMenu: '/artwork/my', transition: 'page-flip' },
      },
      {
        path: 'edit/:id',
        name: 'ArtworkEdit',
        component: () => import('@/views/artwork/edit/index.vue'),
        meta: { title: '编辑作品', hidden: true, activeMenu: '/artwork/my', transition: 'page-flip' },
      },
      {
        path: ':id',
        name: 'ArtworkDetail',
        component: () => import('@/views/artwork/detail/index.vue'),
        meta: { title: '作品详情', hidden: true, activeMenu: '/artwork/list', transition: 'page-slide-left' },
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
        meta: { title: '统计看板', icon: 'DataAnalysis', roles: [1, 2], transition: 'page-slide-up' },
      },
      // 管理端任务（**B**）
      {
        path: 'task/pending',
        name: 'AdminTaskPending',
        component: () => import('@/views/admin/task/pending/index.vue'),
        meta: { title: '待审批任务', icon: 'Bell', roles: [1, 2], parent: '/admin/task', transition: 'page-zoom' },
      },
      {
        path: 'task/active',
        name: 'AdminTaskActive',
        component: () => import('@/views/admin/task/active/index.vue'),
        meta: { title: '进行中任务', icon: 'Loading', roles: [1, 2], parent: '/admin/task', transition: 'page-zoom' },
      },
      {
        path: 'task/history',
        name: 'AdminTaskHistory',
        component: () => import('@/views/admin/task/history/index.vue'),
        meta: { title: '历史任务', icon: 'Finished', roles: [1, 2], parent: '/admin/task', transition: 'page-zoom' },
      },
      {
        path: 'task/stats',
        name: 'AdminTaskStats',
        component: () => import('@/views/admin/task/stats/index.vue'),
        meta: { title: '任务统计', icon: 'TrendCharts', roles: [1, 2], parent: '/admin/task', transition: 'page-slide-up' },
      },
      // ✅ v2.2 round 5 修复："创建项目"已经从 /admin 移到顶级 /project/create
      //    旧路径 /admin/project/create → 跳 /project/create（保留兼容老链接）
      {
        path: 'project/create',
        redirect: '/project/create',
        meta: { hidden: true },
      },
      // ✅ v2.2 round 4 修复：/admin/project/manage 已经被项目列表取代
      //    旧路径 /admin/project/manage → 跳 /project/list（保留兼容老链接）
      //    meta.hidden=true 让 sidebar 不再渲染这个 redirect
      {
        path: 'project/manage',
        redirect: '/project/list',
        meta: { hidden: true },
      },
      // 作品库管理（C）
      {
        path: 'artwork/recommend',
        name: 'AdminArtworkRecommend',
        component: () => import('@/views/admin/artwork/recommend/index.vue'),
        meta: { title: '作品推荐', icon: 'Star', roles: [1, 2], transition: 'page-zoom' },
      },
      // 设备耗材（C）
      {
        path: 'printer',
        name: 'AdminPrinter',
        component: () => import('@/views/admin/printer/list/index.vue'),
        meta: { title: '打印机管理', icon: 'Box', roles: [1, 2], transition: 'page-zoom' },
      },
      {
        path: 'printer/maintenance',
        name: 'AdminPrinterMaintenance',
        component: () => import('@/views/admin/printer/maintenance/index.vue'),
        meta: { title: '维护记录', icon: 'Tools', roles: [1, 2], parent: '/admin/printer', transition: 'page-zoom' },
      },
      {
        path: 'material',
        name: 'AdminMaterial',
        component: () => import('@/views/admin/material/list/index.vue'),
        meta: { title: '耗材库存', icon: 'Goods', roles: [1, 2], transition: 'page-zoom' },
      },
      {
        path: 'material/inbound',
        name: 'AdminMaterialInbound',
        component: () => import('@/views/admin/material/inbound/index.vue'),
        meta: { title: '耗材入库', icon: 'Upload', roles: [1, 2], transition: 'page-flip' },
      },
      {
        path: 'material/log',
        name: 'AdminMaterialLog',
        component: () => import('@/views/admin/material/log/index.vue'),
        meta: { title: '耗材流水', icon: 'Tickets', roles: [1, 2], transition: 'page-slide-up' },
      },
      // 成员管理（C）
      {
        path: 'member',
        name: 'AdminMember',
        component: () => import('@/views/admin/member/index.vue'),
        meta: { title: '成员管理', icon: 'UserFilled', roles: [1, 2], transition: 'page-zoom' },
      },
      // ✅ v2.2 新增（用户反馈）：管理员文件管理（STL/图片/项目文件）
      {
        path: 'file/manage',
        name: 'AdminFileManage',
        component: () => import('@/views/admin/file/manage/index.vue'),
        meta: { title: '文件管理', icon: 'FolderOpened', roles: [1, 2], transition: 'page-zoom' },
      },
      // 系统日志（C）
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/admin/log/index.vue'),
        meta: { title: '系统日志', icon: 'Document', roles: [1], transition: 'page-zoom' },  // 仅社长
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
