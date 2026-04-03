import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listCategories, type CategoryDto } from '../api/categoriesApi'
import { createIssue, createIssueMaster, listIssueMasters, type IssueMasterItem } from '../api/issuesApi'
import { ApiError } from '../api/http'

type Mode = 'catalog' | 'custom'

export function NewIssuePage() {
  const { token, userId } = useAuth()
  const [sp] = useSearchParams()
  const assetIdStr = sp.get('assetId')
  const nav = useNavigate()

  const [masters, setMasters] = useState<IssueMasterItem[]>([])
  const [mastersErr, setMastersErr] = useState<string | null>(null)
  const [categories, setCategories] = useState<CategoryDto[]>([])

  const [newCatalogTitle, setNewCatalogTitle] = useState('')
  const [newCatalogDescription, setNewCatalogDescription] = useState('')
  const [newCatalogCategoryId, setNewCatalogCategoryId] = useState<number | ''>('')
  const [addCatalogBusy, setAddCatalogBusy] = useState(false)
  const [addCatalogErr, setAddCatalogErr] = useState<string | null>(null)
  const [addCatalogOk, setAddCatalogOk] = useState<string | null>(null)
  /** Issue-master creation form: opened via header icon, not shown by default */
  const [showCatalogForm, setShowCatalogForm] = useState(false)
  const [mode, setMode] = useState<Mode>('catalog')
  const [issueMasterId, setIssueMasterId] = useState<number | null>(null)
  const [extraNote, setExtraNote] = useState('')

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('Electrical')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let c = false
    ;(async () => {
      const [catRes, masterRes] = await Promise.allSettled([
        listCategories(token),
        listIssueMasters(token),
      ])
      if (c) return
      if (catRes.status === 'fulfilled') {
        const d = catRes.value?.data
        setCategories(Array.isArray(d) ? d : [])
      } else {
        setCategories([])
      }
      if (masterRes.status === 'fulfilled') {
        const list = masterRes.value
        setMasters(Array.isArray(list) ? list : [])
        setMastersErr(null)
      } else {
        setMasters([])
        const e = masterRes.reason
        setMastersErr(e instanceof ApiError ? e.message : 'Could not load issue catalog')
      }
    })()
    return () => {
      c = true
    }
  }, [token])

  async function onAddCatalogEntry() {
    setAddCatalogErr(null)
    setAddCatalogOk(null)
    if (!token) {
      setAddCatalogErr('You are not signed in.')
      return
    }
    const t = newCatalogTitle.trim()
    if (!t) {
      setAddCatalogErr('Enter a title for the new catalog issue.')
      return
    }
    if (newCatalogCategoryId === '' || !Number.isFinite(Number(newCatalogCategoryId))) {
      setAddCatalogErr('Choose a product category — required to save an issue type.')
      return
    }
    setAddCatalogBusy(true)
    try {
      const created = await createIssueMaster(token, {
        issueTitle: t,
        issueDescription: newCatalogDescription.trim() || undefined,
        categoryId: Number(newCatalogCategoryId),
      })
      const list = await listIssueMasters(token)
      setMasters(Array.isArray(list) ? list : [])
      if (created?.id != null) {
        setIssueMasterId(created.id)
      }
      setNewCatalogTitle('')
      setNewCatalogDescription('')
      setNewCatalogCategoryId('')
      setShowCatalogForm(false)
      setAddCatalogOk('Saved to catalog. It is selected below — add details and submit if you want a ticket.')
    } catch (e) {
      setAddCatalogErr(e instanceof ApiError ? e.message : 'Could not save catalog entry')
    } finally {
      setAddCatalogBusy(false)
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setErr(null)
    if (!token) {
      setErr('You are not signed in.')
      return
    }
    if (userId == null || !Number.isFinite(Number(userId))) {
      setErr('Your account ID is missing — sign out and sign in again, then retry.')
      return
    }
    if (mode === 'catalog' && issueMasterId == null) {
      setErr('Choose an issue type from the catalog, or switch to Custom.')
      return
    }
    if (mode === 'custom') {
      const t = title.trim()
      const d = description.trim()
      if (!t || !d) {
        setErr('Please enter a summary and description.')
        return
      }
      if (t.length > 200) {
        setErr('Summary must be 200 characters or fewer.')
        return
      }
      if (d.length > 4000) {
        setErr('Description must be 4000 characters or fewer.')
        return
      }
    }
    if (mode === 'catalog' && extraNote.length > 4000) {
      setErr('Additional details must be 4000 characters or fewer.')
      return
    }

    setBusy(true)
    try {
      const rawAsset = assetIdStr ? Number(assetIdStr) : NaN
      const assetId = Number.isFinite(rawAsset) ? rawAsset : undefined

      const created =
        mode === 'catalog' && issueMasterId != null
          ? await createIssue(token, {
              issueMasterId,
              priority: 'MEDIUM',
              relatedService: 'ASSET_SERVICE',
              assetId,
              description: extraNote.trim() || undefined,
            })
          : await createIssue(token, {
              title: `${category}: ${title.trim()}`,
              description: description.trim(),
              priority: 'MEDIUM',
              relatedService: 'ASSET_SERVICE',
              assetId,
            })

      // helpdesk-service sets reportedBy from JWT subject (user id) so the ticket is linked for /my-issues
      if (created?.id != null) {
        nav(`/home/issues/${created.id}`)
      } else {
        nav('/home/issues')
      }
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Could not create issue')
    } finally {
      setBusy(false)
    }
  }

  const catalogValid = issueMasterId != null
  const customValid = title.trim() !== '' && description.trim() !== ''
  const canSubmit = mode === 'catalog' ? catalogValid : customValid

  return (
    <div className="page-pad">
      <Link to="/home/issues" className="back-link">
        ← Issues
      </Link>
      <h1>Raise an issue</h1>
      <p className="muted small">Create a ticket via the helpdesk catalog or describe a custom problem.</p>

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
          <Link to="/home/assets" className="asset-pill__change muted small">
            Change
          </Link>
        </div>
      )}

      <div className="segmented" style={{ margin: '0.75rem 0' }}>
        <button
          type="button"
          className={mode === 'catalog' ? 'is-on' : ''}
          onClick={() => {
            setMode('catalog')
            setErr(null)
            setShowCatalogForm(false)
          }}
        >
          From catalog
        </button>
        <button
          type="button"
          className={mode === 'custom' ? 'is-on' : ''}
          onClick={() => {
            setMode('custom')
            setIssueMasterId(null)
            setErr(null)
          }}
        >
          Custom
        </button>
      </div>

      <form onSubmit={onSubmit} className="stack" style={{ paddingBottom: '5.25rem' }}>
        {mode === 'catalog' && (
          <div className="sheet">
            <div className="catalog-section-head">
              <div>
                <h2 className="section-title" style={{ marginTop: 0, marginBottom: '0.25rem' }}>
                  Common issues
                </h2>
                <p className="muted small" style={{ margin: 0 }}>
                  Pick a predefined type, or use <strong>Add to catalog</strong> to create a new issue_master entry.
                </p>
              </div>
              <button
                type="button"
                className={`catalog-add-icon-btn${showCatalogForm ? ' is-active' : ''}`}
                onClick={() => {
                  setAddCatalogErr(null)
                  setShowCatalogForm((wasOpen) => {
                    if (wasOpen) setAddCatalogOk(null)
                    return !wasOpen
                  })
                }}
                aria-expanded={showCatalogForm}
                aria-label={showCatalogForm ? 'Close add to catalog form' : 'Add new issue type to catalog'}
                title={showCatalogForm ? 'Close' : 'Add issue to catalog'}
              >
                <span className="catalog-add-icon-btn__icon" aria-hidden>
                  <svg width="20" height="20" viewBox="0 0 24 24">
                    <g fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M4 7h10l2-2h6v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7z" />
                      <path d="M6 11h12M12 8v6" />
                    </g>
                  </svg>
                </span>
                <span className="catalog-add-icon-btn__title">{showCatalogForm ? 'Close' : 'Add to catalog'}</span>
              </button>
            </div>
            {mastersErr && <p className="error-banner">{mastersErr}</p>}
            {addCatalogOk && (
              <p className="muted small" style={{ marginTop: '0.5rem' }}>
                {addCatalogOk}
              </p>
            )}
            {!mastersErr && masters.length === 0 && (
              <p className="muted small">No catalog entries yet — use Add to catalog above or switch to Custom.</p>
            )}

            {showCatalogForm && (
              <div
                className="catalog-form-panel sheet"
                style={{ marginTop: '0.75rem', background: 'var(--surface-2, rgba(0,0,0,0.04))' }}
              >
                <h3 className="section-title" style={{ marginTop: 0, fontSize: '1rem' }}>
                  New catalog issue type
                </h3>
                <p className="muted small" style={{ marginTop: 0 }}>
                  Saves to helpdesk issue_master. A product category is required.
                </p>
                <label className="field">
                  <span>Title</span>
                  <input
                    value={newCatalogTitle}
                    onChange={(e) => setNewCatalogTitle(e.target.value)}
                    placeholder="e.g. Repair — display not working"
                    maxLength={255}
                  />
                </label>
                <label className="field">
                  <span>Description (optional)</span>
                  <textarea
                    rows={2}
                    value={newCatalogDescription}
                    onChange={(e) => setNewCatalogDescription(e.target.value)}
                    placeholder="What this issue type means for agents"
                  />
                </label>
                <label className="field">
                  <span>Product category</span>
                  <select
                    value={newCatalogCategoryId === '' ? '' : String(newCatalogCategoryId)}
                    onChange={(e) => {
                      const v = e.target.value
                      setNewCatalogCategoryId(v === '' ? '' : Number(v))
                    }}
                  >
                    <option value="">Select category…</option>
                    {categories.map((cat) => {
                      const id = cat.categoryId
                      if (id == null) return null
                      return (
                        <option key={id} value={id}>
                          {cat.categoryName ?? `Category ${id}`}
                        </option>
                      )
                    })}
                  </select>
                </label>
                {categories.length === 0 && (
                  <p className="muted small">No categories loaded — check asset-service or try again later.</p>
                )}
                {addCatalogErr && <p className="error-banner">{addCatalogErr}</p>}
                <button
                  type="button"
                  className="btn"
                  onClick={onAddCatalogEntry}
                  disabled={addCatalogBusy || categories.length === 0}
                >
                  {addCatalogBusy ? 'Saving…' : 'Save to catalog'}
                </button>
              </div>
            )}

            <ul className="plain-list issue-master-list">
              {masters.map((m) => {
                const id = m.id
                if (id == null) return null
                const selected = issueMasterId === id
                return (
                  <li key={id}>
                    <button
                      type="button"
                      className={selected ? 'issue-master-pick is-selected' : 'issue-master-pick'}
                      onClick={() => setIssueMasterId(id)}
                    >
                      <strong>{m.issueTitle || `Issue #${id}`}</strong>
                      {m.issueDescription && <span className="muted small">{m.issueDescription}</span>}
                    </button>
                  </li>
                )
              })}
            </ul>
            <label className="field">
              <span>Additional details (optional)</span>
              <textarea rows={3} value={extraNote} onChange={(e) => setExtraNote(e.target.value)} />
            </label>
          </div>
        )}

        {mode === 'custom' && (
          <>
            <div>
              <h2 className="section-title">Select a category</h2>
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
                <input value={title} onChange={(e) => setTitle(e.target.value)} required={mode === 'custom'} />
              </label>
              <div style={{ height: 10 }} />
              <label className="field">
                <span>Description</span>
                <textarea rows={4} value={description} onChange={(e) => setDescription(e.target.value)} required={mode === 'custom'} />
              </label>
            </div>
          </>
        )}

        {err && <p className="error-banner">{err}</p>}

        <div className="bottom-action">
          <button type="submit" className="btn keeply-submit" disabled={busy || !canSubmit}>
            {busy ? 'Submitting…' : 'Submit'}
          </button>
        </div>
      </form>
    </div>
  )
}
