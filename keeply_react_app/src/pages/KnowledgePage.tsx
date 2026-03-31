import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listFaqs } from '../api/faqsApi'
import { ApiError } from '../api/http'
import type { FaqItem } from '../api/faqsApi'

export function KnowledgePage() {
  const { token } = useAuth()
  const [faqs, setFaqs] = useState<FaqItem[]>([])
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    ;(async () => {
      try {
        setFaqs(await listFaqs(token))
        setErr(null)
      } catch (e) {
        setErr(e instanceof ApiError ? e.message : 'Could not load tips')
      }
    })()
  }, [token])

  return (
    <div className="page-pad">
      <h1>Devices – Know How’s</h1>
      <p className="muted small">Recommended videos and quick articles.</p>
      <p className="muted small">
        Need help? <Link to="/home/issues">Raise an issue</Link>
      </p>
      {err && <p className="error-banner">{err}</p>}

      <h2 className="section-title">Recommended for you</h2>
      <div className="media-row" role="list">
        {[1, 2, 3].map((n) => (
          <div key={n} className="media-card" role="listitem" aria-label="Video">
            <div className="media-card__thumb" aria-hidden>
              <span className="media-card__brand">Keeply</span>
              <span className="media-card__play" aria-hidden>
                ▶
              </span>
              <span className="media-card__avatar" aria-hidden />
            </div>
            <div className="media-card__name">Creator</div>
          </div>
        ))}
      </div>

      <h2 className="section-title" style={{ marginTop: '.75rem' }}>
        Keeply by others
      </h2>
      <div className="sheet feed-card">
        <div className="feed-card__thumb" aria-hidden>
          <span className="media-card__play" aria-hidden>
            ▶
          </span>
        </div>
        <div className="feed-card__body">
          <div className="feed-card__row">
            <span className="feed-card__avatar" aria-hidden />
            <div>
              <strong>Lorem ipsum dolor sit amet consectetur</strong>
              <div className="muted small">24 June, 2025</div>
            </div>
          </div>
        </div>
      </div>

      <ul className="faq-list">
        {faqs.map((f) => (
          <li key={f.id} className="faq-item">
            <strong>{f.question}</strong>
            {f.answer && <p className="muted small">{f.answer}</p>}
          </li>
        ))}
      </ul>
      {faqs.length === 0 && !err && <p className="muted">No articles yet.</p>}
    </div>
  )
}
