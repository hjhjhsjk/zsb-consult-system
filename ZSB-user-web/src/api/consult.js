import request from './request'

/** 政策详情（按 id 查，刷新不丢）；uid 用来带出「是否已收藏」 */
export function getPolicyDetail(id, uid = 0) {
  return request.get(`/home/getPolicies/${id}`, { params: { uid } })
}

/**
 * 获取地区政策
 * @param {Object} params
 * @param {number} params.province_id  省份 id
 * @param {number} params.publisher    1政府 2学校
 * @param {number} params.type         1普通 2免试
 */
export function getPolicies(params) {
  return request.get('/home/getPolicies', { params })
}

/**
 * 获取地区
 * @param {number} parentId  0 拿省份，省 id 拿市
 */
export function getRegions(parentId) {
  return request.get(`/user/regions/${parentId}`)
}