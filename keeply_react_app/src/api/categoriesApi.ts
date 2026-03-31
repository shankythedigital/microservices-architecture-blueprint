import { url } from '../config'
import { apiJson } from './http'
import type { ResponseWrapper } from './types'

export interface CategoryDto {
  categoryId?: number
  categoryName?: string
  description?: string
}

export function listCategories(token: string): Promise<ResponseWrapper<CategoryDto[]>> {
  return apiJson<ResponseWrapper<CategoryDto[]>>(url('asset', '/api/asset/v1/categories'), {
    token,
  })
}
