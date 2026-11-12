<template>
  <div class="school-page">
    <PageHeader title="院校管理" :subtitle="`共 ${list.length} 所`">
      <el-button type="primary" :icon="Plus" @click="openCreate">新增院校</el-button>
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
          <span class="label">类型：</span>
          <el-radio-group v-model="filter.type" @change="fetchList">
            <el-radio-button :value="1">公办</el-radio-button>
            <el-radio-button :value="2">民办</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <span class="label">名称：</span>
          <el-input
            v-model="filter.keyword"
            placeholder="输入院校名筛选"
            clearable
            style="width: 200px"
            :prefix-icon="Search"
          />
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="filteredList" stripe style="width: 100%">
        <el-table-column label="Logo" width="90">
          <template #default="{ row }">
            <SchoolLogo :src="row.logo_url" :name="row.school_name" :size="40" />
          </template>
        </el-table-column>

        <el-table-column prop="school_name" label="学校名称" min-width="200" show-overflow-tooltip />

        <el-table-column prop="city_name" label="城市" width="120" />

        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.school_type === 1 ? 'success' : 'warning'" size="small">
              {{ row.school_type === 1 ? '公办' : '民办' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无院校数据" />
        </template>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑院校' : '新增院校'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="院校名称" prop="school_name">
          <el-input v-model="form.school_name" maxlength="60" placeholder="如：武汉纺织大学" />
        </el-form-item>

        <el-form-item label="校徽">
          <div class="logo-upload">
            <SchoolLogo :src="form.logo_url" :name="form.school_name" :size="64" />
            <el-upload
              :show-file-list="false"
              :before-upload="beforeLogoUpload"
              :http-request="handleLogoUpload"
              accept="image/*"
            >
              <el-button :loading="uploading" size="small">上传图片</el-button>
            </el-upload>
            <el-input
              v-model="form.logo_url"
              placeholder="也可直接填图片地址"
              clearable
              size="small"
              class="logo-input"
            />
          </div>
        </el-form-item>

        <el-form-item label="所属省份" prop="province_id">
          <el-select
            v-model="form.province_id"
            placeholder="请选择省份"
            filterable
            style="width: 100%"
            @change="onFormProvinceChange"
          >
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属城市" prop="city_id">
          <el-select
            v-model="form.city_id"
            placeholder="请选择城市（直辖市选本省即可）"
            filterable
            :loading="cityLoading"
            style="width: 100%"
          >
            <el-option v-for="c in formCities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="城市名称" prop="city_name">
          <el-input v-model="form.city_name" maxlength="30" placeholder="选城市后自动带出，可改" />
        </el-form-item>

        <el-form-item label="办学类型" prop="school_type">
          <el-radio-group v-model="form.school_type">
            <el-radio :value="1">公办</el-radio>
            <el-radio :value="2">民办</el-radio>
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import SchoolLogo from '@/components/SchoolLogo.vue'
import {
  getSchools, getRegions, deleteSchool, addSchool, updateSchool, uploadImage
} from '@/api/user'

const loading = ref(false)
const list = ref([])
const provinces = ref([])

const filter = reactive({
  provinceId: 17,   // 默认湖北
  type: 1,
  keyword: ''
})

// 名称筛选放前端：一个省的公办院校最多几十条，没必要多打一次接口
const filteredList = computed(() => {
  const kw = filter.keyword.trim()
  if (!kw) return list.value
  return list.value.filter(s => (s.school_name || '').includes(kw))
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
    const res = await getSchools(filter.provinceId, filter.type)
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
const uploading = ref(false)
const cityLoading = ref(false)
const formRef = ref()
const formCities = ref([])

const emptyForm = () => ({
  id: null,
  school_name: '',
  logo_url: '',
  city_name: '',
  school_type: 1,
  province_id: filter.provinceId,
  city_id: null
})
const form = reactive(emptyForm())

const rules = {
  school_name: [{ required: true, message: '请输入院校名称', trigger: 'blur' }],
  province_id: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city_id: [{ required: true, message: '请选择城市', trigger: 'change' }],
  city_name: [{ required: true, message: '请填写城市名称', trigger: 'blur' }],
  school_type: [{ required: true, message: '请选择办学类型', trigger: 'change' }]
}

const loadFormCities = async (provinceId) => {
  formCities.value = []
  if (!provinceId) return
  cityLoading.value = true
  try {
    const res = await getRegions(provinceId)
    const cities = res.data || []
    // 直辖市没有下级：把省自己当成城市，保证 city_id 有值
    formCities.value = cities.length
      ? cities
      : provinces.value.filter(p => p.id === provinceId)
  } catch (e) {
    formCities.value = []
  } finally {
    cityLoading.value = false
  }
}

const onFormProvinceChange = async (provinceId) => {
  form.city_id = null
  form.city_name = ''
  await loadFormCities(provinceId)
}

// 选城市 → 自动带出城市名
const syncCityName = () => {
  const city = formCities.value.find(c => c.id === form.city_id)
  if (city && !form.city_name) form.city_name = city.name
}

// 下拉一改就同步，避免用户忘了点「城市名称」
watch(() => form.city_id, () => {
  const city = formCities.value.find(c => c.id === form.city_id)
  if (city) form.city_name = city.name
})

const openCreate = async () => {
  Object.assign(form, emptyForm())
  isEdit.value = false
  dialogVisible.value = true
  await loadFormCities(form.province_id)
}

const openEdit = async (row) => {
  Object.assign(form, {
    id: row.id,
    school_name: row.school_name,
    logo_url: row.logo_url || '',
    city_name: row.city_name || '',
    school_type: row.school_type,
    province_id: row.province_id,
    city_id: row.city_id || null
  })
  isEdit.value = true
  dialogVisible.value = true
  await loadFormCities(row.province_id)
}

const resetForm = () => {
  formRef.value?.clearValidate()
  Object.assign(form, emptyForm())
  formCities.value = []
}

const beforeLogoUpload = (file) => {
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

const handleLogoUpload = async (options) => {
  uploading.value = true
  try {
    const res = await uploadImage(options.file, 'school')
    if (res.code === 200) {
      form.logo_url = res.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 城市名没手动改就用下拉里选中的
  syncCityName()

  submitting.value = true
  try {
    const payload = {
      school_name: form.school_name.trim(),
      logo_url: form.logo_url,
      city_name: form.city_name,
      school_type: form.school_type,
      province_id: form.province_id,
      city_id: form.city_id
    }
    const res = isEdit.value
      ? await updateSchool(form.id, payload)
      : await addSchool(payload)

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      // 新增后如果不在当前筛选条件里，自动切过去，避免“保存了但看不见”
      if (!isEdit.value) {
        filter.provinceId = form.province_id
        filter.type = form.school_type
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
    await ElMessageBox.confirm(
      `确定删除「${row.school_name}」吗？该校的招生计划会一起删除。`,
      '提示',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch { return }

  const res = await deleteSchool(row.id)
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
.table-card {
  border: none;
}

.logo-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}
.logo-input {
  flex: 1;
  min-width: 180px;
}

@media (max-width: 768px) {
  .filter-bar { gap: 12px; }
}
</style>
