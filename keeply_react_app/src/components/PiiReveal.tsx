import { useId, useState } from 'react'
import type { PiiVariant } from '../utils/maskPii'
import { maskPii } from '../utils/maskPii'

function EyeOpen({ className }: { className?: string }) {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className={className} aria-hidden>
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

function EyeOff({ className }: { className?: string }) {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className={className} aria-hidden>
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
      <path d="m1 1 22 22" />
    </svg>
  )
}

type PiiRevealProps = {
  value: string | null | undefined
  variant: PiiVariant
  /** Render inside <code> (e.g. JWT claim) */
  asCode?: boolean
  className?: string
}

export function PiiReveal({ value, variant, asCode, className }: PiiRevealProps) {
  const [revealed, setRevealed] = useState(false)
  const labelId = useId()

  if (value == null || value === '') {
    return <span className="muted">—</span>
  }

  const masked = maskPii(value, variant)
  const display = revealed ? value : masked

  return (
    <span className={`pii-reveal${className ? ` ${className}` : ''}`}>
      {asCode ? (
        <code id={labelId}>{display}</code>
      ) : (
        <span id={labelId} className="pii-reveal__value">
          {display}
        </span>
      )}
      <button
        type="button"
        role="switch"
        className={`pii-reveal__toggle${revealed ? ' is-revealed' : ''}`}
        aria-checked={revealed}
        aria-label={revealed ? 'Hide sensitive value' : 'Show sensitive value'}
        title={revealed ? 'Hide value' : 'Show value'}
        aria-controls={labelId}
        onClick={() => setRevealed((v) => !v)}
      >
        {revealed ? <EyeOff /> : <EyeOpen />}
      </button>
    </span>
  )
}
