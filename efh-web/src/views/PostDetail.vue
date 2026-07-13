<template>
  <div class="post-detail">
    <EfhBackBar title="帖子详情" fallback="/community" />

    <el-card class="post-card" v-loading="loading">
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <el-tag :type="getCategoryType(post.category)">{{ getCategoryName(post.category) }}</el-tag>
      </div>

      <div class="post-meta">
        <span>发布时间：{{ formatTime(post.createTime) }}</span>
        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
          <span><el-icon><Pointer /></el-icon> {{ post.likeCount || 0 }}</span>
        </div>
      </div>

      <el-divider />
      <div class="post-content">{{ post.content }}</div>

      <div class="post-actions">
        <el-button :type="isLiked ? 'primary' : 'default'" :disabled="!userStore.token" @click="handleLike">
          <el-icon><Pointer /></el-icon>
          {{ isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount || 0 }})
        </el-button>
        <el-button :type="isCollected ? 'primary' : 'default'" :disabled="!userStore.token" @click="handleCollection">
          <el-icon><Star /></el-icon>
          {{ isCollected ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </el-card>

    <el-card class="comment-card">
      <div class="comment-title-row">
        <h3>评论 ({{ flatComments.length }})</h3>
      </div>

      <div class="comment-input" v-if="userStore.token">
        <ImageUploadPreview button-text="上传工况图片预览" @change="commentImages = $event" />
        <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的工况、选型或维修经验" />
        <el-button type="primary" :loading="commenting" @click="handleComment">发表评论</el-button>
      </div>
      <el-alert v-else type="info" :closable="false">
        <template #title>
          登录后可以参与评论，
          <router-link to="/login">去登录</router-link>
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
        <EfhEmptyState v-if="commentTree.length === 0" title="暂无评论" description="可以上传工况图片并补充文字说明。" />
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
import CommentThread from '@/components/CommentThread.vue'
import EfhBackBar from '@/components/EfhBackBar.vue'
import EfhEmptyState from '@/components/EfhEmptyState.vue'
import ImageUploadPreview from '@/components/ImageUploadPreview.vue'
import { ChatDotRound, Pointer, Star, View } from '@element-plus/icons-vue'
import { formatDateTime, getPostCategory } from '@/utils/format'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const commenting = ref(false)
const replySubmitting = ref(false)
const post = ref({})
const flatComments = ref([])
const commentContent = ref('')
const commentImages = ref([])
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
    if (parent) parent.children.push(item)
    else roots.push(item)
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
    ElMessage.error('获取帖子详情失败')
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
    ElMessage.error('获取评论失败')
  }
}

const checkLikeStatus = async () => {
  if (!userStore.token) return
  const res = await checkPostLike(route.params.id)
  isLiked.value = !!res.data
}

const checkCollectionStatus = async () => {
  if (!userStore.token) return
  const res = await checkCollection(route.params.id)
  isCollected.value = !!res.data
}

const checkCommentLikes = async () => {
  if (!userStore.token) return
  const next = new Set()
  for (const comment of flatComments.value) {
    try {
      const res = await checkCommentLike(comment.id)
      if (res.data) next.add(comment.id)
    } catch (error) {
      // optional
    }
  }
  commentLikes.value = next
}

const handleLike = async () => {
  await likePost(route.params.id)
  isLiked.value = !isLiked.value
  post.value.likeCount = Math.max(0, (post.value.likeCount || 0) + (isLiked.value ? 1 : -1))
}

const handleCollection = async () => {
  await collectPost(route.params.id)
  isCollected.value = !isCollected.value
}

const buildCommentContent = (content) => {
  if (!commentImages.value.length) return content
  return `${content}\n\n工况图片：\n${commentImages.value.map(item => item.name).join('\n')}`
}

const handleComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commenting.value = true
  try {
    await createComment({ postId: route.params.id, content: buildCommentContent(commentContent.value.trim()), parentId: 0 })
    commentContent.value = ''
    commentImages.value = []
    post.value.commentCount = (post.value.commentCount || 0) + 1
    await fetchCommentList()
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
    ElMessage.warning('请输入回复内容')
    return
  }
  replySubmitting.value = true
  try {
    await createComment({ postId: route.params.id, content: replyContent.value.trim(), parentId: comment.id })
    cancelReply()
    post.value.commentCount = (post.value.commentCount || 0) + 1
    await fetchCommentList()
  } finally {
    replySubmitting.value = false
  }
}

const handleCommentLike = async (commentId) => {
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
}

const handleDeleteComment = async (id) => {
  await ElMessageBox.confirm('确定删除该评论吗？', '删除评论', { type: 'warning' })
  await deleteComment(id)
  post.value.commentCount = Math.max(0, (post.value.commentCount || 0) - 1)
  await fetchCommentList()
}

const getCategoryName = (category) => getPostCategory(category).label
const getCategoryType = (category) => getPostCategory(category).type
const formatTime = (time) => formatDateTime(time)

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) await userStore.fetchUserInfo()
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

@media (max-width: 768px) {
  .post-header,
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-title {
    font-size: 20px;
  }

  .post-actions {
    flex-wrap: wrap;
  }
}
</style>
