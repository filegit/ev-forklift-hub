<template>
  <div class="parts-page efh-page">
    <section class="parts-hero">
      <div>
        <span class="efh-kicker">EV FORKLIFT PARTS</span>
        <h1>车企备件采购与停机保障</h1>
        <p>围绕电池、电机、电控、液压与充电设备，快速完成选型、下单、支付与履约跟踪。</p>
      </div>
      <el-button v-if="canManage" type="primary" plain @click="$router.push('/parts/admin')">
        <el-icon><Operation /></el-icon>
        备件管理
      </el-button>
    </section>

    <div class="efh-metric-grid efh-section">
      <div class="efh-metric">
        <label>在售备件</label>
        <strong>{{ total }}</strong>
      </div>
      <div class="efh-metric">
        <label>核心品类</label>
        <strong>5</strong>
      </div>
      <div class="efh-metric">
        <label>履约方式</label>
        <strong>人工发货</strong>
      </div>
      <div class="efh-metric">
        <label>支付接入</label>
        <strong>支付宝</strong>
      </div>
    </div>

    <el-card class="search-card efh-section">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索备件名称、型号、适配车型"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-radio-group v-model="category" class="category-group" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="电池">电池</el-radio-button>
        <el-radio-button label="电机">电机</el-radio-button>
        <el-radio-button label="控制器">控制器</el-radio-button>
        <el-radio-button label="液压">液压</el-radio-button>
        <el-radio-button label="充电设备">充电设备</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="8" v-for="part in partsList" :key="part.id">
        <el-card class="part-card efh-product-card" shadow="hover" @click="goDetail(part.id)">
          <div class="part-cover">
            <img :src="getImage(part.images)" class="efh-product-image" />
            <el-tag class="stock-tag" :type="part.stock > 10 ? 'success' : 'warning'">
              库存 {{ part.stock || 0 }}
            </el-tag>
          </div>
          <div class="part-info">
            <div class="part-title-row">
              <h4>{{ part.name }}</h4>
              <el-tag size="small" effect="plain">{{ part.category }}</el-tag>
            </div>
            <p class="part-desc">{{ part.description || '适配新能源叉车高频维保场景' }}</p>
            <div class="part-specs">
              <span>{{ part.brand || '原厂/同质件' }}</span>
              <span>{{ part.model || '通用适配' }}</span>
              <span>销量 {{ part.salesCount || 0 }}</span>
            </div>
            <div class="part-footer">
              <span class="efh-product-price">¥{{ part.price }}</span>
              <el-button type="primary" size="small" @click.stop="handleAddCart(part)">
                <el-icon><ShoppingCart /></el-icon>
                加入采购车
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && partsList.length === 0" description="暂无匹配备件" />

    <div class="efh-pagination">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPartsList, addToCart } from '@/api/parts'
import { ElMessage } from 'element-plus'
import { Operation, Search, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const partsList = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const keyword = ref('')
const category = ref('')

const canManage = computed(() => {
  const t = userStore.userInfo?.userType
  return t === 3 || t === 9
})

const getImage = (images) => {
  if (!images) return 'https://via.placeholder.com/480x320/f4f7f6/0f766e?text=EV+Parts'
  return images.split(',')[0]
}

const fetchPartsList = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value
    const res = await getPartsList(params)
    partsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取备件列表失败')
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
  await addToCart({ partsId: part.id, quantity: 1 })
  ElMessage.success('已加入采购车')
}

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      /* ignore */
    }
  }
  fetchPartsList()
})
</script>

<style scoped>
.parts-page {
  max-width: var(--efh-max-width);
}

.parts-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  padding: 22px;
  margin-bottom: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: var(--efh-radius);
  background: linear-gradient(135deg, #ffffff 0%, #eef8f5 100%);
}

.parts-hero h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
}

.parts-hero p {
  max-width: 640px;
  margin: 10px 0 0;
  color: var(--efh-text-secondary);
}

.search-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.category-group {
  display: flex;
  flex-wrap: wrap;
}

.part-card {
  margin-bottom: 16px;
}

.part-cover {
  position: relative;
}

.stock-tag {
  position: absolute;
  right: 10px;
  top: 10px;
}

.part-info {
  padding-top: 12px;
}

.part-title-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
}

.part-info h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  min-height: 44px;
}

.part-desc {
  color: var(--efh-text-secondary);
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  margin: 8px 0;
  line-height: 1.5;
}

.part-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.part-specs span {
  padding: 3px 8px;
  border-radius: 6px;
  background: #f4f7f6;
  color: var(--efh-text-secondary);
  font-size: 12px;
}

.part-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .parts-hero {
    flex-direction: column;
    padding: 18px;
  }

  .parts-hero h1 {
    font-size: 22px;
  }

  .search-bar {
    grid-template-columns: 1fr;
  }
}
</style>
