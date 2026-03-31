import { Link } from 'react-router-dom'

export function AddAssetScanPage() {
  return (
    <div className="page-pad">
      <Link to="/home/assets/add" className="back-link">
        ← Back
      </Link>
      <h1>Scan code</h1>
      <p className="muted small">FR-09–12 — native camera &amp; /api/asset scan endpoints in production build.</p>
      <div className="scan-placeholder">
        <p>Camera viewport (web placeholder)</p>
        <p className="muted small">Use the mobile shell or Capacitor build to attach live scanning.</p>
      </div>
      <Link to="/home/assets/add/manual" className="btn secondary block">
        Enter details manually instead
      </Link>
    </div>
  )
}
