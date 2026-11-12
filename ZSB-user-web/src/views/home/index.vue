<template>
  <div class="home">
    <!-- 轮播：真实校园图片 + 深色渐变蒙层保证文字可读 -->
    <el-carousel :height="bannerHeight" class="banner" :interval="5500" arrow="hover">
      <el-carousel-item v-for="(item, i) in banners" :key="item.title">
        <div class="banner-item">
          <img
            class="banner-bg"
            :src="item.img"
            :alt="item.title"
            :loading="i === 0 ? 'eager' : 'lazy'"
            :fetchpriority="i === 0 ? 'high' : 'auto'"
            decoding="async"
          />
          <div class="banner-mask"></div>
          <div class="banner-content">
            <span class="banner-tag">{{ item.tag }}</span>
            <h2>{{ item.title }}</h2>
            <p>{{ item.sub }}</p>
            <el-button class="banner-btn" round size="large" @click="goTo(item.path)">
              {{ item.action }}
            </el-button>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 快捷入口 -->
    <el-row :gutter="16" class="quick-entry">
      <el-col v-for="entry in entries" :key="entry.title" :xs="24" :sm="8">
        <el-card shadow="hover" class="entry-card" @click="goTo(entry.path)">
          <div class="entry-item">
            <el-icon class="entry-icon" :size="30" :color="entry.color">
              <component :is="entry.icon" />
            </el-icon>
            <div>
              <h3>{{ entry.title }}</h3>
              <p>{{ entry.desc }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门院校 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">热门院校</h2>
        <el-link type="primary" underline="never" @click="goTo('/school')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>

      <el-row v-if="loadingSchools" :gutter="16">
        <el-col v-for="i in 8" :key="i" :xs="12" :sm="8" :md="6">
          <el-card shadow="never" class="school-card skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 64px; height: 64px; border-radius: 8px" />
                <div style="margin-top: 10px">
                  <el-skeleton-item variant="text" style="width: 80%" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-else-if="!hotSchools.length" description="该地区暂无院校数据">
        <el-button type="primary" @click="goTo('/school')">去院校库看看</el-button>
      </el-empty>

      <el-row v-else :gutter="16">
        <el-col v-for="item in hotSchools" :key="item.id" :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="school-card" @click="goSchool(item)">
            <SchoolLogo :src="item.logo_url" :name="item.school_name" :size="64" class="school-logo" />
            <h3 :title="item.school_name">{{ item.school_name }}</h3>
            <p class="city">
              <el-icon><Location /></el-icon>
              {{ item.city_name || '—' }}
            </p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 热门专业 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">热门专业</h2>
        <el-link type="primary" underline="never" @click="goTo('/school')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>

      <el-row v-if="loadingMajors" :gutter="16">
        <el-col v-for="i in 6" :key="i" :xs="8" :sm="6" :md="4">
          <el-card shadow="never" class="major-card">
            <el-skeleton :rows="1" animated />
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-else-if="!hotMajors.length" description="暂无专业数据" />

      <el-row v-else :gutter="16">
        <el-col v-for="item in hotMajors" :key="item.id" :xs="8" :sm="6" :md="4">
          <el-card shadow="hover" class="major-card" @click="goToMajor(item.label)">
            <h3 :title="item.label">{{ item.label }}</h3>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 最新讨论 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">最新讨论</h2>
        <el-link type="primary" underline="never" @click="goTo('/bbs')">
          去论坛逛逛 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>

      <div v-if="loadingPosts" class="post-grid">
        <el-card v-for="i in 6" :key="i" shadow="never" class="post-card">
          <el-skeleton :rows="2" animated />
        </el-card>
      </div>

      <el-empty v-else-if="!latestPosts.length" description="还没有人发帖，来抢个沙发？">
        <el-button type="primary" @click="goTo('/bbs')">去发第一帖</el-button>
      </el-empty>

      <div v-else class="post-grid">
        <article
          v-for="item in latestPosts"
          :key="item.id"
          class="post-card"
          @click="goPost(item)"
        >
          <header class="post-head">
            <el-avatar :size="34" :src="fixImg(item.avatar)" class="post-avatar">
              {{ (item.nickname || '匿')[0] }}
            </el-avatar>
            <div class="post-author">
              <strong>{{ item.nickname || '匿名同学' }}</strong>
              <span>{{ timeAgo(item.post_time) }}</span>
            </div>
          </header>

          <p class="post-content">{{ item.content }}</p>

          <footer class="post-meta">
            <span><el-icon><View /></el-icon>{{ shortNumber(item.view_count) }}</span>
            <span><el-icon><Star /></el-icon>{{ shortNumber(item.like_count) }}</span>
            <span><el-icon><ChatDotRound /></el-icon>{{ shortNumber(item.post_count) }}</span>
          </footer>
        </article>
      </div>
    </section>

    <footer class="footer">
      专升本咨询系统 · 数据仅供参考，请以各省教育考试院公告为准
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  School, Reading, Document, ArrowRight, Location, View, Star, ChatDotRound
} from '@element-plus/icons-vue'
import SchoolLogo from '@/components/SchoolLogo.vue'
import { useUserStore } from '@/store/user'
import { getSchools } from '@/api/school'
import { getMajors } from '@/api/user'
import { getLatestPosts } from '@/api/bbs'
import { fixImg } from '@/utils/image'
import { timeAgo, shortNumber } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const goTo = (path) => router.push(path)
const goSchool = (item) => router.push(`/school/${item.id}`)
const goPost = (item) => router.push(`/bbs/${item.id}`)
const goToMajor = (major) => router.push({ path: '/school', query: { major } })

