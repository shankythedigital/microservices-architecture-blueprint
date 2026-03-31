import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { markNotificationRead, notificationList } from '../api/notificationsApi'
import { ApiError } from '../api/http'
import type { NotificationItem } from '../api/types'

export function AlertsPage() {
  const { token } = useAuth()
  const [items, setItems] = useState<NotificationItem[]>([])
  const [err, setErr] = useState<string | null>(null)

  async function refresh() {
    if (!token) return
    setErr(null)
    try {
      const res = await notificationList(token)
      setItems(res.data ?? [])
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Failed to load alerts')
    }
  }

  useEffect(() => {
    refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  async function onRead(id: number) {
    if (!token) return
    try {
      await markNotificationRead(token, id)
      await refresh()
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Could not update')
    }
  }

  return (
    <div className="page-pad">
      <h1>Alerts</h1>
      <p className="muted small">Service reminders, warranty notices, and updates for your appliances.</p>
      {err && <p className="error-banner">{err}</p>}
      {!err && items.length > 0 && (
        <p className="muted small">{items.filter((i) => !i.read).length} unread</p>
      )}
      <ul className="plain-list tight">
        {items.map((n) => (
          <li key={n.id} className="alert-item">
            <div>
              <strong>{n.title || 'Notice'}</strong>
              {n.message && <p className="muted small">{n.message}</p>}
              {n.createdAt && <span className="muted small">{n.createdAt}</span>}
            </div>
            {!n.read && (
              <button type="button" className="btn secondary tight" onClick={() => onRead(n.id)}>
                Mark read
              </button>
            )}
          </li>
        ))}
      </ul>
      {items.length === 0 && !err && <p className="muted">No alerts in the current window.</p>}
    </div>
  )
}
