<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-brand">
        <div class="auth-brand-icon">
          <el-icon :size="28" color="#fff"><Van /></el-icon>
        </div>
        <h1>叉车社区</h1>
        <p>新能源叉车 · 配件 · 知识 · 服务</p>
      </div>

      <div class="auth-card">
        <h2 class="title">欢迎回来</h2>

        <el-tabs v-model="loginMode" stretch class="login-tabs">
          <el-tab-pane label="验证码登录" name="sms">
            <el-form :model="smsForm" :rules="smsRules" ref="smsFormRef" @submit.prevent>
              <el-form-item prop="phone">
                <el-input
                  v-model="smsForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  maxlength="11"
                  prefix-icon="Iphone"
                />
              </el-form-item>
              <el-form-item prop="smsCode">
                <div class="sms-row">
                  <el-input
                    v-model="smsForm.smsCode"
                    placeholder="短信验证码"
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
                  class="auth-submit"
                  :loading="loading"
                  @click="handleSmsLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="密码登录" name="password">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" @submit.prevent>
              <el-form-item prop="username">
                <el-input
                  v-model="passwordForm.username"
                  placeholder="用户名"
                  size="large"
                  prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="passwordForm.password"
                  type="password"
                  placeholder="密码"
                  size="large"
                  prefix-icon="Lock"
                  show-password
                  @keyup.enter="handlePasswordLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="auth-submit"
                  :loading="loading"
                  @click="handlePasswordLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="auth-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendLoginSms } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, Message, User, Van } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loginMode = ref('sms')
const smsFormRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

const smsForm = ref({ phone: '', smsCode: '' })
const passwordForm = ref({ username: '', password: '' })

const phoneValidator = (rule, value, callback) => {
  if (!value) callback(new Error('请输入手机号'))
  else if (!/^1\d{10}$/.test(value)) callback(new Error('手机号格式不正确'))
  else callback()
}

const smsRules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }]
}

const passwordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
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
  await smsFormRef.value.validateField('phone')
  smsSending.value = true
  try {
    const res = await sendLoginSms({ phone: smsForm.value.phone.trim() })
    const mockCode = res.data?.mockCode
    if (mockCode != null) {
      ElMessage.success(`验证码已发送（演示：${mockCode}）`)
    } else {
      ElMessage.success(res.data?.message || '验证码已发送到手机，请注意查收')
    }
    startSmsCountdown()
  } catch (error) {
    console.error(error)
  } finally {
    smsSending.value = false
  }
}

const handleSmsLogin = async () => {
  await smsFormRef.value.validate()
  loading.value = true
  try {
    await userStore.loginBySmsAction({
      phone: smsForm.value.phone.trim(),
      smsCode: smsForm.value.smsCode.trim()
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePasswordLogin = async () => {
  await passwordFormRef.value.validate()
  loading.value = true
  try {
    await userStore.loginAction(passwordForm.value)
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
.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--efh-border-light);
}

.auth-submit {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 12px !important;
}

.sms-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.sms-row .el-input {
  flex: 1;
}

.sms-row .el-button {
  flex-shrink: 0;
  min-width: 108px;
  border-radius: 10px !important;
}

@media (max-width: 768px) {
  .auth-page {
    align-items: flex-start;
    padding-top: max(8vh, env(safe-area-inset-top, 24px));
  }

  .auth-brand {
    margin-bottom: 20px;
  }

  .auth-card {
    padding: 24px 20px;
  }
}
</style>
