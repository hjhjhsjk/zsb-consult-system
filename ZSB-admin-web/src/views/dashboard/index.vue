<template>
  <div class="dashboard">
    <PageHeader title="仪表盘" subtitle="数据来自 4 张业务表，一次请求聚合" />

    <el-row :gutter="16" v-loading="loading">
      <el-col
        v-for="card in cards"
        :key="card.key"
        :xs="12"
        :sm="12"
        :md="8"
        :lg="4"
        class="col"
      >
        <el-card shadow="hover" class="stat-card" @click="go(card.path)">
          <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats[card.key] ?? 0 }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="quick-card">
      <template #header>
        <span class="card-title">快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button v-for="q in quicks" :key="q.path" :icon="q.icon" @click="go(q.path)">
          {{ q.label }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  School, Document, User, ChatDotRound, Tickets, Plus
} from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getStats } from '@/api/user'

const router = useRouter()
const loading = ref(false)

const stats = ref({
  schoolCount: 0,
  policyCount: 0,
  userCount: 0,
  postCount: 0,
  planCount: 0
})

// 卡片配置化：加指标只加一项，模板不用动
const cards = [
  { key: 'schoolCount', label: '院校总数', icon: School, color: '#409eff', bg: '#ecf5ff', path: '/school' },
  { key: 'policyCount', label: '政策总数', icon: Document, color: '#67c23a', bg: '#f0f9eb', path: '/policy' },
  { key: 'planCount', label: '招生计划', icon: Tickets, color: '#e6a23c', bg: '#fdf6ec', path: '/plan' },
  { key: 'userCount', label: '用户总数', icon: User, color: '#909399', bg: '#f4f4f5', path: '/user' },
  { key: 'postCount', label: '帖子总数', icon: ChatDotRound, color: '#f56c6c', bg: '#fef0f0', path: '/post' }
]

const quicks = [
  { label: '新增院校', icon: Plus, path: '/school' },
  { label: '发布政策', icon: Document, path: '/policy' },
  { label: '维护招生计划', icon: Tickets, path: '/plan' },
  { label: '用户管理', icon: User, path: '/user' }
]

const go = (path) => router.push(path)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStats()
    if (res.code === 200) {
      stats.value = { ...stats.value, ...(res.data || {}) }
    }
  } catch (e) {
    // 统计失败不阻塞页面，保持 0 即可
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.col {
  margin-bottom: 16px;
}
.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-3px);
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info { min-width: 0; }
.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
  white-space: nowrap;
}

.quick-card {
  border: none;
  margin-top: 4px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
