import { getRegions } from '@/api/user'

/**
 * 行政区划缓存
 *
 * 问题：导航栏、院校库、政策页、论坛、个人中心各自都会调 /user/regions，
 * 打开首页就要打 5 次同样的请求。
 * 做法：按 parentId 缓存 Promise（不是结果），并发调用也只会真正发一次。
 */
const cache = new Map()

export function fetchRegions(parentId = 0) {
  const key = Number(parentId) || 0
  if (!cache.has(key)) {
    const task = getRegions(key)
      .then(res => (res.code === 200 ? res.data || [] : []))
      .catch(() => [])
    cache.set(key, task)
    // 失败不缓存，下次还能重试
    task.then(list => { if (!list.length) cache.delete(key) })
  }
  return cache.get(key)
}

/** 省列表（带缓存） */
export const fetchProvinces = () => fetchRegions(0)

/** 省 id → 省名 */
export async function provinceName(id) {
  if (!id) return ''
  const list = await fetchProvinces()
  return list.find(p => p.id === id)?.name || ''
}

/** 市 id → 市名（直辖市直接返回省名） */
export async function cityName(provinceId, cityId) {
  if (!cityId) return ''
  if (cityId === provinceId) return provinceName(provinceId)
  const list = await fetchRegions(provinceId)
  return list.find(c => c.id === cityId)?.name || ''
}

/** 手动失效（管理端改了地区后一般也不需要，留着备用） */
export function clearRegionCache() {
  cache.clear()
}
