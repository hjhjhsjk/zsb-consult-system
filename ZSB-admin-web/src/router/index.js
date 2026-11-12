import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'school',
        name: 'School',
        component: () => import('@/views/school/index.vue'),
        meta: { title: '院校管理', icon: 'School' }
      },
      {
        path: 'policy',
        name: 'Policy',
        component: () => import('@/views/policy/index.vue'),
        meta: { title: '政策管理', icon: 'Document' }
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/plan/index.vue'),
        meta: { title: '招生计划', icon: 'Tickets' }
      },
      {
        path: 'post',
        name: 'Post',
        component: () => import('@/views/post/index.vue'),
        meta: { title: '帖子管理', icon: 'ChatDotRound' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'major',
        name: 'MajorManage',
        component: () => import('@/views/major/index.vue'),
        meta: { title: '专业管理', icon: 'Reading' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  // 兜底：访问不存在的地址回首页，不出现空白页
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 守卫：除登录页外，都要 token
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) return next('/login')
  if (to.path === '/login' && token) return next('/')
  next()
})

// 浏览器标签页标题跟着页面走，多开标签时更好认
router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · 专升本后台` : '专升本后台'
})

export default router
