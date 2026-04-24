import { request, unwrapApiResponse } from '@/api/client'
import type { PageResponse, SkillDetailDTO, SkillSummaryDTO, UserInstallDTO } from '@/types/api'

export async function getMarketSkills(params: {
  page?: number
  size?: number
  category?: string
  q?: string
  sort?: string
}): Promise<PageResponse<SkillSummaryDTO>> {
  const response = await request.get('/v1/market/skills', { params })
  return unwrapApiResponse<PageResponse<SkillSummaryDTO>>(response)
}

export async function getMarketSkillDetail(skillId: number): Promise<SkillDetailDTO> {
  const response = await request.get(`/v1/market/skills/${skillId}`)
  return unwrapApiResponse<SkillDetailDTO>(response)
}

export async function getMyInstalls(params: {
  page?: number
  size?: number
}): Promise<PageResponse<UserInstallDTO>> {
  const response = await request.get('/v1/users/me/installs', { params })
  return unwrapApiResponse<PageResponse<UserInstallDTO>>(response)
}
