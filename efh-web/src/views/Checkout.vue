<template>
  <div class="checkout-page" v-loading="loading">
    <div class="efh-page-header">
      <div>
        <span class="efh-kicker">订单确认</span>
        <h2>确认收货信息与备件清单</h2>
        <p>提交后生成采购订单，支付成功后进入仓库发货与物流录入流程。</p>
      </div>
    </div>

    <div class="checkout-grid">
      <main>
        <el-card class="section">
          <template #header>收货地点</template>
          <div v-if="addresses.length">
            <el-radio-group v-model="selectedAddressId" class="address-list" @change="loadPreview">
              <el-radio v-for="addr in addresses" :key="addr.id" :label="addr.id" border class="address-item">
                <div>
                  <strong>{{ addr.receiverName }}</strong>
                  <span>{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
                  <p>{{ formatAddress(addr) }}</p>
                </div>
              </el-radio>
            </el-radio-group>
          </div>
          <el-empty v-else description="暂无收货地址" />
          <el-button type="primary" link @click="showAddressDialog = true">新增收货地址</el-button>
        </el-card>

        <el-card class="section">
          <template #header>备件清单</template>
          <el-table :data="preview?.items || []">
            <el-table-column prop="name" label="备件" min-width="220" />
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
          <template #header>履约备注</template>
          <el-input
            v-model="remark"
            type="textarea"
            placeholder="可填写车型、车架号、故障场景、期望到货时间等信息"
            :rows="3"
          />
        </el-card>
      </main>

      <aside class="summary-panel">
        <h3>金额确认</h3>
        <div class="summary-row">
          <span>商品总额</span>
          <strong>¥{{ preview?.totalAmount || '0.00' }}</strong>
        </div>
        <div class="summary-row">
          <span>运费</span>
          <strong>¥{{ preview?.freightAmount || '0.00' }}</strong>
        </div>
        <div class="summary-row pay">
          <span>应付金额</span>
          <strong>¥{{ preview?.payAmount || '0.00' }}</strong>
        </div>
        <el-alert
          title="支付后订单进入待发货，供应商录入承运商和运单号后可查看物流。"
          type="info"
          :closable="false"
          show-icon
        />
        <el-button type="primary" size="large" class="submit-btn" @click="submit">
          提交订单
        </el-button>
      </aside>
    </div>

    <el-dialog v-model="showAddressDialog" title="新增收货地址" width="500px">
      <el-form :model="addressForm" label-width="86px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.phone" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="addressForm.province" placeholder="如：上海市" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="addressForm.city" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="addressForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detail" type="textarea" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
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
    if (e.message !== '请选择收货地址') ElMessage.error('加载订单预览失败')
  } finally {
    loading.value = false
  }
}

const saveAddress = async () => {
  await addAddress(addressForm.value)
  ElMessage.success('地址已保存')
  showAddressDialog.value = false
  const res = await getAddressList()
  addresses.value = res.data || []
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
.checkout-page {
  max-width: var(--efh-max-width);
  margin: 0 auto;
}

.checkout-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 18px;
  align-items: start;
}

.section {
  margin-bottom: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.address-item {
  width: 100%;
  height: auto;
  padding: 12px;
  margin: 0 !important;
}

.address-item strong {
  margin-right: 8px;
}

.address-item span {
  margin-right: 8px;
}

.address-item p {
  margin: 6px 0 0;
  color: var(--efh-text-secondary);
  font-size: 13px;
}

.summary-panel {
  position: sticky;
  top: 78px;
  background: #fff;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  padding: 18px;
}

.summary-panel h3 {
  margin: 0 0 14px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--efh-border-light);
}

.summary-row span {
  color: var(--efh-text-secondary);
}

.summary-row.pay {
  border-bottom: none;
  margin-bottom: 12px;
}

.summary-row.pay strong {
  color: #b45309;
  font-size: 24px;
}

.submit-btn {
  width: 100%;
  margin-top: 14px;
}

@media (max-width: 900px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }

  .summary-panel {
    position: static;
  }
}
</style>
