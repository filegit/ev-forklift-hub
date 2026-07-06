<template>
  <div class="knowledge-page">
    <div class="page-header">
      <h2>技术知识库</h2>
      <p>维修手册、技术规范、培训资料，支持积分或付费解锁</p>
      <el-button v-if="isAdmin" type="primary" @click="$router.push('/knowledge/admin')">
        管理文档
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="10">
          <el-input v-model="keyword" placeholder="搜索文档标题、摘要..." clearable @keyup.enter="fetchList">
            <template #append>
              <el-button @click="fetchList">搜索</el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="14">
          <el-radio-group v-model="category" @change="fetchList">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button v-for="c in categories" :key="c" :label="c">{{ c }}</el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="doc in docList" :key="doc.id">
        <el-card class="doc-card" shadow="hover" @click="goDetail(doc.id)">
          <div class="doc-icon">
            <el-icon :size="40"><Document /></el-icon>
            <el-tag size="small" class="file-type">{{ (doc.fileType || 'file').toUpperCase() }}</el-tag>
          </div>
          <h4>{{ doc.title }}</h4>
          <p class="summary">{{ doc.summary || '暂无摘要' }}</p>
          <div class="meta">
            <el-tag size="small" type="info">{{ doc.category }}</el-tag>
            <span>{{ doc.viewCount || 0 }} 浏览</span>
          </div>
          <div class="footer">
            <el-tag :type="doc.unlocked ? 'success' : 'warning'" size="small">
              {{ doc.unlocked ? '已解锁' : doc.accessLabel }}
            </el-tag>
            <el-button type="primary" link @click.stop="goDetail(doc.id)">查看详情</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && docList.length === 0" description="暂无文档" />

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document } from '@element-plus/icons-vue'
import { getKnowledgeList, getKnowledgeCategories } from '@/api/knowledge'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const docList = ref([])
const categories = ref([])
const keyword = ref('')
const category = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const isAdmin = computed(() => userStore.userInfo?.userType === 9)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      category: category.value || undefined
    })
    docList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/knowledge/${id}`)

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  try {
    const catRes = await getKnowledgeCategories()
    categories.value = catRes.data || []
  } catch (e) {
    categories.value = ['维修手册', '技术规范', '培训资料', '安全指南', '其他']
  }
  fetchList()
})
</script>

<style scoped>
.knowledge-page { padding-bottom: 24px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap;
  gap: 12px; margin-bottom: 20px;
}
.page-header h2 { margin: 0; }
.page-header p { margin: 4px 0 0; color: #909399; flex: 1 1 100%; }
.filter-card { margin-bottom: 20px; }
.doc-card { margin-bottom: 20px; cursor: pointer; min-height: 220px; }
.doc-icon { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; color: #409eff; }
.file-type { margin-left: auto; }
.summary {
  color: #606266; font-size: 13px; height: 40px; overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.meta { display: flex; justify-content: space-between; align-items: center; margin: 10px 0; font-size: 12px; color: #909399; }
.footer { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
