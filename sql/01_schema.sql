-- ============================================================================
--  专升本咨询系统 · 数据库初始化脚本（1/2 建库建表）
--  目标版本：MySQL 8.0+        字符集：utf8mb4
--  执行方式：mysql -u root -p < 01_schema.sql
--  说明：脚本可重复执行；执行会 DROP 全部业务表（数据清空），仅用于开发环境
--  对应文档：docs/后端接口文档.md 第 9 章「实体与 DTO」
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `zsb`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `zsb`;

DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `comment_like`;
DROP TABLE IF EXISTS `post_like`;
DROP TABLE IF EXISTS `post_comment`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `recruitment_plan`;
DROP TABLE IF EXISTS `policy`;
DROP TABLE IF EXISTS `school`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `major`;
DROP TABLE IF EXISTS `region`;


-- ----------------------------------------------------------------------------
-- 1. region 行政区划（两级：省 - 市/州）
--    前端强约束：id 1/2/3/4 必须依次为 北京/天津/上海/重庆
--    前端强约束：id 17 必须是 湖北（多处默认值 provinceId: 17）
--    直辖市只存一条省级记录，不建下级；用户表 city_id 直接等于 province_id
-- ----------------------------------------------------------------------------
CREATE TABLE `region` (
  `id`        INT UNSIGNED     NOT NULL                COMMENT '主键；1~4 必须为京津沪渝，17 必须为湖北',
  `parent_id` INT UNSIGNED     NOT NULL DEFAULT 0      COMMENT '父级 ID；0 表示省级',
  `name`      VARCHAR(30)      NOT NULL                COMMENT '名称，如 湖北省 / 武汉市',
  `level`     TINYINT UNSIGNED NOT NULL DEFAULT 1      COMMENT '层级：1=省 2=市',
  `sort`      INT              NOT NULL DEFAULT 0      COMMENT '排序，升序',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '行政区划（前端只支持两级，不支持区县）';


-- ----------------------------------------------------------------------------
-- 2. major 专业
--    列名 c_name 沿用前端约定（GET /user/getMajor 返回 {id, c_name}），不要改名
-- ----------------------------------------------------------------------------
CREATE TABLE `major` (
  `id`     INT UNSIGNED NOT NULL AUTO_INCREMENT       COMMENT '主键',
  `c_name` VARCHAR(60)  NOT NULL                     COMMENT '专业名称（前端字段名 c_name）',
  `sort`   INT          NOT NULL DEFAULT 0           COMMENT '排序，升序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_c_name` (`c_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '专业字典';


-- ----------------------------------------------------------------------------
-- 3. user 用户（学生 + 管理员）
--    注意：phone 必须 BIGINT —— 前端传 JSON 数字 13800138000，超出 int32 范围
--    注意：JSON 字段 major ↔ 本表 major_id；JSON 字段 year ↔ 本表 year
-- ----------------------------------------------------------------------------
CREATE TABLE `user` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '主键，对应前端 uid',
  `uname`       VARCHAR(20)      NOT NULL                 COMMENT '用户名，3~20 位，字母/数字/下划线/中文，唯一',
  `pwd`         VARCHAR(100)     NOT NULL                 COMMENT 'BCrypt 密文（$2a$10$...），禁止明文',
  `nickname`    VARCHAR(20)      NOT NULL DEFAULT ''      COMMENT '昵称，默认同 uname',
  `avatar`      VARCHAR(255)     NOT NULL DEFAULT ''      COMMENT '头像 URL，允许空串',
  `phone`       BIGINT UNSIGNED  NOT NULL                 COMMENT '手机号（BIGINT，不可用 INT）',
  `major_id`    INT UNSIGNED     NULL                     COMMENT '意向专业 → major.id（JSON 字段名 major）',
  `year`        SMALLINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '报考年份，如 2027（JSON 字段名 year）',
  `region_id`   INT UNSIGNED     NOT NULL                 COMMENT '注册选择的地区（最细一级，入参 regions）',
  `province_id` INT UNSIGNED     NOT NULL                 COMMENT '冗余省 ID，由 region_id 推导',
  `city_id`     INT UNSIGNED     NOT NULL                 COMMENT '冗余市 ID；直辖市时 = province_id',
  `ustate`      TINYINT          NOT NULL DEFAULT 0       COMMENT '0=学生 3=管理员',
  `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uname` (`uname`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_region` (`province_id`, `city_id`),
  KEY `idx_major` (`major_id`),
  CONSTRAINT `fk_user_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表';


-- ----------------------------------------------------------------------------
-- 4. school 院校
--    logo_url 必须带前导 / 或完整 http URL（院校列表页不做路径补全）
-- ----------------------------------------------------------------------------
CREATE TABLE `school` (
  `id`          INT UNSIGNED     NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `school_name` VARCHAR(60)      NOT NULL                 COMMENT '院校名称',
  `logo_url`    VARCHAR(255)     NOT NULL DEFAULT ''      COMMENT '校徽地址，须以 / 开头或完整 URL',
  `city_name`   VARCHAR(30)      NOT NULL DEFAULT ''      COMMENT '城市名（冗余，前端直接展示）',
  `school_type` TINYINT UNSIGNED NOT NULL                 COMMENT '1=公办 2=民办',
  `province_id` INT UNSIGNED     NOT NULL                 COMMENT '所属省 → region.id',
  `city_id`     INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '所属市 → region.id',
  `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_province_type` (`province_id`, `school_type`),
  CONSTRAINT `fk_school_province` FOREIGN KEY (`province_id`) REFERENCES `region` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '院校表';


-- ----------------------------------------------------------------------------
-- 5. policy 政策
--    注意：请求参数名为 type，响应字段名为 category_id，DB 用 category_id
-- ----------------------------------------------------------------------------
CREATE TABLE `policy` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `title`       VARCHAR(100)     NOT NULL                 COMMENT '标题',
  `content`     TEXT             NOT NULL                 COMMENT '正文全文（前端列表截断展示、详情整篇渲染）',
  `publisher`   TINYINT UNSIGNED NOT NULL                 COMMENT '1=政府 2=学校',
  `category_id` TINYINT UNSIGNED NOT NULL                 COMMENT '1=普通 2=免试（请求参数名为 type）',
  `province_id` INT UNSIGNED     NOT NULL                 COMMENT '所属省 → region.id',
  `is_top`      TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '1=置顶 0=普通',
  `view_count`  INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '浏览量',
  `url`         VARCHAR(500)     NOT NULL DEFAULT ''      COMMENT '原文外链，允许空串',
  `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_filter` (`province_id`, `publisher`, `category_id`, `is_top`, `create_time`),
  CONSTRAINT `fk_policy_province` FOREIGN KEY (`province_id`) REFERENCES `region` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '政策表';


-- ----------------------------------------------------------------------------
-- 6. recruitment_plan 招生计划
--    school_name / school_logo 为冗余字段：院校详情页刷新时靠它兜底显示标题与校徽
-- ----------------------------------------------------------------------------
CREATE TABLE `recruitment_plan` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `school_id`        INT UNSIGNED     NOT NULL                 COMMENT '院校 → school.id',
  `school_name`      VARCHAR(60)      NOT NULL DEFAULT ''      COMMENT '冗余院校名（详情页兜底）',
  `school_logo`      VARCHAR(255)     NOT NULL DEFAULT ''      COMMENT '冗余校徽（详情页兜底）',
  `major_id`         INT UNSIGNED     NULL                     COMMENT '专业 → major.id，可为空',
  `major_name`       VARCHAR(60)      NOT NULL                 COMMENT '专业名称（冗余，前端直接展示）',
  `year`             SMALLINT UNSIGNED NOT NULL                COMMENT '招生年份',
  `plan_count`       INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '招生人数',
  `tuition_fee`      INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '学费（元/年），整数避免前端显示小数',
  `recruitment_type` TINYINT UNSIGNED NOT NULL DEFAULT 1       COMMENT '1=普通 2=免试',
  `create_time`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_school_year` (`school_id`, `year`),
  CONSTRAINT `fk_plan_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '招生计划表';


-- ----------------------------------------------------------------------------
-- 7. post 帖子
--    school_id = 0 表示「全省帖」，因此不能对 school 建外键
--    post_count 是评论数（字段名沿用前端，勿改）
--    status 为软删除标记，管理端「删除帖子」建议改 status 而非物理删除
-- ----------------------------------------------------------------------------
CREATE TABLE `post` (
  `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `user_id`    BIGINT UNSIGNED  NOT NULL                 COMMENT '作者 → user.id',
  `content`    VARCHAR(500)     NOT NULL                 COMMENT '正文，≤500 字',
  `image_url`  VARCHAR(255)     NOT NULL DEFAULT ''      COMMENT '配图地址，空串表示无图',
  `region_id`  INT UNSIGNED     NOT NULL                 COMMENT '省级 region.id',
  `school_id`  INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '0=全省帖（不建外键）',
  `view_count` INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '浏览量',
  `like_count` INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '点赞数（冗余计数）',
  `fav_count`  INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '收藏数（冗余计数）',
  `post_count` INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '评论数（字段名沿用前端）',
  `status`     TINYINT          NOT NULL DEFAULT 1       COMMENT '1=正常 0=已删除',
  `post_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_region_school` (`region_id`, `school_id`, `post_time` DESC),
  KEY `idx_user` (`user_id`, `post_time` DESC),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '论坛帖子表';


-- ----------------------------------------------------------------------------
-- 8. post_comment 评论
--    （表名加 post_ 前缀，避免 comment 触发关键字困扰）
-- ----------------------------------------------------------------------------
CREATE TABLE `post_comment` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `post_id`     BIGINT UNSIGNED  NOT NULL                 COMMENT '所属帖子 → post.id',
  `user_id`     BIGINT UNSIGNED  NOT NULL                 COMMENT '评论者 → user.id',
  `content`     VARCHAR(500)     NOT NULL                 COMMENT '评论内容',
  `like_count`  INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '点赞数',
  `status`      TINYINT          NOT NULL DEFAULT 1       COMMENT '1=正常 0=已删除',
  `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`, `create_time`),
  CONSTRAINT `fk_cm_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '帖子评论表';


-- ----------------------------------------------------------------------------
-- 9. post_like 帖子点赞（唯一索引实现点赞幂等）
-- ----------------------------------------------------------------------------
CREATE TABLE `post_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '点赞人 → user.id',
  `post_id`     BIGINT UNSIGNED NOT NULL                COMMENT '帖子 → post.id',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_post` (`post_id`),
  CONSTRAINT `fk_pl_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pl_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '帖子点赞表；uk_user_post 保证重复点赞幂等';


-- ----------------------------------------------------------------------------
-- 10. comment_like 评论点赞
-- ----------------------------------------------------------------------------
CREATE TABLE `comment_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '点赞人 → user.id',
  `comment_id`  BIGINT UNSIGNED NOT NULL                COMMENT '评论 → post_comment.id',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
  KEY `idx_comment` (`comment_id`),
  CONSTRAINT `fk_cl_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cl_comment` FOREIGN KEY (`comment_id`) REFERENCES `post_comment` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '评论点赞表；uk_user_comment 保证重复点赞幂等';


-- ----------------------------------------------------------------------------
-- 11. favorite 收藏（target_id 多态：1=帖子 2=政策，因此不建外键）
-- ----------------------------------------------------------------------------
CREATE TABLE `favorite` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED  NOT NULL                COMMENT '收藏人 → user.id',
  `target_type` TINYINT UNSIGNED NOT NULL                COMMENT '1=帖子 2=政策',
  `target_id`   BIGINT UNSIGNED  NOT NULL                COMMENT '目标 ID（帖子 ID 或政策 ID）',
  `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '收藏表（多态，不建外键）';


SET FOREIGN_KEY_CHECKS = 1;

-- 校验：应输出 11 张业务表
SELECT TABLE_NAME AS `表名`, TABLE_COMMENT AS `说明`
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'zsb'
ORDER BY TABLE_NAME;
