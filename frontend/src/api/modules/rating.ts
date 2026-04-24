import { request, unwrapApiResponse } from '@/api/client'
import type { UpsertRatingPayload } from '@/types/api'

export async function upsertSkillRating(skillId: number, payload: UpsertRatingPayload): Promise<void> {
  const response = await request.put(`/v1/skills/${skillId}/ratings`, payload)
  await unwrapApiResponse<void>(response)
}
