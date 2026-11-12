/**
 * 图片地址工具
 * 后端存的是 /uploads/xxx 相对路径；开发环境由 Vite 代理 /uploads 到 8080。
 */
export function fixImg(url) {
  if (!url) return ''
  if (/^(https?:)?\/\//.test(url) || url.startsWith('data:')) return url
  return url.startsWith('/') ? url : '/' + url
}
