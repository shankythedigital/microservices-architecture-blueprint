import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { createFaq, listFaqs, type FaqItem } from '../api/faqsApi'
import {
  createKnowledge,
  listKnowledgeByService,
  searchKnowledge,
  type ServiceKnowledgeItem,
} from '../api/knowledgeApi'
import { ApiError } from '../api/http'
import { HELP_DESK_RELATED_SERVICES, type RelatedService } from '../constants/helpdesk'

function excerpt(text: string | undefined, max = 280): string {
  if (!text || !text.trim()) return ''
  const t = text.trim().replace(/\s+/g, ' ')
  return t.length <= max ? t : `${t.slice(0, max)}…`
}

function serviceLabel(s: RelatedService): string {
  return HELP_DESK_RELATED_SERVICES.find((x) => x.value === s)?.label ?? s.replaceAll('_', ' ')
}

export function KnowledgePage() {
  const { token } = useAuth()
  const [faqs, setFaqs] = useState<FaqItem[]>([])
  const [knowledge, setKnowledge] = useState<ServiceKnowledgeItem[]>([])
  const [knowledgeScope, setKnowledgeScope] = useState<RelatedService>('ASSET_SERVICE')
  const [kSearch, setKSearch] = useState('')
  const [kResults, setKResults] = useState<ServiceKnowledgeItem[] | null>(null)
  const [kBusy, setKBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  const [showFaqForm, setShowFaqForm] = useState(false)
  const [faqQuestion, setFaqQuestion] = useState('')
  const [faqAnswer, setFaqAnswer] = useState('')
  const [faqCategory, setFaqCategory] = useState('General')
  const [faqService, setFaqService] = useState<RelatedService>('ASSET_SERVICE')
  const [faqBusy, setFaqBusy] = useState(false)
  const [faqErr, setFaqErr] = useState<string | null>(null)
  const [faqOk, setFaqOk] = useState<string | null>(null)

  const [showKnForm, setShowKnForm] = useState(false)
  const [knTopic, setKnTopic] = useState('')
  const [knContent, setKnContent] = useState('')
  const [knCategory, setKnCategory] = useState('How-to')
  const [knService, setKnService] = useState<RelatedService>('ASSET_SERVICE')
  const [knApiEndpoints, setKnApiEndpoints] = useState('')
  const [knCommonIssues, setKnCommonIssues] = useState('')
  const [knTroubleshooting, setKnTroubleshooting] = useState('')
  const [knBusy, setKnBusy] = useState(false)
  const [knErr, setKnErr] = useState<string | null>(null)
  const [knOk, setKnOk] = useState<string | null>(null)

  const refreshFaqs = useCallback(async () => {
    if (!token) return
    try {
      const list = await listFaqs(token)
      setFaqs(Array.isArray(list) ? list : [])
    } catch {
      setFaqs([])
    }
  }, [token])

  const refreshKnowledge = useCallback(
    async (scope: RelatedService) => {
      if (!token) return
      try {
        const list = await listKnowledgeByService(token, scope)
        setKnowledge(Array.isArray(list) ? list : [])
      } catch {
        setKnowledge([])
      }
    },
    [token],
  )

  useEffect(() => {
    if (!token) return
    let c = false
    ;(async () => {
      try {
        const f = await listFaqs(token).catch(() => [])
        if (c) return
        setFaqs(Array.isArray(f) ? f : [])
        setErr(null)
      } catch (e) {
        if (!c) setErr(e instanceof ApiError ? e.message : 'Could not load tips')
      }
    })()
    return () => {
      c = true
    }
  }, [token])

  useEffect(() => {
    if (!token) return
    let c = false
    ;(async () => {
      try {
        const k = await listKnowledgeByService(token, knowledgeScope).catch(() => [])
        if (c) return
        setKnowledge(Array.isArray(k) ? k : [])
        setKResults(null)
        setKSearch('')
      } catch (e) {
        if (!c) setErr(e instanceof ApiError ? e.message : 'Could not load knowledge')
      }
    })()
    return () => {
      c = true
    }
  }, [token, knowledgeScope])

  async function onAddFaq(e: FormEvent) {
    e.preventDefault()
    setFaqErr(null)
    setFaqOk(null)
    if (!token) {
      setFaqErr('You are not signed in.')
      return
    }
    const q = faqQuestion.trim()
    const a = faqAnswer.trim()
    const cat = faqCategory.trim()
    if (!q || !a || !cat) {
      setFaqErr('Question, answer, and category are required.')
      return
    }
    setFaqBusy(true)
    try {
      await createFaq(token, {
        question: q,
        answer: a,
        relatedService: faqService,
        category: cat,
      })
      await refreshFaqs()
      setFaqQuestion('')
      setFaqAnswer('')
      setFaqCategory('General')
      setFaqService('ASSET_SERVICE')
      setShowFaqForm(false)
      setFaqOk('FAQ added to the list below.')
    } catch (e) {
      setFaqErr(e instanceof ApiError ? e.message : 'Could not create FAQ')
    } finally {
      setFaqBusy(false)
    }
  }

  async function onAddKnowledge(e: FormEvent) {
    e.preventDefault()
    setKnErr(null)
    setKnOk(null)
    if (!token) {
      setKnErr('You are not signed in.')
      return
    }
    const topic = knTopic.trim()
    const content = knContent.trim()
    const category = knCategory.trim()
    if (!topic || !content || !category) {
      setKnErr('Topic, content, and category are required.')
      return
    }
    setKnBusy(true)
    try {
      await createKnowledge(token, {
        service: knService,
        topic,
        content,
        category,
        apiEndpoints: knApiEndpoints.trim() || undefined,
        commonIssues: knCommonIssues.trim() || undefined,
        troubleshootingSteps: knTroubleshooting.trim() || undefined,
      })
      if (knService === knowledgeScope) {
        await refreshKnowledge(knowledgeScope)
      }
      setKnTopic('')
      setKnContent('')
      setKnCategory('How-to')
      setKnApiEndpoints('')
      setKnCommonIssues('')
      setKnTroubleshooting('')
      setShowKnForm(false)
      setKnOk(
        knService === knowledgeScope
          ? 'Knowledge article added below.'
          : `Saved for ${serviceLabel(knService)}. Choose that service in “Browse service” to see it in the list.`,
      )
    } catch (e) {
      setKnErr(e instanceof ApiError ? e.message : 'Could not create knowledge entry')
    } finally {
      setKnBusy(false)
    }
  }

  async function onKnowledgeSearch(e: FormEvent) {
    e.preventDefault()
    if (!token || !kSearch.trim()) {
      setKResults(null)
      return
    }
    setKBusy(true)
    setErr(null)
    try {
      setKResults(await searchKnowledge(token, knowledgeScope, kSearch.trim()))
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
      <p className="muted small">FAQs and the service knowledge base from the helpdesk API.</p>
      <p className="muted small">
        Need help? <Link to="/home/helpdesk">Help &amp; support</Link>
        {' · '}
        <Link to="/home/issues">My tickets</Link>
      </p>
      {err && <p className="error-banner">{err}</p>}

      <section className="sheet tips-faq-section" aria-label="Frequently asked questions">
        <div className="catalog-section-head">
          <div>
            <h2 className="section-title" style={{ marginTop: 0, marginBottom: '0.25rem' }}>
              FAQs
            </h2>
            <p className="muted small" style={{ margin: 0 }}>
              Browse common answers or use <strong>Add FAQ</strong> to post a new entry via{' '}
              <code style={{ fontSize: '0.85em' }}>POST /api/helpdesk/faqs</code>.
            </p>
          </div>
          <button
            type="button"
            className={`catalog-add-icon-btn${showFaqForm ? ' is-active' : ''}`}
            onClick={() => {
              setFaqErr(null)
              setShowFaqForm((wasOpen) => {
                if (wasOpen) setFaqOk(null)
                return !wasOpen
              })
            }}
            aria-expanded={showFaqForm}
            aria-label={showFaqForm ? 'Close add FAQ form' : 'Add new FAQ'}
            title={showFaqForm ? 'Close' : 'Add FAQ'}
          >
            <span className="catalog-add-icon-btn__icon" aria-hidden>
              <svg width="20" height="20" viewBox="0 0 24 24">
                <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
                  <path d="M12 17h.01" />
                </g>
              </svg>
            </span>
            <span className="catalog-add-icon-btn__title">{showFaqForm ? 'Close' : 'Add FAQ'}</span>
          </button>
        </div>

        {faqOk && <p className="muted small" style={{ marginTop: '0.5rem' }}>{faqOk}</p>}

        {showFaqForm && (
          <form
            className="catalog-form-panel sheet tips-faq-form"
            onSubmit={onAddFaq}
            style={{ marginTop: '0.75rem', background: 'var(--surface-2, rgba(0,0,0,0.04))' }}
          >
            <h3 className="section-title" style={{ marginTop: 0, fontSize: '1rem' }}>
              New FAQ
            </h3>
            <label className="field">
              <span>Question</span>
              <input
                value={faqQuestion}
                onChange={(e) => setFaqQuestion(e.target.value)}
                placeholder="What users ask"
                maxLength={500}
              />
            </label>
            <label className="field">
              <span>Answer</span>
              <textarea
                rows={4}
                value={faqAnswer}
                onChange={(e) => setFaqAnswer(e.target.value)}
                placeholder="Clear, concise answer"
              />
            </label>
            <label className="field">
              <span>Category</span>
              <input
                value={faqCategory}
                onChange={(e) => setFaqCategory(e.target.value)}
                placeholder="e.g. General, Warranty"
                maxLength={100}
              />
            </label>
            <label className="field">
              <span>Related service</span>
              <select value={faqService} onChange={(e) => setFaqService(e.target.value as RelatedService)}>
                {HELP_DESK_RELATED_SERVICES.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
            {faqErr && <p className="error-banner">{faqErr}</p>}
            <button type="submit" className="btn" disabled={faqBusy}>
              {faqBusy ? 'Saving…' : 'Save FAQ'}
            </button>
          </form>
        )}

        <ul className="faq-list tips-faq-list">
          {faqs.map((f) => (
            <li key={f.id ?? `${f.question}-${f.category}`} className="sheet faq-item faq-item--card">
              <div className="faq-item__head">
                <strong>{f.question || 'Question'}</strong>
                <span className="faq-item__meta">
                  {f.category && <span className="pill pill--muted">{f.category}</span>}
                  {f.relatedService && (
                    <span className="pill pill--muted">{f.relatedService.replaceAll('_', ' ')}</span>
                  )}
                </span>
              </div>
              {f.answer && <p className="muted small faq-item__answer">{f.answer}</p>}
            </li>
          ))}
        </ul>
        {faqs.length === 0 && !err && (
          <p className="muted" style={{ marginTop: '0.75rem' }}>
            No FAQs yet — add one with <strong>Add FAQ</strong> or seed them in helpdeskdb.
          </p>
        )}
      </section>

      <section className="sheet tips-knowledge-section" aria-label="Service knowledge base" style={{ marginTop: '1rem' }}>
        <div className="catalog-section-head tips-knowledge-head">
          <div>
            <h2 className="section-title" style={{ marginTop: 0, marginBottom: '0.25rem' }}>
              Knowledge base
            </h2>
            <p className="muted small" style={{ margin: 0 }}>
              Articles and how-tos per service. Use <strong>Add knowledge</strong> for{' '}
              <code style={{ fontSize: '0.85em' }}>POST /api/helpdesk/knowledge</code>.
            </p>
            <label className="field tips-knowledge-scope" style={{ marginTop: '0.65rem', marginBottom: 0 }}>
              <span className="muted small">Browse service (list &amp; search)</span>
              <select
                value={knowledgeScope}
                onChange={(e) => setKnowledgeScope(e.target.value as RelatedService)}
              >
                {HELP_DESK_RELATED_SERVICES.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <button
            type="button"
            className={`catalog-add-icon-btn${showKnForm ? ' is-active' : ''}`}
            onClick={() => {
              setKnErr(null)
              setKnService(knowledgeScope)
              setShowKnForm((wasOpen) => {
                if (wasOpen) setKnOk(null)
                return !wasOpen
              })
            }}
            aria-expanded={showKnForm}
            aria-label={showKnForm ? 'Close add knowledge form' : 'Add knowledge article'}
            title={showKnForm ? 'Close' : 'Add knowledge'}
          >
            <span className="catalog-add-icon-btn__icon" aria-hidden>
              <svg width="20" height="20" viewBox="0 0 24 24">
                <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                  <path d="M8 7h8M8 11h8M8 15h4" />
                </g>
              </svg>
            </span>
            <span className="catalog-add-icon-btn__title">{showKnForm ? 'Close' : 'Add knowledge'}</span>
          </button>
        </div>

        {knOk && <p className="muted small" style={{ marginTop: '0.5rem' }}>{knOk}</p>}

        {showKnForm && (
          <form
            className="catalog-form-panel sheet tips-knowledge-form"
            onSubmit={onAddKnowledge}
            style={{ marginTop: '0.75rem', background: 'var(--surface-2, rgba(0,0,0,0.04))' }}
          >
            <h3 className="section-title" style={{ marginTop: 0, fontSize: '1rem' }}>
              New knowledge article
            </h3>
            <label className="field">
              <span>Service</span>
              <select value={knService} onChange={(e) => setKnService(e.target.value as RelatedService)}>
                {HELP_DESK_RELATED_SERVICES.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>Topic</span>
              <input
                value={knTopic}
                onChange={(e) => setKnTopic(e.target.value)}
                placeholder="Short title"
                maxLength={255}
              />
            </label>
            <label className="field">
              <span>Category</span>
              <input
                value={knCategory}
                onChange={(e) => setKnCategory(e.target.value)}
                placeholder="e.g. How-to, Reference"
                maxLength={100}
              />
            </label>
            <label className="field">
              <span>Content</span>
              <textarea
                rows={5}
                value={knContent}
                onChange={(e) => setKnContent(e.target.value)}
                placeholder="Main article body"
              />
            </label>
            <label className="field">
              <span>API endpoints (optional)</span>
              <textarea
                rows={2}
                value={knApiEndpoints}
                onChange={(e) => setKnApiEndpoints(e.target.value)}
                placeholder="Paths or notes"
              />
            </label>
            <label className="field">
              <span>Common issues (optional)</span>
              <textarea
                rows={2}
                value={knCommonIssues}
                onChange={(e) => setKnCommonIssues(e.target.value)}
              />
            </label>
            <label className="field">
              <span>Troubleshooting steps (optional)</span>
              <textarea
                rows={2}
                value={knTroubleshooting}
                onChange={(e) => setKnTroubleshooting(e.target.value)}
              />
            </label>
            {knErr && <p className="error-banner">{knErr}</p>}
            <button type="submit" className="btn" disabled={knBusy}>
              {knBusy ? 'Saving…' : 'Save article'}
            </button>
          </form>
        )}

        <form onSubmit={onKnowledgeSearch} className="helpdesk-search knowledge-search" style={{ marginTop: '1rem' }}>
          <label className="field">
            <span className="muted small">Search in {serviceLabel(knowledgeScope)}</span>
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

        <h3 className="section-title" style={{ marginTop: '1.25rem', marginBottom: '0.35rem' }}>
          Articles
        </h3>
        {knowledgeToShow.length === 0 && !err && (
          <p className="muted">
            No articles for <strong>{serviceLabel(knowledgeScope)}</strong> yet — use{' '}
            <strong>Add knowledge</strong> or pick another service above.
          </p>
        )}
        <ul className="plain-list knowledge-list">
          {knowledgeToShow.map((row) => (
            <li key={row.id ?? row.topic} className="sheet knowledge-card">
              <div className="knowledge-card__head">
                <strong>{row.topic || 'Article'}</strong>
                {row.category && <span className="pill pill--muted">{row.category}</span>}
              </div>
              {row.service && (
                <span className="muted small" style={{ display: 'block', marginTop: '0.2rem' }}>
                  {String(row.service).replaceAll('_', ' ')}
                </span>
              )}
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
      </section>
    </div>
  )
}
