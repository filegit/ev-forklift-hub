<template>
  <div class="orders-page">
    <div class="efh-page-header">
      <div>
        <span class="efh-kicker">订单中心</span>
        <h2>备件采购订单</h2>
        <p>跟踪付款、发货、在途和签收状态，适合面试展示完整交易闭环。</p>
      </div>
    </div>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="fetchOrders">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="运输中" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>

      <div v-loading="loading">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-head">
            <div>
              <strong>{{ order.orderNo }}</strong>
              <span>{{ order.createTime }}</span>
            </div>
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </div>
          <div class="order-body">
            <div>
              <label>收货信息</label>
              <p>{{ order.receiverName }} {{ order.receiverPhone }}</p>
              <p>{{ order.receiverAddress }}</p>
            </div>
            <div class="amount">
              <label>应付金额</label>
              <strong>¥{{ order.payAmount }}</strong>
            </div>
          </div>
          <div class="order-foot">
            <span>{{ statusHint(order.status) }}</span>
            <div>
              <el-button v-if="order.status === 0" type="primary" size="small" @click="goPay(order)">去支付</el-button>
              <el-button v-if="order.status === 0" size="small" @click="handleCancel(order)">取消</el-button>
              <el-button type="primary" plain size="small" @click="$router.push(`/orders/${order.id}`)">查看详情</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, cancelOrder } from '@/api/parts'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const activeTab = ref('all')
const page = ref(1)
const total = ref(0)

const statusText = (s) => ({ 0: '待付款', 1: '待发货', 2: '运输中', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'info', 2: 'primary', 3: 'success', 4: 'info' }[s] || 'info')
const statusHint = (s) => ({
  0: '等待企业支付，支付后锁定库存。',
  1: '已付款，等待供应商录入发货信息。',
  2: '已发货，可在详情页查看物流轨迹。',
  3: '订单已签收完成。',
  4: '订单已取消。'
}[s] || '')

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (activeTab.value !== 'all') params.status = Number(activeTab.value)
    const res = await getOrderList(params)
    orders.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const goPay = (order) => router.push(`/pay/${order.orderNo}`)

const handleCancel = async (order) => {
  await ElMessageBox.confirm('确定取消该订单？', '提示')
  await cancelOrder(order.id)
  ElMessage.success('已取消')
  fetchOrders()
}

onMounted(fetchOrders)
</script>

<style scoped>
.orders-page {
  max-width: var(--efh-max-width);
  margin: 0 auto;
}

.order-card {
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.order-head,
.order-foot {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  background: #f8fbfa;
}

.order-head strong {
  display: block;
}

.order-head span,
.order-foot span,
.order-body label {
  color: var(--efh-text-muted);
  font-size: 12px;
}

.order-body {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 14px;
}

.order-body p {
  margin: 4px 0 0;
  color: var(--efh-text-secondary);
}

.amount {
  min-width: 150px;
  text-align: right;
}

.amount strong {
  display: block;
  color: #b45309;
  font-size: 22px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .order-head,
  .order-foot,
  .order-body {
    align-items: flex-start;
    flex-direction: column;
  }

  .amount {
    text-align: left;
  }
}
</style>
