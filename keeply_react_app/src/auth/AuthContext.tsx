import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchMyProfile, loginOtp, loginPassword, logoutOnServer } from '../api/authApi'
import type { AuthResponse } from '../api/types'
import { ApiError, resetSessionExpiredLatch, SESSION_EXPIRED_EVENT } from '../api/http'
import type { UserProfileResponse } from '../api/types'
import { userIdFromAccessToken } from './jwtClaims'

const STORAGE_KEY = 'keeply_access_token'
const REFRESH_KEY = 'keeply_refresh_token'
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
  logout: () => Promise<void>
  error: string | null
  clearError: () => void
  /** Clear tokens locally without calling the server (e.g. session expired). */
  logoutLocal: () => void
}

const AuthContext = createContext<AuthState | null>(null)

function SessionExpiryListener({ onExpired }: { onExpired: () => void }) {
  const navigate = useNavigate()

  useEffect(() => {
    const handler = () => {
      onExpired()
      const path = `${window.location.pathname}${window.location.search}`
      navigate('/login', {
        replace: true,
        state: { from: path === '/login' ? '/home' : path, sessionExpired: true },
      })
    }
    window.addEventListener(SESSION_EXPIRED_EVENT, handler)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handler)
  }, [navigate, onExpired])

  return null
}

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

  const persistSession = useCallback((res: AuthResponse) => {
    resetSessionExpiredLatch()
    localStorage.setItem(STORAGE_KEY, res.accessToken)
    if (res.refreshToken) {
      localStorage.setItem(REFRESH_KEY, res.refreshToken)
    } else {
      localStorage.removeItem(REFRESH_KEY)
    }
    if (res.userId != null) {
      localStorage.setItem(USER_KEY, String(res.userId))
      setUserId(res.userId)
    } else {
      localStorage.removeItem(USER_KEY)
      setUserId(null)
    }
    setToken(res.accessToken)
  }, [])

  const logoutLocal = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUserId(null)
    setProfile(null)
    setError(null)
    resetSessionExpiredLatch()
  }, [])

  const logout = useCallback(async () => {
    const at = localStorage.getItem(STORAGE_KEY)
    const rt = localStorage.getItem(REFRESH_KEY)
    await logoutOnServer(at, rt)
    logoutLocal()
  }, [logoutLocal])

  useEffect(() => {
    if (token) resetSessionExpiredLatch()
  }, [token])

  /**
   * Keep `keeply_user_id` aligned with the access token. Stale storage (wrong tab, old refresh,
   * manual edits) caused `/user-asset-links/user/{id}/assets` to 403 while the JWT was valid.
   */
  useEffect(() => {
    if (!token) return
    const fromToken = userIdFromAccessToken(token)
    if (fromToken == null) return
    const storedRaw = localStorage.getItem(USER_KEY)
    let storedNum: number | null = null
    if (storedRaw != null && storedRaw !== '') {
      const n = Number(storedRaw)
      if (Number.isFinite(n)) storedNum = n
    }
    if (storedNum == null || storedNum !== fromToken) {
      localStorage.setItem(USER_KEY, String(fromToken))
      setUserId(fromToken)
    }
  }, [token])

  const login = useCallback(
    async (username: string, password: string) => {
      setError(null)
      try {
        const res = await loginPassword(username, password)
        persistSession(res)
      } catch (e) {
        const msg =
          e instanceof ApiError
            ? e.message
            : e instanceof Error
              ? e.message
              : 'Login failed'
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
        const msg =
          e instanceof ApiError
            ? e.message
            : e instanceof Error
              ? e.message
              : 'OTP login failed'
        setError(msg)
        throw e
      }
    },
    [persistSession],
  )

  /** Prefer JWT `uid` / numeric `sub` so API paths always match the bearer token. */
  const effectiveUserId = useMemo(() => {
    if (!token) return null
    const fromToken = userIdFromAccessToken(token)
    if (fromToken != null) return fromToken
    return userId != null && Number.isFinite(userId) ? userId : null
  }, [token, userId])

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
      logoutLocal,
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
      logoutLocal,
      error,
    ],
  )

  return (
    <AuthContext.Provider value={value}>
      <SessionExpiryListener onExpired={logoutLocal} />
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
