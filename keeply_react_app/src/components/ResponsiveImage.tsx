type ResponsiveImageProps = {
  src: string
  alt: string
  className?: string
  /** Hint for responsive layout; keeps decode work light below the fold. */
  priority?: boolean
}

/**
 * Lazy, async-decoded image with fluid sizing. Pair with CSS max-width / object-fit on the class.
 */
export function ResponsiveImage({ src, alt, className, priority }: ResponsiveImageProps) {
  return (
    <img
      src={src}
      alt={alt}
      className={className}
      loading={priority ? 'eager' : 'lazy'}
      decoding="async"
    />
  )
}
