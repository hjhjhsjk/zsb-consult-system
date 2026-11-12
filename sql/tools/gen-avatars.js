/**
 * 生成默认头像：为 avatar 为空的用户生成 SVG 渐变字母头像，
 * 存到 uploads/avatar/gen/，并把路径写回 user 表。
 *
 * 执行：node sql/tools/gen-avatars.js
 * 依赖：本机 node + mysql 客户端（密码与 application.yml 一致）
 */
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const UPLOAD_DIR = path.join(__dirname, '../../zsb-server/uploads/avatar/gen');
const MYSQL = 'mysql -uroot -p123456 zsb -N';

// 从数据库导出无头像用户（TSV + utf8mb4，避免 GBK 乱码）
const tsvPath = path.join(__dirname, 'users_no_avatar.tsv');
execSync(`${MYSQL} --default-character-set=utf8mb4 -e "SELECT id, uname, IFNULL(nickname,uname) FROM user WHERE avatar IS NULL OR avatar='';" > ${tsvPath}`);
const rows = fs.readFileSync(tsvPath, 'utf8').trim().split(/\r?\n/).filter(Boolean);

// 调色板：柔和双渐变，按用户 id 轮换，保证相邻用户颜色不同
const PALETTE = [
  ['#667eea', '#764ba2'],
  ['#f093fb', '#f5576c'],
  ['#4facfe', '#00f2fe'],
  ['#43e97b', '#38f9d7'],
  ['#fa709a', '#fee140'],
  ['#30cfd0', '#330867'],
  ['#a8edea', '#fed6e3'],
  ['#ff9a9e', '#fecfef'],
  ['#fbc2eb', '#a6c1ee'],
  ['#84fab0', '#8fd3f4'],
  ['#f6d365', '#fda085'],
  ['#5ee7df', '#b490ca'],
  ['#c471f5', '#fa71cd'],
  ['#48c6ef', '#6f86d6'],
  ['#f7971e', '#ffd200'],
  ['#11998e', '#38ef7d'],
  ['#ee0979', '#ff6a00'],
];

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const updates = [];
for (const line of rows) {
  const [id, uname, nickname] = line.split('\t');
  const [c1, c2] = PALETTE[Number(id) % PALETTE.length];
  // 取昵称首字符（中文也行），SVG 里白字居中
  const initial = (nickname || uname || 'U').trim().charAt(0).toUpperCase();
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="${c1}"/>
      <stop offset="1" stop-color="${c2}"/>
    </linearGradient>
  </defs>
  <rect width="200" height="200" rx="0" fill="url(#g)"/>
  <circle cx="170" cy="30" r="60" fill="#ffffff" opacity="0.12"/>
  <circle cx="25" cy="180" r="45" fill="#ffffff" opacity="0.10"/>
  <text x="100" y="100" text-anchor="middle" dominant-baseline="central"
        font-family="system-ui, 'Microsoft YaHei', sans-serif" font-size="88"
        font-weight="600" fill="#ffffff">${initial}</text>
</svg>`;
  const file = `u${id}.svg`;
  fs.writeFileSync(path.join(UPLOAD_DIR, file), svg, 'utf8');
  updates.push(`UPDATE user SET avatar = '/uploads/avatar/gen/${file}' WHERE id = ${id};`);
  console.log(`生成 ${file}  (${uname} → ${initial})`);
}

if (updates.length) {
  const sqlPath = path.join(__dirname, 'avatar_updates.sql');
  fs.writeFileSync(sqlPath, updates.join('\n') + '\n', 'utf8');
  execSync(`mysql -uroot -p123456 zsb < ${sqlPath}`);
  console.log(`\n已更新 ${updates.length} 个用户的 avatar 字段`);
} else {
  console.log('没有需要生成头像的用户');
}
