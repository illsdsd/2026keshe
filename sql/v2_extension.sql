-- =============================================================
--  CampusLink v2 扩容增量脚本
--  执行前提：v1 主脚本 sql/campuslink.sql 已成功执行
--  执行命令：mysql -uroot -proot campuslink < sql/v2_extension.sql
--  原则：只新增/追加，不删除任何原表与原列
-- =============================================================

USE campuslink;

-- ---------------------------------------------------------
-- v1 表追加可空列（不破坏原行为）
-- ---------------------------------------------------------
ALTER TABLE `team`
  ADD COLUMN `archived_time` DATETIME DEFAULT NULL COMMENT '归档时间' AFTER `status`;

ALTER TABLE `team_member`
  ADD COLUMN `deputy_time` DATETIME DEFAULT NULL COMMENT '任命副队长时间';

ALTER TABLE `task`
  ADD COLUMN `priority` VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级 LOW/MEDIUM/HIGH' AFTER `status`,
  ADD COLUMN `tags`     VARCHAR(255) DEFAULT NULL COMMENT '逗号分隔标签',
  ADD COLUMN `parent_id` BIGINT DEFAULT NULL COMMENT '父任务 id（null 表示主任务）',
  ADD KEY `idx_task_parent` (`parent_id`);

ALTER TABLE `notice`
  ADD COLUMN `publish_at` DATETIME DEFAULT NULL COMMENT '定时发布时间，null = 即时',
  ADD COLUMN `scheduled`  TINYINT NOT NULL DEFAULT 0 COMMENT '0即时/1定时';

ALTER TABLE `user`
  ADD COLUMN `enabled`    TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用/1启用' AFTER `role`,
  ADD COLUMN `phone`      VARCHAR(20) DEFAULT NULL COMMENT '手机号';

