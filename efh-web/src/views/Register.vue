<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-brand">
        <div class="auth-brand-icon">
          <el-icon :size="28" color="#fff"><Van /></el-icon>
        </div>
        <h1>叉车社区</h1>
        <p>加入社区，共享叉车知识与经验</p>
      </div>

      <div class="auth-card">
        <h2 class="title">创建账号</h2>
        <el-form :model="registerForm" :rules="rules" ref="formRef" @submit.prevent>
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              size="large"
              prefix-icon="Avatar"
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="手机号"
              size="large"
              maxlength="11"
              prefix-icon="Phone"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="auth-submit"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="auth-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Avatar, Lock, Phone, User, Van } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const registerForm = ref({
  username: '',
  password: '',
  nickname: '',
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.registerAction(registerForm.value)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-submit {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 12px !important;
}

@media (max-width: 768px) {
  .auth-page {
    align-items: flex-start;
    padding-top: max(6vh, env(safe-area-inset-top, 24px));
  }

  .auth-brand {
    margin-bottom: 20px;
  }

  .auth-card {
    padding: 24px 20px;
  }
}
</style>
