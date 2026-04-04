import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { notificationCount } from '../api/notificationsApi'
import { tokenDisplayInfo } from '../auth/jwtClaims'
import { accountInitialFromProfile } from '../utils/accountDisplay'
import { resolveAuthUploadedFileUrl } from '../utils/uploadedFileUrl'

const HOME = '/home'

function navClass(active: boolean) {
  return active ? 'bottom-nav__link is-active' : 'bottom-nav__link'
}

function Icon({ name, active }: { name: 'home' | 'grid' | 'globe' | 'bell'; active?: boolean }) {
  const stroke = active ? 'var(--nav-active)' : 'var(--nav-ink)'
  const fill = 'none'
  const common = { stroke, fill, strokeWidth: 2, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const }

  if (name === 'home') {
    return (
      <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden>
        <path {...common} d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9.5Z" />
      </svg>
    )
  }
  if (name === 'grid') {
    return (
      <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden>
        <path {...common} d="M6 6h5v5H6V6Zm7 0h5v5h-5V6ZM6 13h5v5H6v-5Zm7 0h5v5h-5v-5Z" />
      </svg>
    )
  }
  if (name === 'globe') {
    return (
      <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden>
        <path {...common} d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z" />
        <path {...common} d="M2 12h20" />
        <path {...common} d="M12 2c3 3.4 3 16.6 0 20-3-3.4-3-16.6 0-20Z" />
      </svg>
    )
  }
  if (name === 'bell') {
    return (
      <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden>
        <path {...common} d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path {...common} d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
    )
  }
  return null
}

export function MobileShell() {
  const { token, userId, profile } = useAuth()
  const { pathname } = useLocation()
  const [inboxCount, setInboxCount] = useState<number | null>(null)
  const info = tokenDisplayInfo(token)
  const avatarLetter = accountInitialFromProfile(profile, info.username, userId ?? info.userId)
  const avatarPhotoSrc = resolveAuthUploadedFileUrl(profile?.profilePhotoUrl ?? undefined)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    notificationCount(token)
      .then((n) => {
        if (!cancelled) setInboxCount(n)
      })
      .catch(() => {
        if (!cancelled) setInboxCount(null)
      })
    return () => {
      cancelled = true
    }
  }, [token, pathname])

  const assetsActive =
    pathname === `${HOME}/assets` ||
    (pathname.startsWith(`${HOME}/assets/`) && !pathname.startsWith(`${HOME}/assets/add`))
  const alertsActive =
    pathname.startsWith(`${HOME}/alerts`) || pathname.startsWith(`${HOME}/account/notifications`)
  const tipsActive = pathname.startsWith(`${HOME}/tips`)
  const accountOpen = pathname.startsWith(`${HOME}/account`)

  return (
    <div className="mobile-app">
      <header className="app-header">
        <div className="app-header__brand">
          <span className="app-header__dot" aria-hidden />
          <span className="app-header__wordmark">Keeply</span>
        </div>
        <div className="app-header__meta">
          <Link
            to={`${HOME}/account`}
            className={`account-chip${accountOpen ? ' is-active' : ''}`}
            aria-label="Account, profile, and settings"
          >
            <span className="account-chip__avatar" aria-hidden>
              {avatarPhotoSrc ? (
                <img src={avatarPhotoSrc} alt="" width={32} height={32} />
              ) : (
                avatarLetter
              )}
            </span>
            <span className="account-chip__label muted small">Account</span>
          </Link>
        </div>
      </header>

      <div className="mobile-scroll">
        <Outlet />
      </div>

      <nav className="bottom-nav" aria-label="Main">
        <NavLink to={HOME} end className={({ isActive }) => navClass(isActive)}>
          {({ isActive }) => (
            <>
              <Icon name="home" active={isActive} />
            </>
          )}
        </NavLink>
        <NavLink to={`${HOME}/assets`} className={() => navClass(assetsActive)}>
          <Icon name="grid" active={assetsActive} />
        </NavLink>

        <div className="bottom-nav__fab-slot" aria-hidden />
        <Link to={`${HOME}/assets/add`} className="fab" aria-label="Add new assets">
          <span className="fab__plus" aria-hidden>
            +
          </span>
        </Link>

        <NavLink to={`${HOME}/tips`} className={() => navClass(tipsActive)}>
          <Icon name="globe" active={tipsActive} />
        </NavLink>
        <NavLink to={`${HOME}/alerts`} className={() => navClass(alertsActive)}>
          <span className="bottom-nav__icon-wrap">
            <Icon name="bell" active={alertsActive} />
            {inboxCount != null && inboxCount > 0 && (
              <span className="bottom-nav__badge" aria-label={`${inboxCount} notifications`}>
                {inboxCount > 99 ? '99+' : inboxCount}
              </span>
            )}
          </span>
        </NavLink>
      </nav>
    </div>
  )
}
