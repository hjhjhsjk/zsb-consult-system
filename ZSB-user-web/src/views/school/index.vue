<template>
  <div class="school-page">
    <div class="school-inner">
      <div class="page-head">
        <h2 class="page-title">
          院校库
          <span v-if="majorFilter" class="major-chip">
            专业：{{ majorFilter }}
            <el-icon class="chip-close" @click="clearMajor"><Close /></el-icon>
          </span>
        </h2>
        <span class="count" v-if="!loading">{{ list.length }} 所</span>
      </div>

      <!-- 筛选栏（按专业搜索时隐藏，避免两套筛选打架） -->
      <div v-if="!majorFilter" class="filter-bar">
        <div class="filter-item">
          <span class="label">省份：</span>
          <el-select
            v-model="filter.provinceId"
            style="width: 140px"
            placeholder="请选择省份"
            filterable
            :disabled="loading"
            @change="fetchList"
          >
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="label">类型：</span>
          <el-radio-group v-model="filter.type" :disabled="loading" @change="fetchList">
            <el-radio-button :value="1">公办</el-radio-button>
            <el-radio-button :value="2">民办</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 列表 -->
      <div v-loading="loading" element-loading-text="加载中..." class="school-list">
        <el-empty
          v-if="!loading && list.length === 0"
          :description="majorFilter ? `没有开设「${majorFilter}」的院校` : '暂无院校数据'"
        >
          <el-button v-if="majorFilter" @click="clearMajor">查看全部院校</el-button>
        </el-empty>

        <div
          v-for="item in list"
          :key="item.id"
          class="school-card"
          @click="goDetail(item)"
        >
          <SchoolLogo :src="item.logo_url" :name="item.school_name" :size="60" />
          <div class="info">
            <div class="name" :title="item.school_name">{{ item.school_name }}</div>
            <div class="city">
              <el-icon><Location /></el-icon>
              {{ item.city_name || '—' }}
            </div>
            <div v-if="item.major_name" class="major">
              <el-tag size="small" type="success">{{ item.major_name }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Close } from '@element-plus/icons-vue'
import SchoolLogo from '@/components/SchoolLogo.vue'
import { getSchools } from '@/api/school'
import { searchPlans } from '@/api/search'
import { fetchProvinces } from '@/utils/region'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const list = ref([])
const provinces = ref([])

const majorFilter = computed(() => {
  const m = route.query.major
  return m ? String(m) : ''
})

// 同一筛选条件第二次点直接出结果
const cache = new Map()

const filter = reactive({
  provinceId: Number(route.query.provinceId) || 17,  // 默认湖北
  type: 1
})

const loadProvinces = async () => {
  provinces.value = await fetchProvinces()
}

// 普通浏览：按省 + 办学类型
const fetchList = async () => {
  if (majorFilter.value) return fetchByMajor()

  const key = `${filter.provinceId}_${filter.type}`
  if (cache.has(key)) {
    list.value = cache.get(key)
    return
  }

  loading.value = true
  try {
    const res = await getSchools(filter.provinceId, filter.type)
    list.value = res.code === 200 ? (res.data || []) : []
    cache.set(key, list.value)
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

// 按专业浏览：走搜索接口，结果按院校去重
const fetchByMajor = async () => {
  loading.value = true
  try {
    const res = await searchPlans({ keyword: majorFilter.value })
    const rows = res.code === 200 ? (res.data || []) : []
    const map = new Map()
    rows.forEach(r => {
      if (!map.has(r.id)) {
        map.set(r.id, {
          id: r.id,
          school_name: r.school_name,
          logo_url: r.school_logo,
          city_name: '',
          school_type: r.school_type,
          major_name: r.major_name
        })
      }
    })
    list.value = [...map.values()]
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

const clearMajor = () => {
  const query = { ...route.query }
  delete query.major
  router.replace({ path: '/school', query })
}

const goDetail = (item) => {
  // 详情页首屏先拿缓存渲染，接口兜底，刷新不白屏
  sessionStorage.setItem('school_' + item.id, JSON.stringify(item))
  router.push('/school/' + item.id)
}

watch(() => route.query.major, () => fetchList())

onMounted(async () => {
  await loadProvinces()
  await fetchList()
})
</script>

<style scoped>
.school-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 24px 16px;
}

.school-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}
.page-title {
  font-size: 22px;
  margin: 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.count {
  font-size: 13px;
  color: #909399;
}

.major-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: normal;
  color: #409eff;
  background: #ecf5ff;
  padding: 4px 10px;
  border-radius: 14px;
}
.chip-close {
  cursor: pointer;
  font-size: 12px;
}

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

.school-list {
  min-height: 240px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  align-content: start;
}

.school-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  gap: 16px;
}

.school-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.city {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.major { margin-top: 8px; }

@media (max-width: 900px) {
  .school-list { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 600px) {
  .school-page { padding: 16px 12px; }
  .school-list { grid-template-columns: 1fr; }
  .filter-bar { gap: 12px; padding: 14px; }
  .school-card { padding: 14px; }
}
</style>
