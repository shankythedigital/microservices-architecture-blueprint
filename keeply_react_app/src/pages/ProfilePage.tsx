import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { tokenDisplayInfo } from '../auth/jwtClaims'

export function ProfilePage() {
  const { token, userId } = useAuth()
  const info = tokenDisplayInfo(token)

  return (
    <div className="page-pad">
      <Link to="/home/account" className="back-link">
        ← Account
      </Link>
      <h1>Profile</h1>
      <p className="muted small">
        Details come from your sign-in session. Update contact information in Settings when your organization exposes
        that API.
      </p>

      <div className="detail-card">
        <h2 className="section-title" style={{ marginTop: 0 }}>
          Session
        </h2>
        <dl className="dl-grid">
          <dt>User ID</dt>
          <dd>{userId != null ? String(userId) : '—'}</dd>
          <dt>Account identifier</dt>
          <dd className="profile-username">
            {info.username ? (
              <code>{info.username}</code>
            ) : (
              <span className="muted">Not present in token</span>
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

      <p className="muted small">
        The identifier value may be a hashed or internal username from the auth service — it is still useful for
        support when you report an issue.
      </p>
    </div>
  )
}
