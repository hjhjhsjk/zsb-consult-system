import request from './request'

// ============ 帖子 ============

// 帖子列表（按省，school_id 传 0 表示全省）
export function getPostList(provinceId, schoolId = 0) {
  return request.get(`/BBS/info/${provinceId}/${schoolId}`)
}

// 首页「最新讨论」：跨省份按时间倒序
export function getLatestPosts(limit = 6) {
  return request.get('/BBS/latest', { params: { limit } })
}

// 帖子详情
export function getPostDetail(uid, postId) {
  return request.get(`/BBS/GetPostDetail/${uid}/${postId}`)
}

// 发帖（form-data）
export function insertPost(formData) {
  return request.post('/BBS/insert', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ============ 评论 ============

// 获取评论
export function getComments(uid, postId) {
  return request.get(`/BBS/comments/${uid}/${postId}`)
}

// 发表评论 body: { post_id, content }
export function createComment(data) {
  return request.post('/BBS/comments', data)
}

// 评论点赞 / 取消
export function likeComment(uid, commentId) {
  return request.post(`/BBS/like_comment/${uid}/${commentId}`)
}
export function unlikeComment(uid, commentId) {
  return request.post(`/BBS/unlike_comment/${uid}/${commentId}`)
}

// ============ 帖子点赞 / 收藏 ============

// 帖子点赞 / 取消
export function likePost(uid, postId) {
  return request.post(`/BBS/like_post/${uid}/${postId}`)
}
export function unlikePost(uid, postId) {
  return request.post(`/BBS/unlike_post/${uid}/${postId}`)
}

// 收藏 / 取消（target_type: 1帖子 2政策）
export function addFavorite(uid, targetType, targetId) {
  return request.post(`/BBS/add_favorite/${uid}/${targetType}/${targetId}`)
}
export function clearFavorite(uid, targetType, targetId) {
  return request.post(`/BBS/clear_favorite/${uid}/${targetType}/${targetId}`)
}

// 获取收藏夹（type: 1帖子 2政策）
export function getFavorites(uid, type) {
  return request.get(`/BBS/get_favfavorite/${uid}/${type}`)
}

// 我的发帖（uid 由后端从 token 解析，不用传）
export function getMyPosts() {
  return request.get('/BBS/myPosts')
}