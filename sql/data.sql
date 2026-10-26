-- =====================================================================
-- 三星堆文物数字孪生与智能导览系统 —— 演示数据
-- 说明：
--   1. 文物史实部分仅采用公开报道中较为确定的信息，尺寸重量等均为约数；
--   2. 图片、3DGS 模型文件 URL 为占位路径，训练指标为演示用数值，需在模型训练后替换；
--   3. 客流、问答等运营数据为模拟数据；日期相对当前日期生成，保证大屏趋势图始终有数据。
-- 默认账号：admin/admin123（管理员）  curator/curator123（馆员）  visitor/visitor123（游客）
-- =====================================================================
USE sanxingdui_twin;
SET NAMES utf8mb4;

-- sys_user
INSERT INTO sys_user (id, username, password, nickname, email, phone, gender, user_type, status, remark) VALUES
(1, 'admin', '$2a$10$Tc8Qfb1QHL6YWE/RtUID1e3134BK1HCpJydYRyRp7hbMW5RHZT1I.', '系统管理员', 'admin@sxd-twin.local', '13800000001', 0, 'ADMIN', 1, '默认管理员账号'),
(2, 'curator', '$2a$10$s063Usk7DE9rf3DQV4O0rOJJuWm68F1lHWmx8hestm5miU2AAjPgC', '文物馆员', 'curator@sxd-twin.local', '13800000002', 0, 'CURATOR', 1, '负责文物与知识图谱维护'),
(3, 'visitor', '$2a$10$fL.Us1OhVB4lOvhF7NnDdO.c89C6OGGONq2gvn1LVPBQnxT4NPt.a', '演示游客', 'visitor@sxd-twin.local', '13800000003', 0, 'VISITOR', 1, '演示游客账号'),
(4, 'zhangsan', '$2a$10$fL.Us1OhVB4lOvhF7NnDdO.c89C6OGGONq2gvn1LVPBQnxT4NPt.a', '张三', NULL, '13800000004', 1, 'VISITOR', 1, NULL),
(5, 'lisi', '$2a$10$fL.Us1OhVB4lOvhF7NnDdO.c89C6OGGONq2gvn1LVPBQnxT4NPt.a', '李四', NULL, '13800000005', 2, 'VISITOR', 1, NULL);

-- sys_role
INSERT INTO sys_role (id, role_code, role_name, description, sort) VALUES
(1, 'ADMIN', '系统管理员', '拥有全部权限', 1),
(2, 'CURATOR', '文物馆员', '维护文物、模型、知识图谱、导览路线等业务数据', 2),
(3, 'VISITOR', '游客', '浏览、问答、预约、评价、收藏', 3);

-- sys_permission
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort) VALUES
(1, 0, 'dashboard', '数据大屏', 'MENU', '/dashboard', 'DataLine', 1),
(2, 0, 'artifact', '文物管理', 'MENU', '/artifact', 'Collection', 2),
(3, 2, 'artifact:edit', '文物编辑', 'BUTTON', NULL, NULL, 1),
(4, 0, 'model', '三维模型', 'MENU', '/model', 'Box', 3),
(5, 0, 'kg', '知识图谱', 'MENU', '/kg', 'Share', 4),
(6, 5, 'kg:edit', '图谱编辑', 'BUTTON', NULL, NULL, 1),
(7, 0, 'qa', '智能问答', 'MENU', '/qa', 'ChatDotRound', 5),
(8, 0, 'route', '导览路线', 'MENU', '/route', 'Guide', 6),
(9, 0, 'reservation', '预约管理', 'MENU', '/reservation', 'Calendar', 7),
(10, 0, 'review', '评价管理', 'MENU', '/review', 'Star', 8),
(11, 0, 'content', '运营内容', 'MENU', '/content', 'Notification', 9),
(12, 0, 'system', '系统管理', 'MENU', '/system', 'Setting', 10),
(13, 12, 'system:user', '用户管理', 'MENU', '/system/user', 'User', 1),
(14, 12, 'system:config', '参数配置', 'MENU', '/system/config', 'Tools', 2),
(15, 12, 'system:log', '操作日志', 'MENU', '/system/log', 'Document', 3);

-- sys_role_permission
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13),
(1, 14),
(1, 15),
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 11),
(3, 7),
(3, 8);

-- sys_user_role
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 3);

-- museum
INSERT INTO museum (id, name, short_name, address, longitude, latitude, open_time, introduction, daily_capacity) VALUES
(1, '三星堆博物馆', '三星堆', '四川省德阳市广汉市西安路133号', 104.2205, 30.9925, '开放时间以馆方公告为准', '三星堆博物馆位于四川省广汉市三星堆遗址东北角，是一座现代化的专题性遗址博物馆，1997年10月建成开放。新馆于2023年7月开放，基本陈列以三星堆遗址考古成果为基础，系统展示古蜀文明的面貌。', 30000);

-- exhibition_hall
INSERT INTO exhibition_hall (id, museum_id, hall_code, name, theme, floor, description, map_x, map_y, sort) VALUES
(1, 1, 'H01', '世纪逐梦', '三星堆考古发现与研究历程', 1, '介绍自1929年发现玉石器以来，三星堆遗址近百年的考古发现与研究历程。', 120, 80, 1),
(2, 1, 'H02', '巍然王都', '三星堆古城与古蜀社会生活', 1, '展示三星堆古城城墙、居址与陶器、玉石器等反映古蜀社会生产生活的遗存。', 260, 80, 2),
(3, 1, 'H03', '天地人神', '祭祀坑出土的青铜器、金器与玉器', 1, '集中展示祭祀坑出土的青铜神树、青铜人像、青铜面具、金器等重要文物，呈现古蜀人的精神世界。', 400, 140, 3),
(4, 1, 'H04', '文物保护与修复馆', '文物保护修复工作展示', 1, '以开放式实验室的形式向观众展示文物清理、拼对、修复与保护工作。', 520, 220, 4),
(5, 1, 'H05', '数字孪生展厅（线上）', '基于3D高斯泼溅的线上沉浸式展厅', 0, '本系统构建的线上数字展厅，通过3DGS模型实现文物与展厅的实时三维浏览。', 0, 0, 5);

-- showcase
INSERT INTO showcase (id, hall_id, case_code, name, case_type, pos_x, pos_y, pos_z, temperature, humidity) VALUES
(1, 1, 'S-101', '考古历程展柜', '通柜', 3.0, 0.0, 6.0, 20.5, 50.0),
(2, 2, 'S-201', '陶器与生活用具展柜', '通柜', 4.0, 0.0, 5.0, 20.8, 52.0),
(3, 3, 'S-301', '青铜神树展区', '中心柜', 12.0, 0.0, 10.0, 20.0, 45.0),
(4, 3, 'S-302', '青铜面具展区', '独立柜', 6.0, 0.0, 4.0, 20.2, 45.0),
(5, 3, 'S-303', '青铜人像展区', '独立柜', 18.0, 0.0, 4.0, 20.1, 45.0),
(6, 3, 'S-304', '金器展区', '独立柜', 8.0, 0.0, 16.0, 20.0, 45.0),
(7, 3, 'S-305', '玉石器与象牙展区', '壁柜', 20.0, 0.0, 16.0, 19.8, 55.0),
(8, 4, 'S-401', '修复成果展示柜', '独立柜', 5.0, 0.0, 5.0, 20.0, 48.0);

-- artifact_category
INSERT INTO artifact_category (id, parent_id, name, code, description, sort) VALUES
(1, 0, '青铜器', 'BRONZE', '三星堆出土数量最多、最具特色的一类器物', 1),
(2, 0, '金器', 'GOLD', '金杖、金面罩、金面具、金箔饰件等', 2),
(3, 0, '玉石器', 'JADE', '璋、戈、琮、璧等玉石礼器', 3),
(4, 0, '象牙与骨器', 'IVORY', '象牙及象牙制品', 4),
(5, 0, '陶器', 'POTTERY', '遗址出土的生活与礼仪用陶器', 5),
(6, 1, '青铜人像', 'BRONZE_FIGURE', '立人像、跪坐人像、人头像等', 1),
(7, 1, '青铜面具', 'BRONZE_MASK', '纵目面具、人面具、兽面等', 2),
(8, 1, '青铜神器', 'BRONZE_SACRED', '神树、神坛、太阳形器等祭祀重器', 3),
(9, 1, '青铜礼器', 'BRONZE_RITUAL', '尊、罍等容器', 4);

-- dynasty_period
INSERT INTO dynasty_period (id, name, culture_name, start_year, end_year, era_desc, description, sort) VALUES
(1, '三星堆遗址第一期', '宝墩文化', -2500, -1700, '距今约4500—3700年（约数）', '成都平原新石器时代晚期文化，以城址群著称，是三星堆遗址最早期的文化遗存。', 1),
(2, '三星堆遗址第二、三期', '三星堆文化', -1700, -1200, '约相当于夏代至商代（约数）', '三星堆文化是古蜀文明的鼎盛阶段，三星堆古城在这一时期形成。', 2),
(3, '商代晚期（祭祀坑年代）', '三星堆文化（晚段）', -1250, -1050, '距今约3200—3000年（约数）', '2022年公布的碳十四测年结果显示，三星堆祭祀区多个坑的埋藏年代集中在商代晚期。', 3),
(4, '三星堆遗址第四期', '十二桥文化', -1200, -600, '约相当于商末至春秋（约数）', '十二桥文化以成都金沙遗址为代表，与三星堆文化存在承续关系。', 4);

