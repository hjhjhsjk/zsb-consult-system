import request from './request'

// 获取地区：parentId=0 拿省份，传省 id 拿市
export function getRegions(parentId = 0) {
  return request.get(`/user/regions/${parentId}`)
}

// 获取专业大类
export function getMajors() {
  return request.get('/user/getMajor')
}

// 注册
// data: { uname, pwd, nickname, avatar, major, year, regions, phone }
export function register(data) {
  return request.post('/user/reg', data)
}

// 注册页头像上传：游客可用（无需登录），返回 /uploads/avatar/... 路径
export function uploadRegAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/reg/avatar', fd)
}

// 登录
// data: { uname, pwd }
export function login(data) {
  return request.post('/user/login', data)
}

// 获取用户信息
export function getUserInfo(uid) {
  return request.get(`/user/info/${uid}`)
}

// 修改个人信息（昵称/头像/专业/年份/地区）
// data: { nickname, avatar, major_id, year, region_id }
export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

// 上传头像，返回 /uploads/avatar/... 相对地址
export function uploadAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/avatar', fd)
}