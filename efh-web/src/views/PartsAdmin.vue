<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>配件管理</h2>
      <el-button @click="$router.push('/parts')">返回配件商城</el-button>
    </div>

    <el-card class="form-card">
      <template #header>上架新配件</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="配件名称" prop="name">
              <el-input v-model="form.name" placeholder="如：48V 磷酸铁锂电池组" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="如：比亚迪" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="型号">
              <el-input v-model="form.model" placeholder="如：BYD-48V-200AH" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="配件详细描述" />
        </el-form-item>
        <el-form-item label="图片 URL">
          <el-input v-model="form.images" placeholder="图片链接，多张用英文逗号分隔" />
          <div class="tip">可填网络图片地址，留空则显示默认占位图</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handlePublish">上架配件</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>已上架配件</template>
      <el-table :data="partsList" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getPartsList, publishParts } from '@/api/parts'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const partsList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categories = ['电池', '电机', '控制器', '液压', '充电设备', '其他']

const defaultForm = () => ({
  name: '',
  description: '',
  category: '电池',
  brand: '',
  model: '',
  price: 100,
  stock: 10,
  images: ''
})

const form = ref(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入配件名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const canManage = () => {
  const t = userStore.userInfo?.userType
  return t === 3 || t === 9
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPartsList({ page: currentPage.value, size: pageSize.value })
    partsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载配件列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = defaultForm()
  formRef.value?.clearValidate()
}

const handlePublish = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await publishParts({ ...form.value })
    ElMessage.success('配件上架成功')
    resetForm()
    currentPage.value = 1
    await fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      router.push('/login')
      return
    }
  }
  if (!canManage()) {
    ElMessage.error('需要商家或管理员权限')
    router.push('/parts')
    return
  }
  fetchList()
})
</script>

<style scoped>
.admin-page { max-width: 1000px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; }
.form-card { margin-bottom: 20px; }
.tip { font-size: 12px; color: #909399; margin-top: 4px; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }
</style>
