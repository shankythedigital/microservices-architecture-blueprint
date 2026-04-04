/** Decode JWT payload (no signature verification — identity is enforced by the API). */
export function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = base64.length % 4 === 0 ? '' : '='.repeat(4 - (base64.length % 4))
    const json = atob(base64 + pad)
    return JSON.parse(json) as Record<string, unknown>
  } catch {
    return null
  }
}

/** Numeric user id from access token (`uid` claim preferred, else `sub`). */
export function userIdFromAccessToken(token: string | null | undefined): number | null {
  if (!token) return null
  const p = parseJwtPayload(token)
  if (!p) return null
  const uid = p.uid ?? p.sub
  if (uid == null) return null
  const n = Number(uid)
  return Number.isFinite(n) ? n : null
}

export type TokenDisplayInfo = {
  userId: number | null
  username: string | null
  roles: string[]
  sessionId: number | null
}

/** Best-effort display fields from the JWT (no verification). */
export function tokenDisplayInfo(token: string | null | undefined): TokenDisplayInfo {
  if (!token) return { userId: null, username: null, roles: [], sessionId: null }
  const p = parseJwtPayload(token)
  if (!p) return { userId: null, username: null, roles: [], sessionId: null }

  const uid = p.uid ?? p.sub
  const userId =
    uid != null && Number.isFinite(Number(uid)) ? Number(uid) : null

  const username = typeof p.username === 'string' ? p.username : null

  const rawRoles = p.roles
  const roles = Array.isArray(rawRoles)
    ? rawRoles.filter((r): r is string => typeof r === 'string')
    : []

  const sid = p.sid
  const sessionId =
    sid != null && Number.isFinite(Number(sid)) ? Number(sid) : null

  return { userId, username, roles, sessionId }
}

/** True if the access token includes an admin role (matches helpdesk FAQ/knowledge admin checks). */
export function hasAdminRole(token: string | null | undefined): boolean {
  const { roles } = tokenDisplayInfo(token)
  return roles.some((r) => {
    const x = r.toUpperCase()
    return x === 'ROLE_ADMIN' || x === 'ADMIN'
  })
}
