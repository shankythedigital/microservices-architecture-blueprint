import { useEffect, useState } from 'react'
import { fetchDocumentBlob } from '../api/documentsApi'
import { ResponsiveImage } from './ResponsiveImage'

function looksLikeImageContentType(ct: string | null, declaredDocType?: string | null): boolean {
  if (ct && ct.toLowerCase().startsWith('image/')) return true
  if (!declaredDocType) return false
  const t = declaredDocType.toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg', 'heic', 'image'].some(
    (x) => t === x || t.includes(x),
  )
}

type Props = {
  token: string
  documentId: number
  /** DB doc_type hint when content-type may be generic */
  docTypeHint?: string | null
  alt: string
  className?: string
}

/**
 * Loads a protected document and shows a preview only when bytes look like an image.
 */
export function AuthenticatedDocImage({ token, documentId, docTypeHint, alt, className }: Props) {
  const [src, setSrc] = useState<string | null>(null)
  const [phase, setPhase] = useState<'loading' | 'ready' | 'nonimage' | 'error'>('loading')

  useEffect(() => {
    let cancelled = false
    let objectUrl: string | null = null
    setPhase('loading')
    setSrc(null)
    ;(async () => {
      try {
        const { blob, contentType } = await fetchDocumentBlob(token, documentId)
        if (cancelled) return
        const ok = looksLikeImageContentType(contentType, docTypeHint)
        if (!ok) {
          setPhase('nonimage')
          return
        }
        objectUrl = URL.createObjectURL(blob)
        if (cancelled) {
          URL.revokeObjectURL(objectUrl)
          return
        }
        setSrc(objectUrl)
        setPhase('ready')
      } catch {
        if (!cancelled) setPhase('error')
      }
    })()
    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [token, documentId, docTypeHint])

  if (phase === 'error') {
    return <span className="muted small">Preview unavailable</span>
  }
  if (phase === 'nonimage') {
    return <span className="muted small">No image preview (open document separately).</span>
  }
  if (phase !== 'ready' || !src) {
    return <span className="muted small">Loading preview…</span>
  }
  return <ResponsiveImage src={src} alt={alt} className={className} />
}
