import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { fetchActiveTerms, registerUser, type TermsPayload } from '../api/authApi'
import { ApiError } from '../api/http'
import { DEFAULT_PROJECT_TYPE } from '../constants/project'

export function RegisterPage() {
  const nav = useNavigate()
  const [terms, setTerms] = useState<TermsPayload | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [mobile, setMobile] = useState('')
  const [countryCode, setCountryCode] = useState('+91')
  const [pincode, setPincode] = useState('')
  const [city, setCity] = useState('')
  const [state, setState] = useState('')
  const [country, setCountry] = useState('')
  const [address1, setAddress1] = useState('')
  const [acceptTc, setAcceptTc] = useState(false)
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    let c = false
    ;(async () => {
      const t = await fetchActiveTerms(DEFAULT_PROJECT_TYPE).catch(() => null)
      if (!c) setTerms(t)
    })()
    return () => {
      c = true
    }
  }, [])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setErr(null)
    setBusy(true)
    try {
      const digits = mobile.replace(/\D/g, '')
      await registerUser({
        username: username.trim() || digits,
        password: password.trim() || undefined,
        firstName: firstName.trim() || undefined,
        lastName: lastName.trim() || undefined,
        email: email.trim() || undefined,
        mobile: digits,
        countryCode: countryCode.trim() || '+91',
        projectType: DEFAULT_PROJECT_TYPE,
        acceptTc,
        pincode: pincode.trim() || undefined,
        city: city.trim() || undefined,
        state: state.trim() || undefined,
        country: country.trim() || undefined,
        address1: address1.trim() || undefined,
      })
      nav('/login', { replace: true, state: { registered: true } })
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Registration failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="welcome">
      <div className="welcome__card login register-card">
        <h1>Create account</h1>
        <p className="muted small">We’ll use your mobile for OTP sign-in. Password is optional if you prefer OTP only.</p>

        {terms && (
          <details className="sheet terms-preview">
            <summary>{terms.title || 'Terms & conditions'}</summary>
            {terms.summary && <p className="muted small">{terms.summary}</p>}
            {terms.content && (
              <div className="terms-preview__content muted small">
                {terms.content.length > 1200 ? `${terms.content.slice(0, 1200)}…` : terms.content}
              </div>
            )}
          </details>
        )}

        <form onSubmit={onSubmit} className="stack sheet register-form">
          <label className="field">
            <span>Username</span>
            <input
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              placeholder="Unique login id"
            />
          </label>
          <label className="field">
            <span>Password (optional if you use OTP-only)</span>
            <input
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </label>
          <div className="register-two-col">
            <label className="field">
              <span>First name</span>
              <input value={firstName} onChange={(e) => setFirstName(e.target.value)} autoComplete="given-name" />
            </label>
            <label className="field">
              <span>Last name</span>
              <input value={lastName} onChange={(e) => setLastName(e.target.value)} autoComplete="family-name" />
            </label>
          </div>
          <label className="field">
            <span>Email</span>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
          </label>
          <div className="register-two-col">
            <label className="field">
              <span>Country code</span>
              <input value={countryCode} onChange={(e) => setCountryCode(e.target.value)} placeholder="+91" required />
            </label>
            <label className="field">
              <span>Mobile</span>
              <input
                inputMode="numeric"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                autoComplete="tel"
                required
              />
            </label>
          </div>
          <label className="field">
            <span>Address line 1 (optional)</span>
            <input value={address1} onChange={(e) => setAddress1(e.target.value)} />
          </label>
          <div className="register-two-col">
            <label className="field">
              <span>City</span>
              <input value={city} onChange={(e) => setCity(e.target.value)} />
            </label>
            <label className="field">
              <span>State</span>
              <input value={state} onChange={(e) => setState(e.target.value)} />
            </label>
          </div>
          <div className="register-two-col">
            <label className="field">
              <span>Pincode</span>
              <input value={pincode} onChange={(e) => setPincode(e.target.value)} />
            </label>
            <label className="field">
              <span>Country</span>
              <input value={country} onChange={(e) => setCountry(e.target.value)} />
            </label>
          </div>

          <label className="field field--checkbox">
            <input type="checkbox" checked={acceptTc} onChange={(e) => setAcceptTc(e.target.checked)} required />
            <span>I accept the terms and conditions</span>
          </label>

          {err && <p className="error-banner">{err}</p>}

          <button type="submit" className="btn primary block" disabled={busy}>
            {busy ? 'Creating…' : 'Register'}
          </button>
        </form>

        <p className="muted small center">
          <Link to="/login">Already have an account? Sign in</Link>
        </p>
      </div>
    </div>
  )
}
