import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, money, STATUS_LABEL } from '../api'
import { useAuth } from '../auth'

const STEPS = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED']

export default function OrderTracking() {
  const { id } = useParams()
  const { user } = useAuth()
  const [order, setOrder] = useState(null)
  const [notes, setNotes] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    let stop = false
    const load = async () => {
      try {
        const o = await api(`/api/orders/${id}`)
        if (stop) return
        setOrder(o)
        if (user?.role === 'CUSTOMER') {
          const n = await api('/api/notifications/me').catch(() => [])
          if (!stop) setNotes(n.filter((x) => x.orderId === o.id))
        }
      } catch (e) {
        if (!stop) setError(e.message)
      }
    }
    load()
    const timer = setInterval(load, 5000) // simple polling; websockets would be the next step
    return () => { stop = true; clearInterval(timer) }
  }, [id, user])

  if (error) return <div className="page section"><p className="banner-error">{error}</p></div>
  if (!order) return <div className="page section"><p className="muted">Loading order…</p></div>

  const reached = new Set(order.history.map((h) => h.status))
  const when = Object.fromEntries(order.history.map((h) => [h.status, h.at]))
  const ended = ['CANCELLED', 'PAYMENT_FAILED'].includes(order.status)

  return (
    <div className="page section tracking">
      <div>
        <h1>Order #{order.id}</h1>
        <p className="muted">{order.restaurantName} · to {order.deliveryAddress}</p>
        {ended ? (
          <p className={`status status-${order.status}`}>{STATUS_LABEL[order.status]}</p>
        ) : (
          <ol className="timeline">
            {STEPS.map((s) => (
              <li key={s} className={order.status === s ? 'done now' : reached.has(s) ? 'done' : ''} aria-current={order.status === s ? 'step' : undefined}>
                <span className="dot" aria-hidden="true" />
                <span>{STATUS_LABEL[s]}</span>
                {when[s] && <span className="muted small">{new Date(when[s]).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}</span>}
              </li>
            ))}
          </ol>
        )}
        {notes.length > 0 && (
          <section className="notes">
            <h2>Updates</h2>
            <ul>{notes.map((n) => <li key={n.id}>{n.message}</li>)}</ul>
          </section>
        )}
      </div>
      <aside className="panel">
        <h2>Receipt</h2>
        <ul className="receipt">
          {order.items.map((i) => <li key={i.menuItemId}><span>{i.quantity} × {i.name}</span><span>{money(i.unitPrice * i.quantity)}</span></li>)}
          <li><span>Delivery</span><span>{money(order.deliveryFee)}</span></li>
          <li className="total"><span>Total</span><span>{money(order.total)}</span></li>
        </ul>
      </aside>
    </div>
  )
}
