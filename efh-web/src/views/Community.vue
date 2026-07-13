<template>
  <div class="community-page efh-page">
    <section class="community-hero efh-section">
      <div>
        <span class="efh-kicker">OPERATIONS COMMUNITY</span>
        <h1>运营社区</h1>
        <p>沉淀保养经验、故障维修案例和备件选型讨论，把现场问题转成可复用知识。</p>
      </div>
      <el-button type="primary" @click="openPublish">
        <el-icon><Edit /></el-icon>
        发帖
      </el-button>
    </section>

    <section v-if="topPosts.length" class="pinned-section efh-section">
      <div class="section-title">
        <div>
          <span class="efh-kicker">PINNED</span>
          <h2>置顶帖子</h2>
        </div>
      </div>
      <div class="pinned-grid">
        <article v-for="post in topPosts" :key="post.id" class="pinned-card" @click="goToDetail(post.id)">
          <div class="card-top">
            <el-tag type="warning" effect="dark">置顶</el-tag>
            <el-tag :type="getCategory(post.category).type" effect="light">{{ getCategory(post.category).label }}</el-tag>
          </div>
          <h3>{{ post.title }}</h3>
          <p>{{ post.content }}</p>
          <div class="card-meta">
            <span>发布于 {{ formatDateTime(post.createTime) }}</span>
            <el-button v-if="userStore.token" size="small" plain @click.stop="toggleTop(post, false)">取消置顶</el-button>
          </div>
        </article>
      </div>
    </section>

    <section class="topic-card efh-section" @click="goTopic">
      <div>
        <el-tag type="success">专题互动</el-tag>
        <h2>如何选择合适的电动叉车</h2>
        <p>从载荷、工况、续航、电池类型、备件成本和售后响应六个维度建立选型判断。</p>
        <span>支持上传工况图片 / 文字留言互动</span>
      </div>
      <el-button type="primary">进入专题</el-button>
    </section>

    <el-card class="filter-card efh-section">
      <div class="filter-title">分类筛选</div>
      <el-radio-group v-model="currentFilter" class="category-radios" @change="handleFilterChange">
        <el-radio-button label="technical">技术交流</el-radio-button>
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="2">故障求助</el-radio-button>
        <el-radio-button label="3">经验分享</el-radio-button>
        <el-radio-button label="4">其他</el-radio-button>
      </el-radio-group>
      <el-radio-group
        v-if="currentFilter === 'technical'"
        v-model="technicalCategory"
        class="category-radios technical-sub"
        @change="handleFilterChange"
      >
        <el-radio-button label="technical">全部技术交流</el-radio-button>
        <el-radio-button label="5">保养经验</el-radio-button>
        <el-radio-button label="6">故障维修案例</el-radio-button>
        <el-radio-button label="7">备件选型</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card class="post-list efh-section" v-loading="loading">
      <div class="efh-post-item" v-for="post in postList" :key="post.id" @click="goToDetail(post.id)">
        <div class="post-header">
          <div>
            <div class="post-title-row">
              <el-tag v-if="post.isTop === 1" type="warning" size="small">置顶</el-tag>
              <h3 class="efh-post-title">{{ post.title }}</h3>
            </div>
            <el-tag :type="getCategory(post.category).type" size="small" effect="light">
              {{ getCategory(post.category).label }}
            </el-tag>
          </div>
          <el-button v-if="userStore.token" size="small" plain @click.stop="toggleTop(post, post.isTop !== 1)">
            {{ post.isTop === 1 ? '取消置顶' : '设为置顶' }}
          </el-button>
        </div>
        <div class="efh-post-content">{{ post.content }}</div>
        <div class="efh-post-meta">
          <div class="efh-post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
          </div>
          <div class="post-time">发布于 {{ formatDateTime(post.createTime) }}</div>
        </div>
      </div>

      <EfhEmptyState v-if="!loading && postList.length === 0" title="暂无帖子" description="当前分类还没有内容。" />
    </el-card>

    <div class="efh-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchPostList"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Edit, Star, View } from '@element-plus/icons-vue'
import { getPostList, setPostTop } from '@/api/post'
import { useUserStore } from '@/stores/user'
import { formatDateTime, getPostCategory } from '@/utils/format'
import EfhEmptyState from '@/components/EfhEmptyState.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentFilter = ref('technical')
const technicalCategory = ref('technical')
const topicPostId = ref(null)

const getCategory = getPostCategory
const topPosts = computed(() => postList.value.filter(post => post.isTop === 1).slice(0, 3))

const buildParams = () => {
  const params = { page: currentPage.value, size: pageSize.value }
  if (currentFilter.value === 'technical') {
    if (technicalCategory.value === 'technical') params.categoryGroup = 'technical'
    else params.category = Number(technicalCategory.value)
  } else if (currentFilter.value !== 'all') {
    params.category = Number(currentFilter.value)
  }
  return params
}

const fetchPostList = async () => {
  loading.value = true
  try {
    const res = await getPostList(buildParams())
    postList.value = res.data.records || []
    total.value = res.data.total || 0
    const topic = postList.value.find(post => post.title?.includes('如何选择合适的电动叉车'))
    if (topic) topicPostId.value = topic.id
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  if (currentFilter.value !== 'technical') technicalCategory.value = 'technical'
  currentPage.value = 1
  fetchPostList()
}

const toggleTop = async (post, top) => {
  try {
    await setPostTop(post.id, top)
    ElMessage.success(top ? '已设为置顶' : '已取消置顶')
    fetchPostList()
  } catch (error) {
    console.error(error)
  }
}

const goToDetail = (id) => router.push(`/post/${id}`)
const openPublish = () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  window.dispatchEvent(new Event('efh-open-post-dialog'))
}

const goTopic = () => {
  if (topicPostId.value) {
    router.push(`/post/${topicPostId.value}`)
    return
  }
  router.push('/community')
}

onMounted(fetchPostList)
</script>

<style scoped>
.community-hero,
.topic-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid var(--efh-border-light);
  background: #fff;
}

.community-hero h1,
.topic-card h2,
.section-title h2 {
  margin: 0;
  line-height: 1.2;
}

.community-hero p,
.topic-card p {
  max-width: 680px;
  margin: 10px 0 0;
  color: var(--efh-text-secondary);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pinned-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.pinned-card {
  padding: 16px;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  background: #fff7ed;
  cursor: pointer;
}

.card-top,
.card-meta,
.post-header,
.post-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-top,
.post-header,
.card-meta {
  justify-content: space-between;
}

.pinned-card h3 {
  margin: 12px 0 8px;
  font-size: 17px;
}

.pinned-card p {
  margin: 0 0 12px;
  color: var(--efh-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  color: var(--efh-text-muted);
  font-size: 12px;
}

.topic-card {
  cursor: pointer;
  background: #f0fdfa;
  border-color: #99f6e4;
}

.topic-card span {
  display: inline-block;
  margin-top: 8px;
  color: var(--efh-text-muted);
  font-size: 13px;
}

.filter-card :deep(.el-card__body) {
  display: grid;
  gap: 10px;
  padding: 14px 16px;
}

.filter-title {
  font-weight: 600;
}

.category-radios {
  flex-wrap: wrap;
  gap: 8px;
}

.technical-sub {
  padding: 10px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #f8fafc;
}

.post-list :deep(.el-card__body) {
  padding: 0;
}

.post-header {
  align-items: flex-start;
  margin-bottom: 4px;
}

.post-title-row {
  margin-bottom: 6px;
}

.post-time {
  font-size: 12px;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .pinned-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .community-hero,
  .topic-card,
  .post-header,
  .efh-post-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
