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
          <el-table :data="orderList" style="width: 100%">
            <el-table-column prop="id" label="订单号" width="100" />
            <el-table-column prop="serviceType" label="服务类型" width="120" />
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
                <el-button size="small" type="danger" @click="cancelOrder(row)" v-if="row.status === 'pending'">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="orderList.length === 0" description="暂无订单" />
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
    
    <!-- 预约维修对话框 -->
    <el-dialog v-model="showOrderDialog" title="预约维修" width="500px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="服务类型">
          <el-select v-model="orderForm.serviceType" placeholder="请选择">
            <el-option label="故障诊断" value="diagnosis" />
            <el-option label="维修保养" value="maintenance" />
            <el-option label="紧急救援" value="emergency" />
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
        <el-button type="primary" @click="submitOrder">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref(userStore.token ? 'orders' : 'intro')
const orderList = ref([])
const showOrderDialog = ref(false)
const orderForm = ref({
  serviceType: '',
  description: '',
  phone: '',
  address: ''
})

const fetchOrderList = async () => {
  // TODO: 调用订单列表 API
  ElMessage.info('订单列表功能开发中')
}

const submitOrder = () => {
  ElMessage.success('预约成功')
  showOrderDialog.value = false
}

const viewOrder = (row) => {
  ElMessage.info('查看订单详情')
}

const cancelOrder = (row) => {
  ElMessage.success('取消成功')
}

const getStatusType = (status) => {
  const map = { pending: 'warning', processing: 'primary', completed: 'success', cancelled: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { pending: '待处理', processing: '处理中', completed: '已完成', cancelled: '已取消' }
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
