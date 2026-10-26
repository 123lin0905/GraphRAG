# 基于3D高斯泼溅与GraphRAG的三星堆文物数字孪生与智能导览系统

> 第一阶段：数据库设计 + Java 后端服务
> 后续阶段：Vue3 + Element Plus + ECharts 前端、3DGS Web 查看器（本仓库暂不包含）

## 一、项目简介

本系统以三星堆遗址出土文物为对象，围绕“**数字孪生 + 知识图谱 + 大模型**”构建一套面向博物馆观众与管理人员的智能导览平台：

- **数字孪生**：管理“博物馆 → 展厅 → 展柜 → 文物”的空间层级，记录每件文物的 3D 高斯泼溅（3D Gaussian Splatting，3DGS）模型文件（`.ply / .splat / .ksplat / .spz`）及其训练指标（高斯数量、PSNR、SSIM、LPIPS、训练迭代次数等），为前端 3DGS 查看器提供数据。
- **知识图谱**：以“文物 — 祭祀坑 — 材质 — 年代 — 文化 — 主题 — 人物 — 文献”为核心构建领域图谱，提供全图、n 跳邻居、最短路径等查询接口，返回结构可直接用于 ECharts 关系图。
- **GraphRAG 智能问答**：对观众问题做实体链接，检索 1~2 跳知识子图、文物资料与文献片段作为上下文，调用 OpenAI 兼容的大模型（默认 DeepSeek）生成回答，并支持 SSE 流式输出；**未配置 API Key 时自动降级为基于检索结果的离线回答，保证演示可用**。
- **观众服务与运营**：参观预约（时段余量控制、核销）、评价、收藏、导览路线（含 3D 漫游相机位）、展览活动、公告，以及数据大屏统计接口。

## 二、技术栈

| 层次 | 技术 |
| --- | --- |
| 语言/框架 | Java 17、Spring Boot 3.2.5、Maven |
| 持久层 | MyBatis-Plus 3.5.7（分页插件、逻辑删除、审计字段自动填充）、MySQL 8（兼容 MariaDB 10.6+） |
| 安全 | Spring Security 6 + JWT（jjwt 0.12，HS256）、BCrypt 密码加密、RBAC 角色权限、方法级权限 `@PreAuthorize` |
| 接口文档 | Knife4j 4.5（OpenAPI 3 / springdoc） |
| 工具 | Lombok、Jakarta Validation、Jackson（统一日期格式）、Spring AOP（操作日志） |
| 大模型 | OpenAI Chat Completions 兼容协议（DeepSeek / 通义千问兼容模式 / Moonshot / OpenAI 等），JDK HttpClient 实现普通与流式调用 |
| 通用设计 | 统一响应 `Result<T>`、分页 `PageResult<T>`、全局异常处理、CORS、操作日志切面（敏感字段脱敏） |

## 三、系统架构

```
┌──────────────────────────────────────────────────────────────────────┐
│ 前端（后续阶段）：Vue3 + Element Plus + ECharts │ 3DGS Web Viewer      │
└──────────────────────────────┬───────────────────────────────────────┘
                               │ HTTP/JSON  +  SSE（流式问答）  + JWT
┌──────────────────────────────▼───────────────────────────────────────┐
│ Spring Security 过滤链：CORS → JwtAuthenticationFilter → 授权规则      │
├──────────────────────────────────────────────────────────────────────┤
│ Controller 层：Auth / Artifact / KG / QA / Dashboard / Reservation /   │
│                Review / Favorite / Route / Config / 通用CRUD(BaseCrud) │
├──────────────────────────────────────────────────────────────────────┤
│ Service 层：业务服务 + KnowledgeGraphService（图谱内存快照）            │
│             QaService ──► GraphRagRetriever ──► LlmClient（OpenAI兼容）│
├──────────────────────────────────────────────────────────────────────┤
│ Mapper 层：MyBatis-Plus BaseMapper + XML 多表查询 + 统计聚合 SQL        │
└──────────────────────────────┬───────────────────────────────────────┘
                               ▼
                   MySQL 8（33 张表，utf8mb4）
```

### GraphRAG 问答流程

