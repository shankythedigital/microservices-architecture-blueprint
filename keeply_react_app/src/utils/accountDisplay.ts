export function accountInitial(username: string | null, userId: number | null): string {
  if (username && username.length > 0) {
    const c = username.replace(/[^a-zA-Z0-9]/g, '').charAt(0)
    if (c) return c.toUpperCase()
  }
  if (userId != null) return String(userId % 10)
  return '?'
}
