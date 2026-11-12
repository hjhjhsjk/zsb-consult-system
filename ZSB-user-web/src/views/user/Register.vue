<template>
  <div class="register-page">
    <div class="register-box">
      <!-- 左侧品牌区 -->
      <div class="brand">
        <h1>加入我们</h1>
        <p>注册账号，开启你的升本之旅</p>
        <ul>
          <li>✓ 收藏心仪院校</li>
          <li>✓ 在线提问咨询</li>
          <li>✓ 分数线对比分析</li>
        </ul>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-area">
        <div class="form-header">
          <h2>注册账号</h2>
          <span class="sub">已有账号？<router-link to="/login">立即登录</router-link></span>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <!-- 头像（可选）：选图即上传并预览，可移除，可跳过 -->
          <el-form-item label="头像（可选）">
            <div class="avatar-field">
              <el-upload
                class="reg-avatar-up"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-badge :is-dot="!!avatarUrl" class="avatar-badge">
                  <el-avatar :size="64" :src="fixImg(avatarUrl)" class="reg-avatar">
                    <el-icon v-if="!uploading"><Plus /></el-icon>
                    <el-icon v-else class="is-loading"><Loading /></el-icon>
                  </el-avatar>
                </el-badge>
              </el-upload>
              <div class="avatar-meta">
                <span class="avatar-title">
                  {{ uploading ? '正在上传…' : (avatarUrl ? '已选择头像' : '点击选择头像') }}
                </span>
                <span class="avatar-sub">选填，支持 JPG / PNG / WebP，不超过 5MB</span>
                <el-link
                  v-if="avatarUrl"
                  type="danger"
                  :underline="false"
                  class="avatar-remove"
                  @click="removeAvatar"
                >
                  移除，使用默认头像
                </el-link>
              </div>
            </div>
          </el-form-item>

          <!-- 第一行：用户名 -->
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="3~20位，字母/数字/中文"
              :prefix-icon="User"
              clearable
              size="large"
            />
          </el-form-item>

          <!-- 第二行：手机号 + 地区 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="form.phone"
                  placeholder="请输入手机号"
                  :prefix-icon="Iphone"
                  maxlength="11"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所在地区" prop="area">
                <el-cascader
                  v-model="form.area"
                  :props="areaProps"
                  :show-all-levels="false"
                  placeholder="请选择地区"
                  clearable
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 第三行：报考年份 + 意向专业 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="报考年份" prop="year">
                <el-select v-model="form.year" placeholder="请选择报考年份" style="width: 100%">
                  <el-option
                    v-for="y in yearOptions"
                    :key="y"
                    :label="y + ' 年'"
                    :value="y"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="意向专业" prop="major">
                <el-select
                  v-model="form.major"
                  placeholder="请选择意向专业"
                  filterable
                  style="width: 100%"
                >
                  <el-option
                    v-for="m in majorOptions"
                    :key="m.value"
                    :label="m.label"
                    :value="m.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 第四行：密码 + 确认密码 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="6~20位，字母+数字"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                />
                <div v-if="form.password" class="strength">
                  <div class="bars">
                    <span
                      v-for="i in 3"
                      :key="i"
                      :class="['bar', strengthClass(i)]"
                    ></span>
                  </div>
                  <span class="strength-text" :class="strengthClass(0)">
                    {{ strengthText }}
                  </span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item prop="agree">
            <el-checkbox v-model="form.agree">
              我已阅读并同意
              <el-link type="primary" underline="never" @click.prevent="showAgreement">
                《用户协议》
              </el-link>
              和
              <el-link type="primary" underline="never" @click.prevent="showPrivacy">
                《隐私政策》
              </el-link>
            </el-checkbox>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Iphone, Plus, Loading } from '@element-plus/icons-vue'
import { getRegions, getMajors, register, uploadRegAvatar } from '@/api/user'
import { fixImg } from '@/utils/image'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

