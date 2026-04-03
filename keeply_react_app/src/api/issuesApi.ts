import { url } from '../config'
import type { RelatedService } from '../constants/helpdesk'
import { apiJson } from './http'
import type { IssueItem } from './types'

/** helpdesk-service GET /api/helpdesk/issue-master */
export type IssueMasterItem = {
  id?: number
  issueTitle?: string
  issueDescription?: string
  categoryId?: number
  subCategoryId?: number
  componentId?: number
  sparePartId?: number
}

export function listIssueMasters(token: string): Promise<IssueMasterItem[]> {
  return apiJson<IssueMasterItem[]>(url('helpdesk', '/api/helpdesk/issue-master'), { token })
}

/** POST /api/helpdesk/issue-master — requires at least one of categoryId, subCategoryId, componentId, sparePartId */
export type CreateIssueMasterBody = {
  issueTitle: string
  issueDescription?: string
  categoryId?: number
  subCategoryId?: number
  componentId?: number
  sparePartId?: number
}

export function createIssueMaster(token: string, body: CreateIssueMasterBody): Promise<IssueMasterItem> {
  return apiJson<IssueMasterItem>(url('helpdesk', '/api/helpdesk/issue-master'), {
    method: 'POST',
    token,
    body: JSON.stringify(body),
  })
}

/** Custom issue requires title + description; catalog issue uses issueMasterId (see IssueService). */
export type CreateIssueBody = {
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  relatedService: RelatedService
  assetId?: number
  componentId?: number
  sparePartId?: number
  issueMasterId?: number
  title?: string
  description?: string
}

/** All issues (admin-style listing). Prefer {@link listMyIssues} in the consumer app. */
export function listIssues(token: string): Promise<IssueItem[]> {
  return apiJson<IssueItem[]>(url('helpdesk', '/api/helpdesk/issues'), { token })
}

/** Issues raised by the signed-in user — `GET /api/helpdesk/issues/my-issues`. */
export function listMyIssues(token: string): Promise<IssueItem[]> {
  return apiJson<IssueItem[]>(url('helpdesk', '/api/helpdesk/issues/my-issues'), { token })
}

export function getIssue(token: string, id: number): Promise<IssueItem> {
  return apiJson<IssueItem>(url('helpdesk', `/api/helpdesk/issues/${id}`), { token })
}

/**
 * POST /api/helpdesk/issues — creates a ticket; helpdesk-service sets `reportedBy` / `createdBy`
 * from the JWT subject (user id) so `/my-issues` returns it for the same user.
 */
export function createIssue(token: string, body: CreateIssueBody): Promise<IssueItem> {
  return apiJson<IssueItem>(url('helpdesk', '/api/helpdesk/issues'), {
    method: 'POST',
    token,
    body: JSON.stringify(body),
  })
}
