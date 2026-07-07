<template>
  <div class="main-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="22"><Van /></el-icon>
          <span class="logo-text">{{ t('common.appName') }}</span>
        </div>

        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="nav-menu desktop-only"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
            {{ item.label }}
          </el-menu-item>
        </el-menu>

        <div class="user-actions desktop-only">
          <el-button class="lang-switch" plain @click="toggleLocale">
            <el-icon><Switch /></el-icon>
            {{ nextLocaleLabel }}
          </el-button>

          <el-badge :value="cartCount" :hidden="!cartCount" :max="99" class="cart-badge">
            <el-button v-if="userStore.token" @click="$router.push('/cart')">
              <el-icon><ShoppingCart /></el-icon>
              {{ t('common.cart') }}
            </el-button>
          </el-badge>

          <el-button v-if="userStore.token" type="primary" @click="showPostDialog = true">
            <el-icon><Edit /></el-icon>
            {{ t('common.publish') }}
          </el-button>

          <template v-if="userStore.token">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">{{ t('common.profile') }}</el-dropdown-item>
                  <el-dropdown-item command="orders">{{ t('common.orders') }}</el-dropdown-item>
                  <el-dropdown-item command="collections">{{ t('common.collections') }}</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>{{ t('common.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">{{ t('common.login') }}</el-button>
            <el-button type="primary" @click="$router.push('/register')">{{ t('common.register') }}</el-button>
          </template>
        </div>

        <div class="user-actions mobile-only mobile-header-actions">
          <el-button class="mobile-lang" circle plain @click="toggleLocale">{{ nextLocaleLabel }}</el-button>
          <el-badge :value="cartCount" :hidden="!cartCount" :max="99">
            <el-button v-if="userStore.token" circle @click="$router.push('/cart')">
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          <el-button :icon="Menu" circle @click="drawerOpen = true" />
        </div>
      </div>
    </el-header>

    <el-main class="main-content" :class="{ 'no-tabbar-padding': hideTabBar && isMobile }">
      <router-view />
    </el-main>

    <nav v-if="!hideTabBar" class="mobile-tabbar mobile-only">
      <button
        v-for="tab in tabItems"
        :key="tab.path"
        type="button"
        class="tab-item"
        :class="{ active: isTabActive(tab.path) }"
        @click="goTab(tab.path)"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </button>
    </nav>

    <el-drawer
      v-model="drawerOpen"
      direction="rtl"
      :size="280"
      :title="t('common.menu')"
      class="mobile-drawer"
    >
      <div v-if="userStore.token" class="drawer-user">
        <el-avatar :size="48">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</el-avatar>
        <div>
          <div class="drawer-nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
          <div class="drawer-username">{{ userStore.userInfo?.username }}</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router @select="onDrawerSelect">
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
        <el-menu-item v-if="userStore.token" index="/orders">{{ t('common.orders') }}</el-menu-item>
        <el-menu-item v-if="userStore.token" index="/collections">{{ t('common.collections') }}</el-menu-item>
        <el-menu-item v-if="userStore.token" index="/profile">{{ t('common.profile') }}</el-menu-item>
      </el-menu>

      <div class="drawer-actions">
        <el-button v-if="userStore.token" type="primary" style="width: 100%" @click="openPostDialog">
          <el-icon><Edit /></el-icon>
          {{ t('common.publishPost') }}
        </el-button>
        <el-button
          v-if="userStore.token"
          style="width: 100%; margin-top: 10px; margin-left: 0"
          @click="handleCommand('logout')"
        >
          {{ t('common.logout') }}
        </el-button>
        <template v-else>
          <el-button type="primary" style="width: 100%" @click="goAuth('/login')">{{ t('common.login') }}</el-button>
          <el-button style="width: 100%; margin-top: 10px; margin-left: 0" @click="goAuth('/register')">
            {{ t('common.register') }}
          </el-button>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="showPostDialog"
      :title="t('common.publishPost')"
      :width="dialogWidth"
      :fullscreen="isMobile"
    >
      <el-form :model="postForm" label-position="top">
        <el-form-item :label="t('post.title')">
          <el-input v-model="postForm.title" :placeholder="t('post.titlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('post.category')">
          <el-select v-model="postForm.category" :placeholder="t('post.category')" style="width: 100%">
            <el-option :label="t('post.tech')" :value="1" />
            <el-option :label="t('post.trouble')" :value="2" />
            <el-option :label="t('post.experience')" :value="3" />
            <el-option :label="t('post.other')" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('post.content')">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="isMobile ? 10 : 8"
            :placeholder="t('post.contentPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handlePublishPost">{{ t('common.publish') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createPost } from '@/api/post'
import { getCartCount } from '@/api/parts'
import { ElMessage } from 'element-plus'
import { Menu, House, ShoppingBag, Reading, ChatDotRound, User, Switch, Van, ShoppingCart, Edit } from '@element-plus/icons-vue'
import { useMobile } from '@/composables/useMobile'
import { useI18n } from '@/i18n'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isMobile } = useMobile()
const { t, toggleLocale, nextLocaleLabel } = useI18n()

const dialogWidth = computed(() => (isMobile.value ? '92%' : '600px'))

const hideTabBar = computed(() => {
  const p = route.path
  return p.startsWith('/checkout') || p.startsWith('/pay')
})

const drawerOpen = ref(false)
const activeMenu = computed(() => {
  if (route.path.startsWith('/parts') || route.path.startsWith('/cart') || route.path.startsWith('/checkout') || route.path.startsWith('/orders')) return '/parts'
  if (route.path.startsWith('/knowledge')) return '/knowledge'
  if (route.path.startsWith('/assistant')) return '/assistant'
  if (route.path.startsWith('/service')) return '/service'
  if (route.path.startsWith('/profile') || route.path.startsWith('/collections')) return '/profile'
  return route.path
})

const navItems = computed(() => [
  { path: '/', label: t('common.community') },
  { path: '/parts', label: t('common.parts') },
  { path: '/knowledge', label: t('common.knowledge') },
  { path: '/assistant', label: t('common.assistant') },
  { path: '/service', label: t('common.service') }
])

const tabItems = computed(() => [
  { path: '/', label: t('common.home'), icon: House },
  { path: '/parts', label: t('common.parts'), icon: ShoppingBag },
  { path: '/knowledge', label: t('common.knowledge'), icon: Reading },
  { path: '/assistant', label: 'AI', icon: ChatDotRound },
  { path: '/profile', label: t('common.mine'), icon: User }
])

const showPostDialog = ref(false)
const cartCount = ref(0)
const postForm = ref({ title: '', content: '', category: 1 })

const isTabActive = (path) => {
  if (path === '/') return route.path === '/'
  if (path === '/profile') {
    return route.path.startsWith('/profile') || route.path.startsWith('/collections')
  }
  return route.path.startsWith(path)
}

const goTab = (path) => {
  if (path === '/profile' && !userStore.token) {
    router.push('/login')
    return
  }
  router.push(path)
}

const handleMenuSelect = (index) => router.push(index)
const onDrawerSelect = () => { drawerOpen.value = false }
const goAuth = (path) => { drawerOpen.value = false; router.push(path) }
const openPostDialog = () => { drawerOpen.value = false; showPostDialog.value = true }

const handleCommand = (command) => {
  drawerOpen.value = false
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
    ElMessage.success(t('post.logoutSuccess'))
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'collections') {
    router.push('/collections')
  }
}

const handlePublishPost = async () => {
  if (!postForm.value.title) {
    ElMessage.warning(t('post.titleRequired'))
    return
  }
  if (!postForm.value.content) {
    ElMessage.warning(t('post.contentRequired'))
    return
  }
  try {
    await createPost(postForm.value)
    ElMessage.success(t('post.publishSuccess'))
    showPostDialog.value = false
    postForm.value = { title: '', content: '', category: 1 }
    router.push('/')
  } catch (error) {
    console.error(error)
  }
}

const loadCartCount = async () => {
  if (!userStore.token) {
    cartCount.value = 0
    return
  }
  try {
    const res = await getCartCount()
    cartCount.value = res.data?.count || 0
  } catch (e) {
    cartCount.value = 0
  }
}

watch(() => userStore.token, loadCartCount)
onMounted(loadCartCount)
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: var(--efh-bg);
  width: 100%;
}

.header {
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid var(--efh-border-light);
  box-shadow: var(--efh-shadow-sm);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 900;
}

.header-content {
  max-width: var(--efh-max-width);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--efh-header-height);
  padding: 0 20px;
  gap: 12px;
  width: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 600;
  color: var(--efh-text);
  cursor: pointer;
  flex-shrink: 0;
  white-space: nowrap;
  letter-spacing: 0;
}

