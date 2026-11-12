<template>
  <div class="plan-page">
    <PageHeader title="招生计划" :subtitle="selectedSchoolName ? `当前：${selectedSchoolName}` : '先选学校再管理计划'">
      <el-button
        type="primary"
        :icon="Plus"
        :disabled="!filter.schoolId"
        @click="openCreate"
      >
        新增计划
      </el-button>
    </PageHeader>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="label">省份：</span>
          <el-select
            v-model="filter.provinceId"
            style="width: 140px"
            placeholder="省份"
            filterable
            @change="onProvinceChange"
          >
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="label">类型：</span>
          <el-radio-group v-model="filter.type" @change="loadSchools">
            <el-radio-button :value="1">公办</el-radio-button>
            <el-radio-button :value="2">民办</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <span class="label">学校：</span>
          <el-select
            v-model="filter.schoolId"
            style="width: 240px"
            placeholder="请选择学校"
            filterable
            :loading="schoolLoading"
            @change="loadPlans"
          >
            <el-option v-for="s in schools" :key="s.id" :label="s.school_name" :value="s.id" />
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="plans" stripe style="width: 100%">
        <el-table-column prop="year" label="年份" width="90" />
        <el-table-column prop="major_name" label="专业" min-width="220" show-overflow-tooltip />
        <el-table-column prop="plan_count" label="招生人数" width="110" />

        <el-table-column label="学费" width="120">
          <template #default="{ row }">{{ row.tuition_fee }} 元</template>
        </el-table-column>

        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.recruitment_type === 1 ? 'primary' : 'warning'" size="small">
              {{ row.recruitment_type === 1 ? '普通' : '免试' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="school_name" label="学校" min-width="180" show-overflow-tooltip />

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="filter.schoolId ? '该校暂无招生计划' : '请先选择学校'" />
        </template>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑招生计划' : '新增招生计划'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="学校">
          <el-input :model-value="selectedSchoolName" disabled />
        </el-form-item>

        <el-form-item label="专业" prop="major_id">
          <el-select
            v-model="form.major_id"
            placeholder="选择专业（也可手填专业名）"
            filterable
            clearable
            style="width: 100%"
            @change="onMajorChange"
          >
            <el-option v-for="m in majors" :key="m.id" :label="m.c_name" :value="m.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="专业名称" prop="major_name">
          <el-input v-model="form.major_name" maxlength="60" placeholder="如：计算机科学与技术" />
        </el-form-item>

        <el-form-item label="招生年份" prop="year">
          <el-input-number v-model="form.year" :min="2000" :max="2100" :step="1" style="width: 160px" />
        </el-form-item>

        <el-form-item label="招生人数" prop="plan_count">
          <el-input-number v-model="form.plan_count" :min="0" :max="9999" style="width: 160px" />
        </el-form-item>

        <el-form-item label="学费(元)" prop="tuition_fee">
          <el-input-number v-model="form.tuition_fee" :min="0" :max="999999" :step="100" style="width: 160px" />
        </el-form-item>

        <el-form-item label="计划类型" prop="recruitment_type">
          <el-radio-group v-model="form.recruitment_type">
            <el-radio :value="1">普通</el-radio>
            <el-radio :value="2">免试</el-radio>
          </el-radio-group>
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
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  getSchools, getRegions, getRecruitmentPlan, deletePlan,
  addPlan, updatePlan, listMajors
} from '@/api/user'

const provinces = ref([])
const schools = ref([])
const plans = ref([])
const majors = ref([])
const loading = ref(false)
const schoolLoading = ref(false)

const filter = reactive({
  provinceId: 17,
  type: 1,
  schoolId: null
})

const selectedSchoolName = computed(
  () => schools.value.find(s => s.id === filter.schoolId)?.school_name || ''
)

const fetchProvinces = async () => {
  try {
    const res = await getRegions(0)
    provinces.value = res.data || []
  } catch (e) {}
}

const onProvinceChange = () => {
  filter.schoolId = null
  plans.value = []
  loadSchools()
}

const loadSchools = async () => {
  schoolLoading.value = true
  filter.schoolId = null
  plans.value = []
  try {
    const res = await getSchools(filter.provinceId, filter.type)
    schools.value = res.data || []
  } catch (e) {
    schools.value = []
  } finally {
    schoolLoading.value = false
  }
}

const loadPlans = async () => {
  if (!filter.schoolId) {
    plans.value = []
    return
  }
  loading.value = true
  try {
    const res = await getRecruitmentPlan(filter.schoolId)
    plans.value = res.data || []
  } catch (e) {
    plans.value = []
  } finally {
    loading.value = false
  }
}

const loadMajors = async () => {
  try {
    const res = await listMajors()
    majors.value = res.data || []
  } catch (e) {
    majors.value = []
  }
}

// ================= 表单 =================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const emptyForm = () => ({
  id: null,
  school_id: filter.schoolId,
  major_id: null,
  major_name: '',
  year: new Date().getFullYear() + 1,
  plan_count: 0,
  tuition_fee: 0,
  recruitment_type: 1
})
const form = reactive(emptyForm())

const rules = {
  major_name: [{ required: true, message: '请选择或填写专业名称', trigger: 'blur' }],
  year: [{ required: true, message: '请填写招生年份', trigger: 'change' }],
  plan_count: [{ required: true, message: '请填写招生人数', trigger: 'change' }],
  tuition_fee: [{ required: true, message: '请填写学费', trigger: 'change' }]
}

const onMajorChange = (id) => {
  const m = majors.value.find(item => item.id === id)
  if (m) form.major_name = m.c_name
}

const openCreate = () => {
  Object.assign(form, emptyForm())
  form.school_id = filter.schoolId
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    school_id: row.school_id,
    major_id: row.major_id,
    major_name: row.major_name,
    year: row.year,
    plan_count: row.plan_count,
    tuition_fee: row.tuition_fee,
    recruitment_type: row.recruitment_type
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
    const school = schools.value.find(s => s.id === filter.schoolId)
    const payload = {
      school_id: filter.schoolId,
      // 冗余字段由后端补齐，这里带上也不影响
      school_name: school?.school_name || '',
      major_id: form.major_id,
      major_name: form.major_name.trim(),
      year: form.year,
      plan_count: form.plan_count,
      tuition_fee: form.tuition_fee,
      recruitment_type: form.recruitment_type
    }
    const res = isEdit.value
      ? await updatePlan(form.id, payload)
      : await addPlan(payload)

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadPlans()
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
    await ElMessageBox.confirm(`确定删除「${row.major_name}」这条招生计划吗？`, '提示', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }

  const res = await deletePlan(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadPlans()
  } else {
    ElMessage.error(res.msg || '删除失败')
  }
}

onMounted(async () => {
  await Promise.all([fetchProvinces(), loadMajors()])
  await loadSchools()
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

@media (max-width: 768px) {
  .filter-bar { gap: 12px; }
}
</style>
