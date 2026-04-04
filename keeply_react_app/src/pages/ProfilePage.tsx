import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { updateMyProfilePhoto } from '../api/authApi'
import { ApiError } from '../api/http'
import { PiiReveal } from '../components/PiiReveal'
import { MediaEntityCard } from '../components/MediaEntityCard'
import { ResponsiveImage } from '../components/ResponsiveImage'
import { useAuth } from '../auth/AuthContext'
import { tokenDisplayInfo } from '../auth/jwtClaims'
import { inferPiiVariant } from '../utils/maskPii'
import { resolveAuthUploadedFileUrl } from '../utils/uploadedFileUrl'

const PROFILE_PHOTO_MAX_BYTES = 6 * 1024 * 1024

export function ProfilePage() {
  const { token, userId, profile, profileLoading, refreshProfile } = useAuth()
  const info = tokenDisplayInfo(token)
  const photoInputRef = useRef<HTMLInputElement>(null)
  const [photoBusy, setPhotoBusy] = useState(false)
  const [photoErr, setPhotoErr] = useState<string | null>(null)

  const showId = profile?.userId ?? userId
  const showUsername = profile?.username ?? info.username
  const profilePhotoSrc = resolveAuthUploadedFileUrl(profile?.profilePhotoUrl ?? undefined)

  async function onPhotoSelected(f: File | null) {
    setPhotoErr(null)
    if (!f || !token) return
    if (f.size > PROFILE_PHOTO_MAX_BYTES) {
      setPhotoErr('Photo must be 6 MB or smaller.')
      return
    }
    setPhotoBusy(true)
    try {
      await updateMyProfilePhoto(token, f)
      await refreshProfile()
    } catch (e) {
      setPhotoErr(e instanceof ApiError ? e.message : 'Could not update photo')
    } finally {
      setPhotoBusy(false)
      if (photoInputRef.current) photoInputRef.current.value = ''
    }
  }

  return (
    <div className="page-pad">
      <Link to="/home/account" className="back-link">
        ← Account
      </Link>
      <h1>Profile</h1>
      <div className="media-entity-card-grid profile-card-hero">
        <MediaEntityCard
          className="media-entity-card--profile"
          badge="Profile"
          title={showUsername || 'Your account'}
          subtitle={profile?.email ?? undefined}
          media={
            profilePhotoSrc ? (
              <ResponsiveImage
                src={profilePhotoSrc}
                alt={showUsername ? `Profile photo for ${showUsername}` : 'Profile photo'}
                className="media-entity-card__img"
                priority
              />
            ) : (
              <div className="media-entity-card__placeholder" aria-hidden>
                <svg viewBox="0 0 48 48" width="44" height="44" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="24" cy="18" r="10" />
                  <path d="M12 40c2-8 8-12 12-12s10 4 12 12" />
                </svg>
              </div>
            )
          }
        />
      </div>

      {token && profile && (
        <section className="sheet profile-photo-actions" aria-label="Profile photo">
          <input
            ref={photoInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            className="visually-hidden"
            tabIndex={-1}
            onChange={(e) => void onPhotoSelected(e.target.files?.[0] ?? null)}
          />
          <div className="profile-photo-actions__row">
            <button
              type="button"
              className="btn secondary"
              disabled={photoBusy}
              onClick={() => photoInputRef.current?.click()}
            >
              {photoBusy ? 'Uploading…' : profilePhotoSrc ? 'Change photo' : 'Add photo'}
            </button>
            <span className="muted small">JPEG, PNG, WebP, or GIF · up to 6 MB</span>
          </div>
          {photoErr && <p className="error-banner tight">{photoErr}</p>}
        </section>
      )}

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
