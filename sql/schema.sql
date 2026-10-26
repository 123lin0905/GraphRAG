-- =====================================================================
-- 基于3D高斯泼溅与GraphRAG的三星堆文物数字孪生与智能导览系统
-- 数据库结构脚本（MySQL 8.x / MariaDB 10.6+ 兼容）
-- 字符集：utf8mb4    存储引擎：InnoDB
-- 约定：
--   1. 所有业务表均含 id(主键自增)、created_at、updated_at、deleted(逻辑删除 0-正常 1-删除)
--   2. 关联关系使用外键约束保证引用完整性，常用查询字段建立普通/联合索引
--   3. 因采用逻辑删除，关联表不使用唯一约束，唯一性由业务层保证
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sanxingdui_twin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE sanxingdui_twin;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 按依赖逆序删除，便于重复执行
DROP TABLE IF EXISTS visit_statistics;
DROP TABLE IF EXISTS system_config;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS exhibition_event;
DROP TABLE IF EXISTS restoration_record;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS visitor;
DROP TABLE IF EXISTS guide_route_node;
DROP TABLE IF EXISTS guide_route;
DROP TABLE IF EXISTS qa_message;
DROP TABLE IF EXISTS qa_session;
DROP TABLE IF EXISTS artifact_literature;
DROP TABLE IF EXISTS literature;
DROP TABLE IF EXISTS kg_relation;
DROP TABLE IF EXISTS kg_entity;
DROP TABLE IF EXISTS model_3d;
DROP TABLE IF EXISTS artifact_image;
DROP TABLE IF EXISTS artifact;
DROP TABLE IF EXISTS material;
DROP TABLE IF EXISTS excavation_pit;
DROP TABLE IF EXISTS dynasty_period;
DROP TABLE IF EXISTS artifact_category;
DROP TABLE IF EXISTS showcase;
DROP TABLE IF EXISTS exhibition_hall;
DROP TABLE IF EXISTS museum;
DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 一、系统权限模块（RBAC）
-- =====================================================================

-- 1. 系统用户表
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password        VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密）',
    nickname        VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    avatar          VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    gender          TINYINT      DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    user_type       VARCHAR(20)  NOT NULL DEFAULT 'VISITOR' COMMENT '用户类型：ADMIN-管理员 CURATOR-馆员 VISITOR-游客',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用 1-启用',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(64)  DEFAULT NULL COMMENT '最后登录IP',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_user_type (user_type),
    KEY idx_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统用户表';

-- 2. 角色表
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码（如 ADMIN）',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

-- 3. 权限表（菜单/按钮/接口）
CREATE TABLE sys_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级权限ID，0表示顶级',
    perm_code   VARCHAR(100) NOT NULL COMMENT '权限标识（如 artifact:edit）',
    perm_name   VARCHAR(50)  NOT NULL COMMENT '权限名称',
    perm_type   VARCHAR(20)  NOT NULL DEFAULT 'MENU' COMMENT '权限类型：MENU-菜单 BUTTON-按钮 API-接口',
    path        VARCHAR(200) DEFAULT NULL COMMENT '前端路由或接口路径',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '菜单图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';

