<template>
  <div class="service-page">
    <div class="service-header">
      <div>
        <h2>售后工单</h2>
        <p>面向故障诊断、维修保养和紧急救援的售后履约入口。</p>
      </div>
      <el-button type="primary" v-if="userStore.token" @click="showOrderDialog = true">创建工单</el-button>
    </div>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的工单" name="orders" v-if="userStore.token">
          <el-table :data="orderList" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="工单号" width="180" />
            <el-table-column prop="title" label="服务类型" width="130" />
            <el-table-column prop="description" label="问题描述" min-width="220" />
            <el-table-column label="发布时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" @click="viewOrder(row)">查看</el-button>
                <el-button size="small" type="danger" v-if="row.status === 0" @click="cancelOrder(row)">取消</el-button>
              </template>
            </el-table-column>
          </el-table>
          <EfhEmptyState v-if="!loading && orderList.length === 0" title="暂无工单" description="创建工单后可在这里查看处理进度。" />
        </el-tab-pane>

        <el-tab-pane label="服务介绍" name="intro">
          <div class="service-intro">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="8" v-for="item in serviceItems" :key="item.title">
                <el-card shadow="hover" class="intro-card">
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.desc }}</p>
                  <div class="price">{{ item.price }}</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="showOrderDialog" title="创建售后工单" width="520px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="服务类型">
          <el-select v-model="orderForm.serviceType" placeholder="请选择" style="width: 100%">
            <el-option label="故障诊断" :value="1" />
            <el-option label="维修保养" :value="2" />
            <el-option label="紧急救援" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片预览">
          <ImageUploadPreview button-text="选择现场图片" @change="orderImages = $event" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="orderForm.description" type="textarea" :rows="4" placeholder="描述故障现象、车辆型号、现场工况" />
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
        <el-button type="primary" :loading="submitting" @click="submitOrder">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceOrders, createServiceOrder, cancelServiceOrder } from '@/api/service'
import { formatDateTime } from '@/utils/format'
import EfhEmptyState from '@/components/EfhEmptyState.vue'
import ImageUploadPreview from '@/components/ImageUploadPreview.vue'

const userStore = useUserStore()
const activeTab = ref(userStore.token ? 'orders' : 'intro')
const orderList = ref([])
const loading = ref(false)
const submitting = ref(false)
const showOrderDialog = ref(false)
const orderImages = ref([])
const orderForm = ref({ serviceType: 1, description: '', phone: '', address: '' })

const serviceItems = [
  { title: '故障诊断', desc: '专业技师定位电控、电池、液压和驱动系统异常。', price: '¥200 起' },
  { title: '维修保养', desc: '按保养周期维护车辆，降低停机风险。', price: '¥500 起' },
  { title: '紧急救援', desc: '面向现场停机、无法充电、无法行走等紧急场景。', price: '¥800 起' }
]

const fetchOrderList = async () => {
  if (!userStore.token) return
  loading.value = true
  try {
    const res = await getServiceOrders()
    orderList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取工单列表失败')
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
    const imageText = orderImages.value.length ? `\n\n现场图片：\n${orderImages.value.map(item => item.name).join('\n')}` : ''
    await createServiceOrder({ ...orderForm.value, description: `${orderForm.value.description}${imageText}` })
    ElMessage.success('工单创建成功')
    showOrderDialog.value = false
    orderForm.value = { serviceType: 1, description: '', phone: '', address: '' }
    orderImages.value = []
    await fetchOrderList()
  } finally {
    submitting.value = false
  }
}

const viewOrder = (row) => {
  ElMessageBox.alert(
    `工单号：${row.orderNo}\n服务：${row.title}\n发布时间：${formatDateTime(row.createTime)}\n描述：${row.description}\n地址：${row.address}\n电话：${row.phone}`,
    '工单详情',
    { confirmButtonText: '确定' }
  )
}

const cancelOrder = async (row) => {
  await ElMessageBox.confirm('确定取消该工单吗？', '提示', { type: 'warning' })
  await cancelServiceOrder(row.id)
  ElMessage.success('取消成功')
  await fetchOrderList()
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info' }[status] || 'info')
const getStatusText = (status) => ({ 0: '待处理', 1: '已接单', 2: '服务中', 3: '已完成', 4: '已取消' }[status] || '未知')

onMounted(() => {
  if (userStore.token) fetchOrderList()
})
</script>

<style scoped>
.service-page {
  max-width: 1200px;
  margin: 0 auto;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.service-header h2 {
  margin: 0;
}

.service-header p {
  margin: 4px 0 0;
  color: var(--efh-text-secondary);
}

.service-intro {
  padding: 20px 0;
}

.intro-card {
  min-height: 180px;
}

.intro-card h3 {
  margin-bottom: 10px;
}

.intro-card p {
  color: var(--efh-text-secondary);
  margin-bottom: 15px;
  line-height: 1.7;
}

.price {
  color: #b45309;
  font-size: 20px;
  font-weight: bold;
}
</style>
