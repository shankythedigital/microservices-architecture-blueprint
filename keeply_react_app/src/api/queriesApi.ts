import { url } from '../config'
import type { RelatedService } from '../constants/helpdesk'
import type { HelpdeskQueryItem } from './types'
import { apiJson } from './http'

export function listMyQueries(token: string): Promise<HelpdeskQueryItem[]> {
  return apiJson<HelpdeskQueryItem[]>(url('helpdesk', '/api/helpdesk/queries/my-queries'), { token })
}

export function createQuery(
  token: string,
  body: { question: string; relatedService: RelatedService },
): Promise<HelpdeskQueryItem> {
  return apiJson<HelpdeskQueryItem>(url('helpdesk', '/api/helpdesk/queries'), {
    method: 'POST',
    token,
    body: JSON.stringify(body),
  })
}
