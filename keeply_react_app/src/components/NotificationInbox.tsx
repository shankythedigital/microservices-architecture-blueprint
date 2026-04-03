import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { markAllNotificationsRead, markNotificationRead, notificationList } from '../api/notificationsApi'
import { ApiError } from '../api/http'
import type { NotificationItem } from '../api/types'
import { formatApiDateForDisplay } from '../utils/apiDate'

type Tab = 'all' | 'unread' | 'read'

export function NotificationInbox() {
  const { token } = useAuth()
  const [items, setItems] = useState<NotificationItem[]>([])
  const [err, setErr] = useState<string | null>(null)
  const [tab, setTab] = useState<Tab>('all')
  const [markAllBusy, setMarkAllBusy] = useState(false)

  async function refresh() {
    if (!token) return
    setErr(null)
    try {
      const res = await notificationList(token)
      setItems(res.data ?? [])
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Failed to load notifications')
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

  async function onMarkAllRead() {
    if (!token || !items.some((i) => !i.read)) return
    setMarkAllBusy(true)
    setErr(null)
    try {
      await markAllNotificationsRead(token)
      await refresh()
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Could not mark all as read')
    } finally {
      setMarkAllBusy(false)
    }
  }

  const filtered = useMemo(() => {
    if (tab === 'unread') return items.filter((i) => !i.read)
    if (tab === 'read') return items.filter((i) => i.read)
    return items
  }, [items, tab])

  const unreadCount = items.filter((i) => !i.read).length

  return (
    <>
      <p className="muted small">
        Service reminders, warranty notices, and account messages. Adjust delivery in{' '}
        <Link to="/home/account/settings">Settings</Link>.
      </p>

      {err && <p className="error-banner">{err}</p>}

      {unreadCount > 0 && (
        <div className="notification-inbox-toolbar">
          <button
            type="button"
            className="btn ghost tight"
            disabled={markAllBusy}
            onClick={() => void onMarkAllRead()}
          >
            {markAllBusy ? 'Updating…' : 'Mark all as read'}
          </button>
        </div>
      )}

      <div className="segmented segmented--stretch" role="tablist" aria-label="Notification filter">
        <button
          type="button"
          className={tab === 'all' ? 'is-on' : ''}
          role="tab"
          aria-selected={tab === 'all'}
          onClick={() => setTab('all')}
        >
          All{items.length > 0 ? ` (${items.length})` : ''}
        </button>
        <button
          type="button"
          className={tab === 'unread' ? 'is-on' : ''}
          role="tab"
          aria-selected={tab === 'unread'}
          onClick={() => setTab('unread')}
        >
          Unread{unreadCount > 0 ? ` (${unreadCount})` : ''}
        </button>
        <button
          type="button"
          className={tab === 'read' ? 'is-on' : ''}
          role="tab"
          aria-selected={tab === 'read'}
          onClick={() => setTab('read')}
        >
          Read
        </button>
      </div>

      {!err && tab === 'unread' && unreadCount === 0 && (
        <p className="ok-banner">You’re caught up — no unread notifications.</p>
      )}

      <ul className="plain-list tight notification-list">
        {filtered.map((n) => (
          <li key={n.id} className={`alert-item ${!n.read ? 'alert-item--unread' : ''}`}>
            <div className="alert-item__body">
              <div className="alert-item__title-row">
                <strong>{n.title || 'Notice'}</strong>
                {n.priority && n.priority !== 'NORMAL' && (
                  <span className="pill pill--priority">{n.priority}</span>
                )}
              </div>
              {n.message && <p className="muted small">{n.message}</p>}
              {n.createdAt && (
                <span className="muted small">{formatApiDateForDisplay(n.createdAt)}</span>
              )}
            </div>
            {!n.read && (
              <button type="button" className="btn secondary tight" onClick={() => onRead(n.id)}>
                Mark read
              </button>
            )}
          </li>
        ))}
      </ul>
      {filtered.length === 0 && !err && (
        <p className="muted empty-inbox-hint">
          {tab === 'read' ? 'No read items yet.' : 'No notifications in the current window.'}
        </p>
      )}
    </>
  )
}
