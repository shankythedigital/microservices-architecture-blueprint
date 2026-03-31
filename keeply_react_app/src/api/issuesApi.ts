import { url } from '../config'
import { apiJson } from './http'
import type { IssueItem } from './types'

export interface CreateIssueBody {
  title: string
  description: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  relatedService: 'ASSET_SERVICE' | 'HELPDESK_SERVICE' | 'NOTIFICATION_SERVICE' | 'AUTH_SERVICE' | 'UPCOMING_PROJECT'
  assetId?: number
  issueMasterId?: number
}

export function listIssues(token: string): Promise<IssueItem[]> {
  return apiJson<IssueItem[]>(url('helpdesk', '/api/helpdesk/issues'), { token })
}

export function createIssue(token: string, body: CreateIssueBody): Promise<IssueItem> {
  return apiJson<IssueItem>(url('helpdesk', '/api/helpdesk/issues'), {
    method: 'POST',
    token,
    body: JSON.stringify(body),
  })
}
