import type { IncomingMessage, ServerResponse } from 'node:http'

type ProxyServerLike = {
  on(
    event: 'error',
    listener: (err: Error, req: IncomingMessage, res: ServerResponse | { destroy: () => void }) => void,
  ): unknown
}

function isWritableHttpResponse(
  res: ServerResponse | { destroy: () => void },
): res is ServerResponse {
  return typeof (res as ServerResponse).setHeader === 'function'
}

/**
 * When notification-service is not running, the Vite proxy gets ECONNREFUSED.
 * Respond with the same JSON shape the React app expects so the UI keeps working
 * and the dev server does not leave requests hanging. Real service traffic is unchanged.
 */
export function attachNotificationProxyFallback(proxy: ProxyServerLike) {
  proxy.on('error', (err, req, res) => {
    if (!shouldHandleDevFallback(err)) return
    if (!req || !res) return
    sendFallbackForNotifications(req, res)
  })
}

function shouldHandleDevFallback(err: Error & { code?: string }): boolean {
  const code = err.code
  return (
    code === 'ECONNREFUSED' ||
    code === 'ECONNRESET' ||
    code === 'ETIMEDOUT' ||
    code === 'ENOTFOUND'
  )
}

function sendFallbackForNotifications(
  req: IncomingMessage,
  res: ServerResponse | { destroy: () => void },
) {
  if (!isWritableHttpResponse(res)) {
    res.destroy()
    return
  }
  if (res.writableEnded || res.headersSent) return

  const rawPath = (req.url ?? '').split('?')[0] ?? ''
  if (!rawPath.startsWith('/api/notifications')) return

  const method = req.method ?? 'GET'
  const json = (status: number, body: unknown) => {
    res.statusCode = status
    res.setHeader('Content-Type', 'application/json')
    res.end(JSON.stringify(body))
  }

  if (method === 'GET' && isNotificationCountPath(rawPath)) {
    json(200, {
      success: true,
      message: 'Notification service unavailable (local dev fallback)',
      data: { count: 0 },
    })
    return
  }

  if (method === 'GET' && isNotificationListPath(rawPath)) {
    json(200, {
      success: true,
      message: 'Notification service unavailable (local dev fallback)',
      data: [],
    })
    return
  }

  if (method === 'PUT' && rawPath.includes('/read/')) {
    json(200, { success: true, message: 'ok', data: null })
    return
  }

  json(503, {
    success: false,
    message: 'Notification service unavailable',
    data: null,
  })
}

function isNotificationCountPath(path: string): boolean {
  return path === '/api/notifications/count' || path.startsWith('/api/notifications/count/')
}

function isNotificationListPath(path: string): boolean {
  return path === '/api/notifications/list' || path.startsWith('/api/notifications/list/')
}
