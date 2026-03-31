import { Link } from 'react-router-dom'

export function AddAssetMethodPage() {
  return (
    <div className="page-pad">
      <h1>Add appliance</h1>
      <p className="muted small">FR-09–17 — capture via QR/barcode or manual wizard (KeeplyV1.pdf p.8–12).</p>
      <div className="method-grid">
        <Link to="/home/assets/add/scan" className="method-tile">
          <strong>Scan QR / barcode</strong>
          <span className="muted small">Product details from code</span>
        </Link>
        <Link to="/home/assets/add/manual" className="method-tile">
          <strong>Manual entry</strong>
          <span className="muted small">Category · brand · invoice</span>
        </Link>
      </div>
      <div className="sheet">
        <strong>Tip</strong>
        <p className="muted small" style={{ marginTop: '.25rem' }}>
          If scan can’t find a match, you’ll fall back to manual entry.
        </p>
      </div>
    </div>
  )
}
