<template>
  <div class="main-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="22"><Van /></el-icon>
          <span class="logo-text">叉车社区</span>
        </div>

        <!-- 桌面端导航 -->
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="nav-menu desktop-only"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/parts">配件商城</el-menu-item>
          <el-menu-item index="/knowledge">知识库</el-menu-item>
          <el-menu-item index="/assistant">AI助手</el-menu-item>
          <el-menu-item index="/service">维修服务</el-menu-item>
        </el-menu>

        <!-- 桌面端操作区 -->
        <div class="user-actions desktop-only">
          <el-badge :value="cartCount" :hidden="!cartCount" :max="99" class="cart-badge">
            <el-button @click="$router.push('/cart')" v-if="userStore.token">
              <el-icon><ShoppingCart /></el-icon>
              购物车
            </el-button>
          </el-badge>
          <el-button type="primary" @click="showPostDialog = true" v-if="userStore.token">
            <el-icon><Edit /></el-icon>
            发帖
          </el-button>
          <template v-if="userStore.token">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32">{{ userStore.userInfo?.nickname?.charAt(0) }}</el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="collections">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>

        <!-- 移动端顶栏操作 -->
        <div class="user-actions mobile-only mobile-header-actions">
          <el-badge :value="cartCount" :hidden="!cartCount" :max="99">
            <el-button circle @click="$router.push('/cart')" v-if="userStore.token">
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          <el-button :icon="Menu" circle @click="drawerOpen = true" />
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 移动端底部导航（CSS 控制显隐） -->
    <nav class="mobile-tabbar mobile-only">
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
      title="菜单"
      class="mobile-drawer"
    >
      <div v-if="userStore.token" class="drawer-user">
        <el-avatar :size="48">{{ userStore.userInfo?.nickname?.charAt(0) }}</el-avatar>
        <div>
          <div class="drawer-nickname">{{ userStore.userInfo?.nickname }}</div>
          <div class="drawer-username">{{ userStore.userInfo?.username }}</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router @select="onDrawerSelect">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/parts">配件商城</el-menu-item>
        <el-menu-item index="/knowledge">知识库</el-menu-item>
        <el-menu-item index="/assistant">AI 助手</el-menu-item>
        <el-menu-item index="/service">维修服务</el-menu-item>
        <el-menu-item v-if="userStore.token" index="/orders">我的订单</el-menu-item>
        <el-menu-item v-if="userStore.token" index="/collections">我的收藏</el-menu-item>
        <el-menu-item v-if="userStore.token" index="/profile">个人中心</el-menu-item>
      </el-menu>

      <div class="drawer-actions">
        <el-button v-if="userStore.token" type="primary" style="width: 100%" @click="openPostDialog">
          <el-icon><Edit /></el-icon>
          发布帖子
        </el-button>
        <el-button
          v-if="userStore.token"
          style="width: 100%; margin-top: 10px; margin-left: 0"
          @click="handleCommand('logout')"
        >
          退出登录
        </el-button>
        <template v-else>
          <el-button type="primary" style="width: 100%" @click="goAuth('/login')">登录</el-button>
          <el-button style="width: 100%; margin-top: 10px; margin-left: 0" @click="goAuth('/register')">注册</el-button>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="showPostDialog"
      title="发布帖子"
      :width="dialogWidth"
      :fullscreen="isMobile"
    >
      <el-form :model="postForm" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="postForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="技术交流" :value="1" />
            <el-option label="故障求助" :value="2" />
            <el-option label="经验分享" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="isMobile ? 10 : 8"
            placeholder="请输入帖子内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublishPost">发布</el-button>
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
import { Menu, House, ShoppingBag, Reading, ChatDotRound, User } from '@element-plus/icons-vue'
import { useMobile } from '@/composables/useMobile'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isMobile } = useMobile()

const dialogWidth = computed(() => (isMobile.value ? '92%' : '600px'))

const drawerOpen = ref(false)
const activeMenu = computed(() => {
  if (route.path.startsWith('/parts') || route.path.startsWith('/cart') || route.path.startsWith('/checkout') || route.path.startsWith('/orders')) return '/parts'
  if (route.path.startsWith('/knowledge')) return '/knowledge'
  if (route.path.startsWith('/assistant')) return '/assistant'
  if (route.path.startsWith('/service')) return '/service'
  if (route.path.startsWith('/profile') || route.path.startsWith('/collections')) return '/profile'
  return route.path
})

const tabItems = [
  { path: '/', label: '首页', icon: House },
  { path: '/parts', label: '商城', icon: ShoppingBag },
  { path: '/knowledge', label: '知识库', icon: Reading },
  { path: '/assistant', label: 'AI', icon: ChatDotRound },
  { path: '/profile', label: '我的', icon: User }
]

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
    ElMessage.success('退出成功')
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
    ElMessage.warning('请输入标题')
    return
  }
  if (!postForm.value.content) {
    ElMessage.warning('请输入内容')
    return
  }
  try {
    await createPost(postForm.value)
    ElMessage.success('发布成功')
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
  background: #f5f7fa;
  width: 100%;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  gap: 8px;
  width: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  flex-shrink: 0;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border: none;
  margin: 0 24px;
  min-width: 0;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  width: 100%;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 4px 16px;
  margin-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.drawer-nickname {
  font-weight: 600;
  color: #303133;
}

.drawer-username {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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

/* el-menu 是 flex 子项，桌面端需占满中间区域 */
.main-layout .nav-menu.desktop-only {
  display: flex;
  flex: 1;
}
</style>
