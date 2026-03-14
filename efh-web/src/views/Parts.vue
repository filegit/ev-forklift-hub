<template>
  <div class="parts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配件商城</span>
          <el-button type="primary" @click="showAddDialog = true" v-if="userStore.token">
            添加配件
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6" v-for="part in partsList" :key="part.id">
          <el-card class="part-card" shadow="hover">
            <img :src="part.image || 'https://via.placeholder.com/200'" class="part-image" />
            <div class="part-info">
              <h4>{{ part.name }}</h4>
              <p class="part-desc">{{ part.description }}</p>
              <div class="part-footer">
                <span class="price">¥{{ part.price }}</span>
                <el-button type="primary" size="small" @click="handleOrder(part)">
                  购买
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="partsList.length === 0" description="暂无配件" />
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchPartsList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const partsList = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const showAddDialog = ref(false)

const fetchPartsList = async () => {
  // TODO: 调用配件列表 API
  ElMessage.info('配件列表功能开发中')
}

const handleOrder = (part) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  ElMessage.success('下单成功')
}

onMounted(() => {
  fetchPartsList()
})
</script>

<style scoped>
.parts-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.part-card {
  margin-bottom: 20px;
}

.part-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.part-info {
  padding: 10px 0;
}

.part-info h4 {
  margin: 10px 0;
  font-size: 16px;
}

.part-desc {
  color: #909399;
  font-size: 14px;
  margin: 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.part-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
