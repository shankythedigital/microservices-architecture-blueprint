import { url } from '../config'
import { apiJson } from './http'

export interface FaqItem {
  id?: number
  question?: string
  answer?: string
  category?: string
}

export function listFaqs(token: string): Promise<FaqItem[]> {
  return apiJson<FaqItem[]>(url('helpdesk', '/api/helpdesk/faqs'), { token })
}
