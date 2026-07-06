<template>
  <div class="cart-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>购物车</span>
          <el-button type="primary" :disabled="selected.length === 0" @click="goCheckout">去结算 ({{ selected.length }})</el-button>
        </div>
      </template>

      <el-table :data="cartList" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="item-cell">
              <img :src="row.image" class="thumb" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="160">
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
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && cartList.length === 0" description="购物车是空的">
        <el-button type="primary" @click="$router.push('/parts')">去逛逛</el-button>
      </el-empty>

      <div class="footer" v-if="cartList.length">
        <span>已选 {{ selected.length }} 件，合计：</span>
        <span class="total">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartQuantity, removeFromCart } from '@/api/parts'
import { ElMessage } from 'element-plus'

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
  ElMessage.success('已删除')
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
.cart-page { max-width: 1000px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.item-cell { display: flex; align-items: center; gap: 12px; }
.thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.subtotal { color: #f56c6c; font-weight: bold; }
.footer { margin-top: 20px; text-align: right; font-size: 16px; }
.total { color: #f56c6c; font-size: 22px; font-weight: bold; margin-left: 8px; }
</style>
