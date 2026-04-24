import { request, unwrapApiResponse } from '@/api/client'

export interface WorkspaceSkillItem {
  id: number
  name: string
  description: string
  resourceUrl: string
  categoryId: number
  status: 'draft' | 'pending' | 'published' | 'offline'
  currentVersion: string
  updatedAt: string
  rejectReason?: string
}

export interface CreateSkillPayload {
  name: string
  description: string
  resourceUrl: string
  categoryId: number
}

export interface UpdateSkillPayload {
  name?: string
  description?: string
  resourceUrl?: string
}

export interface CreateVersionPayload {
  version: string
  changelog: string
  resourceUrl: string
}

export async function createSkill(payload: CreateSkillPayload): Promise<void> {
  const response = await request.post('/v1/skills', payload)
  await unwrapApiResponse<void>(response)
}

export async function updateSkill(skillId: number, payload: UpdateSkillPayload): Promise<void> {
  const response = await request.patch(`/v1/skills/${skillId}`, payload)
  await unwrapApiResponse<void>(response)
}

export async function submitSkillReview(skillId: number): Promise<void> {
  const response = await request.post(`/v1/skills/${skillId}/submit-review`)
  await unwrapApiResponse<void>(response)
}

export async function createSkillVersion(skillId: number, payload: CreateVersionPayload): Promise<void> {
  const response = await request.post(`/v1/skills/${skillId}/versions`, payload)
  await unwrapApiResponse<void>(response)
}
