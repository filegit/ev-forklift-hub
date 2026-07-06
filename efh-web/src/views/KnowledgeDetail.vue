<template>
  <div class="detail-page" v-loading="loading">
    <el-page-header @back="$router.push('/knowledge')" content="文档详情" />

    <el-card v-if="doc" class="detail-card">
      <div class="doc-header">
        <el-icon :size="48" color="#409eff"><Document /></el-icon>
        <div>
          <h2>{{ doc.title }}</h2>
          <div class="tags">
            <el-tag>{{ doc.category }}</el-tag>
            <el-tag type="info">{{ doc.fileType?.toUpperCase() }}</el-tag>
            <el-tag type="info">{{ formatSize(doc.fileSize) }}</el-tag>
          </div>
        </div>
      </div>

      <p class="summary">{{ doc.summary || '暂无摘要' }}</p>

      <div class="stats">
        <span>{{ doc.viewCount || 0 }} 浏览</span>
        <span>{{ doc.downloadCount || 0 }} 下载</span>
        <span>上传于 {{ formatDate(doc.createTime) }}</span>
      </div>

      <el-divider />

      <div v-if="doc.unlocked" class="unlocked-panel">
        <el-result icon="success" title="文档已解锁" sub-title="您可以下载完整文档">
          <template #extra>
            <el-button type="primary" :loading="downloading" @click="handleDownload">
              下载文档
            </el-button>
          </template>
        </el-result>
      </div>

      <div v-else class="lock-panel">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>该文档需要解锁后才能下载</template>
          <p>解锁方式：{{ doc.accessLabel }}</p>
          <p v-if="userPoints != null">当前可用积分：{{ userPoints.availablePoints }}</p>
        </el-alert>

        <div class="unlock-actions">
          <el-button
            v-if="canPoints"
            type="primary"
            :loading="unlocking"
            @click="handlePointsUnlock"
          >
            积分解锁（{{ doc.pointsPrice }} 积分）
          </el-button>
          <el-button
            v-if="canPay"
            type="danger"
            :loading="paying"
            @click="handleAlipayUnlock"
          >
            支付宝解锁（¥{{ doc.moneyPrice }}）
          </el-button>
          <el-button v-if="!userStore.token" @click="$router.push('/login')">登录后解锁</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getKnowledgeDetail,
  unlockByPoints,
  unlockByAlipay,
  downloadKnowledgeDoc
} from '@/api/knowledge'
import { getUserPoints } from '@/api/points'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const unlocking = ref(false)
const paying = ref(false)
const downloading = ref(false)
const doc = ref(null)
const userPoints = ref(null)

const canPoints = computed(() => {
  if (!userStore.token || !doc.value) return false
  return doc.value.accessType === 1 || doc.value.accessType === 3
})
const canPay = computed(() => {
  if (!userStore.token || !doc.value) return false
  return doc.value.accessType === 2 || doc.value.accessType === 3
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeDetail(route.params.id)
    doc.value = res.data
  } finally {
    loading.value = false
  }
}

const handlePointsUnlock = async () => {
  await ElMessageBox.confirm(`确认使用 ${doc.value.pointsPrice} 积分解锁该文档？`, '积分解锁')
  unlocking.value = true
  try {
    await unlockByPoints(doc.value.id)
    ElMessage.success('解锁成功')
    await fetchDetail()
    if (userStore.token) await userStore.fetchUserInfo()
  } finally {
    unlocking.value = false
  }
}

const submitAlipayForm = (payForm) => {
  const wrapper = document.createElement('div')
  wrapper.style.display = 'none'
  wrapper.innerHTML = payForm
  document.body.appendChild(wrapper)
  wrapper.querySelector('form')?.submit()
}

const handleAlipayUnlock = async () => {
  paying.value = true
  try {
    const res = await unlockByAlipay(doc.value.id)
    if (res.data?.payForm) {
      submitAlipayForm(res.data.payForm)
    }
  } finally {
    paying.value = false
  }
}

const handleDownload = async () => {
  downloading.value = true
  try {
    await downloadKnowledgeDoc(doc.value.id, doc.value.fileName)
    ElMessage.success('开始下载')
  } catch (e) {
    ElMessage.error('下载失败')
  } finally {
    downloading.value = false
  }
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

const formatDate = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

onMounted(async () => {
  if (userStore.token) {
    if (!userStore.userInfo) await userStore.fetchUserInfo()
    try {
      const res = await getUserPoints()
      userPoints.value = res.data
    } catch (e) { /* ignore */ }
  }
  fetchDetail()
})
</script>

<style scoped>
.detail-page { max-width: 800px; margin: 0 auto; }
.detail-card { margin-top: 20px; }
.doc-header { display: flex; gap: 16px; align-items: flex-start; }
.doc-header h2 { margin: 0 0 8px; }
.tags { display: flex; gap: 8px; flex-wrap: wrap; }
.summary { color: #606266; line-height: 1.6; margin: 16px 0; }
.stats { display: flex; gap: 20px; color: #909399; font-size: 13px; }
.unlock-actions { margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap; }
</style>
