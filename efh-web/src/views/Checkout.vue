<template>
  <div class="checkout-page" v-loading="loading">
    <el-card class="section">
      <template #header>收货地址</template>
      <div v-if="addresses.length">
        <el-radio-group v-model="selectedAddressId" class="address-list">
          <el-radio v-for="addr in addresses" :key="addr.id" :label="addr.id" border class="address-item">
            <div>
              <strong>{{ addr.receiverName }}</strong> {{ addr.phone }}
              <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
              <p>{{ formatAddress(addr) }}</p>
            </div>
          </el-radio>
        </el-radio-group>
      </div>
      <el-empty v-else description="暂无收货地址" />
      <el-button type="primary" link @click="showAddressDialog = true">+ 新增地址</el-button>
    </el-card>

    <el-card class="section">
      <template #header>商品清单</template>
      <el-table :data="preview?.items || []">
        <el-table-column prop="name" label="商品" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ row.subtotal }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="section">
      <el-input v-model="remark" type="textarea" placeholder="订单备注（选填）" :rows="2" />
      <div class="amount-row">
        <span>商品总额：¥{{ preview?.totalAmount || '0.00' }}</span>
        <span>运费：¥{{ preview?.freightAmount || '0.00' }}</span>
        <span class="pay">应付：¥{{ preview?.payAmount || '0.00' }}</span>
      </div>
      <el-button type="danger" size="large" class="submit-btn" @click="submit">提交订单</el-button>
    </el-card>

    <el-dialog v-model="showAddressDialog" title="新增收货地址" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.phone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="addressForm.province" placeholder="如：上海市" /></el-form-item>
        <el-form-item label="市"><el-input v-model="addressForm.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="addressForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detail" type="textarea" /></el-form-item>
        <el-form-item label="默认"><el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { previewOrder, submitOrder } from '@/api/parts'
import { getAddressList, addAddress, formatAddress } from '@/api/address'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const preview = ref(null)
const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const showAddressDialog = ref(false)
const checkoutPayload = ref({})
const addressForm = ref({
  receiverName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 1
})

const buildSubmitBody = () => {
  const addr = addresses.value.find(a => a.id === selectedAddressId.value)
  if (!addr) throw new Error('请选择收货地址')
  return {
    ...checkoutPayload.value,
    addressId: addr.id,
    receiverName: addr.receiverName,
    receiverPhone: addr.phone,
    receiverAddress: formatAddress(addr),
    remark: remark.value
  }
}

const loadPreview = async () => {
  loading.value = true
  try {
    const body = buildSubmitBody()
    const res = await previewOrder(body)
    preview.value = res.data
  } catch (e) {
    if (e.message !== '请选择收货地址') ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const saveAddress = async () => {
  await addAddress(addressForm.value)
  ElMessage.success('地址已保存')
  showAddressDialog.value = false
  const res = await getAddressList()
  addresses.value = res.data
  if (!selectedAddressId.value && addresses.value.length) {
    selectedAddressId.value = addresses.value.find(a => a.isDefault === 1)?.id || addresses.value[0].id
  }
  loadPreview()
}

const submit = async () => {
  try {
    const body = buildSubmitBody()
    const res = await submitOrder(body)
    sessionStorage.removeItem('checkout_cart')
    sessionStorage.removeItem('checkout_direct')
    ElMessage.success('订单提交成功')
    router.push(`/pay/${res.data.orderNo}`)
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  }
}

onMounted(async () => {
  if (route.query.type === 'direct') {
    checkoutPayload.value = JSON.parse(sessionStorage.getItem('checkout_direct') || '{}')
  } else {
    checkoutPayload.value = JSON.parse(sessionStorage.getItem('checkout_cart') || '{}')
  }
  const res = await getAddressList()
  addresses.value = res.data || []
  selectedAddressId.value = addresses.value.find(a => a.isDefault === 1)?.id || addresses.value[0]?.id
  if (selectedAddressId.value) loadPreview()
})
</script>

<style scoped>
.checkout-page { max-width: 900px; margin: 0 auto; }
.section { margin-bottom: 16px; }
.address-list { display: flex; flex-direction: column; gap: 12px; width: 100%; }
.address-item { width: 100%; height: auto; padding: 12px; margin: 0 !important; }
.address-item p { margin: 4px 0 0; color: #606266; font-size: 13px; }
.amount-row { display: flex; justify-content: flex-end; gap: 24px; margin: 20px 0; font-size: 15px; }
.pay { color: #f56c6c; font-size: 20px; font-weight: bold; }
.submit-btn { width: 100%; }
</style>
