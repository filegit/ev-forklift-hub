<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon><Van /></el-icon>
          <span>新能源叉车社区</span>
        </div>
        
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="nav-menu"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/parts">配件商城</el-menu-item>
          <el-menu-item index="/knowledge">知识库</el-menu-item>
          <el-menu-item index="/assistant">AI助手</el-menu-item>
          <el-menu-item index="/service">维修服务</el-menu-item>
        </el-menu>
        
        <div class="user-actions">
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
      </div>
    </el-header>
    
    <!-- 主体内容 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <!-- 发帖对话框 -->
    <el-dialog
      v-model="showPostDialog"
      title="发布帖子"
      width="600px"
    >
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="postForm.category" placeholder="请选择分类">
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
            :rows="8"
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

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/parts')) return '/parts'
  if (route.path.startsWith('/knowledge')) return '/knowledge'
  if (route.path.startsWith('/orders') || route.path.startsWith('/cart') || route.path.startsWith('/checkout')) return '/parts'
  return route.path
})
const showPostDialog = ref(false)
const cartCount = ref(0)
const postForm = ref({
  title: '',
  content: '',
  category: 1
})

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleCommand = (command) => {
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
  height: 100%;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.nav-menu {
  flex: 1;
  border: none;
  margin: 0 40px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
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

.cart-badge {
  margin-right: 4px;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}
</style>
