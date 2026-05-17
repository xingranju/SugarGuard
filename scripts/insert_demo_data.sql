-- ============================================================
-- testuser(id=20) 展示数据插入 + huangpeng头像修复
-- 用于答辩录屏展示
-- ============================================================

-- 1. 修复 huangpeng(id=33) 头像 (原URL返回404)
UPDATE users SET avatar_url = 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=face'
WHERE id = 33;

-- 2. 删除 testuser 旧的打卡记录，重建15天连续打卡
DELETE FROM check_ins WHERE user_id = 20;

INSERT INTO check_ins (user_id, check_in_date, streak, sugar_intake, within_limit, notes, created_at) VALUES
(20, '2026-05-03', 1,  22.5, 1, '今日糖分摄入控制良好', '2026-05-03 08:30:00'),
(20, '2026-05-04', 2,  18.0, 1, '多喝水少喝奶茶',       '2026-05-04 09:15:00'),
(20, '2026-05-05', 3,  30.2, 1, '午餐甜品稍多',         '2026-05-05 08:45:00'),
(20, '2026-05-06', 4,  15.8, 1, '全天无糖饮品',         '2026-05-06 07:50:00'),
(20, '2026-05-07', 5,  28.6, 1, '下午茶选了无糖绿茶',   '2026-05-07 09:00:00'),
(20, '2026-05-08', 6,  20.3, 1, '坚持控糖第6天',        '2026-05-08 08:20:00'),
(20, '2026-05-09', 7,  24.1, 1, '一周打卡达成!',        '2026-05-09 08:10:00'),
(20, '2026-05-10', 8,  19.5, 1, '用水果替代甜食',       '2026-05-10 09:30:00'),
(20, '2026-05-11', 9,  35.0, 1, '聚餐稍微放纵',         '2026-05-11 10:00:00'),
(20, '2026-05-12', 10, 12.8, 1, '今天吃得很清淡',       '2026-05-12 08:00:00'),
(20, '2026-05-13', 11, 21.4, 1, '保持均衡饮食',         '2026-05-13 08:40:00'),
(20, '2026-05-14', 12, 18.9, 1, '控糖习惯养成中',       '2026-05-14 09:05:00'),
(20, '2026-05-15', 13, 25.7, 1, '两周打卡倒计时',       '2026-05-15 08:30:00'),
(20, '2026-05-16', 14, 16.2, 1, '连续14天达成!',        '2026-05-16 08:15:00'),
(20, '2026-05-17', 15, 20.0, 1, '半月打卡纪录!',        '2026-05-17 08:00:00');

-- 3. 为 testuser 颁发更多徽章
DELETE FROM user_badges WHERE user_id = 20;

INSERT INTO user_badges (user_id, badge_id, progress, earned_at) VALUES
(20, 9,  100, '2026-05-03 08:30:00'),
(20, 10, 100, '2026-05-05 08:45:00'),
(20, 11, 100, '2026-05-09 08:10:00'),
(20, 12, 100, '2026-05-16 08:15:00'),
(20, 15, 100, '2026-05-12 08:00:00'),
(20, 20, 100, '2026-05-05 08:45:00'),
(20, 21, 100, '2026-05-09 08:10:00'),
(20, 22, 100, '2026-05-16 08:15:00');

-- 4. 更新 testuser 近期每日健康记录（补充水分/运动/心情）
UPDATE daily_health_records SET
  water_intake = 2200, exercise_minutes = 45, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-17';

UPDATE daily_health_records SET
  water_intake = 2000, exercise_minutes = 30, mood = '平静'
WHERE user_id = 20 AND record_date = '2026-05-16';

UPDATE daily_health_records SET
  water_intake = 1800, exercise_minutes = 50, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-15';

UPDATE daily_health_records SET
  water_intake = 2500, exercise_minutes = 60, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-14';

UPDATE daily_health_records SET
  water_intake = 1600, exercise_minutes = 25, mood = '一般'
WHERE user_id = 20 AND record_date = '2026-05-13';

UPDATE daily_health_records SET
  water_intake = 2100, exercise_minutes = 40, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-12';

UPDATE daily_health_records SET
  water_intake = 1900, exercise_minutes = 35, mood = '平静'
WHERE user_id = 20 AND record_date = '2026-05-11';

UPDATE daily_health_records SET
  water_intake = 2300, exercise_minutes = 55, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-10';

UPDATE daily_health_records SET
  water_intake = 1700, exercise_minutes = 30, mood = '平静'
WHERE user_id = 20 AND record_date = '2026-05-09';

UPDATE daily_health_records SET
  water_intake = 2000, exercise_minutes = 45, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-08';

UPDATE daily_health_records SET
  water_intake = 1500, exercise_minutes = 20, mood = '一般'
WHERE user_id = 20 AND record_date = '2026-05-07';

UPDATE daily_health_records SET
  water_intake = 2400, exercise_minutes = 50, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-06';

UPDATE daily_health_records SET
  water_intake = 1800, exercise_minutes = 35, mood = '平静'
WHERE user_id = 20 AND record_date = '2026-05-05';

UPDATE daily_health_records SET
  water_intake = 2100, exercise_minutes = 40, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-04';

UPDATE daily_health_records SET
  water_intake = 2000, exercise_minutes = 30, mood = '开心'
WHERE user_id = 20 AND record_date = '2026-05-03';

-- 5. 确认结果
SELECT '=== 打卡记录 ===' AS info;
SELECT check_in_date, streak, sugar_intake, within_limit, notes FROM check_ins WHERE user_id=20 ORDER BY check_in_date;

SELECT '=== 徽章 ===' AS info;
SELECT ub.id, b.name, b.category, b.threshold, ub.earned_at
FROM user_badges ub JOIN badges b ON ub.badge_id = b.id
WHERE ub.user_id = 20 ORDER BY ub.earned_at;

SELECT '=== huangpeng头像 ===' AS info;
SELECT id, username, avatar_url FROM users WHERE id = 33;
