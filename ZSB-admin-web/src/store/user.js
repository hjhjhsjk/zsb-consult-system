import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // token 统一存在 localStorage 的 token 键：
  // request.js 的请求拦截器就是读这个键，之前存 admin_token 导致管理端所有请求都不带鉴权头
  const token = ref(localStorage.getItem('token') || '')
  const uid = ref(Number(localStorage.getItem('uid')) || null)
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_userInfo') || 'null'))

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.ustate === 3) // 3=管理员

  // 真实登录：走 /user/login，再校验 ustate 是否为管理员
  async function login(username, password) {
    const res = await loginApi({ uname: username, pwd: password })
    if (res.code !== 200) {
      throw new Error(res.msg || '登录失败')
    }
    if (res.data.ustate !== 3) {
      throw new Error('该账号不是管理员')
    }

    token.value = res.data.token
    uid.value = res.data.uid
    userInfo.value = {
      uid: res.data.uid,
      nickname: res.data.nickname || '管理员',
      avatar: '',
      ustate: res.data.ustate
    }

    localStorage.setItem('token', token.value)
    localStorage.setItem('uid', String(uid.value))
    localStorage.setItem('admin_userInfo', JSON.stringify(userInfo.value))
    return userInfo.value
  }

  async function fetchUserInfo() {
    if (!uid.value) return
    const res = await getUserInfo(uid.value)
    if (res.code === 200) {
      userInfo.value = {
        uid: res.data.uid,
        nickname: res.data.nickname,
        avatar: res.data.avatar,
        ustate: res.data.ustate
      }
      localStorage.setItem('admin_userInfo', JSON.stringify(userInfo.value))
    }
  }

  function logout() {
    token.value = ''
    uid.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('uid')
    localStorage.removeItem('admin_userInfo')
  }

  return { token, uid, userInfo, isLogin, isAdmin, login, fetchUserInfo, logout }
})
