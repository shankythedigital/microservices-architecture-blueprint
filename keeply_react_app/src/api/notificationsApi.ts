import { url } from '../config'
import { apiFetch, apiJson, parseJson } from './http'
import type { NotificationItem, ResponseWrapper } from './types'

export function notificationList(
  token: string,
  days?: number,
): Promise<ResponseWrapper<NotificationItem[]>> {
  const q = new URLSearchParams()
  if (days != null) q.set('days', String(days))
  const qs = q.toString()
  return apiJson<ResponseWrapper<NotificationItem[]>>(
    `${url('notification', '/api/notifications/list')}${qs ? `?${qs}` : ''}`,
    { token },
  )
}

export async function notificationCount(token: string): Promise<number> {
  const w = await apiJson<ResponseWrapper<Record<string, unknown>>>(
    url('notification', '/api/notifications/count'),
    { token },
  )
  return Number(w.data?.count ?? 0)
}

export async function markNotificationRead(token: string, id: number): Promise<void> {
  const r = await apiFetch(url('notification', `/api/notifications/read/${id}`), {
    method: 'PUT',
    token,
  })
  if (!r.ok) {
    const j = await parseJson<{ message?: string } | { error?: string }>(r)
    const msg =
      (j as { error?: string } | undefined)?.error ||
      (j as { message?: string } | undefined)?.message ||
      r.statusText
    throw new Error(msg)
  }
}
