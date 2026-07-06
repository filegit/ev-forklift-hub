<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>知识库管理</h2>
      <el-button @click="$router.push('/knowledge')">返回知识库</el-button>
    </div>

    <el-card class="upload-card">
      <template #header>上传新文档</template>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标题" required>
              <el-input v-model="form.title" placeholder="文档标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" placeholder="选择分类">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简要描述文档内容" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="访问方式" required>
              <el-select v-model="form.accessType">
                <el-option label="免费" :value="0" />
                <el-option label="积分解锁" :value="1" />
                <el-option label="付费解锁" :value="2" />
                <el-option label="积分或付费" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.accessType === 1 || form.accessType === 3">
            <el-form-item label="积分价格">
              <el-input-number v-model="form.pointsPrice" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.accessType === 2 || form.accessType === 3">
            <el-form-item label="现金价格">
              <el-input-number v-model="form.moneyPrice" :min="0.01" :precision="2" :step="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="文档文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="() => (file = null)"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="tip">支持 PDF、Word、Excel 等，最大 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="handleUpload">上传（草稿）</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="list-header">
          <span>文档列表</span>
          <el-radio-group v-model="statusFilter" size="small" @change="fetchList">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="0">草稿</el-radio-button>
            <el-radio-button :label="1">已发布</el-radio-button>
            <el-radio-button :label="2">已下架</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="docList" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="访问" width="120">
          <template #default="{ row }">{{ row.accessLabel }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column prop="downloadCount" label="下载" width="70" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 1" link type="primary" @click="handlePublish(row.id)">发布</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="handleOffline(row.id)">下架</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getKnowledgeCategories,
  adminGetDocList,
  adminUploadDoc,
  adminPublishDoc,
  adminOfflineDoc,
  adminDeleteDoc
} from '@/api/knowledge'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const uploading = ref(false)
const docList = ref([])
const categories = ref([])
const statusFilter = ref(null)
const file = ref(null)

const form = ref({
  title: '',
  summary: '',
  category: '维修手册',
  accessType: 1,
  pointsPrice: 100,
  moneyPrice: 9.9
})

const statusText = (s) => ({ 0: '草稿', 1: '已发布', 2: '已下架' }[s] || '未知')
const statusType = (s) => ({ 0: 'info', 1: 'success', 2: 'warning' }[s] || 'info')

const handleFileChange = (uploadFile) => {
  file.value = uploadFile.raw
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await adminGetDocList({
      page: 1,
      size: 50,
      status: statusFilter.value ?? undefined
    })
    docList.value = res.data?.records || []
  } catch (e) {
    if (e?.message?.includes('管理员')) {
      ElMessage.error('需要管理员权限')
      router.replace('/knowledge')
    }
  } finally {
    loading.value = false
  }
}

const handleUpload = async () => {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!file.value) {
    ElMessage.warning('请选择文件')
    return
  }
  const fd = new FormData()
  fd.append('file', file.value)
  const metaBlob = new Blob([JSON.stringify(form.value)], { type: 'application/json' })
  fd.append('meta', metaBlob, 'meta.json')
  uploading.value = true
  try {
    await adminUploadDoc(fd)
    ElMessage.success('上传成功，请在列表中发布')
    form.value.title = ''
    form.value.summary = ''
    file.value = null
    fetchList()
  } finally {
    uploading.value = false
  }
}

const handlePublish = async (id) => {
  await adminPublishDoc(id)
  ElMessage.success('已发布')
  fetchList()
}

const handleOffline = async (id) => {
  await adminOfflineDoc(id)
  ElMessage.success('已下架')
  fetchList()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该文档？', '提示', { type: 'warning' })
  await adminDeleteDoc(id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(async () => {
  if (!userStore.token) {
    router.replace('/login')
    return
  }
  if (!userStore.userInfo) await userStore.fetchUserInfo()
  if (userStore.userInfo?.userType !== 9) {
    ElMessage.error('需要管理员权限')
    router.replace('/knowledge')
    return
  }
  const catRes = await getKnowledgeCategories()
  categories.value = catRes.data || []
  fetchList()
})
</script>

<style scoped>
.admin-page { padding-bottom: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.upload-card { margin-bottom: 20px; }
.list-header { display: flex; justify-content: space-between; align-items: center; }
.tip { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
