import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listMyQueries } from '../api/queriesApi'
import { ApiError } from '../api/http'
import type { HelpdeskQueryItem } from '../api/types'
import { formatApiDateForDisplay } from '../utils/apiDate'

export function QueriesPage() {
  const { token } = useAuth()
  const [items, setItems] = useState<HelpdeskQueryItem[]>([])
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    ;(async () => {
      try {
        setItems(await listMyQueries(token))
        setErr(null)
      } catch (e) {
        setErr(e instanceof ApiError ? e.message : 'Failed to load questions')
      }
    })()
  }, [token])

  return (
    <div className="page-pad">
      <Link to="/home/helpdesk" className="back-link">
        ← Help &amp; support
      </Link>
      <h1>My questions</h1>
      <p className="muted small">Questions you submitted to support and responses when available.</p>

      <div className="cta-card issue-cta">
        <Link to="/home/helpdesk/queries/new" className="cta-card__inner" aria-label="Ask a new question">
          <span className="cta-card__plus" aria-hidden>
            +
          </span>
          <span>New question</span>
        </Link>
      </div>

      {err && <p className="error-banner">{err}</p>}

      <ul className="plain-list">
        {items.map((q) => (
          <li key={q.id} className="sheet query-card">
            <div className="query-card__head">
              <strong>{q.question || `Question #${q.id}`}</strong>
              <span className="pill">{q.status ?? '—'}</span>
            </div>
            {q.relatedService && <span className="muted small">{q.relatedService.replaceAll('_', ' ')}</span>}
            {q.answer && (
              <div className="query-card__answer">
                <span className="muted small">Answer</span>
                <p style={{ margin: '0.35rem 0 0' }}>{q.answer}</p>
              </div>
            )}
            {q.createdAt && (
              <p className="muted small" style={{ margin: '0.5rem 0 0' }}>
                Asked {formatApiDateForDisplay(q.createdAt)}
                {q.answeredAt ? ` · Answered ${formatApiDateForDisplay(q.answeredAt)}` : ''}
              </p>
            )}
          </li>
        ))}
      </ul>
      {items.length === 0 && !err && <p className="muted">You have not asked any questions yet.</p>}
    </div>
  )
}
