/**
 * 全局前端配置（集中在一处，便于答辩/部署时修改）
 */
export const APP_CONFIG = {
  /** 系统名称 */
  appName: '三星堆数字孪生',
  appFullName: '基于3D高斯泼溅与GraphRAG的三星堆文物数字孪生与智能导览系统',
  /** API 前缀（开发时由 Vite 代理到 :8080） */
  apiBase: '/api',
  /**
   * 3DGS 回退示例模型：当文物的 model_url 为占位路径或无法访问时，查看器自动加载此公开示例，
   * 保证演示效果。可通过环境变量 VITE_SAMPLE_SPLAT_URL 覆盖。
   */
  sampleSplatUrl:
    (import.meta.env.VITE_SAMPLE_SPLAT_URL as string | undefined) ||
    'https://huggingface.co/cakewalk/splat-data/resolve/main/train.splat',
  /** 示例模型的推荐相机参数 */
  sampleCamera: { position: [-6, -2, 6] as number[], lookAt: [0, 0, 0] as number[], up: [0, -1, 0] as number[] },
  /** 默认博物馆 ID */
  museumId: 1,
  /** Token 存储键 */
  tokenKey: 'sxd_token',
  userKey: 'sxd_user'
}

/** 图片地址处理：占位/空地址返回 undefined，交给组件显示纹样占位 */
export function resolveImg(url?: string | null): string | undefined {
  if (!url) return undefined
  return url
}
