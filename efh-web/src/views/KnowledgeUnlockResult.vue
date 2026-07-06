<template>
  <div class="result-page" v-loading="loading">
    <el-card class="result-card">
      <el-result
        :icon="unlocked ? 'success' : 'info'"
        :title="unlocked ? '解锁成功' : '支付处理中'"
        :sub-title="unlocked ? '文档已解锁，可以下载了' : '如已完成支付，请稍候刷新'"
      >
        <template #extra>
          <el-button type="primary" @click="goDetail">查看文档</el-button>
          <el-button @click="$router.push('/knowledge')">返回知识库</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getKnowledgeDetail } from '@/api/knowledge'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const unlocked = ref(false)
const docId = ref(route.query.docId)

const checkStatus = async () => {
  if (!docId.value) {
    loading.value = false
    return
  }
  try {
    const res = await getKnowledgeDetail(docId.value)
    unlocked.value = !!res.data?.unlocked
  } finally {
    loading.value = false
  }
}

const goDetail = () => {
  if (docId.value) router.push(`/knowledge/${docId.value}`)
  else router.push('/knowledge')
}

onMounted(async () => {
  await checkStatus()
  if (!unlocked.value) {
    setTimeout(checkStatus, 2000)
    setTimeout(checkStatus, 5000)
  }
})
</script>

<style scoped>
.result-page { max-width: 600px; margin: 40px auto; }
.result-card { text-align: center; }
</style>
