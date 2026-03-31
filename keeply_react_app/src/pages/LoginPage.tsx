import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { sendLoginOtp } from '../api/authApi'
import { ApiError } from '../api/http'

type Mode = 'otp' | 'password'

export function LoginPage() {
  const { token, login, loginWithOtp, error, clearError } = useAuth()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from || '/home'

  const [mode, setMode] = useState<Mode>('otp')
  const [mobile, setMobile] = useState('')
  const [otp, setOtp] = useState('')
  const [otpStep, setOtpStep] = useState<'phone' | 'code'>('phone')
  const [devOtpHint, setDevOtpHint] = useState<string | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [sendErr, setSendErr] = useState<string | null>(null)

  if (token) return <Navigate to={from} replace />

  async function onSendOtp(e: FormEvent) {
    e.preventDefault()
    clearError()
    setSendErr(null)
    setDevOtpHint(null)
    setBusy(true)
    try {
      const digits = mobile.replace(/\D/g, '')
      const res = await sendLoginOtp(digits)
      setOtpStep('code')
      if (res.otp) setDevOtpHint(res.otp)
    } catch (e) {
      setSendErr(e instanceof ApiError ? e.message : 'Could not send OTP')
    } finally {
      setBusy(false)
    }
  }

  async function onOtpLogin(e: FormEvent) {
    e.preventDefault()
    clearError()
    setBusy(true)
    try {
      const digits = mobile.replace(/\D/g, '')
      await loginWithOtp(digits, otp.trim())
    } catch {
      /* surfaced */
    } finally {
      setBusy(false)
    }
  }

  async function onPasswordLogin(e: FormEvent) {
    e.preventDefault()
    clearError()
    setBusy(true)
    try {
      await login(username.trim(), password)
    } catch {
      /* surfaced */
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="welcome">
      <div className="welcome__card login">
        <h1>Sign in</h1>
        <p className="muted small">Enter your mobile number to get a one-time passcode.</p>

        <div className="segmented">
          <button
            type="button"
            className={mode === 'otp' ? 'is-on' : ''}
            onClick={() => {
              setMode('otp')
              clearError()
            }}
          >
            Mobile OTP
          </button>
          <button
            type="button"
            className={mode === 'password' ? 'is-on' : ''}
            onClick={() => {
              setMode('password')
              clearError()
            }}
          >
            Password
          </button>
        </div>

        {mode === 'otp' && otpStep === 'phone' && (
          <form onSubmit={onSendOtp} className="stack sheet">
            <label className="field">
              <span>Mobile number</span>
              <input
                inputMode="numeric"
                autoComplete="tel"
                placeholder="10–15 digits"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                required
              />
            </label>
            <p className="muted small">Country-aware formatting can mirror KeeplyV1.pdf; API expects digits only.</p>
            {(sendErr || error) && (
              <p className="error-banner">{sendErr || error}</p>
            )}
            <button type="submit" className="btn primary block" disabled={busy}>
              Send OTP
            </button>
          </form>
        )}

        {mode === 'otp' && otpStep === 'code' && (
          <form onSubmit={onOtpLogin} className="stack sheet">
            <label className="field">
              <span>OTP from SMS</span>
              <input
                inputMode="numeric"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                required
              />
            </label>
            {devOtpHint && (
              <p className="dev-hint small">Dev: OTP returned by auth-service: {devOtpHint}</p>
            )}
            {error && <p className="error-banner">{error}</p>}
            <button type="submit" className="btn primary block" disabled={busy}>
              Verify &amp; continue
            </button>
            <button
              type="button"
              className="btn ghost block"
              onClick={() => {
                setOtpStep('phone')
                setOtp('')
                setDevOtpHint(null)
              }}
            >
              Use a different number
            </button>
          </form>
        )}

        {mode === 'password' && (
          <form onSubmit={onPasswordLogin} className="stack sheet">
            <label className="field">
              <span>Username</span>
              <input
                autoComplete="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </label>
            <label className="field">
              <span>Password</span>
              <input
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </label>
            {error && <p className="error-banner">{error}</p>}
            <button type="submit" className="btn primary block" disabled={busy}>
              Sign in
            </button>
          </form>
        )}

        <div className="social-block">
          <p className="muted small">Social login (FR-04)</p>
          <div className="social-row">
            <button type="button" className="btn ghost" disabled title="Roadmap">
              Google
            </button>
            <button type="button" className="btn ghost" disabled title="Roadmap">
              Meta
            </button>
            <button type="button" className="btn ghost" disabled title="Roadmap">
              Apple
            </button>
          </div>
        </div>

        <p className="muted small center">
          <Link to="/welcome">← Back</Link>
        </p>
      </div>
    </div>
  )
}
