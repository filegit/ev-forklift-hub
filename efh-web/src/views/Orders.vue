<template>
  <div class="orders-page">
    <el-card>
      <template #header>我的订单</template>
      <el-tabs v-model="activeTab" @tab-change="fetchOrders">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>

      <div v-loading="loading">
        <el-card v-for="order in orders" :key="order.id" class="order-card" shadow="never">
          <div class="order-head">
            <span>订单号：{{ order.orderNo }}</span>
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </div>
          <div class="order-body">
            <p>收货人：{{ order.receiverName }} {{ order.receiverPhone }}</p>
            <p class="amount">¥{{ order.payAmount }}</p>
          </div>
          <div class="order-foot">
            <span class="time">{{ order.createTime }}</span>
            <div>
              <el-button v-if="order.status === 0" type="danger" size="small" @click="goPay(order)">去支付</el-button>
              <el-button v-if="order.status === 0" size="small" @click="handleCancel(order)">取消</el-button>
              <el-button type="primary" size="small" @click="$router.push(`/orders/${order.id}`)">查看详情</el-button>
            </div>
          </div>
        </el-card>
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

const statusText = (s) => ({ 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'info', 2: 'primary', 3: 'success', 4: 'info' }[s] || 'info')

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (activeTab.value !== 'all') params.status = Number(activeTab.value)
    const res = await getOrderList(params)
    orders.value = res.data.records
    total.value = res.data.total
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
.orders-page { max-width: 900px; margin: 0 auto; }
.order-card { margin-bottom: 12px; }
.order-head { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.order-body { display: flex; justify-content: space-between; color: #606266; margin-bottom: 12px; }
.amount { color: #f56c6c; font-size: 18px; font-weight: bold; }
.order-foot { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #eee; padding-top: 12px; }
.time { font-size: 12px; color: #909399; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }
</style>
