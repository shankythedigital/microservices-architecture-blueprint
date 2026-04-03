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
  updatedAt?: string
  resolvedAt?: string
  resolution?: string
  reportedBy?: string
  assignedTo?: string
  assetId?: number
  issueMasterId?: number
  /** Auth DB user id (numeric); same as JWT subject. */
  loginUserId?: number
}

export interface HelpdeskQueryItem {
  id: number
  question?: string
  answer?: string
  status?: string
  relatedService?: string
  createdAt?: string
  answeredAt?: string
  loginUserId?: number
}

/** Matches auth-service `UserProfileResponse` (decrypted for `GET /api/auth/profile/me`). */
export interface UserProfileResponse {
  userId?: number
  username?: string
  email?: string
  mobile?: string
  firstName?: string
  lastName?: string
  employeeId?: string
  /** Optional avatar URL when auth-service exposes it */
  profilePhotoUrl?: string | null
}
