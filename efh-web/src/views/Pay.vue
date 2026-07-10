<template>
  <div class="pay-page" v-loading="loading">
    <section class="pay-shell" v-if="detail">
      <div class="pay-main">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <span class="efh-kicker">支付确认</span>
        <h1>订单已创建，等待企业支付</h1>
        <p>订单号：{{ orderNo }}</p>

        <div class="amount-card">
          <span>应付金额</span>
          <strong>¥{{ detail?.order?.payAmount || '0.00' }}</strong>
        </div>

        <el-radio-group v-model="payChannel" class="pay-methods">
          <el-radio label="alipay" border>
            <span class="pay-option">
              <el-icon><Wallet /></el-icon>
              支付宝网页支付
            </span>
          </el-radio>
        </el-radio-group>

        <el-alert
          v-if="payChannel === 'alipay' && !alipayReady"
          title="支付宝暂不可用，请稍后重试或联系管理员检查支付配置。"
          type="warning"
          :closable="false"
          show-icon
        />

        <el-button
          type="primary"
          size="large"
          class="pay-btn"
          :loading="paying"
          :disabled="payChannel === 'alipay' && !alipayReady"
          @click="handlePay"
        >
          跳转支付宝支付
        </el-button>
        <el-button link @click="$router.push('/orders')">稍后支付，查看订单</el-button>
      </div>

      <aside class="pay-aside">
        <h3>支付与履约进度</h3>
        <p>支付接入支付宝官方接口，支付成功后订单进入待发货状态；仓库可录入承运商、运单号和物流轨迹。</p>
        <div class="efh-status-rail">
          <div class="efh-status-node active">
            <span>1</span>
            <strong>提交订单</strong>
          </div>
          <div class="efh-status-node active">
            <span>2</span>
            <strong>企业支付</strong>
          </div>
          <div class="efh-status-node">
            <span>3</span>
            <strong>仓库发货</strong>
          </div>
          <div class="efh-status-node">
            <span>4</span>
            <strong>签收完成</strong>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheck, Wallet } from '@element-plus/icons-vue'
import { getOrderByNo, createAlipayPagePay } from '@/api/parts'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const orderNo = ref(route.params.orderNo)
const detail = ref(null)
const payChannel = ref('alipay')
const alipayReady = ref(true)

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
    const res = await createAlipayPagePay(detail.value.payment.payNo)
    submitAlipayForm(res.data.payForm)
  } catch (e) {
    const message = e?.message || ''
    if (message.includes('支付宝未配置')) {
      alipayReady.value = false
    }
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.pay-page {
  max-width: var(--efh-max-width);
  margin: 0 auto;
}

.pay-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 18px;
  align-items: start;
}

.pay-main,
.pay-aside {
  background: #fff;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  padding: 24px;
}

.success-icon {
  width: 54px;
  height: 54px;
  color: var(--efh-primary);
  background: var(--efh-primary-soft);
  border-radius: 8px;
  margin-bottom: 14px;
}

.success-icon :deep(svg) {
  width: 30px;
  height: 30px;
}

.pay-main h1 {
  margin: 0;
  font-size: 26px;
}

.pay-main p,
.pay-aside p {
  color: var(--efh-text-secondary);
  line-height: 1.7;
}

.amount-card {
  margin: 22px 0;
  padding: 18px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
}

.amount-card span {
  display: block;
  color: var(--efh-text-secondary);
}

.amount-card strong {
  display: block;
  color: #b45309;
  font-size: 36px;
  line-height: 1.2;
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: stretch;
  margin-bottom: 16px;
}

.pay-methods :deep(.el-radio) {
  margin-right: 0;
}

.pay-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.pay-btn {
  width: 100%;
  margin-top: 16px;
}

.pay-aside .efh-status-rail {
  grid-template-columns: 1fr;
}

@media (max-width: 900px) {
  .pay-shell {
    grid-template-columns: 1fr;
  }
}
</style>
