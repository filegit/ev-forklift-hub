import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'post/:id', name: 'PostDetail', component: () => import('@/views/PostDetail.vue') },
      { path: 'parts', name: 'Parts', component: () => import('@/views/Parts.vue') },
      { path: 'parts/admin', name: 'PartsAdmin', component: () => import('@/views/PartsAdmin.vue'), meta: { requiresAuth: true } },
      { path: 'parts/:id', name: 'PartsDetail', component: () => import('@/views/PartsDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { requiresAuth: true } },
      { path: 'pay/result', name: 'PayResult', component: () => import('@/views/PayResult.vue'), meta: { requiresAuth: true } },
      { path: 'pay/:orderNo', name: 'Pay', component: () => import('@/views/Pay.vue'), meta: { requiresAuth: true } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/Orders.vue'), meta: { requiresAuth: true } },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { requiresAuth: true } },
      { path: 'service', name: 'Service', component: () => import('@/views/Service.vue') },
      { path: 'knowledge', name: 'Knowledge', component: () => import('@/views/Knowledge.vue') },
      { path: 'knowledge/admin', name: 'KnowledgeAdmin', component: () => import('@/views/KnowledgeAdmin.vue'), meta: { requiresAuth: true } },
      { path: 'knowledge/unlock/result', name: 'KnowledgeUnlockResult', component: () => import('@/views/KnowledgeUnlockResult.vue'), meta: { requiresAuth: true } },
      { path: 'knowledge/:id', name: 'KnowledgeDetail', component: () => import('@/views/KnowledgeDetail.vue') },
      { path: 'assistant', name: 'Assistant', component: () => import('@/views/Assistant.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
      { path: 'collections', name: 'Collections', component: () => import('@/views/Collections.vue'), meta: { requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') }
]

const router = createRouter({
  history: import.meta.env.VITE_APP_PLATFORM === 'native' ? createWebHashHistory() : createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  if ((to.name === 'Login' || to.name === 'Register') && token) {
    next({ name: 'Home' })
    return
  }
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' })
    return
  }
  next()
})

export default router
