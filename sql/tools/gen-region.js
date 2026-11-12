/**
 * 生成全国省市补全 SQL
 *
 * 用法：node sql/tools/gen-region.js
 *   - 读 ZSB-user-web/src/utils/areas.js 里的完整省市名称（34 个省级行政区）
 *   - 读 /tmp/region_rows.tsv（当前库里的 region 数据，由 mysql -N -B 导出）
 *   - 输出 sql/03_region_cities.sql
 *
 * 规则：
 *   1. 已存在的省份/城市按名字匹配，原 id 一律不动（不让已有 school.city_id / user.city_id 失联）
 *   2. 新城市 id = 省 id * 100 + 序号，与湖北（1701~1717）的既有规则一致
 *   3. 只生成本项目涉及业务的 31 个大陆省级行政区，港澳台不参与专升本，不写入
 */
const fs = require('fs')
const path = require('path')

const ROOT = path.resolve(__dirname, '../..')
const AREAS = path.join(ROOT, 'ZSB-user-web/src/utils/areas.js')
const ROWS = path.join(__dirname, 'region_rows.tsv')
const OUT = path.join(ROOT, 'sql/03_region_cities.sql')

// ---------- 1. 读静态省市表 ----------
const src = fs.readFileSync(AREAS, 'utf8').replace(/export const areas\s*=\s*/, 'module.exports = ')
const m = new module.constructor()
m._compile(src, 'areas.js')
const areas = m.exports

// ---------- 2. 读库里现状 ----------
const rows = fs.readFileSync(ROWS, 'utf8').trim().split('\n').map(line => {
  const [id, parentId, name, sort] = line.split('\t')
  return { id: +id, parentId: +parentId, name, sort: +sort }
})

const provinceByName = new Map(rows.filter(r => r.parentId === 0).map(r => [r.name, r]))
const citiesOf = new Map()
rows.filter(r => r.parentId !== 0).forEach(r => {
  if (!citiesOf.has(r.parentId)) citiesOf.set(r.parentId, new Map())
  citiesOf.get(r.parentId).set(r.name, r)
})

// 港澳台没有专升本业务，跳过
const SKIP = new Set(['香港特别行政区', '澳门特别行政区', '台湾省'])

const esc = (s) => String(s).replace(/'/g, "''")

// ---------- 3. 生成 ----------
const inserts = []
const stats = []

for (const p of areas) {
  if (SKIP.has(p.label)) continue
  const province = provinceByName.get(p.label)
  if (!province) {
    console.error(`!! 库里找不到省份：${p.label}，跳过`)
    continue
  }

  const existing = citiesOf.get(province.id) || new Map()
  const usedIds = new Set([...existing.values()].map(c => c.id))
  let nextId = Math.max(province.id * 100, ...usedIds, province.id * 100) + 1

  let added = 0
  p.children.forEach((c, idx) => {
    if (existing.has(c.label)) return // 已有，保留原 id
    while (usedIds.has(nextId)) nextId++
    const id = nextId++
    usedIds.add(id)
    inserts.push(`(${id}, ${province.id}, '${esc(c.label)}', 2, ${idx + 1})`)
    added++
  })

  stats.push({ province: p.label, id: province.id, existing: existing.size, added })
}

const header = `-- ============================================================================
-- 03_region_cities.sql —— 全国省市数据补全
--
-- 背景：region 表原来只有 31 个省级行政区 + 湖北/湖南/河南的少量城市，
--       选择其他省份时二级下拉框显示「暂无数据」。
--
-- 本脚本做法：
--   1. 保留所有已存在的行（id 不变），只 INSERT 缺失的城市；
--   2. 新城市 id 沿用「省 id * 100 + 序号」的既有规则（湖北 1701~1717 即此规则）；
--   3. 可重复执行（先 DELETE 同 id 再 INSERT，等价于幂等 upsert）。
--
-- 生成器：sql/tools/gen-region.js（数据源 ZSB-user-web/src/utils/areas.js）
-- 执行：mysql -uroot -p zsb < sql/03_region_cities.sql
-- ============================================================================

SET NAMES utf8mb4;

`

let sql = header
if (inserts.length) {
  sql += `INSERT INTO \`region\` (\`id\`, \`parent_id\`, \`name\`, \`level\`, \`sort\`) VALUES\n`
  sql += inserts.join(',\n') + '\n'
  sql += `ON DUPLICATE KEY UPDATE \`parent_id\` = VALUES(\`parent_id\`), \`name\` = VALUES(\`name\`), \`sort\` = VALUES(\`sort\`);\n\n`
}

sql += `-- 校验：每个省的下级数量\n-- SELECT p.id, p.name, COUNT(c.id) FROM region p LEFT JOIN region c ON c.parent_id = p.id\n--  WHERE p.parent_id = 0 GROUP BY p.id ORDER BY p.id;\n`

fs.writeFileSync(OUT, sql, 'utf8')

console.log('省份          id   原有城市  新增')
stats.forEach(s => console.log(String(s.province).padEnd(12, ' '), String(s.id).padStart(3), String(s.existing).padStart(7), String(s.added).padStart(5)))
console.log(`\n共新增 ${inserts.length} 条城市记录 -> ${OUT}`)