-- excavation_pit
INSERT INTO excavation_pit (id, pit_code, name, discovery_year, excavation_period, length_m, width_m, description, main_findings, pos_x, pos_y) VALUES
(1, 'K1', '一号祭祀坑', 1986, '1986年夏', 4.6, 3.4, '1986年发现并发掘，出土金杖、金面罩、青铜人头像、龙虎尊、玉石器及象牙等。', '金杖、青铜人头像、龙虎尊、玉器、象牙', 300, 200),
(2, 'K2', '二号祭祀坑', 1986, '1986年夏', 5.3, 2.3, '紧邻一号坑发现，出土青铜神树、青铜大立人像、青铜纵目面具、太阳形器、戴金面罩铜人头像及大量象牙。', '青铜神树、青铜大立人像、纵目面具、太阳形器、象牙', 340, 230),
(3, 'K3', '三号祭祀坑', 2019, '2020年—2022年', NULL, NULL, '2019年发现，2020年起开展系统发掘，出土顶尊跪坐人像、青铜大面具等，象牙数量多达百余根。', '顶尊跪坐人像、青铜面具、象牙', 360, 180),
(4, 'K4', '四号祭祀坑', 2020, '2020年—2022年', NULL, NULL, '出土扭头跪坐人像等，其出土样品的碳十四测年为判定祭祀区年代提供了重要依据。', '扭头跪坐人像、象牙', 380, 210),
(5, 'K5', '五号祭祀坑', 2020, '2020年—2022年', NULL, NULL, '面积较小，出土半张黄金面具及金箔饰件、象牙雕刻残件等。', '黄金面具、金箔饰件', 320, 170),
(6, 'K6', '六号祭祀坑', 2020, '2020年—2022年', NULL, NULL, '发现一件炭化木箱（木匣），为研究祭祀器具与埋藏方式提供了新材料。', '木箱', 330, 150),
(7, 'K7', '七号祭祀坑', 2020, '2020年—2022年', NULL, NULL, '出土龟背形网格状器等造型奇特的青铜器及象牙。', '龟背形网格状器、象牙', 390, 160),
(8, 'K8', '八号祭祀坑', 2020, '2020年—2022年', NULL, NULL, '出土青铜神坛、神兽等复杂青铜器，部分器物可与二号坑残件跨坑拼对。', '青铜神坛、神兽', 410, 190);

UPDATE excavation_pit SET period_id = 3;

-- material
INSERT INTO material (id, name, category, description, conservation_note) VALUES
(1, '青铜', '金属', '铜锡（铅）合金，三星堆青铜器多采用分铸、铸接等工艺。', '控制湿度，防止"青铜病"（粉状锈）发生。'),
(2, '黄金', '金属', '三星堆金器多为金块锤揲成金皮、金箔后再加工。', '化学性质稳定，主要防止形变与划伤。'),
(3, '玉', '玉石', '三星堆玉器以璋、戈、琮、璧等礼器为主。', '避免撞击与温度骤变。'),
(4, '象牙', '有机质', '祭祀坑中出土大量象牙，出土后极易开裂。', '出土后需保湿处理，严格控制温湿度。'),
(5, '陶', '陶土', '遗址生活区出土大量陶器，是文化分期的重要依据。', '防潮、防碰撞。'),
(6, '石', '玉石', '石璧、石器等。', '常规保存。');

-- artifact
INSERT INTO artifact (id, artifact_no, name, alias, category_id, period_id, pit_id, material_id, hall_id, showcase_id, dimensions, height_cm, width_cm, weight_g, excavation_year, grade, is_treasure, description, cultural_value, guide_text, cover_url, view_count, status) VALUES
(1, 'SXD-K2-001', '青铜神树', '一号神树,I号大型铜神树,神树', 8, 3, 2, 1, 3, 3, '残高约396厘米', 396, NULL, NULL, 1986, '一级', 1, '出土于二号祭祀坑，由底座、树干和树枝组成。树干分三层，每层伸出三枝，共九枝，枝上有花果与立鸟，树干一侧有一条龙沿树干蜿蜒而下。出土时已断裂破碎，经多年修复拼接复原。', '是目前已知体量最大的青铜神树之一。不少学者认为其与古代文献中的"扶桑""若木"等神树传说有关，反映了古蜀人的宇宙观与太阳崇拜，但具体含义学界仍有不同看法。', '眼前这棵青铜神树，残高近四米，九根树枝上栖息着神鸟，一条龙沿着树干向下游动。它让人联想到《山海经》中"十日"栖息的扶桑神树。', '/static/images/artifacts/shenshu.jpg', 1860, 1),
(2, 'SXD-K2-002', '青铜纵目面具', '纵目面具,千里眼顺风耳,大面具', 7, 3, 2, 1, 3, 4, '高约66厘米，宽约138厘米', 66, 138, NULL, 1986, '一级', 1, '出土于二号祭祀坑，面具双眼呈柱状向前凸出，双耳向两侧展开，嘴角上扬，造型夸张。', '有学者将其与《华阳国志》"蜀侯蚕丛，其目纵"的记载相联系，认为可能表现的是古蜀人心目中的祖先或神祇形象。', '这件纵目面具宽一米三有余，一双眼睛像柱子一样凸出来。《华阳国志》说古蜀王蚕丛"其目纵"，它会不会就是蚕丛的形象呢？', '/static/images/artifacts/zongmu.jpg', 1720, 1),
(3, 'SXD-K2-003', '青铜大立人像', '大立人,青铜立人像', 6, 3, 2, 1, 3, 5, '通高约260.8厘米（含底座），人像高约180厘米', 260.8, NULL, 180000, 1986, '一级', 1, '出土于二号祭祀坑，人像头戴冠，身着多层衣，赤足立于方形台座之上，双手环握于胸前呈中空状，原先可能持有某种器物。', '是同时期体量最大的青铜立人像之一，一般认为其身份可能是集神权与王权于一身的首领或大巫师形象。', '这尊青铜大立人像连座高约2.6米，双手巨大，环握在胸前。他手里原本握着什么？是象牙、玉琮还是别的法器？至今仍是一个谜。', '/static/images/artifacts/dalirenxiang.jpg', 1650, 1),
(4, 'SXD-K2-004', '戴金面罩青铜人头像', '金面罩人头像,金面罩', 6, 3, 2, 1, 3, 6, NULL, NULL, NULL, NULL, 1986, '一级', 1, '出土于二号祭祀坑，青铜人头像面部覆贴一层金箔面罩，金面罩眉眼部分镂空。', '金面罩与青铜人头像的结合体现了古蜀金器与青铜器工艺的高超水平，也显示了黄金在古蜀祭祀中的特殊地位。', '这位"金面人"的脸上贴着薄薄的金箔面罩，在灯光下熠熠生辉，是三星堆最具代表性的形象之一。', '/static/images/artifacts/jinmianzhao.jpg', 1380, 1),
(5, 'SXD-K1-001', '金杖', '金权杖', 2, 3, 1, 2, 3, 6, '长约142厘米，直径约2.3厘米', 142, 2.3, 463, 1986, '一级', 1, '出土于一号祭祀坑，以金条锤打成金皮后包卷在木杖外，木杖已炭化，仅存金皮。金皮上端刻有人头、鱼、鸟和箭（或称穗形）等图案。', '多数学者认为金杖是权力的象征，可能与古蜀国的王权或神权有关，图案中的鱼和鸟有学者联系到传说中的"鱼凫"王。', '这根金杖长约1.42米，上面刻着戴冠的人头、鱼和鸟。有人说它记录的是古蜀王"鱼凫"的故事，是权力的象征。', '/static/images/artifacts/jinzhang.jpg', 1540, 1),
(6, 'SXD-K5-001', '黄金面具（五号坑）', '五号坑金面具,半张金面具', 2, 3, 5, 2, 3, 6, '宽约23厘米，高约28厘米（残）', 28, 23, 280, 2021, '一级', 1, '2021年出土于五号祭祀坑，现存约半张，眼部镂空，耳部突出。', '研究人员推测其完整形态的重量可能超过500克，若推测成立，将是同时期最重的金面具之一，进一步丰富了对古蜀黄金工艺的认识。', '这半张金面具出土时揉皱在坑中，经过清理展开后重现光彩，它是三星堆新一轮考古发掘最受关注的发现之一。', '/static/images/artifacts/jinmianju.jpg', 1490, 1),
(7, 'SXD-K8-001', '青铜神坛', '神坛', 8, 3, 8, 1, 3, 3, NULL, NULL, NULL, NULL, 2022, '一级', 0, '出土于八号祭祀坑，由多层结构组成，表现了人物、神兽等元素，结构复杂。部分构件与早年二号坑出土的残件可以拼对。', '神坛被认为表现了古蜀人举行祭祀活动的场景，对研究古蜀宗教与祭祀礼仪具有重要价值。', '这件青铜神坛层层叠叠，上面有人物和神兽，仿佛把一场三千年前的祭祀仪式凝固在了青铜之中。', '/static/images/artifacts/shentan.jpg', 980, 1),
(8, 'SXD-K3-001', '铜顶尊跪坐人像', '顶尊跪坐人像,顶尊人像', 6, 3, 3, 1, 3, 5, '通高约115厘米', 115, NULL, NULL, 2021, '一级', 0, '出土于三号祭祀坑，人像双膝跪坐，双手合于胸前，头顶一件大口尊。', '人像将中原地区常见的尊与古蜀特色的人像结合在一起，被认为是古蜀文明与中原商文化交流的实物见证。', '这位跪坐的人头顶一件铜尊，尊是商代中原常见的礼器，它见证了古蜀与中原的交流。', '/static/images/artifacts/dingzun.jpg', 860, 1),
(9, 'SXD-K3-002', '祭祀坑出土象牙', '象牙', 4, 3, 3, 4, 3, 7, NULL, NULL, NULL, NULL, 2021, '一般', 0, '三星堆祭祀坑中普遍出土象牙，1986年一、二号坑出土象牙数十根，新发现的三号坑象牙多达百余根。', '大量象牙的出土反映了象牙在古蜀祭祀活动中的重要地位，也为研究古代成都平原的生态环境提供了线索。', '这些象牙层层铺在祭祀坑中，出土后要立刻保湿保护，否则很快就会开裂。', '/static/images/artifacts/xiangya.jpg', 640, 1),
(10, 'SXD-K2-005', '玉边璋', '祭山图玉璋,边璋,玉璋', 3, 3, 2, 3, 3, 7, NULL, NULL, NULL, NULL, 1986, '一级', 0, '出土于二号祭祀坑，璋身两面刻有成组的图案，表现了人物、山形等内容，常被称为"祭山图"。', '图案被认为表现了古蜀人祭祀山川的场景，是研究古蜀祭祀礼仪的重要图像资料。', '这件玉璋上刻着"祭山图"：有人跪拜，有大山，仿佛在讲述一场祭祀山神的仪式。', '/static/images/artifacts/yuzhang.jpg', 520, 1),
(11, 'SXD-K2-006', '青铜太阳形器', '太阳轮,太阳形器', 8, 3, 2, 1, 3, 3, '直径约85厘米', NULL, 85, NULL, 1986, '一级', 0, '出土于二号祭祀坑，圆形，中心为凸起的圆泡，周围放射出五道芒，外围为一圆环。', '一般认为与古蜀人的太阳崇拜有关，与青铜神树、金沙遗址太阳神鸟金饰共同构成理解古蜀太阳信仰的重要线索。', '这个像车轮一样的青铜器，中央凸起，五道光芒向外放射，很多人认为它就是古蜀人心中的太阳。', '/static/images/artifacts/taiyang.jpg', 760, 1),
(12, 'SXD-K2-007', '青铜人头像', '平顶人头像', 6, 3, 2, 1, 3, 5, NULL, NULL, NULL, NULL, 1986, '二级', 0, '祭祀坑出土的青铜人头像数量较多，发型、冠饰各有不同，眉眼粗犷，下颌宽大。', '不同发式与冠饰的人头像可能代表不同身份或族群，是研究古蜀社会结构的重要资料。', '这些青铜人头像发型各异，有的平顶，有的戴冠，它们可能代表着古蜀社会中不同身份的人。', '/static/images/artifacts/rentouxiang.jpg', 690, 1),
(13, 'SXD-K4-001', '青铜扭头跪坐人像', '扭头跪坐人像', 6, 3, 4, 1, 3, 5, NULL, NULL, NULL, NULL, 2021, '一级', 0, '出土于四号祭祀坑，人像跪坐，头部向一侧扭转，双手作合握状，发型独特。', '新一轮发掘中出土的代表性人像之一，丰富了三星堆人像的类型。', '这位扭着头、跪坐着的小铜人，姿态生动，仿佛正在参加一场神秘的仪式。', '/static/images/artifacts/niutou.jpg', 430, 1),
(14, 'SXD-K7-001', '龟背形网格状器', '龟背形器', 8, 3, 7, 1, 3, 3, NULL, NULL, NULL, NULL, 2022, '一级', 0, '出土于七号祭祀坑，整体呈网格状，形似龟背，两侧有提手类结构，内部发现有玉器。', '造型前所未见，用途尚无定论，体现了古蜀青铜器的独特性与想象力。', '它像一只龟壳，又像一个笼子，用途至今成谜，是新发现中最"神秘"的器物之一。', '/static/images/artifacts/guibei.jpg', 410, 1),
(15, 'SXD-K1-002', '青铜龙虎尊', '龙虎尊', 9, 3, 1, 1, 3, 3, NULL, NULL, NULL, NULL, 1986, '一级', 0, '出土于一号祭祀坑，肩部饰有龙虎纹样，器形属于商代常见的大口尊类。', '其器形与纹饰与中原及长江中游地区的商代铜尊有相似之处，是古蜀与外部文化交流的证据之一。', '这件龙虎尊的造型和中原商代铜尊很像，说明三千多年前的古蜀并不封闭。', '/static/images/artifacts/longhuzun.jpg', 380, 1),
(16, 'SXD-K2-008', '青铜兽面', '兽面', 7, 3, 2, 1, 3, 4, NULL, NULL, NULL, NULL, 1986, '二级', 0, '出土于二号祭祀坑，以较薄的青铜铸成，呈夔龙形向两侧展开，下部为兽面。', '兽面造型与古蜀人的神灵观念有关，是三星堆青铜面具体系的重要组成部分。', '这件兽面薄如片，两边像龙一样卷曲，或许曾挂在某种神器上。', '/static/images/artifacts/shoumian.jpg', 350, 1),
(17, 'SXD-K2-009', '青铜眼形器', '眼形器', 8, 3, 2, 1, 3, 4, NULL, NULL, NULL, NULL, 1986, '三级', 0, '祭祀坑中出土多件眼形、眼泡类青铜器。', '大量与"眼睛"相关的器物，显示了古蜀人对眼睛的特殊崇拜，常与纵目面具一起讨论。', '三星堆出土了许多"眼睛"，古蜀人似乎对眼睛有着特别的崇拜。', '/static/images/artifacts/yanxingqi.jpg', 300, 1),
(18, 'SXD-YZ-001', '陶盉', '盉', 5, 2, NULL, 5, 2, 2, NULL, NULL, NULL, NULL, NULL, '三级', 0, '三星堆遗址生活区出土的陶质酒器，带有袋状足与流口。', '陶盉是三星堆文化具有代表性的陶器之一，也反映了与中原二里头文化等的联系。', '这件陶盉是古蜀人的酒器，三条袋状足稳稳立着，让我们看到了古蜀人的日常生活。', '/static/images/artifacts/taohe.jpg', 210, 1);

