import { request, unwrapApiResponse } from '@/api/client'
import type { AuthProfileDTO } from '@/types/api'

interface RegisterPayload {
  username: string
  password: string
}

interface LoginPayload {
  username: string
  password: string
}

export async function fetchCsrfToken(): Promise<string> {
  const response = await request.get('/v1/auth/csrf-token')
  return unwrapApiResponse<string>(response)
}

export async function register(payload: RegisterPayload): Promise<AuthProfileDTO> {
  await fetchCsrfToken()
  const response = await request.post('/v1/auth/register', payload)
  return unwrapApiResponse<AuthProfileDTO>(response)
}

export async function login(payload: LoginPayload): Promise<AuthProfileDTO> {
  await fetchCsrfToken()
  const response = await request.post('/v1/auth/login', payload)
  return unwrapApiResponse<AuthProfileDTO>(response)
}

export async function logout(): Promise<void> {
  await fetchCsrfToken()
  const response = await request.post('/v1/auth/logout')
  await unwrapApiResponse<void>(response)
}
