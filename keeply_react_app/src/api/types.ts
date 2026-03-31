export interface AuthResponse {
  accessToken: string
  refreshToken?: string
  expiresIn: number
  userId?: number
  sessionId?: number
  roles?: string[]
}

export interface ResponseWrapper<T> {
  success: boolean
  message: string
  data: T
}

export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface NotificationItem {
  id: number
  title?: string
  message?: string
  templateCode?: string
  createdAt?: string
  read?: boolean
  priority?: string
}

export interface IssueItem {
  id: number
  title?: string
  description?: string
  status?: string
  priority?: string
  relatedService?: string
  createdAt?: string
}
