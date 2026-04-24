import { request, unwrapApiResponse } from '@/api/client'
import type { OpsDashboardDTO, OpsGranularity } from '@/types/api'

export async function getOpsDashboard(params: {
  granularity?: OpsGranularity
  days?: number
}): Promise<OpsDashboardDTO> {
  const response = await request.get('/v1/admin/ops/dashboard', { params })
  return unwrapApiResponse<OpsDashboardDTO>(response)
}
