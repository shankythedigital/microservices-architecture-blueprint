import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getAssetById } from '../api/assetsApi'
import { ApiError } from '../api/http'
import type { AssetRecord } from '../api/assetsApi'

export function AssetDetailPage() {
  const { id } = useParams()
  const { token } = useAuth()
  const [asset, setAsset] = useState<AssetRecord | null>(null)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token || !id) return
    let cancelled = false
    ;(async () => {
      try {
        const res = await getAssetById(token, Number(id))
        if (!cancelled) {
          setAsset(res.data ?? null)
          setErr(null)
        }
      } catch (e) {
        if (!cancelled) setErr(e instanceof ApiError ? e.message : 'Not found')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token, id])

  if (!id) return null

  return (
    <div className="page-pad">
      <Link to="/home/assets" className="back-link">
        ← Assets
      </Link>
      <h1>{asset?.assetNameUdv || 'Appliance'}</h1>
      {err && <p className="error-banner">{err}</p>}
      {!asset && !err && <p className="muted">Loading…</p>}
      {asset && (
        <>
          <section className="sheet">
            <h2>Details</h2>
            <dl className="dl-grid">
              <dt>Status</dt>
              <dd>{asset.assetStatus ?? '—'}</dd>
              <dt>Category</dt>
              <dd>{asset.categoryName ?? '—'}</dd>
              <dt>Subcategory</dt>
              <dd>{asset.subCategoryName ?? '—'}</dd>
              <dt>Brand</dt>
              <dd>{asset.makeName ?? '—'}</dd>
              <dt>Model</dt>
              <dd>{asset.modelName ?? '—'}</dd>
            </dl>
          </section>
          <section className="sheet warranty-card" style={{ marginTop: '0.85rem' }}>
            <h2>Warranty &amp; docs</h2>
            <p className="muted small">
              FR-21–24 — expiry reminders and renewal are driven by asset + notification services; wire document upload to
              /api/asset/v1/documents when ready.
            </p>
            <button type="button" className="btn secondary" disabled title="Product roadmap">
              Renew warranty
            </button>
          </section>
          <p>
            <Link to={`/home/issues/new?assetId=${id}`} className="btn keeply-submit block" style={{ textDecoration: 'none' }}>
              Raise a service issue
            </Link>
          </p>
        </>
      )}
    </div>
  )
}
