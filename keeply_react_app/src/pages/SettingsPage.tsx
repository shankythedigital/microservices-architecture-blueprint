import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { logoutAllDevicesOnServer } from '../api/authApi'
import { useKeeplyPreferences, type KeeplyTheme } from '../hooks/useKeeplyPreferences'

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

function SelectRow({
  id,
  label,
  description,
  value,
  onChange,
  options,
}: {
  id: string
  label: string
  description: string
  value: string
  onChange: (v: string) => void
  options: { value: string; label: string }[]
}) {
  return (
    <div className="settings-select-row">
      <div className="settings-select-row__text">
        <label htmlFor={id} className="settings-toggle-row__label">
          {label}
        </label>
        <p className="muted small" id={`${id}-hint`}>
          {description}
        </p>
      </div>
      <select
        id={id}
        className="settings-select-row__control"
        aria-describedby={`${id}-hint`}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
    </div>
  )
}

function SettingsLinkRow({
  to,
  label,
  description,
}: {
  to: string
  label: string
  description: string
}) {
  return (
    <Link to={to} className="settings-link-row">
      <div className="settings-link-row__text">
        <span className="settings-link-row__label">{label}</span>
        <span className="muted small">{description}</span>
      </div>
      <span className="settings-link-row__chevron" aria-hidden />
    </Link>
  )
}

const THEME_OPTIONS: { value: KeeplyTheme; label: string }[] = [
  { value: 'system', label: 'Match device' },
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Dark' },
]

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
        Preferences are saved on this device. Connect a profile API later to sync some of these across sign-in.
      </p>

      <section className="settings-section sheet" aria-labelledby="settings-display-heading">
        <h2 id="settings-display-heading" className="settings-section__title">
          Display &amp; accessibility
        </h2>
        <SelectRow
          id="pref-theme"
          label="Appearance"
          description="Light, dark, or follow your phone or computer setting."
          value={prefs.theme}
          onChange={(v) => prefs.setTheme(v as KeeplyTheme)}
          options={THEME_OPTIONS}
        />
        <ToggleRow
          id="pref-compact"
          label="Compact layout"
          description="Tighter padding on lists and screens to show more at once."
          checked={prefs.compactUi}
          onChange={prefs.setCompactUi}
        />
        <ToggleRow
          id="pref-motion"
          label="Reduce motion"
          description="Shorten animations and transitions for comfort or accessibility."
          checked={prefs.reduceMotion}
          onChange={prefs.setReduceMotion}
        />
        <ToggleRow
          id="pref-thumbs"
          label="Photos on appliance list"
          description="Show thumbnails on My appliances when images are available."
          checked={prefs.showListThumbnails}
          onChange={prefs.setShowListThumbnails}
        />
      </section>

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
          id="pref-helpdesk-activity"
          label="Helpdesk activity hints"
          description="Prefer showing tickets and questions you care about in lists and summaries (UI hints until messaging is wired)."
          checked={prefs.helpdeskActivityAlerts}
          onChange={prefs.setHelpdeskActivityAlerts}
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

      <section className="settings-section sheet" aria-labelledby="settings-shortcuts-heading">
        <h2 id="settings-shortcuts-heading" className="settings-section__title">
          Help &amp; account shortcuts
        </h2>
        <p className="muted small" style={{ margin: '0 0 0.5rem' }}>
          Quick links to support and profile areas.
        </p>
        <nav className="settings-links" aria-label="Shortcuts">
          <SettingsLinkRow to="/home/account/profile" label="Profile" description="Name, contact, and avatar" />
          <SettingsLinkRow to="/home/account/notifications" label="Notification center" description="In-app messages" />
          <SettingsLinkRow to="/home/helpdesk" label="Help &amp; support" description="Support hub" />
          <SettingsLinkRow to="/home/issues" label="My service tickets" description="Issues you raised" />
          <SettingsLinkRow to="/home/helpdesk/queries" label="My questions" description="Helpdesk queries" />
          <SettingsLinkRow to="/home/tips" label="Tips &amp; knowledge" description="FAQs and articles" />
          <SettingsLinkRow to="/home/alerts" label="Alerts inbox" description="Warnings and reminders" />
        </nav>
      </section>

      <section className="settings-section sheet" aria-labelledby="settings-app-heading">
        <h2 id="settings-app-heading" className="settings-section__title">
          About this app
        </h2>
        <p className="muted small" style={{ margin: 0 }}>
          Keeply connects to your asset and helpdesk services. Version and legal copy can be added here for store
          releases.
        </p>
        <p className="muted small settings-footnote" style={{ marginBottom: 0 }}>
          <Link to="/welcome">Welcome &amp; sign-in</Link>
          {' · '}
          <Link to="/home">Home dashboard</Link>
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
