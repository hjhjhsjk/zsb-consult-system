<template>
  <div class="bbs-page">
    <div class="bbs-container">
      <!-- 顶部 -->
      <div class="bbs-header">
        <div class="header-left">
          <h2 class="title">论坛</h2>
          <span class="region">
            <el-icon><Location /></el-icon>
            {{ regionName || '未选择地区' }}
          </span>
        </div>
        <el-button type="primary" :icon="EditPen" @click="openPostDialog">发帖</el-button>
      </div>

      <!-- 列表 -->
      <el-skeleton v-if="loading" :rows="5" animated />

      <el-empty v-else-if="!postList.length" description="该地区暂无帖子，来发第一帖吧">
        <el-button type="primary" :icon="EditPen" @click="openPostDialog">发布第一帖</el-button>
      </el-empty>

      <div v-else class="post-list">
        <article
          v-for="item in postList"
          :key="item.id"
          class="post-item"
          @click="goDetail(item.id)"
        >
          <header class="post-user">
            <el-avatar :size="34" :src="fixImg(item.avatar)">
              {{ (item.nickname || 'U').charAt(0) }}
            </el-avatar>
            <div class="user-meta">
              <span class="nickname">{{ item.nickname || '匿名用户' }}</span>
              <span class="time">{{ timeAgo(item.post_time) }}</span>
            </div>
          </header>

          <div class="post-main">
            <p class="post-content">{{ item.content }}</p>
            <div v-if="item.image_url" class="post-image">
              <img :src="fixImg(item.image_url)" alt="帖子配图" loading="lazy" @error="hideImg" />
            </div>
          </div>

          <footer class="post-meta">
            <span><el-icon><View /></el-icon> {{ item.view_count }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ item.post_count }}</span>
            <span><el-icon><Star /></el-icon> {{ item.like_count || 0 }}</span>
          </footer>
        </article>
      </div>
    </div>

    <!-- 发帖弹窗 -->
    <el-dialog
      v-model="postDialogVisible"
      title="发帖"
      width="520px"
      :close-on-click-modal="false"
      @closed="onDialogClosed"
    >
      <el-form label-position="top">
        <el-form-item label="内容" required>
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="5"
            placeholder="分享你的备考经验、疑问或资讯..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图片（可选，最大 5MB）">
          <div class="upload-box">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeImgUpload"
              :http-request="handleImgUpload"
              accept="image/*"
            >
              <img v-if="postForm.imagePreview" :src="postForm.imagePreview" class="preview-img" />
              <div v-else class="upload-placeholder">
                <el-icon :size="24"><Plus /></el-icon>
                <span>添加图片</span>
              </div>
            </el-upload>
            <el-button
              v-if="postForm.imagePreview"
              link
              type="danger"
              @click="removeImage"
            >
              移除图片
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="posting" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen, View, ChatDotRound, Location, Plus, Star
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getPostList, insertPost } from '@/api/bbs'
import { fixImg } from '@/utils/image'
import { timeAgo } from '@/utils/format'
import { provinceName } from '@/utils/region'

const router = useRouter()
const userStore = useUserStore()

// 游客没选地区时回落湖北（与首页/院校库一致），避免论坛一片空白
const provinceId = computed(() => userStore.prefs.area?.[0] || 17)
const regionName = ref('')

const loadRegionName = async () => {
  regionName.value = await provinceName(provinceId.value)
}

// ========== 列表 ==========
const postList = ref([])
const loading = ref(false)

const loadPosts = async () => {
  if (!provinceId.value) {
    postList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getPostList(provinceId.value, 0)   // school_id=0 全省
    postList.value = res.code === 200 ? (res.data || []) : []
  } catch (e) {
    postList.value = []
  } finally {
    loading.value = false
  }
}

const hideImg = (e) => { e.target.style.display = 'none' }

// ========== 发帖 ==========
const postDialogVisible = ref(false)
const posting = ref(false)
const postForm = reactive({
  content: '',
  imageFile: null,
  imagePreview: ''
})

const openPostDialog = () => {
  if (!userStore.uid) {
    ElMessage.warning('登录后才能发帖')
    router.push({ path: '/login', query: { redirect: '/bbs' } })
    return
  }
  if (!provinceId.value) {
    ElMessage.warning('请先在右上角选择地区')
    return
  }
  postForm.content = ''
  postForm.imageFile = null
  postForm.imagePreview = ''
  postDialogVisible.value = true
}

const beforeImgUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片')
    return false
  }
  if (file.size / 1024 / 1024 >= 5) {
    ElMessage.error('图片不能超过 5MB')
    return false
  }
  return true
}

const handleImgUpload = (options) => {
  // 只做本地预览，真正上传在提交时一起发，避免发帖取消留下孤儿文件
  revokePreview()
  postForm.imageFile = options.file
  postForm.imagePreview = URL.createObjectURL(options.file)
}

const revokePreview = () => {
  if (postForm.imagePreview.startsWith('blob:')) {
    URL.revokeObjectURL(postForm.imagePreview)
  }
}

const removeImage = () => {
  revokePreview()
  postForm.imageFile = null
  postForm.imagePreview = ''
}

const onDialogClosed = () => {
  revokePreview()
  postForm.imagePreview = ''
}

const submitPost = async () => {
  const content = postForm.content.trim()
  if (!content) {
    ElMessage.warning('请输入内容')
    return
  }

  posting.value = true
  try {
    const fd = new FormData()
    fd.append('content', content)
    fd.append('region_id', provinceId.value)
    fd.append('school_id', 0)
    if (postForm.imageFile) fd.append('image', postForm.imageFile)

    const res = await insertPost(fd)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      postDialogVisible.value = false
      await loadPosts()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (e) {
    // 传输层错误 request.js 已提示，这里不重复弹
  } finally {
    posting.value = false
  }
}

const goDetail = (id) => router.push(`/bbs/${id}`)

// 地区变化 → 重新拉；immediate 保证首屏执行一次，无需在 onMounted 再调
watch(provinceId, () => {
  loadRegionName()
  loadPosts()
}, { immediate: true })

onBeforeUnmount(revokePreview)
</script>

<style scoped>
.bbs-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 24px 20px;
}

.bbs-container {
  max-width: 900px;
  margin: 0 auto;
}

.bbs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 18px 22px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 14px;
  flex-wrap: wrap;
}
.title {
  font-size: 21px;
  margin: 0;
  color: #303133;
}
.region {
  font-size: 13px;
  color: #909399;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-item {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.post-item:hover {
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.post-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.user-meta .nickname {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.user-meta .time {
  font-size: 12px;
  color: #c0c4cc;
}

.post-content {
  font-size: 15px;
  color: #303133;
  margin: 0 0 10px;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.post-image img {
  max-width: 220px;
  max-height: 150px;
  border-radius: 6px;
  object-fit: cover;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
  color: #909399;
  margin-top: 10px;
}
.post-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 上传 */
.upload-box {
  display: flex;
  align-items: center;
  gap: 12px;
}
.preview-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}
.upload-placeholder {
  width: 100px;
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #8c939d;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  transition: all 0.2s;
}
.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

@media (max-width: 768px) {
  .bbs-page { padding: 12px; }
  .bbs-header { padding: 14px; }
  .title { font-size: 18px; }
  .post-item { padding: 14px; }
  .post-image img { max-width: 100%; }
}
</style>
