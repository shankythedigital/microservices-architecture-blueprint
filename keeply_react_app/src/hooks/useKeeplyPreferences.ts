import { useCallback, useMemo, useSyncExternalStore } from 'react'

const PREFIX = 'keeply_pref_'

export type KeeplyPreferences = {
  pushNotifications: boolean
  emailReminders: boolean
  assetWarrantyAlerts: boolean
  weeklyDigest: boolean
}

const DEFAULTS: KeeplyPreferences = {
  pushNotifications: true,
  emailReminders: true,
  assetWarrantyAlerts: true,
  weeklyDigest: false,
}

const KEYS = {
  pushNotifications: `${PREFIX}push`,
  emailReminders: `${PREFIX}email`,
  assetWarrantyAlerts: `${PREFIX}warranty`,
  weeklyDigest: `${PREFIX}digest`,
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

  return useMemo(
    () => ({
      ...prefs,
      setPushNotifications,
      setEmailReminders,
      setAssetWarrantyAlerts,
      setWeeklyDigest,
    }),
    [
      prefs,
      setPushNotifications,
      setEmailReminders,
      setAssetWarrantyAlerts,
      setWeeklyDigest,
    ],
  )
}