-- artifact_image
INSERT INTO artifact_image (artifact_id, image_url, image_type, title, width_px, height_px, is_cover, sort) VALUES
(1, '/static/images/artifacts/shenshu.jpg', 'PHOTO', '青铜神树 正面', 1920, 1280, 1, 1),
(1, '/static/images/artifacts/shenshu_detail.jpg', 'DETAIL', '青铜神树 细节', 1920, 1280, 0, 2),
(1, '/static/images/artifacts/shenshu_train_001.jpg', 'TRAIN', '青铜神树 3DGS训练视图', 1600, 1200, 0, 3),
(2, '/static/images/artifacts/zongmu.jpg', 'PHOTO', '青铜纵目面具 正面', 1920, 1280, 1, 1),
(2, '/static/images/artifacts/zongmu_detail.jpg', 'DETAIL', '青铜纵目面具 细节', 1920, 1280, 0, 2),
(2, '/static/images/artifacts/zongmu_train_001.jpg', 'TRAIN', '青铜纵目面具 3DGS训练视图', 1600, 1200, 0, 3),
(3, '/static/images/artifacts/dalirenxiang.jpg', 'PHOTO', '青铜大立人像 正面', 1920, 1280, 1, 1),
(3, '/static/images/artifacts/dalirenxiang_detail.jpg', 'DETAIL', '青铜大立人像 细节', 1920, 1280, 0, 2),
(3, '/static/images/artifacts/dalirenxiang_train_001.jpg', 'TRAIN', '青铜大立人像 3DGS训练视图', 1600, 1200, 0, 3),
(4, '/static/images/artifacts/jinmianzhao.jpg', 'PHOTO', '戴金面罩青铜人头像 正面', 1920, 1280, 1, 1),
(4, '/static/images/artifacts/jinmianzhao_detail.jpg', 'DETAIL', '戴金面罩青铜人头像 细节', 1920, 1280, 0, 2),
(4, '/static/images/artifacts/jinmianzhao_train_001.jpg', 'TRAIN', '戴金面罩青铜人头像 3DGS训练视图', 1600, 1200, 0, 3),
(5, '/static/images/artifacts/jinzhang.jpg', 'PHOTO', '金杖 正面', 1920, 1280, 1, 1),
(5, '/static/images/artifacts/jinzhang_detail.jpg', 'DETAIL', '金杖 细节', 1920, 1280, 0, 2),
(5, '/static/images/artifacts/jinzhang_train_001.jpg', 'TRAIN', '金杖 3DGS训练视图', 1600, 1200, 0, 3),
(6, '/static/images/artifacts/jinmianju.jpg', 'PHOTO', '黄金面具（五号坑） 正面', 1920, 1280, 1, 1),
(6, '/static/images/artifacts/jinmianju_detail.jpg', 'DETAIL', '黄金面具（五号坑） 细节', 1920, 1280, 0, 2),
(6, '/static/images/artifacts/jinmianju_train_001.jpg', 'TRAIN', '黄金面具（五号坑） 3DGS训练视图', 1600, 1200, 0, 3),
(7, '/static/images/artifacts/shentan.jpg', 'PHOTO', '青铜神坛 正面', 1920, 1280, 1, 1),
(7, '/static/images/artifacts/shentan_detail.jpg', 'DETAIL', '青铜神坛 细节', 1920, 1280, 0, 2),
(8, '/static/images/artifacts/dingzun.jpg', 'PHOTO', '铜顶尊跪坐人像 正面', 1920, 1280, 1, 1),
(8, '/static/images/artifacts/dingzun_detail.jpg', 'DETAIL', '铜顶尊跪坐人像 细节', 1920, 1280, 0, 2),
(9, '/static/images/artifacts/xiangya.jpg', 'PHOTO', '祭祀坑出土象牙 正面', 1920, 1280, 1, 1),
(9, '/static/images/artifacts/xiangya_detail.jpg', 'DETAIL', '祭祀坑出土象牙 细节', 1920, 1280, 0, 2),
(10, '/static/images/artifacts/yuzhang.jpg', 'PHOTO', '玉边璋 正面', 1920, 1280, 1, 1),
(10, '/static/images/artifacts/yuzhang_detail.jpg', 'DETAIL', '玉边璋 细节', 1920, 1280, 0, 2),
(11, '/static/images/artifacts/taiyang.jpg', 'PHOTO', '青铜太阳形器 正面', 1920, 1280, 1, 1),
(11, '/static/images/artifacts/taiyang_detail.jpg', 'DETAIL', '青铜太阳形器 细节', 1920, 1280, 0, 2),
(12, '/static/images/artifacts/rentouxiang.jpg', 'PHOTO', '青铜人头像 正面', 1920, 1280, 1, 1),
(12, '/static/images/artifacts/rentouxiang_detail.jpg', 'DETAIL', '青铜人头像 细节', 1920, 1280, 0, 2),
(13, '/static/images/artifacts/niutou.jpg', 'PHOTO', '青铜扭头跪坐人像 正面', 1920, 1280, 1, 1),
(13, '/static/images/artifacts/niutou_detail.jpg', 'DETAIL', '青铜扭头跪坐人像 细节', 1920, 1280, 0, 2),
(14, '/static/images/artifacts/guibei.jpg', 'PHOTO', '龟背形网格状器 正面', 1920, 1280, 1, 1),
(14, '/static/images/artifacts/guibei_detail.jpg', 'DETAIL', '龟背形网格状器 细节', 1920, 1280, 0, 2),
(15, '/static/images/artifacts/longhuzun.jpg', 'PHOTO', '青铜龙虎尊 正面', 1920, 1280, 1, 1),
(15, '/static/images/artifacts/longhuzun_detail.jpg', 'DETAIL', '青铜龙虎尊 细节', 1920, 1280, 0, 2),
(16, '/static/images/artifacts/shoumian.jpg', 'PHOTO', '青铜兽面 正面', 1920, 1280, 1, 1),
(16, '/static/images/artifacts/shoumian_detail.jpg', 'DETAIL', '青铜兽面 细节', 1920, 1280, 0, 2),
(17, '/static/images/artifacts/yanxingqi.jpg', 'PHOTO', '青铜眼形器 正面', 1920, 1280, 1, 1),
(17, '/static/images/artifacts/yanxingqi_detail.jpg', 'DETAIL', '青铜眼形器 细节', 1920, 1280, 0, 2),
(18, '/static/images/artifacts/taohe.jpg', 'PHOTO', '陶盉 正面', 1920, 1280, 1, 1),
(18, '/static/images/artifacts/taohe_detail.jpg', 'DETAIL', '陶盉 细节', 1920, 1280, 0, 2);

