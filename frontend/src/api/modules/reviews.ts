import { request, unwrapApiResponse } from '@/api/client'

export interface PendingReviewItem {
  reviewId: number
  skillId: number
  submittedBy: number
  createdAt: string
}

export async function getPendingReviews(): Promise<PendingReviewItem[]> {
  const response = await request.get('/v1/reviews/pending')
  return unwrapApiResponse<PendingReviewItem[]>(response)
}

export async function approveReview(reviewId: number): Promise<void> {
  const response = await request.post(`/v1/reviews/${reviewId}/approve`)
  await unwrapApiResponse<void>(response)
}

export async function rejectReview(reviewId: number, reason: string): Promise<void> {
  const response = await request.post(`/v1/reviews/${reviewId}/reject`, { reason })
  await unwrapApiResponse<void>(response)
}