// ========== 头像（可选） ==========
const avatarUrl = ref('')     // 上传成功后的 /uploads/... 相对路径
const uploading = ref(false)

const beforeAvatarUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
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
    const res = await uploadRegAvatar(options.file)
    if (res.code === 200 && res.data) {
      avatarUrl.value = res.data
      ElMessage.success('头像已就绪，注册后生效')
    } else {
      ElMessage.error(res.msg || '上传失败，可跳过或重试')
    }
  } catch (e) {
    // 失败不阻断注册：清空预览，用户可重试或跳过
    avatarUrl.value = ''
  } finally {
    uploading.value = false
  }
}

const removeAvatar = () => {
  avatarUrl.value = ''
}

// ========== 报考年份选项 ==========
const currentYear = new Date().getFullYear()
const yearOptions = []
for (let y = currentYear; y <= currentYear + 4; y++) {
  yearOptions.push(y)
}

// ========== 地区（Element Plus 官方懒加载） ==========
const areaProps = {
  lazy: true,
  lazyLoad: async (node, resolve) => {
    const { level, value } = node
    const parentId = level === 0 ? 0 : value

    // 直辖市 id：1北京 2天津 3上海 4重庆
    const municipalities = [1, 2, 3, 4]

    try {
      const res = await getRegions(parentId)
      if (res.code === 200) {
        const list = res.data.map(item => ({
          value: item.id,
          label: item.name,
          // 根级：直辖市直接是叶子；其他省可展开
          // 市一级：都是叶子
          leaf: level === 0 ? municipalities.includes(item.id) : true
        }))
        resolve(list)
      } else {
        resolve([])
      }
    } catch (e) {
      resolve([])
    }
  }
}

// ========== 专业（动态加载） ==========
const majorOptions = ref([])

const loadMajors = async () => {
  const res = await getMajors()
  if (res.code === 200) {
    majorOptions.value = res.data.map(m => ({
      value: m.id,
      label: m.c_name
    }))
  }
}

// ========== 表单 ==========
const form = reactive({
  username: '',
  phone: '',
  area: [],
  year: '',
  major: '',
  password: '',
  confirmPassword: '',
  agree: false
})

// ========== 自定义校验 ==========
const validateConfirm = (rule, value, callback) => {
  if (!value) return callback(new Error('请再次输入密码'))
  if (value !== form.password) return callback(new Error('两次输入的密码不一致'))
  callback()
}
const validateAgree = (rule, value, callback) => {
  if (!value) return callback(new Error('请先阅读并同意用户协议'))
  callback()
}

