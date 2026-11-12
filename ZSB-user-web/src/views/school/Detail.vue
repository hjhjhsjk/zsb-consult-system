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
          <el-breadcrumb-item :to="{ path: '/school' }">院校库</el-breadcrumb-item>
          <el-breadcrumb-item>{{ school.school_name || '院校详情' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div v-loading="loading" element-loading-text="加载中..." class="detail-body">
        <template v-if="school.id">
          <!-- 学校头部 -->
          <div class="school-header">
            <SchoolLogo :src="school.logo_url" :name="school.school_name" :size="96" :lazy="false" />
            <div class="info">
              <h1 class="name">{{ school.school_name }}</h1>
              <div class="tags">
                <el-tag v-if="school.school_type" size="small" :type="school.school_type === 1 ? 'success' : 'warning'">
                  {{ school.school_type === 1 ? '公办' : '民办' }}
                </el-tag>
                <span class="city">
                  <el-icon><Location /></el-icon>
                  {{ school.city_name || '—' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 招生计划 -->
          <div class="section">
            <div class="section-head">
              <h2 class="section-title">招生计划</h2>
              <el-select
                v-model="year"
                placeholder="全部年份"
                clearable
                size="small"
                style="width: 120px"
                @change="onYearChange"
              >
                <el-option v-for="y in years" :key="y" :label="`${y} 年`" :value="y" />
              </el-select>
            </div>

            <!-- 桌面端表格 -->
            <el-table v-if="filteredPlans.length" :data="filteredPlans" stripe border class="plan-table">
              <el-table-column prop="year" label="年份" width="90" />
              <el-table-column prop="major_name" label="专业" min-width="200" show-overflow-tooltip />
              <el-table-column prop="plan_count" label="招生人数" width="110" />
              <el-table-column prop="tuition_fee" label="学费（元/年）" width="130" />
              <el-table-column label="类型" width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.recruitment_type === 1 ? 'primary' : 'warning'">
                    {{ row.recruitment_type === 1 ? '普通' : '免试' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <!-- 移动端卡片 -->
            <div v-if="filteredPlans.length" class="plan-cards">
              <div v-for="row in filteredPlans" :key="row.id" class="plan-card">
                <div class="plan-top">
                  <span class="major">{{ row.major_name }}</span>
                  <el-tag size="small" :type="row.recruitment_type === 1 ? 'primary' : 'warning'">
                    {{ row.recruitment_type === 1 ? '普通' : '免试' }}
                  </el-tag>
                </div>
                <div class="plan-meta">
                  <span>{{ row.year }} 年</span>
                  <span>招 {{ row.plan_count }} 人</span>
                  <span>学费 {{ row.tuition_fee }} 元</span>
                </div>
              </div>
            </div>

            <el-empty v-else description="暂无招生计划" />
          </div>
        </template>

        <el-empty v-else-if="!loading" description="院校不存在或已下架">
          <el-button type="primary" @click="$router.push('/school')">返回院校库</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Location } from '@element-plus/icons-vue'
import SchoolLogo from '@/components/SchoolLogo.vue'
import { getRecruitmentPlan, getSchoolDetail } from '@/api/school'
import { addHistory } from '@/utils/history'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const school = ref({})
const plans = ref([])
const year = ref(null)

// 年份选项：从计划里倒序取
const years = computed(() => [...new Set(plans.value.map(p => p.year))].sort((a, b) => b - a))
const filteredPlans = computed(() =>
  year.value ? plans.value.filter(p => p.year === year.value) : plans.value
)

const onYearChange = () => {}

const fetchData = async () => {
  const id = Number(route.params.id)

  // 列表页带过来的数据先渲染，避免白屏
  const cached = sessionStorage.getItem('school_' + id)
  if (cached) {
    try { school.value = JSON.parse(cached) } catch {}
  }

  loading.value = true
  try {
    const [detail, planRes] = await Promise.all([
      getSchoolDetail(id),
      getRecruitmentPlan(id)
    ])

    if (detail.code === 200 && detail.data) {
      const d = detail.data
      school.value = {
        id: d.id,
        school_name: d.school_name,
        logo_url: d.logo_url || d.school_logo,
        city_name: d.city_name,
        school_type: d.school_type
      }
    }

    if (planRes.code === 200) {
      plans.value = planRes.data || []
      // 详情接口没给校名时，用计划里的冗余字段兜底
      if (!school.value.id && plans.value.length) {
        const first = plans.value[0]
        school.value = {
          id,
          school_name: first.school_name,
          logo_url: first.school_logo,
          city_name: ''
        }
      }
    }

    if (school.value.school_name) {
      addHistory({
        type: '院校',
        title: school.value.school_name,
        path: `/school/${id}`
      })
    }
  } catch (e) {
    console.error('获取院校详情失败', e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/school')
}

onMounted(fetchData)
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 20px 16px;
}

.detail-inner {
  max-width: 1000px;
  margin: 0 auto;
  background: #fff;
  border-radius: 10px;
  padding: 20px 32px 36px;
}

.back-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.school-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 22px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 22px;
}

.info .name {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 10px;
}

.info .tags {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #909399;
}
.info .city {
  display: flex;
  align-items: center;
  gap: 4px;
}

.section { margin-top: 8px; }
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-title {
  font-size: 18px;
  margin: 0;
  color: #303133;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.plan-cards { display: none; }

@media (max-width: 768px) {
  .detail-inner { padding: 14px 14px 24px; }
  .school-header { gap: 14px; }
  .info .name { font-size: 19px; }

  /* 手机上表格横向滚动体验差，换成卡片 */
  .plan-table { display: none; }
  .plan-cards {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .plan-card {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 12px 14px;
  }
  .plan-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
  }
  .plan-top .major {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
  .plan-meta {
    display: flex;
    gap: 14px;
    font-size: 12px;
    color: #909399;
    flex-wrap: wrap;
  }
}
</style>
