<template>
  <div class="post-detail">
    <el-card class="post-card" v-loading="loading">
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <el-tag :type="getCategoryType(post.category)">
          {{ getCategoryName(post.category) }}
        </el-tag>
      </div>

      <div class="post-meta">
        <span>{{ t('post.publishTime') }}：{{ formatTime(post.createTime) }}</span>
        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
          <span><el-icon><Pointer /></el-icon> {{ post.likeCount || 0 }}</span>
        </div>
      </div>

      <el-divider />

      <div class="post-content">{{ post.content }}</div>

      <div class="post-actions">
        <el-button
          :type="isLiked ? 'primary' : 'default'"
          @click="handleLike"
          :disabled="!userStore.token"
        >
          <el-icon><Pointer /></el-icon>
          {{ isLiked ? t('post.liked') : t('post.like') }} ({{ post.likeCount || 0 }})
        </el-button>

        <el-button
          :type="isCollected ? 'primary' : 'default'"
          @click="handleCollection"
          :disabled="!userStore.token"
        >
          <el-icon><Star /></el-icon>
          {{ isCollected ? t('post.collected') : t('post.collect') }}
        </el-button>
      </div>
    </el-card>

    <el-card class="comment-card">
      <div class="comment-title-row">
        <h3>{{ t('post.comments') }} ({{ flatComments.length }})</h3>
      </div>

      <div class="comment-input" v-if="userStore.token">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          :placeholder="t('post.writeComment')"
        />
        <el-button type="primary" @click="handleComment" :loading="commenting">
          {{ t('post.publishComment') }}
        </el-button>
      </div>
      <el-alert v-else type="info" :closable="false">
        <template #title>
          {{ t('post.loginToComment') }}
          <router-link to="/login">{{ t('common.login') }}</router-link>
        </template>
      </el-alert>

      <div class="comment-list">
        <CommentThread
          v-for="comment in commentTree"
          :key="comment.id"
          :comment="comment"
          :current-user-id="userStore.userInfo?.id"
          :token="userStore.token"
          :liked-ids="commentLikes"
          :active-reply-id="activeReplyId"
          :reply-content="replyContent"
          :reply-submitting="replySubmitting"
          @reply="openReply"
          @like="handleCommentLike"
          @delete="handleDeleteComment"
          @cancel-reply="cancelReply"
          @submit-reply="handleReply"
          @update-reply-content="replyContent = $event"
        />

        <el-empty v-if="commentTree.length === 0" :description="t('post.noComments')" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail } from '@/api/post'
import { getCommentList, createComment, deleteComment } from '@/api/comment'
import { likePost, checkPostLike, likeComment, checkCommentLike } from '@/api/like'
import { collectPost, checkCollection } from '@/api/collection'
import { useI18n } from '@/i18n'
import CommentThread from '@/components/CommentThread.vue'
import { ChatDotRound, Pointer, Star, View } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const { t, locale } = useI18n()

const loading = ref(false)
const commenting = ref(false)
const replySubmitting = ref(false)
const post = ref({})
const flatComments = ref([])
const commentContent = ref('')
const replyContent = ref('')
const activeReplyId = ref(null)
const isLiked = ref(false)
const isCollected = ref(false)
const commentLikes = ref(new Set())

const commentTree = computed(() => buildCommentTree(flatComments.value))

const buildCommentTree = (comments) => {
  const nodes = comments.map(item => ({ ...item, children: [] }))
  const map = new Map(nodes.map(item => [String(item.id), item]))
  const roots = []

  nodes.forEach(item => {
    const parentId = item.parentId == null ? '0' : String(item.parentId)
    const parent = parentId !== '0' ? map.get(parentId) : null
    if (parent) {
      parent.children.push(item)
    } else {
      roots.push(item)
    }
  })

  const sortReplies = (items, root = false) => {
    items.sort((a, b) => {
      const av = new Date(a.createTime || 0).getTime()
      const bv = new Date(b.createTime || 0).getTime()
      return root ? bv - av : av - bv
    })
    items.forEach(item => sortReplies(item.children))
  }
  sortReplies(roots, true)
  return roots
}

const findComment = (comments, id) => {
  for (const comment of comments) {
    if (String(comment.id) === String(id)) return comment
    const child = findComment(comment.children || [], id)
    if (child) return child
  }
  return null
}

const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res.data || {}
  } catch (error) {
    ElMessage.error(t('post.fetchPostFailed'))
  } finally {
    loading.value = false
  }
}

const fetchCommentList = async () => {
  try {
    const res = await getCommentList({ postId: route.params.id, page: 1, size: 100 })
    flatComments.value = res.data?.records || []
    await checkCommentLikes()
  } catch (error) {
    ElMessage.error(t('post.fetchCommentsFailed'))
  }
}

