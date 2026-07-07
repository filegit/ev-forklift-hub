<template>
  <div class="comment-node" :class="{ nested: level > 0 }">
    <div class="comment-main">
      <el-avatar :size="level > 0 ? 30 : 36" class="comment-avatar">
        {{ avatarText }}
      </el-avatar>
      <div class="comment-body">
        <div class="comment-topline">
          <div>
            <div class="comment-user">{{ userName }}</div>
            <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
          </div>
          <div class="comment-actions">
            <el-button
              v-if="token"
              text
              size="small"
              :type="liked ? 'primary' : 'default'"
              @click="$emit('like', comment.id)"
            >
              <el-icon><Pointer /></el-icon>
              {{ comment.likeCount || 0 }}
            </el-button>
            <el-button v-if="token" text size="small" @click="$emit('reply', comment)">
              {{ t('common.reply') }}
            </el-button>
            <el-button
              v-if="canDelete"
              text
              size="small"
              type="danger"
              @click="$emit('delete', comment.id)"
            >
              {{ t('common.delete') }}
            </el-button>
          </div>
        </div>

        <div class="comment-content">{{ comment.content }}</div>

        <div v-if="replying" class="reply-editor">
          <el-input
            :model-value="replyContent"
            type="textarea"
            :rows="2"
            :placeholder="t('post.replyPlaceholder', { name: userName })"
            @update:model-value="$emit('update-reply-content', $event)"
          />
          <div class="reply-editor-actions">
            <el-button size="small" @click="$emit('cancel-reply')">{{ t('common.cancel') }}</el-button>
            <el-button size="small" type="primary" :loading="replySubmitting" @click="$emit('submit-reply', comment)">
              {{ t('common.submit') }}
            </el-button>
          </div>
        </div>

        <div v-if="comment.children?.length" class="reply-list">
          <CommentThread
            v-for="child in comment.children"
            :key="child.id"
            :comment="child"
            :level="level + 1"
            :current-user-id="currentUserId"
            :token="token"
            :liked-ids="likedIds"
            :active-reply-id="activeReplyId"
            :reply-content="replyContent"
            :reply-submitting="replySubmitting"
            @reply="$emit('reply', $event)"
            @like="$emit('like', $event)"
            @delete="$emit('delete', $event)"
            @cancel-reply="$emit('cancel-reply')"
            @submit-reply="$emit('submit-reply', $event)"
            @update-reply-content="$emit('update-reply-content', $event)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/i18n'
import { Pointer } from '@element-plus/icons-vue'

defineOptions({ name: 'CommentThread' })

const props = defineProps({
  comment: { type: Object, required: true },
  level: { type: Number, default: 0 },
  currentUserId: { type: [Number, String], default: null },
  token: { type: String, default: '' },
  likedIds: { type: Object, required: true },
  activeReplyId: { type: [Number, String], default: null },
  replyContent: { type: String, default: '' },
  replySubmitting: { type: Boolean, default: false }
})

defineEmits(['reply', 'like', 'delete', 'cancel-reply', 'submit-reply', 'update-reply-content'])

const { t, locale } = useI18n()

const userName = computed(() => props.comment.nickname || props.comment.username || `用户${props.comment.userId}`)
const avatarText = computed(() => String(userName.value || '?').slice(0, 1).toUpperCase())
const canDelete = computed(() => props.currentUserId != null && String(props.currentUserId) === String(props.comment.userId))
const liked = computed(() => props.likedIds.has(props.comment.id))
const replying = computed(() => String(props.activeReplyId) === String(props.comment.id))

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN', { hour12: false })
}
</script>

<style scoped>
.comment-node {
  border-bottom: 1px solid var(--efh-border-light);
  padding: 16px 0;
}

.comment-node.nested {
  border-bottom: none;
  padding: 12px 0 0;
}

.comment-main {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.comment-avatar {
  flex-shrink: 0;
  background: var(--efh-primary);
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-topline {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.comment-user {
  color: var(--efh-text);
  font-weight: 600;
  font-size: 14px;
}

.comment-time {
  color: var(--efh-text-muted);
  font-size: 12px;
  margin-top: 2px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.comment-content {
  margin-top: 8px;
  color: var(--efh-text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.reply-list {
  margin-top: 10px;
  padding: 2px 14px 2px 16px;
  border-left: 2px solid var(--efh-border-light);
  background: #f8faf9;
  border-radius: 0 8px 8px 0;
}

.reply-editor {
  margin-top: 12px;
  padding: 12px;
  background: #f8faf9;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
}

.reply-editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .comment-topline {
    flex-direction: column;
  }

  .comment-actions {
    justify-content: flex-start;
  }
}
</style>
