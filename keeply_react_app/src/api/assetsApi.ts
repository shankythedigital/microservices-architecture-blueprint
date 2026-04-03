import { url } from '../config'
import { toApiLocalDate } from '../utils/apiDate'
import { ApiError, parseJson } from './http'
import type { ResponseWrapper, SpringPage } from './types'

export type AssetComponentSummary = {
  componentId?: number
  componentName?: string
  imageUrl?: string | null
}

export type AssetRecord = {
  assetId?: number
  assetNameUdv?: string
  assetStatus?: string
  serialNumber?: string
  purchaseDate?: string
  categoryName?: string
  subCategoryName?: string
  makeName?: string
  modelName?: string
  imageUrl?: string | null
  categoryImageUrl?: string | null
  subCategoryImageUrl?: string | null
  makeImageUrl?: string | null
  modelImageUrl?: string | null
  vendorName?: string | null
  vendorImageUrl?: string | null
  outletName?: string | null
  outletImageUrl?: string | null
  warrantyDocumentId?: number | null
  warrantyDocumentType?: string | null
  amcDocumentId?: number | null
  amcDocumentType?: string | null
  components?: AssetComponentSummary[]
  [k: string]: unknown
}

/** `data` from GET /api/asset/v1/userlinks/need-your-attention — loaded from DB on the server. */
export type NeedYourAttentionPayload = {
  assets?: AssetRecord[]
  categories?: Array<Record<string, unknown>>
  attention?: Record<string, unknown>
  [k: string]: unknown
}

/** Best URL for compact list thumbnails (lazy-loaded). */
export function assetListThumbnailUrl(a: AssetRecord): string | undefined {
  const u =
    a.imageUrl ||
    a.modelImageUrl ||
    a.makeImageUrl ||
    a.categoryImageUrl ||
    a.subCategoryImageUrl ||
    a.vendorImageUrl
  return typeof u === 'string' && u.trim() !== '' ? u.trim() : undefined
}

/** Normalize asset-service `AssetMaster` JSON from GET .../user-asset-links/user/{id}/assets. */
export function mapWireAssetMasterToRecord(raw: Record<string, unknown>): AssetRecord {
  const num = (v: unknown): number | undefined => {
    if (typeof v === 'number' && Number.isFinite(v)) return v
    if (typeof v === 'string' && v !== '') {
      const x = Number(v)
      return Number.isFinite(x) ? x : undefined
    }
    return undefined
  }
  const category = raw.category as Record<string, unknown> | undefined
  const subCategory = raw.subCategory as Record<string, unknown> | undefined
  const make = raw.make as Record<string, unknown> | undefined
  const model = raw.model as Record<string, unknown> | undefined
  return {
    assetId: raw.assetId != null ? num(raw.assetId) : undefined,
    assetNameUdv: typeof raw.assetNameUdv === 'string' ? raw.assetNameUdv : undefined,
    assetStatus: typeof raw.assetStatus === 'string' ? raw.assetStatus : undefined,
    serialNumber: typeof raw.serialNumber === 'string' ? raw.serialNumber : undefined,
    imageUrl: typeof raw.imageUrl === 'string' ? raw.imageUrl : null,
    categoryName: typeof category?.categoryName === 'string' ? category.categoryName : undefined,
    categoryImageUrl: typeof category?.imageUrl === 'string' ? category.imageUrl : null,
    subCategoryName: typeof subCategory?.subCategoryName === 'string' ? subCategory.subCategoryName : undefined,
    subCategoryImageUrl: typeof subCategory?.imageUrl === 'string' ? subCategory.imageUrl : null,
    makeName: typeof make?.makeName === 'string' ? make.makeName : undefined,
    makeImageUrl: typeof make?.imageUrl === 'string' ? make.imageUrl : null,
    modelName: typeof model?.modelName === 'string' ? model.modelName : undefined,
    modelImageUrl: typeof model?.imageUrl === 'string' ? model.imageUrl : null,
  }
}

/**
 * GET /api/asset/v1/user-asset-links/user/{userId}/assets — appliances linked to this user (asset-service).
 */
export async function fetchAssetsAssignedToUser(token: string, userId: number): Promise<AssetRecord[]> {
  const r = await fetch(url('asset', `/api/asset/v1/user-asset-links/user/${userId}/assets`), {
    headers: { Accept: 'application/json', Authorization: `Bearer ${token}` },
  })
  const w = await unwrap<unknown[]>(r)
  const arr = Array.isArray(w.data) ? w.data : []
  return arr.map((item) =>
    mapWireAssetMasterToRecord(
      typeof item === 'object' && item !== null ? (item as Record<string, unknown>) : {},
    ),
  )
}

type AssetCreateInput = {
  assetNameUdv: string
  serialNumber?: string
  purchaseDate?: string
  categoryId?: number
  // make/model IDs can be added once surfaced in UI
}