const checkLikeStatus = async () => {
  if (!userStore.token) return
  try {
    const res = await checkPostLike(route.params.id)
    isLiked.value = !!res.data
  } catch (error) {
    console.error(error)
  }
}

const checkCollectionStatus = async () => {
  if (!userStore.token) return
  try {
    const res = await checkCollection(route.params.id)
    isCollected.value = !!res.data
  } catch (error) {
    console.error(error)
  }
}

const checkCommentLikes = async () => {
  if (!userStore.token) return
  const next = new Set()
  for (const comment of flatComments.value) {
    try {
      const res = await checkCommentLike(comment.id)
      if (res.data) next.add(comment.id)
    } catch (error) {
      console.error(error)
    }
  }
  commentLikes.value = next
}

const handleLike = async () => {
  try {
    await likePost(route.params.id)
    isLiked.value = !isLiked.value
    post.value.likeCount = (post.value.likeCount || 0) + (isLiked.value ? 1 : -1)
    ElMessage.success(isLiked.value ? t('post.liked') : t('post.like'))
  } catch (error) {
    // handled by interceptor
  }
}

const handleCollection = async () => {
  try {
    await collectPost(route.params.id)
    isCollected.value = !isCollected.value
    ElMessage.success(isCollected.value ? t('post.collected') : t('post.collect'))
  } catch (error) {
    // handled by interceptor
  }
}

const handleComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning(t('post.commentRequired'))
    return
  }
  commenting.value = true
  try {
    await createComment({
      postId: route.params.id,
      content: commentContent.value.trim(),
      parentId: 0
    })
    ElMessage.success(t('post.commentSuccess'))
    commentContent.value = ''
    post.value.commentCount = (post.value.commentCount || 0) + 1
    await fetchCommentList()
  } catch (error) {
    // handled by interceptor
  } finally {
    commenting.value = false
  }
}

const openReply = (comment) => {
  activeReplyId.value = comment.id
  replyContent.value = ''
}

const cancelReply = () => {
  activeReplyId.value = null
  replyContent.value = ''
}

const handleReply = async (comment) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning(t('post.commentRequired'))
    return
  }
  replySubmitting.value = true
  try {
    await createComment({
      postId: route.params.id,
      content: replyContent.value.trim(),
      parentId: comment.id
    })
    ElMessage.success(t('post.replySuccess'))
    cancelReply()
    post.value.commentCount = (post.value.commentCount || 0) + 1
    await fetchCommentList()
  } catch (error) {
    // handled by interceptor
  } finally {
    replySubmitting.value = false
  }
}

const handleCommentLike = async (commentId) => {
  try {
    await likeComment(commentId)
    const comment = findComment(commentTree.value, commentId)
    const next = new Set(commentLikes.value)
    if (next.has(commentId)) {
      next.delete(commentId)
      if (comment) comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
    } else {
      next.add(commentId)
      if (comment) comment.likeCount = (comment.likeCount || 0) + 1
    }
    commentLikes.value = next
  } catch (error) {
    // handled by interceptor
  }
}

const handleDeleteComment = async (id) => {
  try {
    await ElMessageBox.confirm(t('post.deleteConfirm'), t('post.deleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    await deleteComment(id)
    ElMessage.success(t('post.deleteSuccess'))
    post.value.commentCount = Math.max(0, (post.value.commentCount || 0) - 1)
    await fetchCommentList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('post.deleteFailed'))
    }
  }
}

const getCategoryName = (category) => {
  const map = { 1: t('post.tech'), 2: t('post.trouble'), 3: t('post.experience'), 4: t('post.other') }
  return map[category] || t('common.unknown')
}

const getCategoryType = (category) => {
  const map = { 1: 'primary', 2: 'warning', 3: 'success', 4: 'info' }
  return map[category] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN', { hour12: false })
}

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  fetchPostDetail()
  fetchCommentList()
  checkLikeStatus()
  checkCollectionStatus()
})
</script>

<style scoped>
.post-detail {
  max-width: 900px;
  margin: 0 auto;
}

.post-card {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.post-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--efh-text);
  margin: 0;
  line-height: 1.35;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  color: var(--efh-text-muted);
  font-size: 14px;
  gap: 12px;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.post-content {
  font-size: 16px;
  line-height: 1.85;
  color: var(--efh-text-secondary);
  white-space: pre-wrap;
  margin: 20px 0;
}

.post-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.comment-card {
  margin-top: 20px;
}

.comment-title-row h3 {
  margin: 0;
  color: var(--efh-text);
}

.comment-input {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-input .el-button {
  align-self: flex-end;
}

.comment-list {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .post-title {
    font-size: 20px;
  }

  .post-meta {
    flex-direction: column;
    gap: 8px;
  }

  .post-actions {
    flex-wrap: wrap;
  }

  .post-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
}
</style>
