import { url } from '../config'
import { apiJson } from './http'
import type { IssueItem } from './types'

export function listIssues(token: string): Promise<IssueItem[]> {
  return apiJson<IssueItem[]>(url('helpdesk', '/api/helpdesk/issues'), { token })
}
