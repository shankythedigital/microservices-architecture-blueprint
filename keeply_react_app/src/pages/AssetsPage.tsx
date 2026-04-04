import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { searchAssets } from '../api/assetsApi'
import { ApiError } from '../api/http'
import { assetListThumbnailUrl, type AssetRecord } from '../api/assetsApi'
import { AuthenticatedDocImage } from '../components/AuthenticatedDocImage'
import { ResponsiveImage } from '../components/ResponsiveImage'
import { useKeeplyPreferences } from '../hooks/useKeeplyPreferences'

export function AssetsPage() {
  const { token } = useAuth()
  const { showListThumbnails } = useKeeplyPreferences()
  const [keyword, setKeyword] = useState('')
  const [items, setItems] = useState<AssetRecord[]>([])
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  async function load(p: number, kw: string) {
    if (!token) return
    setLoading(true)
    setErr(null)
    try {
      const res = await searchAssets(token, { keyword: kw || undefined, page: p, size: 12 })
      setItems(res.data?.content ?? [])
      setTotal(res.data?.totalElements ?? 0)
      setPage(p)
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Asset search failed')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(0, keyword)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  function onSearch(e: FormEvent) {
    e.preventDefault()
    load(0, keyword)
  }

  const titleOf = (a: AssetRecord) => a.assetNameUdv || `Asset ${a.assetId ?? ''}`

  const categories = useMemo(() => {
    const s = new Set<string>()
    items.forEach((a) => {
      if (a.categoryName) s.add(a.categoryName)
    })
    return ['All', ...[...s].sort()]
  }, [items])
  const [cat, setCat] = useState('All')
  const visible = useMemo(
    () => (cat === 'All' ? items : items.filter((a) => a.categoryName === cat)),
    [items, cat],
  )

  return (
    <div className="page-pad">
      <div className="row-split">
        <div>
          <h1>My appliances</h1>
          <p className="muted small">Search, filter, and open appliance details.</p>
        </div>
        <span className="muted small">{total || 0}</span>
      </div>
      <form onSubmit={onSearch} className="toolbar">
        <input
          placeholder="Search by name, model, category…"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="grow"
        />
        <button type="submit" className="btn primary" disabled={loading}>
          {loading ? 'Searching…' : 'Search'}
        </button>
      </form>
      {err && <p className="error-banner">{err}</p>}

      <div className="chip-row chips-scroll" aria-label="Rooms / categories">
        {categories.map((c) => (
          <button
            key={c}
            type="button"
            className={`chip ${cat === c ? 'is-active' : ''}`}
            onClick={() => setCat(c)}
          >
            {c}
          </button>
        ))}
      </div>

      <h2 className="section-title">{cat === 'All' ? 'All assets' : cat}</h2>
      <div className="asset-grid asset-grid--cards">
        {visible.map((a) => (
          <Link
            key={String(a.assetId ?? titleOf(a))}
            to={a.assetId ? `/home/assets/${a.assetId}` : '#'}
            className="asset-card"
          >
            {showListThumbnails &&
            (assetListThumbnailUrl(a) || (a.assetPhotoDocumentId != null && token)) ? (
              assetListThumbnailUrl(a) ? (
                <ResponsiveImage
                  src={assetListThumbnailUrl(a)!}
                  alt={titleOf(a)}
                  className="asset-card__thumb"
                />
              ) : (
                <AuthenticatedDocImage
                  token={token!}
                  documentId={a.assetPhotoDocumentId!}
                  docTypeHint="asset_photo"
                  alt={titleOf(a)}
                  className="asset-card__thumb"
                />
              )
            ) : (
              <span className="asset-card__icon" aria-hidden>
                <svg width="46" height="46" viewBox="0 0 48 48">
                  <g fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="10" y="14" width="28" height="20" rx="3" />
                    <path d="M16 26h16" />
                    <path d="M18 18h12" />
                    <path d="M34 18h0" />
                  </g>
                </svg>
              </span>
            )}
            <span className="asset-card__divider" aria-hidden />
            <span className="asset-card__name">{titleOf(a)}</span>
            <span className="muted small">
              {a.categoryName || '—'}
              {a.subCategoryName ? ` · ${a.subCategoryName}` : ''}
            </span>
          </Link>
        ))}
      </div>

      <div className="pager">
        <button
          type="button"
          className="btn ghost"
          disabled={loading || page <= 0}
          onClick={() => load(page - 1, keyword)}
        >
          Previous
        </button>
        <span className="muted small">
          {total} total
        </span>
        <button
          type="button"
          className="btn ghost"
          disabled={loading || (page + 1) * 12 >= total}
          onClick={() => load(page + 1, keyword)}
        >
          Next
        </button>
      </div>

      <p className="muted small">
        Tip: try searching by serial number or model name.
      </p>
    </div>
  )
}
