/** 统一响应 envelope（与后端 ApiResponse<T> 契约对齐） */
export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

/** 分页响应（与 config.yaml 约定对齐） */
export interface PageResponse<T> {
  page: number
  size: number
  total: number
  items: T[]
}

export interface SkillSummaryDTO {
  id: number
  name: string
  description: string
  avgRating: number
  ratingCount: number
}

export interface SkillDetailDTO {
  id: number
  name: string
  description: string
  avgRating: number
  ratingCount: number
  versions: Array<{
    version: string
    createdAt: string
  }>
}

export interface InstallCommandDTO {
  command: string
}

export interface UserInstallDTO {
  skillId: number
  skillName: string
  installedAt: string
  installedVersion: string
  latestVersion: string
  updateAvailable: boolean
  offline: boolean
}

export interface UpsertRatingPayload {
  score: number
  comment?: string
}

export interface CategoryItemDTO {
  id: number
  name: string
  enabled: boolean
}

export interface CreateCategoryPayload {
  name: string
  enabled: boolean
}

export interface UpdateCategoryPayload {
  name: string
}

export interface PatchCategoryStatusPayload {
  enabled: boolean
}

export type OpsGranularity = 'day' | 'week' | 'month'

export interface OpsMetricCardDTO {
  key: string
  label: string
  value: number
}

export interface OpsTrendPointDTO {
  bucket: string
  installs: number
}

export interface OpsTopSkillDTO {
  skillId: number
  skillName: string
  installCount: number
}

export interface OpsActiveAuthorDTO {
  authorId: number
  username: string
  publishedCount: number
}

export interface OpsDashboardDTO {
  granularity: OpsGranularity
  days: number
  metrics: OpsMetricCardDTO[]
  installTrend: OpsTrendPointDTO[]
  topSkills: OpsTopSkillDTO[]
  activeAuthors: OpsActiveAuthorDTO[]
}

export type UserRole = 'USER' | 'ADMIN'

export interface AuthProfileDTO {
  userId: number
  username: string
  role: UserRole
  expiresInSeconds: number
}
