<template>
  <div class="detail-page" v-loading="loading">
    <EfhBackBar title="知识库详情" fallback="/knowledge" />

    <el-card v-if="doc" class="detail-card">
      <div class="doc-header">
        <el-icon :size="48" color="#0f766e"><Document /></el-icon>
        <div>
          <h2>{{ doc.title }}</h2>
          <div class="tags">
            <el-tag>{{ doc.category }}</el-tag>
            <el-tag type="info">{{ doc.fileType?.toUpperCase() }}</el-tag>
            <el-tag type="info">{{ formatSize(doc.fileSize) }}</el-tag>
          </div>
        </div>
      </div>

      <p class="summary">{{ doc.summary || t('knowledge.noSummary') }}</p>

      <div class="stats">
        <span>{{ doc.viewCount || 0 }} {{ t('knowledge.views') }}</span>
        <span>{{ doc.downloadCount || 0 }} {{ t('knowledge.downloads') }}</span>
        <span>发布时间 {{ formatDate(doc.createTime) }}</span>
      </div>

      <el-divider />

      <div v-if="doc.unlocked" class="unlocked-panel">
        <el-result icon="success" :title="t('knowledge.unlockedTitle')" :sub-title="t('knowledge.unlockedSubTitle')">
          <template #extra>
            <div class="doc-actions">
              <el-button type="primary" :loading="previewing" @click="handlePreview">
                <el-icon><View /></el-icon>
                {{ t('common.preview') }}
              </el-button>
              <el-button :loading="downloading" @click="handleDownload">
                <el-icon><Download /></el-icon>
                {{ t('common.download') }}
              </el-button>
            </div>
          </template>
        </el-result>
      </div>

      <div v-else class="lock-panel">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>{{ t('knowledge.lockedTitle') }}</template>
          <p>{{ t('knowledge.unlockMethod', { label: doc.accessLabel }) }}</p>
          <p v-if="userPoints != null">{{ t('knowledge.currentPoints', { points: userPoints.availablePoints }) }}</p>
        </el-alert>

        <div class="unlock-actions">
          <el-button
            v-if="canPoints"
            type="primary"
            :loading="unlocking"
            @click="handlePointsUnlock"
          >
            {{ t('knowledge.pointsUnlock', { points: doc.pointsPrice }) }}
          </el-button>
          <el-button
            v-if="canPay"
            type="danger"
            :loading="paying"
            @click="handleAlipayUnlock"
          >
            {{ t('knowledge.alipayUnlock', { money: doc.moneyPrice }) }}
          </el-button>
          <el-button v-if="!userStore.token" @click="$router.push('/login')">{{ t('knowledge.loginToUnlock') }}</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="previewDialogVisible"
      :title="t('knowledge.previewTitle')"
      width="86%"
      class="preview-dialog"
      @closed="clearPreview"
    >
      <div class="preview-shell" v-loading="previewing">
        <iframe v-if="previewKind === 'pdf'" class="preview-frame" :src="previewUrl" />
        <img v-else-if="previewKind === 'image'" class="preview-image" :src="previewUrl" :alt="doc?.title" />
        <pre v-else-if="previewKind === 'text'" class="preview-text">{{ previewText }}</pre>
        <el-result v-else-if="previewKind === 'unsupported'" icon="info" :title="t('knowledge.unsupportedTitle')">
          <template #sub-title>
            <p>{{ t('knowledge.unsupportedDesc') }}</p>
          </template>
          <template #extra>
            <el-button type="primary" @click="handleDownload">{{ t('knowledge.openDownload') }}</el-button>
          </template>
        </el-result>
        <el-result v-else icon="warning" :title="t('knowledge.previewFailed')">
          <template #extra>
            <el-button type="primary" @click="handleDownload">{{ t('knowledge.openDownload') }}</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document, Download, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getKnowledgeDetail,
  unlockByPoints,
  unlockByAlipay,
  downloadKnowledgeDoc,
  previewKnowledgeDoc
} from '@/api/knowledge'
import { getUserPoints } from '@/api/points'
import { useI18n } from '@/i18n'
import EfhBackBar from '@/components/EfhBackBar.vue'

