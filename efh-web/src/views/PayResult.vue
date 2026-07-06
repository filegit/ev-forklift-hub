<template>
  <div class="pay-result-page" v-loading="loading">
    <el-card class="result-card">
      <el-result
        v-if="status === 1"
        icon="success"
        title="支付成功"
        sub-title="订单已支付，商家正在安排发货"
      >
        <template #extra>
          <el-button type="primary" @click="goOrder">查看订单</el-button>
          <el-button @click="$router.push('/parts')">继续购物</el-button>
        </template>
      </el-result>

      <el-result
        v-else-if="status === 2"
        icon="error"
        title="支付失败"
        sub-title="请返回订单重新支付"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/orders')">我的订单</el-button>
        </template>
      </el-result>

      <el-result
        v-else
        icon="info"
        title="等待支付结果"
        sub-title="正在确认支付状态，请稍候..."
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPayStatus } from '@/api/parts'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const status = ref(0)
const orderId = ref(null)
const payNo = ref(route.query.payNo || '')

const pollStatus = async () => {
  if (!payNo.value) {
    loading.value = false
    status.value = 2
    return
  }
  let retries = 15
  while (retries-- > 0) {
    try {
      const res = await getPayStatus(payNo.value)
      orderId.value = res.data.orderId
      status.value = res.data.status
      if (res.data.status === 1 || res.data.status === 2) {
        break
      }
    } catch (e) {
      // ignore and retry
    }
    await new Promise(r => setTimeout(r, 2000))
  }
  loading.value = false
}

const goOrder = () => {
  if (orderId.value) {
    router.push(`/orders/${orderId.value}`)
  } else {
    router.push('/orders')
  }
}

onMounted(pollStatus)
</script>

<style scoped>
.pay-result-page { max-width: 600px; margin: 60px auto; }
.result-card { padding: 20px; }
</style>
