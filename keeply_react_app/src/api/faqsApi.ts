import { url } from '../config'
import type { RelatedService } from '../constants/helpdesk'
import { apiFetch, apiJson, parseJson } from './http'

export interface FaqItem {
  id?: number
  question?: string
  answer?: string
  category?: string
  relatedService?: string
  helpfulCount?: number
  viewCount?: number
  isFavourite?: boolean
}

export function listFaqs(token: string): Promise<FaqItem[]> {
  return apiJson<FaqItem[]>(url('helpdesk', '/api/helpdesk/faqs'), { token })
}

export function searchFaqs(token: string, keyword: string): Promise<FaqItem[]> {
  const q = new URLSearchParams({ keyword })
  return apiJson<FaqItem[]>(url('helpdesk', `/api/helpdesk/faqs/search?${q.toString()}`), { token })
}

export function searchFaqsByService(
  token: string,
  service: RelatedService,
  keyword: string,
): Promise<FaqItem[]> {
  const q = new URLSearchParams({ keyword })
  return apiJson<FaqItem[]>(
    url('helpdesk', `/api/helpdesk/faqs/service/${service}/search?${q.toString()}`),
    { token },
  )
}

export async function markFaqHelpful(token: string, id: number): Promise<void> {
  const r = await apiFetch(url('helpdesk', `/api/helpdesk/faqs/${id}/helpful`), {
    method: 'POST',
    token,
  })
  if (!r.ok) {
    const j = await parseJson<{ message?: string }>(r)
    throw new Error(j?.message || r.statusText)
  }
}
