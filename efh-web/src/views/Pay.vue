<template>
  <div class="pay-page" v-loading="loading">
    <el-card class="pay-card">
      <div class="pay-header">
        <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
        <h2>订单提交成功，请完成支付</h2>
        <p>订单号：{{ orderNo }}</p>
      </div>

      <div class="pay-amount">
        <span>应付金额</span>
        <span class="amount">¥{{ detail?.order?.payAmount || '0.00' }}</span>
      </div>

      <div class="pay-methods">
        <h4>选择支付方式</h4>
        <el-radio-group v-model="payChannel">
          <el-radio label="alipay" border>
            <span class="pay-option">
              <img src="https://img.alicdn.com/tfs/TB1Ma_8RXXXXXaXXXXXXXXXXXX-120-120.png" alt="alipay" class="pay-icon" />
              支付宝
            </span>
          </el-radio>
          <el-radio v-if="showMock" label="mock" border>模拟支付（开发调试）</el-radio>
        </el-radio-group>
        <p v-if="payChannel === 'alipay' && !alipayReady" class="pay-tip">
          支付宝未配置。请设置环境变量 ALIPAY_ENABLED=true 及商户密钥，或使用沙箱账号。
        </p>
      </div>

      <el-button
        type="danger"
        size="large"
        class="pay-btn"
        :loading="paying"
        :disabled="payChannel === 'alipay' && !alipayReady"
        @click="handlePay"
      >
        {{ payChannel === 'alipay' ? '跳转支付宝支付' : '确认支付' }}
        ¥{{ detail?.order?.payAmount || '0.00' }}
      </el-button>
      <el-button link @click="$router.push('/orders')">稍后支付，查看订单</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheck } from '@element-plus/icons-vue'
import { getOrderByNo, createAlipayPagePay, mockPay } from '@/api/parts'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const orderNo = ref(route.params.orderNo)
const detail = ref(null)
const payChannel = ref('alipay')
const alipayReady = ref(true)
const showMock = ref(import.meta.env.DEV)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getOrderByNo(orderNo.value)
    detail.value = res.data
    if (detail.value.order.status !== 0) {
      router.replace(`/orders/${detail.value.order.id}`)
    }
  } finally {
    loading.value = false
  }
})

const submitAlipayForm = (payForm) => {
  const wrapper = document.createElement('div')
  wrapper.style.display = 'none'
  wrapper.innerHTML = payForm
  document.body.appendChild(wrapper)
  const form = wrapper.querySelector('form')
  if (form) {
    form.submit()
  } else {
    ElMessage.error('支付表单生成失败')
  }
}

const handlePay = async () => {
  if (!detail.value?.payment?.payNo) {
    ElMessage.error('支付单不存在')
    return
  }
  paying.value = true
  try {
    if (payChannel.value === 'alipay') {
      const res = await createAlipayPagePay(detail.value.payment.payNo)
      submitAlipayForm(res.data.payForm)
      return
    }
    await mockPay(detail.value.payment.payNo)
    ElMessage.success('支付成功')
    router.push(`/orders/${detail.value.order.id}`)
  } catch (e) {
    if (payChannel.value === 'alipay') {
      alipayReady.value = false
    }
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.pay-page { max-width: 600px; margin: 40px auto; }
.pay-card { text-align: center; padding: 20px; }
.pay-header h2 { margin: 16px 0 8px; }
.pay-header p { color: #909399; }
.pay-amount { background: #fef0f0; padding: 24px; border-radius: 8px; margin: 24px 0; }
.pay-amount .amount { display: block; font-size: 36px; color: #f56c6c; font-weight: bold; margin-top: 8px; }
.pay-methods { text-align: left; margin-bottom: 24px; }
.pay-option { display: inline-flex; align-items: center; gap: 8px; }
.pay-icon { width: 24px; height: 24px; }
.pay-tip { margin-top: 12px; font-size: 13px; color: #e6a23c; line-height: 1.5; }
.pay-btn { width: 100%; margin-bottom: 12px; }
</style>
