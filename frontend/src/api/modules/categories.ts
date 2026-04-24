import { request, unwrapApiResponse } from '@/api/client'
import type {
  CategoryItemDTO,
  CreateCategoryPayload,
  PageResponse,
  PatchCategoryStatusPayload,
  UpdateCategoryPayload,
} from '@/types/api'

export async function getCategories(params: { page?: number; size?: number }): Promise<PageResponse<CategoryItemDTO>> {
  const response = await request.get('/v1/admin/categories', { params })
  return unwrapApiResponse<PageResponse<CategoryItemDTO>>(response)
}

export async function createCategory(payload: CreateCategoryPayload): Promise<void> {
  const response = await request.post('/v1/admin/categories', payload)
  await unwrapApiResponse<void>(response)
}

export async function updateCategory(categoryId: number, payload: UpdateCategoryPayload): Promise<void> {
  const response = await request.put(`/v1/admin/categories/${categoryId}`, payload)
  await unwrapApiResponse<void>(response)
}

export async function patchCategoryStatus(categoryId: number, payload: PatchCategoryStatusPayload): Promise<void> {
  const response = await request.patch(`/v1/admin/categories/${categoryId}/status`, payload)
  await unwrapApiResponse<void>(response)
}
