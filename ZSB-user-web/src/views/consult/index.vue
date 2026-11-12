<template>
  <div class="consult-page">
    <div class="consult-inner">
      <div class="page-head">
        <h2 class="page-title">政策咨询</h2>
        <span class="count" v-if="!loading">{{ list.length }} 条</span>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-item">
          <span class="label">省份：</span>
          <el-select
            v-model="filter.provinceId"
            style="width: 140px"
            placeholder="请选择省份"
            filterable
            @change="fetchList"
          >
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="label">来源：</span>
          <el-radio-group v-model="filter.publisher" @change="fetchList">
            <el-radio-button :value="1">政府</el-radio-button>
            <el-radio-button :value="2">学校</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <span class="label">类型：</span>
          <el-radio-group v-model="filter.type" @change="fetchList">
            <el-radio-button :value="1">普通</el-radio-button>
            <el-radio-button :value="2">免试</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 列表 -->
      <div v-loading="loading" class="policy-list">
        <el-skeleton v-if="loading" :rows="4" animated />

        <el-empty
          v-else-if="list.length === 0"
          description="该条件下暂无政策"
        />

        <div
          v-for="item in list"
          :key="item.id"
          class="policy-card"
          :class="{ top: item.is_top === 1 }"
          @click="goDetail(item)"
        >
          <div class="card-header">
            <el-tag v-if="item.is_top === 1" type="danger" size="small" effect="dark">置顶</el-tag>
            <span class="title">{{ item.title }}</span>
            <el-tag :type="item.publisher === 1 ? 'primary' : 'success'" size="small">
              {{ item.publisher === 1 ? '政府' : '学校' }}
            </el-tag>
          </div>

          <div class="card-content">{{ item.content }}</div>

          <div class="card-footer">
            <span>{{ item.create_time }}</span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ item.view_count }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'
import { getPolicies } from '@/api/consult'
import { fetchProvinces } from '@/utils/region'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const list = ref([])
const provinces = ref([])

const filter = reactive({
  provinceId: 17,   // 默认湖北
  // 首页「免试查询」带 type=2 进来，直接落到免试
  publisher: 1,
  type: Number(route.query.type) === 2 ? 2 : 1
})

const loadProvinces = async () => {
  provinces.value = await fetchProvinces()
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPolicies({
      province_id: filter.provinceId,
      publisher: filter.publisher,
      type: filter.type
    })
    list.value = res.code === 200 ? (res.data || []) : []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

const goDetail = (item) => {
  // 先塞缓存，详情页首屏不白
  sessionStorage.setItem('policy_' + item.id, JSON.stringify(item))
  router.push('/consult/' + item.id)
}

onMounted(async () => {
  await loadProvinces()
  await fetchList()
})
</script>

<style scoped>
.consult-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 24px 16px;
}

.consult-inner {
  max-width: 1000px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;
}
.page-title {
  font-size: 22px;
  margin: 0;
  color: #303133;
}
.count { font-size: 13px; color: #909399; }

.filter-bar {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item .label {
  font-size: 14px;
  color: #606266;
}

.policy-list { min-height: 220px; }

.policy-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.policy-card.top {
  border-left: 3px solid #f56c6c;
}

.policy-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}

.card-footer .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .consult-page { padding: 16px 12px; }
  .filter-bar { gap: 12px; padding: 14px; }
  .filter-item { width: 100%; }
  .page-title { font-size: 19px; }
  .policy-card { padding: 14px; }
}
</style>