// ========== 验证规则 ==========
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3~20 位', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
      message: '只能包含字母、数字、下划线或中文',
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  area: [
    { required: true, type: 'array', message: '请选择所在地区', trigger: 'change' }
  ],
  year: [
    { required: true, message: '请选择报考年份', trigger: 'change' }
  ],
  major: [
    { required: true, message: '请选择意向专业', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6~20 位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && !/^[a-zA-Z0-9!@#$%^&*_]+$/.test(value)) {
          return callback(new Error('密码只能包含字母、数字和常用符号'))
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ],
  agree: [
    { validator: validateAgree, trigger: 'change' }
  ]
}

// ========== 密码强度 ==========
const strengthLevel = computed(() => {
  const p = form.password
  if (!p) return 0
  let score = 0
  if (p.length >= 6) score++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) score++
  if (/\d/.test(p)) score++
  if (/[!@#$%^&*_]/.test(p)) score++
  if (score <= 1) return 1
  if (score <= 2) return 2
  return 3
})
const strengthText = computed(() => ['', '弱', '中', '强'][strengthLevel.value])
const strengthClass = (i) => {
  const cls = ['', 'weak', 'medium', 'strong'][strengthLevel.value]
  if (i === 0) return cls
  return i <= strengthLevel.value ? cls : ''
}

// ========== 协议弹窗 ==========
const showAgreement = () => {
  ElMessageBox.alert(
    `1. 本系统仅供专升本学生查询院校、专业、分数线等信息使用。
2. 用户注册时需保证信息真实有效。
3. 用户不得利用本系统从事违法违规活动。
4. 系统内数据仅供参考，最终以官方公布为准。
5. 用户账号仅限本人使用，不得转借、出售。`,
    '用户协议',
    { confirmButtonText: '我知道了', customClass: 'agreement-box' }
  )
}

const showPrivacy = () => {
  ElMessageBox.alert(
    `1. 我们仅收集必要的注册信息（用户名、手机号、地区等）。
2. 收集的信息仅用于提供专升本查询与咨询服务。
3. 我们不会向任何第三方泄露您的个人信息。
4. 您可以随时申请删除账号及全部数据。
5. 使用本系统即表示您同意本隐私政策。`,
    '隐私政策',
    { confirmButtonText: '我知道了', customClass: 'agreement-box' }
  )
}

// ========== 注册 ==========
const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const payload = {
      uname: form.username,
      pwd: form.password,
      nickname: form.username,
      avatar: avatarUrl.value || '',
      major: form.major,
      year: form.year,
      // 取数组最后一个：湖北武汉 [17,1301] → 1301；重庆 [4] → 4
      regions: form.area[form.area.length - 1],
     phone: Number(form.phone),   // ← 转数字
    }

    const res = await register(payload)

    if (res.code !== 200) {
      throw new Error(res.msg || '注册失败')
    }

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}

// ========== 初始化 ==========
onMounted(() => {
  loadMajors()
})
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  width: 1000px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  overflow: hidden;
}

.brand {
  width: 38%;
  background: linear-gradient(135deg, #409eff 0%, #2c5aa0 100%);
  color: #fff;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand h1 { font-size: 28px; margin: 0 0 12px; }
.brand p { font-size: 14px; opacity: 0.9; margin-bottom: 32px; }
.brand ul { list-style: none; padding: 0; margin: 0; }
.brand li { font-size: 14px; padding: 8px 0; opacity: 0.95; }

.form-area {
  flex: 1;
  padding: 40px 45px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 24px;
}
.form-area h2 { font-size: 24px; margin: 0; color: #303133; }
.form-area .sub { font-size: 13px; color: #909399; }
.form-area .sub a { color: #409eff; text-decoration: none; }

.form-area :deep(.el-form-item) {
  margin-bottom: 18px;
}

.avatar-field {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
}
.avatar-badge :deep(.el-badge__content.is-dot) {
  background: #67c23a;
  border: none;
}
.reg-avatar {
  cursor: pointer;
  background: #f0f3f8;
  color: #909399;
  border: 1.5px dashed #c0c8d4;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}
.reg-avatar:hover {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
  transform: scale(1.05);
}
.avatar-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.avatar-title { font-size: 14px; color: #303133; }
.avatar-sub { font-size: 12px; color: #909399; }
.avatar-remove { font-size: 12px; align-self: flex-start; height: auto; }

.submit-btn { width: 100%; letter-spacing: 4px; margin-top: 4px; }

.strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  width: 100%;
}
.bars { display: flex; gap: 4px; flex: 1; }
.bar {
  flex: 1;
  height: 5px;
  border-radius: 3px;
  background: #ebeef5;
  transition: background 0.3s;
}
.bar.weak { background: #f56c6c; }
.bar.medium { background: #e6a23c; }
.bar.strong { background: #67c23a; }
.strength-text { font-size: 12px; }
.strength-text.weak { color: #f56c6c; }
.strength-text.medium { color: #e6a23c; }
.strength-text.strong { color: #67c23a; }

:deep(.agreement-box .el-message-box__message) {
  white-space: pre-line;
  line-height: 1.8;
  text-align: left;
}

@media (max-width: 768px) {
  .brand { display: none; }
  .form-area { padding: 30px 20px; }
}
</style>