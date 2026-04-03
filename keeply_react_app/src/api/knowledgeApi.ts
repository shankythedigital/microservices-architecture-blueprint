import { url } from '../config'
import type { RelatedService } from '../constants/helpdesk'
import { apiJson } from './http'

/** Matches helpdesk `ServiceKnowledgeResponse` (Postman: GET …/knowledge/service/{service}). */
export type ServiceKnowledgeItem = {
  id?: number
  service?: string
  topic?: string
  content?: string
  category?: string
  apiEndpoints?: string
  commonIssues?: string
  troubleshootingSteps?: string
  createdAt?: string
  updatedAt?: string
}

export function listKnowledgeByService(token: string, service: RelatedService): Promise<ServiceKnowledgeItem[]> {
  return apiJson<ServiceKnowledgeItem[]>(url('helpdesk', `/api/helpdesk/knowledge/service/${service}`), {
    token,
  })
}

export function searchKnowledge(
  token: string,
  service: RelatedService,
  keyword: string,
): Promise<ServiceKnowledgeItem[]> {
  const q = new URLSearchParams({ keyword })
  return apiJson<ServiceKnowledgeItem[]>(
    url('helpdesk', `/api/helpdesk/knowledge/service/${service}/search?${q.toString()}`),
    { token },
  )
}
