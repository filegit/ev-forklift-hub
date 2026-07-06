<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="part" class="detail-shell">
      <section class="media-panel">
        <img :src="mainImage" class="main-image" />
        <div class="delivery-strip">
          <div>
            <span>交付方式</span>
            <strong>仓库发货 / 运单录入</strong>
          </div>
          <div>
            <span>售后支持</span>
            <strong>维保工单联动</strong>
          </div>
        </div>
      </section>

      <section class="info-panel">
        <span class="efh-kicker">备件详情</span>
        <h1>{{ part.name }}</h1>
        <p class="desc">{{ part.description || '适配新能源叉车日常维保与故障抢修场景。' }}</p>

        <div class="tags">
          <el-tag>{{ part.category }}</el-tag>
          <el-tag type="info">{{ part.brand || '原厂/同质件' }}</el-tag>
          <el-tag type="warning">型号 {{ part.model || '通用适配' }}</el-tag>
        </div>

        <div class="price-box">
          <span>采购价</span>
          <strong>¥{{ part.price }}</strong>
          <small>含税/不含税以企业合同配置为准</small>
        </div>

        <div class="spec-grid">
          <div>
            <span>可用库存</span>
            <strong>{{ part.stock }} 件</strong>
          </div>
          <div>
            <span>累计采购</span>
            <strong>{{ part.salesCount || 0 }} 件</strong>
          </div>
          <div>
            <span>适配建议</span>
            <strong>{{ part.model || '按车型核对' }}</strong>
          </div>
        </div>

        <div class="qty-row">
          <span>采购数量</span>
          <el-input-number v-model="quantity" :min="1" :max="part.stock" />
        </div>

        <div class="actions">
          <el-button type="primary" size="large" @click="buyNow">
            <el-icon><ShoppingBag /></el-icon>
            立即采购
          </el-button>
          <el-button size="large" @click="addCart">
            <el-icon><ShoppingCart /></el-icon>
            加入采购车
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPartsDetail, addToCart } from '@/api/parts'
import { ElMessage } from 'element-plus'
import { ShoppingBag, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const part = ref(null)
const quantity = ref(1)

const mainImage = computed(() => {
  if (!part.value?.images) return 'https://via.placeholder.com/640x440/f4f7f6/0f766e?text=EV+Forklift+Parts'
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
  ElMessage.success('已加入采购车')
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
.detail-page {
  max-width: var(--efh-max-width);
  margin: 0 auto;
}

.detail-shell {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
  gap: 20px;
}

.media-panel,
.info-panel {
  background: #fff;
  border: 1px solid var(--efh-border-light);
  border-radius: var(--efh-radius);
  padding: 18px;
}

.main-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 8px;
  background: #eef3f1;
}

.delivery-strip {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
}

.delivery-strip div,
.spec-grid div {
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  padding: 12px;
}

.delivery-strip span,
.spec-grid span,
.price-box span {
  display: block;
  color: var(--efh-text-muted);
  font-size: 12px;
}

.delivery-strip strong,
.spec-grid strong {
  display: block;
  margin-top: 4px;
  font-size: 14px;
}

.info-panel h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.25;
}

.desc {
  color: var(--efh-text-secondary);
  line-height: 1.8;
  margin: 14px 0;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.price-box {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 14px;
}

.price-box strong {
  display: block;
  color: #b45309;
  font-size: 30px;
  line-height: 1.2;
}

.price-box small {
  color: var(--efh-text-secondary);
}

.spec-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .detail-shell,
  .spec-grid,
  .delivery-strip {
    grid-template-columns: 1fr;
  }
}
</style>
