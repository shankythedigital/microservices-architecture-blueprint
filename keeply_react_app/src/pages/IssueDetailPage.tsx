import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getIssue } from '../api/issuesApi'
import { ApiError } from '../api/http'
import type { IssueItem } from '../api/types'
import { formatApiDateForDisplay } from '../utils/apiDate'

export function IssueDetailPage() {
  const { id } = useParams()
  const { token } = useAuth()
  const [issue, setIssue] = useState<IssueItem | null>(null)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token || id == null) return
    const n = Number(id)
    if (!Number.isFinite(n)) {
      setErr('Invalid issue id')
      return
    }
    let c = false
    ;(async () => {
      try {
        const data = await getIssue(token, n)
        if (!c) {
          setIssue(data)
          setErr(null)
        }
      } catch (e) {
        if (!c) {
          setIssue(null)
          setErr(e instanceof ApiError ? e.message : 'Could not load issue')
        }
      }
    })()
    return () => {
      c = true
    }
  }, [token, id])

  return (
    <div className="page-pad">
      <Link to="/home/issues" className="back-link">
        ← My tickets
      </Link>
      <h1>{issue?.title || (id ? `Issue #${id}` : 'Issue')}</h1>

      {err && <p className="error-banner">{err}</p>}

      {issue && (
        <div className="detail-card">
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginBottom: '0.65rem' }}>
            {issue.status && <span className="pill">{issue.status}</span>}
            {issue.priority && <span className="pill pill--muted">{issue.priority}</span>}
            {issue.relatedService && (
              <span className="pill pill--muted">{issue.relatedService.replaceAll('_', ' ')}</span>
            )}
          </div>
          {issue.description && <p style={{ margin: 0 }}>{issue.description}</p>}
          <dl className="dl-grid" style={{ marginTop: '1rem' }}>
            {issue.assetId != null && (
              <>
                <dt>Asset ID</dt>
                <dd>
                  <Link to={`/home/assets/${issue.assetId}`}>#{issue.assetId}</Link>
                </dd>
              </>
            )}
            {issue.reportedBy && (
              <>
                <dt>Reported by</dt>
                <dd>{issue.reportedBy}</dd>
              </>
            )}
            {issue.assignedTo && (
              <>
                <dt>Assigned to</dt>
                <dd>{issue.assignedTo}</dd>
              </>
            )}
            {issue.createdAt && (
              <>
                <dt>Created</dt>
                <dd>{formatApiDateForDisplay(issue.createdAt)}</dd>
              </>
            )}
            {issue.updatedAt && (
              <>
                <dt>Updated</dt>
                <dd>{formatApiDateForDisplay(issue.updatedAt)}</dd>
              </>
            )}
            {issue.resolvedAt && (
              <>
                <dt>Resolved</dt>
                <dd>{formatApiDateForDisplay(issue.resolvedAt)}</dd>
              </>
            )}
          </dl>
          {issue.resolution && (
            <div className="sheet" style={{ marginTop: '0.85rem', background: '#f8fffe' }}>
              <strong className="small">Resolution</strong>
              <p className="muted small" style={{ margin: '0.35rem 0 0' }}>
                {issue.resolution}
              </p>
            </div>
          )}
          <p className="muted small" style={{ marginTop: '1rem' }}>
            <Link to="/home/helpdesk">Help hub</Link>
            {' · '}
            <Link to="/home/helpdesk/queries/new">Ask support a question</Link>
          </p>
        </div>
      )}
    </div>
  )
}
