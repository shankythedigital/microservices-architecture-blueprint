import { url } from '../config'
import { ApiError, apiFetch, apiJson, parseJson } from './http'
import type { AuthResponse, UserProfileResponse } from './types'

/** GET /api/auth/terms-and-conditions (Postman: Auth → Terms & Conditions). */
export type TermsPayload = {
  tcId?: number
  title?: string
  content?: string
  summary?: string
  version?: string
  projectType?: string
  language?: string
  effectiveDate?: string
  lastUpdated?: string
}

export async function fetchActiveTerms(projectType?: string): Promise<TermsPayload | null> {
  const q = new URLSearchParams({ language: 'en' })
  if (projectType) q.set('projectType', projectType)
  const r = await apiFetch(`${url('auth', '/api/auth/terms-and-conditions')}?${q}`)
  const data = await parseJson<TermsPayload>(r)
  if (!r.ok || !data) return null
  return data
}

/** POST /api/auth/register (Postman: Registration → Register User). */
export type RegisterRequestBody = {
  username: string
  password?: string
  firstName?: string
  lastName?: string
  email?: string
  mobile: string
  countryCode: string
  projectType: string
  acceptTc: boolean
  pincode?: string
  city?: string
  state?: string
  country?: string
  address1?: string
  address2?: string
  address3?: string
}

export async function registerUser(
  body: RegisterRequestBody,
): Promise<{ message?: string; username?: string }> {
  const r = await apiFetch(url('auth', '/api/auth/register'), {
    method: 'POST',
    body: JSON.stringify(body),
  })
  const data = await parseJson<{ error?: string; message?: string; username?: string }>(r)
  if (!r.ok) {
    throw new ApiError(data?.error || r.statusText, r.status)
  }
  return data ?? {}
}

/** Current user profile with decrypted PII (auth-service `getUserProfileExtended` for self). */
export async function fetchMyProfile(token: string): Promise<UserProfileResponse> {
  return apiJson<UserProfileResponse>(url('auth', '/api/auth/profile/me'), { token })
}

export async function loginPassword(username: string, password: string): Promise<AuthResponse> {
  return apiJson<AuthResponse>(url('auth', '/api/auth/login'), {
    method: 'POST',
    body: JSON.stringify({
      loginType: 'PASSWORD',
      username,
      password,
      deviceInfo: 'Keeply web',
    }),
  })
}

export async function loginOtp(mobile: string, otp: string): Promise<AuthResponse> {
  return apiJson<AuthResponse>(url('auth', '/api/auth/login'), {
    method: 'POST',
    body: JSON.stringify({
      loginType: 'OTP',
      username: mobile,
      otp,
      deviceInfo: 'Keeply web',
    }),
  })
}

/** SMS OTP for existing users (purpose LOGIN). Backend may return `otp` in dev. */
export async function sendLoginOtp(mobile: string): Promise<{ message?: string; otp?: string }> {
  const r = await apiFetch(url('auth', '/api/auth/otp/send'), {
    method: 'POST',
    body: JSON.stringify({
      type: 'SMS',
      mobile,
      purpose: 'LOGIN',
    }),
  })
  const data = (await parseJson<{ error?: string; otp?: string; message?: string }>(r)) || {}
  if (!r.ok) {
    throw new ApiError(data.error || r.statusText, r.status)
  }
  return data
}

/**
 * POST /api/auth/logout — Bearer revokes current session; body `{ refreshToken }` when access token is expired.
 * @see AuthController.logout
 */
export async function logoutOnServer(
  accessToken: string | null | undefined,
  refreshToken?: string | null,
): Promise<void> {
  const at = accessToken?.trim()
  const rt = refreshToken?.trim()
  if (at) {
    await apiFetch(url('auth', '/api/auth/logout'), { method: 'POST', token: at }).catch(() => {})
    return
  }
  if (rt) {
    await apiFetch(url('auth', '/api/auth/logout'), {
      method: 'POST',
      body: JSON.stringify({ refreshToken: rt }),
    }).catch(() => {})
  }
}

/** POST /api/auth/logout?all=true — revoke all sessions for the user (requires valid access token). */
export async function logoutAllDevicesOnServer(accessToken: string): Promise<void> {
  await apiFetch(url('auth', '/api/auth/logout?all=true'), {
    method: 'POST',
    token: accessToken,
  }).catch(() => {})
}

/** POST /api/auth/refresh — returns new AuthResponse. */
export async function refreshAccessToken(refreshToken: string): Promise<AuthResponse> {
  const q = new URLSearchParams({ refreshToken: refreshToken.trim() })
  return apiJson<AuthResponse>(`${url('auth', '/api/auth/refresh')}?${q}`, { method: 'POST' })
}
