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
  profilePhoto?: File | null,
): Promise<{ message?: string; username?: string }> {
  const endpoint = url('auth', '/api/auth/register')

  if (profilePhoto) {
    const fd = new FormData()
    fd.set('username', body.username)
    if (body.password) fd.set('password', body.password)
    if (body.firstName) fd.set('firstName', body.firstName)
    if (body.lastName) fd.set('lastName', body.lastName)
    if (body.email) fd.set('email', body.email)
    fd.set('mobile', body.mobile)
    fd.set('countryCode', body.countryCode)
    fd.set('projectType', body.projectType)
    fd.set('acceptTc', body.acceptTc ? 'true' : 'false')
    if (body.pincode) fd.set('pincode', body.pincode)
    if (body.city) fd.set('city', body.city)
    if (body.state) fd.set('state', body.state)
    if (body.country) fd.set('country', body.country)
    if (body.address1) fd.set('address1', body.address1)
    if (body.address2) fd.set('address2', body.address2)
    if (body.address3) fd.set('address3', body.address3)
    fd.append('profilePhoto', profilePhoto, profilePhoto.name)
    const r = await apiFetch(endpoint, { method: 'POST', body: fd })
    const data = await parseJson<{ error?: string; message?: string; username?: string }>(r)
    if (!r.ok) {
      throw new ApiError(data?.error || r.statusText, r.status)
    }
    return data ?? {}
  }

  const r = await apiFetch(endpoint, {
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

/**
 * PUT /api/auth/profile/me — multipart with `profilePhoto` only (other fields unchanged).
 * @see AuthController.updateMyProfile
 */
export async function updateMyProfilePhoto(token: string, file: File): Promise<UserProfileResponse> {
  const fd = new FormData()
  fd.append('profilePhoto', file, file.name)
  const r = await apiFetch(url('auth', '/api/auth/profile/me'), {
    method: 'PUT',
    token,
    body: fd,
  })
  const data =
    (await parseJson<{ message?: string; profile?: UserProfileResponse; error?: string }>(r)) || {}
  if (!r.ok) {
    throw new ApiError(data.error || r.statusText, r.status)
  }
  if (!data.profile) {
    throw new ApiError(data.error || 'Profile update did not return profile', r.status)
  }
  return data.profile
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