// ========== 轮播图（本地真实图片，public/images/banners） ==========
const banners = [
  {
    tag: '2026 报考季',
    title: '2026 年专升本报名时间公布',
    sub: '各省报名时间陆续发布，第一时间掌握关键节点',
    action: '查看政策',
    path: '/consult',
    img: '/images/banners/banner-1.jpg'
  },
  {
    tag: '院校库',
    title: '全国 130+ 所院校招生计划一站查',
    sub: '按省份、城市、专业筛选，对比招生人数与学费',
    action: '查院校',
    path: '/school',
    img: '/images/banners/banner-2.jpg'
  },
  {
    tag: '政策解读',
    title: '从报名到录取，全流程政策讲清楚',
    sub: '报名条件、考试科目、免试办法，一篇看懂',
    action: '看政策',
    path: '/consult',
    img: '/images/banners/banner-3.jpg'
  },
  {
    tag: '学长学姐',
    title: '在论坛找到同校同专业的过来人',
    sub: '提问、答疑、交换资料，备考路上不孤单',
    action: '进论坛',
    path: '/bbs',
    img: '/images/banners/banner-4.jpg'
  }
]

const entries = [
  { title: '院校查询', desc: '查找全国专升本院校', icon: School, color: '#409eff', path: '/school' },
  { title: '专业查询', desc: '按专业筛选院校', icon: Reading, color: '#67c23a', path: '/school' },
  { title: '免试查询', desc: '查看免试政策与名单', icon: Document, color: '#e6a23c', path: '/consult?type=2' }
]

// 轮播高度跟屏幕走，手机上 360px 太高
const isMobile = ref(window.innerWidth < 768)
const bannerHeight = computed(() => (isMobile.value ? '220px' : '360px'))
const onResize = () => { isMobile.value = window.innerWidth < 768 }

// ========== 热门院校 ==========
const schoolList = ref([])
const loadingSchools = ref(false)
const hotSchools = computed(() => schoolList.value.slice(0, 8))

const loadSchools = async () => {
  // 未登录/未选地区时默认湖北（17），避免首页首屏空白
  const provinceId = userStore.prefs.area?.[0] || 17
  if (!provinceId) {
    schoolList.value = []
    return
  }
  loadingSchools.value = true
  try {
    const res = await getSchools(provinceId, 1)
    schoolList.value = res.code === 200 ? (res.data || []) : []
  } catch (e) {
    schoolList.value = []
  } finally {
    loadingSchools.value = false
  }
}

// ========== 热门专业 ==========
const majorList = ref([])
const loadingMajors = ref(false)
const hotMajors = computed(() => majorList.value.slice(0, 6))

const loadMajors = async () => {
  loadingMajors.value = true
  try {
    const res = await getMajors()
    majorList.value = res.code === 200
      ? (res.data || []).map(m => ({ id: m.id, label: m.c_name }))
      : []
  } catch (e) {
    majorList.value = []
  } finally {
    loadingMajors.value = false
  }
}

// ========== 最新讨论 ==========
const latestPosts = ref([])
const loadingPosts = ref(false)

const loadLatestPosts = async () => {
  loadingPosts.value = true
  try {
    const res = await getLatestPosts(6)
    latestPosts.value = res.code === 200 ? (res.data || []) : []
  } catch (e) {
    latestPosts.value = []
  } finally {
    loadingPosts.value = false
  }
}

