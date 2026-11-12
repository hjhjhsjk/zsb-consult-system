<template>
  <div class="login-page">
    <div class="login-box">
      <!-- 左侧品牌区 -->
      <div class="brand">
        <h1>专升本咨询系统</h1>
        <p>一站式查询院校 · 专业 · 分数线</p>
        <ul>
          <li>✓ 全国院校专业数据</li>
          <li>✓ 历年分数线查询</li>
          <li>✓ 在线咨询答疑</li>
        </ul>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-area">
        <el-link class="back-home" underline="never" @click="$router.push('/')">
          ← 返回首页
        </el-link>
        <h2>欢迎登录</h2>
        <p class="sub">还没有账号？<router-link to="/register">立即注册</router-link></p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
              size="large"
            />
          </el-form-item>

          <div class="row-between">
            <el-checkbox v-model="form.remember">记住我</el-checkbox>
            <el-link type="primary" underline="never" @click="handleForgot">
              忘记密码？
            </el-link>
          </div>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: false
})

// ============ 验证规则 ============
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
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6~20 位', trigger: 'blur' }
  ]
}

// ============ 登录（真调后端） ============
const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    // store.login 内部：调 /user/login → 存 token/uid → 拉 /user/info/{uid}
    await userStore.login(form.username, form.password)

    if (form.remember) {
      localStorage.setItem('rememberUser', form.username)
    } else {
      localStorage.removeItem('rememberUser')
    }

    ElMessage.success('登录成功')

    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    ElMessage.error(e.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}

// ============ 忘记密码 ============
const handleForgot = () => {
  ElMessageBox.alert(
    `请联系管理员重置密码：

邮箱：admin@zsb.com
电话：138-0000-0000
工作时间：周一至周五 9:00-18:00`,
    '忘记密码',
    {
      confirmButtonText: '知道了',
      type: 'info',
      customClass: 'agreement-box'
    }
  )
}

// 记住的用户名回填
const remembered = localStorage.getItem('rememberUser')
if (remembered) {
  form.username = remembered
  form.remember = true
}

// 已登录就不用再看登录页
onMounted(() => {
  if (userStore.isLogin) router.replace('/')
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 900px;
  max-width: 100%;
  min-height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  overflow: hidden;
}

.brand {
  width: 45%;
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
  padding: 50px 45px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.form-area h2 { font-size: 24px; margin: 0 0 8px; color: #303133; }
.form-area .sub { font-size: 13px; color: #909399; margin-bottom: 28px; }
.form-area .sub a { color: #409eff; text-decoration: none; }
.back-home {
  align-self: flex-start;
  font-size: 13px;
  color: #909399;
  margin-bottom: 18px;
  cursor: pointer;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  letter-spacing: 4px;
}

:deep(.agreement-box .el-message-box__message) {
  white-space: pre-line;
  line-height: 1.8;
  text-align: left;
}

@media (max-width: 768px) {
  .brand { display: none; }
  .form-area { padding: 40px 30px; }
}
</style>