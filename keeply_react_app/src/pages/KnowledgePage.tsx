import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listFaqs } from '../api/faqsApi'
import { listKnowledgeByService, searchKnowledge, type ServiceKnowledgeItem } from '../api/knowledgeApi'
import { ApiError } from '../api/http'
import type { FaqItem } from '../api/faqsApi'

function excerpt(text: string | undefined, max = 280): string {
  if (!text || !text.trim()) return ''
  const t = text.trim().replace(/\s+/g, ' ')
  return t.length <= max ? t : `${t.slice(0, max)}…`
}

export function KnowledgePage() {
  const { token } = useAuth()
  const [faqs, setFaqs] = useState<FaqItem[]>([])
  const [knowledge, setKnowledge] = useState<ServiceKnowledgeItem[]>([])
  const [kSearch, setKSearch] = useState('')
  const [kResults, setKResults] = useState<ServiceKnowledgeItem[] | null>(null)
  const [kBusy, setKBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let c = false
    ;(async () => {
      try {
        const [f, k] = await Promise.all([
          listFaqs(token).catch(() => []),
          listKnowledgeByService(token, 'ASSET_SERVICE').catch(() => []),
        ])
        if (c) return
        setFaqs(f ?? [])
        setKnowledge(Array.isArray(k) ? k : [])
        setErr(null)
      } catch (e) {
        if (!c) setErr(e instanceof ApiError ? e.message : 'Could not load tips')
      }
    })()
    return () => {
      c = true
    }
  }, [token])

  async function onKnowledgeSearch(e: FormEvent) {
    e.preventDefault()
    if (!token || !kSearch.trim()) {
      setKResults(null)
      return
    }
    setKBusy(true)
    setErr(null)
    try {
      setKResults(await searchKnowledge(token, 'ASSET_SERVICE', kSearch.trim()))
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Knowledge search failed')
      setKResults([])
    } finally {
      setKBusy(false)
    }
  }

  const knowledgeToShow = kResults ?? knowledge

  return (
    <div className="page-pad">
      <h1>Devices – Know How’s</h1>
      <p className="muted small">FAQs and the asset service knowledge base from the helpdesk API.</p>
      <p className="muted small">
        Need help? <Link to="/home/helpdesk">Help &amp; support</Link>
        {' · '}
        <Link to="/home/issues">My tickets</Link>
      </p>
      {err && <p className="error-banner">{err}</p>}

      <section className="sheet" aria-label="Search knowledge base">
        <h2 className="section-title" style={{ marginTop: 0 }}>
          Search knowledge (assets)
        </h2>
        <p className="muted small">Search topics and how-to content scoped to your appliances and asset service.</p>
        <form onSubmit={onKnowledgeSearch} className="helpdesk-search knowledge-search">
          <label className="field">
            <span className="muted small">Keyword</span>
            <input
              value={kSearch}
              onChange={(e) => setKSearch(e.target.value)}
              placeholder="e.g. warranty, asset create"
            />
          </label>
          <button type="submit" className="btn primary tight" disabled={kBusy || !kSearch.trim()}>
            {kBusy ? 'Searching…' : 'Search'}
          </button>
          {kResults !== null && (
            <button
              type="button"
              className="btn ghost tight"
              onClick={() => {
                setKResults(null)
                setKSearch('')
              }}
            >
              Show all
            </button>
          )}
        </form>
      </section>

      <h2 className="section-title">Knowledge articles</h2>
      {knowledgeToShow.length === 0 && !err && (
        <p className="muted">No knowledge articles yet for ASSET_SERVICE. Add entries via helpdesk admin or Postman.</p>
      )}
      <ul className="plain-list knowledge-list">
        {knowledgeToShow.map((row) => (
          <li key={row.id ?? row.topic} className="sheet knowledge-card">
            <div className="knowledge-card__head">
              <strong>{row.topic || 'Article'}</strong>
              {row.category && <span className="pill pill--muted">{row.category}</span>}
            </div>
            {row.content && <p className="muted small knowledge-card__body">{excerpt(row.content)}</p>}
            {row.troubleshootingSteps && (
              <p className="muted small">
                <strong>Troubleshooting:</strong> {excerpt(row.troubleshootingSteps, 200)}
              </p>
            )}
            {row.apiEndpoints && (
              <p className="muted small knowledge-card__mono" title={row.apiEndpoints}>
                API: {excerpt(row.apiEndpoints, 120)}
              </p>
            )}
          </li>
        ))}
      </ul>

      <h2 className="section-title">FAQs</h2>
      <ul className="faq-list">
        {faqs.map((f) => (
          <li key={f.id ?? f.question} className="faq-item">
            <strong>{f.question}</strong>
            {f.answer && <p className="muted small">{f.answer}</p>}
          </li>
        ))}
      </ul>
      {faqs.length === 0 && !err && <p className="muted">No FAQs loaded.</p>}
    </div>
  )
}
