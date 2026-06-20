-- =============================================================
--  CampusLink 校园竞赛组队与协作平台 数据库脚本
--  MySQL 8.0 / utf8mb4 / InnoDB
-- =============================================================

DROP DATABASE IF EXISTS campuslink;
CREATE DATABASE campuslink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campuslink;

-- ---------------------------------------------------------
-- 1. user 用户表
-- ---------------------------------------------------------
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
    `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `college`     VARCHAR(50)  DEFAULT NULL COMMENT '学院',
    `major`       VARCHAR(50)  DEFAULT NULL COMMENT '专业',
    `grade`       INT          DEFAULT NULL COMMENT '年级',
    `intro`       VARCHAR(500) DEFAULT NULL COMMENT '个人简介/项目经历',
    `reputation`  DECIMAL(4,2) NOT NULL DEFAULT 5.00 COMMENT '信誉分',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT/ADMIN',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------------
-- 2. skill 技能库表
-- ---------------------------------------------------------
CREATE TABLE `skill` (
    `id`       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`     VARCHAR(50) NOT NULL COMMENT '技能名称',
    `category` VARCHAR(20) DEFAULT NULL COMMENT '分类',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_skill_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能库表';

-- ---------------------------------------------------------
-- 3. user_skill 用户技能关联表
-- ---------------------------------------------------------
CREATE TABLE `user_skill` (
    `id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`  BIGINT NOT NULL COMMENT '用户 id',
    `skill_id` BIGINT NOT NULL COMMENT '技能 id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_skill` (`user_id`, `skill_id`),
    KEY `idx_us_skill` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能关联表';

-- ---------------------------------------------------------
-- 4. competition 竞赛表
-- ---------------------------------------------------------
CREATE TABLE `competition` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100)  NOT NULL COMMENT '竞赛名称',
    `type`        VARCHAR(20)   NOT NULL COMMENT 'PROGRAM/MODELING/INNOVATION/COURSE',
    `intro`       VARCHAR(1000) DEFAULT NULL COMMENT '简介',
    `deadline`    DATETIME      DEFAULT NULL COMMENT '报名截止时间',
    `creator_id`  BIGINT        DEFAULT NULL COMMENT '录入管理员 id',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_comp_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';

-- ---------------------------------------------------------
-- 5. team 队伍表
-- ---------------------------------------------------------
CREATE TABLE `team` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`           VARCHAR(100)  NOT NULL COMMENT '队伍名称',
    `competition_id` BIGINT        DEFAULT NULL COMMENT '关联竞赛 id',
    `leader_id`      BIGINT        NOT NULL COMMENT '队长 id',
    `intro`          VARCHAR(1000) DEFAULT NULL COMMENT '队伍简介',
    `total_size`     INT           NOT NULL DEFAULT 1 COMMENT '需要总人数',
    `current_size`   INT           NOT NULL DEFAULT 1 COMMENT '当前人数',
    `college`        VARCHAR(50)   DEFAULT NULL COMMENT '队伍所属学院',
    `status`         VARCHAR(20)   NOT NULL DEFAULT 'RECRUITING' COMMENT 'RECRUITING/FULL/CLOSED',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_team_comp` (`competition_id`),
    KEY `idx_team_leader` (`leader_id`),
    KEY `idx_team_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍表';

-- ---------------------------------------------------------
-- 6. team_recruit 招募岗位表
-- ---------------------------------------------------------
CREATE TABLE `team_recruit` (
    `id`       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`  BIGINT      NOT NULL COMMENT '队伍 id',
    `position` VARCHAR(50) NOT NULL COMMENT '岗位名称',
    `count`    INT         NOT NULL DEFAULT 1 COMMENT '需求人数',
    `filled`   INT         NOT NULL DEFAULT 0 COMMENT '已招人数',
    PRIMARY KEY (`id`),
    KEY `idx_recruit_team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招募岗位表';

-- ---------------------------------------------------------
-- 7. apply 申请表
-- ---------------------------------------------------------
CREATE TABLE `apply` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`       BIGINT       NOT NULL COMMENT '申请队伍 id',
    `user_id`       BIGINT       NOT NULL COMMENT '申请人 id',
    `self_intro`    VARCHAR(500) DEFAULT NULL COMMENT '自我介绍',
    `skill_desc`    VARCHAR(500) DEFAULT NULL COMMENT '技能说明',
    `profile_link`  VARCHAR(255) DEFAULT NULL COMMENT '个人主页链接',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝理由',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`id`),
    KEY `idx_apply_team` (`team_id`),
    KEY `idx_apply_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请表';

-- ---------------------------------------------------------
-- 8. team_member 队伍成员表
-- ---------------------------------------------------------
CREATE TABLE `team_member` (
    `id`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`   BIGINT      NOT NULL COMMENT '队伍 id',
    `user_id`   BIGINT      NOT NULL COMMENT '成员 id',
    `role`      VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'LEADER/MEMBER',
    `join_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_user` (`team_id`, `user_id`),
    KEY `idx_member_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍成员表';

-- ---------------------------------------------------------
-- 9. task 任务表
-- ---------------------------------------------------------
CREATE TABLE `task` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`     BIGINT        NOT NULL COMMENT '所属队伍 id',
    `title`       VARCHAR(100)  NOT NULL COMMENT '任务标题',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '任务描述',
    `assignee_id` BIGINT        DEFAULT NULL COMMENT '负责人 id',
    `deadline`    DATETIME      DEFAULT NULL COMMENT '截止时间',
    `status`      VARCHAR(20)   NOT NULL DEFAULT 'TODO' COMMENT 'TODO/DOING/DONE',
    `sort_order`  INT           NOT NULL DEFAULT 0 COMMENT '看板内排序',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- ---------------------------------------------------------
