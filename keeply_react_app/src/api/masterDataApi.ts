import { url } from '../config'
import { apiJson } from './http'
import type { ResponseWrapper } from './types'

export type SubCategory = { subCategoryId?: number; subCategoryName?: string; category?: { categoryId?: number } }
export type Make = { makeId?: number; makeName?: string; subCategory?: { subCategoryId?: number } }
export type Model = { modelId?: number; modelName?: string; makeId?: number; make?: { makeId?: number } }

export function listSubCategories(token: string): Promise<ResponseWrapper<SubCategory[]>> {
  return apiJson<ResponseWrapper<SubCategory[]>>(url('asset', '/api/asset/v1/subcategories'), { token })
}

export function listMakes(token: string): Promise<ResponseWrapper<Make[]>> {
  return apiJson<ResponseWrapper<Make[]>>(url('asset', '/api/asset/v1/makes'), { token })
}

export function listModels(token: string): Promise<ResponseWrapper<Model[]>> {
  return apiJson<ResponseWrapper<Model[]>>(url('asset', '/api/asset/v1/models'), { token })
}

