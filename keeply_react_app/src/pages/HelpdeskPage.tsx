import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listFaqs, markFaqHelpful, searchFaqs, type FaqItem } from '../api/faqsApi'
import { listMyIssues } from '../api/issuesApi'
import { listMyQueries } from '../api/queriesApi'
import { ApiError } from '../api/http'

export function HelpdeskPage() {
  const { token } = useAuth()
  const [issueCount, setIssueCount] = useState<number | null>(null)
  const [queryCount, setQueryCount] = useState<number | null>(null)
  const [faqPreview, setFaqPreview] = useState<FaqItem[]>([])
  const [searchQ, setSearchQ] = useState('')
  const [searchResults, setSearchResults] = useState<FaqItem[] | null>(null)
  const [searchBusy, setSearchBusy] = useState(false)
  const [helpfulBusyId, setHelpfulBusyId] = useState<number | null>(null)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let c = false
    ;(async () => {
      try {
        const [issues, queries, faqs] = await Promise.all([
          listMyIssues(token).catch(() => []),
          listMyQueries(token).catch(() => []),
          listFaqs(token).catch(() => []),
        ])
        if (c) return
        setIssueCount(issues.length)
        setQueryCount(queries.length)
        setFaqPreview((faqs ?? []).slice(0, 5))
        setErr(null)
      } catch (e) {
        if (!c) setErr(e instanceof ApiError ? e.message : 'Could not load helpdesk')
      }
    })()
    return () => {
      c = true
    }
  }, [token])

  async function onSearch(e: FormEvent) {
    e.preventDefault()
    if (!token || !searchQ.trim()) {
      setSearchResults(null)
      return
    }
    setSearchBusy(true)
    setErr(null)
    try {
      setSearchResults(await searchFaqs(token, searchQ.trim()))
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Search failed')
      setSearchResults([])
    } finally {
      setSearchBusy(false)
    }
  }

  async function onHelpful(id: number | undefined) {
    if (!token || id == null) return
    setHelpfulBusyId(id)
    try {
      await markFaqHelpful(token, id)
      setFaqPreview((prev) => prev.map((f) => (f.id === id ? { ...f, helpfulCount: (f.helpfulCount ?? 0) + 1 } : f)))
      setSearchResults((prev) =>
        prev ? prev.map((f) => (f.id === id ? { ...f, helpfulCount: (f.helpfulCount ?? 0) + 1 } : f)) : prev,
      )
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Could not record feedback')
    } finally {
      setHelpfulBusyId(null)
    }
  }

  const showResults = searchResults !== null

  return (
    <div className="page-pad">
      <Link to="/home" className="back-link">
        ← Home
      </Link>
      <h1>Help &amp; support</h1>
      <p className="muted small">
        Service tickets, expert Q&amp;A, and FAQs from the helpdesk — scoped to your signed-in account where applicable.
      </p>

      {err && <p className="error-banner">{err}</p>}

      <div className="helpdesk-stats">
        <Link to="/home/issues" className="helpdesk-stat sheet">
          <span className="helpdesk-stat__value">{issueCount != null ? issueCount : '—'}</span>
          <span className="helpdesk-stat__label">My tickets</span>
        </Link>
        <Link to="/home/helpdesk/queries" className="helpdesk-stat sheet">
          <span className="helpdesk-stat__value">{queryCount != null ? queryCount : '—'}</span>
          <span className="helpdesk-stat__label">My questions</span>
        </Link>
      </div>

      <nav className="helpdesk-actions" aria-label="Support actions">
        <Link to="/home/issues/new" className="list-link helpdesk-action">
          <strong>Raise a service issue</strong>
          <span className="muted small">Appliance problems, repairs, warranty — creates a tracked ticket.</span>
        </Link>
        <Link to="/home/helpdesk/queries/new" className="list-link helpdesk-action">
          <strong>Ask a question</strong>
          <span className="muted small">Submit a question to support (separate from a repair ticket).</span>
        </Link>
        <Link to="/home/tips" className="list-link helpdesk-action">
          <strong>Videos &amp; community tips</strong>
          <span className="muted small">Know-how articles and recommended clips.</span>
        </Link>
      </nav>

      <section className="sheet" aria-label="FAQ search">
        <h2 className="section-title" style={{ marginTop: 0 }}>
          Search FAQs
        </h2>
        <form onSubmit={onSearch} className="helpdesk-search">
          <label className="field">
            <span className="muted small">Keyword</span>
            <input
              value={searchQ}
              onChange={(e) => setSearchQ(e.target.value)}
              placeholder="e.g. warranty, installation"
            />
          </label>
          <button type="submit" className="btn primary tight" disabled={searchBusy || !searchQ.trim()}>
            {searchBusy ? 'Searching…' : 'Search'}
          </button>
        </form>
        {showResults && (
          <ul className="faq-list helpdesk-faq-results">
            {searchResults.length === 0 && <li className="muted">No FAQs match that keyword.</li>}
            {searchResults.map((f) => (
              <li key={f.id ?? f.question} className="faq-item helpdesk-faq-item">
                <strong>{f.question}</strong>
                {f.answer && <p className="muted small">{f.answer}</p>}
                <div className="helpdesk-faq-meta">
                  {f.category && <span className="pill pill--muted">{f.category}</span>}
                  {f.helpfulCount != null && <span className="muted small">{f.helpfulCount} found helpful</span>}
                  {f.id != null && (
                    <button
                      type="button"
                      className="btn ghost tight"
                      disabled={helpfulBusyId === f.id}
                      onClick={() => onHelpful(f.id)}
                    >
                      {helpfulBusyId === f.id ? 'Thanks…' : 'Helpful'}
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      {!showResults && faqPreview.length > 0 && (
        <section className="sheet" aria-label="Recent FAQs">
          <h2 className="section-title" style={{ marginTop: 0 }}>
            From the FAQ library
          </h2>
          <ul className="faq-list">
            {faqPreview.map((f) => (
              <li key={f.id ?? f.question} className="faq-item">
                <strong>{f.question}</strong>
                {f.answer && <p className="muted small">{f.answer}</p>}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