-- model_3d
INSERT INTO model_3d (artifact_id, hall_id, name, model_type, model_url, format, file_size, gaussian_count, sh_degree, image_count, capture_device, sfm_tool, trained_by, train_iterations, train_minutes, psnr, ssim, lpips, camera_position, camera_target, thumbnail_url, is_default, status, remark) VALUES
(1, NULL, '青铜神树 3DGS 模型', 'OBJECT', '/models/splat/shenshu.splat', 'splat', 52428800, 1638400, 3, 220, '单反相机 + 环形轨道', 'COLMAP', 'gaussian-splatting 官方实现', 30000, 42.5, 31.82, 0.942, 0.081, '[0,1.6,4.5]', '[0,1.5,0]', '/static/images/models/shenshu.jpg', 1, 1, '演示数据，模型文件待替换'),
(1, NULL, '青铜神树 3DGS 模型（压缩版）', 'OBJECT', '/models/ksplat/shenshu.ksplat', 'ksplat', 18874368, 1638400, 1, 220, '单反相机 + 环形轨道', 'COLMAP', 'gaussian-splatting 官方实现', 30000, 42.5, 31.2, 0.935, 0.089, '[0,1.6,4.5]', '[0,1.5,0]', '/static/images/models/shenshu.jpg', 0, 1, 'Web端流式加载用'),
(2, NULL, '青铜纵目面具 3DGS 模型', 'OBJECT', '/models/splat/zongmu.splat', 'splat', 31457280, 982000, 3, 160, '手机多视角拍摄', 'COLMAP', 'gsplat', 30000, 25.0, 33.06, 0.961, 0.062, '[0,0,2.2]', '[0,0,0]', '/static/images/models/zongmu.jpg', 1, 1, '演示数据，模型文件待替换'),
(3, NULL, '青铜大立人像 3DGS 模型', 'OBJECT', '/models/ply/dalirenxiang.ply', 'ply', 241172480, 1050000, 3, 240, '单反相机 + 环形轨道', 'COLMAP', 'gaussian-splatting 官方实现', 30000, 48.3, 30.95, 0.938, 0.092, '[0,1.3,4.0]', '[0,1.2,0]', '/static/images/models/dalirenxiang.jpg', 1, 1, '原始ply，体积较大'),
(4, NULL, '戴金面罩青铜人头像 3DGS 模型', 'OBJECT', '/models/splat/jinmianzhao.splat', 'splat', 20971520, 655000, 3, 140, '手机多视角拍摄', 'COLMAP', 'gsplat', 30000, 19.6, 32.41, 0.955, 0.071, '[0,0,1.6]', '[0,0,0]', '/static/images/models/jinmianzhao.jpg', 1, 1, '演示数据，模型文件待替换'),
(5, NULL, '金杖 3DGS 模型', 'OBJECT', '/models/splat/jinzhang.splat', 'splat', 12582912, 393000, 3, 120, '微距镜头', 'COLMAP', 'gsplat', 20000, 12.4, 34.1, 0.968, 0.055, '[0,0,1.8]', '[0,0,0]', '/static/images/models/jinzhang.jpg', 1, 1, '演示数据，模型文件待替换'),
(6, NULL, '黄金面具（五号坑）3DGS 模型', 'OBJECT', '/models/splat/jinmianju.splat', 'splat', 14680064, 458000, 3, 130, '微距镜头', 'COLMAP', 'gsplat', 30000, 15.2, 33.75, 0.964, 0.058, '[0,0,1.2]', '[0,0,0]', '/static/images/models/jinmianju.jpg', 1, 1, '演示数据，模型文件待替换'),
(11, NULL, '青铜太阳形器 3DGS 模型', 'OBJECT', '/models/spz/taiyang.spz', 'spz', 8388608, 520000, 3, 110, '手机多视角拍摄', 'COLMAP', 'gsplat', 30000, 16.8, 32.9, 0.957, 0.066, '[0,0,2.0]', '[0,0,0]', '/static/images/models/taiyang.jpg', 1, 0, '训练中'),
(NULL, 3, '天地人神展厅 场景模型', 'SCENE', '/models/scene/hall_h03.ksplat', 'ksplat', 157286400, 3200000, 3, 680, '全景相机 + 单反', 'COLMAP', 'gaussian-splatting 官方实现', 30000, 96.0, 27.64, 0.882, 0.173, '[0,1.7,12]', '[0,1.5,0]', '/static/images/models/hall_h03.jpg', 1, 1, '展厅级数字孪生场景（演示）');

-- restoration_record
INSERT INTO restoration_record (artifact_id, restorer, start_date, end_date, condition_before, method, condition_after, status, remark) VALUES
(1, '三星堆博物馆文物保护修复团队', NULL, NULL, '出土时断裂成多段，残缺严重，多处锈蚀。', '清理除锈、拼对、焊接与加固，并对缺失部位进行支撑。', '主体结构复原，现于展厅展出。', 'DONE', '修复历时多年（具体时间以馆方资料为准）'),
(6, '三星堆考古发掘与文物保护团队', NULL, NULL, '出土时褶皱变形。', '实验室清理、整形展平。', '恢复面具基本形态。', 'DONE', NULL),
(7, '三星堆考古发掘与文物保护团队', NULL, NULL, '残件分散于不同祭祀坑。', '三维扫描辅助的跨坑拼对与虚拟复原。', '多个构件完成拼对。', 'ONGOING', '跨坑拼对研究持续进行中'),
(9, '三星堆文物保护修复团队', NULL, NULL, '出土后失水易开裂。', '保湿包裹、低温保存与加固处理。', '状态稳定。', 'ONGOING', NULL);

-- literature
INSERT INTO literature (id, title, authors, source, publish_year, lit_type, keywords, abstract_text, content) VALUES
(1, '华阳国志·蜀志', '常璩', '古籍', '东晋', 'ANCIENT', '蚕丛,鱼凫,柏灌,纵目,古蜀', '东晋常璩所撰地方志，记载了古蜀国早期诸王的传说。', '有周之衰，蜀先称王。有蜀侯蚕丛，其目纵，始称王。死，作石棺石椁，国人从之，故俗以石棺椁为纵目人冢也。次王曰柏灌。次王曰鱼凫。'),
(2, '蜀道难', '李白', '唐诗', '唐', 'ANCIENT', '蚕丛,鱼凫,古蜀', '李白名篇，开篇提及古蜀先王蚕丛与鱼凫。', '噫吁嚱，危乎高哉！蜀道之难，难于上青天！蚕丛及鱼凫，开国何茫然！尔来四万八千岁，不与秦塞通人烟。'),
(3, '山海经·海外东经（扶桑）', '佚名', '古籍', '先秦至汉', 'ANCIENT', '扶桑,神树,太阳,十日', '记载了"十日"栖息于扶桑神树的神话，常被用来解读三星堆青铜神树。', '汤谷上有扶桑，十日所浴，在黑齿北。居水中，有大木，九日居下枝，一日居上枝。'),
(4, '三星堆祭祀坑', '四川省文物考古研究所', '文物出版社', '1999', 'REPORT', '一号祭祀坑,二号祭祀坑,考古报告,青铜神树,金杖', '1986年一、二号祭祀坑的正式考古发掘报告。', '本报告系统发表了1986年发掘的一号、二号祭祀坑资料，包括坑的形制、埋藏情况及出土的青铜器、金器、玉石器、象牙等遗物，是研究三星堆文化最基础的资料之一。'),
(5, '三星堆考古发现历程（导览文稿）', '系统编写', '本系统', '2026', 'GUIDE', '燕道诚,葛维汉,考古历程,祭祀坑,新发现', '按时间线梳理三星堆遗址近百年的考古发现。', '1929年，广汉农民燕道诚在宅旁挖水沟时发现一批玉石器，三星堆由此进入人们视野。1934年，华西协合大学博物馆葛维汉等人在月亮湾一带进行了首次考古发掘。1986年，一号、二号祭祀坑相继被发现，出土青铜神树、青铜大立人像、纵目面具、金杖等珍贵文物，引起世界轰动。2019年至2020年，又在祭祀区新发现三至八号祭祀坑，2021年3月发布了阶段性考古成果，出土黄金面具、顶尊跪坐人像、龟背形网格状器、青铜神坛等重要文物。'),
(6, '古蜀青铜铸造与黄金工艺简述（导览文稿）', '系统编写', '本系统', '2026', 'GUIDE', '青铜铸造,分铸,金器,锤揲,金箔', '概述三星堆青铜器与金器的制作工艺。', '三星堆大型青铜器多采用分段铸造、再以铸接等方式组合成器，青铜神树、青铜神坛等复杂器物体现了较高的铸造水平。金器多以金块锤揲成金皮、金箔，再经剪切、刻划、包卷等工序制成，金杖即为金皮包卷木杖而成，金面罩则贴附于青铜人头像面部。'),
(7, '3D Gaussian Splatting for Real-Time Radiance Field Rendering', 'Bernhard Kerbl, Georgios Kopanas, Thomas Leimkühler, George Drettakis', 'ACM Transactions on Graphics (SIGGRAPH)', '2023', 'PAPER', '3D高斯泼溅,3DGS,实时渲染,辐射场', '提出以三维高斯基元表示场景、通过可微光栅化实现实时高质量新视角合成的方法。', '该方法从SfM稀疏点云初始化三维高斯，每个高斯具有位置、协方差、不透明度与球谐系数，并通过自适应密度控制与可微分的基于瓦片的光栅化进行优化，可在1080p分辨率下实现实时渲染。本系统使用该技术对文物进行数字化重建。'),
(8, 'From Local to Global: A Graph RAG Approach to Query-Focused Summarization', 'Darren Edge 等', 'arXiv（Microsoft Research）', '2024', 'PAPER', 'GraphRAG,知识图谱,检索增强生成', '提出基于知识图谱的检索增强生成方法GraphRAG。', 'GraphRAG先利用大模型从文本中抽取实体与关系构建知识图谱，再结合图结构检索相关子图与社区摘要作为上下文生成回答，相比仅基于向量检索的RAG更擅长回答全局性、关联性问题。本系统借鉴其思想，以文物知识图谱子图作为问答上下文。');

