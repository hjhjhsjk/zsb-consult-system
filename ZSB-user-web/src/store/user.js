import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const uid = ref(Number(localStorage.getItem('uid')) || null)
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const prefs = ref(JSON.parse(localStorage.getItem('userPrefs') || '{}'))

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  // ============ 登录：真调后端 ============
  async function login(username, password) {
    const res = await loginApi({ uname: username, pwd: password })

    if (res.code !== 200) {
      throw new Error(res.msg || '登录失败')
    }

    token.value = res.data.token
    uid.value = res.data.uid
    localStorage.setItem('token', token.value)
    localStorage.setItem('uid', uid.value)

    // 登录后拉用户信息
    await fetchUserInfo()

    return userInfo.value
  }

  // ============ 拉用户信息 ============
  async function fetchUserInfo() {
    if (!uid.value) return
    const res = await getUserInfo(uid.value)
    if (res.code === 200) {
      userInfo.value = {
        uid: res.data.uid,
        nickname: res.data.nickname,
        avatar: res.data.avatar,
        provinceId: res.data.province_id,
        cityId: res.data.city_id,
        major: res.data.major,
        year: res.data.year,
        role: 'student'
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

      // ★ 把地区写进 prefs，供导航栏用
      if (res.data.province_id) {
        const areaArr = (res.data.city_id && res.data.city_id !== res.data.province_id)
          ? [res.data.province_id, res.data.city_id]
          : [res.data.province_id]
        setPrefs({ area: areaArr })
      }
    }
  }

  // ============ 偏好 ============
  function setPrefs(data) {
    prefs.value = { ...prefs.value, ...data }
    localStorage.setItem('userPrefs', JSON.stringify(prefs.value))
  }
  function setArea(area) {
    setPrefs({ area })
  }

  // ============ 退出 ============
  function logout() {
    token.value = ''
    uid.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('uid')
    localStorage.removeItem('userInfo')
    // prefs 保留
  }

  return {
    token, uid, userInfo, prefs,
    isLogin, isAdmin,
    login, fetchUserInfo, setPrefs, setArea, logout
  }
})