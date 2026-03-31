import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const HOME = '/home'

function navClass(active: boolean) {
  return active ? 'bottom-nav__link is-active' : 'bottom-nav__link'
}

function Icon({ name, active }: { name: 'home' | 'grid' | 'globe' | 'headset'; active?: boolean }) {
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
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden>
      <path {...common} d="M4 12a8 8 0 1 1 16 0v4a2 2 0 0 1-2 2h-2v-6h4" />
      <path {...common} d="M4 16a2 2 0 0 0 2 2h2v-6H4v4Z" />
    </svg>
  )
}

export function MobileShell() {
  const { logout, userId } = useAuth()
  const { pathname } = useLocation()

  const assetsActive =
    pathname === `${HOME}/assets` ||
    (pathname.startsWith(`${HOME}/assets/`) && !pathname.startsWith(`${HOME}/assets/add`))
  const alertsActive = pathname.startsWith(`${HOME}/alerts`)
  const tipsActive = pathname.startsWith(`${HOME}/tips`)

  return (
    <div className="mobile-app">
      <header className="app-header">
        <div className="app-header__brand">
          <span className="app-header__dot" aria-hidden />
          <span>Keeply</span>
        </div>
        <div className="app-header__meta">
          {userId != null && <span className="muted small">#{userId}</span>}
          <button type="button" className="btn text" onClick={logout}>
            Log out
          </button>
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
          <Icon name="headset" active={alertsActive} />
        </NavLink>
      </nav>
    </div>
  )
}
