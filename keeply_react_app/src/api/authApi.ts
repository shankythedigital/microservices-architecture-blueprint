import { url } from '../config'
import { ApiError, apiFetch, apiJson, parseJson } from './http'
import type { AuthResponse, UserProfileResponse } from './types'

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
