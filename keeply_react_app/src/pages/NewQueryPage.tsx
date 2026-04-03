import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { createQuery } from '../api/queriesApi'
import { HELP_DESK_RELATED_SERVICES, type RelatedService } from '../constants/helpdesk'
import { ApiError } from '../api/http'

export function NewQueryPage() {
  const { token } = useAuth()
  const nav = useNavigate()
  const [question, setQuestion] = useState('')
  const [relatedService, setRelatedService] = useState<RelatedService>('ASSET_SERVICE')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setBusy(true)
    setErr(null)
    try {
      await createQuery(token, { question: question.trim(), relatedService })
      nav('/home/helpdesk/queries')
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Could not submit question')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page-pad">
      <Link to="/home/helpdesk/queries" className="back-link">
        ← My questions
      </Link>
      <h1>Ask a question</h1>
      <p className="muted small">This creates a support query (not an appliance repair ticket). Use “Raise issue” for repairs.</p>

      <form onSubmit={onSubmit} className="stack" style={{ paddingBottom: '5.25rem' }}>
        <div className="sheet">
          <h2 className="section-title" style={{ marginTop: 0 }}>
            Topic
          </h2>
          <div className="issue-pick">
            {HELP_DESK_RELATED_SERVICES.map((opt) => {
              const selected = relatedService === opt.value
              return (
                <button
                  key={opt.value}
                  type="button"
                  className={selected ? 'issue-card is-selected' : 'issue-card'}
                  title={opt.hint}
                  onClick={() => setRelatedService(opt.value)}
                >
                  <span className="issue-card__icon" aria-hidden>
                    <svg width="22" height="22" viewBox="0 0 24 24">
                      <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <path d="M12 16v-4M12 8h.01" />
                      </g>
                    </svg>
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
            <span>Your question</span>
            <textarea
              rows={5}
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              required
              placeholder="What do you need help with?"
            />
          </label>
        </div>

        {err && <p className="error-banner">{err}</p>}

        <div className="bottom-action">
          <button type="submit" className="btn keeply-submit" disabled={busy}>
            {busy ? 'Sending…' : 'Submit question'}
          </button>
        </div>
      </form>
    </div>
  )
}
