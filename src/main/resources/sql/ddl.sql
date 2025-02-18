-- DB 생성
CREATE DATABASE `kimpscan`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- USER 테이블 생성
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(255) NOT NULL,
  `oauth2_sub` varchar(255) NOT NULL,
  `oauth2_provider` varchar(100) NOT NULL,
  `is_active` tinyint(1) default 0 NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP
);
