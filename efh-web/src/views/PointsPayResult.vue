<template>
  <div class="points-pay-result-page" v-loading="loading">
    <el-card class="result-card">
      <el-result
        v-if="status === 1"
        icon="success"
        title="积分购买成功"
        sub-title="支付已完成，积分已经到账"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/profile')">查看积分</el-button>
        </template>
      </el-result>

      <el-result
        v-else-if="status === 2"
        icon="error"
        title="支付失败"
        sub-title="请返回个人中心重新购买"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/profile')">返回个人中心</el-button>
        </template>
      </el-result>

      <el-result
        v-else
        icon="info"
        title="等待支付结果"
        sub-title="正在确认积分到账状态，请稍候..."
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPointsPayStatus } from '@/api/points'

const route = useRoute()
const loading = ref(true)
const status = ref(0)
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
      const res = await getPointsPayStatus(payNo.value)
      status.value = res.data.status
      if (status.value === 1 || status.value === 2) {
        break
      }
    } catch (e) {
      // keep polling
    }
    await new Promise(resolve => setTimeout(resolve, 2000))
  }
  loading.value = false
}

onMounted(pollStatus)
</script>

<style scoped>
.points-pay-result-page {
  max-width: 600px;
  margin: 60px auto;
}

.result-card {
  padding: 20px;
}
</style>
