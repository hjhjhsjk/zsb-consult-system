# 专升本咨询系统 · 项目说明

> 前端项目文档 · 用于后续开发查阅

---

## 一、技术栈

| 分类 | 技术 |
|------|------|
| 框架 | Vue 3（`<script setup>` 语法） |
| 构建 | Vite |
| UI | Element Plus |
| 图标 | @element-plus/icons-vue |
| 路由 | Vue Router 4 |
| 状态 | Pinia |
| 请求 | Axios（预留，暂未正式使用） |
| 包管理 | pnpm |

---

## 二、目录结构

```
ZSB-user-web/
├── src/
│   ├── main.js              # 入口：注册 Pinia / Router / ElementPlus / 全局图标
│   ├── App.vue              # 根组件：仅 <router-view />
│   ├── style.css            # 全局样式
│   │
│   ├── router/
│   │   └── index.js         # 路由表 + 登录守卫
│   │
│   ├── store/
│   │   └── user.js          # Pinia：token / userInfo / prefs
│   │
│   ├── utils/
│   │   └── areas.js         # 地区级联数据
│   │
│   ├── api/                 # 【预留】接口封装
│   │   └── request.js       # 【待建】Axios 实例
│   │
│   ├── layout/
│   │   └── index.vue        # 导航栏布局
│   │
│   └── views/
│       ├── home/
│       │   └── index.vue    # 首页
│       ├── school/
│       │   └── index.vue    # 院校页
│       ├── consult/
│       │   └── index.vue    # 咨询页
│       └── user/
│           ├── index.vue    # 个人中心
│           ├── Login.vue    # 登录
│           └── Register.vue # 注册
```

---

## 三、路由与页面

| 路由 | 页面 | 权限 | 状态 |
|------|------|:----:|:----:|
| `/` | 首页 | 公开 | ✅ |
| `/school` | 院校页 | 公开 | 🔲 |
| `/consult` | 咨询页 | 需登录 | 🔲 |
| `/user` | 个人中心 | 需登录 | 🔲 |
| `/login` | 登录 | 公开 | ✅ |
| `/register` | 注册 | 公开 | ✅ |

> **导航栏** 位于 `layout/index.vue`，通过嵌套路由包裹上方页面；登录 / 注册页不嵌套。

---

## 四、数据流

### 4.1 登录态

```
Login 提交
  └─ 生成 token → 存 Pinia + localStorage
       └─ 路由守卫读 token → 控制受保护页面
            └─ 导航栏读 isLogin → 切换显示 登录/注册 或 用户下拉
```

### 4.2 用户偏好（地区 / 报考年份 / 意向专业）

```
Register 表单
  └─ setPrefs({ area, year, major }) → 存 Pinia + localStorage
       └─ 导航栏 areaProxy(computed) 读 prefs.area → 级联显示
            └─ 用户改地区 → areaProxy.set → setArea → 写回 store
```

> **核心**：注册时填的偏好，登录后导航栏自动同步。

---

## 五、Pinia Store

**`store/user.js`**

```
useUserStore()
├── state
│   ├── token       登录令牌
│   ├── userInfo    用户信息 { username, role }
│   └── prefs       偏好 { area, year, major }
│
├── getters
│   ├── isLogin     是否登录
│   └── isAdmin     是否管理员
│
└── actions
    ├── setLogin(data)    登录成功写入
    ├── setPrefs(data)    保存偏好
    ├── setArea(area)     仅更新地区（导航栏用）
    └── logout()          清空登录态
```

**localStorage 映射**

| store 字段 | localStorage key |
|-----------|------------------|
| token | `token` |
| userInfo | `userInfo` |
| prefs | `userPrefs` |

---

## 六、导航栏  layout/index.vue

**结构**

```
el-row (header)
├── el-col :span="4"     Logo「专升本咨询」
├── div                  搜索框（绝对定位居中）
└── el-col :span="20"    右侧区
    ├── el-menu          首页 / 咨询 / 院校 / 个人中心
    ├── el-cascader      地区（v-model="areaProxy"）
    ├── el-button        登录
    └── el-button        注册
```

**关键变量**

| 变量 | 类型 | 说明 |
|------|------|------|
| `keyword` | ref | 搜索关键词 |
| `areaProxy` | computed | get 读 `prefs.area`，set 写 store |
| `userStore` | store | Pinia 实例 |

