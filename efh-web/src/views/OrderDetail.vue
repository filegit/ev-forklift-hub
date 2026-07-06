<template>
  <div class="order-detail-page" v-loading="loading">
    <template v-if="detail">
      <div class="efh-page-header">
        <div>
          <span class="efh-kicker">订单详情</span>
          <h2>{{ detail.order.orderNo }}</h2>
          <p>{{ statusText(detail.order.status) }}</p>
        </div>
        <el-tag :type="statusType(detail.order.status)" size="large">{{ statusShort(detail.order.status) }}</el-tag>
      </div>

      <div class="efh-status-rail section">
        <div class="efh-status-node" :class="{ active: detail.order.status >= 0 && detail.order.status !== 4 }">
          <span>STEP 1</span>
          <strong>提交订单</strong>
        </div>
        <div class="efh-status-node" :class="{ active: detail.order.status >= 1 && detail.order.status !== 4 }">
          <span>STEP 2</span>
          <strong>付款成功</strong>
        </div>
        <div class="efh-status-node" :class="{ active: detail.order.status >= 2 && detail.order.status !== 4 }">
          <span>STEP 3</span>
          <strong>仓库发货</strong>
        </div>
        <div class="efh-status-node" :class="{ active: detail.order.status >= 3 && detail.order.status !== 4 }">
          <span>STEP 4</span>
          <strong>签收完成</strong>
        </div>
      </div>

      <div class="detail-grid">
        <main>
          <el-card class="section">
            <template #header>备件明细</template>
            <el-table :data="detail.items">
              <el-table-column label="备件" min-width="260">
                <template #default="{ row }">
                  <div class="item-cell">
                    <img :src="row.partsImage" class="thumb" />
                    <span>{{ row.partsName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="110"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column label="小计" width="110"><template #default="{ row }">¥{{ row.subtotal }}</template></el-table-column>
            </el-table>
          </el-card>

          <el-card class="section">
            <template #header>物流信息</template>
            <div v-if="detail.shipment" class="shipment-head">
              <div>
                <label>承运商</label>
                <strong>{{ detail.shipment.carrier }}</strong>
              </div>
              <div>
                <label>运单号</label>
                <strong>{{ detail.shipment.trackingNo }}</strong>
              </div>
            </div>
            <el-timeline v-if="detail.shipment && detail.traces?.length">
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
            <el-empty v-else description="尚未录入物流信息" />
          </el-card>

          <el-card v-if="canFulfill" class="section">
            <template #header>供应商履约操作</template>
            <el-alert
              title="低成本真实方案：仓库发货后人工录入承运商、运单号和关键轨迹；后续需要自动查询时再接入快递100/快递鸟最低套餐。"
              type="info"
              :closable="false"
              show-icon
            />
            <el-form v-if="detail.order.status === 1" :model="shipForm" label-width="86px" class="fulfill-form">
              <el-form-item label="承运商"><el-input v-model="shipForm.carrier" placeholder="如：顺丰速运 / 德邦快运 / 厂内配送" /></el-form-item>
              <el-form-item label="运单号"><el-input v-model="shipForm.trackingNo" placeholder="请输入真实运单号或内部配送单号" /></el-form-item>
              <el-form-item label="发货地点"><el-input v-model="shipForm.location" placeholder="如：上海备件中心仓" /></el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleShip">确认发货</el-button>
            </el-form>
            <el-form v-if="detail.order.status === 2" :model="traceForm" label-width="86px" class="fulfill-form">
              <el-form-item label="节点地点"><el-input v-model="traceForm.location" placeholder="如：武汉转运中心" /></el-form-item>
              <el-form-item label="轨迹说明"><el-input v-model="traceForm.description" placeholder="如：货物已到达转运中心，等待分拨" /></el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleAppendTrace">追加物流轨迹</el-button>
            </el-form>
          </el-card>
        </main>

        <aside class="side-panel">
          <el-descriptions title="订单信息" :column="1" border>
            <el-descriptions-item label="下单时间">{{ detail.order.createTime }}</el-descriptions-item>
            <el-descriptions-item label="收货人">{{ detail.order.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ detail.order.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ detail.order.receiverAddress }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ detail.order.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ detail.order.shipTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="应付金额">¥{{ detail.order.payAmount }}</el-descriptions-item>
          </el-descriptions>

          <div class="actions">
            <el-button v-if="detail.order.status === 0" type="primary" @click="$router.push(`/pay/${detail.order.orderNo}`)">去支付</el-button>
            <el-button v-if="detail.order.status === 0" @click="handleCancel">取消订单</el-button>
            <el-button v-if="detail.order.status === 2" type="primary" @click="handleConfirm">确认收货</el-button>
            <el-button @click="$router.push('/orders')">返回列表</el-button>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getOrderDetail,
  cancelOrder,
  confirmReceive,
  shipOrder,
  appendShipmentTrace
} from '@/api/parts'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const detail = ref(null)
const shipForm = ref({ carrier: '', trackingNo: '', location: '备件中心仓' })
const traceForm = ref({ location: '', description: '' })

const statusShort = (s) => ({ 0: '待付款', 1: '待发货', 2: '运输中', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusText = (s) => ({
  0: '等待企业付款，付款成功后锁定库存。',
  1: '订单已付款，等待供应商录入发货信息。',
  2: '订单已发货，等待收货确认。',
  3: '订单已签收，履约完成。',
  4: '订单已取消。'
}[s])
const statusType = (s) => ({ 0: 'warning', 1: 'info', 2: 'primary', 3: 'success', 4: 'info' }[s] || 'info')

const canFulfill = computed(() => {
  const userId = userStore.userInfo?.id
  const type = userStore.userInfo?.userType
  return detail.value && (detail.value.order.sellerId === userId || type === 3 || type === 9)
})

const handleCancel = async () => {
  await ElMessageBox.confirm('确定取消订单？', '提示')
  await cancelOrder(detail.value.order.id)
  ElMessage.success('已取消')
  loadDetail()
}

const handleConfirm = async () => {
  await ElMessageBox.confirm('确认已收到货物？', '提示')
  await confirmReceive(detail.value.order.id)
  ElMessage.success('确认收货成功')
  loadDetail()
}

const handleShip = async () => {
  submitting.value = true
  try {
    await shipOrder(detail.value.order.id, shipForm.value)
    ElMessage.success('发货信息已录入')
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

const handleAppendTrace = async () => {
  submitting.value = true
  try {
    await appendShipmentTrace(detail.value.order.id, traceForm.value)
    ElMessage.success('物流轨迹已追加')
    traceForm.value = { location: '', description: '' }
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    if (userStore.token && !userStore.userInfo) {
      await userStore.fetchUserInfo()
    }
    const res = await getOrderDetail(route.params.id)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.order-detail-page {
  max-width: var(--efh-max-width);
  margin: 0 auto;
}

.section {
  margin-bottom: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thumb {
  width: 54px;
  height: 54px;
  object-fit: cover;
  border-radius: 6px;
  background: #eef3f1;
}

.shipment-head {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.shipment-head div {
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  padding: 12px;
}

.shipment-head label {
  display: block;
  color: var(--efh-text-muted);
  font-size: 12px;
}

.shipment-head strong {
  display: block;
  margin-top: 4px;
}

.loc {
  color: var(--efh-text-muted);
  font-size: 12px;
}

.fulfill-form {
  margin-top: 16px;
}

.side-panel {
  position: sticky;
  top: 78px;
  background: #fff;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  padding: 16px;
}

.actions {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.actions .el-button {
  width: 100%;
  margin-left: 0;
}

@media (max-width: 900px) {
  .detail-grid,
  .shipment-head {
    grid-template-columns: 1fr;
  }

  .side-panel {
    position: static;
  }
}
</style>
