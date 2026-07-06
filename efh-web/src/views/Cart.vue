<template>
  <div class="cart-page">
    <div class="efh-page-header">
      <div>
        <span class="efh-kicker">采购车</span>
        <h2>备件批量采购</h2>
        <p>统一勾选需要补库或抢修的备件，提交后按供应商拆单履约。</p>
      </div>
      <el-button type="primary" :disabled="selected.length === 0" @click="goCheckout">
        <el-icon><Tickets /></el-icon>
        去结算 {{ selected.length }} 项
      </el-button>
    </div>

    <el-card>
      <el-table :data="cartList" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="备件" min-width="300">
          <template #default="{ row }">
            <div class="item-cell">
              <img :src="row.image" class="thumb" />
              <div>
                <strong>{{ row.name }}</strong>
                <p>库存 {{ row.stock }} 件，适合维保补库与现场抢修</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="170">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock"
              size="small"
              @change="(v) => updateQty(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="130">
          <template #default="{ row }">
            <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="remove(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && cartList.length === 0" description="采购车暂无备件">
        <el-button type="primary" @click="$router.push('/parts')">去选备件</el-button>
      </el-empty>

      <div class="footer" v-if="cartList.length">
        <div>
          <span>已选 {{ selected.length }} 项</span>
          <small>付款后进入仓库发货流程</small>
        </div>
        <div class="amount">
          <span>合计</span>
          <strong>¥{{ totalAmount.toFixed(2) }}</strong>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartQuantity, removeFromCart } from '@/api/parts'
import { ElMessage } from 'element-plus'
import { Tickets } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const cartList = ref([])
const selected = ref([])

const totalAmount = computed(() =>
  selected.value.reduce((sum, row) => sum + row.price * row.quantity, 0)
)

const fetchCart = async () => {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (rows) => { selected.value = rows }

const updateQty = async (row, qty) => {
  if (!qty) return
  await updateCartQuantity(row.partsId, qty)
  row.subtotal = row.price * qty
}

const remove = async (row) => {
  await removeFromCart(row.partsId)
  ElMessage.success('已移除')
  fetchCart()
}

const goCheckout = () => {
  const ids = selected.value.map(i => i.id)
  sessionStorage.setItem('checkout_cart', JSON.stringify({ cartItemIds: ids }))
  router.push('/checkout?type=cart')
}

onMounted(fetchCart)
</script>

<style scoped>
.cart-page {
  max-width: var(--efh-max-width);
  margin: 0 auto;
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-cell strong {
  display: block;
  color: var(--efh-text);
}

.item-cell p {
  margin: 4px 0 0;
  color: var(--efh-text-muted);
  font-size: 12px;
}

.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  background: #eef3f1;
}

.subtotal {
  color: #b45309;
  font-weight: 700;
}

.footer {
  margin-top: 18px;
  padding: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.footer small {
  display: block;
  margin-top: 4px;
  color: var(--efh-text-muted);
}

.amount {
  text-align: right;
}

.amount span {
  color: var(--efh-text-secondary);
}

.amount strong {
  display: block;
  color: #b45309;
  font-size: 24px;
}

@media (max-width: 768px) {
  .footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .amount {
    text-align: left;
  }
}
</style>
