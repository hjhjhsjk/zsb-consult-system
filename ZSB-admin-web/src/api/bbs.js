import request from './request'

// 帖子列表（按省，school_id=0 全省）
export function getPostList(provinceId, schoolId = 0) {
  return request.get(`/BBS/info/${provinceId}/${schoolId}`)
}

// 帖子详情
export function getPostDetail(uid, postId) {
  return request.get(`/BBS/GetPostDetail/${uid}/${postId}`)
}

// 获取评论
export function getComments(uid, postId) {
  return request.get(`/BBS/comments/${uid}/${postId}`)
}