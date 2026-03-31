import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { createIssue } from '../api/issuesApi'
import { ApiError } from '../api/http'

export function NewIssuePage() {
  const { token } = useAuth()
  const [sp] = useSearchParams()
  const assetIdStr = sp.get('assetId')
  const nav = useNavigate()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('Electrical')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setBusy(true)
    setErr(null)
    try {
      await createIssue(token, {
        title: `${category}: ${title}`,
        description,
        priority: 'MEDIUM',
        relatedService: 'ASSET_SERVICE',
        assetId: assetIdStr ? Number(assetIdStr) : undefined,
      })
      nav('/home/issues')
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Could not create issue')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page-pad">
      <Link to="/home/issues" className="back-link">
        ← Issues
      </Link>
      <h1>Raise an issue</h1>
      <p className="muted small">Select an issue type and tell us what’s wrong.</p>

      {assetIdStr && (
        <div className="sheet asset-pill">
          <div className="asset-pill__icon" aria-hidden>
            <svg width="34" height="34" viewBox="0 0 48 48">
              <g fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                <rect x="10" y="14" width="28" height="20" rx="3" />
                <path d="M16 26h16" />
              </g>
            </svg>
          </div>
          <div>
            <strong>Selected asset</strong>
            <div className="muted small">Asset ID: {assetIdStr}</div>
          </div>
          <span className="asset-pill__change muted small">Change</span>
        </div>
      )}

      <form onSubmit={onSubmit} className="stack" style={{ paddingBottom: '5.25rem' }}>
        <div>
          <h2 className="section-title">Select an issue</h2>
          <div className="issue-pick">
            {[
              { key: 'Electrical', label: 'Electrical & Power Related', icon: 'plug' },
              { key: 'Heating', label: 'Heating Related', icon: 'flame' },
              { key: 'Mechanical', label: 'Mechanical Parts', icon: 'gear' },
              { key: 'Other', label: 'Miscellaneous', icon: 'misc' },
            ].map((opt) => {
              const selected = category === opt.key
              return (
                <button
                  key={opt.key}
                  type="button"
                  className={selected ? 'issue-card is-selected' : 'issue-card'}
                  onClick={() => setCategory(opt.key)}
                >
                  <span className="issue-card__icon" aria-hidden>
                    {opt.icon === 'plug' && (
                      <svg width="22" height="22" viewBox="0 0 24 24">
                        <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M9 3v5M15 3v5" />
                          <path d="M7 8h10v4a5 5 0 0 1-5 5H12a5 5 0 0 1-5-5V8Z" />
                          <path d="M12 17v4" />
                        </g>
                      </svg>
                    )}
                    {opt.icon === 'flame' && (
                      <svg width="22" height="22" viewBox="0 0 24 24">
                        <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M12 2s4 4 4 8a4 4 0 0 1-8 0c0-2 1-4 2-6" />
                          <path d="M12 12c2 2 3 3 3 5a3 3 0 0 1-6 0c0-1 1-2 3-5Z" />
                        </g>
                      </svg>
                    )}
                    {opt.icon === 'gear' && (
                      <svg width="22" height="22" viewBox="0 0 24 24">
                        <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                          <path d="M19.4 15a7.8 7.8 0 0 0 .1-2l2-1.2-2-3.4-2.3.6a7.5 7.5 0 0 0-1.7-1l-.3-2.3H11l-.3 2.3c-.6.2-1.2.6-1.7 1l-2.3-.6-2 3.4 2 1.2a7.8 7.8 0 0 0 0 2l-2 1.2 2 3.4 2.3-.6c.5.4 1.1.7 1.7 1l.3 2.3h4.2l.3-2.3c.6-.2 1.2-.6 1.7-1l2.3.6 2-3.4-2-1.2Z" />
                        </g>
                      </svg>
                    )}
                    {opt.icon === 'misc' && (
                      <svg width="22" height="22" viewBox="0 0 24 24">
                        <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M4 7h16M4 12h10M4 17h16" />
                        </g>
                      </svg>
                    )}
                  </span>
                  <span className="issue-card__label">{opt.label}</span>
                  <span className="issue-card__check" aria-hidden>
                    {selected ? '✓' : ''}
                  </span>
                </button>
              )
            })}
          </div>
        </div>

        <div className="sheet">
          <label className="field">
            <span>Summary</span>
            <input value={title} onChange={(e) => setTitle(e.target.value)} required />
          </label>
          <div style={{ height: 10 }} />
          <label className="field">
            <span>Description</span>
            <textarea rows={4} value={description} onChange={(e) => setDescription(e.target.value)} required />
          </label>
        </div>

        {err && <p className="error-banner">{err}</p>}

        <div className="bottom-action">
          <button type="submit" className="btn keeply-submit" disabled={busy}>
            {busy ? 'Submitting…' : 'Submit'}
          </button>
        </div>
      </form>
    </div>
  )
}
