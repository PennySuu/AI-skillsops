import type { AxiosResponse } from 'axios'
import { describe, expect, it } from 'vitest'
import { unwrapApiResponse } from '@/api/client'
import type { ApiResponse } from '@/types/api'

describe('unwrapApiResponse', () => {
  it('should return data when success is true', async () => {
    const payload: ApiResponse<{ name: string }> = {
      success: true,
      code: 'OK',
      message: 'success',
      data: { name: 'skillsops' },
    }
    const response = { data: payload } as AxiosResponse<ApiResponse<{ name: string }>>
    await expect(unwrapApiResponse(response)).resolves.toEqual({ name: 'skillsops' })
  })

  it('should throw error message when success is false', async () => {
    const payload: ApiResponse<null> = {
      success: false,
      code: 'VALIDATION_FAILED',
      message: '参数错误',
      data: null,
    }
    const response = { data: payload } as AxiosResponse<ApiResponse<null>>
    await expect(unwrapApiResponse(response)).rejects.toThrow('参数错误')
  })
})
