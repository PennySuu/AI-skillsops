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

export type UserRole = 'USER' | 'ADMIN'

export interface AuthProfileDTO {
  userId: number
  username: string
  role: UserRole
  expiresInSeconds: number
}