type AssetRequestPayload = {
  userId?: number | null
  username?: string | null
  projectType?: string | null
  asset: Record<string, unknown>
}

function buildCreateAssetPayload(
  meta: { userId?: number | null; username?: string | null; projectType?: string | null },
  input: AssetCreateInput & { subCategoryId?: number; makeId?: number; modelId?: number },
): AssetRequestPayload {
  const asset: Record<string, unknown> = {
    assetNameUdv: input.assetNameUdv,
    serialNumber: input.serialNumber || undefined,
    purchaseDate: toApiLocalDate(input.purchaseDate ?? null),
    assetStatus: 'ACTIVE',
  }
  if (input.categoryId) asset.category = { categoryId: input.categoryId }
  if (input.subCategoryId) asset.subCategory = { subCategoryId: input.subCategoryId }
  if (input.makeId) asset.make = { makeId: input.makeId }
  if (input.modelId) asset.model = { modelId: input.modelId }

  return {
    userId: meta.userId ?? undefined,
    username: meta.username ?? undefined,
    projectType: meta.projectType ?? 'ECOM',
    asset,
  }
}

async function unwrap<T>(r: Response): Promise<ResponseWrapper<T>> {
  const data = await parseJson<ResponseWrapper<T>>(r)
  if (!r.ok || !data) {
    throw new ApiError((data as { message?: string })?.message || r.statusText, r.status)
  }
  if (data.success === false) {
    throw new ApiError(data.message || 'Request failed', r.status)
  }
  return data
}

/**
 * Aggregated dashboard payload for the logged-in user (assets, categories, warranties, attention flags, etc.).
 * Reads persisted entities via asset-service repositories.
 */
export async function getNeedYourAttention(
  token: string,
): Promise<ResponseWrapper<NeedYourAttentionPayload>> {
  const r = await fetch(url('asset', '/api/asset/v1/userlinks/need-your-attention'), {
    headers: { Accept: 'application/json', Authorization: `Bearer ${token}` },
  })
  return unwrap<NeedYourAttentionPayload>(r)
}

export async function searchAssets(
  token: string,
  params: { keyword?: string; page?: number; size?: number },
): Promise<ResponseWrapper<SpringPage<AssetRecord>>> {
  const q = new URLSearchParams()
  if (params.keyword != null && params.keyword !== '') q.set('keyword', params.keyword)
  q.set('page', String(params.page ?? 0))
  q.set('size', String(params.size ?? 20))
  const r = await fetch(`${url('asset', '/api/asset/v1/assets/search')}?${q}`, {
    headers: { Accept: 'application/json', Authorization: `Bearer ${token}` },
  })
  return unwrap<SpringPage<AssetRecord>>(r)
}

export async function getAssetById(token: string, id: number): Promise<ResponseWrapper<AssetRecord>> {
  const r = await fetch(url('asset', `/api/asset/v1/assets/${id}`), {
    headers: { Accept: 'application/json', Authorization: `Bearer ${token}` },
  })
  return unwrap<AssetRecord>(r)
}

export async function createAsset(
  token: string,
  meta: { userId?: number | null; username?: string | null; projectType?: string | null },
  input: AssetCreateInput & { subCategoryId?: number; makeId?: number; modelId?: number },
): Promise<ResponseWrapper<AssetRecord>> {
  const body = buildCreateAssetPayload(meta, input)

  const r = await fetch(url('asset', '/api/asset/v1/assets'), {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  })
  return unwrap<AssetRecord>(r)
}

/** POST /assets/with-document — raw JSON: { request: AssetRequest, document: base64, docType } (asset-service) */
export async function createAssetWithDocument(
  token: string,
  meta: { userId?: number | null; username?: string | null; projectType?: string | null },
  input: AssetCreateInput & { subCategoryId?: number; makeId?: number; modelId?: number },
  documentBase64: string,
  docType: string,
): Promise<ResponseWrapper<AssetRecord>> {
  const request = buildCreateAssetPayload(meta, input)
  const r = await fetch(url('asset', '/api/asset/v1/assets/with-document'), {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      request,
      document: documentBase64,
      docType: docType.trim(),
    }),
  })
  return unwrap<AssetRecord>(r)
}

/**
 * POST /api/asset/v1/assets/complete — multipart/form-data.
 * Creates asset, warranty, document, and user link in one transaction.
 * Do not set Content-Type; the browser sets the multipart boundary for FormData.
 */
export async function createAssetComplete(
  token: string,
  formData: FormData,
): Promise<ResponseWrapper<Record<string, unknown>>> {
  const r = await fetch(url('asset', '/api/asset/v1/assets/complete'), {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  })
  return unwrap<Record<string, unknown>>(r)
}