// 地区变化 → 只重拉院校，专业不用动。
// immediate：首屏就执行一次，省掉 onMounted 里再调一遍（否则会和导航栏初始化重复请求）
watch(() => userStore.prefs.area?.[0], () => loadSchools(), { immediate: true })

onMounted(() => {
  loadMajors()
  loadLatestPosts()
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => window.removeEventListener('resize', onResize))
</script>

<style scoped>
.home {
  padding: 24px 20px 0;
  max-width: 1280px;
  margin: 0 auto;
}

/* ===== 轮播 ===== */
.banner {
  margin-bottom: 24px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12);
}
.banner-item {
  position: relative;
  height: 100%;
  overflow: hidden;
}
.banner-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transform: scale(1.02);
}
.banner-mask {
  position: absolute;
  inset: 0;
  /* 两层叠加：横向左深右浅保证文字区对比度，纵向轻微压暗避免高光背景吃掉白字 */
  background:
    linear-gradient(90deg, rgba(6, 12, 24, 0.92) 0%, rgba(6, 12, 24, 0.72) 45%, rgba(6, 12, 24, 0.22) 100%),
    linear-gradient(180deg, rgba(6, 12, 24, 0.18) 0%, rgba(6, 12, 24, 0.42) 100%);
}
.banner-content {
  position: relative;
  z-index: 2;
  height: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  color: #fff;
}
.banner-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(64, 158, 255, 0.92);
  font-size: 12px;
  letter-spacing: 1px;
}
.banner-content h2 {
  font-size: 32px;
  line-height: 1.3;
  margin: 14px 0 10px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
}
.banner-content p {
  font-size: 15px;
  margin: 0;
  max-width: 560px;
  color: rgba(255, 255, 255, 0.88);
}
.banner-btn {
  margin-top: 24px;
  padding: 0 30px;
  color: #1f2d3d;
  font-weight: 600;
  border: none;
}

/* ===== 快捷入口 ===== */
.quick-entry { margin-bottom: 28px; }
.entry-card {
  cursor: pointer;
  margin-bottom: 16px;
  transition: transform 0.2s;
}
.entry-card:hover { transform: translateY(-3px); }
.entry-item {
  display: flex;
  align-items: center;
  gap: 14px;
}
.entry-icon { flex-shrink: 0; }
.entry-item h3 {
  font-size: 16px;
  margin: 0 0 4px;
  color: #303133;
}
.entry-item p { font-size: 13px; color: #909399; margin: 0; }

/* ===== 区块 ===== */
.section { margin-bottom: 32px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #303133;
  position: relative;
  padding-left: 12px;
}
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #409eff;
  border-radius: 2px;
}

/* ===== 院校卡片 ===== */
.school-card {
  margin-bottom: 16px;
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
  height: calc(100% - 16px);
}
.school-card:hover { transform: translateY(-4px); }
.school-logo { margin: 0 auto 10px; }
.school-card h3 {
  font-size: 14px;
  margin: 0 0 6px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.school-card .city {
  font-size: 12px;
  color: #909399;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
}
.skeleton-card { cursor: default; }

/* ===== 专业卡片 ===== */
.major-card {
  margin-bottom: 16px;
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f9ff 100%);
  border: none;
}
.major-card:hover { transform: translateY(-4px); }
.major-card h3 {
  font-size: 14px;
  margin: 4px 0;
  color: #409eff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 最新讨论 ===== */
.post-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}
.post-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px 18px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.post-card:hover {
  transform: translateY(-3px);
  border-color: #c6e2ff;
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.12);
}
.post-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.post-avatar { flex-shrink: 0; background: #409eff; }
.post-author {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.post-author strong {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.post-author span { font-size: 12px; color: #a8abb2; }
.post-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: #4b5563;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.post-meta {
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: #909399;
  margin-top: auto;
}
.post-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* ===== Footer ===== */
.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 24px 0;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .home { padding: 16px 12px 0; }
  .banner { border-radius: 10px; }
  .banner-content {
    padding: 0 22px;
    align-items: center;
    text-align: center;
  }
  .banner-content h2 { font-size: 20px; margin: 10px 0 6px; }
  .banner-content p { font-size: 13px; }
  .banner-btn { margin-top: 16px; height: 36px; }
  .section-title { font-size: 17px; }
  .post-grid { grid-template-columns: 1fr; }
}
</style>
