import { useCallback, useMemo, useSyncExternalStore } from 'react'

const PREFIX = 'keeply_pref_'

export type KeeplyTheme = 'system' | 'light' | 'dark'

export type KeeplyPreferences = {
  pushNotifications: boolean
  emailReminders: boolean
  assetWarrantyAlerts: boolean
  weeklyDigest: boolean
  helpdeskActivityAlerts: boolean
  compactUi: boolean
  reduceMotion: boolean
  showListThumbnails: boolean
  theme: KeeplyTheme
}

const DEFAULTS: KeeplyPreferences = {
  pushNotifications: true,
  emailReminders: true,
  assetWarrantyAlerts: true,
  weeklyDigest: false,
  helpdeskActivityAlerts: true,
  compactUi: false,
  reduceMotion: false,
  showListThumbnails: true,
  theme: 'system',
}

const KEYS = {
  pushNotifications: `${PREFIX}push`,
  emailReminders: `${PREFIX}email`,
  assetWarrantyAlerts: `${PREFIX}warranty`,
  weeklyDigest: `${PREFIX}digest`,
  helpdeskActivityAlerts: `${PREFIX}helpdesk_activity`,
  compactUi: `${PREFIX}compact`,
  reduceMotion: `${PREFIX}reduce_motion`,
  showListThumbnails: `${PREFIX}thumbnails`,
  theme: `${PREFIX}theme`,
} as const

function readBool(key: string, fallback: boolean): boolean {
  try {
    const v = localStorage.getItem(key)
    if (v === null) return fallback
    return v === '1' || v === 'true'
  } catch {
    return fallback
  }
}

function writeBool(key: string, value: boolean) {
  try {
    localStorage.setItem(key, value ? '1' : '0')
  } catch {
    /* ignore */
  }
}

function readTheme(key: string, fallback: KeeplyTheme): KeeplyTheme {
  try {
    const v = localStorage.getItem(key)
    if (v === 'light' || v === 'dark' || v === 'system') return v
    return fallback
  } catch {
    return fallback
  }
}

function writeTheme(key: string, value: KeeplyTheme) {
  try {
    localStorage.setItem(key, value)
  } catch {
    /* ignore */
  }
}

let prefListeners = new Set<() => void>()

function emitPrefChange() {
  prefListeners.forEach((fn) => fn())
}

function subscribe(fn: () => void) {
  prefListeners.add(fn)
  return () => {
    prefListeners.delete(fn)
  }
}

function snapshot(): KeeplyPreferences {
  return {
    pushNotifications: readBool(KEYS.pushNotifications, DEFAULTS.pushNotifications),
    emailReminders: readBool(KEYS.emailReminders, DEFAULTS.emailReminders),
    assetWarrantyAlerts: readBool(KEYS.assetWarrantyAlerts, DEFAULTS.assetWarrantyAlerts),
    weeklyDigest: readBool(KEYS.weeklyDigest, DEFAULTS.weeklyDigest),
    helpdeskActivityAlerts: readBool(KEYS.helpdeskActivityAlerts, DEFAULTS.helpdeskActivityAlerts),
    compactUi: readBool(KEYS.compactUi, DEFAULTS.compactUi),
    reduceMotion: readBool(KEYS.reduceMotion, DEFAULTS.reduceMotion),
    showListThumbnails: readBool(KEYS.showListThumbnails, DEFAULTS.showListThumbnails),
    theme: readTheme(KEYS.theme, DEFAULTS.theme),
  }
}

export function useKeeplyPreferences() {
  const prefs = useSyncExternalStore(subscribe, snapshot, snapshot)

  const setPushNotifications = useCallback((v: boolean) => {
    writeBool(KEYS.pushNotifications, v)
    emitPrefChange()
  }, [])
  const setEmailReminders = useCallback((v: boolean) => {
    writeBool(KEYS.emailReminders, v)
    emitPrefChange()
  }, [])
  const setAssetWarrantyAlerts = useCallback((v: boolean) => {
    writeBool(KEYS.assetWarrantyAlerts, v)
    emitPrefChange()
  }, [])
  const setWeeklyDigest = useCallback((v: boolean) => {
    writeBool(KEYS.weeklyDigest, v)
    emitPrefChange()
  }, [])
  const setHelpdeskActivityAlerts = useCallback((v: boolean) => {
    writeBool(KEYS.helpdeskActivityAlerts, v)
    emitPrefChange()
  }, [])
  const setCompactUi = useCallback((v: boolean) => {
    writeBool(KEYS.compactUi, v)
    emitPrefChange()
  }, [])
  const setReduceMotion = useCallback((v: boolean) => {
    writeBool(KEYS.reduceMotion, v)
    emitPrefChange()
  }, [])
  const setShowListThumbnails = useCallback((v: boolean) => {
    writeBool(KEYS.showListThumbnails, v)
    emitPrefChange()
  }, [])
  const setTheme = useCallback((v: KeeplyTheme) => {
    writeTheme(KEYS.theme, v)
    emitPrefChange()
  }, [])

  return useMemo(
    () => ({
      ...prefs,
      setPushNotifications,
      setEmailReminders,
      setAssetWarrantyAlerts,
      setWeeklyDigest,
      setHelpdeskActivityAlerts,
      setCompactUi,
      setReduceMotion,
      setShowListThumbnails,
      setTheme,
    }),
    [
      prefs,
      setPushNotifications,
      setEmailReminders,
      setAssetWarrantyAlerts,
      setWeeklyDigest,
      setHelpdeskActivityAlerts,
      setCompactUi,
      setReduceMotion,
      setShowListThumbnails,
      setTheme,
    ],
  )
}
