import type { UserProfileResponse } from '../api/types'

export function accountInitial(username: string | null, userId: number | null): string {
  if (username && username.length > 0) {
    const c = username.replace(/[^a-zA-Z0-9]/g, '').charAt(0)
    if (c) return c.toUpperCase()
  }
  if (userId != null) return String(userId % 10)
  return '?'
}

export function displayNameFromProfile(profile: UserProfileResponse | null | undefined): string | null {
  if (!profile) return null
  const name = [profile.firstName, profile.lastName].filter(Boolean).join(' ').trim()
  if (name) return name
  if (profile.username) return profile.username
  if (profile.mobile) return profile.mobile
  if (profile.email) return profile.email
  if (profile.userId != null) return `Member #${profile.userId}`
  return null
}

/** Prefer decrypted profile for the avatar letter; fall back to JWT username / user id. */
export function accountInitialFromProfile(
  profile: UserProfileResponse | null | undefined,
  jwtUsername: string | null,
  fallbackUserId: number | null,
): string {
  if (profile?.firstName || profile?.lastName) {
    const compact = `${profile.firstName ?? ''}${profile.lastName ?? ''}`.replace(/\s/g, '')
    const c = compact.replace(/[^a-zA-Z0-9]/g, '').charAt(0)
    if (c) return c.toUpperCase()
  }
  if (profile?.username) return accountInitial(profile.username, null)
  if (profile?.email) {
    const local = profile.email.split('@')[0] ?? profile.email
    return accountInitial(local, null)
  }
  if (profile?.mobile) return accountInitial(profile.mobile.replace(/\s/g, ''), null)
  return accountInitial(jwtUsername, profile?.userId ?? fallbackUserId)
}
