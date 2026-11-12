<template>
  <div class="detail-page">
    <div class="detail-inner">
      <div class="back-bar">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/consult' }">政策咨询</el-breadcrumb-item>
          <el-breadcrumb-item>正文</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <el-skeleton v-if="loading && !policy.id" :rows="8" animated />

      <div v-else class="detail-body">
        <template v-if="policy.id">
          <h1 class="title">{{ policy.title }}</h1>

          <div class="meta">
            <el-tag :type="policy.publisher === 1 ? 'primary' : 'success'" size="small">
              {{ policy.publisher === 1 ? '政府' : '学校' }}
            </el-tag>
            <el-tag v-if="policy.is_top === 1" type="danger" size="small" effect="dark">置顶</el-tag>
            <el-tag size="small" type="info">
              {{ policy.category_id === 2 ? '免试' : '普通' }}
            </el-tag>
            <span>{{ policy.create_time }}</span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ policy.view_count }}
            </span>
          </div>

          <article class="content">{{ policy.content }}</article>

          <div v-if="policy.url" class="url-box">
            <el-link type="primary" :href="policy.url" target="_blank" rel="noopener">
              查看原文链接 →
            </el-link>
          </div>

          <div class="action-bar">
            <el-button
              :type="isFav ? 'warning' : 'default'"
              :icon="Collection"
              :loading="favLoading"
              @click="toggleFav"
            >
              {{ isFav ? '已收藏' : '收藏政策' }}
            </el-button>
            <el-button :icon="Share" @click="copyLink">复制链接</el-button>
          </div>
        </template>

        <el-empty v-else description="政策不存在或已删除">
          <el-button type="primary" @click="$router.push('/consult')">返回政策列表</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Collection, Share } from '@element-plus/icons-vue'
import { getPolicyDetail } from '@/api/consult'
import { addFavorite, clearFavorite } from '@/api/bbs'
import { useUserStore } from '@/store/user'
import { addHistory } from '@/utils/history'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const policy = ref({})
const isFav = ref(false)
const favLoading = ref(false)

const fetchDetail = async () => {
  const id = Number(route.params.id)

  // 列表页缓存先顶上，页面不白屏
  const cached = sessionStorage.getItem('policy_' + id)
  if (cached) {
    try { policy.value = JSON.parse(cached) } catch {}
  }

  loading.value = true
  try {
    const res = await getPolicyDetail(id, userStore.uid || 0)
    if (res.code === 200 && res.data) {
      policy.value = res.data
      isFav.value = Number(res.data.is_fav) > 0
      addHistory({
        type: '政策',
        title: res.data.title,
        path: `/consult/${id}`
      })
    }
  } catch (e) {
    console.error('获取政策详情失败', e)
  } finally {
    loading.value = false
  }
}

const toggleFav = async () => {
  if (!userStore.uid) {
    ElMessage.warning('登录后才能收藏')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const id = Number(route.params.id)
  favLoading.value = true
  try {
    const res = isFav.value
      ? await clearFavorite(userStore.uid, 2, id)
      : await addFavorite(userStore.uid, 2, id)

    if (res.code === 200) {
      isFav.value = !isFav.value
      ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    favLoading.value = false
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
  else router.push('/consult')
}

onMounted(fetchDetail)
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 20px 16px;
}

.detail-inner {
  max-width: 820px;
  margin: 0 auto;
  background: #fff;
  border-radius: 10px;
  padding: 20px 36px 32px;
}

.back-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.title {
  font-size: 23px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 14px;
  line-height: 1.45;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #909399;
  padding-bottom: 14px;
  margin-bottom: 22px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
}

.meta .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.content {
  font-size: 16px;
  line-height: 1.95;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.url-box {
  margin-top: 28px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
}

.action-bar {
  margin-top: 28px;
  padding-top: 18px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .detail-page { padding: 12px; }
  .detail-inner { padding: 16px 16px 24px; }
  .title { font-size: 19px; }
  .content { font-size: 15px; line-height: 1.85; }
}
</style>
