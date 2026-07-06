<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="smsCode">
          <div class="sms-row">
            <el-input
              v-model="loginForm.smsCode"
              placeholder="请输入短信验证码"
              size="large"
              maxlength="6"
              prefix-icon="Message"
            />
            <el-button
              size="large"
              :disabled="smsCountdown > 0 || smsSending"
              :loading="smsSending"
              @click="handleSendSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendLoginSms } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

const loginForm = ref({
  username: '',
  password: '',
  smsCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }]
}

const startSmsCountdown = (seconds = 60) => {
  smsCountdown.value = seconds
  smsTimer = setInterval(() => {
    smsCountdown.value -= 1
    if (smsCountdown.value <= 0) {
      clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
}

const handleSendSms = async () => {
  if (!loginForm.value.username?.trim()) {
    ElMessage.warning('请先输入用户名')
    return
  }
  smsSending.value = true
  try {
    const res = await sendLoginSms({ username: loginForm.value.username.trim() })
    const mockCode = res.data?.mockCode
    if (mockCode) {
      ElMessage.success(`验证码已发送（演示：${mockCode}）`)
    } else {
      ElMessage.success(res.data?.message || '验证码已发送')
    }
    startSmsCountdown()
  } catch (error) {
    console.error(error)
  } finally {
    smsSending.value = false
  }
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  
  try {
    await userStore.loginAction(loginForm.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (smsTimer) clearInterval(smsTimer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.footer a {
  color: #409eff;
  text-decoration: none;
}

.sms-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.sms-row .el-input {
  flex: 1;
}

.sms-row .el-button {
  flex-shrink: 0;
  min-width: 110px;
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 12vh;
  }

  .login-card {
    width: 100%;
    max-width: 420px;
    padding: 16px;
  }
}
</style>
