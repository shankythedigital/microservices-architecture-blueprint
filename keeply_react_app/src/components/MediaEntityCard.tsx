import type { ReactNode } from 'react'

export type MediaEntityCardProps = {
  /** Short label, e.g. "Category", "Warranty" */
  badge: string
  title: string
  subtitle?: string
  /** Image or document preview */
  media: ReactNode
  className?: string
}

/**
 * Responsive media card: fixed-aspect media region, footer with badge + title.
 * Use inside a `media-entity-card-grid` (or similar) for breakpoints.
 */
export function MediaEntityCard({ badge, title, subtitle, media, className }: MediaEntityCardProps) {
  return (
    <article className={['media-entity-card', className].filter(Boolean).join(' ')}>
      <div className="media-entity-card__media">{media}</div>
      <div className="media-entity-card__body">
        <span className="media-entity-card__badge">{badge}</span>
        <h3 className="media-entity-card__title">{title}</h3>
        {subtitle ? <p className="media-entity-card__subtitle muted small">{subtitle}</p> : null}
      </div>
    </article>
  )
}
