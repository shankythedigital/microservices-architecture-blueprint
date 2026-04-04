import { Link } from 'react-router-dom'

export function WelcomePage() {
  return (
    <div className="welcome">
      <div className="welcome__card">
        <div className="welcome__logo" aria-hidden />
        <h1>Keeply</h1>
        <p className="muted">
          Keep track of appliances, invoices, warranties, and service reminders—so you never miss an expiry.
        </p>
        <p className="muted small">
          New here? Create a free account first. Already registered? Sign in to open your home dashboard.
        </p>
        <ul className="muted small welcome__bullets" aria-label="Highlights">
          <li>Add appliances by scan or manual entry</li>
          <li>Store invoice documents</li>
          <li>Get alerts before warranty or service due</li>
          <li>Raise service issues and track status</li>
        </ul>
        <Link to="/register" className="btn primary block">
          Create an account
        </Link>
        <Link to="/login" className="btn secondary block" style={{ marginTop: '0.65rem' }}>
          Sign in
        </Link>
        <p className="muted small legal">
          Registration verifies your mobile (OTP). Password login is available for admin/dev accounts after you
          register.
        </p>
      </div>
    </div>
  )
}
