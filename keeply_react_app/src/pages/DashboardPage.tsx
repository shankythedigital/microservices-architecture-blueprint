import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import {
  fetchAssetsAssignedToUser,
  getNeedYourAttention,
  searchAssets,
  assetListThumbnailUrl,
  type AssetRecord,
} from '../api/assetsApi'
import { notificationCount } from '../api/notificationsApi'
import { listMyIssues } from '../api/issuesApi'
import { ApiError } from '../api/http'
import { AuthenticatedDocImage } from '../components/AuthenticatedDocImage'
import { ResponsiveImage } from '../components/ResponsiveImage'

const ROOM_FILTERS = ['All', 'Kitchen', 'Living room', 'Laundry', 'Other']

export function DashboardPage() {
  const { token, userId } = useAuth()
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
        const [nc, issues] = await Promise.all([
          notificationCount(token).catch(() => null),
          listMyIssues(token).catch(() => []),
        ])
        if (cancelled) return

        let list: AssetRecord[] = []
        const uid = userId != null && Number.isFinite(Number(userId)) ? Number(userId) : null
        if (uid != null) {
          try {
            list = await fetchAssetsAssignedToUser(token, uid)
          } catch (e) {
            console.warn('fetchAssetsAssignedToUser', e)
          }
        }
        if (list.length === 0) {
          const nya = await getNeedYourAttention(token).catch((e) => {
            console.warn('need-your-attention', e)
            return null
          })
          if (cancelled) return
          const fromNya = nya?.data?.assets
          if (Array.isArray(fromNya) && fromNya.length > 0) {
            list = fromNya as AssetRecord[]
          }
        }
        if (list.length === 0 && uid == null) {
          try {
            const page = await searchAssets(token, { page: 0, size: 50 })
            if (cancelled) return
            list = page.data?.content ?? []
          } catch (e) {
            console.warn('searchAssets', e)
          }
        }
        if (!cancelled) setAssets(list)

        if (nc != null) setAlertCount(nc)
        const active = new Set(['OPEN', 'IN_PROGRESS', 'REOPENED'])
        setIssueOpen(
          Array.isArray(issues)
            ? issues.filter((i) => i.status && active.has(String(i.status))).length
            : 0,
        )
      } catch (e) {
        if (!cancelled) setErr(e instanceof ApiError ? e.message : 'Could not load home')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token, userId])

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
            {' · '}
            <Link to="/home/account">Account</Link>
          </p>
        </div>
        <Link to="/home/issues" className="btn secondary tight">
          Service issues {issueOpen != null ? `(${issueOpen})` : ''}
        </Link>
      </section>

      <Link to="/home/helpdesk" className="list-link helpdesk-dashboard-link">
        <strong>Help &amp; support</strong>
        <span className="muted small">FAQs, ask a question, and ticket options in one place.</span>
      </Link>

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
        {filtered.length === 0 && assets.length === 0 && (
          <p className="muted">
            No appliances are linked to your account yet. Use <strong>Add new assets</strong>—after linking, they
            appear here.
          </p>
        )}
        {filtered.length === 0 && assets.length > 0 && (
          <p className="muted">No assets match this room filter.</p>
        )}
        {filtered.map((a) => (
          <Link
            key={a.assetId ?? JSON.stringify(a)}
            to={a.assetId ? `/home/assets/${a.assetId}` : '/home/assets'}
            className="asset-card"
          >
            {assetListThumbnailUrl(a) || (a.assetPhotoDocumentId != null && token) ? (
              assetListThumbnailUrl(a) ? (
                <ResponsiveImage
                  src={assetListThumbnailUrl(a)!}
                  alt={a.assetNameUdv || 'Asset'}
                  className="asset-card__thumb"
                />
              ) : (
                <AuthenticatedDocImage
                  token={token!}
                  documentId={a.assetPhotoDocumentId!}
                  docTypeHint="asset_photo"
                  alt={a.assetNameUdv || 'Asset'}
                  className="asset-card__thumb"
                />
              )
            ) : (
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
            )}
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
