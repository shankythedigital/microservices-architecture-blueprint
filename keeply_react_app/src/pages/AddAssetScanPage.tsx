import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

const SCAN_IMAGE_SESSION_KEY = 'keeply_scan_image'

export function AddAssetScanPage() {
  const nav = useNavigate()
  const [err, setErr] = useState<string | null>(null)

  function onPickFile(f: File | null) {
    setErr(null)
    if (!f) return
    if (!f.type.toLowerCase().startsWith('image/')) {
      setErr('Please choose an image file.')
      return
    }
    const reader = new FileReader()
    reader.onload = () => {
      const dataUrl = reader.result as string
      const payload = JSON.stringify({ dataUrl, name: f.name?.trim() || 'appliance-photo.jpg' })
      try {
        sessionStorage.setItem(SCAN_IMAGE_SESSION_KEY, payload)
      } catch {
        setErr(
          'This photo is too large to pass to the form in the browser. Try a smaller image or pick the photo directly on the manual entry page.',
        )
        return
      }
      nav('/home/assets/add/manual')
    }
    reader.onerror = () => setErr('Could not read the image file.')
    reader.readAsDataURL(f)
  }

  return (
    <div className="page-pad">
      <Link to="/home/assets/add" className="back-link">
        ← Back
      </Link>
      <h1>Add from photo</h1>
      <p className="muted small">
        Take a picture or choose an image of your appliance. You will finish registration on the manual entry page (the
        photo is attached automatically). You still need your invoice or proof document there.
      </p>
      <div className="sheet stack">
        <label className="field">
          <span>Camera or gallery</span>
          <input
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp,image/*"
            capture="environment"
            onChange={(e) => onPickFile(e.target.files?.[0] ?? null)}
          />
          <span className="muted small">
            On phones, this typically opens the camera; you can also pick from gallery.
          </span>
        </label>
        {err && <p className="error-banner">{err}</p>}
        <Link to="/home/assets/add/manual" className="btn secondary block">
          Skip — enter details manually
        </Link>
      </div>
    </div>
  )
}
