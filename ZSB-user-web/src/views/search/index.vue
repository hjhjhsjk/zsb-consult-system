<template>
  <div class="search-page">
    <div class="inner">
      <!-- 搜索框 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索专业或院校，例如：计算机"
          clearable
          size="large"
          @keyup.enter="doSearch"
        >
          <template #append>
            <el-button type="primary" :icon="Search" @click="doSearch">搜索</el-button>
          </template>
        </el-input>

        <div class="hot-words">
          <span class="label">热门：</span>
          <el-tag
            v-for="w in hotWords"
            :key="w"
            class="hot-tag"
            effect="plain"
            @click="searchWord(w)"
          >
            {{ w }}
          </el-tag>
        </div>
      </div>

      <!-- 结果 -->
      <el-skeleton v-if="loading" :rows="5" animated />

      <el-empty
        v-else-if="!results.length"
        :description="searched ? `没有找到与「${keyword}」相关的结果` : '输入关键词开始搜索'"
      >
        <el-button v-if="searched" @click="goSchoolList">去院校库浏览全部</el-button>
      </el-empty>

      <template v-else>
        <div class="result-head">
          找到 <b>{{ results.length }}</b> 条与「{{ keyword }}」相关的招生计划
        </div>

        <div class="result-list">
          <div
            v-for="item in results"
            :key="`${item.id}-${item.major_name}-${item.year}`"
            class="result-card"
            @click="goSchool(item)"
          >
            <SchoolLogo :src="item.school_logo" :name="item.school_name" :size="64" />

            <div class="info">
              <div class="school-name">{{ item.school_name }}</div>
              <div class="major-line">
                <el-tag size="small" type="success">{{ item.major_name }}</el-tag>
                <el-tag size="small" :type="item.school_type === '公办' ? 'primary' : 'warning'">
                  {{ item.school_type }}
                </el-tag>
              </div>
              <div class="meta">
                <span class="item"><el-icon><Coin /></el-icon> 学费 {{ item.tuition_fee }} 元</span>
                <span class="item"><el-icon><User /></el-icon> 招 {{ item.plan_count }} 人</span>
                <span class="item">{{ item.year }} 年</span>
              </div>
            </div>

            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Coin, User, ArrowRight } from '@element-plus/icons-vue'
import SchoolLogo from '@/components/SchoolLogo.vue'
import { searchPlans } from '@/api/search'

const route = useRoute()
const router = useRouter()

const keyword = ref(route.query.keyword || '')
const results = ref([])
const loading = ref(false)
const searched = ref(false)

const hotWords = ['计算机', '护理', '学前教育', '会计', '电子商务', '机械设计']

const searchWord = (word) => {
  keyword.value = word
  router.replace({ path: '/search', query: { keyword: word } })
  doSearch()
}

const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) {
    searched.value = false
    results.value = []
    return
  }

  loading.value = true
  searched.value = true
  try {
    const res = await searchPlans({ keyword: kw })
    results.value = res.code === 200 ? (res.data || []) : []
  } catch (e) {
    results.value = []
  } finally {
    loading.value = false
  }
}

const goSchool = (item) => {
  // 详情页会再调接口，这里只带最少信息做首屏占位
  sessionStorage.setItem('school_' + item.id, JSON.stringify({
    id: item.id,
    school_name: item.school_name,
    logo_url: item.school_logo,
    city_name: ''
  }))
  router.push('/school/' + item.id)
}

const goSchoolList = () => router.push('/school')

onMounted(() => {
  if (keyword.value) doSearch()
})

watch(() => route.query.keyword, (kw) => {
  if (kw && kw !== keyword.value) {
    keyword.value = kw
    doSearch()
  }
})
</script>

<style scoped>
.search-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 24px 16px;
}
.inner {
  max-width: 900px;
  margin: 0 auto;
}

.search-bar {
  background: #fff;
  padding: 20px 22px;
  border-radius: 10px;
  margin-bottom: 16px;
}
.hot-words {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
  font-size: 13px;
}
.hot-words .label { color: #909399; }
.hot-tag { cursor: pointer; }

.result-head {
  font-size: 14px;
  color: #606266;
  margin: 0 4px 12px;
}
.result-head b { color: #409eff; }

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.result-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.info { flex: 1; min-width: 0; }
.school-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.major-line {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}
.meta .item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.arrow { color: #c0c4cc; flex-shrink: 0; }

@media (max-width: 768px) {
  .search-page { padding: 12px; }
  .search-bar { padding: 14px; }
  .result-card { padding: 12px; gap: 12px; }
  .arrow { display: none; }
  .meta { gap: 10px; }
}
</style>
