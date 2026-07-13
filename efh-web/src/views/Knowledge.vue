<template>
  <div class="knowledge-page">
    <div class="page-header">
      <div>
        <h2>技术知识库</h2>
        <p>拆分常规维修案例库与电池保养资料库，支持在线预览和权限解锁。</p>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push('/battery-care')">电池保养专区</el-button>
        <el-button v-if="isAdmin" type="primary" @click="$router.push('/knowledge/admin')">管理文档</el-button>
      </div>
    </div>

    <div class="knowledge-layout">
      <aside class="knowledge-menu">
        <button
          v-for="dir in menuItems"
          :key="dir.value"
          type="button"
          :class="{ active: activeDirectory === dir.value }"
          @click="selectDirectory(dir)"
        >
          <strong>{{ dir.label }}</strong>
          <span>{{ dir.children.join(' / ') }}</span>
        </button>
      </aside>

      <main>
        <el-card class="filter-card">
          <el-row :gutter="16" align="middle">
            <el-col :xs="24" :sm="10">
              <el-input v-model="keyword" placeholder="搜索文档标题、摘要..." clearable @keyup.enter="fetchList">
                <template #append>
                  <el-button @click="fetchList">搜索</el-button>
                </template>
              </el-input>
            </el-col>
            <el-col :xs="24" :sm="14">
              <el-radio-group v-model="category" @change="fetchList">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button v-for="c in activeCategories" :key="c" :label="c">{{ c }}</el-radio-button>
              </el-radio-group>
            </el-col>
          </el-row>
        </el-card>

        <el-row :gutter="20" v-loading="loading">
          <el-col :xs="24" :sm="12" :md="8" v-for="doc in docList" :key="doc.id">
            <el-card class="doc-card" shadow="hover" @click="goDetail(doc.id)">
              <div class="doc-icon">
                <el-icon :size="40"><Document /></el-icon>
                <el-tag size="small" class="file-type">{{ (doc.fileType || 'file').toUpperCase() }}</el-tag>
              </div>
              <h4>{{ doc.title }}</h4>
              <p class="summary">{{ doc.summary || '暂无摘要' }}</p>
              <div class="meta">
                <el-tag size="small" type="info">{{ doc.category }}</el-tag>
                <span>发布于 {{ formatDateTime(doc.createTime) }}</span>
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

        <EfhEmptyState v-if="!loading && docList.length === 0" title="暂无文档" description="当前目录下还没有可查看的知识文档。" />

        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="fetchList"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document } from '@element-plus/icons-vue'
import { getKnowledgeList, getKnowledgeCategories } from '@/api/knowledge'
import { knowledgeDirectories, formatDateTime } from '@/utils/format'
import EfhEmptyState from '@/components/EfhEmptyState.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const docList = ref([])
const categories = ref([])
const keyword = ref('')
const category = ref('')
const activeDirectory = ref('repair')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const isAdmin = computed(() => userStore.userInfo?.userType === 9)
const menuItems = knowledgeDirectories
const activeCategories = computed(() => {
  const dir = menuItems.find(item => item.value === activeDirectory.value)
  const preferred = dir?.children || []
  const known = categories.value.filter(item => preferred.includes(item))
  return known.length ? known : preferred
})

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

const selectDirectory = (dir) => {
  activeDirectory.value = dir.value
  category.value = ''
  currentPage.value = 1
  fetchList()
}

const goDetail = (id) => router.push(`/knowledge/${id}`)

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) await userStore.fetchUserInfo()
  try {
    const catRes = await getKnowledgeCategories()
    categories.value = catRes.data || []
  } catch (e) {
    categories.value = []
  }
  fetchList()
})
</script>

<style scoped>
.knowledge-page {
  padding-bottom: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.page-header p {
  margin: 4px 0 0;
  color: var(--efh-text-secondary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.knowledge-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
}

.knowledge-menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.knowledge-menu button {
  padding: 14px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.knowledge-menu button.active {
  border-color: var(--efh-primary);
  background: var(--efh-primary-soft);
}

.knowledge-menu strong,
.knowledge-menu span {
  display: block;
}

.knowledge-menu span {
  margin-top: 6px;
  color: var(--efh-text-muted);
  font-size: 12px;
  line-height: 1.5;
}

.filter-card {
  margin-bottom: 20px;
}

.doc-card {
  margin-bottom: 20px;
  cursor: pointer;
  min-height: 238px;
}

.doc-icon {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  color: var(--efh-primary);
}

.file-type {
  margin-left: auto;
}

.summary {
  color: var(--efh-text-secondary);
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta,
.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  font-size: 12px;
  color: var(--efh-text-muted);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 900px) {
  .knowledge-layout {
    grid-template-columns: 1fr;
  }

  .knowledge-menu {
    flex-direction: row;
    overflow-x: auto;
  }

  .knowledge-menu button {
    min-width: 220px;
  }
}
</style>
