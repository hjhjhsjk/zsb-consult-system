/**
 * 浏览历史：只存在本地，不上报后端。
 * 结构 { type, title, path, time }，最多保留 50 条，按路径去重后置顶。
 */
const KEY = 'viewHistory'
const MAX = 50

export function readHistory() {
  try {
    const list = JSON.parse(localStorage.getItem(KEY) || '[]')
    return Array.isArray(list) ? list : []
  } catch {
    return []
  }
}

export function addHistory(item) {
  if (!item?.path || !item?.title) return
  const list = readHistory().filter(h => h.path !== item.path)
  list.unshift({
    type: item.type || '浏览',
    title: item.title,
    path: item.path,
    time: formatNow()
  })
  localStorage.setItem(KEY, JSON.stringify(list.slice(0, MAX)))
}

export function clearHistory() {
  localStorage.removeItem(KEY)
}

function formatNow() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}