-- 10. notice 公告表
-- ---------------------------------------------------------
CREATE TABLE `notice` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`     BIGINT        NOT NULL COMMENT '所属队伍 id',
    `author_id`   BIGINT        NOT NULL COMMENT '发布者 id',
    `title`       VARCHAR(100)  NOT NULL COMMENT '标题',
    `content`     VARCHAR(2000) DEFAULT NULL COMMENT '内容',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_notice_team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ---------------------------------------------------------
-- 11. message 站内消息表
-- ---------------------------------------------------------
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `receiver_id` BIGINT       NOT NULL COMMENT '接收人 id',
    `type`        VARCHAR(20)  NOT NULL COMMENT 'APPLY/AUDIT/NOTICE/TASK',
    `content`     VARCHAR(500) DEFAULT NULL COMMENT '消息内容',
    `ref_id`      BIGINT       DEFAULT NULL COMMENT '关联业务 id',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读/1已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    KEY `idx_msg_receiver` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ---------------------------------------------------------
-- 12. evaluation 互评表
-- ---------------------------------------------------------
CREATE TABLE `evaluation` (
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`        BIGINT   NOT NULL COMMENT '所属队伍 id',
    `from_user_id`   BIGINT   NOT NULL COMMENT '评价人 id',
    `to_user_id`     BIGINT   NOT NULL COMMENT '被评价人 id',
    `responsibility` INT      NOT NULL COMMENT '责任心 1-5',
    `tech`           INT      NOT NULL COMMENT '技术能力 1-5',
    `communication`  INT      NOT NULL COMMENT '沟通能力 1-5',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_eval` (`team_id`, `from_user_id`, `to_user_id`),
    KEY `idx_eval_to` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互评表';

-- =============================================================
--  初始化数据
-- =============================================================

-- 用户（密码均为 123456 的 BCrypt 值）
-- $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
INSERT INTO `user` (`username`,`password`,`nickname`,`email`,`college`,`major`,`grade`,`intro`,`reputation`,`role`) VALUES
('admin','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','管理员','admin@hit.edu.cn','软件学院','软件工程',2022,'平台管理员',5.00,'ADMIN'),
('alice','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','Alice','alice@hit.edu.cn','软件学院','软件工程',2023,'热爱算法与后端开发，参加过蓝桥杯省一。',4.80,'STUDENT'),
('bob','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','Bob','bob@hit.edu.cn','计算机学院','计算机科学',2023,'前端工程师，熟悉 Vue 全家桶。',4.50,'STUDENT'),
('carol','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','Carol','carol@hit.edu.cn','新媒体学院','数字媒体',2024,'UI/UX 设计师，负责过多个项目视觉。',4.90,'STUDENT');

-- 技能库
INSERT INTO `skill` (`name`,`category`) VALUES
('Java','后端'),('SpringBoot','后端'),('MySQL','后端'),('Redis','后端'),
('Vue','前端'),('React','前端'),('JavaScript','前端'),('CSS','前端'),
('Python','算法'),('算法','算法'),('数学建模','算法'),('机器学习','算法'),
('UI 设计','设计'),('产品','设计'),('文档写作','设计');

-- 用户技能
INSERT INTO `user_skill` (`user_id`,`skill_id`) VALUES
(2,1),(2,2),(2,9),(2,10),          -- alice: Java SpringBoot Python 算法
(3,5),(3,7),(3,8),                 -- bob: Vue JS CSS
(4,13),(4,14),(4,15);              -- carol: UI 产品 文档

-- 竞赛
INSERT INTO `competition` (`name`,`type`,`intro`,`deadline`,`creator_id`) VALUES
('蓝桥杯程序设计大赛','PROGRAM','面向高校的程序设计竞赛，分 Java/C++/Python 组。','2026-09-30 23:59:59',1),
('全国大学生数学建模竞赛','MODELING','三天完成建模+编程+论文，团队赛。','2026-09-15 23:59:59',1),
('"互联网+"创新创业大赛','INNOVATION','商业计划与产品创新比拼。','2026-10-20 23:59:59',1),
('软件工程综合课程设计','COURSE','课程内组队完成完整软件项目。','2026-07-10 23:59:59',1);

-- 队伍
INSERT INTO `team` (`name`,`competition_id`,`leader_id`,`intro`,`total_size`,`current_size`,`college`,`status`) VALUES
('代码先锋队',1,2,'冲刺蓝桥杯国一，目前缺前端与 UI。',4,2,'软件学院','RECRUITING'),
('建模梦之队',2,2,'数学建模国赛，需要擅长论文写作的同学。',3,1,'软件学院','RECRUITING');

-- 队伍成员
INSERT INTO `team_member` (`team_id`,`user_id`,`role`) VALUES
(1,2,'LEADER'),(1,3,'MEMBER'),
(2,2,'LEADER');

-- 招募岗位
INSERT INTO `team_recruit` (`team_id`,`position`,`count`,`filled`) VALUES
(1,'Vue 开发',1,0),(1,'UI 设计',1,0),
(2,'论文写作',1,0),(2,'建模手',1,0);
