import { getServiceBase } from '../config'

/** Turn auth-service stored paths (e.g. uploads/USER_PROFILE/…) into a browser-loadable URL. */
export function resolveAuthUploadedFileUrl(storedPath: string | null | undefined): string | undefined {
  const raw = storedPath?.trim()
  if (!raw) return undefined
  if (/^https?:\/\//i.test(raw)) return raw
  const path = raw.startsWith('/') ? raw : `/${raw}`
  const base = getServiceBase('auth')
  return base ? `${base.replace(/\/$/, '')}${path}` : path
}
