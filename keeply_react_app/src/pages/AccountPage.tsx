import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { tokenDisplayInfo } from '../auth/jwtClaims'
import { accountInitial } from '../utils/accountDisplay'

export function AccountPage() {
  const { token, userId, logout } = useAuth()
  const info = tokenDisplayInfo(token)

  const label = info.username || (userId != null ? `Member #${userId}` : 'Your account')
  const initial = accountInitial(info.username, userId ?? info.userId)

  return (
    <div className="page-pad account-page">
      <h1>Account</h1>
      <p className="muted small">Profile, preferences, and notification inbox for your home assets.</p>

      <section className="account-hero sheet" aria-label="Signed-in user">
        <div className="account-hero__avatar" aria-hidden>
          {initial}
        </div>
        <div className="account-hero__meta">
          <p className="account-hero__title">{label}</p>
          {userId != null && (
            <p className="muted small">
              User ID <strong>#{userId}</strong>
            </p>
          )}
        </div>
      </section>

      <nav className="account-menu" aria-label="Account sections">
        <Link to="/home/account/profile" className="account-menu__row">
          <span className="account-menu__icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </span>
          <span className="account-menu__label">Profile</span>
          <span className="account-menu__chevron" aria-hidden />
        </Link>
        <Link to="/home/account/settings" className="account-menu__row">
          <span className="account-menu__icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="3" />
              <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
            </svg>
          </span>
          <span className="account-menu__label">Settings</span>
          <span className="account-menu__chevron" aria-hidden />
        </Link>
        <Link to="/home/account/notifications" className="account-menu__row">
          <span className="account-menu__icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
          </span>
          <span className="account-menu__label">Notifications</span>
          <span className="account-menu__chevron" aria-hidden />
        </Link>
        <Link to="/home/alerts" className="account-menu__row">
          <span className="account-menu__icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M10.29 3.86 1.82 18a1 1 0 0 0 .9 1.44h18.56a1 1 0 0 0 .9-1.44L13.71 3.86a1 1 0 0 0-1.42 0z" />
              <path d="M12 9v4M12 17h.01" />
            </svg>
          </span>
          <span className="account-menu__label">Alerts inbox</span>
          <span className="account-menu__chevron" aria-hidden />
        </Link>
        <Link to="/home/issues" className="account-menu__row">
          <span className="account-menu__icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M4 12h16M4 12l4-4m-4 4 4 4" />
            </svg>
          </span>
          <span className="account-menu__label">Service issues</span>
          <span className="account-menu__chevron" aria-hidden />
        </Link>
        <Link to="/home/tips" className="account-menu__row">
          <span className="account-menu__icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 16v-4M12 8h.01" />
            </svg>
          </span>
          <span className="account-menu__label">Help &amp; tips</span>
          <span className="account-menu__chevron" aria-hidden />
        </Link>
      </nav>

      <section className="sheet account-legal">
        <p className="muted small" style={{ margin: 0 }}>
          More account tools (password, devices, linked homes) can plug in here when the API is available.
        </p>
      </section>

      <button type="button" className="btn secondary account-signout" onClick={logout}>
        Sign out
      </button>
    </div>
  )
}