-- ---------------------------------------------------------
-- 13. user_login_log 登录日志
-- ---------------------------------------------------------
CREATE TABLE `user_login_log` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT DEFAULT NULL COMMENT '用户 id，匿名登录失败时可为空',
    `username`    VARCHAR(50) NOT NULL COMMENT '登录账号',
    `ip`          VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
    `device`      VARCHAR(255) DEFAULT NULL COMMENT 'User-Agent',
    `success`     TINYINT NOT NULL DEFAULT 1 COMMENT '0失败/1成功',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `login_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_user_time` (`user_id`, `login_time` DESC),
    KEY `idx_log_username` (`username`, `login_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录日志表';

-- ---------------------------------------------------------
-- 14. user_privacy 用户隐私设置
-- ---------------------------------------------------------
CREATE TABLE `user_privacy` (
    `user_id`            BIGINT NOT NULL COMMENT '用户 id',
    `profile_public`     TINYINT NOT NULL DEFAULT 1 COMMENT '主页是否公开',
    `reputation_public`  TINYINT NOT NULL DEFAULT 1 COMMENT '信誉分是否公开',
    `push_apply`         TINYINT NOT NULL DEFAULT 1 COMMENT '申请消息开关',
    `push_audit`         TINYINT NOT NULL DEFAULT 1 COMMENT '审核消息开关',
    `push_task`          TINYINT NOT NULL DEFAULT 1 COMMENT '任务消息开关',
    `push_notice`        TINYINT NOT NULL DEFAULT 1 COMMENT '公告消息开关',
    `push_eval`          TINYINT NOT NULL DEFAULT 1 COMMENT '评价消息开关',
    `update_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐私与推送设置表';

-- ---------------------------------------------------------
-- 15. user_project 个人作品集
-- ---------------------------------------------------------
CREATE TABLE `user_project` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT NOT NULL COMMENT '作者 id',
    `title`       VARCHAR(100) NOT NULL COMMENT '项目名称',
    `cover`       VARCHAR(255) DEFAULT NULL COMMENT '封面 URL',
    `intro`       VARCHAR(1000) DEFAULT NULL COMMENT '项目描述',
    `link`        VARCHAR(255) DEFAULT NULL COMMENT '项目链接',
    `award`       VARCHAR(255) DEFAULT NULL COMMENT '获奖情况',
    `role`        VARCHAR(100) DEFAULT NULL COMMENT '项目分工',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户作品集表';

-- ---------------------------------------------------------
-- 16. user_certificate 用户证书
-- ---------------------------------------------------------
CREATE TABLE `user_certificate` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT NOT NULL COMMENT '所属用户',
    `name`        VARCHAR(100) NOT NULL COMMENT '证书名称',
    `image_url`   VARCHAR(255) DEFAULT NULL COMMENT '证书图片地址',
    `obtain_date` DATE DEFAULT NULL COMMENT '获得日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_cert_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户证书表';

-- ---------------------------------------------------------
-- 17. user_favorite 用户收藏
-- ---------------------------------------------------------
CREATE TABLE `user_favorite` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT NOT NULL COMMENT '收藏者 id',
    `ref_type`    VARCHAR(20) NOT NULL COMMENT 'COMPETITION/TEAM',
    `ref_id`      BIGINT NOT NULL COMMENT '被收藏对象 id',
    `note`        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_favorite` (`user_id`, `ref_type`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ---------------------------------------------------------
-- 18. auth_verify_code 邮箱验证码
-- ---------------------------------------------------------
CREATE TABLE `auth_verify_code` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `email`       VARCHAR(100) NOT NULL COMMENT '邮箱',
    `scene`       VARCHAR(30) NOT NULL DEFAULT 'RESET_PWD' COMMENT '场景 RESET_PWD/CHANGE_EMAIL',
    `code`        VARCHAR(10) NOT NULL COMMENT '6 位验证码',
    `expire_at`   DATETIME NOT NULL COMMENT '过期时间',
    `used`        TINYINT NOT NULL DEFAULT 0 COMMENT '0未用/1已用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (`id`),
    KEY `idx_code_email` (`email`, `scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码表';

-- ---------------------------------------------------------
-- 19. competition_register 赛事报名
-- ---------------------------------------------------------
CREATE TABLE `competition_register` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `competition_id` BIGINT NOT NULL COMMENT '竞赛 id',
    `team_id`        BIGINT NOT NULL COMMENT '队伍 id',
    `applicant_id`   BIGINT NOT NULL COMMENT '提交人 id（一般为队长）',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `status`         VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `audit_reason`   VARCHAR(255) DEFAULT NULL COMMENT '审核理由',
    `audit_time`     DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comp_team` (`competition_id`, `team_id`),
    KEY `idx_register_comp` (`competition_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事报名表';

-- ---------------------------------------------------------
-- 20. competition_attachment 赛事附件
-- ---------------------------------------------------------
CREATE TABLE `competition_attachment` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `competition_id` BIGINT NOT NULL COMMENT '竞赛 id',
    `file_id`        BIGINT NOT NULL COMMENT '文件 id（file_object.id）',
    `name`           VARCHAR(255) NOT NULL COMMENT '展示名称',
    `category`       VARCHAR(30) DEFAULT 'OTHER' COMMENT 'NOTICE/RULE/FORM/OTHER',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_attach_comp` (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事附件表';

-- ---------------------------------------------------------
-- 21. competition_news 赛事资讯/公告
-- ---------------------------------------------------------
CREATE TABLE `competition_news` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `competition_id` BIGINT NOT NULL COMMENT '竞赛 id',
    `title`          VARCHAR(150) NOT NULL COMMENT '标题',
    `content`        TEXT COMMENT '内容',
    `author_id`      BIGINT DEFAULT NULL COMMENT '发布人',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_news_comp` (`competition_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事资讯/公告表';

-- ---------------------------------------------------------
-- 22. team_file 队伍文件库
-- ---------------------------------------------------------
CREATE TABLE `team_file` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`     BIGINT NOT NULL COMMENT '队伍 id',
    `folder`      VARCHAR(255) NOT NULL DEFAULT '/' COMMENT '虚拟文件夹路径',
    `file_id`     BIGINT NOT NULL COMMENT '文件 id（file_object.id）',
    `uploader_id` BIGINT NOT NULL COMMENT '上传人',
    `name`        VARCHAR(255) NOT NULL COMMENT '展示名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_team_file` (`team_id`, `folder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍文件库表';

-- ---------------------------------------------------------
-- 23. team_post 队伍动态
-- ---------------------------------------------------------
CREATE TABLE `team_post` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`        BIGINT NOT NULL COMMENT '队伍 id',
    `author_id`      BIGINT NOT NULL COMMENT '发布人',
    `content`        VARCHAR(2000) NOT NULL COMMENT '动态内容',
    `image_urls`     VARCHAR(2000) DEFAULT NULL COMMENT '图片，逗号分隔',
    `like_count`     INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count`  INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_team_time` (`team_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍动态墙表';

CREATE TABLE `team_post_comment` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`     BIGINT NOT NULL COMMENT '动态 id',
    `author_id`   BIGINT NOT NULL COMMENT '评论人',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_comment` (`post_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

CREATE TABLE `team_post_like` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`     BIGINT NOT NULL COMMENT '动态 id',
    `user_id`     BIGINT NOT NULL COMMENT '点赞人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_like` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

-- ---------------------------------------------------------
-- 24. team_blacklist 队伍黑名单
-- ---------------------------------------------------------
CREATE TABLE `team_blacklist` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`     BIGINT NOT NULL COMMENT '队伍 id',
    `user_id`     BIGINT NOT NULL COMMENT '被拉黑用户',
    `reason`      VARCHAR(255) DEFAULT NULL COMMENT '原因',
    `operator_id` BIGINT NOT NULL COMMENT '操作人（队长）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_blacklist` (`team_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍黑名单表';

-- ---------------------------------------------------------
-- 25. task_comment 任务评论
-- ---------------------------------------------------------
CREATE TABLE `task_comment` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`     BIGINT NOT NULL COMMENT '任务 id',
    `author_id`   BIGINT NOT NULL COMMENT '评论人',
    `content`     VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_comment` (`task_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务评论表';

-- ---------------------------------------------------------
-- 26. task_template 任务模板
-- ---------------------------------------------------------
CREATE TABLE `task_template` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_id`    BIGINT NOT NULL COMMENT '创建者',
    `name`        VARCHAR(100) NOT NULL COMMENT '模板名称',
    `payload`     TEXT NOT NULL COMMENT '任务结构 JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板表';

-- ---------------------------------------------------------
-- 27. worklog 工时填报
-- ---------------------------------------------------------
CREATE TABLE `worklog` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`     BIGINT NOT NULL COMMENT '队伍 id',
    `task_id`     BIGINT DEFAULT NULL COMMENT '任务 id（可空，表示通用工时）',
    `user_id`     BIGINT NOT NULL COMMENT '填报人',
    `work_date`   DATE NOT NULL COMMENT '工作日',
    `hours`       DECIMAL(4,1) NOT NULL COMMENT '工时（小时，0.5 步进）',
    `content`     VARCHAR(500) DEFAULT NULL COMMENT '工作内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '填报时间',
    PRIMARY KEY (`id`),
    KEY `idx_worklog_team_date` (`team_id`, `work_date`),
    KEY `idx_worklog_user_date` (`user_id`, `work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时填报表';

-- ---------------------------------------------------------
-- 28. evaluation_reply 评价回复
-- ---------------------------------------------------------
CREATE TABLE `evaluation_reply` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `evaluation_id` BIGINT NOT NULL COMMENT '评价 id',
    `author_id`     BIGINT NOT NULL COMMENT '回复人',
    `content`       VARCHAR(500) NOT NULL COMMENT '回复内容',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    PRIMARY KEY (`id`),
    KEY `idx_reply_eval` (`evaluation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价回复表';

-- ---------------------------------------------------------
-- 29. report 举报表
-- ---------------------------------------------------------
CREATE TABLE `report` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `target_type`    VARCHAR(20) NOT NULL COMMENT 'EVALUATION/TEAM/USER/POST',
    `target_id`      BIGINT NOT NULL COMMENT '被举报对象 id',
    `reporter_id`    BIGINT NOT NULL COMMENT '举报人',
    `reason`         VARCHAR(500) NOT NULL COMMENT '举报原因',
    `status`         VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/HANDLED/REJECTED',
    `handle_remark`  VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `handler_id`     BIGINT DEFAULT NULL COMMENT '处理人',
    `handle_time`    DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_status` (`status`, `create_time` DESC),
    KEY `idx_report_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- ---------------------------------------------------------
-- 30. file_object 通用文件元数据
-- ---------------------------------------------------------
CREATE TABLE `file_object` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_id`       BIGINT NOT NULL COMMENT '上传者 id',
    `scope`          VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER/TEAM/COMPETITION/PUBLIC',
    `business_id`    BIGINT DEFAULT NULL COMMENT '业务关联 id（如 team_id/competition_id）',
    `original_name`  VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_path`    VARCHAR(500) NOT NULL COMMENT '磁盘存储相对路径',
    `extension`      VARCHAR(20) DEFAULT NULL COMMENT '扩展名',
    `mime`           VARCHAR(100) DEFAULT NULL COMMENT 'MIME 类型',
    `size_bytes`     BIGINT NOT NULL DEFAULT 0 COMMENT '大小（字节）',
    `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    `expire_at`      DATETIME DEFAULT NULL COMMENT '过期时间（null 表示永久）',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_owner` (`owner_id`, `create_time` DESC),
    KEY `idx_file_scope_biz` (`scope`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用文件元数据表';

-- ---------------------------------------------------------
-- 31. operation_log 操作日志
-- ---------------------------------------------------------
CREATE TABLE `operation_log` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT DEFAULT NULL COMMENT '操作人',
    `username`    VARCHAR(50) DEFAULT NULL COMMENT '操作人账号',
    `method`      VARCHAR(10) NOT NULL COMMENT 'HTTP method',
    `path`        VARCHAR(255) NOT NULL COMMENT 'URI',
    `params`      TEXT COMMENT '入参（已对敏感字段脱敏）',
    `ip`          VARCHAR(64) DEFAULT NULL COMMENT 'IP',
    `status`      VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAIL',
    `error_msg`   VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `cost_ms`     INT NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    KEY `idx_oplog_user_time` (`user_id`, `create_time` DESC),
    KEY `idx_oplog_status_time` (`status`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ---------------------------------------------------------
-- 32. dict 数据字典
-- ---------------------------------------------------------
CREATE TABLE `dict` (
    `id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`   VARCHAR(50) NOT NULL COMMENT '字典分组',
    `code`   VARCHAR(50) NOT NULL COMMENT '编码',
    `label`  VARCHAR(100) NOT NULL COMMENT '展示名',
    `sort`   INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_code` (`type`, `code`),
    KEY `idx_dict_type_sort` (`type`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ---------------------------------------------------------
-- 33. system_notice 平台全局公告
-- ---------------------------------------------------------
CREATE TABLE `system_notice` (
    `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        VARCHAR(150) NOT NULL COMMENT '标题',
    `content`      TEXT COMMENT '内容',
    `publisher_id` BIGINT NOT NULL COMMENT '发布管理员',
    `publish_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台全局公告表';

-- =============================================================
--  初始化数据：数据字典 + 默认隐私
-- =============================================================
INSERT INTO `dict` (`type`,`code`,`label`,`sort`) VALUES
('TASK_PRIORITY','LOW','低',1),('TASK_PRIORITY','MEDIUM','中',2),('TASK_PRIORITY','HIGH','高',3),
('COMP_TYPE','PROGRAM','程序设计',1),('COMP_TYPE','MODELING','数学建模',2),('COMP_TYPE','INNOVATION','创新创业',3),('COMP_TYPE','COURSE','课程设计',4),
('TEAM_ROLE','LEADER','队长',1),('TEAM_ROLE','LEADER_DEPUTY','副队长',2),('TEAM_ROLE','MEMBER','成员',3),
('REPORT_TARGET','EVALUATION','评价',1),('REPORT_TARGET','TEAM','队伍',2),('REPORT_TARGET','USER','用户',3),('REPORT_TARGET','POST','动态',4),
('EVAL_DIM','responsibility','责任心',1),('EVAL_DIM','tech','技术能力',2),('EVAL_DIM','communication','沟通能力',3);

INSERT INTO `user_privacy` (`user_id`,`profile_public`,`reputation_public`)
SELECT `id`, 1, 1 FROM `user`;
