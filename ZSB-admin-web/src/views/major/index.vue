<template>
  <div class="major-page">
    <PageHeader title="专业管理" :subtitle="`共 ${list.length} 个专业`">
      <el-button type="primary" :icon="Plus" @click="openCreate">新增专业</el-button>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="label">搜索：</span>
          <el-input
            v-model="keyword"
            placeholder="专业名称"
            clearable
            style="width: 240px"
            :prefix-icon="Search"
          />
        </div>
        <span class="tip">专业字典供注册页与招生计划共用，删除前请确认没有被计划引用。</span>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="filteredList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="c_name" label="专业名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="100" />

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无专业" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="新增专业"
      width="460px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="专业名称" prop="c_name">
          <el-input v-model="form.c_name" maxlength="60" placeholder="如：计算机科学与技术" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="hint">数字越小越靠前</span>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { listMajors, addMajor, deleteMajor } from '@/api/user'

const loading = ref(false)
const list = ref([])
const keyword = ref('')

const filteredList = computed(() => {
  const kw = keyword.value.trim()
  if (!kw) return list.value
  return list.value.filter(m => (m.c_name || '').includes(kw))
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listMajors()
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ c_name: '', sort: 0 })

const rules = {
  c_name: [{ required: true, message: '请输入专业名称', trigger: 'blur' }]
}

const openCreate = () => {
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.clearValidate()
  Object.assign(form, { c_name: '', sort: 0 })
}

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await addMajor({ c_name: form.c_name.trim(), sort: form.sort })
    if (res.code === 200) {
      ElMessage.success('新增成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  } catch (e) {
    ElMessage.error('新增失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除专业「${row.c_name}」吗？`, '提示', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }

  const res = await deleteMajor(row.id)
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
  gap: 16px;
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
.tip {
  font-size: 12px;
  color: #909399;
}
.table-card { border: none; }
.hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
