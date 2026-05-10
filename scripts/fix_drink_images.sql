-- BUG-024: 修复过期饮品图片链接
-- source.unsplash.com 已停用，替换为 picsum.photos 稳定占位图
-- 使用：mysql -uroot -p123456 Android_health_db < fix_drink_images.sql

SET NAMES utf8mb4;

UPDATE drinks
SET image_url = CONCAT('https://picsum.photos/seed/', drink_id, '/600/600'),
    updated_at = NOW()
WHERE image_url LIKE '%source.unsplash.com%';
