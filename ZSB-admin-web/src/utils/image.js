/**
 * 图片地址工具：后端返回的是 /uploads/... 相对路径，
 * 开发环境走 Vite 代理，生产环境同域，直接拼根路径即可。
 */
export function fixImg(url) {
  if (!url) return ''
  if (/^(https?:)?\/\//.test(url) || url.startsWith('data:')) return url
  return url.startsWith('/') ? url : '/' + url
}
