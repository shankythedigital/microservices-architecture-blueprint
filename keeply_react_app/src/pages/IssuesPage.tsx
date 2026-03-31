import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listIssues } from '../api/issuesApi'
import { ApiError } from '../api/http'
import type { IssueItem } from '../api/types'

export function IssuesPage() {
  const { token } = useAuth()
  const [items, setItems] = useState<IssueItem[]>([])
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    ;(async () => {
      try {
        setItems(await listIssues(token))
        setErr(null)
      } catch (e) {
        setErr(e instanceof ApiError ? e.message : 'Failed to load issues')
      }
    })()
  }, [token])

  return (
    <div className="page-pad">
      <Link to="/home" className="back-link">
        ← Home
      </Link>
      <h1>Service issues</h1>
      <p className="muted small">Raise a ticket for an appliance and track status updates.</p>

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
        <ul className="plain-list">
        {items.map((i) => (
          <li key={i.id} className="issue-row">
            <div>
              <strong>{i.title || `Issue #${i.id}`}</strong>
              <div className="muted small">
                <span className="pill">{i.status}</span>
                {i.priority ? <span className="pill pill--muted">{i.priority}</span> : null}
              </div>
              {i.description && <p className="muted small" style={{ marginTop: '.35rem' }}>{i.description}</p>}
            </div>
          </li>
        ))}
        </ul>
        {items.length === 0 && !err && <p className="muted">No issues logged.</p>}
      </div>
    </div>
  )
}