**易错提醒**

- 模板使用 `areaProxy`，**script 必须定义**，否则级联不显示值
- 登录 / 注册按钮分别跳 `/login`、`/register`

---

## 七、注册页  Register.vue

**表单字段**

| 字段 | 校验规则 |
|------|---------|
| `username` | 必填 · 3~20 位 · 字母/数字/中文 |
| `phone` | 必填 · `1[3-9]\d{9}` |
| `area` | 必填 · 级联数组 |
| `year` | 必填 · 下拉（当前年 ~ 未来 3 年） |
| `major` | 必填 · 下拉（分组专业） |
| `password` | 必填 · 6~20 位 · 含强度条 |
| `confirmPassword` | 必填 · 与密码一致 |
| `agree` | 必勾 |

**注册流程**

```
handleRegister()
  ├─ formRef.validate()
  ├─ 假注册（setTimeout 模拟请求）
  ├─ userStore.setPrefs({ area, year, major })
  └─ router.push('/login')
```

**密码强度**：按长度 / 大小写 / 数字 / 符号评分 → `1 弱 / 2 中 / 3 强`

---

## 八、登录页 Login.vue

**表单**：username · password · 记住我

**登录流程**

```
handleLogin()
  ├─ formRef.validate()
  ├─ 假登录：生成 token
  ├─ 存 localStorage + Pinia
  └─ router.push(route.query.redirect || '/')
```

**记住我**：勾选后存 `rememberUser`，进入页面自动回填。

---

## 九、路由守卫

```js
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) return next('/login')
  next()
})
```

**需要登录的页面**：路由 `meta.requiresAuth === true`（`/consult`、`/user`）。

---

## 十、开发进度

### ✅ 已完成

- 项目脚手架（Vite + Vue3 + ElementPlus + Pinia + Router）
- 全局图标注册
- 导航栏布局
- 首页
- 登录页（校验 / 记住我 / 假登录）
- 注册页（校验 / 密码强度 / 地区 / 年份 / 专业 / 偏好存 store）
- 地区级联数据
- Pinia user store
- 路由 + 守卫

### 🔲 待办

- [ ] `api/request.js` Axios 封装
- [ ] 后端接口对接（当前均为假数据）
- [ ] 院校页（列表 + 详情）
- [ ] 咨询页（提问 + 我的咨询）
- [ ] 个人中心
- [ ] 首页接真实数据
- [ ] 后台管理（Dashboard / 院校 / 专业 / 分数线 / 咨询 / 用户）
- [ ] 部署（Nginx + 后端）

---

## 十一、常见问题速查

| 现象 | 原因 | 处理 |
|------|------|------|
| `Failed to resolve import "xxx"` | 包未安装 | `pnpm add 包名` |
| 图标不显示 | 图标库缺失 / 未注册 | 安装 + main.js 循环注册 |
| 地区级联无选中值 | `areaProxy` 未定义 | 导航栏 script 补 computed |
| 页面只剩导航栏 | 布局缺 `<router-view />` | Layout 模板补上 |
| `Unexpected EOF in tag` | 标签未闭合 | 检查最近修改的 `.vue` |
| Pinia 报错 | 未 `app.use(pinia)` | main.js 补上 |
| 清空本地数据 | — | 控制台 `localStorage.clear(); location.reload()` |

---

## 十二、测试流程（重置后）

```js
// F12 Console
localStorage.clear()
location.reload()
```

1. 打开 `/register`，完整填写（含地区 / 年份 / 专业）
2. 点击注册 → 提示成功 → 跳 `/login`
3. 登录 → 返回首页
4. 检查导航栏地区是否自动显示
5. 控制台验证 `JSON.parse(localStorage.getItem('userPrefs'))`

---

## 十三、后续开发顺序

```
1. 个人中心
2. 前端接后端真实接口
3. 后台管理
```

---

## 十四、核心备忘

> - **登录态**：`localStorage + Pinia`
> - **用户偏好**：注册时存 `prefs`，导航栏 `computed` 双向同步
> - **导航栏**：`layout/index.vue`，所有页面共享
> - **受保护页面**：路由 `meta.requiresAuth` + 守卫

---

## 十五、更新日志 · 2026-09-24

### 修复 / 优化

