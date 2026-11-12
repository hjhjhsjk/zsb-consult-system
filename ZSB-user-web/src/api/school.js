import request from './request'

/**
 * 获取学校列表
 * @param {number} provinceId  省份 id
 * @param {number} type        1公办 2民办
 */
export function getSchools(provinceId, type) {
  return request.get(`/home/Schools/${provinceId}/${type}`)
}

/** 院校详情（刷新页面时不再依赖 sessionStorage） */
export function getSchoolDetail(id) {
  return request.get(`/home/schools/${id}`)
}

/**
 * 获取学校招生计划（详情页用）
 * @param {number} schoolId  学校 id
 */
export function getRecruitmentPlan(schoolId) {
  return request.get(`/home/getRecruitmentPlan/${schoolId}`)
}