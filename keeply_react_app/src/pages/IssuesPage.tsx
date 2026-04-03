import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listMyIssues } from '../api/issuesApi'
import { ApiError } from '../api/http'
import type { IssueItem } from '../api/types'

function statusLabel(status: string | undefined): string {
  if (!status) return '—'
  return status.replace(/_/g, ' ')
}

export function IssuesPage() {
  const { token } = useAuth()
  const location = useLocation()
  const [items, setItems] = useState<IssueItem[]>([])
  const [err, setErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    ;(async () => {
      setLoading(true)
      try {
        const rows = await listMyIssues(token)
        if (!cancelled) {
          setItems(Array.isArray(rows) ? rows : [])
          setErr(null)
        }
      } catch (e) {
        if (!cancelled) {
          setItems([])
          setErr(e instanceof ApiError ? e.message : 'Failed to load issues')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token, location.pathname, location.key])

  const sortedItems = useMemo(() => {
    return [...items].sort((a, b) => {
      const ta = a.createdAt ? Date.parse(a.createdAt) : 0
      const tb = b.createdAt ? Date.parse(b.createdAt) : 0
      return tb - ta
    })
  }, [items])

  return (
    <div className="page-pad">
      <Link to="/home" className="back-link">
        ← Home
      </Link>
      <h1>Service issues</h1>
      <p className="muted small">
        Tickets you raised on this account, newest first, with current status.{' '}
        <Link to="/home/helpdesk">Help &amp; support hub</Link>
      </p>

      <div className="cta-card issue-cta">
        <Link to="/home/issues/new" className="cta-card__inner" aria-label="Raise new issue">
          <span className="cta-card__plus" aria-hidden>
            +
          </span>
          <span>Raise New Issue</span>
        </Link>
      </div>

      {err && <p className="error-banner">{err}</p>}
      <div className="sheet">
        <h2 className="section-title" style={{ marginTop: 0 }}>
          Recent tickets
        </h2>
        {loading && <p className="muted small">Loading your tickets…</p>}
        <ul className="plain-list">
        {sortedItems.map((i) => (
          <li key={i.id} className="issue-row">
            <Link to={`/home/issues/${i.id}`} className="issue-row__link">
              <strong>{i.title || `Issue #${i.id}`}</strong>
              <div className="muted small">
                <span className="pill" title="Status">
                  {statusLabel(i.status)}
                </span>
                {i.priority ? (
                  <span className="pill pill--muted" title="Priority">
                    {i.priority}
                  </span>
                ) : null}
              </div>
              {i.description && <p className="muted small" style={{ marginTop: '.35rem' }}>{i.description}</p>}
              <span className="muted small issue-row__view">View details →</span>
            </Link>
          </li>
        ))}
        </ul>
        {!loading && sortedItems.length === 0 && !err && (
          <p className="muted">No issues logged yet. Raise one to see it here.</p>
        )}
      </div>
    </div>
  )
}