```
观众问题
  │ ① 实体链接：kg_entity 名称/别名最长匹配（被更长命中词覆盖的短词丢弃）
  │            未命中或命中过少时，用字符二元组相似度做模糊补充
  ▼
种子实体 ──② 子图扩展：内存快照上多源 BFS，1~2 跳，按关系权重优先，限制节点数
  ▼
局部子图 ──③ 三元组排序：与种子直连的边优先 → 文本化“头 -关系-> 尾”
  │      ④ 文本补充：相关文物描述/价值（artifact）、文献命中片段（literature）
  ▼
上下文 + 最近3轮历史 + 系统提示词 ──► 大模型（DeepSeek，默认 deepseek-chat）
  │                                     └ 未配置 Key / 调用失败 → 离线兜底回答
  ▼
保存 qa_message（检索上下文 JSON、引用实体、模型、Token、耗时、是否兜底）
返回：回答 + 引用实体 + 三元组 + 参考文献 + 子图（可直接用 ECharts 渲染）
```

## 四、目录结构

```
sanxingdui/
├── README.md
├── sql/
│   ├── schema.sql               # 建库建表脚本（33 张表，含中文注释、外键、索引）
│   ├── data.sql                 # 演示数据（文物、祭祀坑、知识图谱、路线、用户等）
│   └── tools/gen_data.py        # 演示数据生成脚本（维护图谱实体/关系的主键映射）
└── backend/
    ├── pom.xml
    ├── scripts/smoke_test.sh    # 接口冒烟测试脚本（67 项）
    └── src/main/
        ├── java/com/sanxingdui/twin/
        │   ├── SanxingduiTwinApplication.java
        │   ├── annotation/      # @OperationLog 操作日志注解
        │   ├── aspect/          # 操作日志切面
        │   ├── common/          # Result、PageResult、PageQuery、BaseEntity、异常处理
        │   ├── config/          # MyBatis-Plus、OpenAPI、Jackson、线程池、JWT/LLM/RAG 配置属性
        │   ├── security/        # SecurityConfig、JWT 过滤器、JwtUtil、LoginUser、UserDetailsService
        │   ├── controller/      # REST 接口（BaseCrudController 通用增删改查 + 业务控制器）
        │   ├── service/(impl)/  # 业务服务
        │   ├── rag/             # GraphRAG：KgSnapshot、GraphRagRetriever、LlmClient、LlmSettings
        │   ├── mapper/          # MyBatis-Plus Mapper（含 DashboardMapper 聚合统计）
        │   ├── entity/          # 33 个实体（与表一一对应）
        │   ├── dto/  vo/        # 请求参数 / 视图对象
        │   └── util/            # IP 工具
        └── resources/
            ├── application.yml  # 主配置（数据库、JWT、LLM、RAG、Knife4j）
            ├── application-dev.yml / application-prod.yml
            └── mapper/ArtifactMapper.xml
```

## 五、数据库设计（33 张表）

所有表均包含 `id`（自增主键）、`created_at`、`updated_at`、`deleted`（逻辑删除），表与字段均有中文注释，主要关联使用外键约束。

| 模块 | 表 |
| --- | --- |
| 系统权限（6） | `sys_user` 用户、`sys_role` 角色、`sys_permission` 权限、`sys_user_role`、`sys_role_permission`、`sys_operation_log` 操作日志 |
| 场馆空间（3） | `museum` 博物馆、`exhibition_hall` 展厅、`showcase` 展柜（含坐标与温湿度） |
| 文物本体（8） | `artifact_category` 分类（两级）、`dynasty_period` 年代/文化期、`excavation_pit` 祭祀坑、`material` 材质、`artifact` 文物、`artifact_image` 图片、`model_3d` 3DGS 模型、`restoration_record` 修复记录 |
| 知识图谱与文献（4） | `kg_entity` 实体、`kg_relation` 关系（有向三元组）、`literature` 文献、`artifact_literature` 文物-文献关联 |
| 智能问答（2） | `qa_session` 会话、`qa_message` 消息（检索上下文、引用实体、Token、耗时、兜底标记、反馈） |
| 智能导览（2） | `guide_route` 路线、`guide_route_node` 路线节点（讲解词、3D 相机位） |
| 观众服务（4） | `visitor` 游客档案、`reservation` 预约、`review` 评价、`favorite` 收藏 |
| 运营内容（4） | `exhibition_event` 展览活动、`announcement` 公告、`system_config` 系统参数（含大模型配置）、`visit_statistics` 每日访问统计 |

演示数据规模：文物 18 件、祭祀坑 8 个、知识图谱实体 66 个 / 关系 152 条、文献 8 篇、3DGS 模型 9 个、导览路线 5 条（25 个节点）、用户 5 个、近 30 天访问统计。

> **数据说明**：文物史实部分仅采用公开资料中较为确定的信息，尺寸、重量等均为约数，学术上有争议的观点以“有学者认为”表述；图片与 3DGS 模型 URL 为占位路径，模型训练指标、客流与问答等运营数据为模拟数据，需在后续阶段替换为真实数据。答辩前建议对照三星堆博物馆官方资料再核对一遍文物描述。

