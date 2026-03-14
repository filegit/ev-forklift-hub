import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue')
      },
      {
        path: 'parts',
        name: 'Parts',
        component: () => import('@/views/Parts.vue')
      },
      {
        path: 'service',
        name: 'Service',
        component: () => import('@/views/Service.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'collections',
        name: 'Collections',
        component: () => import('@/views/Collections.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  
  // 如果访问登录或注册页面，且已登录，则跳转到首页
  if ((to.name === 'Login' || to.name === 'Register') && token) {
    next({ name: 'Home' })
    return
  }
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' })
    return
  }
  
  next()
})

export default router
