import { url } from '../config'
import { apiFetch } from './http'

/**
 * Download document bytes (asset-service). Caller should revoke the returned URL after use.
 */
export async function fetchDocumentBlob(
  token: string,
  documentId: number,
): Promise<{ blob: Blob; contentType: string | null }> {
  const r = await apiFetch(url('asset', `/api/asset/v1/documents/download/${documentId}`), {
    token,
    headers: { Accept: '*/*' },
  })
  if (!r.ok) {
    throw new Error(r.statusText || 'Download failed')
  }
  const blob = await r.blob()
  const contentType = r.headers.get('Content-Type')
  return { blob, contentType }
}
