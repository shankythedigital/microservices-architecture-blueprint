import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { logoutAllDevicesOnServer } from '../api/authApi'
import { useKeeplyPreferences } from '../hooks/useKeeplyPreferences'

function ToggleRow({
  id,
  label,
  description,
  checked,
  onChange,
}: {
  id: string
  label: string
  description: string
  checked: boolean
  onChange: (v: boolean) => void
}) {
  return (
    <div className="settings-toggle-row">
      <div className="settings-toggle-row__text">
        <label htmlFor={id} className="settings-toggle-row__label">
          {label}
        </label>
        <p className="muted small" id={`${id}-hint`}>
          {description}
        </p>
      </div>
      <button
        id={id}
        type="button"
        role="switch"
        aria-checked={checked}
        aria-describedby={`${id}-hint`}
        className={`settings-switch ${checked ? 'is-on' : ''}`}
        onClick={() => onChange(!checked)}
      >
        <span className="settings-switch__thumb" aria-hidden />
      </button>
    </div>
  )
}

export function SettingsPage() {
  const prefs = useKeeplyPreferences()
  const { token, logout } = useAuth()
  const [signOutAllBusy, setSignOutAllBusy] = useState(false)

  return (
    <div className="page-pad">
      <Link to="/home/account" className="back-link">
        ← Account
      </Link>
      <h1>Settings</h1>
      <p className="muted small">
        These choices are stored on this device and align with what a future preferences API can sync across your
        account.
      </p>

      <section className="settings-section sheet" aria-labelledby="settings-notifications-heading">
        <h2 id="settings-notifications-heading" className="settings-section__title">
          Notifications
        </h2>
        <ToggleRow
          id="pref-push"
          label="Push-style alerts in Keeply"
          description="Bell counts and highlights on the Alerts tab reflect new items when this is on."
          checked={prefs.pushNotifications}
          onChange={prefs.setPushNotifications}
        />
        <ToggleRow
          id="pref-email"
          label="Email reminders"
          description="Placeholder for warranty and service email — requires backend integration."
          checked={prefs.emailReminders}
          onChange={prefs.setEmailReminders}
        />
        <ToggleRow
          id="pref-warranty"
          label="Asset &amp; warranty alerts"
          description="Surface in-app messages about expiring coverage and scheduled care."
          checked={prefs.assetWarrantyAlerts}
          onChange={prefs.setAssetWarrantyAlerts}
        />
        <ToggleRow
          id="pref-digest"
          label="Weekly digest"
          description="Summary of assets and open issues (when messaging is connected)."
          checked={prefs.weeklyDigest}
          onChange={prefs.setWeeklyDigest}
        />
        <p className="muted small settings-footnote">
          Open your{' '}
          <Link to="/home/account/notifications">notification center</Link> or{' '}
          <Link to="/home/alerts">alerts inbox</Link> to read messages.
        </p>
      </section>

      <section className="settings-section sheet" aria-labelledby="settings-app-heading">
        <h2 id="settings-app-heading" className="settings-section__title">
          App
        </h2>
        <p className="muted small" style={{ margin: 0 }}>
          Theme and language follow your browser for now. Data &amp; privacy policies belong in your product wiki when
          you publish Keeply broadly.
        </p>
      </section>

      <section className="settings-section sheet" aria-labelledby="settings-security-heading">
        <h2 id="settings-security-heading" className="settings-section__title">
          Security
        </h2>
        <p className="muted small">
          Revokes every active sign-in for your account (other phones and browsers too), then returns you to the welcome
          screen on this device.
        </p>
        <button
          type="button"
          className="btn secondary"
          disabled={!token || signOutAllBusy}
          onClick={() => {
            if (!token) return
            setSignOutAllBusy(true)
            void (async () => {
              try {
                await logoutAllDevicesOnServer(token)
              } finally {
                setSignOutAllBusy(false)
                await logout()
              }
            })()
          }}
        >
          {signOutAllBusy ? 'Signing out…' : 'Sign out on all devices'}
        </button>
      </section>
    </div>
  )
}