## 六、本地运行

### 1. 环境要求

- JDK 17+、Maven 3.8+
- MySQL 8.0+（或 MariaDB 10.6+）

### 2. 导入数据库

```bash
mysql -uroot -p < sql/schema.sql     # 创建数据库 sanxingdui_twin 及 33 张表
mysql -uroot -p --default-character-set=utf8mb4 < sql/data.sql   # 导入演示数据
```

> 演示数据中的日期（预约、问答、客流趋势）基于 `CURDATE()/NOW()` 相对生成，请确保 MySQL 时区为东八区：
> `SET GLOBAL time_zone = '+08:00';`（或在 my.cnf 中配置 `default-time-zone='+08:00'`）。

### 3. 修改配置

编辑 `backend/src/main/resources/application.yml`（也可以用环境变量覆盖）：

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root123}   # ← 改成你的 MySQL 密码

llm:
  base-url: ${LLM_BASE_URL:https://api.deepseek.com}
  api-key:  ${LLM_API_KEY:}            # ← 填入 DeepSeek API Key；留空则问答使用离线兜底模式
  model:    ${LLM_MODEL:deepseek-chat}
```

大模型配置也可以在系统运行后由管理员通过接口 `PUT /api/configs` 修改（存于 `system_config` 表，**数据库配置优先于 yml**），修改后立即生效，无需重启。其他 OpenAI 兼容服务示例：

| 服务 | base-url | model |
| --- | --- | --- |
| DeepSeek | `https://api.deepseek.com` | `deepseek-chat` |
| 通义千问（兼容模式） | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `qwen-plus` |
| OpenAI | `https://api.openai.com/v1` | `gpt-4o-mini` |

### 4. 启动

```bash
cd backend
mvn spring-boot:run
# 或打包后运行
mvn clean package
java -jar target/sanxingdui-twin.jar
# 用环境变量传参示例
DB_PASSWORD=123456 LLM_API_KEY=sk-xxx java -jar target/sanxingdui-twin.jar
```

启动成功后访问：

- **接口文档（Knife4j）**：http://localhost:8080/doc.html
- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs/default

### 5. 冒烟测试

```bash
bash backend/scripts/smoke_test.sh http://localhost:8080
```

## 七、默认账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 全部权限（用户、角色、系统配置、操作日志） |
| 馆员 | `curator` | `curator123` | 维护文物、模型、图谱、路线、预约、评价 |
| 游客 | `visitor` | `visitor123` | 浏览、问答、预约、评价、收藏 |
| 游客 | `zhangsan` / `lisi` | `visitor123` | 演示数据用户 |

密码均以 BCrypt 加密存储。登录后在请求头携带 `Authorization: Bearer <token>`（Knife4j 中点击“Authorize”填写）。

## 八、接口概览

统一响应格式：

```json
{ "code": 200, "message": "操作成功", "data": { }, "timestamp": 1730000000000 }
```

`code`：200 成功、400 参数错误、401 未登录、403 无权限、404 不存在、409 数据冲突、500 服务器错误。

| 模块 | 主要接口 |
| --- | --- |
| 认证 | `POST /api/auth/login`、`POST /api/auth/register`、`GET /api/auth/me`、`PUT /api/auth/password` |
| 文物 | `GET /api/artifacts/search`（多条件检索：关键词/分类含子类/祭祀坑/材质/年代/展厅/镇馆之宝，`orderBy=hot`）、`GET /api/artifacts/{id}/detail`（图片+3DGS模型+图谱一跳邻居+文献+修复记录+相关文物+评分+收藏状态）、`GET /api/artifacts/hot`，以及通用 `page/list/{id}/POST/PUT/DELETE` |
| 通用 CRUD | `/api/categories`、`/api/periods`、`/api/pits`、`/api/materials`、`/api/museums`、`/api/halls`、`/api/showcases`、`/api/images`、`/api/models`、`/api/literature`、`/api/kg/entities`、`/api/kg/relations`、`/api/restorations`、`/api/events`、`/api/announcements`、`/api/route-nodes`、`/api/visitors`、`/api/roles`、`/api/permissions`、`/api/logs`、`/api/visit-statistics`（均支持 `/page?current=&size=&keyword=` 及字段过滤） |
| 知识图谱 | `GET /api/kg/graph?types=&limit=`（ECharts nodes/links/categories）、`GET /api/kg/neighbors/{id}?hops=1~3`、`GET /api/kg/path?fromId=&toId=`、`GET /api/kg/search?keyword=`、`GET /api/kg/statistics`、`POST /api/kg/refresh` |
| 智能问答 | `POST /api/qa/ask`、`POST /api/qa/stream`（SSE）、`GET /api/qa/stream?question=`（EventSource）、`GET /api/qa/retrieve`（仅检索调试）、`GET /api/qa/sessions`、`GET /api/qa/sessions/{id}/messages`、`POST /api/qa/messages/{id}/feedback`、`GET /api/qa/llm-status`、`GET /api/qa/admin/messages` |
| 数据大屏 | `GET /api/dashboard/overview`、`/artifact-distribution?dimension=topCategory|category|pit|material|period|hall`、`/artifact-distribution/all`、`/visit-trend?days=30`、`/qa?days=14`、`/models`、`/services` |
| 预约 | `GET /api/reservations/slots?date=`、`POST /api/reservations`、`GET /api/reservations/my`、`PUT /api/reservations/{id}/cancel`、管理端 `GET /page`、`PUT /{id}/status`、`POST /check-in` |
| 评价 | `POST /api/reviews`、`GET /api/reviews/target`、`GET /api/reviews/summary`、`GET /api/reviews/my`、`POST /{id}/like`、管理端 `GET /admin/page`、`PUT /{id}/audit` |
| 收藏 | `POST /api/favorites/toggle`、`GET /api/favorites/check`、`GET /api/favorites/artifacts` |
| 导览路线 | `GET /api/routes/{id}/detail`、`GET /api/routes/recommend?interests=&minutes=`、`POST /api/routes/{id}/start` + 通用 CRUD |
| 系统管理 | `GET /api/users/page`、`PUT /api/users/{id}/status`、`PUT /api/users/{id}/roles`、`GET/PUT /api/configs`、`POST /api/configs/llm/test` |

### 权限规则

- 匿名可访问：登录/注册、问答、各类展示数据的 GET 接口（文物、图谱、路线、评价列表、大屏等）。
- 登录用户：预约、评价、收藏、个人问答会话。
- 馆员/管理员：业务数据的增删改、预约核销、评价审核。
- 仅管理员：用户、角色权限、系统配置、操作日志。

### SSE 流式问答前端示例

```js
// POST + fetch 读取流（推荐，可携带 Authorization 头）
const resp = await fetch('/api/qa/stream', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
  body: JSON.stringify({ sessionId, question })
})
// 事件顺序：meta（sessionId/引用实体/三元组/子图）→ delta（{content}，多次）→ done（messageId/耗时）| error

// 或 EventSource（仅 GET，登录用户用 token 查询参数）
const es = new EventSource(`/api/qa/stream?question=${encodeURIComponent(q)}&token=${token}`)
es.addEventListener('delta', e => answer += JSON.parse(e.data).content)
es.addEventListener('done', () => es.close())
```

## 九、设计要点（可写入论文）

1. **通用 CRUD 基类**：`BaseCrudController` 以泛型封装分页/列表/详情/增删改，子类只需声明关键词列与过滤列白名单，避免 SQL 注入，同时减少重复代码。
2. **知识图谱内存快照**：`KgSnapshot` 将实体与关系加载为无向邻接表，BFS 子图扩展无需逐跳查询数据库；快照 60 秒自动过期，也可调用 `/api/kg/refresh` 立即刷新。
3. **实体链接策略**：名称/别名最长匹配优先，丢弃被更长命中词覆盖的短词（如“青铜神树”命中时不再单独链接“青铜”）；无命中时用字符二元组打分兜底。
4. **大模型可插拔与降级**：配置“数据库优先、yml 兜底”，管理员可在线切换模型服务；未配置 Key 或调用失败时自动生成离线回答，并在消息表中记录 `is_fallback`，便于统计对比。
5. **可追溯的问答记录**：每条回答保存检索到的三元组、文物与文献片段（JSON）、引用实体、Token 用量与耗时，可用于问答质量评估。
6. **安全**：BCrypt 密码、无状态 JWT、每次请求重新加载用户（禁用/改角色即时生效）、RBAC + 方法级权限、操作日志对密码/API Key 脱敏、系统配置接口返回脱敏 API Key。

## 十、后续阶段计划

- 前端：Vue3 + Element Plus 管理后台与观众端、ECharts 数据大屏与知识图谱可视化。
- 3DGS：基于 COLMAP + gaussian-splatting / gsplat 训练文物模型，Web 端使用 splat 查看器加载 `model_3d.model_url`，按 `camera_position / camera_target` 初始化视角，路线节点驱动 3D 漫游。
- GraphRAG 增强：引入向量检索（如文献分块 + 向量索引）与图检索混合召回、社区摘要。
