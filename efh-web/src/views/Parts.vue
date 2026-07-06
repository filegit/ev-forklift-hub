<template>
  <div class="parts-page">
    <el-card class="search-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="10">
          <el-input
            v-model="keyword"
            placeholder="搜索配件名称、描述..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="14">
          <el-radio-group v-model="category" @change="handleSearch">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="电池">电池</el-radio-button>
            <el-radio-button label="电机">电机</el-radio-button>
            <el-radio-button label="控制器">控制器</el-radio-button>
            <el-radio-button label="液压">液压</el-radio-button>
            <el-radio-button label="充电设备">充电设备</el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="part in partsList" :key="part.id">
        <el-card class="part-card" shadow="hover" @click="goDetail(part.id)">
          <img :src="getImage(part.images)" class="part-image" />
          <div class="part-info">
            <h4>{{ part.name }}</h4>
            <p class="part-desc">{{ part.description }}</p>
            <div class="part-meta">
              <el-tag size="small">{{ part.category }}</el-tag>
              <span class="sales">已售 {{ part.salesCount || 0 }}</span>
            </div>
            <div class="part-footer">
              <span class="price">¥{{ part.price }}</span>
              <el-button type="primary" size="small" @click.stop="handleAddCart(part)">加购物车</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && partsList.length === 0" description="暂无配件" />

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchPartsList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPartsList, addToCart } from '@/api/parts'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const partsList = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const keyword = ref('')
const category = ref('')

const getImage = (images) => {
  if (!images) return 'https://via.placeholder.com/300x200?text=Parts'
  return images.split(',')[0]
}

const fetchPartsList = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value
    const res = await getPartsList(params)
    partsList.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('获取配件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPartsList()
}

const goDetail = (id) => router.push(`/parts/${id}`)

const handleAddCart = async (part) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCart({ partsId: part.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchPartsList)
</script>

<style scoped>
.parts-page { max-width: 1200px; margin: 0 auto; }
.search-card { margin-bottom: 20px; }
.part-card { margin-bottom: 20px; cursor: pointer; }
.part-image { width: 100%; height: 180px; object-fit: cover; border-radius: 4px; }
.part-info h4 { margin: 12px 0 8px; font-size: 15px; height: 40px; overflow: hidden; }
.part-desc { color: #909399; font-size: 13px; height: 36px; overflow: hidden; margin-bottom: 8px; }
.part-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.sales { font-size: 12px; color: #909399; }
.part-footer { display: flex; justify-content: space-between; align-items: center; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
