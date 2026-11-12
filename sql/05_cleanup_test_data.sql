-- ============================================================================
-- 05_cleanup_test_data.sql —— 清理联调/冒烟测试留下的脏数据
--
-- 背景：之前接口冒烟测试用脚本注册了 stuXXXXXX / tuXXXXXX / test01 等账号，
--       反复发帖留下大量内容完全相同的测试帖，用户管理与论坛列表都很杂乱。
-- 做法：只删测试账号（uname 形如 stu*/tu*/test*）及其级联数据，
--       保留管理员账号与真实注册用户；帖子外键为 ON DELETE CASCADE。
-- 执行：mysql -uroot -p zsb < sql/05_cleanup_test_data.sql
-- ============================================================================

SET NAMES utf8mb4;

-- 1. 清掉重复的测试帖（内容完全一致的「有没有武汉纺织大学的学长」系列）
DELETE FROM `post`
 WHERE `content` LIKE '有没有武汉纺织大学的学长%';

-- 2. 删测试账号（会级联删除其帖子、评论、点赞、收藏）
--    按 uname 命名规则识别，不限 id：自动增长可能越过 1000（演示账号 1000~1012）
DELETE FROM `user`
 WHERE `ustate` <> 3
   AND `uname` REGEXP '^(stu|tu|test)[0-9A-Za-z_]*$';

-- 3. 删字典里的测试专业
DELETE FROM `major` WHERE `c_name` = '测试';

-- 校验
SELECT '剩余用户' AS 项目, COUNT(*) AS 数量 FROM `user`
UNION ALL SELECT '剩余帖子', COUNT(*) FROM `post`
UNION ALL SELECT '剩余专业', COUNT(*) FROM `major`;