-- artifact_literature
INSERT INTO artifact_literature (artifact_id, literature_id, note) VALUES
(1, 3, '扶桑神话与神树'),
(1, 4, '考古报告著录'),
(2, 1, '蚕丛纵目记载'),
(2, 4, '考古报告著录'),
(3, 4, '考古报告著录'),
(5, 4, '考古报告著录'),
(5, 2, '鱼凫传说'),
(11, 3, '太阳崇拜'),
(6, 5, '新发现'),
(7, 5, '新发现'),
(8, 5, '新发现'),
(14, 5, '新发现'),
(5, 6, '金器工艺'),
(4, 6, '金面罩工艺'),
(7, 6, '分铸工艺');

-- kg_entity
INSERT INTO kg_entity (id, name, entity_type, aliases, description, ref_table, ref_id, weight) VALUES
(1, '青铜神树', 'ARTIFACT', '一号神树,I号大型铜神树,神树', '出土于二号祭祀坑，由底座、树干和树枝组成。树干分三层，每层伸出三枝，共九枝，枝上有花果与立鸟，树干一侧有一条龙沿树干蜿蜒而下。出土时已断裂破碎，经多年修复拼接复原。', 'artifact', 1, 3.0),
(2, '青铜纵目面具', 'ARTIFACT', '纵目面具,千里眼顺风耳,大面具', '出土于二号祭祀坑，面具双眼呈柱状向前凸出，双耳向两侧展开，嘴角上扬，造型夸张。', 'artifact', 2, 3.0),
(3, '青铜大立人像', 'ARTIFACT', '大立人,青铜立人像', '出土于二号祭祀坑，人像头戴冠，身着多层衣，赤足立于方形台座之上，双手环握于胸前呈中空状，原先可能持有某种器物。', 'artifact', 3, 3.0),
(4, '戴金面罩青铜人头像', 'ARTIFACT', '金面罩人头像,金面罩', '出土于二号祭祀坑，青铜人头像面部覆贴一层金箔面罩，金面罩眉眼部分镂空。', 'artifact', 4, 3.0),
(5, '金杖', 'ARTIFACT', '金权杖', '出土于一号祭祀坑，以金条锤打成金皮后包卷在木杖外，木杖已炭化，仅存金皮。金皮上端刻有人头、鱼、鸟和箭（或称穗形）等图案。', 'artifact', 5, 3.0),
(6, '黄金面具（五号坑）', 'ARTIFACT', '五号坑金面具,半张金面具', '2021年出土于五号祭祀坑，现存约半张，眼部镂空，耳部突出。', 'artifact', 6, 3.0),
(7, '青铜神坛', 'ARTIFACT', '神坛', '出土于八号祭祀坑，由多层结构组成，表现了人物、神兽等元素，结构复杂。部分构件与早年二号坑出土的残件可以拼对。', 'artifact', 7, 2.0),
(8, '铜顶尊跪坐人像', 'ARTIFACT', '顶尊跪坐人像,顶尊人像', '出土于三号祭祀坑，人像双膝跪坐，双手合于胸前，头顶一件大口尊。', 'artifact', 8, 2.0),
(9, '祭祀坑出土象牙', 'ARTIFACT', '象牙', '三星堆祭祀坑中普遍出土象牙，1986年一、二号坑出土象牙数十根，新发现的三号坑象牙多达百余根。', 'artifact', 9, 2.0),
(10, '玉边璋', 'ARTIFACT', '祭山图玉璋,边璋,玉璋', '出土于二号祭祀坑，璋身两面刻有成组的图案，表现了人物、山形等内容，常被称为"祭山图"。', 'artifact', 10, 2.0),
(11, '青铜太阳形器', 'ARTIFACT', '太阳轮,太阳形器', '出土于二号祭祀坑，圆形，中心为凸起的圆泡，周围放射出五道芒，外围为一圆环。', 'artifact', 11, 2.0),
(12, '青铜人头像', 'ARTIFACT', '平顶人头像', '祭祀坑出土的青铜人头像数量较多，发型、冠饰各有不同，眉眼粗犷，下颌宽大。', 'artifact', 12, 2.0),
(13, '青铜扭头跪坐人像', 'ARTIFACT', '扭头跪坐人像', '出土于四号祭祀坑，人像跪坐，头部向一侧扭转，双手作合握状，发型独特。', 'artifact', 13, 2.0),
(14, '龟背形网格状器', 'ARTIFACT', '龟背形器', '出土于七号祭祀坑，整体呈网格状，形似龟背，两侧有提手类结构，内部发现有玉器。', 'artifact', 14, 2.0),
(15, '青铜龙虎尊', 'ARTIFACT', '龙虎尊', '出土于一号祭祀坑，肩部饰有龙虎纹样，器形属于商代常见的大口尊类。', 'artifact', 15, 2.0),
(16, '青铜兽面', 'ARTIFACT', '兽面', '出土于二号祭祀坑，以较薄的青铜铸成，呈夔龙形向两侧展开，下部为兽面。', 'artifact', 16, 2.0),
(17, '青铜眼形器', 'ARTIFACT', '眼形器', '祭祀坑中出土多件眼形、眼泡类青铜器。', 'artifact', 17, 2.0),
(18, '陶盉', 'ARTIFACT', '盉', '三星堆遗址生活区出土的陶质酒器，带有袋状足与流口。', 'artifact', 18, 2.0),
(19, '一号祭祀坑', 'PIT', 'K1,1号坑,一号坑', '1986年发现并发掘，出土金杖、金面罩、青铜人头像、龙虎尊、玉石器及象牙等。', 'excavation_pit', 1, 2.5),
(20, '二号祭祀坑', 'PIT', 'K2,2号坑,二号坑', '紧邻一号坑发现，出土青铜神树、青铜大立人像、青铜纵目面具、太阳形器、戴金面罩铜人头像及大量象牙。', 'excavation_pit', 2, 2.5),
(21, '三号祭祀坑', 'PIT', 'K3,3号坑,三号坑', '2019年发现，2020年起开展系统发掘，出土顶尊跪坐人像、青铜大面具等，象牙数量多达百余根。', 'excavation_pit', 3, 2.5),
(22, '四号祭祀坑', 'PIT', 'K4,4号坑,四号坑', '出土扭头跪坐人像等，其出土样品的碳十四测年为判定祭祀区年代提供了重要依据。', 'excavation_pit', 4, 2.5),
(23, '五号祭祀坑', 'PIT', 'K5,5号坑,五号坑', '面积较小，出土半张黄金面具及金箔饰件、象牙雕刻残件等。', 'excavation_pit', 5, 2.5),
(24, '六号祭祀坑', 'PIT', 'K6,6号坑,六号坑', '发现一件炭化木箱（木匣），为研究祭祀器具与埋藏方式提供了新材料。', 'excavation_pit', 6, 2.5),
(25, '七号祭祀坑', 'PIT', 'K7,7号坑,七号坑', '出土龟背形网格状器等造型奇特的青铜器及象牙。', 'excavation_pit', 7, 2.5),
(26, '八号祭祀坑', 'PIT', 'K8,8号坑,八号坑', '出土青铜神坛、神兽等复杂青铜器，部分器物可与二号坑残件跨坑拼对。', 'excavation_pit', 8, 2.5),
(27, '青铜', 'MATERIAL', '青铜器,铜', NULL, 'material', 1, 1.5),
(28, '黄金', 'MATERIAL', '金,金器', NULL, 'material', 2, 1.5),
(29, '玉', 'MATERIAL', '玉器,玉石', NULL, 'material', 3, 1.5),
(30, '象牙', 'MATERIAL', '象牙制品', NULL, 'material', 4, 1.5),
(31, '陶', 'MATERIAL', '陶器', NULL, 'material', 5, 1.5),
(32, '三星堆遗址第一期', 'PERIOD', '宝墩期', NULL, 'dynasty_period', 1, 1.5),
(33, '三星堆遗址第二、三期', 'PERIOD', '夏商时期', NULL, 'dynasty_period', 2, 1.5),
(34, '商代晚期', 'PERIOD', '商晚期,殷商晚期', NULL, 'dynasty_period', 3, 1.5),
(35, '三星堆遗址第四期', 'PERIOD', '商末周初', NULL, 'dynasty_period', 4, 1.5),
(36, '宝墩文化', 'CULTURE', '宝墩', '成都平原新石器时代晚期考古学文化。', NULL, NULL, 1.5),
(37, '三星堆文化', 'CULTURE', NULL, '以三星堆遗址为代表的青铜时代考古学文化，是古蜀文明的鼎盛阶段。', NULL, NULL, 2.5),
(38, '十二桥文化', 'CULTURE', NULL, '以成都十二桥遗址、金沙遗址为代表的考古学文化。', NULL, NULL, 1.5),
(39, '古蜀文明', 'CULTURE', '古蜀,古蜀国,蜀', '成都平原及周边地区的古代文明，是中华文明多元一体格局中的重要组成部分。', NULL, NULL, 3.0),
(40, '中原商文化', 'CULTURE', '商文化,中原文化,殷商', '以黄河中下游为中心的商代青铜文化。', NULL, NULL, 1.5),
(41, '三星堆遗址', 'SITE', '三星堆', '位于四川省广汉市，分布面积约12平方公里，是迄今在西南地区发现的范围最大、延续时间最长、文化内涵最丰富的古城、古国、古蜀文化遗址之一。', NULL, NULL, 3.0),
(42, '三星堆祭祀区', 'SITE', '祭祀区', '三星堆遗址中八座祭祀坑集中分布的区域。', NULL, NULL, 2.0),
(43, '金沙遗址', 'SITE', '金沙', '位于四川省成都市，是十二桥文化的代表性遗址，出土太阳神鸟金饰等文物。', NULL, NULL, 2.0),
(44, '太阳神鸟金饰', 'ARTIFACT', '太阳神鸟', '金沙遗址出土的金箔饰品，图案为环绕太阳飞翔的四只神鸟，2005年被定为中国文化遗产标志。', NULL, NULL, 2.0),
(45, '三星堆博物馆', 'MUSEUM', '三星堆博物馆新馆', '位于广汉市的三星堆遗址专题博物馆，1997年建成开放，新馆于2023年开放。', 'museum', 1, 2.5),
(46, '世纪逐梦展厅', 'HALL', '世纪逐梦', '介绍自1929年发现玉石器以来，三星堆遗址近百年的考古发现与研究历程。', 'exhibition_hall', 1, 1.5),
(47, '巍然王都展厅', 'HALL', '巍然王都', '展示三星堆古城城墙、居址与陶器、玉石器等反映古蜀社会生产生活的遗存。', 'exhibition_hall', 2, 1.5),
(48, '天地人神展厅', 'HALL', '天地人神', '集中展示祭祀坑出土的青铜神树、青铜人像、青铜面具、金器等重要文物，呈现古蜀人的精神世界。', 'exhibition_hall', 3, 1.5),
(49, '文物保护与修复馆', 'HALL', '文物保护与修复馆', '以开放式实验室的形式向观众展示文物清理、拼对、修复与保护工作。', 'exhibition_hall', 4, 1.5),
(50, '太阳崇拜', 'THEME', NULL, '古蜀人对太阳的崇拜，可见于太阳形器、神树、太阳神鸟等器物。', NULL, NULL, 2.0),
(51, '神树信仰', 'THEME', NULL, '与扶桑、若木、建木等神树神话相关的信仰观念。', NULL, NULL, 2.0),
(52, '祭祀礼仪', 'THEME', NULL, '祭祀坑及其出土器物反映的古蜀祭祀活动。', NULL, NULL, 2.0),
(53, '王权与神权', 'THEME', NULL, '金杖、大立人像等反映的古蜀统治者集王权与神权于一身的观念。', NULL, NULL, 2.0),
(54, '眼睛崇拜', 'THEME', NULL, '纵目面具、眼形器等体现的对眼睛的特殊崇拜。', NULL, NULL, 2.0),
(55, '文明交流', 'THEME', NULL, '古蜀与中原及长江流域等地区的文化交流。', NULL, NULL, 2.0),
(56, '青铜分铸工艺', 'TECH', '分铸,铸接,青铜铸造', '大型青铜器分段铸造、再组合成器的工艺。', NULL, NULL, 1.5),
(57, '金器锤揲工艺', 'TECH', '锤揲,金箔,黄金工艺', '将金块锤打成金皮、金箔再加工成器的工艺。', NULL, NULL, 1.5),
(58, '蚕丛', 'PERSON', NULL, '传说中古蜀国的开国之王，《华阳国志》称其"其目纵"。', NULL, NULL, 2.0),
(59, '鱼凫', 'PERSON', NULL, '传说中的古蜀王之一，见于《华阳国志》与李白《蜀道难》。', NULL, NULL, 2.0),
(60, '燕道诚', 'PERSON', NULL, '1929年在广汉发现一批玉石器的当地村民，三星堆由此为世人所知。', NULL, NULL, 1.0),
(61, '葛维汉', 'PERSON', NULL, '华西协合大学博物馆馆长，1934年主持了三星堆一带的首次考古发掘。', NULL, NULL, 1.0),
(62, '《华阳国志·蜀志》', 'LITERATURE', '华阳国志·蜀志', '东晋常璩所撰地方志，记载了古蜀国早期诸王的传说。', 'literature', 1, 1.2),
(63, '《蜀道难》', 'LITERATURE', '蜀道难', '李白名篇，开篇提及古蜀先王蚕丛与鱼凫。', 'literature', 2, 1.2),
(64, '《山海经·海外东经》', 'LITERATURE', '山海经·海外东经（扶桑）', '记载了"十日"栖息于扶桑神树的神话，常被用来解读三星堆青铜神树。', 'literature', 3, 1.2),
(65, '《三星堆祭祀坑》', 'LITERATURE', '三星堆祭祀坑', '1986年一、二号祭祀坑的正式考古发掘报告。', 'literature', 4, 1.2),
(66, '《三星堆考古发现历程》', 'LITERATURE', '三星堆考古发现历程（导览文稿）', '按时间线梳理三星堆遗址近百年的考古发现。', 'literature', 5, 1.2);

