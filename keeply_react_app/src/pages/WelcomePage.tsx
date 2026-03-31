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
        <ul className="muted small welcome__bullets" aria-label="Highlights">
          <li>Add appliances by scan or manual entry</li>
          <li>Store invoice documents</li>
          <li>Get alerts before warranty or service due</li>
          <li>Raise service issues and track status</li>
        </ul>
        <Link to="/login" className="btn primary block">
          Get started
        </Link>
        <p className="muted small legal">
          By continuing you’ll verify your mobile (OTP). Password login is available for admin/dev accounts.
        </p>
      </div>
    </div>
  )
}
