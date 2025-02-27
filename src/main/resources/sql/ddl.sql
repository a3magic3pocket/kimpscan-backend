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

-- SERVICE_LEADER_LOCK 테이블 생성
CREATE TABLE IF NOT EXISTS `service_leader_lock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(100) NOT NULl,
  `container_id` varchar(100) DEFAULT NULL,
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
   INDEX `service_leader_lock-name` (`name`)  -- 인덱스를 추가
)
;

-- KEY_VALUE_STORE 테이블 생성
CREATE TABLE IF NOT EXISTS `key_value_store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `key` varchar(100) NOT NULl UNIQUE,
  `value` TEXT NOT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;