const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const loading = ref(false)
const unlocking = ref(false)
const paying = ref(false)
const downloading = ref(false)
const previewing = ref(false)
const doc = ref(null)
const userPoints = ref(null)
const previewDialogVisible = ref(false)
const previewKind = ref('')
const previewUrl = ref('')
const previewText = ref('')

const canPoints = computed(() => {
  if (!userStore.token || !doc.value) return false
  return doc.value.accessType === 1 || doc.value.accessType === 3
})
const canPay = computed(() => {
  if (!userStore.token || !doc.value) return false
  return doc.value.accessType === 2 || doc.value.accessType === 3
})

const fileType = computed(() => String(doc.value?.fileType || '').toLowerCase())
const isImage = computed(() => ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp'].includes(fileType.value))
const isText = computed(() => ['txt', 'md', 'csv', 'json', 'log', 'xml', 'yaml', 'yml'].includes(fileType.value))
const isPdf = computed(() => fileType.value === 'pdf')

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
  await ElMessageBox.confirm(
    t('knowledge.confirmPoints', { points: doc.value.pointsPrice }),
    t('knowledge.pointsTitle'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  )
  unlocking.value = true
  try {
    await unlockByPoints(doc.value.id)
    ElMessage.success(t('knowledge.unlockSuccess'))
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
    ElMessage.success(t('knowledge.downloadStart'))
  } catch (e) {
    ElMessage.error(t('knowledge.downloadFailed'))
  } finally {
    downloading.value = false
  }
}

const handlePreview = async () => {
  previewDialogVisible.value = true
  clearPreview()

  if (!isPdf.value && !isImage.value && !isText.value) {
    previewKind.value = 'unsupported'
    return
  }

  previewing.value = true
  try {
    const res = await previewKnowledgeDoc(doc.value.id)
    const type = res.headers?.['content-type'] || ''
    const blob = new Blob([res.data], { type })
    if (isText.value) {
      previewKind.value = 'text'
      previewText.value = await blob.text()
    } else {
      previewKind.value = isPdf.value ? 'pdf' : 'image'
      previewUrl.value = URL.createObjectURL(blob)
    }
  } catch (e) {
    previewKind.value = 'failed'
    ElMessage.error(t('knowledge.previewFailed'))
  } finally {
    previewing.value = false
  }
}

const clearPreview = () => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  previewText.value = ''
  previewKind.value = ''
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

const formatDate = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '-')

onMounted(async () => {
  if (userStore.token) {
    if (!userStore.userInfo) await userStore.fetchUserInfo()
    try {
      const res = await getUserPoints()
      userPoints.value = res.data
    } catch (e) {
      // optional data
    }
  }
  fetchDetail()
})

onBeforeUnmount(clearPreview)
</script>

<style scoped>
.detail-page {
  max-width: 860px;
  margin: 0 auto;
}

.detail-card {
  margin-top: 20px;
}

.doc-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.doc-header h2 {
  margin: 0 0 8px;
  color: var(--efh-text);
  line-height: 1.35;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.summary {
  color: var(--efh-text-secondary);
  line-height: 1.7;
  margin: 16px 0;
}

.stats {
  display: flex;
  gap: 20px;
  color: var(--efh-text-muted);
  font-size: 13px;
  flex-wrap: wrap;
}

.doc-actions,
.unlock-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.preview-shell {
  min-height: 420px;
}

.preview-frame {
  width: 100%;
  height: min(72vh, 760px);
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
}

.preview-image {
  display: block;
  max-width: 100%;
  max-height: 72vh;
  margin: 0 auto;
  object-fit: contain;
}

.preview-text {
  min-height: 420px;
  max-height: 72vh;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #101820;
  color: #e5edf0;
  line-height: 1.65;
}

@media (max-width: 768px) {
  .doc-header {
    gap: 12px;
  }

  .doc-actions,
  .unlock-actions {
    justify-content: stretch;
  }

  .doc-actions .el-button,
  .unlock-actions .el-button {
    flex: 1;
  }

  .preview-shell {
    min-height: 320px;
  }
}
</style>
