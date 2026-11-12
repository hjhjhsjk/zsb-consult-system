<template>
  <div class="user-page">
    <PageHeader title="用户管理" :subtitle="`共 ${list.length} 人`" />

    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="label">搜索：</span>
          <el-input
            v-model="keyword"
            placeholder="用户名 / 昵称 / 手机号"
            clearable
            style="width: 240px"
            :prefix-icon="Search"
          />
        </div>

        <div class="filter-item">
          <span class="label">角色：</span>
          <el-radio-group v-model="role">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="student">学生</el-radio-button>
            <el-radio-button value="admin">管理员</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="filteredList" stripe style="width: 100%">
        <el-table-column prop="id" label="UID" width="80" />

        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="34" :src="fixImg(row.avatar)">
                {{ (row.nickname || row.uname || 'U').charAt(0) }}
              </el-avatar>
              <div class="names">
                <span class="nickname">{{ row.nickname || '未设置昵称' }}</span>
                <span class="uname">@{{ row.uname }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" width="140" />

        <el-table-column prop="year" label="报考年份" width="100" />

        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.ustate === 3 ? 'danger' : 'info'" size="small">
              {{ row.ustate === 3 ? '管理员' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="create_time" label="注册时间" width="170" />

        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.ustate === 3 ? 'warning' : 'primary'"
              :disabled="row.id === userStore.uid"
              @click="handleToggleState(row)"
            >
              {{ row.ustate === 3 ? '降为学生' : '设为管理员' }}
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="row.id === userStore.uid"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无用户" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { fixImg } from '@/utils/image'
import { useUserStore } from '@/store/user'
import { getUsers, deleteUser, setUserState } from '@/api/user'

const userStore = useUserStore()

const loading = ref(false)
const list = ref([])
const keyword = ref('')
const role = ref('all')

// 用户量不大，筛选放前端，省一次接口
const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return list.value.filter(u => {
    if (role.value === 'admin' && u.ustate !== 3) return false
    if (role.value === 'student' && u.ustate === 3) return false
    if (!kw) return true
    return [u.uname, u.nickname, u.phone]
      .filter(Boolean)
      .some(v => String(v).toLowerCase().includes(kw))
  })
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

const handleToggleState = async (row) => {
  const toAdmin = row.ustate !== 3
  try {
    await ElMessageBox.confirm(
      toAdmin
        ? `确定将「${row.nickname || row.uname}」设为管理员吗？该用户将可以登录管理后台。`
        : `确定将「${row.nickname || row.uname}」降为学生吗？该用户将失去管理后台权限。`,
      '提示',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
  } catch { return }

  const res = await setUserState(row.id, toAdmin ? 3 : 0)
  if (res.code === 200) {
    ElMessage.success(res.data || (toAdmin ? '已设为管理员' : '已降为学生'))
    fetchList()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.nickname || row.uname}」吗？该用户的帖子会一并清理。`,
      '提示',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch { return }

  const res = await deleteUser(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  } else {
    ElMessage.error(res.msg || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
  border: none;
}
.filter-bar {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: center;
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
.table-card { border: none; }

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.names {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.nickname {
  font-size: 14px;
  color: #303133;
}
.uname {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .filter-bar { gap: 12px; }
}
</style>
