import { url } from '../config'
import { toApiLocalDate } from '../utils/apiDate'
import { ApiError, apiFetch, considerUnauthorizedResponse, parseJson } from './http'
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
  /** User-uploaded appliance photo (use documents download API). */
  assetPhotoDocumentId?: number | null
  assetPhotoDocumentType?: string | null
  /** Earliest linked user (registered owner); used for who may change the appliance photo. */
  ownerUserId?: number | null
  createdByUsername?: string | null
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

/** Warranty / AMC rows ending within this many calendar days (server pre-filters to 30; we narrow for “few days”). */
export const EXPIRING_COVERAGE_REMINDER_MAX_DAYS = 14

export type CoverageExpiryReminder = {
  kind: 'warranty' | 'amc'
  assetId: number
  assetName: string
  /** ISO date string (YYYY-MM-DD) when available */
  endDate: string
  daysLeft: number
}

function attentionNum(v: unknown): number | null {
  if (typeof v === 'number' && Number.isFinite(v)) return v
  if (typeof v === 'string' && v !== '') {
    const x = Number(v)
    return Number.isFinite(x) ? x : null
  }
  return null
}

function parseAttentionEndDate(raw: unknown): Date | null {
  if (raw == null) return null
  if (typeof raw === 'string' && raw !== '') {
    const d = new Date(raw.length <= 10 ? `${raw}T12:00:00` : raw)
    return Number.isNaN(d.getTime()) ? null : d
  }
  return null
}

function startOfLocalDay(d: Date): number {
  return Date.UTC(d.getFullYear(), d.getMonth(), d.getDate())
}

function calendarDaysBetween(from: Date, to: Date): number {
  const a = startOfLocalDay(from)
  const b = startOfLocalDay(to)
  return Math.round((b - a) / 86400000)
}

/**
 * Uses `attention.expiringWarranties` / `attention.expiringAmcs` from need-your-attention (server: within 30 days).
 * Optionally narrows to `maxDaysLeft` for UI (“few days”).
 */
export function extractExpiringCoverageReminders(
  payload: NeedYourAttentionPayload | null | undefined,
  maxDaysLeft: number = EXPIRING_COVERAGE_REMINDER_MAX_DAYS,
): CoverageExpiryReminder[] {
  const att = payload?.attention
  if (!att || typeof att !== 'object') return []

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const rows: CoverageExpiryReminder[] = []

  const pushRows = (list: unknown, kind: 'warranty' | 'amc', endKey: string) => {
    if (!Array.isArray(list)) return
    for (const item of list) {
      if (!item || typeof item !== 'object') continue
      const r = item as Record<string, unknown>
      const assetId = attentionNum(r.assetId)
      if (assetId == null) continue
      const end = parseAttentionEndDate(r[endKey])
      if (!end) continue
      const daysLeft = calendarDaysBetween(today, end)
      if (daysLeft < 0 || daysLeft > maxDaysLeft) continue
      const name =
        typeof r.assetName === 'string' && r.assetName.trim()
          ? r.assetName.trim()
          : 'Appliance'
      const endIso =
        typeof r[endKey] === 'string' && (r[endKey] as string).length <= 10
          ? (r[endKey] as string)
          : end.toISOString().slice(0, 10)
      rows.push({ kind, assetId, assetName: name, endDate: endIso, daysLeft })
    }
  }

  pushRows(att.expiringWarranties, 'warranty', 'warrantyEndDate')
  pushRows(att.expiringAmcs, 'amc', 'amcEndDate')

  rows.sort((a, b) => a.daysLeft - b.daysLeft || a.assetName.localeCompare(b.assetName))
  return rows
}

export function formatCoverageDaysLeft(daysLeft: number): string {
  if (daysLeft <= 0) return 'today'
  if (daysLeft === 1) return 'tomorrow'
  return `in ${daysLeft} days`
}

/** Best URL for compact list thumbnails (lazy-loaded). Does not include `assetPhotoDocumentId` (use AuthenticatedDocImage). */
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

/** Normalize asset rows from GET .../user-asset-links/user/{id}/assets (entity or AssetResponseDTO shape). */
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
    categoryName:
      typeof category?.categoryName === 'string'
        ? category.categoryName
        : typeof raw.categoryName === 'string'
          ? raw.categoryName
          : undefined,
    categoryImageUrl:
      typeof category?.imageUrl === 'string'
        ? category.imageUrl
        : typeof raw.categoryImageUrl === 'string'
          ? raw.categoryImageUrl
          : null,
    subCategoryName:
      typeof subCategory?.subCategoryName === 'string'
        ? subCategory.subCategoryName
        : typeof raw.subCategoryName === 'string'
          ? raw.subCategoryName
          : undefined,
    subCategoryImageUrl:
      typeof subCategory?.imageUrl === 'string'
        ? subCategory.imageUrl
        : typeof raw.subCategoryImageUrl === 'string'
          ? raw.subCategoryImageUrl
          : null,
    makeName:
      typeof make?.makeName === 'string'
        ? make.makeName
        : typeof raw.makeName === 'string'
          ? raw.makeName
          : undefined,
    makeImageUrl:
      typeof make?.imageUrl === 'string'
        ? make.imageUrl
        : typeof raw.makeImageUrl === 'string'
          ? raw.makeImageUrl
          : null,
    modelName:
      typeof model?.modelName === 'string'
        ? model.modelName
        : typeof raw.modelName === 'string'
          ? raw.modelName
          : undefined,
    modelImageUrl:
      typeof model?.imageUrl === 'string'
        ? model.imageUrl
        : typeof raw.modelImageUrl === 'string'
          ? raw.modelImageUrl
          : null,
    vendorName: typeof raw.vendorName === 'string' ? raw.vendorName : undefined,
    vendorImageUrl: typeof raw.vendorImageUrl === 'string' ? raw.vendorImageUrl : null,
    outletName: typeof raw.outletName === 'string' ? raw.outletName : undefined,
    outletImageUrl: typeof raw.outletImageUrl === 'string' ? raw.outletImageUrl : null,
    warrantyDocumentId: raw.warrantyDocumentId != null ? num(raw.warrantyDocumentId) : undefined,
    warrantyDocumentType:
      typeof raw.warrantyDocumentType === 'string' ? raw.warrantyDocumentType : undefined,
    amcDocumentId: raw.amcDocumentId != null ? num(raw.amcDocumentId) : undefined,
    amcDocumentType: typeof raw.amcDocumentType === 'string' ? raw.amcDocumentType : undefined,
    assetPhotoDocumentId: raw.assetPhotoDocumentId != null ? num(raw.assetPhotoDocumentId) : undefined,
    assetPhotoDocumentType:
      typeof raw.assetPhotoDocumentType === 'string' ? raw.assetPhotoDocumentType : undefined,
    ownerUserId: raw.ownerUserId != null ? num(raw.ownerUserId) : undefined,
    createdByUsername:
      typeof raw.createdByUsername === 'string' ? raw.createdByUsername : undefined,
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
  considerUnauthorizedResponse(r, true)
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
  const r = await apiFetch(url('asset', '/api/asset/v1/userlinks/need-your-attention'), { token })
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
  const r = await apiFetch(url('asset', '/api/asset/v1/assets/complete'), {
    method: 'POST',
    headers: { Accept: 'application/json' },
    body: formData,
    token,
  })
  return unwrap<Record<string, unknown>>(r)
}
