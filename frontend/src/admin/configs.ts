import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, reservationApi } from '@/api'
import { http } from '@/api/http'
import type { CrudConfig } from './types'
import type { Option } from '@/utils/dicts'

const o = (pairs: [any, string][]): Option[] => pairs.map(([value, label]) => ({ value, label }))
const YES_NO = o([[1, '是'], [0, '否']])
const ENABLE = o([[1, '启用'], [0, '停用']])
const ENTITY_TYPES = o([['ARTIFACT', '文物'], ['PIT', '祭祀坑'], ['MATERIAL', '材质'], ['PERIOD', '年代'], ['CULTURE', '文化'], ['THEME', '主题'], ['SITE', '遗址'], ['MUSEUM', '博物馆'], ['PERSON', '人物'], ['HALL', '展厅'], ['LITERATURE', '文献'], ['TECH', '工艺'], ['CATEGORY', '分类']])

export const CRUD_CONFIGS: CrudConfig[] = [
  // ======================= 文物管理 =======================
  {
    key: 'artifacts', title: '文物信息', group: '文物管理', icon: 'Trophy', api: '/artifacts', dialogWidth: '880px',
    desc: '维护文物本体信息：编号、分类、出土坑、材质、年代、陈列位置与讲解词', keywordPlaceholder: '名称 / 别名 / 编号',
    fields: [
      { prop: 'coverUrl', label: '封面', type: 'image', width: 84, span: 2 },
      { prop: 'artifactNo', label: '文物编号', required: true, width: 130 },
      { prop: 'name', label: '文物名称', required: true, minWidth: 150 },
      { prop: 'alias', label: '别名', table: false },
      { prop: 'categoryId', label: '分类', type: 'select', dict: 'categories', filter: true, width: 110 },
      { prop: 'pitId', label: '祭祀坑', type: 'select', dict: 'pits', filter: true, width: 130 },
      { prop: 'materialId', label: '材质', type: 'select', dict: 'materials', filter: true, width: 90 },
      { prop: 'periodId', label: '年代', type: 'select', dict: 'periods', table: false },
      { prop: 'hallId', label: '展厅', type: 'select', dict: 'halls', width: 110 },
      { prop: 'showcaseId', label: '展柜', type: 'select', dict: 'showcases', table: false },
      { prop: 'grade', label: '等级', type: 'select', options: o([['一级', '一级'], ['二级', '二级'], ['三级', '三级'], ['一般', '一般']]), width: 80 },
      { prop: 'isTreasure', label: '镇馆之宝', type: 'switch', width: 96, tags: { 1: 'warning', 0: 'info' }, options: YES_NO },
      { prop: 'status', label: '状态', type: 'select', options: o([[1, '在展'], [0, '库房'], [2, '修复中']]), tags: { 1: 'success', 0: 'info', 2: 'warning' }, width: 90, default: 1 },
      { prop: 'dimensions', label: '尺寸描述', table: false },
      { prop: 'heightCm', label: '高度(cm)', type: 'number', table: false, precision: 2 },
      { prop: 'excavationYear', label: '出土年份', type: 'number', table: false },
      { prop: 'viewCount', label: '浏览', type: 'number', width: 80, form: false },
      { prop: 'description', label: '文物描述', type: 'textarea', table: false, span: 2 },
      { prop: 'culturalValue', label: '文化价值', type: 'textarea', table: false, span: 2 },
      { prop: 'guideText', label: '讲解词', type: 'textarea', table: false, span: 2 }
    ]
  },
  {
    key: 'categories', title: '文物分类', group: '文物管理', icon: 'Files', api: '/categories', desc: '两级分类体系',
    fields: [
      { prop: 'name', label: '分类名称', required: true },
      { prop: 'code', label: '分类编码', required: true },
      { prop: 'parentId', label: '父分类', type: 'select', dict: 'topCategories', formatter: (r) => (r.parentId ? '' : '顶级'), default: 0 },
      { prop: 'sort', label: '排序', type: 'number', width: 80 },
      { prop: 'description', label: '说明', type: 'textarea', span: 2, ellipsis: true }
    ]
  },
  {
    key: 'pits', title: '祭祀坑', group: '文物管理', icon: 'MapLocation', api: '/pits', desc: '三星堆祭祀区 8 个祭祀坑',
    fields: [
      { prop: 'pitCode', label: '坑号', required: true, width: 80 },
      { prop: 'name', label: '名称', required: true },
      { prop: 'discoveryYear', label: '发现年份', type: 'number', width: 100 },
      { prop: 'excavationPeriod', label: '发掘时间', width: 160 },
      { prop: 'areaSqm', label: '面积(㎡)', type: 'number', width: 100, precision: 2 },
      { prop: 'lengthM', label: '长(m)', type: 'number', table: false, precision: 2 },
      { prop: 'widthM', label: '宽(m)', type: 'number', table: false, precision: 2 },
      { prop: 'periodId', label: '年代', type: 'select', dict: 'periods', table: false },
      { prop: 'description', label: '概况', type: 'textarea', span: 2, ellipsis: true },
      { prop: 'mainFindings', label: '主要出土器物', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'materials', title: '材质', group: '文物管理', icon: 'Coin', api: '/materials',
    fields: [
      { prop: 'name', label: '材质名称', required: true },
      { prop: 'category', label: '大类', type: 'select', filter: true, options: o([['金属', '金属'], ['玉石', '玉石'], ['有机质', '有机质'], ['陶土', '陶土']]) },
      { prop: 'description', label: '说明', type: 'textarea', span: 2, ellipsis: true },
      { prop: 'conservationNote', label: '保护要点', type: 'textarea', span: 2, ellipsis: true }
    ]
  },
  {
    key: 'periods', title: '年代 / 文化期', group: '文物管理', icon: 'Clock', api: '/periods',
    fields: [
      { prop: 'name', label: '名称', required: true, minWidth: 180 },
      { prop: 'cultureName', label: '考古学文化' },
      { prop: 'startYear', label: '起始年', type: 'number', width: 90 },
      { prop: 'endYear', label: '结束年', type: 'number', width: 90 },
      { prop: 'eraDesc', label: '年代描述' },
      { prop: 'sort', label: '排序', type: 'number', width: 70 },
      { prop: 'description', label: '介绍', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'images', title: '文物图片', group: '文物管理', icon: 'Picture', api: '/images',
    fields: [
      { prop: 'imageUrl', label: '图片', type: 'image', width: 90, required: true, span: 2 },
      { prop: 'artifactId', label: '所属文物', type: 'select', dict: 'artifacts', filter: true, required: true },
      { prop: 'title', label: '标题' },
      { prop: 'imageType', label: '类型', type: 'select', filter: true, options: o([['PHOTO', '实拍'], ['DETAIL', '细节'], ['LINE', '线图'], ['TRAIN', '3DGS训练视图'], ['RENDER', '三维扫描渲染图']]), tags: { PHOTO: 'primary', DETAIL: 'success', LINE: 'info', TRAIN: 'warning', RENDER: 'danger' } },
      { prop: 'isCover', label: '封面', type: 'switch', options: YES_NO, tags: { 1: 'warning', 0: 'info' }, width: 80 },
      { prop: 'widthPx', label: '宽(px)', type: 'number', table: false },
      { prop: 'heightPx', label: '高(px)', type: 'number', table: false },
      { prop: 'sort', label: '排序', type: 'number', width: 70 }
    ]
  },
  {
    key: 'models', title: '3D 模型', group: '文物管理', icon: 'Box', api: '/models', dialogWidth: '880px',
    desc: '真实三维扫描模型（GLB，第三方 CC BY 4.0）与 3D 高斯泼溅模型（训练指标：高斯数、PSNR/SSIM/LPIPS）',
    fields: [
      { prop: 'name', label: '模型名称', required: true, minWidth: 180 },
      { prop: 'artifactId', label: '关联文物', type: 'select', dict: 'artifacts', filter: true },
      { prop: 'hallId', label: '关联展厅', type: 'select', dict: 'halls', table: false },
      { prop: 'modelType', label: '类型', type: 'select', options: o([['OBJECT', '单体文物'], ['SCENE', '展厅场景']]), filter: true, width: 96, default: 'OBJECT' },
      { prop: 'format', label: '格式', type: 'select', options: o([['glb', 'glb'], ['gltf', 'gltf'], ['splat', 'splat'], ['ksplat', 'ksplat'], ['ply', 'ply'], ['spz', 'spz']]), filter: true, width: 80, tags: { glb: 'danger', gltf: 'danger', splat: 'primary', ksplat: 'success', ply: 'warning', spz: 'info' } },
      { prop: 'modelUrl', label: '模型URL', required: true, span: 2, minWidth: 200, ellipsis: true },
      { prop: 'gaussianCount', label: '高斯数', type: 'number', width: 100 },
      { prop: 'psnr', label: 'PSNR', type: 'number', width: 80, precision: 3 },
      { prop: 'ssim', label: 'SSIM', type: 'number', width: 80, precision: 4 },
      { prop: 'lpips', label: 'LPIPS', type: 'number', table: false, precision: 4 },
      { prop: 'fileSize', label: '文件大小(B)', type: 'number', table: false },
      { prop: 'shDegree', label: 'SH阶数', type: 'number', table: false, min: 0, max: 3 },
      { prop: 'imageCount', label: '训练视图数', type: 'number', table: false },
      { prop: 'trainIterations', label: '训练迭代', type: 'number', table: false },
      { prop: 'trainMinutes', label: '训练耗时(分)', type: 'number', table: false, precision: 1 },
      { prop: 'captureDevice', label: '采集设备', table: false },
      { prop: 'sfmTool', label: '位姿估计工具', table: false },
      { prop: 'trainedBy', label: '训练框架', table: false },
      { prop: 'cameraPosition', label: '相机位置[x,y,z]', table: false },
      { prop: 'cameraTarget', label: '相机观察点[x,y,z]', table: false },
      { prop: 'author', label: '模型作者', table: false },
      { prop: 'license', label: '授权协议', width: 96 },
      { prop: 'sourceUrl', label: '来源链接', span: 2, table: false },
      { prop: 'remark', label: '备注', span: 2, table: false },
      { prop: 'isDefault', label: '默认', type: 'switch', options: YES_NO, width: 70, tags: { 1: 'success', 0: 'info' } },
      { prop: 'status', label: '状态', type: 'select', options: o([[1, '可用'], [0, '训练中'], [2, '已下线']]), tags: { 1: 'success', 0: 'warning', 2: 'info' }, width: 86, filter: true, default: 1 }
    ]
  },
  {
    key: 'restorations', title: '修复记录', group: '文物管理', icon: 'FirstAidKit', api: '/restorations',
    fields: [
      { prop: 'artifactId', label: '文物', type: 'select', dict: 'artifacts', filter: true, required: true },
      { prop: 'restorer', label: '修复人员/团队' },
      { prop: 'startDate', label: '开始日期', type: 'date', width: 110 },
      { prop: 'endDate', label: '结束日期', type: 'date', width: 110 },
      { prop: 'status', label: '状态', type: 'select', filter: true, options: o([['PLANNED', '计划'], ['ONGOING', '进行中'], ['DONE', '已完成']]), tags: { PLANNED: 'info', ONGOING: 'warning', DONE: 'success' }, width: 90 },
      { prop: 'conditionBefore', label: '修复前状况', type: 'textarea', span: 2, ellipsis: true },
      { prop: 'method', label: '修复方法', type: 'textarea', span: 2, table: false },
      { prop: 'conditionAfter', label: '修复后状况', type: 'textarea', span: 2, table: false },
      { prop: 'remark', label: '备注', table: false, span: 2 }
    ]
  },

  // ======================= 场馆空间 =======================
  {
    key: 'halls', title: '展厅', group: '场馆空间', icon: 'OfficeBuilding', api: '/halls',
    fields: [
      { prop: 'hallCode', label: '编号', width: 90, required: true },
      { prop: 'name', label: '展厅名称', required: true },
      { prop: 'museumId', label: '博物馆', type: 'select', dict: 'museums', table: false, default: 1 },
      { prop: 'theme', label: '展陈主题' },
      { prop: 'floor', label: '楼层', type: 'number', width: 70 },
      { prop: 'area', label: '面积(㎡)', type: 'number', width: 100 },
      { prop: 'sort', label: '排序', type: 'number', width: 70, table: false },
      { prop: 'status', label: '状态', type: 'select', options: o([[1, '开放'], [0, '闭馆']]), tags: { 1: 'success', 0: 'info' }, width: 80, filter: true, default: 1 },
      { prop: 'sceneUrl', label: '三维场景URL', table: false, span: 2 },
      { prop: 'description', label: '介绍', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'showcases', title: '展柜', group: '场馆空间', icon: 'Grid', api: '/showcases', desc: '展柜坐标与柜内环境监测',
    fields: [
      { prop: 'caseCode', label: '展柜编号', required: true, width: 110 },
      { prop: 'name', label: '名称', required: true },
      { prop: 'hallId', label: '所属展厅', type: 'select', dict: 'halls', filter: true },
      { prop: 'caseType', label: '类型', type: 'select', options: o([['独立柜', '独立柜'], ['通柜', '通柜'], ['壁柜', '壁柜'], ['中心柜', '中心柜']]), width: 90 },
      { prop: 'posX', label: 'X(m)', type: 'number', width: 70, precision: 2 },
      { prop: 'posY', label: 'Y(m)', type: 'number', width: 70, precision: 2 },
      { prop: 'posZ', label: 'Z(m)', type: 'number', table: false, precision: 2 },
      { prop: 'temperature', label: '温度℃', type: 'number', width: 80, precision: 1 },
      { prop: 'humidity', label: '湿度%', type: 'number', width: 80, precision: 1 }
    ]
  },

  // ======================= 知识图谱 =======================
  {
    key: 'kg-entities', title: '图谱实体', group: '知识图谱', icon: 'Share', api: '/kg/entities', desc: '实体及别名（用于问句实体链接）',
    toolbarActions: [{ label: '刷新图谱缓存', icon: 'Refresh', handler: async () => { await adminApi.refreshKg(); ElMessage.success('图谱快照已刷新') } }],
    fields: [
      { prop: 'name', label: '实体名称', required: true, minWidth: 140 },
      { prop: 'entityType', label: '类型', type: 'select', options: ENTITY_TYPES, filter: true, required: true, width: 90 },
      { prop: 'aliases', label: '别名', ellipsis: true },
      { prop: 'weight', label: '重要度', type: 'number', width: 80, precision: 2 },
      { prop: 'refTable', label: '关联表', width: 120 },
      { prop: 'refId', label: '关联ID', type: 'number', width: 80 },
      { prop: 'description', label: '描述', type: 'textarea', span: 2, ellipsis: true },
      { prop: 'properties', label: '扩展属性(JSON)', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'kg-relations', title: '图谱关系', group: '知识图谱', icon: 'Connection', api: '/kg/relations', desc: '有向三元组（头实体 - 关系 - 尾实体）',
    fields: [
      { prop: 'sourceId', label: '头实体', type: 'select', dict: 'entities', filter: true, required: true },
      { prop: 'relationName', label: '关系名称', required: true, width: 110 },
      { prop: 'targetId', label: '尾实体', type: 'select', dict: 'entities', filter: true, required: true },
      { prop: 'relationType', label: '关系编码', required: true },
      { prop: 'weight', label: '权重', type: 'number', width: 80, precision: 2, default: 1 },
      { prop: 'description', label: '说明/证据', type: 'textarea', span: 2, ellipsis: true }
    ]
  },
  {
    key: 'literature', title: '文献资料', group: '知识图谱', icon: 'Reading', api: '/literature', desc: 'GraphRAG 文本检索片段来源',
    fields: [
      { prop: 'title', label: '标题', required: true, minWidth: 200 },
      { prop: 'authors', label: '作者' },
      { prop: 'source', label: '出处' },
      { prop: 'publishYear', label: '年份', width: 90 },
      { prop: 'litType', label: '类型', type: 'select', filter: true, options: o([['BOOK', '专著'], ['PAPER', '论文'], ['ANCIENT', '古籍'], ['REPORT', '考古报告'], ['GUIDE', '导览文稿']]), width: 96 },
      { prop: 'keywords', label: '关键词', table: false },
      { prop: 'url', label: '原文链接', table: false },
      { prop: 'abstractText', label: '摘要', type: 'textarea', span: 2, table: false },
      { prop: 'content', label: '正文/摘录', type: 'textarea', span: 2, table: false }
    ]
  },

  // ======================= 导览服务 =======================
  {
    key: 'routes', title: '导览路线', group: '导览服务', icon: 'Guide', api: '/routes',
    fields: [
      { prop: 'coverUrl', label: '封面', type: 'image', width: 90, span: 2 },
      { prop: 'name', label: '路线名称', required: true, minWidth: 150 },
      { prop: 'routeType', label: '类型', type: 'select', filter: true, options: o([['CLASSIC', '经典'], ['THEME', '主题'], ['QUICK', '快速'], ['FAMILY', '亲子']]), width: 80 },
      { prop: 'theme', label: '主题' },
      { prop: 'durationMin', label: '时长(分)', type: 'number', width: 90 },
      { prop: 'distanceM', label: '距离(m)', type: 'number', width: 90 },
      { prop: 'difficulty', label: '强度', type: 'select', options: o([[1, '轻松'], [2, '适中'], [3, '较长']]), width: 80 },
      { prop: 'suitableCrowd', label: '适合人群', table: false },
      { prop: 'museumId', label: '博物馆', type: 'select', dict: 'museums', table: false, default: 1 },
      { prop: 'useCount', label: '使用次数', type: 'number', width: 90, form: false },
      { prop: 'status', label: '状态', type: 'select', options: ENABLE, tags: { 1: 'success', 0: 'info' }, width: 80, default: 1 },
      { prop: 'description', label: '介绍', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'route-nodes', title: '路线节点', group: '导览服务', icon: 'Place', api: '/route-nodes', desc: '讲解词与 3D 漫游相机位',
    fields: [
      { prop: 'routeId', label: '所属路线', type: 'select', dict: 'routes', filter: true, required: true },
      { prop: 'seq', label: '顺序', type: 'number', width: 70, required: true },
      { prop: 'title', label: '节点标题', required: true },
      { prop: 'nodeType', label: '类型', type: 'select', options: o([['ARTIFACT', '文物'], ['HALL', '展厅'], ['POI', '服务点']]), width: 80, tags: { ARTIFACT: 'warning', HALL: 'success', POI: 'info' } },
      { prop: 'artifactId', label: '文物', type: 'select', dict: 'artifacts', filter: true },
      { prop: 'hallId', label: '展厅', type: 'select', dict: 'halls', table: false },
      { prop: 'stayMinutes', label: '停留(分)', type: 'number', width: 90 },
      { prop: 'cameraPosition', label: '相机位置', table: false },
      { prop: 'cameraTarget', label: '相机观察点', table: false },
      { prop: 'narration', label: '讲解词', type: 'textarea', span: 2, ellipsis: true }
    ]
  },
  {
    key: 'reservations', title: '预约管理', group: '导览服务', icon: 'Tickets', api: '/reservations', create: false, edit: false, remove: false,
    desc: '预约审核、状态变更与核销入馆', keywordPlaceholder: '单号 / 联系人 / 手机号',
    toolbarActions: [{
      label: '按单号核销', icon: 'Finished', type: 'primary',
      handler: async (ctx) => {
        const { value } = await ElMessageBox.prompt('请输入预约单号', '核销入馆', { inputPlaceholder: 'R2026…', confirmButtonText: '核销' })
        if (!value) return
        await reservationApi.checkIn(value.trim())
        ElMessage.success('核销成功')
        ctx.reload()
      }
    }],
    rowActions: [
      { label: '确认', type: 'success', show: (r) => r.status === 'PENDING', handler: async (r, c) => { await reservationApi.setStatus(r.id, 'CONFIRMED'); ElMessage.success('已确认'); c.reload() } },
      { label: '核销', type: 'primary', show: (r) => r.status === 'CONFIRMED' || r.status === 'PENDING', handler: async (r, c) => { await reservationApi.checkIn(r.reservationNo); ElMessage.success('已核销'); c.reload() } },
      { label: '取消', type: 'danger', confirm: '确定取消该预约？', show: (r) => r.status === 'PENDING' || r.status === 'CONFIRMED', handler: async (r, c) => { await reservationApi.setStatus(r.id, 'CANCELLED'); c.reload() } }
    ],
    fields: [
      { prop: 'reservationNo', label: '预约单号', width: 150 },
      { prop: 'status', label: '状态', type: 'select', filter: true, width: 90, options: o([['PENDING', '待确认'], ['CONFIRMED', '已确认'], ['CANCELLED', '已取消'], ['COMPLETED', '已核销']]), tags: { PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'info', COMPLETED: 'primary' } },
      { prop: 'visitDate', label: '参观日期', type: 'date', width: 110, filter: true },
      { prop: 'timeSlot', label: '时段', width: 110 },
      { prop: 'visitorCount', label: '人数', type: 'number', width: 70 },
      { prop: 'contactName', label: '联系人', width: 90 },
      { prop: 'contactPhone', label: '手机号', width: 120 },
      { prop: 'routeId', label: '意向路线', type: 'select', dict: 'routes' },
      { prop: 'needGuide', label: '讲解', type: 'switch', options: YES_NO, tags: { 1: 'warning', 0: 'info' }, width: 70 },
      { prop: 'checkInTime', label: '核销时间', type: 'datetime', width: 160 },
      { prop: 'remark', label: '备注', table: false }
    ]
  },
  {
    key: 'reviews', title: '评价审核', group: '导览服务', icon: 'ChatLineSquare', api: '/reviews', pageUrl: '/reviews/admin/page', create: false, edit: false,
    desc: '观众评价的审核、屏蔽与馆方回复',
    rowActions: [
      { label: '通过', type: 'success', show: (r) => r.status !== 1, handler: async (r, c) => { await adminApi.auditReview(r.id, 1, r.reply); ElMessage.success('已通过'); c.reload() } },
      { label: '屏蔽', type: 'warning', show: (r) => r.status !== 2, handler: async (r, c) => { await adminApi.auditReview(r.id, 2, r.reply); ElMessage.success('已屏蔽'); c.reload() } },
      {
        label: '回复', type: 'primary', handler: async (r, c) => {
          const { value } = await ElMessageBox.prompt('馆方回复内容', `回复 ${r.nickname || ''}`, { inputValue: r.reply || '', inputType: 'textarea' })
          await adminApi.auditReview(r.id, r.status === 2 ? 2 : 1, value)
          ElMessage.success('已回复')
          c.reload()
        }
      }
    ],
    fields: [
      { prop: 'nickname', label: '用户', width: 100 },
      { prop: 'targetType', label: '对象', type: 'select', filter: true, width: 80, options: o([['ARTIFACT', '文物'], ['ROUTE', '路线'], ['MUSEUM', '博物馆'], ['QA', '问答']]) },
      { prop: 'targetName', label: '对象名称', width: 140 },
      { prop: 'rating', label: '评分', type: 'rate', width: 140 },
      { prop: 'content', label: '内容', minWidth: 220, ellipsis: true },
      { prop: 'reply', label: '馆方回复', ellipsis: true },
      { prop: 'likeCount', label: '赞', type: 'number', width: 60 },
      { prop: 'status', label: '状态', type: 'select', filter: true, width: 90, options: o([[0, '待审核'], [1, '已通过'], [2, '已屏蔽']]), tags: { 0: 'warning', 1: 'success', 2: 'danger' } },
      { prop: 'createdAt', label: '时间', type: 'datetime', width: 160 }
    ]
  },
  {
    key: 'qa-records', title: '问答记录', group: '导览服务', icon: 'ChatDotRound', api: '/qa', pageUrl: '/qa/admin/messages', create: false, edit: false, remove: false,
    desc: 'GraphRAG 问答消息：检索上下文、引用实体、模型、Token、耗时与兜底标记',
    fields: [
      { prop: 'sessionId', label: '会话', type: 'number', width: 70 },
      { prop: 'role', label: '角色', type: 'select', filter: true, width: 80, options: o([['user', '用户'], ['assistant', '助手']]), tags: { user: 'primary', assistant: 'success' } },
      { prop: 'content', label: '内容', minWidth: 300, ellipsis: true, type: 'textarea' },
      { prop: 'citedEntities', label: '引用实体', ellipsis: true, width: 180, formatter: (r) => { try { return (JSON.parse(r.citedEntities || '[]') as string[]).join('、') } catch { return r.citedEntities || '' } } },
      { prop: 'modelName', label: '模型', width: 120 },
      { prop: 'latencyMs', label: '耗时ms', type: 'number', width: 80 },
      { prop: 'totalTokens', label: 'Tokens', type: 'number', width: 80 },
      { prop: 'isFallback', label: '兜底', type: 'select', width: 70, options: YES_NO, tags: { 1: 'warning', 0: 'success' } },
      { prop: 'fallback', label: '仅看兜底', type: 'select', filter: true, table: false, form: false, options: o([[1, '是'], [0, '否']]) },
      { prop: 'feedback', label: '反馈', type: 'select', width: 70, options: o([[1, '有用'], [-1, '无用']]), tags: { 1: 'success', '-1': 'danger' } },
      { prop: 'retrievedContext', label: '检索上下文', type: 'json', table: false, span: 2 },
      { prop: 'createdAt', label: '时间', type: 'datetime', width: 160 }
    ]
  },

  // ======================= 运营内容 =======================
  {
    key: 'events', title: '展览活动', group: '运营内容', icon: 'Calendar', api: '/events',
    fields: [
      { prop: 'title', label: '活动标题', required: true, minWidth: 180 },
      { prop: 'eventType', label: '类型', type: 'select', filter: true, options: o([['EXHIBITION', '展览'], ['LECTURE', '讲座'], ['WORKSHOP', '研学'], ['ONLINE', '线上活动']]), width: 90 },
      { prop: 'startTime', label: '开始时间', type: 'datetime', width: 160 },
      { prop: 'endTime', label: '结束时间', type: 'datetime', width: 160 },
      { prop: 'location', label: '地点' },
      { prop: 'organizer', label: '主办方', table: false },
      { prop: 'capacity', label: '名额', type: 'number', width: 70 },
      { prop: 'hallId', label: '展厅', type: 'select', dict: 'halls', table: false },
      { prop: 'museumId', label: '博物馆', type: 'select', dict: 'museums', table: false, default: 1 },
      { prop: 'status', label: '状态', type: 'select', filter: true, width: 86, options: o([[0, '未开始'], [1, '进行中'], [2, '已结束']]), tags: { 0: 'info', 1: 'success', 2: 'warning' } },
      { prop: 'content', label: '活动内容', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'announcements', title: '公告', group: '运营内容', icon: 'Bell', api: '/announcements',
    fields: [
      { prop: 'title', label: '公告标题', required: true, minWidth: 220 },
      { prop: 'annType', label: '类型', type: 'select', filter: true, options: o([['NOTICE', '通知'], ['NEWS', '新闻'], ['CLOSE', '闭馆通知']]), width: 96, tags: { NOTICE: 'primary', NEWS: 'success', CLOSE: 'danger' } },
      { prop: 'isTop', label: '置顶', type: 'switch', options: YES_NO, width: 70, tags: { 1: 'danger', 0: 'info' } },
      { prop: 'publishTime', label: '发布时间', type: 'datetime', width: 160 },
      { prop: 'viewCount', label: '阅读', type: 'number', width: 70, form: false },
      { prop: 'status', label: '状态', type: 'select', filter: true, width: 86, options: o([[1, '已发布'], [0, '草稿']]), tags: { 1: 'success', 0: 'info' }, default: 1 },
      { prop: 'content', label: '内容', type: 'textarea', span: 2, table: false }
    ]
  },
  {
    key: 'visit-statistics', title: '访问统计', group: '运营内容', icon: 'DataLine', api: '/visit-statistics',
    fields: [
      { prop: 'statDate', label: '日期', type: 'date', required: true },
      { prop: 'offlineVisitors', label: '线下客流', type: 'number' },
      { prop: 'onlineVisits', label: '线上访问', type: 'number' },
      { prop: 'modelViews', label: '3D浏览', type: 'number' },
      { prop: 'qaCount', label: '问答次数', type: 'number' },
      { prop: 'reservationCount', label: '预约单数', type: 'number' }
    ]
  },

  // ======================= 系统管理（仅管理员） =======================
  {
    key: 'users', title: '用户管理', group: '系统管理', icon: 'User', api: '/users', create: false, edit: false, remove: false, roles: ['ADMIN'],
    desc: '账号启用/禁用、角色分配与密码重置', keywordPlaceholder: '用户名 / 昵称 / 手机',
    rowActions: [
      { label: '禁用', type: 'warning', show: (r) => r.status === 1, confirm: (r) => `确定禁用用户 ${r.username}？`, handler: async (r, c) => { await adminApi.setUserStatus(r.id, 0); c.reload() } },
      { label: '启用', type: 'success', show: (r) => r.status !== 1, handler: async (r, c) => { await adminApi.setUserStatus(r.id, 1); c.reload() } },
      {
        label: '分配角色', type: 'primary', handler: async (r, c) => {
          const roles = await http.get<any[]>('/roles/list')
          const cur = roles.filter((x) => (r.roles || []).includes(x.roleCode)).map((x) => x.roleCode).join(',')
          const { value } = await ElMessageBox.prompt(`可选：${roles.map((x) => `${x.roleCode}(${x.roleName})`).join('、')}，多个用逗号分隔`, `为 ${r.username} 分配角色`, { inputValue: cur })
          const codes = (value || '').split(/[,，\s]+/).filter(Boolean)
          const ids = roles.filter((x) => codes.includes(x.roleCode)).map((x) => x.id)
          await adminApi.assignRoles(r.id, ids)
          ElMessage.success('角色已更新')
          c.reload()
        }
      },
      { label: '重置密码', type: 'danger', confirm: (r) => `确定将 ${r.username} 的密码重置为 123456？`, handler: async (r) => { await adminApi.resetPassword(r.id); ElMessage.success('已重置为 123456') } }
    ],
    fields: [
      { prop: 'username', label: '用户名', width: 110 },
      { prop: 'nickname', label: '昵称', width: 120 },
      { prop: 'userType', label: '类型', type: 'select', filter: true, width: 90, options: o([['ADMIN', '管理员'], ['CURATOR', '馆员'], ['VISITOR', '游客']]), tags: { ADMIN: 'danger', CURATOR: 'warning', VISITOR: 'primary' } },
      { prop: 'roles', label: '角色', formatter: (r) => (r.roles || []).join(', '), width: 140 },
      { prop: 'phone', label: '手机', width: 120 },
      { prop: 'email', label: '邮箱', minWidth: 160 },
      { prop: 'status', label: '状态', type: 'select', filter: true, width: 80, options: o([[1, '正常'], [0, '禁用']]), tags: { 1: 'success', 0: 'danger' } },
      { prop: 'lastLoginTime', label: '最后登录', type: 'datetime', width: 160 },
      { prop: 'createdAt', label: '注册时间', type: 'datetime', width: 160 }
    ]
  },
  {
    key: 'roles', title: '角色管理', group: '系统管理', icon: 'Avatar', api: '/roles', roles: ['ADMIN'],
    fields: [
      { prop: 'roleCode', label: '角色编码', required: true },
      { prop: 'roleName', label: '角色名称', required: true },
      { prop: 'description', label: '描述', span: 2 },
      { prop: 'sort', label: '排序', type: 'number', width: 80 },
      { prop: 'status', label: '状态', type: 'select', options: ENABLE, tags: { 1: 'success', 0: 'info' }, width: 80, default: 1 }
    ]
  },
  {
    key: 'logs', title: '操作日志', group: '系统管理', icon: 'Document', api: '/logs', create: false, edit: false, roles: ['ADMIN'],
    desc: 'AOP 切面记录的写操作日志（敏感字段已脱敏）', keywordPlaceholder: '用户 / 操作 / URI / 模块',
    fields: [
      { prop: 'username', label: '用户', width: 100 },
      { prop: 'module', label: '模块', width: 110 },
      { prop: 'operation', label: '操作', minWidth: 140 },
      { prop: 'requestMethod', label: '方法', width: 76, tags: { GET: 'info', POST: 'success', PUT: 'warning', DELETE: 'danger' } },
      { prop: 'requestUri', label: 'URI', minWidth: 180, ellipsis: true },
      { prop: 'ip', label: 'IP', width: 120 },
      { prop: 'status', label: '结果', type: 'select', filter: true, width: 76, options: o([[1, '成功'], [0, '失败']]), tags: { 1: 'success', 0: 'danger' } },
      { prop: 'costMs', label: '耗时ms', type: 'number', width: 80 },
      { prop: 'requestParams', label: '请求参数', type: 'json', table: false, span: 2 },
      { prop: 'errorMsg', label: '异常信息', type: 'textarea', table: false, span: 2 },
      { prop: 'createdAt', label: '时间', type: 'datetime', width: 160 }
    ]
  }
]

export const CRUD_MAP: Record<string, CrudConfig> = Object.fromEntries(CRUD_CONFIGS.map((c) => [c.key, c]))
export const MENU_GROUPS = (() => {
  const groups: { name: string; icon: string; items: CrudConfig[] }[] = []
  const icons: Record<string, string> = { 文物管理: 'Trophy', 场馆空间: 'OfficeBuilding', 知识图谱: 'Share', 导览服务: 'Guide', 运营内容: 'Promotion', 系统管理: 'Setting' }
  for (const c of CRUD_CONFIGS) {
    let g = groups.find((x) => x.name === c.group)
    if (!g) { g = { name: c.group, icon: icons[c.group] || 'Menu', items: [] }; groups.push(g) }
    g.items.push(c)
  }
  return groups
})()