- **登录**：改为调 `userStore.login()`，走真实账号比对 + Pinia 同步，登录后**导航栏立即显示头像/昵称**（不再需要刷新）
- **头像**：注册上传时压缩到 200x200 / JPEG 0.8，**一张 1MB 图压缩到 10~20KB**，避免 localStorage 溢出
- **导航栏**：已登录显示头像 + 昵称 + 下拉（个人中心 / 退出登录），未登录显示 登录 / 注册 按钮
- **死链接**：
  - 登录页「忘记密码？」→ 弹提示框（联系方式）
  - 注册页《用户协议》《隐私政策》→ 弹提示框
  - 点击链接**不会误勾选** checkbox（用 `@click.prevent`）
- **Element Plus**：`:underline="false"` 统一改成 `underline="never"`，消除控制台弃用警告

### 新增 localStorage

| key | 说明 |
|-----|------|
| `userAccounts` | 已注册账号列表（含 avatar / area / year / major） |
| `rememberUser` | 登录页「记住我」的用户名 |

### 关键函数位置

| 功能 | 位置 |
|------|------|
| 头像压缩 | `Register.vue` → `compressImage(file, maxSize, quality)` |
| 账号校验登录 | `store/user.js` → `login(username, password)` |
| 退出登录 | `store/user.js` → `logout()` + `layout/index.vue` → `handleCommand` |
| 忘记密码弹窗 | `Login.vue` → `handleForgot()` |
| 协议弹窗 | `Register.vue` → `showAgreement()` / `showPrivacy()` |

---

## 十六、更新日志 · 2026-10-01

### 新增页面

#### 咨询页 `views/consult/index.vue`

- 政策列表，卡片展示
- 筛选条件：
  - 省份（`province_id`）
  - 发布方 `publisher`：1 政府 / 2 学校
  - 类型 `type`：1 普通 / 2 免试
- 点击卡片进入政策详情

接口：`GET /home/getPolicies`

#### 政策详情页 `views/consult/Detail.vue`

- 展示政策标题、发布方、类型、省份、正文内容
- 顶部返回

#### 院校页 `views/school/index.vue`

- 院校列表，卡片展示（含 logo / 名称 / 类型）
- 筛选条件：
  - 省份（`province_id`）
  - 类型 `type`：1 公办 / 2 民办
- 点击卡片进入院校详情

接口：`GET /home/Schools/{province_id}/{type}`

#### 院校详情页 `views/school/Detail.vue`

- 展示院校基本信息（名称 / logo）
- 招生计划表：`major_name` / `plan_count` / `tuition_fee` / `year`
- 顶部返回

接口：`GET /home/getRecruitmentPlan/{school_id}`

### 新增封装

| 文件 | 内容 |
|------|------|
| `api/request.js` | Axios 实例，baseURL='/api' |
| `api/consult.js` | `getPolicies` / `getRegions` |
| `api/school.js` | `getSchools` / `getRecruitmentPlan` |

- Vite 代理 `/api`、`/uploads` → `http://8.211.173.225:666`

### 已知问题

- `getRecruitmentPlan` 接口响应 3.19s，较慢（后端加索引可解）
- 部分学校 logo 404（后端图片缺失）

---

## 十七、更新日志 · 2026-10-07

### 完成

- **导航栏地区**：改用真接口 + Element Plus 懒加载，与注册页统一（`/user/regions`）
- **首页接真数据**：
  - 热门院校 `GET /home/Schools/{province_id}/{type}`，跟用户地区联动
  - 热门专业 `GET /user/getMajor`
  - 轮播图 / 快捷入口保持静态
  - 无数据时显示"暂无院校数据"

### 关键技术点

- 导航栏地区：`el-cascader` 用 `:options` 一次性加载省份 + `@expand-change` 懒加载市
- 地区回显：登录后 `fetchUserInfo` 把 `province_id`/`city_id` 写进 `prefs.area`
- 首页地区联动：`watch(() => userStore.prefs.area)` 变化重新拉院校

---

## 十八、更新日志 · 2026-10-08

### 论坛（BBS）—— 全部完成

#### 新增页面

- **论坛列表** `views/bbs/index.vue`
  - 按省拉帖子（`GET /BBS/info/{province_id}/0`，school_id 传 0 表示全省）
  - 顶部显示当前地区 + 发帖按钮
  - 帖子卡片（内容 / 图片 / 浏览数 / 时间）
  - 发帖弹窗（内容 + 可选图片，form-data 提交）
  - 跟导航栏地区联动（`watch prefs.area`）

