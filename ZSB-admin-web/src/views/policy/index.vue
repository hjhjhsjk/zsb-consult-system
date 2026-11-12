<template>
  <div class="policy-page">
    <PageHeader title="政策管理" :subtitle="`当前 ${list.length} 条`">
      <el-button type="primary" :icon="Plus" @click="openCreate">发布政策</el-button>
    </PageHeader>

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
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="label">发布方：</span>
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
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="list" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />

        <el-table-column label="标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.is_top === 1" type="danger" size="small" class="top-tag">置顶</el-tag>
            {{ row.title }}
          </template>
        </el-table-column>

        <el-table-column label="发布方" width="100">
          <template #default="{ row }">
            <el-tag :type="row.publisher === 1 ? 'success' : 'warning'" size="small">
              {{ row.publisher === 1 ? '政府' : '学校' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.category_id === 2 ? 'danger' : 'primary'" size="small">
              {{ row.category_id === 2 ? '免试' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="view_count" label="浏览量" width="90" />

        <el-table-column prop="create_time" label="创建时间" width="170" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无政策数据" />
        </template>
      </el-table>
    </el-card>

    <!-- 发布 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑政策' : '发布政策'"
      width="720px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入政策标题" />
        </el-form-item>

        <el-form-item label="所属省份" prop="province_id">
          <el-select v-model="form.province_id" placeholder="请选择省份" filterable style="width: 100%">
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布方" prop="publisher">
          <el-radio-group v-model="form.publisher">
            <el-radio :value="1">政府</el-radio>
            <el-radio :value="2">学校</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="政策类型" prop="category_id">
          <el-radio-group v-model="form.category_id">
            <el-radio :value="1">普通</el-radio>
            <el-radio :value="2">免试</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="置顶">
          <el-switch v-model="form.is_top" :active-value="1" :inactive-value="0" />
          <span class="hint">置顶后会排在列表最前面</span>
        </el-form-item>

        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            maxlength="20000"
            show-word-limit
            placeholder="支持多段文本，换行会原样保留"
          />
        </el-form-item>

        <el-form-item label="原文链接">
          <el-input v-model="form.url" maxlength="500" placeholder="选填，如 https://..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  getPolicies, getRegions, deletePolicy, addPolicy, updatePolicy
} from '@/api/user'

const loading = ref(false)
const list = ref([])
const provinces = ref([])

const filter = reactive({
  provinceId: 17,
  publisher: 1,
  type: 1
})

const fetchProvinces = async () => {
  try {
    const res = await getRegions(0)
    provinces.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPolicies({
      province_id: filter.provinceId,
      publisher: filter.publisher,
      type: filter.type
    })
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

// ================= 表单 =================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const emptyForm = () => ({
  id: null,
  title: '',
  content: '',
  publisher: filter.publisher,
  category_id: filter.type,
  province_id: filter.provinceId,
  is_top: 0,
  url: ''
})
const form = reactive(emptyForm())

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
  publisher: [{ required: true, message: '请选择发布方', trigger: 'change' }],
  category_id: [{ required: true, message: '请选择政策类型', trigger: 'change' }],
  province_id: [{ required: true, message: '请选择省份', trigger: 'change' }]
}

const openCreate = () => {
  Object.assign(form, emptyForm())
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    content: row.content,
    publisher: row.publisher,
    category_id: row.category_id,
    province_id: row.province_id,
    is_top: row.is_top ?? 0,
    url: row.url || ''
  })
  isEdit.value = true
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.clearValidate()
  Object.assign(form, emptyForm())
}

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      content: form.content,
      publisher: form.publisher,
      category_id: form.category_id,
      province_id: form.province_id,
      is_top: form.is_top,
      url: form.url.trim()
    }
    const res = isEdit.value
      ? await updatePolicy(form.id, payload)
      : await addPolicy(payload)

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      dialogVisible.value = false
      if (!isEdit.value) {
        // 切到刚发布的分类，避免“发布了却看不到”
        filter.provinceId = form.province_id
        filter.publisher = form.publisher
        filter.type = form.category_id
      }
      fetchList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '提示', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }

  const res = await deletePolicy(row.id)
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
.top-tag { margin-right: 6px; }
.hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .filter-bar { gap: 12px; }
}
</style>
