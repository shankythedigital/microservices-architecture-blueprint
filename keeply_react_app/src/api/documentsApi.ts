import { url } from '../config'
import { DEFAULT_PROJECT_TYPE } from '../constants/project'
import { ApiError, apiFetch, parseJson } from './http'
import type { ResponseWrapper } from './types'

/** document_type_master code for user appliance photos (asset-service). */
export const ASSET_PHOTO_DOC_TYPE = 'asset_photo'

export type AssetDocumentSummary = {
  documentId?: number
  fileName?: string | null
  docType?: string | null
  entityType?: string | null
  entityId?: number | null
}

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

/**
 * POST /api/asset/v1/documents/upload — links file to an asset (e.g. docType `asset_photo`).
 */
export async function uploadAssetDocument(
  token: string,
  params: {
    assetId: number
    userId: number
    username: string
    file: File
    docType: string
    projectType?: string
  },
): Promise<AssetDocumentSummary> {
  const fd = new FormData()
  fd.append('file', params.file, params.file.name)
  fd.set('entityType', 'ASSET')
  fd.set('entityId', String(params.assetId))
  fd.set('userId', String(params.userId))
  fd.set('username', params.username)
  fd.set('projectType', (params.projectType ?? DEFAULT_PROJECT_TYPE).trim())
  fd.set('docType', params.docType.trim())

  const r = await apiFetch(url('asset', '/api/asset/v1/documents/upload'), {
    method: 'POST',
    token,
    body: fd,
    headers: { Accept: 'application/json' },
  })
  const data = (await parseJson<ResponseWrapper<AssetDocumentSummary>>(r)) || ({} as ResponseWrapper<AssetDocumentSummary>)
  if (!r.ok || data.success === false) {
    throw new ApiError(data.message || r.statusText, r.status)
  }
  return data.data
}