.logo .el-icon {
  color: var(--efh-primary);
}

.nav-menu {
  flex: 1;
  border: none;
  margin: 0 16px;
  min-width: 0;
  background: transparent !important;
}

.nav-menu :deep(.el-menu-item) {
  font-weight: 500;
  border-bottom: 2px solid transparent !important;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: var(--efh-primary) !important;
  border-bottom-color: var(--efh-primary) !important;
  background: transparent !important;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.lang-switch {
  min-width: 72px;
}

.mobile-lang {
  width: 36px;
  font-size: 12px;
  padding: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: var(--efh-primary-soft);
}

.username {
  font-size: 14px;
  color: var(--efh-text-secondary);
  font-weight: 500;
}

.main-content {
  max-width: var(--efh-max-width);
  margin: 0 auto;
  padding: 24px 20px;
  width: 100%;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 4px 20px;
  margin-bottom: 4px;
  border-bottom: 1px solid var(--efh-border-light);
}

.drawer-nickname {
  font-weight: 600;
  font-size: 16px;
  color: var(--efh-text);
}

.drawer-username {
  font-size: 13px;
  color: var(--efh-text-muted);
  margin-top: 2px;
}

.drawer-actions {
  padding: 16px 4px;
  margin-top: 8px;
}
</style>

<style>
.mobile-drawer .el-drawer__body {
  padding: 12px;
  display: flex;
  flex-direction: column;
}

.mobile-drawer .el-menu {
  border-right: none;
  flex: 1;
}

.main-layout .nav-menu.desktop-only {
  display: flex;
  flex: 1;
}
</style>