-- kg_relation
INSERT INTO kg_relation (source_id, target_id, relation_type, relation_name, weight, description) VALUES
(1, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(1, 27, 'MADE_OF', '材质为', 1.0, NULL),
(1, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(1, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(2, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(2, 27, 'MADE_OF', '材质为', 1.0, NULL),
(2, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(2, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(3, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(3, 27, 'MADE_OF', '材质为', 1.0, NULL),
(3, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(3, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(4, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(4, 27, 'MADE_OF', '材质为', 1.0, NULL),
(4, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(4, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(5, 19, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(5, 28, 'MADE_OF', '材质为', 1.0, NULL),
(5, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(5, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(6, 23, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(6, 28, 'MADE_OF', '材质为', 1.0, NULL),
(6, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(6, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(7, 26, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(7, 27, 'MADE_OF', '材质为', 1.0, NULL),
(7, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(7, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(8, 21, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(8, 27, 'MADE_OF', '材质为', 1.0, NULL),
(8, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(8, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(9, 21, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(9, 30, 'MADE_OF', '材质为', 1.0, NULL),
(9, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(9, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(10, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(10, 29, 'MADE_OF', '材质为', 1.0, NULL),
(10, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(10, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(11, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(11, 27, 'MADE_OF', '材质为', 1.0, NULL),
(11, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(11, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(12, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(12, 27, 'MADE_OF', '材质为', 1.0, NULL),
(12, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(12, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(13, 22, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(13, 27, 'MADE_OF', '材质为', 1.0, NULL),
(13, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(13, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(14, 25, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(14, 27, 'MADE_OF', '材质为', 1.0, NULL),
(14, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(14, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(15, 19, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(15, 27, 'MADE_OF', '材质为', 1.0, NULL),
(15, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(15, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(16, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(16, 27, 'MADE_OF', '材质为', 1.0, NULL),
(16, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(16, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(17, 20, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(17, 27, 'MADE_OF', '材质为', 1.0, NULL),
(17, 34, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(17, 48, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(18, 31, 'MADE_OF', '材质为', 1.0, NULL),
(18, 33, 'BELONGS_TO_PERIOD', '年代属于', 1.0, NULL),
(18, 47, 'DISPLAYED_IN', '陈列于', 1.0, NULL),
(19, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(19, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(20, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(20, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(21, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(21, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(22, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(22, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(23, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(23, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(24, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(24, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(25, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(25, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(26, 42, 'LOCATED_IN', '位于', 1.0, NULL),
(26, 34, 'DATED_TO', '埋藏年代为', 1.0, NULL),
(42, 41, 'PART_OF', '属于', 1.0, NULL),
(32, 36, 'CORRESPONDS_TO', '对应文化', 1.0, NULL),
(33, 37, 'CORRESPONDS_TO', '对应文化', 1.0, NULL),
(34, 37, 'CORRESPONDS_TO', '对应文化', 1.0, NULL),
(35, 38, 'CORRESPONDS_TO', '对应文化', 1.0, NULL),
(36, 39, 'PART_OF', '属于', 1.0, NULL),
(37, 39, 'PART_OF', '属于', 1.0, NULL),
(38, 39, 'PART_OF', '属于', 1.0, NULL),
(41, 37, 'REPRESENTS', '代表遗址', 1.0, NULL),
(43, 38, 'REPRESENTS', '代表遗址', 1.0, NULL),
(37, 38, 'SUCCEEDED_BY', '发展为', 1.0, '两者存在承续关系'),
(41, 43, 'RELATED_TO', '文化关联', 1.0, NULL),
(44, 43, 'EXCAVATED_FROM', '出土于', 1.0, NULL),
(44, 50, 'REFLECTS', '体现', 1.0, NULL),
(45, 41, 'LOCATED_IN', '位于', 1.0, NULL),
(46, 45, 'PART_OF', '属于', 1.0, NULL),
(47, 45, 'PART_OF', '属于', 1.0, NULL),
(48, 45, 'PART_OF', '属于', 1.0, NULL),
(49, 45, 'PART_OF', '属于', 1.0, NULL),
(1, 51, 'REFLECTS', '体现', 1.0, NULL),
(1, 50, 'REFLECTS', '体现', 1.0, NULL),
(1, 52, 'REFLECTS', '体现', 1.0, NULL),
(2, 54, 'REFLECTS', '体现', 1.0, NULL),
(3, 53, 'REFLECTS', '体现', 1.0, NULL),
(3, 52, 'REFLECTS', '体现', 1.0, NULL),
(4, 53, 'REFLECTS', '体现', 1.0, NULL),
(5, 53, 'REFLECTS', '体现', 1.0, NULL),
(6, 52, 'REFLECTS', '体现', 1.0, NULL),
(7, 52, 'REFLECTS', '体现', 1.0, NULL),
(8, 55, 'REFLECTS', '体现', 1.0, NULL),
(8, 52, 'REFLECTS', '体现', 1.0, NULL),
(9, 52, 'REFLECTS', '体现', 1.0, NULL),
(10, 52, 'REFLECTS', '体现', 1.0, NULL),
(11, 50, 'REFLECTS', '体现', 1.0, NULL),
(14, 52, 'REFLECTS', '体现', 1.0, NULL),
(15, 55, 'REFLECTS', '体现', 1.0, NULL),
(17, 54, 'REFLECTS', '体现', 1.0, NULL),
(18, 55, 'REFLECTS', '体现', 1.0, NULL),
(1, 56, 'MADE_BY', '制作工艺', 1.0, NULL),
(3, 56, 'MADE_BY', '制作工艺', 1.0, NULL),
(7, 56, 'MADE_BY', '制作工艺', 1.0, NULL),
(4, 57, 'MADE_BY', '制作工艺', 1.0, NULL),
(5, 57, 'MADE_BY', '制作工艺', 1.0, NULL),
(6, 57, 'MADE_BY', '制作工艺', 1.0, NULL),
(2, 58, 'ASSOCIATED_WITH', '可能关联', 0.6, '有学者据"其目纵"推测纵目面具与蚕丛有关'),
(5, 59, 'ASSOCIATED_WITH', '可能关联', 0.6, '有学者认为金杖鱼鸟纹与鱼凫传说有关'),
(1, 11, 'SIMILAR_THEME', '主题相近', 1.0, '均与太阳崇拜相关'),
(11, 44, 'SIMILAR_THEME', '主题相近', 1.0, '均与太阳崇拜相关'),
(2, 17, 'SIMILAR_THEME', '主题相近', 1.0, '均与眼睛崇拜相关'),
(4, 12, 'SIMILAR_TYPE', '同类器物', 1.0, NULL),
(7, 20, 'JOINED_WITH', '跨坑拼对', 0.8, '部分构件可与二号坑残件拼对'),
(15, 40, 'INFLUENCED_BY', '受影响于', 1.0, NULL),
(8, 40, 'INFLUENCED_BY', '受影响于', 1.0, NULL),
(60, 41, 'DISCOVERED', '发现', 1.0, '1929年发现玉石器'),
(61, 41, 'EXCAVATED', '首次发掘', 1.0, '1934年'),
(58, 39, 'BELONGS_TO', '传说人物', 1.0, NULL),
(59, 39, 'BELONGS_TO', '传说人物', 1.0, NULL),
(62, 58, 'MENTIONS', '记载', 1.0, NULL),
(62, 59, 'MENTIONS', '记载', 1.0, NULL),
(63, 58, 'MENTIONS', '记载', 1.0, NULL),
(63, 59, 'MENTIONS', '记载', 1.0, NULL),
(64, 51, 'MENTIONS', '记载', 1.0, NULL),
(65, 19, 'MENTIONS', '著录', 1.0, NULL),
(65, 20, 'MENTIONS', '著录', 1.0, NULL),
(66, 41, 'MENTIONS', '介绍', 1.0, NULL);

-- guide_route
INSERT INTO guide_route (id, museum_id, name, route_type, theme, description, duration_min, distance_m, difficulty, suitable_crowd, cover_url, use_count) VALUES
(1, 1, '镇馆之宝经典路线', 'CLASSIC', '国宝精华', '串联三星堆最具代表性的镇馆之宝，适合首次到访的观众。', 90, 1200, 2, '首次参观的所有观众', '/static/images/routes/classic.jpg', 326),
(2, 1, '神秘面具主题路线', 'THEME', '面具与人像', '聚焦纵目面具、金面罩人头像、黄金面具等，解读古蜀人的"面孔"。', 45, 600, 1, '历史与艺术爱好者', '/static/images/routes/mask.jpg', 158),
(3, 1, '太阳与神树主题路线', 'THEME', '太阳崇拜', '从青铜神树到太阳形器，探索古蜀人的宇宙观与太阳信仰。', 40, 500, 1, '对神话与宗教感兴趣的观众', '/static/images/routes/sun.jpg', 97),
(4, 1, '亲子研学路线', 'FAMILY', '研学探秘', '以故事化讲解带领孩子认识象牙、神树、人像，并参观文物修复。', 60, 800, 1, '亲子家庭、中小学生', '/static/images/routes/family.jpg', 211),
(5, 1, '30分钟快速打卡', 'QUICK', '快速参观', '时间有限的观众可在半小时内看完最重要的四件文物。', 30, 400, 1, '时间紧张的观众', '/static/images/routes/quick.jpg', 402);

-- guide_route_node
INSERT INTO guide_route_node (route_id, seq, node_type, artifact_id, hall_id, title, narration, stay_minutes, camera_position, camera_target) VALUES
(1, 1, 'HALL', NULL, 1, '世纪逐梦', '欢迎来到「世纪逐梦」，介绍自1929年发现玉石器以来，三星堆遗址近百年的考古发现与研究历程。', 10, '[2.5,1.6,5.0]', '[2.5,1.2,0]'),
(1, 2, 'ARTIFACT', 5, 3, '金杖', '这根金杖长约1.42米，上面刻着戴冠的人头、鱼和鸟。有人说它记录的是古蜀王"鱼凫"的故事，是权力的象征。', 5, '[5.0,1.6,6.0]', '[5.0,1.2,0]'),
(1, 3, 'ARTIFACT', 3, 3, '青铜大立人像', '这尊青铜大立人像连座高约2.6米，双手巨大，环握在胸前。他手里原本握着什么？是象牙、玉琮还是别的法器？至今仍是一个谜。', 8, '[7.5,1.6,4.0]', '[7.5,1.2,0]'),
(1, 4, 'ARTIFACT', 2, 3, '青铜纵目面具', '这件纵目面具宽一米三有余，一双眼睛像柱子一样凸出来。《华阳国志》说古蜀王蚕丛"其目纵"，它会不会就是蚕丛的形象呢？', 8, '[10.0,1.6,5.0]', '[10.0,1.2,0]'),
(1, 5, 'ARTIFACT', 1, 3, '青铜神树', '眼前这棵青铜神树，残高近四米，九根树枝上栖息着神鸟，一条龙沿着树干向下游动。它让人联想到《山海经》中"十日"栖息的扶桑神树。', 8, '[12.5,1.6,6.0]', '[12.5,1.2,0]'),
(1, 6, 'ARTIFACT', 6, 3, '黄金面具（五号坑）', '这半张金面具出土时揉皱在坑中，经过清理展开后重现光彩，它是三星堆新一轮考古发掘最受关注的发现之一。', 5, '[15.0,1.6,4.0]', '[15.0,1.2,0]'),
(1, 7, 'ARTIFACT', 7, 3, '青铜神坛', '这件青铜神坛层层叠叠，上面有人物和神兽，仿佛把一场三千年前的祭祀仪式凝固在了青铜之中。', 5, '[17.5,1.6,5.0]', '[17.5,1.2,0]'),
(2, 1, 'ARTIFACT', 2, 3, '青铜纵目面具', '这件纵目面具宽一米三有余，一双眼睛像柱子一样凸出来。《华阳国志》说古蜀王蚕丛"其目纵"，它会不会就是蚕丛的形象呢？', 8, '[2.5,1.6,5.0]', '[2.5,1.2,0]'),
(2, 2, 'ARTIFACT', 4, 3, '戴金面罩青铜人头像', '这位"金面人"的脸上贴着薄薄的金箔面罩，在灯光下熠熠生辉，是三星堆最具代表性的形象之一。', 5, '[5.0,1.6,6.0]', '[5.0,1.2,0]'),
(2, 3, 'ARTIFACT', 12, 3, '青铜人头像', '这些青铜人头像发型各异，有的平顶，有的戴冠，它们可能代表着古蜀社会中不同身份的人。', 5, '[7.5,1.6,4.0]', '[7.5,1.2,0]'),
(2, 4, 'ARTIFACT', 16, 3, '青铜兽面', '这件兽面薄如片，两边像龙一样卷曲，或许曾挂在某种神器上。', 5, '[10.0,1.6,5.0]', '[10.0,1.2,0]'),
(2, 5, 'ARTIFACT', 6, 3, '黄金面具（五号坑）', '这半张金面具出土时揉皱在坑中，经过清理展开后重现光彩，它是三星堆新一轮考古发掘最受关注的发现之一。', 5, '[12.5,1.6,6.0]', '[12.5,1.2,0]'),
(3, 1, 'ARTIFACT', 11, 3, '青铜太阳形器', '这个像车轮一样的青铜器，中央凸起，五道光芒向外放射，很多人认为它就是古蜀人心中的太阳。', 5, '[2.5,1.6,5.0]', '[2.5,1.2,0]'),
(3, 2, 'ARTIFACT', 1, 3, '青铜神树', '眼前这棵青铜神树，残高近四米，九根树枝上栖息着神鸟，一条龙沿着树干向下游动。它让人联想到《山海经》中"十日"栖息的扶桑神树。', 8, '[5.0,1.6,6.0]', '[5.0,1.2,0]'),
(3, 3, 'ARTIFACT', 17, 3, '青铜眼形器', '三星堆出土了许多"眼睛"，古蜀人似乎对眼睛有着特别的崇拜。', 5, '[7.5,1.6,4.0]', '[7.5,1.2,0]'),
(3, 4, 'ARTIFACT', 10, 3, '玉边璋', '这件玉璋上刻着"祭山图"：有人跪拜，有大山，仿佛在讲述一场祭祀山神的仪式。', 5, '[10.0,1.6,5.0]', '[10.0,1.2,0]'),
(4, 1, 'ARTIFACT', 9, 3, '祭祀坑出土象牙', '这些象牙层层铺在祭祀坑中，出土后要立刻保湿保护，否则很快就会开裂。', 5, '[2.5,1.6,5.0]', '[2.5,1.2,0]'),
(4, 2, 'ARTIFACT', 1, 3, '青铜神树', '眼前这棵青铜神树，残高近四米，九根树枝上栖息着神鸟，一条龙沿着树干向下游动。它让人联想到《山海经》中"十日"栖息的扶桑神树。', 8, '[5.0,1.6,6.0]', '[5.0,1.2,0]'),
(4, 3, 'ARTIFACT', 8, 3, '铜顶尊跪坐人像', '这位跪坐的人头顶一件铜尊，尊是商代中原常见的礼器，它见证了古蜀与中原的交流。', 5, '[7.5,1.6,4.0]', '[7.5,1.2,0]'),
(4, 4, 'ARTIFACT', 13, 3, '青铜扭头跪坐人像', '这位扭着头、跪坐着的小铜人，姿态生动，仿佛正在参加一场神秘的仪式。', 5, '[10.0,1.6,5.0]', '[10.0,1.2,0]'),
(4, 5, 'HALL', NULL, 4, '文物保护与修复馆', '欢迎来到「文物保护与修复馆」，以开放式实验室的形式向观众展示文物清理、拼对、修复与保护工作。', 10, '[12.5,1.6,6.0]', '[12.5,1.2,0]'),
(5, 1, 'ARTIFACT', 1, 3, '青铜神树', '眼前这棵青铜神树，残高近四米，九根树枝上栖息着神鸟，一条龙沿着树干向下游动。它让人联想到《山海经》中"十日"栖息的扶桑神树。', 8, '[2.5,1.6,5.0]', '[2.5,1.2,0]'),
(5, 2, 'ARTIFACT', 2, 3, '青铜纵目面具', '这件纵目面具宽一米三有余，一双眼睛像柱子一样凸出来。《华阳国志》说古蜀王蚕丛"其目纵"，它会不会就是蚕丛的形象呢？', 8, '[5.0,1.6,6.0]', '[5.0,1.2,0]'),
(5, 3, 'ARTIFACT', 3, 3, '青铜大立人像', '这尊青铜大立人像连座高约2.6米，双手巨大，环握在胸前。他手里原本握着什么？是象牙、玉琮还是别的法器？至今仍是一个谜。', 8, '[7.5,1.6,4.0]', '[7.5,1.2,0]'),
(5, 4, 'ARTIFACT', 6, 3, '黄金面具（五号坑）', '这半张金面具出土时揉皱在坑中，经过清理展开后重现光彩，它是三星堆新一轮考古发掘最受关注的发现之一。', 5, '[10.0,1.6,5.0]', '[10.0,1.2,0]');

-- visitor
INSERT INTO visitor (user_id, real_name, age_group, region, visitor_type, interests, visit_count, points) VALUES
(3, '演示游客', '青年', '四川成都', 'STUDENT', '青铜器,面具,3D模型', 2, 120),
(4, '张三', '中年', '北京', 'INDIVIDUAL', '金器,历史', 1, 40),
(5, '李四', '青年', '上海', 'RESEARCHER', '知识图谱,祭祀坑', 3, 200);

-- reservation
INSERT INTO reservation (reservation_no, user_id, museum_id, visit_date, time_slot, visitor_count, contact_name, contact_phone, route_id, need_guide, status, check_in_time) VALUES
('R202600000001', 3, 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '09:00-11:00', 2, '演示游客', '13800000003', 1, 0, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('R202600000002', 4, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '13:00-15:00', 3, '张三', '13800000004', 4, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('R202600000003', 3, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00-11:00', 1, '演示游客', '13800000003', 2, 0, 'CONFIRMED', NULL),
('R202600000004', 5, 1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:00-13:00', 4, '李四', '13800000005', 1, 1, 'CONFIRMED', NULL),
('R202600000005', 4, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00-17:00', 2, '张三', '13800000004', NULL, 0, 'CANCELLED', NULL);

-- review
INSERT INTO review (user_id, target_type, target_id, rating, content, like_count, status, reply) VALUES
(3, 'ARTIFACT', 1, 5, '青铜神树太震撼了，3D模型可以360度旋转看细节，比在展柜外看得还清楚！', 12, 1, '感谢您的喜爱！'),
(4, 'ARTIFACT', 2, 5, '纵目面具的讲解很有意思，原来和蚕丛的传说有关。', 8, 1, NULL),
(5, 'ARTIFACT', 6, 4, '黄金面具很精美，希望能补充更多发掘过程的介绍。', 3, 1, NULL),
(3, 'ROUTE', 1, 5, '经典路线安排合理，一个半小时刚好看完重点。', 6, 1, NULL),
(4, 'ROUTE', 4, 4, '孩子很喜欢修复馆，讲解词通俗易懂。', 2, 1, NULL),
(5, 'QA', 0, 5, '智能问答能给出关联文物和出处，对做研究很有帮助。', 4, 1, NULL),
(3, 'MUSEUM', 1, 5, '新馆环境很好，数字展厅加分。', 5, 1, NULL);

-- favorite
INSERT INTO favorite (user_id, target_type, target_id) VALUES
(3, 'ARTIFACT', 1),
(3, 'ARTIFACT', 2),
(3, 'ARTIFACT', 6),
(4, 'ARTIFACT', 5),
(5, 'ARTIFACT', 1),
(5, 'ARTIFACT', 7),
(3, 'ROUTE', 1);

UPDATE artifact a SET favorite_count = (SELECT COUNT(*) FROM favorite f WHERE f.target_type = 'ARTIFACT' AND f.target_id = a.id AND f.deleted = 0);

-- exhibition_event
INSERT INTO exhibition_event (museum_id, hall_id, title, event_type, start_time, end_time, location, organizer, capacity, content, status) VALUES
(1, 5, '三星堆数字孪生展厅线上开放', 'ONLINE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 335 DAY), '线上', '本系统', NULL, '基于3D高斯泼溅技术重建的文物模型与展厅场景在线开放浏览，支持智能问答导览。', 1),
(1, 3, '"天地人神"专题导览', 'LECTURE', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), '天地人神展厅', '三星堆博物馆（演示）', 40, '由讲解员带领观众深入解读青铜神树、青铜大立人像等文物。', 0),
(1, 4, '小小修复师研学活动', 'WORKSHOP', DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), '文物保护与修复馆', '三星堆博物馆（演示）', 30, '面向中小学生的文物修复体验活动（演示数据）。', 0),
(1, NULL, '古蜀文明与三星堆考古讲座', 'LECTURE', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), '学术报告厅', '三星堆博物馆（演示）', 200, '介绍三星堆考古的最新进展（演示数据）。', 2);

-- announcement
INSERT INTO announcement (title, content, ann_type, is_top, publisher_id, publish_time, view_count, status) VALUES
('数字孪生导览系统上线公告', '三星堆文物数字孪生与智能导览系统正式上线，欢迎体验3D文物浏览与GraphRAG智能问答。', 'NEWS', 1, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 1280, 1),
('参观预约须知', '请提前在系统中预约参观时段，每个时段名额有限，入馆时出示预约单号即可核销。', 'NOTICE', 0, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), 860, 1),
('部分3D模型更新说明', '青铜太阳形器3DGS模型正在训练中，完成后将上线数字展厅。', 'NOTICE', 0, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), 120, 1);

-- system_config
INSERT INTO system_config (config_key, config_value, config_group, value_type, description) VALUES
('llm.base_url', 'https://api.deepseek.com', 'LLM', 'STRING', '大模型API地址（OpenAI兼容，留空则使用application.yml配置）'),
('llm.api_key', '', 'LLM', 'SECRET', '大模型API Key（留空则使用application.yml配置；均为空时启用离线兜底回答）'),
('llm.model', 'deepseek-chat', 'LLM', 'STRING', '模型名称'),
('llm.temperature', '0.3', 'LLM', 'NUMBER', '采样温度'),
('llm.max_tokens', '1024', 'LLM', 'NUMBER', '最大生成Token数'),
('rag.max_hops', '2', 'RAG', 'NUMBER', '子图检索最大跳数（1或2）'),
('rag.max_triples', '40', 'RAG', 'NUMBER', '注入上下文的最大三元组数量'),
('rag.max_literature', '3', 'RAG', 'NUMBER', '注入上下文的最大文献片段数量'),
('reservation.slot_capacity', '300', 'RESERVATION', 'NUMBER', '每个入馆时段的最大预约人数'),
('reservation.time_slots', '09:00-11:00,11:00-13:00,13:00-15:00,15:00-17:00', 'RESERVATION', 'STRING', '可预约时段'),
('system.name', '三星堆文物数字孪生与智能导览系统', 'SYSTEM', 'STRING', '系统名称');

-- qa_session
INSERT INTO qa_session (id, user_id, title, message_count, last_active_time, created_at) VALUES
(1, 3, '青铜神树有什么含义？', 2, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 5, '纵目面具和蚕丛有什么关系？', 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, NULL, '五号坑出土了什么？', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- qa_message
INSERT INTO qa_message (session_id, role, content, retrieved_context, cited_entities, model_name, latency_ms, is_fallback, created_at) VALUES
(1, 'user', '青铜神树有什么含义？', NULL, NULL, NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 'assistant', '青铜神树出土于二号祭祀坑，残高约396厘米，九枝立鸟、一龙沿干而下。不少学者认为它与《山海经》中"十日"栖息的扶桑神树有关，体现了古蜀人的太阳崇拜与宇宙观，但学界仍有不同看法。', '[{"triple":"青铜神树-出土于-二号祭祀坑"},{"triple":"青铜神树-体现-太阳崇拜"}]', '["青铜神树","二号祭祀坑","太阳崇拜","神树信仰"]', 'deepseek-chat', 1850, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'user', '纵目面具和蚕丛有什么关系？', NULL, NULL, NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'assistant', '《华阳国志·蜀志》记载"蜀侯蚕丛，其目纵，始称王"。有学者据此推测，青铜纵目面具柱状凸出的双眼可能与"纵目"的蚕丛形象有关，但这只是学术推测之一。', '[{"triple":"青铜纵目面具-可能关联-蚕丛"},{"triple":"《华阳国志》-记载-蚕丛"}]', '["青铜纵目面具","蚕丛","《华阳国志·蜀志》"]', 'deepseek-chat', 2100, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 'user', '五号坑出土了什么？', NULL, NULL, NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '【离线模式】根据知识库检索：五号祭祀坑面积较小，出土半张黄金面具及金箔饰件、象牙雕刻残件等。', '[{"triple":"黄金面具（五号坑）-出土于-五号祭祀坑"}]', '["五号祭祀坑","黄金面具（五号坑）"]', NULL, 35, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- visit_statistics：生成最近30天的模拟统计数据（周末客流更高）
INSERT INTO visit_statistics (stat_date, offline_visitors, online_visits, model_views, qa_count, reservation_count)
SELECT DATE_SUB(CURDATE(), INTERVAL n DAY),
       6000 + MOD(n * 733, 2500) + IF(DAYOFWEEK(DATE_SUB(CURDATE(), INTERVAL n DAY)) IN (1, 7), 4000, 0),
       12000 + MOD(n * 1291, 6000) + (30 - n) * 120,
       3000 + MOD(n * 577, 1800) + (30 - n) * 60,
       200 + MOD(n * 97, 260) + (30 - n) * 8,
       900 + MOD(n * 211, 700)
FROM (SELECT a.d * 10 + b.d AS n
      FROM (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2) a
      CROSS JOIN (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b) nums
WHERE n BETWEEN 1 AND 30;
