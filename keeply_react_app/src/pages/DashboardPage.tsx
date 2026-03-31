import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { searchAssets } from '../api/assetsApi'
import { notificationCount } from '../api/notificationsApi'
import { listIssues } from '../api/issuesApi'
import { ApiError, apiJson } from '../api/http'
import { url } from '../config'
import type { AssetRecord } from '../api/assetsApi'
import type { ResponseWrapper } from '../api/types'

const ROOM_FILTERS = ['All', 'Kitchen', 'Living room', 'Laundry', 'Other']

type CategoryDto = {
  categoryId?: number
  categoryName?: string
  description?: string
}

export function DashboardPage() {
  const { token } = useAuth()
  const [assets, setAssets] = useState<AssetRecord[]>([])
  const [alertCount, setAlertCount] = useState<number | null>(null)
  const [issueOpen, setIssueOpen] = useState<number | null>(null)
  const [room, setRoom] = useState('All')
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    ;(async () => {
      try {
        setErr(null)
        const [cats, page, nc, issues] = await Promise.all([
          apiJson<ResponseWrapper<CategoryDto[]>>(url('asset', '/api/asset/v1/categories'), {
            token,
          }).catch((e) => console.warn(e)),
          searchAssets(token, { page: 0, size: 50 }),
          notificationCount(token).catch(() => null),
          listIssues(token).catch(() => []),
        ])
        if (cancelled) return
        void cats
        setAssets(page.data?.content ?? [])
        if (nc != null) setAlertCount(nc)
        setIssueOpen(
          Array.isArray(issues) ? issues.filter((i) => i.status && i.status !== 'CLOSED').length : 0,
        )
      } catch (e) {
        if (!cancelled) setErr(e instanceof ApiError ? e.message : 'Could not load home')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token])

  const filtered = useMemo(() => {
    if (room === 'All') return assets
    const r = room.toLowerCase()
    return assets.filter((a) => (a.categoryName || '').toLowerCase().includes(r.slice(0, 4)))
  }, [assets, room])

  return (
    <div className="page-pad">
      <div className="hero">
        <div className="hero__brand" aria-hidden>
          <span className="hero__mark" />
        </div>
        <div className="hero__row">
          <div>
            <h1 className="hero__title">Manage your home assets</h1>
            <p className="muted">Keep your appliances organized with reminders for service and warranties.</p>
          </div>
          <div className="hero__art" aria-hidden />
        </div>
      </div>
      {err && <p className="error-banner">{err}</p>}

      <section className="cta-card" aria-label="Primary action">
        <Link to="/home/assets/add" className="cta-card__inner">
          <span className="cta-card__plus" aria-hidden>
            +
          </span>
          <span>Add new assets</span>
        </Link>
      </section>

      <section className="banner alert-banner" aria-label="Highlights">
        <div>
          <strong>Upcoming reminders</strong>
          <p className="muted small">
            Alerts in the current window{' '}
            {alertCount != null ? (
              <><strong>{alertCount}</strong> in-app items · </>
            ) : (
              '— · '
            )}
            <Link to="/home/alerts">Open alerts</Link>
          </p>
        </div>
        <Link to="/home/issues" className="btn secondary tight">
          Service issues {issueOpen != null ? `(${issueOpen})` : ''}
        </Link>
      </section>

      <h2 className="section-title">Rooms</h2>
      <div className="chip-row" role="tablist" aria-label="Room filter">
        {ROOM_FILTERS.map((label) => (
          <button
            key={label}
            type="button"
            className={`chip ${room === label ? 'is-active' : ''}`}
            onClick={() => setRoom(label)}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="row-split">
        <h2 className="section-title">My Assets</h2>
        <Link to="/home/assets" className="muted small">
          View all
        </Link>
      </div>
      <div className="asset-grid">
        {filtered.length === 0 && <p className="muted">No assets in this filter yet.</p>}
        {filtered.map((a) => (
          <Link
            key={a.assetId ?? JSON.stringify(a)}
            to={a.assetId ? `/home/assets/${a.assetId}` : '/home/assets'}
            className="asset-card"
          >
            <span className="asset-card__icon" aria-hidden>
              <svg width="44" height="44" viewBox="0 0 48 48">
                <g fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="10" y="14" width="28" height="20" rx="3" />
                  <path d="M16 26h16" />
                  <path d="M18 18h12" />
                  <path d="M34 18h0" />
                </g>
              </svg>
            </span>
            <span className="asset-card__divider" aria-hidden />
            <span className="asset-card__name">{a.assetNameUdv || 'Unnamed appliance'}</span>
            <span className="muted small">
              {a.categoryName}
              {a.subCategoryName ? ` · ${a.subCategoryName}` : ''}
            </span>
            {a.makeName && <span className="small">{a.makeName}</span>}
          </Link>
        ))}
      </div>

      <div style={{ height: 10 }} />
    </div>
  )
}
