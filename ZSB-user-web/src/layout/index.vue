<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ArrowDown, Search, Menu } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { fetchRegions } from '@/utils/region'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const keyword = ref('')
const municipalities = [1, 2, 3, 4]

const navItems = [
  { path: '/', label: '首页' },
  { path: '/consult', label: '政策' },
  { path: '/school', label: '院校' },
  { path: '/bbs', label: '论坛' },
  { path: '/user', label: '个人中心' }
]

// 移动端抽屉
const drawerOpen = ref(false)

const handleSearch = (kw = keyword.value) => {
  const text = String(kw || '').trim()
  if (!text) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  drawerOpen.value = false
  router.push({ path: '/search', query: { keyword: text } })
}

const go = (path) => {
  drawerOpen.value = false
  router.push(path)
}

// 菜单高亮
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/bbs')) return '/bbs'
  if (path.startsWith('/school')) return '/school'
  if (path.startsWith('/consult')) return '/consult'
  if (path.startsWith('/user')) return '/user'
  return '/'
})

// ================= 地区 =================
const areaOptions = ref([])
const areaValue = ref([])

const buildProvinces = (list) => list.map(p => ({
  value: p.id,
  label: p.name,
  leaf: municipalities.includes(p.id),
  children: municipalities.includes(p.id) ? undefined : []
}))

const loadProvinces = async () => {
  areaOptions.value = buildProvinces(await fetchRegions(0))
}

const loadCities = async (provinceId) => {
  const province = areaOptions.value.find(p => p.value === provinceId)
  if (!province || province.children?.length) return
  const list = await fetchRegions(provinceId)
  province.children = list.map(c => ({ value: c.id, label: c.name }))
}

// 用「用户注册的地区」重置
const resetAreaFromUser = async () => {
  const u = userStore.userInfo
  if (u?.provinceId) {
    const arr = (u.cityId && u.cityId !== u.provinceId)
      ? [u.provinceId, u.cityId]
      : [u.provinceId]
    userStore.setArea(arr)
    areaValue.value = [...arr]
    await loadCities(arr[0])
  } else {
    areaValue.value = []
  }
}

const handleChange = (val) => {
  userStore.setArea(val || [])
}

const handleExpand = (val) => {
  if (val && val.length === 1) loadCities(val[0])
}

// 用户下拉
const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '退出',
        cancelButtonText: '取消'
      })
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {}
  } else if (cmd === 'profile') {
    router.push('/user')
  }
}

// 登录后（或刷新后拿到用户信息）地区要跟着变
watch(() => userStore.userInfo?.provinceId, () => resetAreaFromUser())

onMounted(async () => {
  await loadProvinces()
  await resetAreaFromUser()
})
</script>

<template>
  <div class="app">
    <header class="header">
      <div class="header-inner">
        <!-- 左：logo + 移动端菜单按钮 -->
        <div class="left">
          <el-button
            class="menu-btn"
            text
            :icon="Menu"
            aria-label="打开菜单"
            @click="drawerOpen = true"
          />
          <div class="logo" @click="go('/')">专升本咨询</div>
        </div>

        <!-- 中：搜索 -->
        <div class="header-search">
          <el-input
            v-model="keyword"
            placeholder="搜索院校、专业、政策..."
            clearable
            class="search-input"
            @keyup.enter="handleSearch()"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch()">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 右：导航 + 地区 + 用户 -->
        <div class="header-right">
          <nav class="nav">
            <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="nav-link"
              :class="{ active: activeMenu === item.path }"
            >
              {{ item.label }}
            </router-link>
          </nav>

          <el-cascader
            v-model="areaValue"
            :options="areaOptions"
            :show-all-levels="false"
            placeholder="地区"
            clearable
            class="area-select"
            @change="handleChange"
            @expand-change="handleExpand"
          />

          <div class="user-area">
            <el-dropdown v-if="userStore.isLogin" @command="handleCommand">
              <div class="user-info">
                <el-avatar
                  :size="32"
                  :icon="UserFilled"
                  :src="userStore.userInfo?.avatar || ''"
                />
                <span class="username">
                  {{ userStore.userInfo?.nickname || '用户' }}
                </span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <template v-else>
              <el-button type="primary" @click="go('/login')">登录</el-button>
              <el-button class="register-btn" @click="go('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 移动端菜单 -->
    <el-drawer v-model="drawerOpen" direction="rtl" size="78%" title="菜单">
      <div class="drawer-search">
        <el-input
          v-model="keyword"
          placeholder="搜索院校、专业..."
          clearable
          @keyup.enter="handleSearch()"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch()" />
          </template>
        </el-input>
      </div>

      <div class="drawer-region">
        <span class="label">当前地区</span>
        <el-cascader
          v-model="areaValue"
          :options="areaOptions"
          :show-all-levels="false"
          placeholder="选择地区"
          clearable
          style="width: 100%"
          @change="handleChange"
          @expand-change="handleExpand"
        />
      </div>

      <ul class="drawer-nav">
        <li
          v-for="item in navItems"
          :key="item.path"
          :class="{ active: activeMenu === item.path }"
          @click="go(item.path)"
        >
          {{ item.label }}
        </li>
      </ul>

      <div class="drawer-user">
        <template v-if="userStore.isLogin">
          <span class="nickname">{{ userStore.userInfo?.nickname || '用户' }}</span>
          <el-button type="danger" plain @click="handleCommand('logout')">退出登录</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="go('/login')">登录</el-button>
          <el-button @click="go('/register')">注册</el-button>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<style>
.app {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.menu-btn {
  display: none;
  font-size: 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}

.header-search {
  flex: 1;
  max-width: 380px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-link {
  padding: 8px 12px;
  font-size: 15px;
  color: #606266;
  text-decoration: none;
  border-radius: 6px;
  white-space: nowrap;
  transition: color 0.2s, background 0.2s;
}
.nav-link:hover {
  color: #409eff;
  background: #f5f7fa;
}
.nav-link.active {
  color: #409eff;
  font-weight: 600;
  background: #ecf5ff;
}

.area-select {
  width: 120px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}
.user-info:hover {
  background: #f5f7fa;
}
.username {
  font-size: 14px;
  color: #303133;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main {
  flex: 1;
  min-height: 0;
}

/* 抽屉 */
.drawer-search { margin-bottom: 20px; }
.drawer-region {
  margin-bottom: 20px;
}
.drawer-region .label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.drawer-nav {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
}
.drawer-nav li {
  padding: 14px 12px;
  font-size: 16px;
  color: #303133;
  border-radius: 8px;
  cursor: pointer;
}
.drawer-nav li.active {
  color: #409eff;
  background: #ecf5ff;
  font-weight: 600;
}
.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
.drawer-user .nickname {
  font-size: 15px;
  color: #303133;
  margin-right: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.el-cascader-panel {
  max-height: 300px;
  overflow-y: auto;
}

/* 中屏收掉地区选择，导航仍然保留 */
@media (max-width: 1100px) {
  .header-right .area-select { display: none; }
  .header-search { max-width: 300px; }
}

@media (max-width: 900px) {
  .header-search { display: none; }
  .nav { display: none; }
  .menu-btn { display: inline-flex; }
  .header-inner { padding: 0 12px; gap: 8px; }
  .logo { font-size: 18px; }
  .register-btn { display: none; }
  /* 手机上空间不够，只留头像和下拉箭头 */
  .username { display: none; }
  .user-info { padding: 4px; }
}
</style>