- **帖子详情** `views/bbs/Detail.vue`
  - 帖子内容 + 图片预览
  - 帖子点赞 / 取消（乐观更新）
  - 收藏 / 取消（乐观更新）
  - 评论列表 + 评论点赞
  - 发评论：后端无接口，显示"开发中"提示

#### 新增接口封装 `api/bbs.js`

| 函数 | 接口 |
|------|------|
| getPostList | `GET /BBS/info/{province_id}/{school_id}` |
| getPostDetail | `GET /BBS/GetPostDetail/{uid}/{post_id}` |
| insertPost | `POST /BBS/insert`（form-data） |
| getComments | `GET /BBS/comments/{uid}/{post_id}` |
| likeComment / unlikeComment | `POST /BBS/like_comment|unlike_comment/{uid}/{comment_id}` |
| likePost / unlikePost | `POST /BBS/like_post|unlike_post/{uid}/{post_id}` |
| addFavorite / clearFavorite | `POST /BBS/add_favorite|clear_favorite/{uid}/{type}/{id}` |
| getFavorites | `GET /BBS/get_favfavorite/{uid}/{type}` |

### 个人中心 —— 收尾

- **我的收藏**：接 `getFavorites(uid, 1)`（帖子）
- **我的发帖**：拉当前省帖子，前端筛 `user_id`
- 切 Tab 按需加载
- 地区 / 专业 id 转中文（调 `/user/regions`、`/user/getMajor`）

### 搜索 —— 新增

#### 新增页面 `views/search/index.vue`

- 导航栏搜索框 → 回车 → `/search?keyword=xxx`
- 调 `GET /home/search?keyword=xxx`（**不绑地区，全国搜**）
- 结果卡片：学校 + 专业 + 学费 + 招生数 + 年份 + 类型
- 点卡片跳院校详情

#### 新增接口 `api/search.js`

| 函数 | 接口 |
|------|------|
| searchPlans | `GET /home/search` |

### 导航栏

- 加"论坛"菜单（`/bbs`）
- 搜索框接上（回车 / 点按钮跳搜索页）
- `activeMenu` 计算属性：子路径（如 `/bbs/3`）归到一级菜单高亮
- **地区刷新重置**：进页面时用 `userInfo.provinceId` 重置 `prefs.area`

### 路由

- 新增 `/bbs`、`/bbs/:id`、`/search`（都在主 Layout 的 children 里）

### 关键技术点

- 图片路径 `fixImg`：`uploads/xxx.jpg` → `/uploads/xxx.jpg`
- 点赞 / 收藏用**乐观更新**（先改 UI，接口后台跑，失败回滚）
- 地区级联：省份一次性加载 + 市懒加载（`@expand-change`）
- 帖子点赞 / 收藏状态：`is_liked` / `is_fav`（1=已操作，0=未操作）

### 遗留问题（都是后端）

- **发评论**：后端没有 `POST` 发评论接口，前端只做只读
- **头像**：后端无图片上传接口，注册时 `avatar` 传空
- **图片 404**：BBS 帖子图片后端文件缺失（数据库有路径，服务器无文件）
- **post_count 不准**：BBS 列表返回的评论数不准确（列表显示 1，详情无评论）
- **我的发帖跨省**：后端无"按用户查帖"接口，只能拉当前省

### 用户端进度

**✅ 全部完成**

| 模块 | 状态 |
|------|------|
| 首页 | ✅ |
| 咨询页（政策列表 + 详情） | ✅ |
| 院校页（列表 + 详情） | ✅ |
| 搜索（招生计划） | ✅ |
| 论坛（列表 / 详情 / 发帖 / 点赞 / 收藏） | ✅ |
| 个人中心（资料 / 收藏 / 发帖） | ✅ |
| 登录 / 注册 | ✅ |
| 导航栏（菜单 / 地区 / 用户区） | ✅ |
| 路由守卫 | ✅ |

### 下一步

- [ ] 后台管理系统（下一阶段）
- [ ] 等后端补：发评论、头像上传、图片、post_count

### 已知问题

- 部分省（如直辖市）无院校数据（后端数据覆盖不全）
- 少量 Element Plus 警告（`underline` / `none`，不影响功能）
