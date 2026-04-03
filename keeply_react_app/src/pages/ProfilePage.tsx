import { Link } from 'react-router-dom'
import { PiiReveal } from '../components/PiiReveal'
import { useAuth } from '../auth/AuthContext'
import { tokenDisplayInfo } from '../auth/jwtClaims'
import { inferPiiVariant } from '../utils/maskPii'

export function ProfilePage() {
  const { token, userId, profile, profileLoading, refreshProfile } = useAuth()
  const info = tokenDisplayInfo(token)

  const showId = profile?.userId ?? userId
  const showUsername = profile?.username ?? info.username

  return (
    <div className="page-pad">
      <Link to="/home/account" className="back-link">
        ← Account
      </Link>
      <h1>Profile</h1>
      <p className="muted small">
        Personal fields are masked by default. Use the eye icon to reveal each value on this device.
      </p>

      {profileLoading && !profile && <p className="muted small">Loading profile…</p>}

      {!profileLoading && !profile && token && (
        <p className="error-banner">
          Could not load decrypted profile. Check that auth-service is running and you are signed in.
          <button type="button" className="btn secondary tight" style={{ marginLeft: 8 }} onClick={refreshProfile}>
            Retry
          </button>
        </p>
      )}

      <div className="detail-card">
        <h2 className="section-title" style={{ marginTop: 0 }}>
          Account
        </h2>
        <dl className="dl-grid">
          <dt>User ID</dt>
          <dd>{showId != null ? String(showId) : '—'}</dd>
          <dt>Username</dt>
          <dd className="profile-username">
            {showUsername ? (
              <PiiReveal value={showUsername} variant={inferPiiVariant(showUsername)} />
            ) : (
              <span className="muted">—</span>
            )}
          </dd>
          {profile?.email && (
            <>
              <dt>Email</dt>
              <dd>
                <PiiReveal value={profile.email} variant="email" />
              </dd>
            </>
          )}
          {profile?.mobile && (
            <>
              <dt>Mobile</dt>
              <dd>
                <PiiReveal value={profile.mobile} variant="mobile" />
              </dd>
            </>
          )}
          {profile?.employeeId && (
            <>
              <dt>Employee ID</dt>
              <dd>
                <PiiReveal value={profile.employeeId} variant="text" />
              </dd>
            </>
          )}
        </dl>
      </div>

      <div className="detail-card">
        <h2 className="section-title" style={{ marginTop: 0 }}>
          Session (JWT)
        </h2>
        <dl className="dl-grid">
          <dt>Token username claim</dt>
          <dd className="profile-username">
            {info.username ? (
              <PiiReveal value={info.username} variant={inferPiiVariant(info.username)} asCode />
            ) : (
              <span className="muted">—</span>
            )}
          </dd>
          <dt>Session ID</dt>
          <dd>{info.sessionId != null ? String(info.sessionId) : '—'}</dd>
        </dl>
      </div>

      {info.roles.length > 0 && (
        <div className="detail-card">
          <h2 className="section-title" style={{ marginTop: 0 }}>
            Roles
          </h2>
          <ul className="plain-list role-chips">
            {info.roles.map((r) => (
              <li key={r}>
                <span className="pill">{r}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
