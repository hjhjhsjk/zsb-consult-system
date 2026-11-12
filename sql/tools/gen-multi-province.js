/**
 * 生成多省业务数据 SQL：sql/04_multi_province_data.sql
 *
 * 用法：node sql/tools/gen-multi-province.js
 * 依赖：sql/tools/region_rows.tsv（当前 region 表导出，含全部省市）
 *
 * 生成内容：
 *   - 1 个管理员账号（superadmin / Admin@2026）
 *   - 12 个演示用户（密码统一 Stu@2026）
 *   - 30 个省份 × 3~4 所院校（湖北已有，不重复生成）
 *   - 每所院校 3 条招生计划
 *   - 每省 3 条政策（报名通知 / 考试安排 / 免试办法）
 *   - 40 条帖子 + 配套评论 + 点赞（计数字段与明细表严格一致）
 *
 * 幂等策略：生成的数据 id 落在固定区间（school>=1000、post>=1000 等），
 *          脚本开头先删除这些区间的数据，可反复执行。
 */
const fs = require('fs')
const path = require('path')

const ROOT = path.resolve(__dirname, '../..')
const ROWS = path.join(__dirname, 'region_rows.tsv')
const OUT = path.join(ROOT, 'sql/04_multi_province_data.sql')

// ============================================================================
// 0. 常量
// ============================================================================
const PWD_STU = '$2a$10$GEWyLwM1I0xrt9VuM0pODevpA42M5DS3B5hqHjDGxHwqJvMMvaDkO'   // Stu@2026
const PWD_ADMIN = '$2a$10$QGyahWKTSDJprgtUIOu.Ju7cCaXIF0SxSCSCuB5FPWqcdUGR3H1Ny' // Admin@2026

// 专业 → 学费（理工/文史/艺术/医学大致区分）
const MAJORS = [
  { id: 1, name: '计算机科学与技术', fee: 5200 },
  { id: 2, name: '软件工程', fee: 5200 },
  { id: 3, name: '数据科学与大数据技术', fee: 5200 },
  { id: 4, name: '网络工程', fee: 5200 },
  { id: 5, name: '人工智能', fee: 5500 },
  { id: 6, name: '电子信息工程', fee: 5200 },
  { id: 7, name: '机械设计制造及其自动化', fee: 5000 },
  { id: 8, name: '电气工程及其自动化', fee: 5200 },
  { id: 9, name: '土木工程', fee: 5000 },
  { id: 10, name: '会计学', fee: 4500 },
  { id: 11, name: '财务管理', fee: 4500 },
  { id: 12, name: '工商管理', fee: 4500 },
  { id: 13, name: '市场营销', fee: 4500 },
  { id: 14, name: '学前教育', fee: 4500 },
  { id: 15, name: '护理学', fee: 6000 },
  { id: 16, name: '汉语言文学', fee: 4500 },
  { id: 17, name: '英语', fee: 4800 },
  { id: 18, name: '法学', fee: 4800 },
  { id: 19, name: '视觉传达设计', fee: 9000 },
  { id: 20, name: '环境设计', fee: 9000 }
]

