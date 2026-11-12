<template>
  <div class="post-page">
    <PageHeader title="帖子管理" :subtitle="`共 ${list.length} 条`" />

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="label">省份：</span>
          <el-select
            v-model="filter.provinceId"
            style="width: 160px"
            placeholder="请选择省份"
            filterable
            @change="fetchList"
          >
            <el-option
              v-for="p in provinces"
              :key="p.id"
              :label="p.name"
              :value="p.id"
            />
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />

        <el-table-column label="内容" min-width="240">
          <template #default="{ row }">
            <span class="content-cell">{{ row.content }}</span>
          </template>
        </el-table-column>

        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <img
              v-if="row.image_url && !imgFailed[row.id]"
              :src="fixImg(row.image_url)"
              class="thumb"
              @error="imgFailed[row.id] = true"
            />
            <span v-else class="no-img">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="user_id" label="用户ID" width="90" />
        <el-table-column prop="view_count" label="浏览" width="80" />
        <el-table-column prop="post_time" label="发布时间" width="170" />

        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无帖子" />
        </template>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="帖子详情"
      width="600px"
    >
      <div v-loading="detailLoading" class="detail-box">
        <template v-if="detail">
          <div class="detail-row">
            <span class="label">作者：</span>
            <span>{{ detail.nickname || '匿名用户' }}（UID: {{ detail.user_id }}）</span>
          </div>
          <div class="detail-row">
            <span class="label">发布时间：</span>
            <span>{{ detail.post_time }}</span>
          </div>
          <div class="detail-row">
            <span class="label">浏览量：</span>
            <span>{{ detail.view_count }}</span>
          </div>
          <div class="detail-row">
            <span class="label">点赞 / 收藏：</span>
            <span>{{ detail.like_count || 0 }} / {{ detail.fav_count || 0 }}</span>
          </div>

          <el-divider />

          <div class="detail-content">{{ detail.content }}</div>

          <div v-if="detail.image_url" class="detail-image">
            <img :src="fixImg(detail.image_url)" alt="" />
          </div>
        </template>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { fixImg } from '@/utils/image'
import { getPostList, getPostDetail } from '@/api/bbs'
import { getRegions, deletePost } from '@/api/user'

const loading = ref(false)
const list = ref([])
const provinces = ref([])
const imgFailed = reactive({})

const filter = reactive({
  provinceId: 17
})

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

// 省份
const fetchProvinces = async () => {
  try {
    const res = await getRegions(0)
    provinces.value = res.data || []
  } catch (e) {}
}

// 列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPostList(filter.provinceId, 0)
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    // uid 传 0（后台不关心"我是否点赞"）
    const res = await getPostDetail(0, row.id)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 删除：软删除（后端把 status 改成 0，数据仍可追溯）
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除帖子 #${row.id} 吗？`, '提示', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }

  const res = await deletePost(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  } else {
    ElMessage.error(res.msg || '删除失败')
  }
}

onMounted(async () => {
  await fetchProvinces()
  await fetchList()
})
</script>

<style scoped>
.post-page { padding: 0; }

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

.content-cell {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}
.no-img { color: #c0c4cc; }

/* 详情弹窗 */
.detail-box {
  min-height: 100px;
}
.detail-row {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.detail-row .label {
  color: #909399;
  display: inline-block;
  width: 90px;
}
.detail-content {
  font-size: 15px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 16px;
}
.detail-image img {
  max-width: 100%;
  border-radius: 8px;
}
</style>