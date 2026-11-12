import request from './request'

// 搜索招生计划
// params: { keyword, region_id }
export function searchPlans(params) {
  return request.get('/home/search', { params })
}