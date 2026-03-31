import { url } from '../config'
import { toApiLocalDate } from '../utils/apiDate'
import { ApiError, parseJson } from './http'
import type { ResponseWrapper, SpringPage } from './types'

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
  [k: string]: unknown
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
