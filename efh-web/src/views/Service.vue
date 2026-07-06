<template>
  <div class="service-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修服务</span>
          <el-button type="primary" @click="showOrderDialog = true" v-if="userStore.token">
            预约维修
          </el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的订单" name="orders" v-if="userStore.token">
          <el-table :data="orderList" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="工单号" width="180" />
            <el-table-column prop="title" label="服务类型" width="120" />
            <el-table-column prop="description" label="问题描述" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" @click="viewOrder(row)">查看</el-button>
                <el-button size="small" type="danger" @click="cancelOrder(row)" v-if="row.status === 0">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />
        </el-tab-pane>
        
        <el-tab-pane label="服务介绍" name="intro">
          <div class="service-intro">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card shadow="hover">
                  <h3>故障诊断</h3>
                  <p>专业技师上门诊断叉车故障，快速定位问题</p>
                  <div class="price">¥200 起</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover">
                  <h3>维修保养</h3>
                  <p>定期保养维护，延长叉车使用寿命</p>
                  <div class="price">¥500 起</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover">
                  <h3>紧急救援</h3>
                  <p>24小时紧急救援服务，快速响应</p>
                  <div class="price">¥800 起</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog v-model="showOrderDialog" title="预约维修" width="500px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="服务类型">
          <el-select v-model="orderForm.serviceType" placeholder="请选择">
            <el-option label="故障诊断" :value="1" />
            <el-option label="维修保养" :value="2" />
            <el-option label="紧急救援" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="orderForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="orderForm.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="orderForm.address" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceOrders, createServiceOrder, cancelServiceOrder } from '@/api/service'

const userStore = useUserStore()
const activeTab = ref(userStore.token ? 'orders' : 'intro')
const orderList = ref([])
const loading = ref(false)
const submitting = ref(false)
const showOrderDialog = ref(false)
const orderForm = ref({
  serviceType: 1,
  description: '',
  phone: '',
  address: ''
})

const fetchOrderList = async () => {
  if (!userStore.token) return
  loading.value = true
  try {
    const res = await getServiceOrders()
    orderList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const submitOrder = async () => {
  if (!orderForm.value.serviceType || !orderForm.value.description || !orderForm.value.phone || !orderForm.value.address) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await createServiceOrder(orderForm.value)
    ElMessage.success('预约成功')
    showOrderDialog.value = false
    orderForm.value = { serviceType: 1, description: '', phone: '', address: '' }
    await fetchOrderList()
  } catch (error) {
    // handled by request interceptor
  } finally {
    submitting.value = false
  }
}

const viewOrder = (row) => {
  ElMessageBox.alert(
    `工单号：${row.orderNo}\n服务：${row.title}\n描述：${row.description}\n地址：${row.address}\n电话：${row.phone}`,
    '工单详情',
    { confirmButtonText: '确定' }
  )
}

const cancelOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该工单吗？', '提示', { type: 'warning' })
    await cancelServiceOrder(row.id)
    ElMessage.success('取消成功')
    await fetchOrderList()
  } catch (error) {
    if (error !== 'cancel') {
      // handled by request interceptor
    }
  }
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '已接单', 2: '服务中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

onMounted(() => {
  if (userStore.token) {
    fetchOrderList()
  }
})
</script>

<style scoped>
.service-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-intro {
  padding: 20px 0;
}

.service-intro h3 {
  margin-bottom: 10px;
}

.service-intro p {
  color: #606266;
  margin-bottom: 15px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
</style>