// 各省代表性招生院校（真实存在的本科院校，专升本招生省份以此为准）
const PROVINCE_SCHOOLS = {
  '北京市': [['北京联合大学', '北京市'], ['北京城市学院', '北京市'], ['北京石油化工学院', '北京市'], ['北京农学院', '北京市']],
  '天津市': [['天津工业大学', '天津市'], ['天津理工大学', '天津市'], ['天津城建大学', '天津市'], ['天津农学院', '天津市']],
  '河北省': [['河北科技大学', '石家庄市'], ['河北经贸大学', '石家庄市'], ['河北师范大学', '石家庄市'], ['河北工程大学', '邯郸市']],
  '山西省': [['太原科技大学', '太原市'], ['山西财经大学', '太原市'], ['山西大同大学', '大同市'], ['长治学院', '长治市']],
  '内蒙古自治区': [['内蒙古工业大学', '呼和浩特市'], ['内蒙古农业大学', '呼和浩特市'], ['内蒙古财经大学', '呼和浩特市'], ['赤峰学院', '赤峰市']],
  '辽宁省': [['沈阳工业大学', '沈阳市'], ['沈阳师范大学', '沈阳市'], ['大连交通大学', '大连市'], ['辽宁科技学院', '本溪市']],
  '吉林省': [['长春工业大学', '长春市'], ['北华大学', '吉林市'], ['吉林财经大学', '长春市'], ['长春大学', '长春市']],
  '黑龙江省': [['哈尔滨理工大学', '哈尔滨市'], ['哈尔滨师范大学', '哈尔滨市'], ['东北石油大学', '大庆市'], ['佳木斯大学', '佳木斯市']],
  '上海市': [['上海应用技术大学', '上海市'], ['上海第二工业大学', '上海市'], ['上海商学院', '上海市'], ['上海电机学院', '上海市']],
  '江苏省': [['南京工业大学', '南京市'], ['江苏大学', '镇江市'], ['扬州大学', '扬州市'], ['常州大学', '常州市']],
  '浙江省': [['浙江工业大学', '杭州市'], ['浙江理工大学', '杭州市'], ['杭州电子科技大学', '杭州市'], ['浙江工商大学', '杭州市']],
  '安徽省': [['安徽工业大学', '马鞍山市'], ['安徽理工大学', '淮南市'], ['安徽财经大学', '蚌埠市'], ['合肥学院', '合肥市']],
  '福建省': [['福建工程学院', '福州市'], ['闽江学院', '福州市'], ['厦门理工学院', '厦门市'], ['福建江夏学院', '福州市']],
  '江西省': [['江西财经大学', '南昌市'], ['华东交通大学', '南昌市'], ['江西理工大学', '赣州市'], ['南昌工程学院', '南昌市']],
  '山东省': [['山东科技大学', '青岛市'], ['济南大学', '济南市'], ['青岛理工大学', '青岛市'], ['山东建筑大学', '济南市']],
  '河南省': [['河南理工大学', '焦作市'], ['河南科技大学', '洛阳市'], ['河南工业大学', '郑州市'], ['郑州轻工业大学', '郑州市']],
  '湖南省': [['长沙理工大学', '长沙市'], ['湖南农业大学', '长沙市'], ['中南林业科技大学', '长沙市'], ['湖南工商大学', '长沙市']],
  '广东省': [['广东工业大学', '广州市'], ['广州大学', '广州市'], ['广东财经大学', '广州市'], ['深圳技术大学', '深圳市']],
  '广西壮族自治区': [['桂林电子科技大学', '桂林市'], ['广西师范大学', '桂林市'], ['广西科技大学', '柳州市'], ['北部湾大学', '钦州市']],
  '海南省': [['海南师范大学', '海口市'], ['三亚学院', '三亚市'], ['海南热带海洋学院', '三亚市'], ['海口经济学院', '海口市']],
  '重庆市': [['重庆交通大学', '重庆市'], ['重庆理工大学', '重庆市'], ['重庆工商大学', '重庆市'], ['重庆科技大学', '重庆市']],
  '四川省': [['西南科技大学', '绵阳市'], ['成都信息工程大学', '成都市'], ['西华大学', '成都市'], ['四川轻化工大学', '自贡市']],
  '贵州省': [['贵州师范大学', '贵阳市'], ['贵州财经大学', '贵阳市'], ['贵州理工学院', '贵阳市'], ['遵义师范学院', '遵义市']],
  '云南省': [['昆明理工大学', '昆明市'], ['云南农业大学', '昆明市'], ['云南财经大学', '昆明市'], ['西南林业大学', '昆明市']],
  '西藏自治区': [['西藏大学', '拉萨市'], ['西藏农牧学院', '林芝市'], ['西藏藏医药大学', '拉萨市']],
  '陕西省': [['西安理工大学', '西安市'], ['西安工业大学', '西安市'], ['西安工程大学', '西安市'], ['陕西理工大学', '汉中市']],
  '甘肃省': [['兰州理工大学', '兰州市'], ['兰州交通大学', '兰州市'], ['甘肃农业大学', '兰州市'], ['西北师范大学', '兰州市']],
  '青海省': [['青海大学', '西宁市'], ['青海师范大学', '西宁市'], ['青海民族大学', '西宁市']],
  '宁夏回族自治区': [['宁夏大学', '银川市'], ['宁夏医科大学', '银川市'], ['北方民族大学', '银川市']],
  '新疆维吾尔自治区': [['新疆大学', '乌鲁木齐市'], ['新疆农业大学', '乌鲁木齐市'], ['新疆财经大学', '乌鲁木齐市'], ['石河子大学', '石河子市']]
}

