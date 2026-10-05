// All calls go to /api/... on the same origin. Locally the gateway container
// routes them; in AKS the Ingress (Application Gateway) does.
const TOKEN_KEY = 'ch_token'

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined })
  } catch {
    throw new ApiError('Can’t reach CraftHinge. Check your connection and try again.', 0)
  }
  if (res.status === 401 && token) {
    tokenStore.clear()
    window.dispatchEvent(new Event('ch-logout'))
  }
  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
  if (!res.ok) {
    const msg = data?.detail || (res.status === 400 ? 'Some details are missing or invalid.' : `Something went wrong (${res.status}).`)
    throw new ApiError(msg, res.status)
  }
  return data
}

export const money = (n) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(n || 0))

export const STATUS_LABEL = {
  PENDING_PAYMENT: 'Processing payment',
  PAYMENT_FAILED: 'Payment failed',
  PLACED: 'Order placed',
  ACCEPTED: 'Accepted by restaurant',
  PREPARING: 'Being prepared',
  READY_FOR_PICKUP: 'Ready for pickup',
  OUT_FOR_DELIVERY: 'On the way',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
}
