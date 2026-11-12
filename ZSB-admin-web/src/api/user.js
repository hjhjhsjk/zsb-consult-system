import request from './request'

// ============ 登录 / 用户 ============
// 登录（复用用户端接口，前端再校验 ustate === 3）
export function login(data) {
  return request.post('/user/login', data)
}

export function getUserInfo(uid) {
  return request.get(`/user/info/${uid}`)
}

// ============ 公共查询 ============
// 院校列表
export function getSchools(provinceId, type) {
  return request.get(`/home/Schools/${provinceId}/${type}`)
}

// 地区
export function getRegions(parentId = 0) {
  return request.get(`/user/regions/${parentId}`)
}

// 政策
export function getPolicies(params) {
  return request.get('/home/getPolicies', { params })
}

// 招生计划
export function getRecruitmentPlan(schoolId) {
  return request.get(`/home/getRecruitmentPlan/${schoolId}`)
}

// ============ 仪表盘 ============
export function getStats() {
  return request.get('/admin/stats')
}

// 通用图片上传（校徽、政策配图），返回 /uploads/... 相对地址
export function uploadImage(file, module = 'common') {
  const fd = new FormData()
  fd.append('file', file)
  return request.post(`/admin/upload?module=${module}`, fd)
}

// ============ 用户管理 ============
export function getUsers() {
  return request.get('/admin/user/list')
}

export function deleteUser(id) {
  return request.delete(`/admin/user/${id}`)
}

// 调整角色：ustate=3 设为管理员，ustate=0 降为学生
export function setUserState(id, ustate) {
  return request.put(`/admin/user/${id}/ustate`, { ustate })
}

// ============ 专业管理 ============
export function listMajors() {
  return request.get('/admin/major/list')
}

export function addMajor(data) {
  return request.post('/admin/major', data)
}

export function deleteMajor(id) {
  return request.delete(`/admin/major/${id}`)
}

// ============ 院校管理 ============
export function addSchool(data) {
  return request.post('/admin/school', data)
}

export function updateSchool(id, data) {
  return request.put(`/admin/school/${id}`, data)
}

export function deleteSchool(id) {
  return request.delete(`/admin/school/${id}`)
}

// ============ 政策管理 ============
export function addPolicy(data) {
  return request.post('/admin/policy', data)
}

export function updatePolicy(id, data) {
  return request.put(`/admin/policy/${id}`, data)
}

export function deletePolicy(id) {
  return request.delete(`/admin/policy/${id}`)
}

// ============ 招生计划管理 ============
export function listPlans(schoolId) {
  return request.get('/admin/plan/list', { params: { schoolId } })
}

export function addPlan(data) {
  return request.post('/admin/plan', data)
}

export function updatePlan(id, data) {
  return request.put(`/admin/plan/${id}`, data)
}

export function deletePlan(id) {
  return request.delete(`/admin/plan/${id}`)
}

// ============ 帖子管理 ============
export function deletePost(id) {
  return request.delete(`/admin/post/${id}`)
}