// 民办院校（按名字识别，school_type=2）
const PRIVATE_SCHOOLS = new Set(['北京城市学院', '三亚学院', '海口经济学院'])

// ============================================================================
// 1. 读地区数据
// ============================================================================
const rows = fs.readFileSync(ROWS, 'utf8').trim().split('\n').map(l => {
  const [id, parentId, name] = l.split('\t')
  return { id: +id, parentId: +parentId, name }
})
const provinces = rows.filter(r => r.parentId === 0)
const citiesByProvince = new Map()
rows.filter(r => r.parentId !== 0).forEach(r => {
  if (!citiesByProvince.has(r.parentId)) citiesByProvince.set(r.parentId, [])
  citiesByProvince.get(r.parentId).push(r)
})

// ============================================================================
// 2. 工具
// ============================================================================
const esc = (s) => String(s).replace(/\\/g, '\\\\').replace(/'/g, "''")
const q = (s) => `'${esc(s)}'`
const pad = (n) => String(n).padStart(2, '0')

const statements = []
const push = (s) => statements.push(s)

const now = new Date('2026-11-09T14:00:00+08:00')
const fmtTime = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
const hoursAgo = (h) => fmtTime(new Date(now.getTime() - h * 3600 * 1000))

// ============================================================================
// 3. 清理（幂等）
// ============================================================================
push(`-- 清理上一轮生成的数据（按 id 区间，不动手工数据与湖北原有数据）`)
push(`DELETE FROM \`post\`         WHERE \`id\` >= 1000;`)
push(`DELETE FROM \`policy\`       WHERE \`id\` >= 10000;`)
push(`DELETE FROM \`recruitment_plan\` WHERE \`school_id\` >= 1000;`)
push(`DELETE FROM \`school\`       WHERE \`id\` >= 1000;`)
push(`DELETE FROM \`favorite\`     WHERE \`user_id\` >= 1000;`)
push(`DELETE FROM \`user\`         WHERE \`id\` >= 1000;`)

// ============================================================================
// 4. 管理员 + 演示用户
// ============================================================================
const ADMIN = {
  id: 1000, uname: 'superadmin', nickname: '超级管理员',
  phone: 13900009999, major: 1, year: 2027, province: 17, city: 1701
}
const DEMO_USERS = [
  { id: 1001, uname: 'xiaolin', nickname: '小林同学', major: 1, year: 2027, province: 17, city: 1701 },
  { id: 1002, uname: 'azhe', nickname: '阿哲', major: 2, year: 2026, province: 11, city: 1101 },
  { id: 1003, uname: 'susu', nickname: '苏苏', major: 10, year: 2027, province: 12, city: 1201 },
  { id: 1004, uname: 'beikao_mao', nickname: '备考的猫', major: 16, year: 2026, province: 19, city: 1901 },
  { id: 1005, uname: 'yizhixiong', nickname: '一只熊', major: 7, year: 2027, province: 16, city: 1601 },
  { id: 1006, uname: 'shangan2026', nickname: '专升本上岸人', major: 15, year: 2026, province: 31, city: 3101 },
  { id: 1007, uname: 'jiangzhe', nickname: '江浙沪考生', major: 3, year: 2027, province: 12, city: 1202 },
  { id: 1008, uname: 'xibeifeng', nickname: '西北风', major: 9, year: 2027, province: 26, city: 2601 },
  { id: 1009, uname: 'nifeng', nickname: '逆风翻盘', major: 18, year: 2026, province: 22, city: 2201 },
  { id: 1010, uname: 'amay', nickname: '阿May', major: 17, year: 2027, province: 14, city: 1401 },
  { id: 1011, uname: 'xuebudong', nickname: '学不动了', major: 14, year: 2027, province: 18, city: 1801 },
  { id: 1012, uname: 'naicha', nickname: '秋天第一杯奶茶', major: 19, year: 2026, province: 5, city: 501 }
]

const userValues = [
  `(${ADMIN.id}, ${q(ADMIN.uname)}, ${q(PWD_ADMIN)}, ${q(ADMIN.nickname)}, '', ${ADMIN.phone}, ${ADMIN.major}, ${ADMIN.year}, ${ADMIN.province}, ${ADMIN.province}, ${ADMIN.city}, 3)`,
  ...DEMO_USERS.map(u =>
    `(${u.id}, ${q(u.uname)}, ${q(PWD_STU)}, ${q(u.nickname)}, '', ${13900001000 + (u.id - 1000)}, ${u.major}, ${u.year}, ${u.city}, ${u.province}, ${u.city}, 0)`)
]
push(`-- 管理员账号 superadmin / Admin@2026；演示用户口令统一 Stu@2026`)
push(`INSERT INTO \`user\` (\`id\`, \`uname\`, \`pwd\`, \`nickname\`, \`avatar\`, \`phone\`, \`major_id\`, \`year\`, \`region_id\`, \`province_id\`, \`city_id\`, \`ustate\`) VALUES\n${userValues.join(',\n')};`)

// ============================================================================
// 5. 院校
// ============================================================================
const schoolRows = []
const schoolMeta = []   // 供计划/帖子引用
let schoolSeq = 0
provinces.forEach(p => {
  const items = PROVINCE_SCHOOLS[p.name]
  if (!items) return
  const cities = citiesByProvince.get(p.id) || []
  // 直辖市没有下级市，沿用「city_id = province_id」的既有约定
  const isMunicipality = p.id <= 4
  items.forEach(([name, cityName], i) => {
    const city = isMunicipality
      ? { id: p.id, name: p.name }
      : (cities.find(c => c.name === cityName) || cities[0] || { id: p.id, name: p.name })
    const id = p.id * 1000 + (i + 1)
    const type = PRIVATE_SCHOOLS.has(name) ? 2 : 1
    schoolRows.push(`(${id}, ${q(name)}, '', ${q(city.name)}, ${type}, ${p.id}, ${city.id})`)
    schoolMeta.push({ id, name, provinceId: p.id, provinceName: p.name, cityId: city.id, cityName: city.name })
    schoolSeq++
  })
})
push(`-- 院校：30 个省份共 ${schoolSeq} 所（湖北原有 13 所不动）`)
push(`INSERT INTO \`school\` (\`id\`, \`school_name\`, \`logo_url\`, \`city_name\`, \`school_type\`, \`province_id\`, \`city_id\`) VALUES\n${schoolRows.join(',\n')};`)

// 湖北现有院校（已在库中，不再插库；仅用于帖子/评论里引用真实校名）
const HUBEI_META = ['武汉纺织大学', '江汉大学', '湖北经济学院', '湖北师范大学', '湖北理工学院', '湖北文理学院', '黄冈师范学院', '荆楚理工学院']
  .map((name, i) => ({ id: 101 + i, name, provinceId: 17, provinceName: '湖北省', cityId: 1701, cityName: '武汉市' }))

// ============================================================================
// 6. 招生计划：每校 3 个专业
// ============================================================================
const planRows = []
schoolMeta.forEach((s, idx) => {
  for (let k = 0; k < 3; k++) {
    const major = MAJORS[(idx * 3 + k * 7) % MAJORS.length]
    const year = 2026
    const planCount = 25 + ((idx * 7 + k * 13) % 60)
    const type = (idx + k) % 9 === 0 ? 2 : 1
    planRows.push(`(${s.id}, ${q(s.name)}, '', ${major.id}, ${q(major.name)}, ${year}, ${planCount}, ${major.fee}, ${type})`)
  }
})
push(`-- 招生计划：${planRows.length} 条`)
push(`INSERT INTO \`recruitment_plan\` (\`school_id\`, \`school_name\`, \`school_logo\`, \`major_id\`, \`major_name\`, \`year\`, \`plan_count\`, \`tuition_fee\`, \`recruitment_type\`) VALUES\n${planRows.join(',\n')};`)

// ============================================================================
// 7. 政策：每省 3 条（湖北已有 5 条，跳过）
// ============================================================================
const policyRows = []
let policyId = 10000
provinces.filter(p => p.id !== 17).forEach((p, pi) => {
  const d1 = 5 + (p.id % 9)
  const d2 = d1 + 10
  const examDay = 8 + (p.id % 6)

  // 7.1 报名通知
  policyRows.push([
    ++policyId,
    `${2026}年${p.name}普通高校专升本报名工作通知`,
    `一、报名时间\n2026年3月${d1}日9:00至3月${d2}日17:00，逾期不予补报。\n\n二、报名方式\n考生登录${p.name}教育考试院官方网站在线报名，按要求上传本人照片与学籍证明。\n\n三、报考条件\n1. ${p.name}普通高校应届专科毕业生；\n2. 遵守中华人民共和国宪法和法律，无违纪处分记录；\n3. 身体健康状况符合招生院校专业要求。\n\n四、注意事项\n1. 报名信息须由考生本人填写并认真核对，因填报错误造成的一切后果由考生自负；\n2. 报名费按物价部门核定标准收取，缴费成功方视为报名完成。`,
    1, 1, p.id, 1, `2026-01-${pad(8 + (p.id % 15))} 09:00:00`
  ])

  // 7.2 考试安排
  policyRows.push([
    ++policyId,
    `2026年${p.name}专升本统一考试时间及科目安排`,
    `一、考试时间\n2026年5月${examDay}日（星期六）。\n\n二、考试科目\n上午 9:00-11:00  公共课（英语）\n下午 14:00-16:30 专业课（按报考专业确定）\n\n三、准考证打印\n5月1日起，考生可登录报名系统自行下载打印准考证。\n\n四、成绩与录取\n考试结束后约20个工作日公布成绩，录取工作由各招生院校按计划择优进行，拟录取名单在院校官网公示。`,
    1, 1, p.id, 0, `2026-02-${pad(10 + (p.id % 12))} 10:30:00`
  ])

  // 7.3 免试办法
  policyRows.push([
    ++policyId,
    `2026年${p.name}专升本免试生招生工作办法`,
    `一、免试条件\n1. 在校期间获得全国职业院校技能大赛三等奖及以上；\n2. 获得省级职业院校技能大赛一等奖；\n3. 退役大学生士兵，且服役期间未受处分。\n\n二、申请流程\n符合条件的考生须于3月${d1}日至3月${d1 + 5}日向报考院校提交免试申请及获奖证书、退役证明等材料。\n\n三、考核与录取\n招生院校组织综合考查，重点考查专业基础与综合素质，择优录取，不再参加全省统一考试。`,
    1, 2, p.id, 0, `2026-03-${pad(2 + (p.id % 10))} 08:45:00`
  ])
})

const policyValues = policyRows.map(r => `(${r[0]}, ${q(r[1])}, ${q(r[2])}, ${r[3]}, ${r[4]}, ${r[5]}, ${r[6]}, ${200 + ((r[0] * 17) % 900)}, '', ${q(r[7])})`)
push(`-- 政策：${policyRows.length} 条（每省报名/考试/免试各一条，湖北保留原有 5 条）`)
push(`INSERT INTO \`policy\` (\`id\`, \`title\`, \`content\`, \`publisher\`, \`category_id\`, \`province_id\`, \`is_top\`, \`view_count\`, \`url\`, \`create_time\`) VALUES\n${policyValues.join(',\n')};`)

// ============================================================================
// 8. 帖子 + 评论 + 点赞
// ============================================================================
const allSchools = [...schoolMeta, ...HUBEI_META]
const schoolsOfProvince = (pid) => allSchools.filter(s => s.provinceId === pid)

// 内容模板：用真实省名/校名/专业名组合，避免千篇一律
const TEMPLATES = [
  (p, s, m) => `有没有${s ? s.name : p.name + '某高校'}的学长学姐？想了解${m.name}专业的考试科目和参考书，官网信息太少了一头雾水。`,
  (p, s, m) => `${p.name}2026 年专升本报名条件有哪些？往届生还能报吗？我是 2025 届的，想再考一次。`,
  (p, s, m) => `刚上岸${s ? s.name : p.name + '某高校'}${m.name}专业，开个答疑帖，复习规划、资料选择、面试流程都可以问，知无不言。`,
  (p, s, m) => `想问下${s ? s.name : p.name + '某高校'}${m.name}今年大概招多少人？去年分数线是多少分啊，卷不卷？`,
  (p, s, m) => `求推荐${p.name}专升本${m.name}方向的复习资料，市面上太多了，怕买到不适合的。`,
  (p, s, m) => `${p.name}专升本英语大概是什么难度？四级还没过，现在开始准备来得及吗？`,
  (p, s, m) => `${s ? s.name : '目标院校'}的宿舍条件怎么样？有空调和独立卫浴吗？想提前了解一下，谢谢！`,
  (p, s, m) => `跨专业考${m.name}可行吗？我专科是${MAJORS[(m.id + 5) % MAJORS.length].name}，担心专业课完全跟不上。`,
  (p, s, m) => `${p.name}专升本免试条件是什么？省级技能大赛一等奖算不算？有没有办理过的同学说下流程。`,
  (p, s, m) => `在职备考${p.name}专升本，每天下班只有三小时，还有五个月，想问问大家这样的节奏现实吗？`
]

// 评论模板
const COMMENT_TEMPLATES = [
  '我也想知道，蹲一个回复。',
  '同问，这所学校的资料真的好少，找到的都是好几年前的。',
  '去年考过，专业课比想象中简单，重点吃透官方考纲就行，别乱买资料。',
  '建议先看省教育考试院发的考试大纲，再决定买什么书，不然很容易走弯路。',
  '我就是这个学校的，宿舍有空调，晚上 11 点熄灯，四人间上床下桌。',
  '英语四级过了基本稳一半，多刷近五年真题，阅读和作文提分最快。',
  '跨专业要早点动手，专业课从零开始至少要过三轮，越早越好。',
  '已上岸，加油！坚持到考完那天就已经赢了一半人。',
  '招生计划学校官网能查到，今年大概招 60 人左右，推免占一部分。',
  '别焦虑，按部就班复习就行，我当初也是零基础，最后超线 40 分。',
  '这个分数段建议多准备两所保底院校，志愿填报也很关键。',
  '资料不用买太多，考纲 + 真题 + 一本教材足够了。'
]

const postRows = []
const commentRows = []
const likeRows = []
const postCountById = new Map()
const likeCountById = new Map()

let postId = 1000
let postIdx = 0

function addPost(author, provinceId, schoolId, content, hours) {
  postId++
  postIdx++
  const views = 40 + ((postIdx * 37) % 900)
  postRows.push({
    id: postId, uid: author.id, content, regionId: provinceId, schoolId,
    viewCount: views, postTime: hoursAgo(hours)
  })

  // 评论 2~3 条
  const cCount = 2 + (postIdx % 2)
  const commenters = DEMO_USERS.filter(u => u.id !== author.id)
  for (let i = 0; i < cCount; i++) {
    const u = commenters[(postIdx * 3 + i * 5) % commenters.length]
    const text = COMMENT_TEMPLATES[(postIdx + i * 3) % COMMENT_TEMPLATES.length]
    commentRows.push({ postId, uid: u.id, content: text, time: hoursAgo(hours - 1 - i * 0.5) })
  }
  postCountById.set(postId, cCount)

  // 点赞 1~7 人
  const lCount = 1 + ((postIdx * 5) % 7)
  const likers = DEMO_USERS.filter(u => u.id !== author.id)
  for (let i = 0; i < lCount; i++) {
    const u = likers[(postIdx * 2 + i * 3) % likers.length]
    if (!likeRows.some(l => l.postId === postId && l.uid === u.id)) {
      likeRows.push({ postId, uid: u.id, time: hoursAgo(hours - 0.5) })
    }
  }
  likeCountById.set(postId, likeRows.filter(l => l.postId === postId).length)
}

// 8.1 每省 1 条主帖（覆盖全部 30 省）
provinces.forEach((p, i) => {
  const schools = schoolsOfProvince(p.id)
  if (!schools.length) return
  const s = schools[i % schools.length]
  const m = MAJORS[(i * 5) % MAJORS.length]
  const author = DEMO_USERS[i % DEMO_USERS.length]
  const tpl = TEMPLATES[i % TEMPLATES.length]
  addPost(author, p.id, s.id, tpl(p, s, m), 3 + i * 6)
})

// 8.2 热门省份补充帖子
const HOT = [17, 11, 19, 12, 16, 31, 22, 18, 5, 26]
HOT.forEach((pid, i) => {
  const p = provinces.find(x => x.id === pid)
  const schools = schoolsOfProvince(pid)
  if (!p || !schools.length) return
  const s = schools[(i + 1) % schools.length]
  const m = MAJORS[(i * 3 + 4) % MAJORS.length]
  const author = DEMO_USERS[(i + 5) % DEMO_USERS.length]
  const tpl = TEMPLATES[(i + 3) % TEMPLATES.length]
  addPost(author, pid, i % 2 === 0 ? s.id : 0, tpl(p, s, m), 1.5 + i * 3)
})

const postValues = postRows.map(p =>
  `(${p.id}, ${p.uid}, ${q(p.content)}, '', ${p.regionId}, ${p.schoolId}, ${p.viewCount}, ${likeCountById.get(p.id)}, 0, ${postCountById.get(p.id)}, 1, ${q(p.postTime)})`)
push(`-- 帖子：${postRows.length} 条（覆盖 30 个省份）`)
push(`INSERT INTO \`post\` (\`id\`, \`user_id\`, \`content\`, \`image_url\`, \`region_id\`, \`school_id\`, \`view_count\`, \`like_count\`, \`fav_count\`, \`post_count\`, \`status\`, \`post_time\`) VALUES\n${postValues.join(',\n')};`)

const commentValues = commentRows.map(c => `(${c.postId}, ${c.uid}, ${q(c.content)}, 1, 1, ${q(c.time)})`)
push(`-- 评论：${commentRows.length} 条`)
push(`INSERT INTO \`post_comment\` (\`post_id\`, \`user_id\`, \`content\`, \`like_count\`, \`status\`, \`create_time\`) VALUES\n${commentValues.join(',\n')};`)

const likeValues = likeRows.map(l => `(${l.uid}, ${l.postId}, ${q(l.time)})`)
push(`-- 点赞：${likeRows.length} 条（post.like_count 与明细严格一致）`)
push(`INSERT INTO \`post_like\` (\`user_id\`, \`post_id\`, \`create_time\`) VALUES\n${likeValues.join(',\n')};`)

// ============================================================================
// 9. 汇总校验
// ============================================================================
push(`-- ===== 汇总校验 =====`)
push(`SELECT '地区' AS 项目, COUNT(*) AS 数量 FROM \`region\`
UNION ALL SELECT '院校', COUNT(*) FROM \`school\`
UNION ALL SELECT '招生计划', COUNT(*) FROM \`recruitment_plan\`
UNION ALL SELECT '政策', COUNT(*) FROM \`policy\`
UNION ALL SELECT '帖子', COUNT(*) FROM \`post\`
UNION ALL SELECT '评论', COUNT(*) FROM \`post_comment\`
UNION ALL SELECT '用户', COUNT(*) FROM \`user\`;`)

// ============================================================================
// 10. 输出
// ============================================================================
const header = `-- ============================================================================
-- 04_multi_province_data.sql —— 全国多省业务数据
--
-- 由 sql/tools/gen-multi-province.js 生成，请勿手工编辑。
--
-- 包含：
--   * 管理员账号  superadmin / Admin@2026
--   * 演示用户    12 个，口令统一 Stu@2026
--   * 院校        30 个省份（湖北已有 13 所不重复）
--   * 招生计划    每所院校 3 个专业
--   * 政策        每省报名通知 / 考试安排 / 免试办法各 1 条
--   * 论坛        40 条帖子 + 评论 + 点赞，覆盖 30 个省份
--
-- 幂等：生成数据的 id 落在固定区间（school/post >= 1000 等），
--       脚本开头先删除这些区间，可反复执行。
-- 执行：mysql -uroot -p zsb < sql/04_multi_province_data.sql
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 1;

`

fs.writeFileSync(OUT, header + statements.join('\n\n') + '\n', 'utf8')
console.log(`院校 ${schoolSeq} 所 / 计划 ${planRows.length} 条 / 政策 ${policyRows.length} 条 / 帖子 ${postRows.length} 条 / 评论 ${commentRows.length} 条 / 点赞 ${likeRows.length} 条`)
console.log('->', OUT)
