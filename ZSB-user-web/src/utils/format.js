/**
 * 时间格式化工具
 *
 * 后端返回的是 "yyyy-MM-dd HH:mm:ss" 字符串（Jackson 全局配置），
 * 直接 new Date('2026-11-09 12:00:00') 在 Safari / iOS 上会得到 Invalid Date，
 * 所以这里统一把空格换成 T 再解析，避免只在 Chrome 正常。
 */

function toDate(value) {
  if (!value) return null
  if (value instanceof Date) return value
  const d = new Date(String(value).replace(' ', 'T'))
  return isNaN(d.getTime()) ? null : d
}

/** 相对时间：刚刚 / 5 分钟前 / 3 小时前 / 昨天 / 3 天前 / 2026-10-01 */
export function timeAgo(value) {
  const d = toDate(value)
  if (!d) return ''
  const diff = Date.now() - d.getTime()
  const min = 60 * 1000
  const hour = 60 * min
  const day = 24 * hour

  if (diff < min) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / min)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 2 * day) return '昨天'
  if (diff < 7 * day) return `${Math.floor(diff / day)} 天前`

  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 完整时间：2026-11-09 12:00 */
export function formatTime(value) {
  const d = toDate(value)
  if (!d) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 数字缩写：1234 → 1.2k */
export function shortNumber(n) {
  const num = Number(n) || 0
  if (num < 1000) return String(num)
  if (num < 10000) return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  return (num / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
}
