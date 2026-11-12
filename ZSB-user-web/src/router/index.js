import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/index.vue'

const routes = [
  // ===== 带导航栏的页面（全部按需加载，首屏只下首页 chunk）=====
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'school',
        name: 'School',
        component: () => import('../views/school/index.vue'),
        meta: { title: '院校库' }
      },
      {
        path: 'school/:id',
        name: 'SchoolDetail',
        component: () => import('../views/school/Detail.vue'),
        meta: { title: '院校详情' }
      },
      {
        path: 'consult',
        name: 'Consult',
        component: () => import('../views/consult/index.vue'),
        meta: { title: '政策咨询' }
      },
      {
        path: 'consult/:id',
        name: 'ConsultDetail',
        component: () => import('../views/consult/Detail.vue'),
        meta: { title: '政策详情' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'bbs',
        name: 'Bbs',
        component: () => import('../views/bbs/index.vue'),
        meta: { title: '论坛' }
      },
      {
        path: 'bbs/:id',
        name: 'BbsDetail',
        component: () => import('../views/bbs/Detail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('../views/search/index.vue'),
        meta: { title: '搜索' }
      }
    ]
  },

  // ===== 不带导航栏 =====
  {
    path: '/login',
    component: () => import('../views/user/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    component: () => import('../views/user/Register.vue'),
    meta: { title: '注册' }
  },
  // 兜底：地址写错回首页
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 切页面回到顶部；浏览器前进后退则恢复原位置
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

// 路由守卫：需要登录的页面没 token 就带去登录页，并记住原地址
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · 专升本咨询` : '专升本咨询'
})

export default router
