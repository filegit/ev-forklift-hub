<template>
  <div class="detail-page" v-loading="loading">
    <el-card v-if="part">
      <el-row :gutter="30">
        <el-col :span="10">
          <img :src="mainImage" class="main-image" />
        </el-col>
        <el-col :span="14">
          <h2>{{ part.name }}</h2>
          <p class="desc">{{ part.description }}</p>
          <div class="tags">
            <el-tag>{{ part.category }}</el-tag>
            <el-tag type="info">{{ part.brand }}</el-tag>
            <el-tag type="warning">型号 {{ part.model }}</el-tag>
          </div>
          <div class="price-row">
            <span class="label">价格</span>
            <span class="price">¥{{ part.price }}</span>
          </div>
          <div class="stock-row">
            <span>库存：{{ part.stock }} 件</span>
            <span>销量：{{ part.salesCount || 0 }}</span>
          </div>
          <div class="qty-row">
            <span>数量</span>
            <el-input-number v-model="quantity" :min="1" :max="part.stock" />
          </div>
          <div class="actions">
            <el-button type="warning" size="large" @click="addCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPartsDetail, addToCart } from '@/api/parts'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const part = ref(null)
const quantity = ref(1)

const mainImage = computed(() => {
  if (!part.value?.images) return 'https://via.placeholder.com/400x300?text=Parts'
  return part.value.images.split(',')[0]
})

const checkLogin = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const addCart = async () => {
  if (!checkLogin()) return
  await addToCart({ partsId: part.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

const buyNow = () => {
  if (!checkLogin()) return
  sessionStorage.setItem('checkout_direct', JSON.stringify({
    directItems: [{ partsId: part.value.id, quantity: quantity.value }]
  }))
  router.push('/checkout?type=direct')
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getPartsDetail(route.params.id)
    part.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page { max-width: 1000px; margin: 0 auto; }
.main-image { width: 100%; border-radius: 8px; }
.desc { color: #606266; line-height: 1.8; margin: 16px 0; }
.tags { display: flex; gap: 8px; margin-bottom: 20px; }
.price-row { background: #fdf6ec; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.label { color: #909399; margin-right: 12px; }
.price { color: #f56c6c; font-size: 28px; font-weight: bold; }
.stock-row { display: flex; gap: 24px; color: #606266; margin-bottom: 20px; }
.qty-row { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.actions { display: flex; gap: 16px; }
</style>
