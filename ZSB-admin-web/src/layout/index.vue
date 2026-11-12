<template>
  <el-container class="admin-layout">
    <!-- 移动端遮罩 -->
    <div v-if="isMobile && drawerOpen" class="mask" @click="drawerOpen = false" />

    <!-- 侧边栏 -->
    <el-aside
      :width="asideWidth"
      class="aside"
      :class="{ 'is-mobile': isMobile, 'is-open': drawerOpen }"
    >
      <div class="logo">
        <span v-if="!collapsed || isMobile">专升本 · 后台</span>
        <span v-else>ZSB</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
        :collapse="collapsed && !isMobile"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#b7bdc3"
        active-text-color="#409eff"
        @select="onMenuSelect"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="body">
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            text
            class="collapse-btn"
            :icon="isMobile || collapsed ? Expand : Fold"
            @click="toggleAside"
          />
          <div class="header-title">{{ currentTitle }}</div>
        </div>

        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="nickname">{{ userStore.userInfo?.nickname || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Odometer, School, Document, Tickets, ChatDotRound, User, Reading,
  UserFilled, ArrowDown, Expand, Fold
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 菜单数据化：加一页只改这里，不用再复制一段 <el-menu-item>
const menus = [
  { path: '/dashboard', title: '仪表盘', icon: Odometer },
  { path: '/school', title: '院校管理', icon: School },
  { path: '/policy', title: '政策管理', icon: Document },
  { path: '/plan', title: '招生计划', icon: Tickets },
  { path: '/post', title: '帖子管理', icon: ChatDotRound },
  { path: '/user', title: '用户管理', icon: User },
  { path: '/major', title: '专业管理', icon: Reading }
]

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

// ===== 侧边栏折叠 / 移动端抽屉 =====
const isMobile = ref(window.innerWidth < 768)
const collapsed = ref(false)
const drawerOpen = ref(false)

const asideWidth = computed(() => {
  if (isMobile.value) return '220px'
  return collapsed.value ? '64px' : '220px'
})

const toggleAside = () => {
  if (isMobile.value) drawerOpen.value = !drawerOpen.value
  else collapsed.value = !collapsed.value
}

const onMenuSelect = () => {
  if (isMobile.value) drawerOpen.value = false
}

const onResize = () => {
  const mobile = window.innerWidth < 768
  if (mobile !== isMobile.value) {
    isMobile.value = mobile
    drawerOpen.value = false
  }
}

onMounted(() => window.addEventListener('resize', onResize))
onBeforeUnmount(() => window.removeEventListener('resize', onResize))

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {}
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.aside {
  background: #001529;
  overflow: hidden;
  transition: width 0.2s;
  z-index: 20;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: left;
  padding-left: 20px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
  white-space: nowrap;
  overflow: hidden;
}

.menu {
  border-right: none;
}
.menu:not(.el-menu--collapse) {
  width: 100%;
}

.body {
  min-width: 0;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 20px;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.collapse-btn {
  font-size: 18px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 4px;
  transition: background 0.2s;
  flex-shrink: 0;
}
.user-info:hover {
  background: #f5f7fa;
}
.nickname {
  font-size: 14px;
  color: #303133;
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 移动端：侧栏改成抽屉 */
.aside.is-mobile {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  transform: translateX(-100%);
  transition: transform 0.25s;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.2);
}
.aside.is-mobile.is-open {
  transform: translateX(0);
}
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 15;
}

/* 页面切换淡入，避免内容硬闪 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .main { padding: 12px; }
  .nickname { display: none; }
  .header { padding: 0 12px; }
}
</style>
