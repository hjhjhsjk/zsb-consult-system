<template>
  <div class="bbs-detail">
    <div class="container">
      <el-button text :icon="ArrowLeft" @click="goBack" class="back-btn">返回论坛</el-button>

      <el-skeleton v-if="loading" :rows="6" animated />

      <template v-else-if="post">
        <!-- 帖子主体 -->
        <article class="post-card">
          <header class="post-header">
            <el-avatar :size="40" :src="fixImg(post.avatar)">
              {{ (post.nickname || 'U').charAt(0) }}
            </el-avatar>
            <div class="post-user">
              <span class="nickname">{{ post.nickname || '匿名用户' }}</span>
              <span class="time">{{ timeAgo(post.post_time) }}</span>
            </div>
          </header>

          <p class="post-content">{{ post.content }}</p>

          <div v-if="post.image_url" class="post-image">
            <el-image
              :src="fixImg(post.image_url)"
              :preview-src-list="[fixImg(post.image_url)]"
              fit="cover"
              hide-on-click-modal
            />
          </div>

          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.view_count }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.post_count || comments.length }}</span>
          </div>

          <div class="post-actions">
            <el-button
              :type="post.is_liked ? 'primary' : 'default'"
              :icon="Star"
              @click="toggleLike"
            >
              {{ post.is_liked ? '已赞' : '点赞' }} {{ post.like_count || 0 }}
            </el-button>
            <el-button
              :type="post.is_fav ? 'warning' : 'default'"
              :icon="Collection"
              @click="toggleFav"
            >
              {{ post.is_fav ? '已收藏' : '收藏' }} {{ post.fav_count || 0 }}
            </el-button>
            <el-button :icon="Share" @click="copyLink">复制链接</el-button>
          </div>
        </article>

        <!-- 评论区 -->
        <section class="comments-card" ref="commentsRef">
          <h3 class="comments-title">评论 ({{ comments.length }})</h3>

          <div class="comment-editor">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              placeholder="友善发言，Ctrl + Enter 快速发送"
              @keydown.ctrl.enter.prevent="submitComment"
            />
            <div class="editor-actions">
              <el-button type="primary" :loading="commenting" @click="submitComment">
                发表评论
              </el-button>
            </div>
          </div>

          <el-empty v-if="!comments.length" description="还没有评论，来说第一句吧" />

          <div v-else class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <el-avatar :size="32" :src="fixImg(c.avatar)">
                {{ (c.nickname || 'U').charAt(0) }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-head">
                  <span class="nickname">{{ c.nickname || '匿名用户' }}</span>
                  <span class="time">{{ timeAgo(c.create_time) }}</span>
                </div>
                <p class="comment-content">{{ c.content }}</p>
                <div class="comment-actions">
                  <span
                    class="like-btn"
                    :class="{ active: c.is_liked }"
                    @click="toggleCommentLike(c)"
                  >
                    <el-icon><Pointer /></el-icon> {{ c.like_count || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>

      <el-empty v-else description="帖子不存在或已删除">
        <el-button type="primary" @click="$router.push('/bbs')">返回论坛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, View, ChatDotRound, Star, Collection, Pointer, Share
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getPostDetail, getComments, createComment,
  likePost, unlikePost,
  likeComment, unlikeComment,
  addFavorite, clearFavorite
} from '@/api/bbs'
import { fixImg } from '@/utils/image'
import { addHistory } from '@/utils/history'
import { timeAgo } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const postId = Number(route.params.id)
const uid = userStore.uid

// 游客可以看，但点赞/收藏/评论必须登录
const requireLogin = () => {
  if (userStore.uid) return true
  ElMessage.warning('登录后才能操作')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

const post = ref(null)
const comments = ref([])
const loading = ref(false)
const commentText = ref('')
const commenting = ref(false)
const commentsRef = ref()

const loadPost = async () => {
  const res = await getPostDetail(uid || 0, postId)
  if (res.code === 200) {
    post.value = res.data
    addHistory({
      type: '帖子',
      title: (res.data.content || '').slice(0, 30) || `帖子 #${postId}`,
      path: `/bbs/${postId}`
    })
  }
}

const loadComments = async () => {
  const res = await getComments(uid || 0, postId)
  if (res.code === 200) comments.value = res.data || []
}

// 点赞：先改 UI（秒反馈），失败再回滚
const toggleLike = async () => {
  if (!requireLogin()) return
  const wasLiked = post.value.is_liked
  post.value.is_liked = wasLiked ? 0 : 1
  post.value.like_count = Math.max(0, (post.value.like_count || 0) + (wasLiked ? -1 : 1))

  try {
    const res = wasLiked ? await unlikePost(userStore.uid, postId) : await likePost(userStore.uid, postId)
    if (res.code !== 200) throw new Error(res.msg)
  } catch (e) {
    post.value.is_liked = wasLiked
    post.value.like_count = Math.max(0, (post.value.like_count || 0) + (wasLiked ? 1 : -1))
    ElMessage.error('操作失败')
  }
}

const toggleFav = async () => {
  if (!requireLogin()) return
  try {
    const res = post.value.is_fav
      ? await clearFavorite(userStore.uid, 1, postId)
      : await addFavorite(userStore.uid, 1, postId)

    if (res.code !== 200) throw new Error(res.msg)

    post.value.is_fav = post.value.is_fav ? 0 : 1
    post.value.fav_count = Math.max(0, (post.value.fav_count || 0) + (post.value.is_fav ? 1 : -1))
    ElMessage.success(post.value.is_fav ? '已收藏' : '已取消收藏')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const toggleCommentLike = async (c) => {
  if (!requireLogin()) return
  try {
    const res = c.is_liked
      ? await unlikeComment(userStore.uid, c.id)
      : await likeComment(userStore.uid, c.id)
    if (res.code !== 200) throw new Error(res.msg)

    c.is_liked = c.is_liked ? 0 : 1
    c.like_count = Math.max(0, (c.like_count || 0) + (c.is_liked ? 1 : -1))
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const submitComment = async () => {
  if (!requireLogin()) return
  const text = commentText.value.trim()
  if (!text) {
    ElMessage.warning('请先写点内容')
    return
  }
  commenting.value = true
  try {
    const res = await createComment({ post_id: postId, content: text })
    if (res.code === 200) {
      ElMessage.success('评论成功')
      commentText.value = ''
      await loadComments()
      if (post.value) post.value.post_count = comments.value.length
      await nextTick()
      commentsRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    } else {
      ElMessage.error(res.msg || '评论失败')
    }
  } catch (e) {
    // 传输层错误已统一提示
  } finally {
    commenting.value = false
  }
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(window.location.href)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制地址栏')
  }
}

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/bbs')
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadPost(), loadComments()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.bbs-detail {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 20px 20px 40px;
}
.container {
  max-width: 800px;
  margin: 0 auto;
}

.back-btn { margin-bottom: 10px; }

.post-card,
.comments-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px 26px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.post-user {
  display: flex;
  flex-direction: column;
}
.post-user .nickname {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.post-user .time {
  font-size: 12px;
  color: #909399;
}

.post-content {
  font-size: 16px;
  line-height: 1.75;
  color: #303133;
  margin: 0 0 16px;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-image { margin-bottom: 16px; }
.post-image :deep(.el-image) {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.post-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  padding-bottom: 14px;
  border-bottom: 1px solid #ebeef5;
}
.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.comments-card { margin-top: 16px; }
.comments-title {
  font-size: 16px;
  margin: 0 0 16px;
  color: #303133;
}

.comment-editor {
  padding-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 18px;
}
.editor-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.comment-item {
  display: flex;
  gap: 12px;
}
.comment-body { flex: 1; min-width: 0; }
.comment-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 4px;
}
.comment-head .nickname {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.comment-head .time {
  font-size: 12px;
  color: #c0c4cc;
}
.comment-content {
  font-size: 14px;
  color: #606266;
  margin: 0 0 6px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
}
.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}
.like-btn:hover,
.like-btn.active {
  color: #409eff;
}

@media (max-width: 768px) {
  .bbs-detail { padding: 12px; }
  .post-card, .comments-card { padding: 16px; }
  .post-content { font-size: 15px; }
}
</style>
