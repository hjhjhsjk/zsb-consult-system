<template>
  <div class="user-page">
    <div class="user-card">
      <!-- 左侧：资料卡 -->
      <aside class="profile">
        <el-avatar :size="88" :src="fixImg(userInfo?.avatar)">
          {{ (userInfo?.nickname || 'U').charAt(0) }}
        </el-avatar>
        <h2 class="nickname">{{ userInfo?.nickname || '未设置昵称' }}</h2>
        <p class="uid">UID: {{ userInfo?.uid || '-' }}</p>

        <div class="info-list">
          <div class="info-item">
            <span class="label">报考年份</span>
            <span class="value">{{ userInfo?.year || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">所在地区</span>
            <span class="value">{{ areaText }}</span>
          </div>
          <div class="info-item">
            <span class="label">意向专业</span>
            <span class="value">{{ majorText }}</span>
          </div>
        </div>

        <div class="profile-actions">
          <el-button type="primary" plain @click="openEdit">编辑资料</el-button>
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </aside>

      <!-- 右侧：内容 -->
      <section class="content">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 我的收藏 -->
          <el-tab-pane label="我的收藏" name="favorite">
            <el-skeleton v-if="loadingFav" :rows="3" animated />
            <el-empty v-else-if="!favList.length" description="还没有收藏，去论坛或政策页看看" />

            <ul v-else class="item-list">
              <li v-for="item in favList" :key="`${item.target_type}-${item.id}`" @click="openFav(item)">
                <div class="item-main">
                  <el-tag size="small" :type="item.target_type === 2 ? 'warning' : 'primary'">
                    {{ item.target_type === 2 ? '政策' : '帖子' }}
                  </el-tag>
                  <p class="item-text">{{ item.title || item.content || '内容已删除' }}</p>
                </div>
                <span class="item-time">{{ item.create_time || item.post_time || '' }}</span>
              </li>
            </ul>
          </el-tab-pane>

          <!-- 我的发帖 -->
          <el-tab-pane label="我的发帖" name="posts">
            <el-skeleton v-if="loadingPosts" :rows="3" animated />
            <el-empty v-else-if="!myPosts.length" description="还没有发过帖子">
              <el-button type="primary" @click="$router.push('/bbs')">去论坛发帖</el-button>
            </el-empty>

            <ul v-else class="item-list">
              <li v-for="item in myPosts" :key="item.id" @click="goPost(item.id)">
                <div class="item-main">
                  <p class="item-text">{{ item.content }}</p>
                  <span class="item-sub">
                    <el-icon><View /></el-icon> {{ item.view_count }}
                    <el-icon><ChatDotRound /></el-icon> {{ item.post_count }}
                  </span>
                </div>
                <span class="item-time">{{ item.post_time }}</span>
              </li>
            </ul>
          </el-tab-pane>

          <!-- 浏览历史 -->
          <el-tab-pane label="浏览历史" name="history">
            <div class="tab-toolbar">
              <el-button v-if="historyList.length" link type="danger" @click="clearAllHistory">
                清空历史
              </el-button>
            </div>

            <el-empty v-if="!historyList.length" description="暂无浏览记录" />

            <ul v-else class="history-list">
              <li v-for="(item, i) in historyList" :key="i">
                <span class="type">{{ item.type }}</span>
                <span class="title" @click="$router.push(item.path)">{{ item.title }}</span>
                <span class="time">{{ item.time }}</span>
              </li>
            </ul>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>

    <!-- 编辑资料 -->
    <el-dialog
      v-model="editVisible"
      title="编辑资料"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="84px">
        <el-form-item label="头像">
          <div class="avatar-row">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <el-tooltip content="点击头像即可更换" placement="top">
                <el-avatar :size="60" :src="fixImg(form.avatar)" class="avatar-clickable">
                  {{ (form.nickname || 'U').charAt(0) }}
                </el-avatar>
              </el-tooltip>
            </el-upload>
            <div class="avatar-tips">
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button size="small" :loading="uploading">上传头像</el-button>
              </el-upload>
              <span class="avatar-hint">支持 JPG / PNG / WebP，不超过 5MB</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="20" show-word-limit placeholder="给自己起个昵称" />
        </el-form-item>

        <el-form-item label="报考年份" prop="year">
          <el-input-number v-model="form.year" :min="2000" :max="2100" style="width: 160px" />
        </el-form-item>

        <el-form-item label="所在地区" prop="regionArr">
          <el-cascader
            v-model="form.regionArr"
            :options="areaOptions"
            :show-all-levels="false"
            placeholder="选择省份 / 城市"
            clearable
            style="width: 100%"
            @expand-change="handleExpand"
          />
        </el-form-item>

        <el-form-item label="意向专业" prop="major_id">
          <el-select v-model="form.major_id" placeholder="选择专业" filterable clearable style="width: 100%">
            <el-option v-for="m in majors" :key="m.id" :label="m.c_name" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getMajors, updateUserInfo, uploadAvatar } from '@/api/user'
import { getFavorites, getMyPosts } from '@/api/bbs'
import { fixImg } from '@/utils/image'
import { fetchRegions, provinceName, cityName } from '@/utils/region'
import { readHistory, clearHistory } from '@/utils/history'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const activeTab = ref('favorite')

// ============ 地区 / 专业中文名 ============
const provinceText = ref('')
const cityText = ref('')
const majorText = ref('')

const areaText = computed(() => {
  if (!provinceText.value && !cityText.value) return '未设置'
  if (provinceText.value === cityText.value) return provinceText.value
  return [provinceText.value, cityText.value].filter(Boolean).join(' / ')
})

const loadNames = async () => {
  const u = userInfo.value
  if (!u) return

  try {
    const res = await getMajors()
    const found = (res.data || []).find(m => m.id === u.major)
    majorText.value = found ? found.c_name : ''
  } catch (e) {}

  provinceText.value = await provinceName(u.provinceId)
  cityText.value = await cityName(u.provinceId, u.cityId)
  if (u.cityId && u.cityId === u.provinceId) cityText.value = provinceText.value
}

// ============ 收藏 ============
const favList = ref([])
const loadingFav = ref(false)

const loadFavorites = async () => {
  if (!userStore.uid) return
  loadingFav.value = true
  try {
    // 帖子和政策分开查，合并后统一展示
    const [posts, policies] = await Promise.all([
      getFavorites(userStore.uid, 1),
      getFavorites(userStore.uid, 2)
    ])
    const list = [
      ...(posts.code === 200 ? posts.data || [] : []),
      ...(policies.code === 200 ? policies.data || [] : [])
    ]
    favList.value = list.sort((a, b) => {
      const ta = a.create_time || a.post_time || ''
      const tb = b.create_time || b.post_time || ''
      return tb.localeCompare(ta)
    })
  } catch (e) {
    favList.value = []
  } finally {
    loadingFav.value = false
  }
}

const openFav = (item) => {
  router.push(item.target_type === 2 ? `/consult/${item.id}` : `/bbs/${item.id}`)
}

// ============ 我的发帖 ============
const myPosts = ref([])
const loadingPosts = ref(false)

const loadMyPosts = async () => {
  if (!userStore.uid) return
  loadingPosts.value = true
  try {
    const res = await getMyPosts()
    myPosts.value = res.code === 200 ? (res.data || []) : []
  } catch (e) {
    myPosts.value = []
  } finally {
    loadingPosts.value = false
  }
}

const goPost = (id) => router.push(`/bbs/${id}`)

// ============ 浏览历史 ============
const historyList = ref([])
const loadHistory = () => { historyList.value = readHistory() }

const clearAllHistory = async () => {
  try {
    await ElMessageBox.confirm('确定清空全部浏览记录吗？', '提示', { type: 'warning' })
  } catch { return }
  clearHistory()
  historyList.value = []
  ElMessage.success('已清空')
}

// 切 Tab 时按需加载
const handleTabChange = (name) => {
  if (name === 'favorite' && !favList.value.length) loadFavorites()
  if (name === 'posts' && !myPosts.value.length) loadMyPosts()
  if (name === 'history') loadHistory()
}

// ============ 编辑资料 ============
const editVisible = ref(false)
const saving = ref(false)
const uploading = ref(false)
const formRef = ref()
const majors = ref([])
const areaOptions = ref([])

const form = reactive({
  nickname: '',
  avatar: '',
  year: null,
  major_id: null,
  regionArr: []
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称最多 20 个字', trigger: 'blur' }
  ]
}

const municipalities = [1, 2, 3, 4]

const buildAreaOptions = async () => {
  const provinces = await fetchRegions(0)
  areaOptions.value = provinces.map(p => ({
    value: p.id,
    label: p.name,
    leaf: municipalities.includes(p.id),
    children: municipalities.includes(p.id) ? undefined : []
  }))
}

const handleExpand = async (val) => {
  if (!val || val.length !== 1) return
  const province = areaOptions.value.find(p => p.value === val[0])
  if (!province || province.children?.length) return
  const cities = await fetchRegions(val[0])
  province.children = cities.map(c => ({ value: c.id, label: c.name }))
}

const openEdit = async () => {
  const u = userInfo.value || {}
  form.nickname = u.nickname || ''
  form.avatar = u.avatar || ''
  form.year = u.year || new Date().getFullYear() + 1
  form.major_id = u.major || null
  form.regionArr = u.provinceId
    ? (u.cityId && u.cityId !== u.provinceId ? [u.provinceId, u.cityId] : [u.provinceId])
    : []

  if (!areaOptions.value.length) await buildAreaOptions()
  if (form.regionArr.length) await handleExpand([form.regionArr[0]])

  if (!majors.value.length) {
    const res = await getMajors()
    majors.value = res.code === 200 ? (res.data || []) : []
  }

  editVisible.value = true
}

const beforeAvatarUpload = (file) => {
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

const handleAvatarUpload = async (options) => {
  uploading.value = true
  try {
    const res = await uploadAvatar(options.file)
    if (res.code === 200) {
      form.avatar = res.data
      ElMessage.success('头像已上传，保存后生效')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    // 错误已统一提示
  } finally {
    uploading.value = false
  }
}

const saveProfile = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const res = await updateUserInfo({
      nickname: form.nickname.trim(),
      avatar: form.avatar,
      year: form.year,
      major_id: form.major_id,
      region_id: form.regionArr.length ? form.regionArr[form.regionArr.length - 1] : null
    })

    if (res.code === 200) {
      ElMessage.success('保存成功')
      editVisible.value = false
      await userStore.fetchUserInfo()
      await loadNames()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    // 错误已统一提示
  } finally {
    saving.value = false
  }
}

// ============ 退出登录 ============
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

// ============ 初始化 ============
onMounted(async () => {
  if (!userInfo.value) await userStore.fetchUserInfo()
  await loadNames()
  loadHistory()
  await loadFavorites()
})
</script>

<style scoped>
.user-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 28px 20px;
  display: flex;
  justify-content: center;
}

.user-card {
  width: 1040px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  display: flex;
  overflow: hidden;
  min-height: 540px;
}

.profile {
  width: 300px;
  padding: 36px 26px;
  background: linear-gradient(160deg, #409eff 0%, #2c5aa0 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.nickname {
  margin: 16px 0 6px;
  font-size: 19px;
  font-weight: 600;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.uid {
  font-size: 12px;
  opacity: 0.75;
  margin: 0 0 26px;
}

.info-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  gap: 10px;
}
.info-item .label { opacity: 0.8; flex-shrink: 0; }
.info-item .value {
  font-weight: 500;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-actions {
  margin-top: auto;
  padding-top: 28px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.profile-actions .el-button {
  width: 100%;
  margin-left: 0;
}

.content {
  flex: 1;
  min-width: 0;
  padding: 24px 32px 30px;
}

.item-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.item-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 13px 14px;
  border-radius: 8px;
  margin-bottom: 10px;
  background: #f9fafb;
  cursor: pointer;
  transition: background 0.2s;
}
.item-list li:hover { background: #ecf5ff; }

.item-main { flex: 1; min-width: 0; }
.item-text {
  font-size: 14px;
  color: #303133;
  margin: 6px 0 0;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}
.item-sub {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
.item-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  white-space: nowrap;
}

.tab-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 6px;
}

.history-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.history-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
}
.history-list .type {
  color: #409eff;
  font-size: 12px;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}
.history-list .title {
  flex: 1;
  min-width: 0;
  cursor: pointer;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.history-list .title:hover { color: #409eff; }
.history-list .time { color: #909399; font-size: 12px; flex-shrink: 0; }

.avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.avatar-uploader :deep(.el-upload) { border-radius: 50%; }
.avatar-clickable {
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.avatar-clickable:hover {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.25);
  transform: scale(1.05);
}
.avatar-tips {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.avatar-hint {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 820px) {
  .user-page { padding: 16px 12px; }
  .user-card { flex-direction: column; min-height: 0; }
  .profile {
    width: 100%;
    padding: 24px 20px;
  }
  .profile-actions { padding-top: 20px; }
  .content { padding: 16px 16px 24px; }
  .item-list li { flex-direction: column; gap: 6px; }
  .item-time { align-self: flex-end; }
}
</style>
