<template>
  <div class="order-detail-page" v-loading="loading">
    <el-card v-if="detail">
      <el-steps :active="stepActive" finish-status="success" align-center class="steps">
        <el-step title="提交订单" />
        <el-step title="付款成功" />
        <el-step title="商家发货" />
        <el-step title="确认收货" />
      </el-steps>

      <el-alert :title="statusText(detail.order.status)" :type="statusAlertType" show-icon :closable="false" class="status-alert" />

      <h3>商品信息</h3>
      <el-table :data="detail.items" class="section">
        <el-table-column label="商品">
          <template #default="{ row }">
            <div class="item-cell">
              <img :src="row.partsImage" class="thumb" />
              {{ row.partsName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="100"><template #default="{ row }">¥{{ row.subtotal }}</template></el-table-column>
      </el-table>

      <el-descriptions title="订单信息" :column="2" border class="section">
        <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detail.order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detail.order.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">¥{{ detail.order.payAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.order.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail.shipment" class="section">
        <h3>物流信息</h3>
        <p><strong>{{ detail.shipment.carrier }}</strong> 运单号：{{ detail.shipment.trackingNo }}</p>
        <el-timeline>
          <el-timeline-item
            v-for="trace in detail.traces"
            :key="trace.id"
            :timestamp="trace.traceTime"
            placement="top"
          >
            <p>{{ trace.description }}</p>
            <p v-if="trace.location" class="loc">{{ trace.location }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="actions">
        <el-button v-if="detail.order.status === 0" type="danger" @click="$router.push(`/pay/${detail.order.orderNo}`)">去支付</el-button>
        <el-button v-if="detail.order.status === 0" @click="handleCancel">取消订单</el-button>
        <el-button v-if="detail.order.status === 2" type="primary" @click="handleConfirm">确认收货</el-button>
        <el-button @click="$router.push('/orders')">返回列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder, confirmReceive } from '@/api/parts'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)

const statusText = (s) => ({ 0: '等待买家付款', 1: '买家已付款，等待商家发货', 2: '商家已发货，等待买家收货', 3: '交易完成', 4: '订单已取消' }[s])
const statusAlertType = computed(() => ({ 0: 'warning', 1: 'info', 2: 'primary', 3: 'success', 4: 'info' }[detail.value?.order?.status]))

const stepActive = computed(() => {
  const s = detail.value?.order?.status
  if (s === 0) return 1
  if (s === 1) return 2
  if (s === 2) return 3
  if (s >= 3) return 4
  return 0
})

const handleCancel = async () => {
  await ElMessageBox.confirm('确定取消订单？')
  await cancelOrder(detail.value.order.id)
  ElMessage.success('已取消')
  loadDetail()
}

const handleConfirm = async () => {
  await ElMessageBox.confirm('确认已收到货物？')
  await confirmReceive(detail.value.order.id)
  ElMessage.success('确认收货成功')
  loadDetail()
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.order-detail-page { max-width: 900px; margin: 0 auto; }
.steps { margin-bottom: 24px; }
.status-alert { margin-bottom: 20px; }
.section { margin-bottom: 24px; }
.item-cell { display: flex; align-items: center; gap: 10px; }
.thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; }
.loc { color: #909399; font-size: 12px; }
.actions { display: flex; gap: 12px; justify-content: center; margin-top: 24px; }
</style>
