export interface UserInfo {
  id: number
  username: string
  nickname?: string
  avatar?: string
  email?: string
  phone?: string
  userType?: string
  status?: number
  lastLoginTime?: string
  createdAt?: string
  roles: string[]
  permissions?: string[] | null
}
export interface LoginVO {
  token: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}
export interface Artifact {
  id: number
  artifactNo: string
  name: string
  alias?: string
  categoryId?: number
  periodId?: number
  pitId?: number
  materialId?: number
  hallId?: number
  showcaseId?: number
  dimensions?: string
  heightCm?: number
  widthCm?: number
  weightG?: number
  excavationYear?: number
  grade?: string
  isTreasure?: number
  description?: string
  culturalValue?: string
  guideText?: string
  coverUrl?: string
  viewCount?: number
  favoriteCount?: number
  status?: number
  categoryName?: string
  periodName?: string
  pitName?: string
  pitCode?: string
  materialName?: string
  hallName?: string
  hasModel?: boolean
}
export interface ArtifactImage {
  id: number
  artifactId: number
  imageUrl: string
  imageType: string
  title?: string
  isCover?: number
}
export interface Model3d {
  id: number
  artifactId?: number
  hallId?: number
  name: string
  modelType?: string
  modelUrl: string
  format?: string
  fileSize?: number
  gaussianCount?: number
  shDegree?: number
  imageCount?: number
  captureDevice?: string
  sfmTool?: string
  trainedBy?: string
  trainIterations?: number
  trainMinutes?: number
  psnr?: number
  ssim?: number
  lpips?: number
  cameraPosition?: string
  cameraTarget?: string
  thumbnailUrl?: string
  isDefault?: number
  status?: number
}
export interface GraphNode {
  id: string
  name: string
  category: number
  entityType: string
  symbolSize: number
  hop?: number | null
  description?: string
  refTable?: string
  refId?: number
}
export interface GraphLink {
  id?: string
  source: string
  target: string
  name: string
  relationType?: string
  value?: number
}
export interface GraphVO {
  nodes: GraphNode[]
  links: GraphLink[]
  categories: { name: string; code: string }[]
}
export interface Literature {
  id: number
  title: string
  authors?: string
  source?: string
  publishYear?: string
  litType?: string
  abstractText?: string
}
export interface ArtifactDetail {
  artifact: Artifact
  images: ArtifactImage[]
  models: Model3d[]
  graph: GraphVO
  literature: Literature[]
  restorations: any[]
  relatedArtifacts: Artifact[]
  avgRating: number
  reviewCount: number
  favorited: boolean
}
export interface Review {
  id: number
  createdAt: string
  userId: number
  targetType: string
  targetId: number
  rating: number
  content: string
  likeCount: number
  status: number
  reply?: string
  nickname?: string
  avatar?: string
  targetName?: string
}
export interface NameValue {
  name: string
  value: number
}
export interface Route {
  id: number
  name: string
  routeType: string
  theme?: string
  description?: string
  durationMin?: number
  distanceM?: number
  difficulty?: number
  suitableCrowd?: string
  coverUrl?: string
  useCount?: number
  status?: number
}
export interface RouteNode {
  id: number
  routeId: number
  seq: number
  nodeType: string
  artifactId?: number
  hallId?: number
  title: string
  narration?: string
  stayMinutes?: number
  cameraPosition?: string
  cameraTarget?: string
  artifactCover?: string
  hallName?: string
  modelUrl?: string
}
export interface RouteDetail {
  route: Route
  nodes: RouteNode[]
  totalStayMinutes: number
  avgRating?: number
}
export interface Slot {
  timeSlot: string
  booked: number
  capacity: number
  remaining: number
}
export interface Reservation {
  id: number
  reservationNo: string
  visitDate: string
  timeSlot: string
  visitorCount: number
  contactName: string
  contactPhone: string
  routeId?: number
  needGuide?: number
  status: string
  checkInTime?: string
  remark?: string
  createdAt?: string
}
export interface QaSession {
  id: number
  title: string
  messageCount: number
  lastActiveTime?: string
  createdAt?: string
}
export interface QaMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant'
  content: string
  retrievedContext?: string
  citedEntities?: string
  modelName?: string
  totalTokens?: number
  latencyMs?: number
  isFallback?: number
  feedback?: number
  createdAt?: string
}
export interface Overview {
  artifactCount: number
  treasureCount: number
  pitCount: number
  modelCount: number
  gaussianTotal: number
  entityCount: number
  relationCount: number
  literatureCount: number
  userCount: number
  qaSessionCount: number
  qaQuestionCount: number
  routeCount: number
  reservationCount: number
  todayReservationVisitors: number
  visitors30d: number
  avgRating: number
}
export interface Announcement {
  id: number
  title: string
  content: string
  annType: string
  isTop: number
  publishTime?: string
}
export interface ExhibitionEvent {
  id: number
  title: string
  eventType: string
  startTime?: string
  endTime?: string
  location?: string
  content?: string
  status?: number
}
