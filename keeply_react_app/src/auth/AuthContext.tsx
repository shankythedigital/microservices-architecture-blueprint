import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { fetchMyProfile, loginOtp, loginPassword } from '../api/authApi'
import { ApiError } from '../api/http'
import type { UserProfileResponse } from '../api/types'
import { userIdFromAccessToken } from './jwtClaims'

const STORAGE_KEY = 'keeply_access_token'
const USER_KEY = 'keeply_user_id'

type AuthState = {
  token: string | null
  userId: number | null
  /** Decrypted profile from `GET /api/auth/profile/me`; null if not loaded or unavailable */
  profile: UserProfileResponse | null
  profileLoading: boolean
  refreshProfile: () => Promise<void>
  login: (username: string, password: string) => Promise<void>
  loginWithOtp: (mobile: string, otp: string) => Promise<void>
  logout: () => void
  error: string | null
  clearError: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(STORAGE_KEY))
  const [userId, setUserId] = useState<number | null>(() => {
    const r = localStorage.getItem(USER_KEY)
    return r ? Number(r) : null
  })
  const [profile, setProfile] = useState<UserProfileResponse | null>(null)
  const [profileLoading, setProfileLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadProfile = useCallback(async (accessToken: string | null) => {
    if (!accessToken) {
      setProfile(null)
      setProfileLoading(false)
      return
    }
    setProfileLoading(true)
    try {
      const p = await fetchMyProfile(accessToken)
      setProfile(p)
    } catch {
      setProfile(null)
    } finally {
      setProfileLoading(false)
    }
  }, [])

  useEffect(() => {
    loadProfile(token)
  }, [token, loadProfile])

  const refreshProfile = useCallback(async () => {
    await loadProfile(token)
  }, [token, loadProfile])

  const persistSession = useCallback((res: { accessToken: string; userId?: number | null }) => {
    localStorage.setItem(STORAGE_KEY, res.accessToken)
    if (res.userId != null) {
      localStorage.setItem(USER_KEY, String(res.userId))
      setUserId(res.userId)
    } else {
      setUserId(null)
    }
    setToken(res.accessToken)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUserId(null)
    setProfile(null)
  }, [])

  const login = useCallback(
    async (username: string, password: string) => {
      setError(null)
      try {
        const res = await loginPassword(username, password)
        persistSession(res)
      } catch (e) {
        const msg = e instanceof ApiError ? e.message : 'Login failed'
        setError(msg)
        throw e
      }
    },
    [persistSession],
  )

  const loginWithOtp = useCallback(
    async (mobile: string, otp: string) => {
      setError(null)
      try {
        const res = await loginOtp(mobile, otp)
        persistSession(res)
      } catch (e) {
        const msg = e instanceof ApiError ? e.message : 'OTP login failed'
        setError(msg)
        throw e
      }
    },
    [persistSession],
  )

  const effectiveUserId = useMemo(() => {
    if (userId != null) return userId
    return userIdFromAccessToken(token)
  }, [userId, token])

  const value = useMemo(
    () => ({
      token,
      /** From login payload, or parsed from access token when missing */
      userId: effectiveUserId,
      profile,
      profileLoading,
      refreshProfile,
      login,
      loginWithOtp,
      logout,
      error,
      clearError: () => setError(null),
    }),
    [
      token,
      effectiveUserId,
      profile,
      profileLoading,
      refreshProfile,
      login,
      loginWithOtp,
      logout,
      error,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
