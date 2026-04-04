import { useEffect } from 'react'
import { useKeeplyPreferences } from '../hooks/useKeeplyPreferences'

/**
 * Applies Keeply local preferences (theme, compact UI, reduced motion) to {@code document.documentElement}.
 * Mount once under {@link AuthProvider} so they apply across the whole app.
 */
export function KeeplyRootPreferences() {
  const prefs = useKeeplyPreferences()

  useEffect(() => {
    const applyTheme = () => {
      const dark =
        prefs.theme === 'dark' ||
        (prefs.theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)
      document.documentElement.dataset.keeplyTheme = dark ? 'dark' : 'light'
    }
    applyTheme()
    if (prefs.theme !== 'system') return
    const mq = window.matchMedia('(prefers-color-scheme: dark)')
    mq.addEventListener('change', applyTheme)
    return () => mq.removeEventListener('change', applyTheme)
  }, [prefs.theme])

  useEffect(() => {
    document.documentElement.dataset.keeplyCompact = prefs.compactUi ? '1' : '0'
  }, [prefs.compactUi])

  useEffect(() => {
    document.documentElement.dataset.keeplyReducedMotion = prefs.reduceMotion ? '1' : '0'
  }, [prefs.reduceMotion])

  return null
}