-- 4. 用户-角色关联表
CREATE TABLE sys_user_role (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    role_id     BIGINT   NOT NULL COMMENT '角色ID',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_ur_user (user_id),
    KEY idx_ur_role (role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色关联表';

-- 5. 角色-权限关联表
CREATE TABLE sys_role_permission (
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id       BIGINT   NOT NULL COMMENT '角色ID',
    permission_id BIGINT   NOT NULL COMMENT '权限ID',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_rp_role (role_id),
    KEY idx_rp_perm (permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色权限关联表';

-- 6. 操作日志表
CREATE TABLE sys_operation_log (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id       BIGINT        DEFAULT NULL COMMENT '操作用户ID（匿名为空）',
    username      VARCHAR(50)   DEFAULT NULL COMMENT '操作用户名',
    module        VARCHAR(50)   DEFAULT NULL COMMENT '业务模块',
    operation     VARCHAR(100)  DEFAULT NULL COMMENT '操作描述',
    method        VARCHAR(200)  DEFAULT NULL COMMENT '调用的Java方法',
    request_uri   VARCHAR(255)  DEFAULT NULL COMMENT '请求URI',
    request_method VARCHAR(10)  DEFAULT NULL COMMENT 'HTTP方法',
    request_params TEXT         COMMENT '请求参数（截断保存）',
    ip            VARCHAR(64)   DEFAULT NULL COMMENT '客户端IP',
    status        TINYINT       NOT NULL DEFAULT 1 COMMENT '执行结果：0-失败 1-成功',
    error_msg     VARCHAR(1000) DEFAULT NULL COMMENT '异常信息',
    cost_ms       BIGINT        DEFAULT NULL COMMENT '耗时（毫秒）',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_log_user (user_id),
    KEY idx_log_created (created_at),
    KEY idx_log_module (module)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统操作日志表';

-- =====================================================================
-- 二、场馆空间模块（数字孪生空间层级：博物馆 → 展厅 → 展柜）
-- =====================================================================

-- 7. 博物馆表
CREATE TABLE museum (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(100)  NOT NULL COMMENT '博物馆名称',
    short_name    VARCHAR(50)   DEFAULT NULL COMMENT '简称',
    address       VARCHAR(255)  DEFAULT NULL COMMENT '地址',
    longitude     DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    latitude      DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    open_time     VARCHAR(100)  DEFAULT NULL COMMENT '开放时间说明',
    phone         VARCHAR(30)   DEFAULT NULL COMMENT '联系电话',
    introduction  TEXT          COMMENT '博物馆简介',
    cover_url     VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
    daily_capacity INT          DEFAULT NULL COMMENT '每日最大接待量（人）',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '博物馆表';

-- 8. 展厅表
CREATE TABLE exhibition_hall (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    museum_id    BIGINT        NOT NULL COMMENT '所属博物馆ID',
    hall_code    VARCHAR(30)   NOT NULL COMMENT '展厅编号',
    name         VARCHAR(100)  NOT NULL COMMENT '展厅名称',
    theme        VARCHAR(200)  DEFAULT NULL COMMENT '展陈主题',
    floor        INT           DEFAULT 1 COMMENT '所在楼层',
    area         DECIMAL(10,2) DEFAULT NULL COMMENT '面积（平方米）',
    description  TEXT          COMMENT '展厅介绍',
    cover_url    VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
    scene_url    VARCHAR(255)  DEFAULT NULL COMMENT '展厅三维场景文件URL（3DGS/全景）',
    map_x        DECIMAL(10,2) DEFAULT NULL COMMENT '平面导览图X坐标',
    map_y        DECIMAL(10,2) DEFAULT NULL COMMENT '平面导览图Y坐标',
    sort         INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-闭馆 1-开放',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_hall_museum (museum_id),
    CONSTRAINT fk_hall_museum FOREIGN KEY (museum_id) REFERENCES museum (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '展厅表';

-- 9. 展柜表
CREATE TABLE showcase (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    hall_id      BIGINT        NOT NULL COMMENT '所属展厅ID',
    case_code    VARCHAR(30)   NOT NULL COMMENT '展柜编号',
    name         VARCHAR(100)  DEFAULT NULL COMMENT '展柜名称',
    case_type    VARCHAR(30)   DEFAULT NULL COMMENT '展柜类型：独立柜/通柜/壁柜/中心柜',
    pos_x        DECIMAL(10,3) DEFAULT NULL COMMENT '展厅坐标系X（米）',
    pos_y        DECIMAL(10,3) DEFAULT NULL COMMENT '展厅坐标系Y（米）',
    pos_z        DECIMAL(10,3) DEFAULT NULL COMMENT '展厅坐标系Z（米）',
    temperature  DECIMAL(5,2)  DEFAULT NULL COMMENT '柜内温度（℃，环境监测）',
    humidity     DECIMAL(5,2)  DEFAULT NULL COMMENT '柜内相对湿度（%，环境监测）',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_case_hall (hall_id),
    CONSTRAINT fk_case_hall FOREIGN KEY (hall_id) REFERENCES exhibition_hall (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '展柜表';

-- =====================================================================
-- 三、文物本体模块
-- =====================================================================

-- 10. 文物分类表（支持两级树形）
CREATE TABLE artifact_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    code        VARCHAR(50)  DEFAULT NULL COMMENT '分类编码',
    description VARCHAR(500) DEFAULT NULL COMMENT '分类说明',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_cat_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文物分类表';

-- 11. 年代/文化期表
CREATE TABLE dynasty_period (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name         VARCHAR(50)  NOT NULL COMMENT '年代或文化期名称',
    culture_name VARCHAR(50)  DEFAULT NULL COMMENT '对应考古学文化',
    start_year   INT          DEFAULT NULL COMMENT '起始年份（公元前为负数，约数）',
    end_year     INT          DEFAULT NULL COMMENT '结束年份（公元前为负数，约数）',
    era_desc     VARCHAR(100) DEFAULT NULL COMMENT '年代文字描述（如 距今约4500-3700年）',
    description  TEXT         COMMENT '文化期介绍',
    sort         INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '年代文化期表';

-- 12. 祭祀坑（出土单位）表
CREATE TABLE excavation_pit (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    pit_code        VARCHAR(20)   NOT NULL COMMENT '坑号（如 K1）',
    name            VARCHAR(50)   NOT NULL COMMENT '名称（如 一号祭祀坑）',
    discovery_year  INT           DEFAULT NULL COMMENT '发现年份',
    excavation_period VARCHAR(100) DEFAULT NULL COMMENT '发掘时间段',
    length_m        DECIMAL(6,2)  DEFAULT NULL COMMENT '坑口长（米，约数）',
    width_m         DECIMAL(6,2)  DEFAULT NULL COMMENT '坑口宽（米，约数）',
    area_sqm        DECIMAL(8,2)  DEFAULT NULL COMMENT '面积（平方米，约数）',
    period_id       BIGINT        DEFAULT NULL COMMENT '所属年代/文化期ID',
    description     TEXT          COMMENT '坑位概况',
    main_findings   VARCHAR(500)  DEFAULT NULL COMMENT '主要出土器物概述',
    pos_x           DECIMAL(10,2) DEFAULT NULL COMMENT '遗址平面示意图X坐标',
    pos_y           DECIMAL(10,2) DEFAULT NULL COMMENT '遗址平面示意图Y坐标',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pit_code (pit_code),
    KEY idx_pit_period (period_id),
    CONSTRAINT fk_pit_period FOREIGN KEY (period_id) REFERENCES dynasty_period (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '祭祀坑（出土单位）表';

-- 13. 材质表
CREATE TABLE material (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(50)  NOT NULL COMMENT '材质名称',
    category      VARCHAR(30)  DEFAULT NULL COMMENT '材质大类：金属/玉石/有机质/陶土',
    description   VARCHAR(500) DEFAULT NULL COMMENT '材质说明',
    conservation_note VARCHAR(500) DEFAULT NULL COMMENT '保存保护要点',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_material_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '材质表';

-- 14. 文物主表
CREATE TABLE artifact (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    artifact_no     VARCHAR(50)   NOT NULL COMMENT '文物编号（系统内编号）',
    name            VARCHAR(100)  NOT NULL COMMENT '文物名称',
    alias           VARCHAR(200)  DEFAULT NULL COMMENT '别名（逗号分隔）',
    category_id     BIGINT        DEFAULT NULL COMMENT '分类ID',
    period_id       BIGINT        DEFAULT NULL COMMENT '年代/文化期ID',
    pit_id          BIGINT        DEFAULT NULL COMMENT '出土祭祀坑ID',
    material_id     BIGINT        DEFAULT NULL COMMENT '主要材质ID',
    hall_id         BIGINT        DEFAULT NULL COMMENT '陈列展厅ID',
    showcase_id     BIGINT        DEFAULT NULL COMMENT '陈列展柜ID',
    dimensions      VARCHAR(200)  DEFAULT NULL COMMENT '尺寸描述（如 通高396厘米）',
    height_cm       DECIMAL(8,2)  DEFAULT NULL COMMENT '高度（厘米，约数）',
    width_cm        DECIMAL(8,2)  DEFAULT NULL COMMENT '宽度（厘米，约数）',
    weight_g        DECIMAL(10,2) DEFAULT NULL COMMENT '重量（克，约数）',
    excavation_year INT           DEFAULT NULL COMMENT '出土年份',
    grade           VARCHAR(20)   DEFAULT NULL COMMENT '文物等级：一级/二级/三级/一般',
    is_treasure     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否镇馆之宝：0-否 1-是',
    description     TEXT          COMMENT '文物描述',
    cultural_value  TEXT          COMMENT '文化价值与研究意义',
    guide_text      TEXT          COMMENT '语音导览讲解词',
    cover_url       VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
    view_count      INT           NOT NULL DEFAULT 0 COMMENT '浏览次数',
    favorite_count  INT           NOT NULL DEFAULT 0 COMMENT '收藏次数',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-下展/库房 1-在展 2-修复中',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_artifact_no (artifact_no),
    KEY idx_art_name (name),
    KEY idx_art_category (category_id),
    KEY idx_art_period (period_id),
    KEY idx_art_pit (pit_id),
    KEY idx_art_material (material_id),
    KEY idx_art_hall (hall_id),
    KEY idx_art_showcase (showcase_id),
    KEY idx_art_view (view_count),
    CONSTRAINT fk_art_category FOREIGN KEY (category_id) REFERENCES artifact_category (id),
    CONSTRAINT fk_art_period FOREIGN KEY (period_id) REFERENCES dynasty_period (id),
    CONSTRAINT fk_art_pit FOREIGN KEY (pit_id) REFERENCES excavation_pit (id),
    CONSTRAINT fk_art_material FOREIGN KEY (material_id) REFERENCES material (id),
    CONSTRAINT fk_art_hall FOREIGN KEY (hall_id) REFERENCES exhibition_hall (id),
    CONSTRAINT fk_art_showcase FOREIGN KEY (showcase_id) REFERENCES showcase (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文物主表';

-- 15. 文物图片表
CREATE TABLE artifact_image (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    artifact_id  BIGINT       NOT NULL COMMENT '文物ID',
    image_url    VARCHAR(255) NOT NULL COMMENT '图片URL',
    image_type   VARCHAR(20)  NOT NULL DEFAULT 'PHOTO' COMMENT '图片类型：PHOTO-实拍 DETAIL-细节 LINE-线图 TRAIN-3DGS训练视图 RENDER-三维扫描渲染图',
    title        VARCHAR(100) DEFAULT NULL COMMENT '图片标题',
    width_px     INT          DEFAULT NULL COMMENT '图片宽度（像素）',
    height_px    INT          DEFAULT NULL COMMENT '图片高度（像素）',
    is_cover     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否封面：0-否 1-是',
    sort         INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_img_artifact (artifact_id),
    CONSTRAINT fk_img_artifact FOREIGN KEY (artifact_id) REFERENCES artifact (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文物图片表';

-- 16. 三维模型表（3D高斯泼溅 3DGS 模型文件及训练指标）
CREATE TABLE model_3d (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    artifact_id         BIGINT        DEFAULT NULL COMMENT '关联文物ID（展厅场景模型可为空）',
    hall_id             BIGINT        DEFAULT NULL COMMENT '关联展厅ID（场景级模型）',
    name                VARCHAR(100)  NOT NULL COMMENT '模型名称',
    model_type          VARCHAR(20)   NOT NULL DEFAULT 'OBJECT' COMMENT '模型类型：OBJECT-单体文物 SCENE-展厅场景',
    model_url           VARCHAR(255)  NOT NULL COMMENT '模型文件URL',
    format              VARCHAR(20)   NOT NULL COMMENT '文件格式：ply/splat/ksplat/spz（3DGS）、glb/gltf（三维网格扫描）',
    file_size           BIGINT        DEFAULT NULL COMMENT '文件大小（字节）',
    gaussian_count      INT           DEFAULT NULL COMMENT '高斯基元数量',
    sh_degree           TINYINT       DEFAULT NULL COMMENT '球谐函数阶数（0-3）',
    image_count         INT           DEFAULT NULL COMMENT '训练用多视角图像数量',
    capture_device      VARCHAR(100)  DEFAULT NULL COMMENT '采集设备',
    sfm_tool            VARCHAR(50)   DEFAULT NULL COMMENT '位姿估计工具（如 COLMAP）',
    trained_by          VARCHAR(100)  DEFAULT NULL COMMENT '训练框架/实现（如 gaussian-splatting官方实现、gsplat）',
    train_iterations    INT           DEFAULT NULL COMMENT '训练迭代次数',
    train_minutes       DECIMAL(8,2)  DEFAULT NULL COMMENT '训练耗时（分钟）',
    psnr                DECIMAL(6,3)  DEFAULT NULL COMMENT '峰值信噪比PSNR（dB）',
    ssim                DECIMAL(6,4)  DEFAULT NULL COMMENT '结构相似性SSIM',
    lpips               DECIMAL(6,4)  DEFAULT NULL COMMENT '感知相似度LPIPS（越低越好）',
    camera_position     VARCHAR(100)  DEFAULT NULL COMMENT '默认相机位置（JSON数组 [x,y,z]）',
    camera_target       VARCHAR(100)  DEFAULT NULL COMMENT '默认相机观察点（JSON数组 [x,y,z]）',
    thumbnail_url       VARCHAR(255)  DEFAULT NULL COMMENT '缩略图URL',
    is_default          TINYINT       NOT NULL DEFAULT 0 COMMENT '是否该文物默认展示模型',
    status              TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-训练中 1-可用 2-已下线',
    remark              VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    source_url          VARCHAR(255)  DEFAULT NULL COMMENT '第三方模型来源链接（如 Sketchfab 页面）',
    author              VARCHAR(100)  DEFAULT NULL COMMENT '模型作者（第三方模型署名）',
    license             VARCHAR(50)   DEFAULT NULL COMMENT '授权协议（如 CC BY 4.0）',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_model_artifact (artifact_id),
    KEY idx_model_hall (hall_id),
    KEY idx_model_format (format),
    CONSTRAINT fk_model_artifact FOREIGN KEY (artifact_id) REFERENCES artifact (id),
    CONSTRAINT fk_model_hall FOREIGN KEY (hall_id) REFERENCES exhibition_hall (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '三维模型表（3DGS）';

-- 17. 文物修复记录表
CREATE TABLE restoration_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    artifact_id     BIGINT       NOT NULL COMMENT '文物ID',
    restorer        VARCHAR(100) DEFAULT NULL COMMENT '修复人员/团队',
    start_date      DATE         DEFAULT NULL COMMENT '开始日期',
    end_date        DATE         DEFAULT NULL COMMENT '结束日期',
    condition_before VARCHAR(500) DEFAULT NULL COMMENT '修复前病害状况',
    method          VARCHAR(500) DEFAULT NULL COMMENT '修复方法与工艺',
    condition_after VARCHAR(500) DEFAULT NULL COMMENT '修复后状况',
    status          VARCHAR(20)  NOT NULL DEFAULT 'DONE' COMMENT '状态：PLANNED-计划 ONGOING-进行中 DONE-已完成',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_rest_artifact (artifact_id),
    CONSTRAINT fk_rest_artifact FOREIGN KEY (artifact_id) REFERENCES artifact (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文物修复记录表';

-- =====================================================================
-- 四、知识图谱与文献模块（GraphRAG 数据底座）
-- =====================================================================

-- 18. 知识图谱实体表
CREATE TABLE kg_entity (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name         VARCHAR(100) NOT NULL COMMENT '实体名称',
    entity_type  VARCHAR(30)  NOT NULL COMMENT '实体类型：ARTIFACT-文物 PIT-祭祀坑 MATERIAL-材质 PERIOD-年代 CULTURE-文化 THEME-主题 SITE-遗址 MUSEUM-博物馆 PERSON-人物 HALL-展厅 LITERATURE-文献 TECH-工艺 CATEGORY-文物分类',
    aliases      VARCHAR(255) DEFAULT NULL COMMENT '别名（逗号分隔，用于问句实体链接）',
    description  TEXT         COMMENT '实体描述',
    ref_table    VARCHAR(50)  DEFAULT NULL COMMENT '关联业务表名（如 artifact）',
    ref_id       BIGINT       DEFAULT NULL COMMENT '关联业务表主键',
    properties   TEXT         COMMENT '扩展属性（JSON）',
    weight       DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '实体重要度（可视化节点大小）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_entity_name (name),
    KEY idx_entity_type (entity_type),
    KEY idx_entity_ref (ref_table, ref_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '知识图谱实体表';

-- 19. 知识图谱关系表（有向三元组）
CREATE TABLE kg_relation (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    source_id      BIGINT       NOT NULL COMMENT '头实体ID',
    target_id      BIGINT       NOT NULL COMMENT '尾实体ID',
    relation_type  VARCHAR(50)  NOT NULL COMMENT '关系类型编码（如 EXCAVATED_FROM）',
    relation_name  VARCHAR(50)  NOT NULL COMMENT '关系名称（如 出土于）',
    weight         DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '关系权重',
    description    VARCHAR(500) DEFAULT NULL COMMENT '关系说明/证据',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_rel_source (source_id),
    KEY idx_rel_target (target_id),
    KEY idx_rel_type (relation_type),
    CONSTRAINT fk_rel_source FOREIGN KEY (source_id) REFERENCES kg_entity (id),
    CONSTRAINT fk_rel_target FOREIGN KEY (target_id) REFERENCES kg_entity (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '知识图谱关系表';

-- 20. 文献资料表
CREATE TABLE literature (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title          VARCHAR(200) NOT NULL COMMENT '文献标题',
    authors        VARCHAR(200) DEFAULT NULL COMMENT '作者/编者',
    source         VARCHAR(200) DEFAULT NULL COMMENT '出处（出版社/期刊/古籍）',
    publish_year   VARCHAR(20)  DEFAULT NULL COMMENT '出版年份或朝代',
    lit_type       VARCHAR(20)  NOT NULL DEFAULT 'BOOK' COMMENT '文献类型：BOOK-专著 PAPER-论文 ANCIENT-古籍 REPORT-考古报告 GUIDE-导览文稿',
    keywords       VARCHAR(255) DEFAULT NULL COMMENT '关键词（逗号分隔）',
    abstract_text  TEXT         COMMENT '摘要',
    content        MEDIUMTEXT   COMMENT '正文/摘录（GraphRAG文本检索片段来源）',
    url            VARCHAR(255) DEFAULT NULL COMMENT '原文链接',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_lit_title (title),
    KEY idx_lit_type (lit_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文献资料表';

-- 21. 文物-文献关联表
CREATE TABLE artifact_literature (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    artifact_id    BIGINT       NOT NULL COMMENT '文物ID',
    literature_id  BIGINT       NOT NULL COMMENT '文献ID',
    note           VARCHAR(255) DEFAULT NULL COMMENT '引用说明',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_al_artifact (artifact_id),
    KEY idx_al_literature (literature_id),
    CONSTRAINT fk_al_artifact FOREIGN KEY (artifact_id) REFERENCES artifact (id),
    CONSTRAINT fk_al_literature FOREIGN KEY (literature_id) REFERENCES literature (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文物文献关联表';

-- =====================================================================
-- 五、智能问答模块
-- =====================================================================

-- 22. 问答会话表
CREATE TABLE qa_session (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT       DEFAULT NULL COMMENT '用户ID（匿名游客为空）',
    title           VARCHAR(100) DEFAULT NULL COMMENT '会话标题（取首个问题）',
    client_ip       VARCHAR(64)  DEFAULT NULL COMMENT '客户端IP',
    message_count   INT          NOT NULL DEFAULT 0 COMMENT '消息条数',
    last_active_time DATETIME    DEFAULT NULL COMMENT '最近活跃时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_session_user (user_id),
    KEY idx_session_active (last_active_time),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '问答会话表';

-- 23. 问答消息表
CREATE TABLE qa_message (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    session_id         BIGINT       NOT NULL COMMENT '会话ID',
    role               VARCHAR(20)  NOT NULL COMMENT '消息角色：user-用户 assistant-助手',
    content            MEDIUMTEXT   NOT NULL COMMENT '消息内容',
    retrieved_context  MEDIUMTEXT   COMMENT 'GraphRAG检索到的上下文（三元组/文物/文献，JSON）',
    cited_entities     TEXT         COMMENT '引用的知识图谱实体（JSON数组）',
    model_name         VARCHAR(50)  DEFAULT NULL COMMENT '调用的大模型名称',
    prompt_tokens      INT          DEFAULT NULL COMMENT '提示词Token数',
    completion_tokens  INT          DEFAULT NULL COMMENT '生成Token数',
    total_tokens       INT          DEFAULT NULL COMMENT '总Token数',
    latency_ms         BIGINT       DEFAULT NULL COMMENT '响应耗时（毫秒）',
    is_fallback        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否为离线兜底回答：0-否 1-是',
    feedback           TINYINT      DEFAULT NULL COMMENT '用户反馈：1-有用 -1-无用',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_msg_session (session_id),
    KEY idx_msg_created (created_at),
    CONSTRAINT fk_msg_session FOREIGN KEY (session_id) REFERENCES qa_session (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '问答消息表';

-- =====================================================================
-- 六、智能导览模块
-- =====================================================================

-- 24. 导览路线表
CREATE TABLE guide_route (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    museum_id       BIGINT        DEFAULT NULL COMMENT '所属博物馆ID',
    name            VARCHAR(100)  NOT NULL COMMENT '路线名称',
    route_type      VARCHAR(20)   NOT NULL DEFAULT 'THEME' COMMENT '路线类型：CLASSIC-经典 THEME-主题 QUICK-快速 FAMILY-亲子',
    theme           VARCHAR(100)  DEFAULT NULL COMMENT '路线主题',
    description     TEXT          COMMENT '路线介绍',
    duration_min    INT           DEFAULT NULL COMMENT '预计时长（分钟）',
    distance_m      INT           DEFAULT NULL COMMENT '步行距离（米，约数）',
    difficulty      TINYINT       DEFAULT 1 COMMENT '难度/强度：1-轻松 2-适中 3-较长',
    suitable_crowd  VARCHAR(100)  DEFAULT NULL COMMENT '适合人群',
    cover_url       VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
    use_count       INT           NOT NULL DEFAULT 0 COMMENT '使用次数',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_route_museum (museum_id),
    CONSTRAINT fk_route_museum FOREIGN KEY (museum_id) REFERENCES museum (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '导览路线表';

-- 25. 导览路线节点表
CREATE TABLE guide_route_node (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    route_id        BIGINT        NOT NULL COMMENT '路线ID',
    seq             INT           NOT NULL COMMENT '节点顺序（从1开始）',
    node_type       VARCHAR(20)   NOT NULL DEFAULT 'ARTIFACT' COMMENT '节点类型：ARTIFACT-文物 HALL-展厅 POI-服务点',
    artifact_id     BIGINT        DEFAULT NULL COMMENT '文物ID（文物节点）',
    hall_id         BIGINT        DEFAULT NULL COMMENT '展厅ID',
    title           VARCHAR(100)  NOT NULL COMMENT '节点标题',
    narration       TEXT          COMMENT '节点讲解词',
    stay_minutes    INT           DEFAULT 5 COMMENT '建议停留时长（分钟）',
    camera_position VARCHAR(100)  DEFAULT NULL COMMENT '3D漫游相机位置（JSON [x,y,z]）',
    camera_target   VARCHAR(100)  DEFAULT NULL COMMENT '3D漫游相机观察点（JSON [x,y,z]）',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_node_route (route_id, seq),
    KEY idx_node_artifact (artifact_id),
    KEY idx_node_hall (hall_id),
    CONSTRAINT fk_node_route FOREIGN KEY (route_id) REFERENCES guide_route (id),
    CONSTRAINT fk_node_artifact FOREIGN KEY (artifact_id) REFERENCES artifact (id),
    CONSTRAINT fk_node_hall FOREIGN KEY (hall_id) REFERENCES exhibition_hall (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '导览路线节点表';

-- =====================================================================
-- 七、观众服务模块
-- =====================================================================

-- 26. 游客档案表（与 sys_user 一对一扩展）
CREATE TABLE visitor (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT       NOT NULL COMMENT '关联用户ID',
    real_name      VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    age_group      VARCHAR(20)  DEFAULT NULL COMMENT '年龄段：少年/青年/中年/老年',
    region         VARCHAR(100) DEFAULT NULL COMMENT '常住地区',
    visitor_type   VARCHAR(20)  DEFAULT 'INDIVIDUAL' COMMENT '游客类型：INDIVIDUAL-散客 STUDENT-学生 TEAM-团队 RESEARCHER-研究者',
    interests      VARCHAR(255) DEFAULT NULL COMMENT '兴趣标签（逗号分隔，用于路线推荐）',
    visit_count    INT          NOT NULL DEFAULT 0 COMMENT '累计到馆次数',
    points         INT          NOT NULL DEFAULT 0 COMMENT '积分',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_visitor_user (user_id),
    CONSTRAINT fk_visitor_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '游客档案表';

-- 27. 参观预约表
CREATE TABLE reservation (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    reservation_no  VARCHAR(40)  NOT NULL COMMENT '预约单号',
    user_id         BIGINT       NOT NULL COMMENT '预约用户ID',
    museum_id       BIGINT       NOT NULL COMMENT '博物馆ID',
    visit_date      DATE         NOT NULL COMMENT '参观日期',
    time_slot       VARCHAR(20)  NOT NULL COMMENT '入馆时段（如 09:00-11:00）',
    visitor_count   INT          NOT NULL DEFAULT 1 COMMENT '参观人数',
    contact_name    VARCHAR(50)  NOT NULL COMMENT '联系人姓名',
    contact_phone   VARCHAR(20)  NOT NULL COMMENT '联系人手机号',
    route_id        BIGINT       DEFAULT NULL COMMENT '意向导览路线ID',
    need_guide      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否需要人工讲解：0-否 1-是',
    status          VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED' COMMENT '状态：PENDING-待确认 CONFIRMED-已确认 CANCELLED-已取消 COMPLETED-已核销',
    check_in_time   DATETIME     DEFAULT NULL COMMENT '核销入馆时间',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_no (reservation_no),
    KEY idx_res_user (user_id),
    KEY idx_res_date_slot (visit_date, time_slot),
    KEY idx_res_status (status),
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_res_museum FOREIGN KEY (museum_id) REFERENCES museum (id),
    CONSTRAINT fk_res_route FOREIGN KEY (route_id) REFERENCES guide_route (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参观预约表';

-- 28. 评价表（可评价文物、路线、博物馆）
CREATE TABLE review (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id      BIGINT       NOT NULL COMMENT '评价用户ID',
    target_type  VARCHAR(20)  NOT NULL COMMENT '评价对象类型：ARTIFACT-文物 ROUTE-路线 MUSEUM-博物馆 QA-问答服务',
    target_id    BIGINT       NOT NULL COMMENT '评价对象ID',
    rating       TINYINT      NOT NULL COMMENT '评分（1-5星）',
    content      VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    images       VARCHAR(1000) DEFAULT NULL COMMENT '评价图片URL（逗号分隔）',
    like_count   INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '审核状态：0-待审核 1-已通过 2-已屏蔽',
    reply        VARCHAR(500) DEFAULT NULL COMMENT '馆方回复',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_review_target (target_type, target_id),
    KEY idx_review_user (user_id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评价表';

-- 29. 收藏表
CREATE TABLE favorite (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    target_type  VARCHAR(20)  NOT NULL DEFAULT 'ARTIFACT' COMMENT '收藏对象类型：ARTIFACT-文物 ROUTE-路线',
    target_id    BIGINT       NOT NULL COMMENT '收藏对象ID',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_fav_user_target (user_id, target_type, target_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏表';

-- =====================================================================
-- 八、运营内容模块
-- =====================================================================

-- 30. 展览活动表
CREATE TABLE exhibition_event (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    museum_id     BIGINT       DEFAULT NULL COMMENT '博物馆ID',
    hall_id       BIGINT       DEFAULT NULL COMMENT '举办展厅ID',
    title         VARCHAR(200) NOT NULL COMMENT '活动标题',
    event_type    VARCHAR(20)  NOT NULL DEFAULT 'EXHIBITION' COMMENT '活动类型：EXHIBITION-展览 LECTURE-讲座 WORKSHOP-研学 ONLINE-线上活动',
    start_time    DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time      DATETIME     DEFAULT NULL COMMENT '结束时间',
    location      VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
    organizer     VARCHAR(100) DEFAULT NULL COMMENT '主办方',
    capacity      INT          DEFAULT NULL COMMENT '名额上限',
    content       TEXT         COMMENT '活动内容',
    cover_url     VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_event_time (start_time, end_time),
    CONSTRAINT fk_event_museum FOREIGN KEY (museum_id) REFERENCES museum (id),
    CONSTRAINT fk_event_hall FOREIGN KEY (hall_id) REFERENCES exhibition_hall (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '展览活动表';

-- 31. 公告表
CREATE TABLE announcement (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title         VARCHAR(200) NOT NULL COMMENT '公告标题',
    content       TEXT         NOT NULL COMMENT '公告内容',
    ann_type      VARCHAR(20)  NOT NULL DEFAULT 'NOTICE' COMMENT '公告类型：NOTICE-通知 NEWS-新闻 CLOSE-闭馆通知',
    is_top        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    publisher_id  BIGINT       DEFAULT NULL COMMENT '发布人ID',
    publish_time  DATETIME     DEFAULT NULL COMMENT '发布时间',
    view_count    INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-草稿 1-已发布',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    KEY idx_ann_publish (publish_time),
    CONSTRAINT fk_ann_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告表';

-- 32. 系统参数配置表（含大模型API设置）
CREATE TABLE system_config (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_key    VARCHAR(100) NOT NULL COMMENT '配置键（如 llm.api_key）',
    config_value  VARCHAR(1000) DEFAULT NULL COMMENT '配置值',
    config_group  VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM' COMMENT '配置分组：LLM/RAG/RESERVATION/SYSTEM',
    value_type    VARCHAR(20)  NOT NULL DEFAULT 'STRING' COMMENT '值类型：STRING/NUMBER/BOOLEAN/SECRET',
    description   VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_config_group (config_group)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统参数配置表';

-- 33. 每日访问统计表（数据大屏客流趋势）
CREATE TABLE visit_statistics (
    id                BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stat_date         DATE     NOT NULL COMMENT '统计日期',
    offline_visitors  INT      NOT NULL DEFAULT 0 COMMENT '线下入馆人数',
    online_visits     INT      NOT NULL DEFAULT 0 COMMENT '线上数字展厅访问量（PV）',
    model_views       INT      NOT NULL DEFAULT 0 COMMENT '3D模型浏览次数',
    qa_count          INT      NOT NULL DEFAULT 0 COMMENT '智能问答次数',
    reservation_count INT      NOT NULL DEFAULT 0 COMMENT '预约单数',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stat_date (stat_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '每日访问统计表';
